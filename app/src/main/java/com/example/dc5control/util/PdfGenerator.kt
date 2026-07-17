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
 * Generador de PDF con el formato oficial DC-3 (STPS) exacto.
 * Basado en los lineamientos de validez oficial y las imágenes proporcionadas.
 */
object PdfGenerator {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 25f
    
    // Colores oficiales
    private val ORANGE_LABEL = Color.rgb(204, 51, 0) // Naranja/Rojo STPS
    private val HEADER_BG = Color.BLACK
    private val LINE_COLOR = Color.rgb(180, 180, 180)

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
        val paint = Paint().apply { isAntiAlias = true }
        var y = 40f

        // Fondo blanco
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), PAGE_H.toFloat(), paint)

        // Títulos Principales
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        paint.textSize = 14f
        drawCenteredText(canvas, "FORMATO DC-3", PAGE_W / 2f, y, paint)
        y += 15f
        paint.textSize = 9f
        drawCenteredText(canvas, "CONSTANCIA DE COMPETENCIAS O DE HABILIDADES LABORALES", PAGE_W / 2f, y, paint)
        y += 25f

        // --- SECCIÓN 1: DATOS DEL TRABAJADOR ---
        drawSectionHeader(canvas, "DATOS DEL TRABAJADOR", y, paint)
        y += 16f
        val sec1Start = y

        drawOrangeLabel(canvas, "Nombre (Anotar apellido paterno, apellido materno y nombre(s))", MARGIN + 6f, y + 10f, paint)
        drawBlackValue(canvas, worker.name.uppercase(), MARGIN + 6f, y + 23f, paint, 10f)
        y += 28f
        
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint.apply { color = LINE_COLOR })
        
        // CURP y Ocupación
        drawOrangeLabel(canvas, "Clave Única de Registro de Población (CURP)", MARGIN + 6f, y + 10f, paint)
        drawCharGrid(canvas, worker.curp.uppercase(), MARGIN + 6f, y + 14f, 18, paint)
        
        val col2X = MARGIN + (PAGE_W - 2 * MARGIN) / 2
        drawOrangeLabel(canvas, "Ocupación específica (Catálogo Nacional de Ocupaciones)", col2X + 6f, y + 10f, paint)
        drawBlackValue(canvas, worker.occupation.uppercase(), col2X + 6f, y + 24f, paint, 8.5f)
        
        y += 34f
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint.apply { color = LINE_COLOR })

        // Puesto
        drawOrangeLabel(canvas, "Puesto", MARGIN + 6f, y + 10f, paint)
        drawBlackValue(canvas, worker.position.uppercase(), MARGIN + 6f, y + 22f, paint, 9f)
        y += 27f

        // Recuadro Sección 1
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        canvas.drawRect(MARGIN, sec1Start, PAGE_W - MARGIN, y, paint)
        canvas.drawLine(col2X, sec1Start + 28f, col2X, sec1Start + 62f, paint)
        paint.style = Paint.Style.FILL

        y += 15f

        // --- SECCIÓN 2: DATOS DE LA EMPRESA ---
        drawSectionHeader(canvas, "DATOS DE LA EMPRESA", y, paint)
        y += 16f
        val sec2Start = y

        drawOrangeLabel(canvas, "Nombre o razón social (En caso de persona física, anotar apellido paterno, materno y nombre(s))", MARGIN + 6f, y + 10f, paint)
        drawBlackValue(canvas, companyName.uppercase(), MARGIN + 6f, y + 23f, paint, 10f)
        y += 28f

        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint.apply { color = LINE_COLOR })

        drawOrangeLabel(canvas, "Registro Federal de Contribuyentes con homoclave (SHCP)", MARGIN + 6f, y + 10f, paint)
        drawCharGrid(canvas, companyRfc.uppercase().replace("-",""), MARGIN + 6f, y + 14f, 13, paint)
        y += 34f

        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        canvas.drawRect(MARGIN, sec2Start, PAGE_W - MARGIN, y, paint)
        paint.style = Paint.Style.FILL

        y += 15f

        // --- SECCIÓN 3: DATOS DEL PROGRAMA ---
        drawSectionHeader(canvas, "DATOS DEL PROGRAMA DE CAPACITACIÓN, ADIESTRAMIENTO Y PRODUCTIVIDAD", y, paint)
        y += 16f
        val sec3Start = y

        drawOrangeLabel(canvas, "Nombre del curso", MARGIN + 6f, y + 10f, paint)
        drawBlackValue(canvas, course.name.uppercase(), MARGIN + 6f, y + 23f, paint, 9.5f)
        y += 28f

        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint.apply { color = LINE_COLOR })

        // Duración y Periodo
        drawOrangeLabel(canvas, "Duración en horas", MARGIN + 6f, y + 10f, paint)
        drawBlackValue(canvas, "${course.duration_hours} HORAS", MARGIN + 6f, y + 23f, paint, 9f)

        drawOrangeLabel(canvas, "Periodo de ejecución:   De", MARGIN + 110f, y + 10f, paint)
        
        // Fecha Inicio
        drawDateHeaders(canvas, MARGIN + 225f, y + 8f, paint)
        drawDateGrid(canvas, startDate, MARGIN + 215f, y + 11f, paint)

        drawOrangeLabel(canvas, "a", MARGIN + 335f, y + 22f, paint)

        // Fecha Fin
        drawDateHeaders(canvas, MARGIN + 365f, y + 8f, paint)
        drawDateGrid(canvas, endDate, MARGIN + 355f, y + 11f, paint)

        y += 28f
        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint.apply { color = LINE_COLOR })

        drawOrangeLabel(canvas, "Área temática del curso", MARGIN + 6f, y + 10f, paint)
        drawBlackValue(canvas, course.thematicArea.uppercase(), MARGIN + 6f, y + 22f, paint, 9f)
        y += 27f

        canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, paint.apply { color = LINE_COLOR })

        drawOrangeLabel(canvas, "Nombre del agente capacitador o STPS", MARGIN + 6f, y + 10f, paint)
        drawBlackValue(canvas, "${agent.name.uppercase()}  (STPS REG: ${agent.stps.uppercase()})", MARGIN + 6f, y + 22f, paint, 9f)
        y += 27f

        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        canvas.drawRect(MARGIN, sec3Start, PAGE_W - MARGIN, y, paint)
        paint.style = Paint.Style.FILL

        y += 20f

        // --- ÁREA DE FIRMAS ---
        paint.textSize = 7f
        paint.color = Color.BLACK
        paint.isFakeBoldText = false
        drawCenteredText(canvas, "aquél que no se conduce con verdad.", PAGE_W / 2f, y, paint)
        y += 40f

        val sigW = 155f
        val gap = (PAGE_W - 2 * MARGIN - 3 * sigW) / 2
        
        // Líneas de firma
        paint.strokeWidth = 0.8f
        canvas.drawLine(MARGIN, y, MARGIN + sigW, y, paint)
        canvas.drawLine(MARGIN + sigW + gap, y, MARGIN + 2 * sigW + gap, y, paint)
        canvas.drawLine(PAGE_W - MARGIN - sigW, y, PAGE_W - MARGIN, y, paint)

        // Textos de firma
        paint.textSize = 6.5f
        drawCenteredText(canvas, "Instructor o tutor", MARGIN + sigW / 2f, y + 10f, paint)
        drawCenteredText(canvas, "Nombre y firma", MARGIN + sigW / 2f, y + 18f, paint)

        drawCenteredText(canvas, "Patrón o representante legal", MARGIN + sigW + gap + sigW / 2f, y + 10f, paint)
        drawCenteredText(canvas, "Nombre y firma", MARGIN + sigW + gap + sigW / 2f, y + 18f, paint)

        drawCenteredText(canvas, "Representante de los trabajadores", PAGE_W - MARGIN - sigW / 2f, y + 10f, paint)
        drawCenteredText(canvas, "Nombre y firma", PAGE_W - MARGIN - sigW / 2f, y + 18f, paint)

        // Instrucciones al pie
        y = 760f
        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 6f
        canvas.drawText("INSTRUCCIONES", MARGIN, y, paint)
        y += 10f
        val footers = listOf(
            "- Llenar a máquina o con letra de molde con tinta negra.",
            "- Entregar esta constancia a los trabajadores que aprueben el curso correspondiente, dentro de los veinte días hábiles siguientes al término del mismo.",
            "- Las áreas y subáreas de ocupación específica se refieren al Catálogo Nacional de Ocupaciones.",
            "- Las áreas temáticas de los cursos se refieren al catálogo de la parte posterior de este formato."
        )
        for (line in footers) {
            canvas.drawText(line, MARGIN, y, paint)
            y += 8f
        }
        
        paint.textSize = 8f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("DC-3 ANVERSO", PAGE_W - MARGIN - 70f, PAGE_H - 20f, paint)
    }

    private fun drawReverso(canvas: Canvas) {
        val paint = Paint().apply { isAntiAlias = true }
        var y = 35f

        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), PAGE_H.toFloat(), paint)

        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        paint.textSize = 9f
        drawCenteredText(canvas, "CLAVES Y DENOMINACIONES DE ÁREAS Y SUBÁREAS DEL CATÁLOGO NACIONAL DE OCUPACIONES", PAGE_W / 2f, y, paint)
        y += 20f

        paint.textSize = 7.5f
        paint.isFakeBoldText = false
        
        // Simulación de las dos columnas de ocupaciones
        val occs = listOf(
            "01  Cultivo, crianza y aprovechamiento forestal" to "06  Transporte",
            "02  Extracción y saneamiento" to "07  Provisión de bienes y servicios",
            "03  Construcción" to "08  Servicios educativos, sociales y de salud",
            "04  Electricidad, gas y agua" to "09  Gobierno, administración y defensa",
            "05  Industria manufacturera" to ""
        )

        val colW = (PAGE_W - 2 * MARGIN) / 2
        for (pair in occs) {
            canvas.drawText(pair.first, MARGIN + 6f, y, paint)
            if (pair.second.isNotEmpty()) canvas.drawText(pair.second, MARGIN + colW + 6f, y, paint)
            y += 12f
        }

        y += 20f
        paint.isFakeBoldText = true
        drawCenteredText(canvas, "ÁREAS TEMÁTICAS DE LOS CURSOS", PAGE_W / 2f, y, paint)
        y += 15f

        paint.isFakeBoldText = false
        val areas = listOf(
            "1000  Producción agropecuaria" to "6000  Seguridad e higiene",
            "2000  Tecnología de la información" to "7000  Desarrollo humano",
            "3000  Servicios" to "8000  Idiomas y comunicación",
            "4000  Procesos industriales" to "9000  Otros servicios",
            "5000  Administración y mercadotecnia" to ""
        )

        for (pair in areas) {
            canvas.drawText(pair.first, MARGIN + 6f, y, paint)
            if (pair.second.isNotEmpty()) canvas.drawText(pair.second, MARGIN + colW + 6f, y, paint)
            y += 12f
        }

        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("DC-3 REVERSO", PAGE_W - MARGIN - 70f, PAGE_H - 20f, paint)
    }

    // --- HELPERS DE DIBUJO ---

    private fun drawSectionHeader(canvas: Canvas, title: String, y: Float, paint: Paint) {
        paint.color = HEADER_BG
        canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + 16f, paint)
        paint.color = Color.WHITE
        paint.isFakeBoldText = true
        paint.textSize = 8.5f
        drawCenteredText(canvas, title, PAGE_W / 2f, y + 11.5f, paint)
        paint.color = Color.BLACK // Reset
    }

    private fun drawOrangeLabel(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        paint.color = ORANGE_LABEL
        paint.textSize = 6.5f
        paint.isFakeBoldText = false
        canvas.drawText(text, x, y, paint)
    }

    private fun drawBlackValue(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, size: Float) {
        paint.color = Color.BLACK
        paint.textSize = size
        paint.isFakeBoldText = true
        canvas.drawText(text, x, y, paint)
    }

    private fun drawCharGrid(canvas: Canvas, text: String, x: Float, y: Float, count: Int, paint: Paint) {
        val boxW = 12f
        val boxH = 14f
        val spacing = 1.5f
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        val charPaint = Paint().apply { isAntiAlias = true; textSize = 8.5f; isFakeBoldText = true }

        for (i in 0 until count) {
            val bx = x + i * (boxW + spacing)
            canvas.drawRect(bx, y, bx + boxW, y + boxH, paint)
            if (i < text.length) {
                val charStr = text[i].toString()
                val tw = charPaint.measureText(charStr)
                canvas.drawText(charStr, bx + (boxW - tw) / 2f, y + 10.5f, charPaint)
            }
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawDateGrid(canvas: Canvas, dateStr: String, x: Float, y: Float, paint: Paint) {
        // Formato esperado: YYYY-MM-DD
        val clean = dateStr.replace("-", "").replace("/", "")
        val year = if (clean.length >= 4) clean.substring(0, 4) else "    "
        val month = if (clean.length >= 6) clean.substring(4, 6) else "  "
        val day = if (clean.length >= 8) clean.substring(6, 8) else "  "

        val boxW = 12f
        val spacing = 1f
        
        // Año (4)
        drawSubGrid(canvas, year, x, y, 4, boxW, paint)
        // Mes (2)
        drawSubGrid(canvas, month, x + 4 * (boxW + spacing) + 4f, y, 2, boxW, paint)
        // Día (2)
        drawSubGrid(canvas, day, x + 6 * (boxW + spacing) + 8f, y, 2, boxW, paint)
    }

    private fun drawSubGrid(canvas: Canvas, text: String, x: Float, y: Float, count: Int, boxW: Float, paint: Paint) {
        paint.style = Paint.Style.STROKE
        val charPaint = Paint().apply { isAntiAlias = true; textSize = 8f; isFakeBoldText = true }
        for (i in 0 until count) {
            val bx = x + i * (boxW + 1f)
            canvas.drawRect(bx, y, bx + boxW, y + 14f, paint)
            if (i < text.length) {
                val charStr = text[i].toString()
                canvas.drawText(charStr, bx + (boxW - charPaint.measureText(charStr)) / 2f, y + 10.5f, charPaint)
            }
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawDateHeaders(canvas: Canvas, x: Float, y: Float, paint: Paint) {
        val oldColor = paint.color
        paint.color = Color.BLACK
        paint.textSize = 6f
        paint.isFakeBoldText = false
        canvas.drawText("AÑO", x, y, paint)
        canvas.drawText("MES", x + 25f, y, paint)
        canvas.drawText("DÍA", x + 45f, y, paint)
        paint.color = oldColor
    }

    private fun drawCenteredText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        val width = paint.measureText(text)
        canvas.drawText(text, x - width / 2f, y, paint)
    }
}
