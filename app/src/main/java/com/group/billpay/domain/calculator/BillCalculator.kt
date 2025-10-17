package com.group.billpay.domain.calculator

import com.group.billpay.data.model.*

object BillCalculator {

    fun calculateSessionSummary(
        participants: List<Participant>,
        bills: List<Bill>,
        items: List<Item>,
        shares: List<ItemShare>
    ): Map<Long, BalanceSummary> {
        // Peta untuk menyimpan [Total Tagihan, Total Bayar] per peserta
        val summaryMap = participants.associate { it.id to Pair(0.0, 0.0) }.toMutableMap()

        // 1. Hitung Total Uang yang Sudah Dibayar (Nalangin) oleh tiap peserta
        bills.forEach { bill ->
            if (bill.payerId != null) {
                val currentData = summaryMap[bill.payerId] ?: (0.0 to 0.0)
                summaryMap[bill.payerId] = currentData.first to (currentData.second + bill.totalPaid)
            }
        }

        // 2. Hitung Total Tagihan per peserta berdasarkan item yang mereka ikuti
        items.forEach { item ->
            val itemShares = shares.filter { it.itemId == item.id }
            val sharerCount = itemShares.size

            if (sharerCount > 0) {
                val bill = bills.find { it.id == item.billId }
                val taxMultiplier = 1 + ((bill?.taxPercentage ?: 0.0) / 100.0)
                val totalItemCost = (item.price * item.quantity) * taxMultiplier

                val costPerSharer = totalItemCost / sharerCount

                itemShares.forEach { share ->
                    val currentData = summaryMap[share.participantId] ?: (0.0 to 0.0)
                    summaryMap[share.participantId] = (currentData.first + costPerSharer) to currentData.second
                }
            }
        }

        // 3. Konversi ke format data class BalanceSummary
        return summaryMap.mapValues { (_, pair) ->
            BalanceSummary(totalBill = pair.first, totalPaid = pair.second)
        }
    }
}