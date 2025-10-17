package com.group.billpay.domain.exporter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.group.billpay.data.model.BalanceSummary
import com.group.billpay.data.model.Participant
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object PdfExporter {

    fun createAndSharePdf(
        context: Context,
        sessionName: String,
        summaryData: Map<Participant, BalanceSummary>
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }

        // --- Gambar Konten PDF ---
        var yPosition = 40f

        // Judul
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Ringkasan Bill: $sessionName", 20f, yPosition, paint)
        yPosition += 40f

        // Header Tabel
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Peserta", 20f, yPosition, paint)
        canvas.drawText("Total Tagihan", 200f, yPosition, paint)
        canvas.drawText("Sudah Dibayar", 350f, yPosition, paint)
        canvas.drawText("Saldo Akhir", 480f, yPosition, paint)
        yPosition += 20f

        // Garis pemisah
        canvas.drawLine(20f, yPosition, 575f, yPosition, paint)
        yPosition += 25f

        // Isi Tabel
        paint.isFakeBoldText = false
        var totalTagihan = 0.0
        var totalBayar = 0.0

        summaryData.forEach { (participant, summary) ->
            canvas.drawText(participant.name, 20f, yPosition, paint)
            canvas.drawText(rupiahFormat.format(summary.totalBill), 200f, yPosition, paint)
            canvas.drawText(rupiahFormat.format(summary.totalPaid), 350f, yPosition, paint)
            canvas.drawText(rupiahFormat.format(summary.finalBalance), 480f, yPosition, paint)
            yPosition += 20f

            totalTagihan += summary.totalBill
            totalBayar += summary.totalPaid
        }

        // Garis pemisah
        yPosition += 10f
        canvas.drawLine(20f, yPosition, 575f, yPosition, paint)
        yPosition += 25f

        // Total Keseluruhan
        paint.isFakeBoldText = true
        canvas.drawText("Total", 20f, yPosition, paint)
        canvas.drawText(rupiahFormat.format(totalTagihan), 200f, yPosition, paint)
        canvas.drawText(rupiahFormat.format(totalBayar), 350f, yPosition, paint)

        document.finishPage(page)

        // --- Simpan dan Bagikan File ---
        val pdfFile = savePdfToFile(context, document)
        if (pdfFile != null) {
            sharePdf(context, pdfFile)
        }
    }

    private fun savePdfToFile(context: Context, document: PdfDocument): File? {
        return try {
            val dir = File(context.cacheDir, "pdfs")
            dir.mkdirs()
            val file = File(dir, "bill_summary_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun sharePdf(context: Context, file: File) {
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan PDF via..."))
    }
}