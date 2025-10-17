package com.group.billpay.data.repository

import android.content.Context
import com.group.billpay.data.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

@Serializable
data class SessionData(
    val session: Session,
    val participants: List<Participant>,
    val bills: List<Bill>,
    val items: List<Item>,
    val shares: List<ItemShare>
    // List 'payments' dihapus dari sini
)

class FileSessionRepository(private val context: Context) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val fileName = "active_session.json"

    fun saveSession(data: SessionData) {
        try {
            val jsonString = json.encodeToString(data)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadSession(): SessionData? {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                return null
            }
            val jsonString = context.openFileInput(fileName).bufferedReader().use {
                it.readText()
            }
            json.decodeFromString<SessionData>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}