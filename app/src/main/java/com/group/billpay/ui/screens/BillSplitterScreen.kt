package com.group.billpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group.billpay.data.model.*
import com.group.billpay.ui.theme.BillpayTheme
import com.group.billpay.ui.viewmodel.BillSplitterViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillSplitterScreen(
    viewModel: BillSplitterViewModel = viewModel(),
    onNavigateToBill: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var newParticipantName by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Splitter") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                ParticipantInputSection(
                    participants = uiState.participants,
                    newParticipantName = newParticipantName,
                    onNewParticipantChange = { newParticipantName = it },
                    onAddParticipant = {
                        viewModel.addParticipant(newParticipantName)
                        newParticipantName = ""
                    },
                    onRemoveParticipant = { participant ->
                        viewModel.removeParticipant(participant)
                    }
                )
            }

            item {
                BillListSection(
                    bills = uiState.bills,
                    onAddBillClicked = {
                        val newBillId = viewModel.addBill()
                        if (newBillId != -1L) {
                            onNavigateToBill(newBillId)
                        }
                    },
                    onBillClicked = { bill ->
                        onNavigateToBill(bill.id)
                    },
                    onDeleteBill = { bill ->
                        viewModel.removeBill(bill)
                    }
                )
            }

            item {
                SummarySection(summary = uiState.summary)
            }

            item {
                Button(
                    onClick = { viewModel.exportToPdf(context) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.summary.isNotEmpty()
                ) {
                    Text("Export ke PDF")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParticipantInputSection(
    participants: List<Participant>,
    newParticipantName: String,
    onNewParticipantChange: (String) -> Unit,
    onAddParticipant: () -> Unit,
    onRemoveParticipant: (Participant) -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Peserta", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                participants.forEach { participant ->
                    InputChip(
                        selected = false,
                        onClick = { onRemoveParticipant(participant) },
                        label = { Text(participant.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remove ${participant.name}",
                                Modifier.size(InputChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newParticipantName,
                    onValueChange = onNewParticipantChange,
                    label = { Text("Nama peserta baru") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = onAddParticipant, enabled = newParticipantName.isNotBlank()) {
                    Text("Tambah")
                }
            }
        }
    }
}

@Composable
fun BillListSection(
    bills: List<Bill>,
    onAddBillClicked: () -> Unit,
    onBillClicked: (Bill) -> Unit,
    onDeleteBill: (Bill) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Daftar Bill/Struk", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            if (bills.isEmpty()) {
                Text(
                    "Belum ada bill. Tekan tombol untuk menambah.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                bills.forEach { bill ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBillClicked(bill) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bill.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDeleteBill(bill) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Bill")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onAddBillClicked,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("+ Tambah Bill")
            }
        }
    }
}


@Composable
fun SummarySection(summary: Map<Participant, BalanceSummary>) {
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            // V V V PERBAIKAN DI SINI V V V
            maximumFractionDigits = 0 // <-- Hapus semua angka di belakang koma
            // ^ ^ ^ -------------------- ^ ^ ^
        }
    }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Hasil Akhir Pembagian", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            // Header Tabel
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 8.dp)
            ) {
                Text("Peserta", Modifier.weight(1.5f).padding(start = 8.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Tagihan", Modifier.weight(1.1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Bayar", Modifier.weight(1.1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Saldo", Modifier.weight(1.3f).padding(end = 8.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            // Isi Tabel
            if (summary.isEmpty()) {
                Text(
                    "Tambahkan bill dan peserta untuk melihat ringkasan.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                summary.forEach { (participant, balance) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(participant.name, Modifier.weight(1.5f).padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = rupiahFormat.format(balance.totalBill),
                            modifier = Modifier.weight(1.1f),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = rupiahFormat.format(balance.totalPaid),
                            modifier = Modifier.weight(1.1f),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = rupiahFormat.format(balance.finalBalance),
                            modifier = Modifier.weight(1.3f).padding(end = 8.dp),
                            color = when {
                                balance.finalBalance < 0 -> MaterialTheme.colorScheme.error
                                balance.finalBalance > 0 -> Color(0xFF006400) // DarkGreen
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Divider()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BillSplitterScreenPreview() {
    BillpayTheme {
        BillSplitterScreen(onNavigateToBill = {})
    }
}