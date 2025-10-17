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

// Perubahan: Tambahkan parameter navController untuk navigasi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillSplitterScreen(
    viewModel: BillSplitterViewModel = viewModel(),
    onNavigateToBill: (Long) -> Unit // Fungsi untuk berpindah layar
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var newParticipantName by remember { mutableStateOf("") }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        viewModel.addBill()
                        // TODO: Navigasi ke bill yang baru dibuat
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
                    onClick = { /* TODO: Panggil fungsi export PDF */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export ke PDF")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantInputSection(
    participants: List<Participant>,
    newParticipantName: String,
    onNewParticipantChange: (String) -> Unit,
    onAddParticipant: () -> Unit,
    onRemoveParticipant: (Participant) -> Unit
) {
    // ... Tidak ada perubahan di fungsi ini ...
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Peserta", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                Button(onClick = onAddParticipant) {
                    Text("Tambah")
                }
            }
        }
    }
}

// Perubahan besar: Fungsi ini sekarang lebih interaktif
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
    // ... Tidak ada perubahan di fungsi ini ...
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Hasil Akhir Pembagian", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.background(Color.LightGray.copy(alpha = 0.3f))) {
                Text("Peserta", Modifier.weight(1.5f).padding(8.dp), fontWeight = FontWeight.Bold)
                Text("Tagihan", Modifier.weight(1f).padding(8.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Bayar", Modifier.weight(1f).padding(8.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                Text("Saldo", Modifier.weight(1f).padding(8.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }

            summary.forEach { (participant, balance) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(participant.name, Modifier.weight(1.5f).padding(8.dp))
                    Text(rupiahFormat.format(balance.totalBill), Modifier.weight(1f).padding(8.dp), textAlign = TextAlign.End)
                    Text(rupiahFormat.format(balance.totalPaid), Modifier.weight(1f).padding(8.dp), textAlign = TextAlign.End)
                    Text(
                        text = rupiahFormat.format(balance.finalBalance),
                        modifier = Modifier.weight(1f).padding(8.dp),
                        color = if (balance.finalBalance < 0) MaterialTheme.colorScheme.error else Color(0xFF006400), // DarkGreen
                        textAlign = TextAlign.End
                    )
                }
                Divider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BillSplitterScreenPreview() {
    BillpayTheme {
        // Preview tidak bisa menangani navigasi, jadi kita buat simpel
        BillSplitterScreen(onNavigateToBill = {})
    }
}