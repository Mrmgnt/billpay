package com.group.billpay.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.group.billpay.data.model.*
import com.group.billpay.ui.viewmodel.BillSplitterViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailScreen(
    billId: Long,
    viewModel: BillSplitterViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bill = uiState.bills.find { it.id == billId }
    val itemsForBill = uiState.items.filter { it.billId == billId }
    val participants = uiState.participants
    val shares = uiState.shares

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bill?.name ?: "Detail Bill") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (bill == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Bill tidak ditemukan.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    BillHeaderCard(
                        bill = bill,
                        participants = participants,
                        onUpdate = { newName, newTax, payerId, totalPaid ->
                            viewModel.updateBillHeader(billId, newName, newTax, payerId, totalPaid)
                        }
                    )
                }
                item {
                    ItemSection(
                        items = itemsForBill,
                        participants = participants,
                        shares = shares,
                        onAddItem = { name, price, qty -> viewModel.addItemToBill(billId, name, price, qty) },
                        onRemoveItem = { item -> viewModel.removeItem(item) },
                        onUpdateSharing = { itemId, pIds -> viewModel.updateItemSharing(itemId, pIds) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillHeaderCard(
    bill: Bill,
    participants: List<Participant>,
    onUpdate: (newName: String, newTax: Double, payerId: Long?, totalPaid: Double) -> Unit
) {
    var billName by remember(bill.name) { mutableStateOf(bill.name) }
    var tax by remember(bill.taxPercentage) { mutableStateOf(bill.taxPercentage.toString()) }
    var totalPaid by remember(bill.totalPaid) { mutableStateOf(if (bill.totalPaid > 0) bill.totalPaid.toString() else "") }
    var selectedPayer by remember(bill.payerId) { mutableStateOf(participants.find { it.id == bill.payerId }) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    fun triggerUpdate() {
        onUpdate(billName, tax.toDoubleOrNull() ?: 0.0, selectedPayer?.id, totalPaid.toDoubleOrNull() ?: 0.0)
    }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = billName,
                onValueChange = {
                    billName = it
                    triggerUpdate()
                },
                label = { Text("Nama Bill") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedPayer?.name ?: "— Pilih Payer —",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pembayar (Payer)")},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                        participants.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = {
                                    selectedPayer = p
                                    isDropdownExpanded = false
                                    triggerUpdate()
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = totalPaid,
                    onValueChange = {
                        totalPaid = it
                        triggerUpdate()
                    },
                    label = { Text("Total Dibayar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = tax,
                onValueChange = {
                    tax = it
                    triggerUpdate()
                },
                label = { Text("PPN (%)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )
        }
    }
}

@Composable
fun ItemSection(
    items: List<Item>,
    participants: List<Participant>,
    shares: List<ItemShare>,
    onAddItem: (name: String, price: Double, quantity: Int) -> Unit,
    onRemoveItem: (Item) -> Unit,
    onUpdateSharing: (itemId: Long, participantIds: List<Long>) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Daftar Item", style = MaterialTheme.typography.titleMedium)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Nama Item") }, modifier = Modifier.weight(2f), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                OutlinedTextField(value = itemPrice, onValueChange = { itemPrice = it }, label = { Text("Harga") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.weight(1.5f))
                OutlinedTextField(value = itemQty, onValueChange = { itemQty = it }, label = { Text("Qty") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), modifier = Modifier.weight(0.8f))
            }
            Button(
                onClick = {
                    val price = itemPrice.toDoubleOrNull()
                    val qty = itemQty.toIntOrNull()
                    if (itemName.isNotBlank() && price != null && qty != null && price > 0 && qty > 0) {
                        onAddItem(itemName, price, qty)
                        itemName = ""; itemPrice = ""; itemQty = "1"
                    }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = itemName.isNotBlank() && (itemPrice.toDoubleOrNull() ?: 0.0) > 0 && (itemQty.toIntOrNull() ?: 0) > 0
            ) {
                Text("+ Tambah Item")
            }

            if (items.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                items.forEach { item ->
                    ItemEntry(
                        item = item,
                        participants = participants,
                        itemShares = shares.filter { it.itemId == item.id },
                        onRemoveItem = onRemoveItem,
                        onUpdateSharing = onUpdateSharing
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemEntry(
    item: Item,
    participants: List<Participant>,
    itemShares: List<ItemShare>,
    onRemoveItem: (Item) -> Unit,
    onUpdateSharing: (itemId: Long, participantIds: List<Long>) -> Unit
) {
    val rupiahFormat = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply { maximumFractionDigits = 0 } }
    val isSplitEvenly = itemShares.size == participants.size && participants.isNotEmpty()

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${item.quantity}x ${item.name}", style = MaterialTheme.typography.bodyLarge)
                Text(rupiahFormat.format(item.price), style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { onRemoveItem(item) }) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus Item")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = isSplitEvenly,
                onCheckedChange = { isChecked ->
                    val newSharers = if (isChecked) participants.map { it.id } else emptyList()
                    onUpdateSharing(item.id, newSharers)
                }
            )
            Text("Bagi Rata (Untuk Semua Peserta)")
        }

        if (!isSplitEvenly) {
            FlowRow(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                participants.forEach { participant ->
                    val isSelected = itemShares.any { it.participantId == participant.id }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val currentSharers = itemShares.map { it.participantId }.toMutableSet()
                            if (isSelected) currentSharers.remove(participant.id) else currentSharers.add(participant.id)
                            onUpdateSharing(item.id, currentSharers.toList())
                        },
                        label = { Text(participant.name) }
                    )
                }
            }
        }
        Divider(modifier = Modifier.padding(top = 12.dp))
    }
}