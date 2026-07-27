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
    private const val TEMPLATE_NAME = "Plantilla diploma.png"
    private const val PW = 792f // Letter Landscape width
    private const val PH = 612f // Letter Landscape height

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

        fun textCentered(xC: Float, yF: Float, t: String, sz: Float, bold: Boolean = false) {
            if (t.isBlank()) return
            val f = if (bold) fontB else font
            val st = t.uppercase()
            val w = f.getStringWidth(st) / 1000 * sz
            cs.beginText()
            cs.setFont(f, sz)
            cs.newLineAtOffset(xC - (w / 2f), PH - yF)
            cs.showText(st)
            cs.endText()
        }

        // 2. Limpiar zona de Cynthia (Rectángulo blanco sobre la firma y datos actuales)
        // Estimación: x=340-540, y=470-580
        cs.setNonStrokingColor(1f, 1f, 1f)
        cs.addRect(330f, PH - 585f, 220f, 100f) 
        cs.fill()
        cs.setNonStrokingColor(0f, 0f, 0f)

        // 3. Escribir Datos Dinámicos
        
        // Trabajador (Grande y Azul oscuro/Negro)
        val workerName = "${employee.nombres} ${employee.apellidoPaterno} ${employee.apellidoMaterno}".trim()
        textCentered(PW / 2f, 325f, workerName, 28f, true)

        // Nombre del Curso
        textCentered(PW / 2f, 445f, course.name, 18f, true)

        // Duración y Fecha
        val dateText = "CON DURACIÓN DE ${course.durationHours} HORAS DEL ${formatDateRange(startDate, endDate)}"
        textCentered(PW / 2f, 480f, dateText, 11f)

        // 4. Datos de Jesus Dario (En el área limpia)
        textCentered(440f, 540f, "Jesus Dario Robles Trujillo", 10f, true)
        textCentered(440f, 555f, "REGISTRO STPS ${agent.stps}", 8f)

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
