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

    private fun calculateStps(agentStps: String): String {
        val base = agentStps.removePrefix("STPS-").removePrefix("STPS-").trim()
        return "STPS-$base"
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
        design: com.example.dc5control.data.model.AgentDesign? = null,
        qrUrl: String? = null
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
            val bleed = 10f // Aumentamos el sangrado para asegurar cobertura total
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

        fun textCentered(xC: Float, yF: Float, t: String, sz: Float, bold: Boolean = false, colorType: Int = 0, maxWidth: Float = 0f) {
            if (t.isBlank()) return
            val f = if (bold) fontB else font
            val st = t.toUpperCase(Locale.ROOT)
            
            var currentSz = sz
            var w = f.getStringWidth(st) / 1000 * currentSz
            
            if (maxWidth > 0 && w > maxWidth) {
                currentSz = (maxWidth / w) * sz
                w = f.getStringWidth(st) / 1000 * currentSz
            }

            cs.beginText()
            cs.setFont(f, currentSz)
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

        val centerX = PW / 2f
        
        // Use design coordinates if available, otherwise use defaults
        val workerX = design?.dipWorkerX ?: centerX
        val workerY = design?.dipWorkerY ?: 245f
        val workerSz = design?.dipWorkerSz ?: 28f

        val courseX = design?.dipCourseX ?: centerX
        val courseY = design?.dipCourseY ?: 330f
        val courseSz = design?.dipCourseSz ?: 18f

        val durationX = design?.dipDurationX ?: centerX
        val durationY = design?.dipDurationY ?: 405f
        val durationSz = design?.dipDurationSz ?: 12f

        val dateX = design?.dipDateX ?: centerX
        val dateY = design?.dipDateY ?: 445f
        val dateSz = design?.dipDateSz ?: 11f

        val agentX = design?.dipAgentX ?: centerX
        val agentY = design?.dipAgentY ?: 572f
        val agentSz = design?.dipAgentSz ?: 10f

        val stpsX = design?.dipStpsX ?: centerX
        val stpsY = design?.dipStpsY ?: 584f
        val stpsSz = design?.dipStpsSz ?: 8f

        val folioX = design?.dipFolioX ?: 396f
        val folioY = design?.dipFolioY ?: 550f
        val folioSz = design?.dipFolioSz ?: 10f

        val workerName = "${employee.nombres} ${employee.apellidoPaterno} ${employee.apellidoMaterno}".trim()
        textCentered(workerX, workerY, workerName, workerSz, true, 1, 450f)

        textCentered(courseX, courseY, course.name, courseSz, true, 0, 450f)
        textCentered(durationX, durationY, course.durationHours, durationSz, true)
        textCentered(dateX, dateY, formatDateRange(startDate, endDate), dateSz, true)

        textCentered(agentX, agentY, design?.agentName ?: agent.name, agentSz, true)

        val finalStps = calculateStps(agent.stps)
        textCentered(stpsX, stpsY, "REGISTRO $finalStps", stpsSz, true)

        agent.cedulaProfesional?.let { cp ->
            val cedX = design?.dipCedulaX ?: centerX
            val cedY = design?.dipCedulaY ?: 596f
            val cedSz = design?.dipCedulaSz ?: 8f
            textCentered(cedX, cedY, "CÉDULA PROFESIONAL: $cp", cedSz, true)
        }

        folio?.let { textCentered(folioX, folioY, it, folioSz, true, 2) }

        // QR Code
        qrUrl?.let { url ->
            QRGenerator.generateQR(url)?.let { qrBitmap ->
                val stream = java.io.ByteArrayOutputStream()
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val img = PDImageXObject.createFromByteArray(document, stream.toByteArray(), "qr")
                val qsz = design?.dipQrSz ?: 50f
                val qx = design?.dipQrX ?: 680f
                val qy = design?.dipQrY ?: 500f
                cs.drawImage(img, qx - qsz/2, PH - qy - qsz/2, qsz, qsz)
            }
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