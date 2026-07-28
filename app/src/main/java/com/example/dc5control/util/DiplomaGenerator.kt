package com.example.dc5control.util

import android.content.Context
import android.graphics.Bitmap
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
        endDate: String,
        customTemplateBitmap: Bitmap? = null,
        folio: String? = null,
        folioX: Float = 396f,
        folioY: Float = 550f,
        folioSz: Float = 10f
    ): File {
        PDFBoxResourceLoader.init(context)
        val document = PDDocument()
        val page = PDPage(PDRectangle(PW, PH))
        document.addPage(page)

        val cs = PDPageContentStream(document, page)

        // 0. Rellenar fondo con color oscuro para evitar bordes blancos
        cs.setNonStrokingColor(10, 20, 40) // Azul muy oscuro (casi negro)
        cs.addRect(0f, 0f, PW, PH)
        cs.fill()

        // 1. Cargar imagen de fondo forzando cobertura total
        try {
            val bleed = 5f 
            val imgXObject = if (customTemplateBitmap != null) {
                val stream = java.io.ByteArrayOutputStream()
                customTemplateBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                PDImageXObject.createFromByteArray(document, stream.toByteArray(), "custom_template")
            } else {
                val inputStream = context.assets.open(TEMPLATE_NAME)
                val bytes = inputStream.readBytes()
                inputStream.close()
                PDImageXObject.createFromByteArray(document, bytes, "template")
            }
            
            // Dibujamos la imagen ligeramente más grande y centrada para ocultar cualquier margen
            cs.drawImage(imgXObject, -bleed, -bleed, PW + (bleed * 2), PH + (bleed * 2))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading template image", e)
        }

        // Fuentes
        val font = PDType1Font.HELVETICA
        val fontB = PDType1Font.HELVETICA_BOLD

        fun textCentered(xC: Float, yF: Float, t: String, sz: Float, bold: Boolean = false, colorType: Int = 0) {
            if (t.isBlank()) return
            val f = if (bold) fontB else font
            val st = t.uppercase()
            val w = f.getStringWidth(st) / 1000 * sz
            
            cs.beginText()
            cs.setFont(f, sz)
            when (colorType) {
                1 -> cs.setNonStrokingColor(25, 51, 102) // Azul marino
                2 -> cs.setNonStrokingColor(76, 102, 0) // Verde oliva para folio
                else -> cs.setNonStrokingColor(0, 0, 0)
            }
            cs.newLineAtOffset(xC - (w / 2f), PH - yF)
            cs.showText(st)
            cs.endText()
            cs.setNonStrokingColor(0, 0, 0)
        }

        val isDario = agent.name.contains("Dario", ignoreCase = true)
        val centerX = PW / 2f

        if (isDario) {
            // --- DISEÑO AJUSTADO EHS SOLUTIONS (DARIO) ---
            
            // 1. Nombre del Trabajador: ENCIMA de la línea (Y=245)
            val workerName = "${employee.nombres} ${employee.apellidoPaterno} ${employee.apellidoMaterno}".trim()
            textCentered(centerX, 245f, workerName, 28f, true, 1)

            // 2. Nombre del Curso: Abajo de "Por haber concluido satisfactoriamente..." (Y=330)
            textCentered(centerX, 330f, course.name, 18f, true)

            // 3. Duración: Abajo de "Con duración de" (Y=405)
            textCentered(centerX, 405f, course.durationHours, 12f, true)

            // 4. Fecha: Abajo de "Del" (Y=445)
            textCentered(centerX, 445f, formatDateRange(startDate, endDate), 11f, true)

            // 5. Datos del Agente (Sobre placeholders)
            textCentered(centerX, 572f, "JESUS DARIO Robles Trujillo", 10f, true)
            
            val finalStps = calculateStps(agent.stps, course.stpsId)
            textCentered(centerX, 584f, "REGISTRO $finalStps", 8f, true)

            // 6. Folio
            folio?.let { textCentered(folioX, folioY, "folio: $it", folioSz, true, 2) }

        } else {
            // --- DISEÑO GENÉRICO (OTROS) ---
            val workerName = "${employee.nombres} ${employee.apellidoPaterno} ${employee.apellidoMaterno}".trim()
            textCentered(centerX, 325f, workerName, 28f, true)
            textCentered(centerX, 445f, course.name, 18f, true)
            val dateText = "CON DURACIÓN DE ${course.durationHours} DEL ${formatDateRange(startDate, endDate)}"
            textCentered(centerX, 480f, dateText, 11f)
            
            textCentered(centerX, 570f, agent.name, 10f, true)
            val finalStps = calculateStps(agent.stps, course.stpsId)
            textCentered(centerX, 582f, "REGISTRO $finalStps", 8f)

            // 6. Folio
            folio?.let { textCentered(folioX, folioY, "folio: $it", folioSz, true, 2) }
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
