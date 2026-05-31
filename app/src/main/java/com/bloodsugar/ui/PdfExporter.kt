package com.bloodsugar.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.bloodsugar.R
import com.bloodsugar.data.Record
import com.bloodsugar.data.SegmentStats
import com.bloodsugar.util.MealSegment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    fun generate(
        context: Context,
        stats: List<SegmentStats>,
        records: List<Record>,
        rangeLabel: String
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 18f; color = Color.BLACK; isAntiAlias = true; isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            textSize = 12f; color = Color.BLACK; isAntiAlias = true; isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            textSize = 10f; color = Color.DKGRAY; isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY; strokeWidth = 0.5f
        }

        var y = 40f

        // Title
        canvas.drawText("${context.getString(R.string.pdf_title)} - $rangeLabel", 40f, y, titlePaint)
        y += 14f
        canvas.drawText("${context.getString(R.string.pdf_generated)}: ${
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        }", 40f, y, textPaint)
        y += 24f

        // Stats table header
        canvas.drawText(context.getString(R.string.stats_title), 40f, y, headerPaint)
        y += 18f
        val cols = floatArrayOf(40f, 140f, 220f, 300f, 380f, 460f)
        val headers = listOf("", context.getString(R.string.stats_avg), context.getString(R.string.stats_max),
            context.getString(R.string.stats_min), "Count")
        headers.forEachIndexed { i, h -> if (i > 0) canvas.drawText(h, cols[i], y, headerPaint) }
        y += 4f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 14f

        // Stats rows
        val segmentLabels = MealSegment.values().map { context.getString(it.labelResId) }
        stats.forEach { stat ->
            val segIndex = MealSegment.safeValueOf(stat.segment)?.ordinal ?: -1
            val label = if (segIndex >= 0) segmentLabels[segIndex] else stat.segment
            canvas.drawText(label, cols[0], y, textPaint)
            canvas.drawText("%.1f".format(stat.avg), cols[1], y, textPaint)
            canvas.drawText("%.1f".format(stat.max), cols[2], y, textPaint)
            canvas.drawText("%.1f".format(stat.min), cols[3], y, textPaint)
            canvas.drawText("${stat.count}", cols[4], y, textPaint)
            y += 16f
        }
        y += 16f

        // Records table header
        canvas.drawText("${context.getString(R.string.btn_record)} (${records.size})", 40f, y, headerPaint)
        y += 18f
        val rCols = floatArrayOf(40f, 160f, 280f, 360f, 440f)
        listOf("Date", "Segment", "Value", "Note").forEachIndexed { i, h ->
            canvas.drawText(h, rCols[i], y, headerPaint)
        }
        y += 4f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 14f

        // Records rows
        val dateFmt = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        records.forEach { record ->
            if (y > 800f) return@forEach // skip if exceeds page
            val segIdx = MealSegment.safeValueOf(record.segment)?.ordinal ?: -1
            val segLabel = if (segIdx >= 0) segmentLabels[segIdx] else record.segment
            canvas.drawText(dateFmt.format(Date(record.timestamp)), rCols[0], y, textPaint)
            canvas.drawText(segLabel, rCols[1], y, textPaint)
            canvas.drawText("%.1f mmol/L".format(record.value), rCols[2], y, textPaint)
            canvas.drawText(record.note.take(20), rCols[3], y, textPaint)
            y += 14f
        }

        doc.finishPage(page)

        // Save file
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val fileName = "${context.getString(R.string.pdf_title)}_${
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }.pdf"
        val file = File(dir, fileName)
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
