package com.example.dc5control.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.dc5control.data.model.Worker
import com.example.dc5control.data.model.Course
import com.example.dc5control.data.model.TrainingAgent
import java.io.File
import java.io.FileOutputStream

/**
 * Generador de PDF con el formato oficial DC-3 (STPS) exacto basado en la imagen proporcionada.
 */
object PdfGenerator {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 30f
    
    // Colores del formato
    private val GRAY_BAR = Color.rgb(230, 230, 230)
    private val TEXT_BLACK = Color.BLACK
    private val LINE_GRAY = Color.rgb(180, 180, 180)

    fun generateDC3(
        context: Context,
        worker: Worker,
        course: Course,
        agent: TrainingAgent,
        companyName: String,
        companyRfc: String,
        startDate: String,
        endDate: String
    ): File {
        val pdfDocument = PdfDocument()
        
        // --- PÁGINA 1: ANVERSO ---
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        drawAnverso(page1.canvas, worker, course, agent, companyName, companyRfc, startDate, endDate)
        pdfDocument.finishPage(page1)

        // --- PÁGINA 2: REVERSO ---
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        drawReverso(page2.canvas)
        pdfDocument.finishPage(page2)

        val file = File(context.getExternalFilesDir(null), "DC3_${worker.curp.ifEmpty { worker.name.replace(" ", "_") }}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    private fun drawAnverso(
        canvas: Canvas, worker: Worker, course: Course, agent: TrainingAgent,
        companyName: String, companyRfc: String, startDate: String, endDate: String
    ) {
        val paint = Paint().apply { isAntiAlias = true; color = TEXT_BLACK }
        var y = 50f

        // Fondo blanco
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), PAGE_H.toFloat(), paint)

        // Títulos Principales
        paint.color = TEXT_BLACK
        paint.isFakeBoldText = true
        paint.textSize = 16f
        drawCenteredText(canvas, "FORMATO DC-3", PAGE_W / 2f, y, paint)
        y += 18f
        paint.textSize = 10f
        drawCenteredText(canvas, "CONSTANCIA DE COMPETENCIAS O DE HABILIDADES LABORALES", PAGE_W / 2f, y, paint)
        y += 35f

        // --- SECCIÓN 1: DATOS DEL TRABAJADOR ---
        drawGraySectionHeader(canvas, "DATOS DEL TRABAJADOR", y, paint)
        y += 20f

        drawLabel(canvas, "Nombre (Anotar apellido paterno, apellido materno y nombre(s))", MARGIN, y, paint)
        y += 15f
        drawValue(canvas, worker.name.uppercase(), MARGIN, y, paint, 11f)
        y += 5f
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint.apply { color = TEXT_BLACK; strokeWidth = 0.8f })
        y += 25f
        
        // CURP y Ocupación
        drawLabel(canvas, "Clave Única de Registro de Población", MARGIN, y, paint)
        val col2X = MARGIN + 310f
        drawLabel(canvas, "Ocupación específica (Catálogo Nacional de Ocupaciones)", col2X, y, paint)
        y += 8f
        drawCharBoxes(canvas, worker.curp.uppercase(), MARGIN, y, 18, paint)
        drawValue(canvas, worker.occupation, col2X, y + 12f, paint, 9f)
        y += 15f
        canvas.drawLine(col2X, y, PAGE_W - MARGIN, y, paint)
        y += 25f

        // Puesto
        drawLabel(canvas, "Puesto", MARGIN, y, paint)
        y += 15f
        drawValue(canvas, worker.position.uppercase(), MARGIN, y, paint, 10f)
        y += 5f
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint)
        y += 40f

        // --- SECCIÓN 2: DATOS DE LA EMPRESA ---
        drawGraySectionHeader(canvas, "DATOS DE LA EMPRESA", y, paint)
        y += 20f

        drawLabel(canvas, "Nombre o razón social (En caso de persona física, anotar apellido paterno, materno y nombre(s))", MARGIN, y, paint)
        y += 15f
        drawValue(canvas, companyName.uppercase(), MARGIN, y, paint, 11f)
        y += 5f
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint)
        y += 25f

        drawLabel(canvas, "Registro Federal de Contribuyentes con homoclave (SHCP)", MARGIN, y, paint)
        y += 8f
        drawCharBoxes(canvas, companyRfc.uppercase().replace("-",""), MARGIN, y, 13, paint)
        y += 40f

        // --- SECCIÓN 3: DATOS DEL PROGRAMA ---
        drawGraySectionHeader(canvas, "DATOS DEL PROGRAMA DE CAPACITACIÓN, ADIESTRAMIENTO Y PRODUCTIVIDAD", y, paint)
        y += 20f

        drawLabel(canvas, "Nombre del curso", MARGIN, y, paint)
        y += 15f
        drawValue(canvas, course.name.uppercase(), MARGIN, y, paint, 11f)
        y += 5f
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint)
        y += 25f

        // Duración
        drawLabel(canvas, "Duración en horas", MARGIN, y, paint)
        y += 15f
        drawValue(canvas, "${course.duration_hours} HORAS", MARGIN, y, paint, 10f)
        y += 5f
        canvas.drawLine(MARGIN, y, MARGIN + 120f, y, paint)

        // Footer Anverso
        paint.textSize = 8f
        paint.isFakeBoldText = false
        canvas.drawText("DC-3 ANVERSO", PAGE_W - MARGIN - 70f, PAGE_H - 30f, paint)
    }

    private fun drawReverso(canvas: Canvas) {
        val paint = Paint().apply { isAntiAlias = true; color = TEXT_BLACK }
        var y = 40f
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), PAGE_H.toFloat(), paint)
        paint.color = TEXT_BLACK
        paint.textSize = 9f
        paint.isFakeBoldText = true
        drawCenteredText(canvas, "CATÁLOGO DE OCUPACIONES Y ÁREAS TEMÁTICAS", PAGE_W / 2f, y, paint)
    }

    // --- HELPERS ---

    private fun drawGraySectionHeader(canvas: Canvas, title: String, y: Float, paint: Paint) {
        paint.color = GRAY_BAR
        canvas.drawRect(MARGIN, y - 14f, PAGE_W - MARGIN, y + 4f, paint)
        paint.color = TEXT_BLACK
        paint.isFakeBoldText = true
        paint.textSize = 10f
        canvas.drawText(title, MARGIN + 5f, y - 1f, paint)
    }

    private fun drawLabel(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        paint.color = TEXT_BLACK
        paint.isFakeBoldText = false
        paint.textSize = 7f
        canvas.drawText(text, x, y, paint)
    }

    private fun drawValue(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, size: Float) {
        paint.color = TEXT_BLACK
        paint.isFakeBoldText = true
        paint.textSize = size
        canvas.drawText(text, x, y, paint)
    }

    private fun drawCharBoxes(canvas: Canvas, text: String, x: Float, y: Float, count: Int, paint: Paint) {
        val boxW = 14f
        val boxH = 16f
        paint.style = Paint.Style.STROKE
        paint.color = TEXT_BLACK
        paint.strokeWidth = 1f
        
        val charPaint = Paint().apply { isAntiAlias = true; textSize = 10f; isFakeBoldText = true; color = TEXT_BLACK }

        for (i in 0 until count) {
            val bx = x + i * boxW
            canvas.drawRect(bx, y, bx + boxW, y + boxH, paint)
            if (i < text.length) {
                val charStr = text[i].toString()
                val tw = charPaint.measureText(charStr)
                canvas.drawText(charStr, bx + (boxW - tw) / 2f, y + 12f, charPaint)
            }
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawCenteredText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        val width = paint.measureText(text)
        canvas.drawText(text, x - width / 2f, y, paint)
    }
}
