package com.example.dc5control.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.dc5control.data.model.Agent
import com.example.dc5control.data.model.Course
import com.example.dc5control.data.model.Employee
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import java.util.Locale

object DiplomaGenerator {
    private const val TAG = "DiplomaGenerator"
    private const val TEMPLATE_NAME = "plantilla_diploma.png"
    private const val PW = 792f // Letter Landscape width
    private const val PH = 612f // Letter Landscape height

    private fun calculateStps(agentStps: String, courseStpsId: String?): String {
        val base = agentStps.removePrefix("STPS-").removePrefix("STPS-").trim()
        if (courseStpsId.isNullOrBlank()) return "STPS-$base"
        
        val parts = base.split("-")
        if (parts.size < 2) return "STPS-$base-$courseStpsId"
        
        val baseWithoutSuffix = parts.dropLast(1).joinToString("-")
        return "STPS-$baseWithoutSuffix-$courseStpsId"
    }

    fun generateDiploma(
        context: Context,
        employee: Employee,
        course: Course,
        agent: Agent,
        startDate: String,
        endDate: String
    ): File {
        PDFBoxResourceLoader.init(context)
        val document = PDDocument()
        val page = PDPage(PDRectangle(PW, PH))
        document.addPage(page)

        val cs = PDPageContentStream(document, page)

        // 1. Cargar imagen de fondo
        try {
            val inputStream = context.assets.open(TEMPLATE_NAME)
            val bytes = inputStream.readBytes()
            inputStream.close()
            val img = PDImageXObject.createFromByteArray(document, bytes, "template")
            cs.drawImage(img, 0f, 0f, PW, PH)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading template image", e)
        }

        // Fuentes
        val font = PDType1Font.HELVETICA
        val fontB = PDType1Font.HELVETICA_BOLD

        fun textCentered(xC: Float, yF: Float, t: String, sz: Float, bold: Boolean = false, color: Int = 0) {
            if (t.isBlank()) return
            val f = if (bold) fontB else font
            val st = t.uppercase()
            val w = f.getStringWidth(st) / 1000 * sz
            cs.beginText()
            cs.setFont(f, sz)
            if (color == 1) cs.setNonStrokingColor(25, 51, 102) // Azul marino
            else cs.setNonStrokingColor(0, 0, 0)
            cs.newLineAtOffset(xC - (w / 2f), PH - yF)
            cs.showText(st)
            cs.endText()
            cs.setNonStrokingColor(0, 0, 0)
        }

        val isDario = agent.name.contains("Dario", ignoreCase = true)

        if (isDario) {
            // --- DISEÑO AJUSTADO EHS SOLUTIONS (DARIO) ---
            
            // 1. Nombre del Trabajador: ENCIMA de la línea (Y=245)
            val workerName = "${employee.nombres} ${employee.apellidoPaterno} ${employee.apellidoMaterno}".trim()
            textCentered(PW / 2f, 245f, workerName, 28f, true, 1)

            // 2. Nombre del Curso: Abajo de "Por haber concluido satisfactoriamente..." (Y=330)
            textCentered(PW / 2f, 330f, course.name, 18f, true)

            // 3. Duración: Abajo de "Con duración de" (Y=405)
            textCentered(PW / 2f, 405f, course.durationHours, 12f, true)

            // 4. Fecha: Abajo de "Del" (Y=445)
            textCentered(PW / 2f, 445f, formatDateRange(startDate, endDate), 11f, true)

            // 5. Datos del Agente (Sobre placeholders)
            textCentered(PW / 2f, 572f, "JESUS DARIO Robles Trujillo", 10f, true)
            
            val finalStps = calculateStps(agent.stps, course.stpsId)
            textCentered(PW / 2f, 584f, "REGISTRO $finalStps", 8f, true)

        } else {
            // --- DISEÑO GENÉRICO (OTROS) ---
            val workerName = "${employee.nombres} ${employee.apellidoPaterno} ${employee.apellidoMaterno}".trim()
            textCentered(PW / 2f, 325f, workerName, 28f, true)
            textCentered(PW / 2f, 445f, course.name, 18f, true)
            val dateText = "CON DURACIÓN DE ${course.durationHours} DEL ${formatDateRange(startDate, endDate)}"
            textCentered(PW / 2f, 480f, dateText, 11f)
            
            textCentered(PW / 2f, 570f, agent.name, 10f, true)
            val finalStps = calculateStps(agent.stps, course.stpsId)
            textCentered(PW / 2f, 582f, "REGISTRO $finalStps", 8f)
        }

        cs.close()

        val fileName = "Diploma_${employee.curp}_${course.name.take(10).replace(" ","_")}.pdf"
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val file = File(downloadsDir, fileName)
        document.save(file)
        document.close()

        return file
    }

    private fun formatDateRange(start: String, end: String): String {
        // start no se usa por ahora pero se mantiene por si se requiere "Del X al Y"
        Log.d(TAG, "Formateando fecha desde: $start")
        return try {
            val parts = end.split("/")
            val day = parts[0]
            val month = getMonthName(parts[1].toInt())
            val year = parts[2]
            "$day DE $month DEL $year"
        } catch (ex: Exception) {
            end.uppercase()
        }
    }

    private fun getMonthName(m: Int): String {
        return when (m) {
            1 -> "ENERO"; 2 -> "FEBRERO"; 3 -> "MARZO"; 4 -> "ABRIL"
            5 -> "MAYO"; 6 -> "JUNIO"; 7 -> "JULIO"; 8 -> "AGOSTO"
            9 -> "SEPTIEMBRE"; 10 -> "OCTUBRE"; 11 -> "NOVIEMBRE"; 12 -> "DICIEMBRE"
            else -> ""
        }
    }
}
