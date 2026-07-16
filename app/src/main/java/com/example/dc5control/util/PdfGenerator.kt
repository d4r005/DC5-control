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

object PdfGenerator {
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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // --- DRAWING FRONT (ANVERSO) ---
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("FORMATO DC-3", 240f, 50f, paint)
        paint.textSize = 10f
        canvas.drawText("CONSTANCIA DE COMPETENCIAS O DE HABILIDADES LABORALES", 140f, 70f, paint)

        // Worker Data
        drawSectionHeader(canvas, "DATOS DEL TRABAJADOR", 50f, 100f, paint)
        paint.isFakeBoldText = false
        drawField(canvas, "Nombre:", worker.name, 60f, 130f, paint)
        
        // CURP Grid Simulation
        drawCurpGrid(canvas, worker.curp, 60f, 150f, paint)

        drawField(canvas, "Ocupación específica:", worker.occupation, 60f, 190f, paint)
        drawField(canvas, "Puesto:", worker.position, 60f, 210f, paint)

        // Company Data
        drawSectionHeader(canvas, "DATOS DE LA EMPRESA", 50f, 250f, paint)
        drawField(canvas, "Nombre o razón social:", companyName, 60f, 280f, paint)
        drawRfcGrid(canvas, companyRfc, 60f, 300f, paint)

        // Course Data
        drawSectionHeader(canvas, "DATOS DEL PROGRAMA DE CAPACITACIÓN", 50f, 350f, paint)
        drawField(canvas, "Nombre del curso:", course.name, 60f, 380f, paint)
        drawField(canvas, "Duración en horas:", "${course.durationHours}", 60f, 400f, paint)
        drawField(canvas, "Periodo de ejecución:", "De $startDate a $endDate", 60f, 420f, paint)
        drawField(canvas, "Área temática del curso:", course.thematicArea, 60f, 440f, paint)
        drawField(canvas, "Nombre del agente capacitador:", agent.name, 60f, 460f, paint)
        drawField(canvas, "Registro STPS:", agent.stpsRegistry, 60f, 480f, paint)

        // Signatures
        paint.textSize = 8f
        canvas.drawLine(60f, 650f, 200f, 650f, paint)
        canvas.drawText("Instructor o Tutor", 90f, 665f, paint)
        
        canvas.drawLine(240f, 650f, 380f, 650f, paint)
        canvas.drawText("Patrón o representante legal", 250f, 665f, paint)

        canvas.drawLine(420f, 650f, 550f, 650f, paint)
        canvas.drawText("Representante de los trabajadores", 425f, 665f, paint)

        pdfDocument.finishPage(page)
    }

    private fun drawSectionHeader(canvas: Canvas, title: String, x: Float, y: Float, paint: Paint) {
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        paint.textSize = 10f
        canvas.drawRect(x, y - 15f, 545f, y + 5f, Paint().apply { color = Color.LTGRAY; alpha = 100 })
        canvas.drawText(title, x + 10f, y, paint)
    }

    private fun drawField(canvas: Canvas, label: String, value: String, x: Float, y: Float, paint: Paint) {
        paint.isFakeBoldText = true
        canvas.drawText(label, x, y, paint)
        paint.isFakeBoldText = false
        canvas.drawText(value, x + paint.measureText(label) + 10f, y, paint)
    }

    private fun drawCurpGrid(canvas: Canvas, curp: String, x: Float, y: Float, paint: Paint) {
        val boxSize = 15f
        paint.style = Paint.Style.STROKE
        for (i in 0 until 18) {
            canvas.drawRect(x + (i * boxSize), y, x + ((i + 1) * boxSize), y + boxSize, paint)
            if (i < curp.length) {
                paint.style = Paint.Style.FILL
                canvas.drawText(curp[i].toString(), x + (i * boxSize) + 4f, y + 11f, paint)
                paint.style = Paint.Style.STROKE
            }
        }
        paint.style = Paint.Style.FILL
        canvas.drawText("CURP", x, y - 5f, paint)
    }

    private fun drawRfcGrid(canvas: Canvas, rfc: String, x: Float, y: Float, paint: Paint) {
        val boxSize = 15f
        paint.style = Paint.Style.STROKE
        for (i in 0 until 13) {
            canvas.drawRect(x + (i * boxSize), y, x + ((i + 1) * boxSize), y + boxSize, paint)
            if (i < rfc.length) {
                paint.style = Paint.Style.FILL
                canvas.drawText(rfc[i].toString(), x + (i * boxSize) + 4f, y + 11f, paint)
                paint.style = Paint.Style.STROKE
            }
        }
        paint.style = Paint.Style.FILL
        canvas.drawText("RFC con homoclave", x, y - 5f, paint)
    }

        // --- DRAWING BACK (REVERSO) ---
        // (Similar logic for the tables shown in the second image)

        val file = File(context.getExternalFilesDir(null), "DC3_${worker.curp}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    }
}
