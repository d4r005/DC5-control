// ============================================================
// DC3Generator.kt
// Generador del Formato DC-3 (STPS México) para Android
// No requiere librerías externas: usa android.graphics.pdf.PdfDocument
// ============================================================

package com.example.dc5control.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

// ---------- Modelo de datos ----------
data class DC3Data(
    val nombreTrabajador: String,
    val curp: String,
    val ocupacionEspecifica: String,
    val puesto: String,
    val razonSocial: String,
    val rfc: String,
    val nombreCurso: String,
    val duracionHoras: String,
    val periodoInicio: String, // dd/mm/aaaa
    val periodoFin: String,    // dd/mm/aaaa
    val areaTematica: String,  // ej. "3000 Administración, contabilidad y economía"
    val agenteCapacitador: String,
    val instructorNombre: String,
    val patronNombre: String,
    val representanteTrabajadoresNombre: String? = null
)

// ---------- Generador ----------
class DC3Generator(private val context: Context) {

    // Tamaño carta en puntos (72 dpi): 612 x 792
    private val pageWidth = 612
    private val pageHeight = 792
    private val marginX = 40f

    fun generar(data: DC3Data, outputFile: File) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        dibujarFormato(canvas, data)

        document.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
    }

    private fun dibujarFormato(canvas: Canvas, data: DC3Data) {
        val titlePaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val subtitlePaint = Paint().apply {
            textSize = 11f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val labelPaint = Paint().apply {
            textSize = 8f
            color = android.graphics.Color.DKGRAY
        }
        val valuePaint = Paint().apply {
            textSize = 11f
            color = android.graphics.Color.BLACK
        }
        val sectionPaint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
            color = android.graphics.Color.WHITE
        }
        val sectionBgPaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            style = Paint.Style.FILL
        }
        val linePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 1f
        }

        var y = 50f

        // ---- Encabezado ----
        canvas.drawText("FORMATO DC-3", pageWidth / 2f, y, titlePaint)
        y += 20f
        canvas.drawText(
            "CONSTANCIA DE COMPETENCIAS O DE HABILIDADES LABORALES",
            pageWidth / 2f, y, subtitlePaint
        )
        y += 30f

        // ---- Sección: Datos del trabajador ----
        y = dibujarSeccion(canvas, "DATOS DEL TRABAJADOR", y, sectionPaint, sectionBgPaint)
        y = dibujarCampo(canvas, "Nombre (apellido paterno, apellido materno, nombre(s))",
            data.nombreTrabajador, y, labelPaint, valuePaint, linePaint)
        y = dibujarCampo(canvas, "Clave Única de Registro de Población (CURP)",
            data.curp, y, labelPaint, valuePaint, linePaint)
        y = dibujarCampo(canvas, "Ocupación específica (Catálogo Nacional de Ocupaciones)",
            data.ocupacionEspecifica, y, labelPaint, valuePaint, linePaint)
        y = dibujarCampo(canvas, "Puesto", data.puesto, y, labelPaint, valuePaint, linePaint)
        y += 10f

        // ---- Sección: Datos de la empresa ----
        y = dibujarSeccion(canvas, "DATOS DE LA EMPRESA", y, sectionPaint, sectionBgPaint)
        y = dibujarCampo(canvas, "Nombre o razón social",
            data.razonSocial, y, labelPaint, valuePaint, linePaint)
        y = dibujarCampo(canvas, "Registro Federal de Contribuyentes (RFC)",
            data.rfc, y, labelPaint, valuePaint, linePaint)
        y += 10f

        // ---- Sección: Datos del programa de capacitación ----
        y = dibujarSeccion(canvas, "DATOS DEL PROGRAMA DE CAPACITACIÓN, ADIESTRAMIENTO Y PRODUCTIVIDAD",
            y, sectionPaint, sectionBgPaint)
        y = dibujarCampo(canvas, "Nombre del curso",
            data.nombreCurso, y, labelPaint, valuePaint, linePaint)
        y = dibujarCampo(canvas, "Duración en horas",
            data.duracionHoras, y, labelPaint, valuePaint, linePaint)
        y = dibujarCampo(canvas, "Periodo de ejecución (Del)",
            data.periodoInicio, y, labelPaint, valuePaint, linePaint)
        y = dibujarCampo(canvas, "Periodo de ejecución (Al)",
            data.periodoFin, y, labelPaint, valuePaint, linePaint)
        y = dibujarCampo(canvas, "Área temática del curso",
            data.areaTematica, y, labelPaint, valuePaint, linePaint)
        y = dibujarCampo(canvas, "Nombre del agente capacitador o STPS",
            data.agenteCapacitador, y, labelPaint, valuePaint, linePaint)
        y += 20f

        // ---- Sección: Firmas ----
        val colWidth = (pageWidth - 2 * marginX) / 3f
        val firmaLabelY = y + 40f
        canvas.drawLine(marginX, firmaLabelY, marginX + colWidth - 10f, firmaLabelY, linePaint)
        canvas.drawLine(marginX + colWidth, firmaLabelY, marginX + 2 * colWidth - 10f, firmaLabelY, linePaint)
        canvas.drawLine(marginX + 2 * colWidth, firmaLabelY, marginX + 3 * colWidth, firmaLabelY, linePaint)

        val firmaTextPaint = Paint().apply { textSize = 9f; textAlign = Paint.Align.CENTER }
        canvas.drawText(data.instructorNombre, marginX + colWidth / 2 - 5f, firmaLabelY - 5f, firmaTextPaint)
        canvas.drawText(data.patronNombre, marginX + colWidth + colWidth / 2 - 5f, firmaLabelY - 5f, firmaTextPaint)
        canvas.drawText(
            data.representanteTrabajadoresNombre ?: "",
            marginX + 2 * colWidth + colWidth / 2, firmaLabelY - 5f, firmaTextPaint
        )

        canvas.drawText("Instructor o tutor", marginX + colWidth / 2 - 5f, firmaLabelY + 12f, labelPaint)
        canvas.drawText("Patrón o representante legal", marginX + colWidth + colWidth / 2 - 5f, firmaLabelY + 12f, labelPaint)
        canvas.drawText("Representante de los trabajadores", marginX + 2 * colWidth + colWidth / 2, firmaLabelY + 12f, labelPaint)

        // ---- Pie de página con instrucciones ----
        val footerPaint = Paint().apply { textSize = 7f; color = android.graphics.Color.GRAY }
        canvas.drawText(
            "Deberá entregarse al trabajador dentro de los veinte días hábiles siguientes al término del curso.",
            marginX, pageHeight - 30f, footerPaint
        )
    }

    private fun dibujarSeccion(
        canvas: Canvas, titulo: String, y: Float,
        textPaint: Paint, bgPaint: Paint
    ): Float {
        val rect = RectF(marginX, y, pageWidth - marginX, y + 18f)
        canvas.drawRect(rect, bgPaint)
        canvas.drawText(titulo, marginX + 5f, y + 13f, textPaint)
        return y + 30f
    }

    private fun dibujarCampo(
        canvas: Canvas, label: String, valor: String, y: Float,
        labelPaint: Paint, valuePaint: Paint, linePaint: Paint
    ): Float {
        canvas.drawText(label, marginX, y, labelPaint)
        canvas.drawText(valor, marginX, y + 16f, valuePaint)
        canvas.drawLine(marginX, y + 20f, pageWidth - marginX, y + 20f, linePaint)
        return y + 34f
    }
}
