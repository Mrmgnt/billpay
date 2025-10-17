package com.group.billpay.data.model
import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Participant(
    val id: Long = 0,
    val sessionId: Long,
    val name: String
)

@Serializable
data class Bill(
    val id: Long = 0,
    val sessionId: Long,
    val name: String,
    val taxPercentage: Double = 0.0,
    val payerId: Long? = null,      // <-- ID dari satu orang yang membayar (nalangin)
    val totalPaid: Double = 0.0     // <-- Jumlah total yang dibayarkan
)

@Serializable
data class Item(
    val id: Long = 0,
    val billId: Long,
    val name: String,
    val price: Double,
    val quantity: Int = 1           // <-- TAMBAHAN: Kuantitas item
)

@Serializable
data class ItemShare(
    val itemId: Long,
    val participantId: Long
    // Properti 'proportion' dihapus karena pembagian selalu rata di antara yang terpilih
)

// data class 'Payment' tidak lagi dibutuhkan karena sudah diwakili di 'Bill'

@Serializable
data class BalanceSummary(
    val totalBill: Double,
    val totalPaid: Double
) {
    val finalBalance: Double
        get() = totalPaid - totalBill
}