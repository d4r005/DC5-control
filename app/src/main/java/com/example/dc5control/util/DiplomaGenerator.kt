package com.example.dc5control.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Base64
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale

object DiplomaGenerator {
    private const val TAG = "DiplomaGenerator"
    private const val TEMPLATE_NAME = "plantilla_diploma.png"
    private const val PW = 792f
    private const val PH = 612f

    private fun calculateStps(agentStps: String): String {
        val base = agentStps.removePrefix("STPS-").removePrefix("STPS-").trim()
        return "STPS-$base"
    }

    // Extract raw bytes from base64 (preserves original quality, same as web's embedBase64Img)
    private fun base64ToImageBytes(base64: String): ByteArray? {
        return try {
            val data = base64.substringAfter("base64,")
            Base64.decode(data, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding base64 image", e)
            null
        }
    }

    fun generateDiploma(
        context: Context,
        employee: Employee,
        course: Course,
        agent: Agent,
        startDate: String,
        endDate: String,
        customTemplateBase64: String? = null,
        folio: String? = null,
        design: com.example.dc5control.data.model.AgentDesign? = null,
        qrUrl: String? = null
    ): File {
        PDFBoxResourceLoader.init(context)
        val document = PDDocument()
        val page = PDPage(PDRectangle(PW, PH))
        document.addPage(page)

        val cs = PDPageContentStream(document, page)

        // 1. Dibujar plantilla — igual que web: x=0, y=0, full page (sin fondo oscuro ni sangrado)
        try {
            val imgBytes = if (!customTemplateBase64.isNullOrBlank()) {
                base64ToImageBytes(customTemplateBase64)
            } else {
                val inputStream = context.assets.open(TEMPLATE_NAME)
                val bytes = inputStream.readBytes()
                inputStream.close()
                bytes
            }
            if (imgBytes != null) {
                val img = PDImageXObject.createFromByteArray(document, imgBytes, "template")
                cs.drawImage(img, 0f, 0f, PW, PH)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading template image", e)
        }

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
                1 -> cs.setNonStrokingColor(25, 51, 102) // Azul marino (worker name)
                else -> cs.setNonStrokingColor(0, 0, 0)  // Negro (igual que web)
            }
            cs.newLineAtOffset(xC - (w / 2f), PH - yF)
            cs.showText(st)
            cs.endText()
            cs.setNonStrokingColor(0, 0, 0)
        }

        val centerX = PW / 2f

        // Usar coordenadas del diseño si disponibles
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

        // 1. Nombre del Trabajador (nombres + apellidoPaterno + apellidoMaterno, igual que web)
        val workerName = "${employee.nombres} ${employee.apellidoPaterno} ${employee.apellidoMaterno}".trim()
        textCentered(workerX, workerY, workerName, workerSz, true, 1, 450f)

        // 2. Nombre del Curso
        textCentered(courseX, courseY, course.name, courseSz, true, 0, 450f)

        // 3. Duracion
        textCentered(durationX, durationY, course.durationHours, durationSz, true)

        // 4. Fecha
        textCentered(dateX, dateY, formatDateRange(endDate), dateSz, true)

        // 5. Firma del agente — igual que web: x = agentX - 45, y = PH - agentY + 10, w=90, h=50
        design?.firmaBase64?.let { base64 ->
            try {
                val bytes = base64ToImageBytes(base64)
                if (bytes != null) {
                    val img = PDImageXObject.createFromByteArray(document, bytes, "firma")
                    cs.drawImage(img, agentX - 45f, PH - agentY + 10f, 90f, 50f)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error dibujando firma en diploma", e)
            }
        }

        // 6. Nombre del agente
        textCentered(agentX, agentY, agent.name, agentSz, true)

        // 7. STPS
        val finalStps = calculateStps(agent.stps)
        textCentered(stpsX, stpsY, "REGISTRO $finalStps", stpsSz, true)

        // 8. Cedula profesional
        agent.cedulaProfesional?.let { cp ->
            val cedX = design?.dipCedulaX ?: centerX
            val cedY = design?.dipCedulaY ?: 596f
            val cedSz = design?.dipCedulaSz ?: 8f
            textCentered(cedX, cedY, "CÉDULA PROFESIONAL: $cp", cedSz, true)
        }

        // 9. Folio — NEGRO (igual que web, antes era verde)
        folio?.let { textCentered(folioX, folioY, it, folioSz, true) }

        // 10. QR Code
        qrUrl?.let { url ->
            QRGenerator.generateQR(url)?.let { qrBitmap ->
                try {
                    val stream = ByteArrayOutputStream()
                    qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val img = PDImageXObject.createFromByteArray(document, stream.toByteArray(), "qr")
                    val qsz = design?.dipQrSz ?: 50f
                    val qx = design?.dipQrX ?: 680f
                    val qy = design?.dipQrY ?: 500f
                    cs.drawImage(img, qx - qsz/2, PH - qy - qsz/2, qsz, qsz)
                } catch (e: Exception) {
                    Log.e(TAG, "Error embedding QR", e)
                }
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

    private fun formatDateRange(end: String): String {
        Log.d(TAG, "Formateando fecha: $end")
        return try {
            val parts = if (end.contains("-")) end.split("-") else end.split("/")
            val day = parts.getOrElse(if (end.contains("-")) 2 else 0) { "  " }
            val monthNum = parts.getOrElse(if (end.contains("-")) 1 else 1) { "1" }.toIntOrNull() ?: 1
            val year = parts.getOrElse(if (end.contains("-")) 0 else 2) { "    " }
            "$day DE ${getMonthName(monthNum)} DEL $year"
        } catch (ex: Exception) {
            end.uppercase()
        }
    }

    private fun getMonthName(m: Int): String = when (m) {
        1 -> "ENERO"; 2 -> "FEBRERO"; 3 -> "MARZO"; 4 -> "ABRIL"
        5 -> "MAYO"; 6 -> "JUNIO"; 7 -> "JULIO"; 8 -> "AGOSTO"
        9 -> "SEPTIEMBRE"; 10 -> "OCTUBRE"; 11 -> "NOVIEMBRE"; 12 -> "DICIEMBRE"
        else -> ""
    }
}
