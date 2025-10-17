package com.group.billpay.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.group.billpay.data.model.*
import com.group.billpay.data.repository.FileSessionRepository
import com.group.billpay.data.repository.SessionData
import com.group.billpay.domain.calculator.BillCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BillSplitterUiState(
    val sessionName: String = "Sesi Nongkrong",
    val participants: List<Participant> = emptyList(),
    val bills: List<Bill> = emptyList(),
    val items: List<Item> = emptyList(),
    val shares: List<ItemShare> = emptyList(),
    val summary: Map<Participant, BalanceSummary> = emptyMap(),
    val isLoading: Boolean = true
)

class BillSplitterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileSessionRepository(application)
    private val _sessionData = MutableStateFlow<SessionData?>(null)

    private val _uiState = MutableStateFlow(BillSplitterUiState())
    val uiState: StateFlow<BillSplitterUiState> = _uiState.asStateFlow()

    init {
        loadDataFromFile()
        observeSessionData()
    }

    private fun observeSessionData() {
        viewModelScope.launch {
            _sessionData.filterNotNull().collect { data ->
                val summaryMapById = BillCalculator.calculateSessionSummary(
                    participants = data.participants,
                    bills = data.bills,
                    items = data.items,
                    shares = data.shares
                )

                // Filter agar hanya menampilkan peserta yang masih ada
                val validSummary = summaryMapById.filterKeys { id -> data.participants.any { it.id == id } }

                val summaryMapByParticipant = validSummary.mapKeys { entry ->
                    data.participants.find { it.id == entry.key }!!
                }

                _uiState.value = BillSplitterUiState(
                    sessionName = data.session.name,
                    participants = data.participants,
                    bills = data.bills,
                    items = data.items,
                    shares = data.shares,
                    summary = summaryMapByParticipant,
                    isLoading = false
                )
            }
        }
    }

    private fun loadDataFromFile() {
        var data = repository.loadSession()
        if (data == null) {
            data = createInitialDummyData()
            repository.saveSession(data)
        }
        _sessionData.value = data
    }

    private fun saveData() {
        _sessionData.value?.let {
            repository.saveSession(it)
        }
    }

    fun addParticipant(name: String) {
        if (name.isBlank() || _sessionData.value == null) return
        val currentData = _sessionData.value!!
        val newParticipant = Participant(
            id = (currentData.participants.maxOfOrNull { it.id } ?: 0L) + 1,
            sessionId = currentData.session.id, name = name
        )
        _sessionData.value = currentData.copy(participants = currentData.participants + newParticipant)
        saveData()
    }

    // --- FUNGSI YANG DIPERBAIKI ---
    fun removeParticipant(participant: Participant) {
        if (_sessionData.value == null) return
        val currentData = _sessionData.value!!

        val updatedParticipants = currentData.participants - participant
        // Hapus semua pembagian item yang melibatkan peserta ini
        val updatedShares = currentData.shares.filterNot { it.participantId == participant.id }
        // Hapus status "Payer" jika peserta ini yang membayar
        val updatedBills = currentData.bills.map {
            if (it.payerId == participant.id) it.copy(payerId = null) else it
        }

        _sessionData.value = currentData.copy(
            participants = updatedParticipants,
            shares = updatedShares,
            bills = updatedBills
        )
        saveData()
    }
    // --- AKHIR FUNGSI YANG DIPERBAIKI ---

    fun addBill(): Long {
        if (_sessionData.value == null) return -1
        val currentData = _sessionData.value!!
        val newBill = Bill(
            id = (currentData.bills.maxOfOrNull { it.id } ?: 0L) + 1,
            sessionId = currentData.session.id,
            name = "Bill Baru #${currentData.bills.size + 1}"
        )
        _sessionData.value = currentData.copy(bills = currentData.bills + newBill)
        saveData()
        return newBill.id
    }

    fun removeBill(bill: Bill) {
        if (_sessionData.value == null) return
        val currentData = _sessionData.value!!
        _sessionData.value = currentData.copy(
            bills = currentData.bills.filterNot { it.id == bill.id },
            items = currentData.items.filterNot { it.billId == bill.id },
            shares = currentData.shares.filterNot { share ->
                currentData.items.any { item -> item.billId == bill.id && item.id == share.itemId }
            }
        )
        saveData()
    }

    fun updateBillHeader(billId: Long, newName: String, newTax: Double, payerId: Long?, totalPaid: Double) {
        if (_sessionData.value == null) return
        val currentData = _sessionData.value!!
        _sessionData.value = currentData.copy(
            bills = currentData.bills.map {
                if (it.id == billId) it.copy(
                    name = newName,
                    taxPercentage = newTax,
                    payerId = payerId,
                    totalPaid = totalPaid
                ) else it
            }
        )
        saveData()
    }

    fun addItemToBill(billId: Long, itemName: String, price: Double, quantity: Int) {
        if (itemName.isBlank() || price <= 0 || quantity <= 0 || _sessionData.value == null) return
        val currentData = _sessionData.value!!
        val newItem = Item(
            id = (currentData.items.maxOfOrNull { it.id } ?: 0L) + 1,
            billId = billId, name = itemName, price = price, quantity = quantity
        )
        val newShares = currentData.participants.map { participant ->
            ItemShare(itemId = newItem.id, participantId = participant.id)
        }
        _sessionData.value = currentData.copy(
            items = currentData.items + newItem,
            shares = currentData.shares + newShares
        )
        saveData()
    }

    fun removeItem(item: Item) {
        if (_sessionData.value == null) return
        val currentData = _sessionData.value!!
        _sessionData.value = currentData.copy(
            items = currentData.items - item,
            shares = currentData.shares.filterNot { it.itemId == item.id }
        )
        saveData()
    }

    fun updateItemSharing(itemId: Long, selectedParticipantIds: List<Long>) {
        if (_sessionData.value == null) return
        val currentData = _sessionData.value!!
        val otherShares = currentData.shares.filterNot { it.itemId == itemId }
        val newShares = selectedParticipantIds.map { participantId ->
            ItemShare(itemId = itemId, participantId = participantId)
        }
        _sessionData.value = currentData.copy(shares = otherShares + newShares)
        saveData()
    }

    fun exportToPdf(context: android.content.Context) {
        val state = _uiState.value
        if (state.summary.isNotEmpty()) {
            com.group.billpay.domain.exporter.PdfExporter.createAndSharePdf(
                context = context,
                sessionName = state.sessionName,
                summaryData = state.summary
            )
        }
    }

    private fun createInitialDummyData(): SessionData {
        val session = Session(id = 1, name = "Sesi Coba-coba")
        val participants = listOf(
            Participant(id = 1, sessionId = 1, name = "A"),
            Participant(id = 2, sessionId = 1, name = "B"),
            Participant(id = 3, sessionId = 1, name = "C")
        )
        return SessionData(session, participants, emptyList(), emptyList(), emptyList())
    }
}