package com.example.dc5control.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.dc5control.data.model.Worker
import com.example.dc5control.data.model.Course
import com.example.dc5control.data.model.TrainingAgent
import java.io.File
import java.io.FileOutputStream

/**
 * Generador de PDF con el formato oficial DC-3 (STPS).
 * Anverso: datos del trabajador, empresa, programa de capacitación y firmas.
 * Reverso: catálogos de áreas ocupacionales y áreas temáticas.
 */
object PdfGenerator {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40f

    fun generateDC3(
        context: Context,
        worker: Worker,
        course: Course,
        agent: TrainingAgent,
        companyName: String,
        companyRfc: String,
        startDate: String,
        endDate: String,
        thematicArea: String = "(6000) Seguridad",
        occupationCode: String = ""
    ): File {
        val pdfDocument = PdfDocument()
        // ── PÁGINA 1: ANVERSO ──
        val page1 = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        drawAnverso(page1.canvas, worker, course, agent, companyName, companyRfc, startDate, endDate, thematicArea, occupationCode)
        pdfDocument.finishPage(page1)

        // ── PÁGINA 2: REVERSO ──
        val page2 = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 2).create())
        drawReverso(page2.canvas)
        pdfDocument.finishPage(page2)

        val file = File(context.getExternalFilesDir(null), "DC3_${worker.curp.ifEmpty { worker.name }}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    }

    private fun drawAnverso(
        canvas: Canvas, worker: Worker, course: Course, agent: TrainingAgent,
        companyName: String, companyRfc: String, startDate: String, endDate: String,
        thematicArea: String, occupationCode: String
    ) {
        val paint = Paint().apply { color = Color.BLACK; isAntiAlias = true }
        val thin = Paint().apply { color = Color.BLACK; strokeWidth = 0.8f; isAntiAlias = true }
        val bold = Paint().apply { color = Color.BLACK; isFakeBoldText = true; isAntiAlias = true }
        val gray = Paint().apply { color = Color.rgb(230,230,230) }
        val labelPaint = Paint().apply { color = Color.rgb(80,80,80); textSize = 6f; isAntiAlias = true }
        val valuePaint = Paint().apply { color = Color.BLACK; textSize = 8f; isFakeBoldText = true; isAntiAlias = true }

        var y = 30f

        // ── Título ──
        bold.textSize = 13f
        canvas.drawText("FORMATO DC-3", 240f, y, bold)
        y += 14f
        paint.textSize = 7f
        canvas.drawText("CONSTANCIA DE COMPETENCIAS O DE HABILIDADES LABORALES", 165f, y, paint)
        y += 16f

        // ── Sección 1: DATOS DEL TRABAJADOR ──
        drawSectionBar(canvas, "DATOS DEL TRABAJADOR", y, gray, bold)
        y += 14f

        // Nombre
        labelPaint.textSize = 6f
        canvas.drawText("Nombre (Anotar apellido paterno, apellido materno y nombre(s))", MARGIN, y, labelPaint)
        y += 8f
        valuePaint.textSize = 9f
        canvas.drawText(worker.name, MARGIN, y, valuePaint)
        canvas.drawLine(MARGIN, y + 2f, PAGE_W - MARGIN, y + 2f, thin)
        y += 14f

        // CURP grid (18 casillas)
        labelPaint.textSize = 5f
        canvas.drawText("Clave Única de Registro de Población", MARGIN, y, labelPaint)
        y += 6f
        drawCharGrid(canvas, worker.curp.uppercase().padEnd(18).take(18), MARGIN, y, 18, 12f, 14f, thin, valuePaint)

        // Ocupación al lado derecho del CURP
        val occX = MARGIN + 18 * 12f + 8f
        labelPaint.textSize = 5f
        canvas.drawText("Ocupación específica (Catálogo Nacional de Ocupaciones)", occX, y - 2f, labelPaint)
        y += 16f
        val occText = if (occupationCode.isNotEmpty()) "$occupationCode ${worker.occupation}" else worker.occupation
        valuePaint.textSize = 7f
        canvas.drawText(occText, occX, y, valuePaint)
        canvas.drawLine(occX, y + 2f, PAGE_W - MARGIN, y + 2f, thin)
        y += 12f

        // Puesto
        labelPaint.textSize = 5f
        canvas.drawText("Puesto", MARGIN, y, labelPaint)
        y += 7f
        valuePaint.textSize = 8f
        canvas.drawText(worker.position, MARGIN, y, valuePaint)
        canvas.drawLine(MARGIN, y + 2f, PAGE_W - MARGIN, y + 2f, thin)
        y += 16f

        // ── Sección 2: DATOS DE LA EMPRESA ──
        drawSectionBar(canvas, "DATOS DE LA EMPRESA", y, gray, bold)
        y += 14f

        // Nombre o razón social
        labelPaint.textSize = 6f
        canvas.drawText("Nombre o razón social (En caso de persona física, anotar apellido paterno, materno y nombre(s))", MARGIN, y, labelPaint)
        y += 8f
        valuePaint.textSize = 9f
        canvas.drawText(companyName, MARGIN, y, valuePaint)
        canvas.drawLine(MARGIN, y + 2f, PAGE_W - MARGIN, y + 2f, thin)
        y += 14f

        // RFC grid (13 casillas con homoclave)
        labelPaint.textSize = 5f
        canvas.drawText("Registro Federal de Contribuyentes con homoclave (SHCP)", MARGIN, y, labelPaint)
        y += 6f
        drawCharGrid(canvas, companyRfc.uppercase().padEnd(13).take(13), MARGIN, y, 13, 14f, 14f, thin, valuePaint)
        y += 22f

        // ── Sección 3: DATOS DEL PROGRAMA DE CAPACITACIÓN ──
        drawSectionBar(canvas, "DATOS DEL PROGRAMA DE CAPACITACIÓN, ADIESTRAMIENTO Y PRODUCTIVIDAD", y, gray, bold)
        y += 14f

        // Nombre del curso
        labelPaint.textSize = 6f
        canvas.drawText("Nombre del curso", MARGIN, y, labelPaint)
        y += 8f
        valuePaint.textSize = 9f
        canvas.drawText(course.name, MARGIN, y, valuePaint)
        canvas.drawLine(MARGIN, y + 2f, PAGE_W - MARGIN, y + 2f, thin)
        y += 14f

        // Duración en horas
        labelPaint.textSize = 5f
        canvas.drawText("Duración en horas", MARGIN, y, labelPaint)
        y += 7f
        valuePaint.textSize = 8f
        canvas.drawText("${course.duration_hours} HORAS", MARGIN, y, valuePaint)
        canvas.drawLine(MARGIN, y + 2f, MARGIN + 80f, y + 2f, thin)
        y += 14f

        // Periodo de ejecución
        labelPaint.textSize = 5f
        canvas.drawText("Periodo de ejecución:", MARGIN, y, labelPaint)
        y += 8f
        valuePaint.textSize = 7f
        canvas.drawText("De: $startDate    a: $endDate", MARGIN + 60f, y, valuePaint)
        canvas.drawLine(MARGIN + 55f, y + 2f, PAGE_W - MARGIN, y + 2f, thin)
        y += 14f

        // Área temática
        labelPaint.textSize = 5f
        canvas.drawText("Área temática del curso", MARGIN, y, labelPaint)
        y += 8f
        valuePaint.textSize = 8f
        canvas.drawText(thematicArea, MARGIN, y, valuePaint)
        canvas.drawLine(MARGIN, y + 2f, PAGE_W - MARGIN, y + 2f, thin)
        y += 14f

        // Nombre del agente capacitador o STPS
        labelPaint.textSize = 5f
        canvas.drawText("Nombre del agente capacitador o STPS", MARGIN, y, labelPaint)
        y += 8f
        valuePaint.textSize = 8f
        canvas.drawText("${agent.name}    ${agent.stps}", MARGIN, y, valuePaint)
        canvas.drawLine(MARGIN, y + 2f, PAGE_W - MARGIN, y + 2f, thin)
        y += 24f

        // ── Firmas ──
        val sigY = 680f
        thin.strokeWidth = 1f
        // Instructor
        canvas.drawLine(50f, sigY, 180f, sigY, thin)
        labelPaint.textSize = 7f
        canvas.drawText("Instructor o tutor", 85f, sigY + 12f, labelPaint)
        valuePaint.textSize = 7f
        canvas.drawText(agent.name, 60f, sigY - 4f, valuePaint)

        // Patrón
        canvas.drawLine(210f, sigY, 380f, sigY, thin)
        labelPaint.textSize = 7f
        canvas.drawText("Patrón o representante legal", 230f, sigY + 12f, labelPaint)

        // Representante trabajadores
        canvas.drawLine(410f, sigY, 550f, sigY, thin)
        labelPaint.textSize = 7f
        canvas.drawText("Representante de los trabajadores", 415f, sigY + 12f, labelPaint)

        // Nota legal
        labelPaint.textSize = 5f
        canvas.drawText("Aviso de privacidad: En el acto de firma, se presume la conformidad del trabajador con la información asentada.", MARGIN, 720f, labelPaint)
        canvas.drawText("Serán sancionados aquellos que no se conduzcan con verdad.", MARGIN, 728f, labelPaint)
    }

    private fun drawReverso(canvas: Canvas) {
        val thin = Paint().apply { color = Color.BLACK; strokeWidth = 0.6f; isAntiAlias = true }
        val bold = Paint().apply { color = Color.BLACK; isFakeBoldText = true; textSize = 8f; isAntiAlias = true }
        val normal = Paint().apply { color = Color.BLACK; textSize = 6f; isAntiAlias = true }

        var y = 30f

        // ── Título ──
        bold.textSize = 10f
        canvas.drawText("CLAVES Y DENOMINACIONES DE ÁREAS Y SUBÁREAS DEL CATÁLOGO NACIONAL DE OCUPACIONES", 30f, y, bold)
        y += 14f

        // Tabla de áreas ocupacionales (2 columnas)
        val occupations = listOf(
            "01" to "Cultivo, crianza y aprovechamiento",
            "01.1" to "Agricultura y silvicultura",
            "01.2" to "Ganadería",
            "01.3" to "Pesca y acuacultura",
            "02" to "Extracción y suministro",
            "02.1" to "Exploración",
            "02.2" to "Extracción",
            "02.3" to "Refinación y beneficio",
            "02.4" to "Provisión de energía",
            "02.5" to "Provisión de agua",
            "03" to "Construcción",
            "03.1" to "Planeación y dirección de obras",
            "03.2" to "Edificación y urbanización",
            "03.3" to "Acabado",
            "03.4" to "Instalación y mantenimiento",
            "04" to "Tecnología",
            "04.1" to "Mecánica",
            "04.2" to "Electricidad",
            "04.3" to "Electrónica",
            "04.4" to "Informática",
            "04.5" to "Telecomunicaciones",
            "04.6" to "Procesos industriales",
            "05" to "Procesamiento y fabricación",
            "05.1" to "Minerales no metálicos",
            "05.2" to "Metales",
            "05.3" to "Alimentos y bebidas",
            "05.4" to "Textiles y prendas de vestir",
            "05.5" to "Materia orgánica",
            "05.6" to "Productos químicos",
            "05.7" to "Productos metálicos y de hule y plástico",
            "05.8" to "Productos eléctricos y electrónicos",
            "05.9" to "Productos impresos"
        )
        val occupations2 = listOf(
            "06" to "Transporte",
            "06.1" to "Ferroviario",
            "06.2" to "Autotransporte",
            "06.3" to "Aéreo",
            "06.4" to "Marítimo y fluvial",
            "06.5" to "Servicios de apoyo",
            "07" to "Provisión de bienes y servicios",
            "07.1" to "Comercio",
            "07.2" to "Alimentación y hospedaje",
            "07.3" to "Turismo",
            "07.4" to "Deporte y esparcimiento",
            "07.5" to "Servicios personales",
            "07.6" to "Reparación de artículos de uso doméstico y personal",
            "07.7" to "Limpieza",
            "07.8" to "Servicio postal y mensajería",
            "08" to "Gestión y soporte administrativo",
            "08.1" to "Bolsa, banca y seguros",
            "08.2" to "Administración",
            "08.3" to "Servicios legales",
            "09" to "Salud y protección social",
            "09.1" to "Servicios médicos",
            "09.2" to "Inspección sanitaria y del medio ambiente",
            "09.3" to "Seguridad social",
            "09.4" to "Protección de bienes y/o personas",
            "10" to "Comunicación",
            "10.1" to "Publicación",
            "10.2" to "Radio, cine, televisión y teatro",
            "10.3" to "Interpretación artística",
            "10.4" to "Traducción e interpretación lingüística",
            "10.5" to "Publicidad, propaganda y relaciones públicas",
            "11" to "Desarrollo y extensión del conocimiento",
            "11.1" to "Investigación",
            "11.2" to "Enseñanza",
            "11.3" to "Difusión cultural"
        )

        // Header de la tabla
        drawTableCell(canvas, "CLAVE DEL\nÁREA/SUBÁREA", 30f, y, 60f, 10f, thin, bold, 5f)
        drawTableCell(canvas, "DENOMINACIÓN", 90f, y, 200f, 10f, thin, bold, 5f)
        drawTableCell(canvas, "CLAVE DEL\nÁREA/SUBÁREA", 300f, y, 60f, 10f, thin, bold, 5f)
        drawTableCell(canvas, "DENOMINACIÓN", 360f, y, 200f, 10f, thin, bold, 5f)
        y += 12f

        val maxRows = maxOf(occupations.size, occupations2.size)
        for (i in 0 until maxRows) {
            val rowH = 8f
            // Columna izquierda
            if (i < occupations.size) {
                drawTableCell(canvas, occupations[i].first, 30f, y, 60f, rowH, thin, normal, 5f)
                drawTableCell(canvas, occupations[i].second, 90f, y, 210f, rowH, thin, normal, 5f)
            } else {
                drawTableCell(canvas, "", 30f, y, 60f, rowH, thin, normal, 5f)
                drawTableCell(canvas, "", 90f, y, 210f, rowH, thin, normal, 5f)
            }
            // Columna derecha
            if (i < occupations2.size) {
                drawTableCell(canvas, occupations2[i].first, 300f, y, 60f, rowH, thin, normal, 5f)
                drawTableCell(canvas, occupations2[i].second, 360f, y, 210f, rowH, thin, normal, 5f)
            } else {
                drawTableCell(canvas, "", 300f, y, 60f, rowH, thin, normal, 5f)
                drawTableCell(canvas, "", 360f, y, 210f, rowH, thin, normal, 5f)
            }
            y += rowH
        }

        y += 10f

        // ── Áreas temáticas ──
        bold.textSize = 8f
        canvas.drawText("CLAVES Y DENOMINACIONES DEL CATÁLOGO DE ÁREAS TEMÁTICAS DE LOS CURSOS", 30f, y, bold)
        y += 12f

        val thematic = listOf(
            "1000" to "Producción general",
            "2000" to "Servicios",
            "3000" to "Administración, contabilidad y economía",
            "4000" to "Comercialización",
            "5000" to "Mantenimiento y reparación",
            "6000" to "Seguridad",
            "7000" to "Desarrollo personal y familiar",
            "8000" to "Uso de tecnologías de la información y comunicación",
            "9000" to "Participación social"
        )

        // Header
        drawTableCell(canvas, "CLAVE DEL\nÁREA", 30f, y, 60f, 10f, thin, bold, 5f)
        drawTableCell(canvas, "DENOMINACIÓN", 90f, y, 210f, 10f, thin, bold, 5f)
        drawTableCell(canvas, "CLAVE DEL\nÁREA", 300f, y, 60f, 10f, thin, bold, 5f)
        drawTableCell(canvas, "DENOMINACIÓN", 360f, y, 210f, 10f, thin, bold, 5f)
        y += 12f

        val half = (thematic.size + 1) / 2
        for (i in 0 until half) {
            val rowH = 10f
            // Izquierda
            if (i < thematic.size) {
                drawTableCell(canvas, thematic[i].first, 30f, y, 60f, rowH, thin, normal, 5f)
                drawTableCell(canvas, thematic[i].second, 90f, y, 210f, rowH, thin, normal, 5f)
            }
            // Derecha
            val j = i + half
            if (j < thematic.size) {
                drawTableCell(canvas, thematic[j].first, 300f, y, 60f, rowH, thin, normal, 5f)
                drawTableCell(canvas, thematic[j].second, 360f, y, 210f, rowH, thin, normal, 5f)
            } else {
                drawTableCell(canvas, "", 300f, y, 60f, rowH, thin, normal, 5f)
                drawTableCell(canvas, "", 360f, y, 210f, rowH, thin, normal, 5f)
            }
            y += rowH
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────
    private fun drawSectionBar(canvas: Canvas, title: String, y: Float, gray: Paint, bold: Paint) {
        canvas.drawRect(40f, y - 2f, 555f, y + 8f, gray)
        bold.textSize = 7f
        canvas.drawText(title, 44f, y + 6f, bold)
    }

    private fun drawCharGrid(
        canvas: Canvas, text: String, x: Float, y: Float,
        count: Int, boxW: Float, boxH: Float, line: Paint, textPaint: Paint
    ) {
        for (i in 0 until count) {
            val cx = x + (i * boxW)
            canvas.drawRect(cx, y, cx + boxW, y + boxH, line)
            if (i < text.length) {
                val ch = text[i]
                val tw = textPaint.measureText(ch.toString())
                canvas.drawText(ch.toString(), cx + (boxW - tw) / 2f, y + boxH - 3f, textPaint)
            }
        }
    }

    private fun drawTableCell(
        canvas: Canvas, text: String, x: Float, y: Float,
        w: Float, h: Float, line: Paint, textPaint: Paint, textSize: Float = 6f
    ) {
        canvas.drawRect(x, y, x + w, y + h, line)
        textPaint.textSize = textSize
        // Soportar \n
        val lines = text.split("\n")
        lines.forEachIndexed { i, ln ->
            canvas.drawText(ln, x + 3f, y + h - 4f - (lines.size - 1 - i) * (textSize + 1f), textPaint)
        }
    }
}
