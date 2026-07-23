// ============================================================
// PdfGenerator.kt
// Genera el DC-3 usando Android PdfRenderer + Canvas + PdfDocument.
// Coordenadas milimétricas sincronizadas con la versión Web.
// ============================================================

package com.example.dc5control.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import com.example.dc5control.data.model.*

data class DC3FormData(
    val nombreTrabajador: String,
    val curp: String,
    val ocupacion: String,
    val puesto: String,
    val razonSocial: String,
    val rfc: String,
    val nombreCurso: String,
    val duracionHoras: String,
    val fechaInicio: String,   // "dd/MM/yyyy"
    val fechaFin: String,      // "dd/MM/yyyy"
    val areaTematica: String,
    val agenteCapacitador: String,
    val instructor: String,
    val representanteLegal: String = "",
    val representanteTrabajadores: String? = null,
    val signatureBitmap: Bitmap? = null,
    val logoBitmap: Bitmap? = null,
    val photoBitmap: Bitmap? = null
)

object PdfGenerator {

    private const val TAG = "PdfGenerator"
    private const val TEMPLATE_ASSET = "plantilla_dc3.pdf"
    private const val LOGO_ASSET     = "logo_luber.png"
    private const val FIRMA_ASSET    = "cynthia_firma_oficial.png"

    private const val PDF_W  = 612f
    private const val PDF_H  = 792f
    private const val SCALE  = 2f
    private val BMP_W get() = (PDF_W * SCALE).toInt()
    private val BMP_H get() = (PDF_H * SCALE).toInt()

    private val CURP_CENTERS = floatArrayOf(
        32.0f,47.4f,62.8f,78.2f,93.5f,108.8f,124.2f,139.6f,
        155.0f,170.3f,185.8f,201.2f,216.6f,231.9f,247.2f,262.6f,278.0f,293.3f
    )
    private const val CURP_Y = 196f

    private val RFC_CENTERS = floatArrayOf(
        34.9f,52.1f,66.0f,80.8f,95.4f,109.8f,124.0f,138.3f,
        152.8f,167.0f,181.2f,195.5f,209.8f,227.4f,245.1f
    )
    private const val RFC_Y = 311f

    private val AÑO_INI_CENTERS = floatArrayOf(260.2f, 276.1f, 292.2f, 308.3f)
    private val MES_INI_CENTERS = floatArrayOf(348.2f, 369.7f)
    private val DIA_INI_CENTERS = floatArrayOf(390.7f, 412.1f)
    private val AÑO_FIN_CENTERS = floatArrayOf(452.4f, 471.9f, 491.4f, 511.8f)
    private val MES_FIN_CENTERS = floatArrayOf(532.8f, 554.3f)
    private val DIA_FIN_CENTERS = floatArrayOf(575.7f, 595.9f)
    private const val FECHA_Y = 393f

    private const val Y_NOMBRE_TRAB = 172.5f
    private const val Y_PUESTO      = 220.0f
    private const val Y_EMPRESA     = 272.0f
    private const val Y_CURSO       = 362.0f
    private const val Y_DURACION    = 378.0f
    private const val Y_AREA        = 412.0f
    private const val Y_AGENTE      = 437.0f

    private const val SIG_INS_X  = 132f
    private const val SIG_PAT_X  = 295f
    private const val SIG_REP_X  = 464f
    private const val SIG_IMG_Y  = 473f
    private const val SIG_NAME_Y1 = 516f
    private const val SIG_NAME_Y2 = 526f

    private fun paintText(size: Float, bold: Boolean = false, center: Boolean = false) = Paint().apply {
        color = Color.BLACK
        textSize = size * SCALE
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        textAlign = if (center) Paint.Align.CENTER else Paint.Align.LEFT
        isAntiAlias = true
    }

    fun generate(context: Context, data: DC3FormData): File {
        val tmpFile = File(context.cacheDir, "dc3_template_tmp.pdf")
        context.assets.open(TEMPLATE_ASSET).use { input ->
            FileOutputStream(tmpFile).use { output -> input.copyTo(output) }
        }

        val pfd = ParcelFileDescriptor.open(tmpFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val pdfPage = renderer.openPage(0)

        val bmp = Bitmap.createBitmap(BMP_W, BMP_H, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)
        pdfPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        pdfPage.close()
        renderer.close()
        pfd.close()

        val canvas = Canvas(bmp)
        drawContent(context, canvas, data)

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(BMP_W, BMP_H, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        page.canvas.drawBitmap(bmp, 0f, 0f, null)
        pdfDoc.finishPage(page)

        val name = "DC3_${sanitize(data.nombreTrabajador.replace(" ","_"))}.pdf"
        val out = File(context.getExternalFilesDir(null), name)
        FileOutputStream(out).use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        return out
    }

    fun generateDC3(
        context: Context,
        employee: Employee,
        course: Course,
        agent: Agent,
        companyName: String,
        companyRfc: String,
        companyPatron: String,
        companyRepresentante: String?,
        startDate: String,
        endDate: String,
        signatureBitmap: Bitmap?,
        logoBitmap: Bitmap?
    ): File {
        val data = DC3FormData(
            nombreTrabajador = "${employee.apellidoPaterno} ${employee.apellidoMaterno} ${employee.nombres}".trim(),
            curp = employee.curp,
            ocupacion = employee.occupation,
            puesto = employee.position,
            razonSocial = companyName,
            rfc = companyRfc,
            nombreCurso = course.name,
            duracionHoras = course.durationHours,
            fechaInicio = startDate,
            fechaFin = endDate,
            areaTematica = course.thematicArea ?: "",
            agenteCapacitador = agent.name,
            instructor = agent.name,
            representanteLegal = companyPatron,
            representanteTrabajadores = companyRepresentante,
            signatureBitmap = signatureBitmap,
            logoBitmap = logoBitmap
        )
        return generate(context, data)
    }

    private fun drawContent(context: Context, canvas: Canvas, d: DC3FormData) {
        val pNormal = paintText(9f)
        val pBold   = paintText(9f, bold = true)
        val pBox    = paintText(8f, center = true)
        val whitePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }

        fun text(p: Paint, x: Float, y: Float, t: String) {
            if (t.isBlank()) return
            canvas.drawText(sanitize(t), x * SCALE, y * SCALE, p)
        }

        fun charInCell(x: Float, y: Float, ch: String) {
            val w = pBox.measureText(ch)
            canvas.drawText(ch, (x * SCALE) - (w/2f), y * SCALE, pBox)
        }

        fun parseFecha(f: String): Triple<String, String, String> {
            val p = f.split("/")
            return Triple(
                p.getOrElse(2) { "" }.padStart(4, '0'),
                p.getOrElse(1) { "" }.padStart(2, '0'),
                p.getOrElse(0) { "" }.padStart(2, '0')
            )
        }

        text(pBold, 30f, Y_NOMBRE_TRAB, d.nombreTrabajador.uppercase())
        d.curp.uppercase().replace(" ","").take(18).forEachIndexed { i, c ->
            if (i < CURP_CENTERS.size) charInCell(CURP_CENTERS[i], CURP_Y, c.toString())
        }
        text(paintText(7f), 307f, 183f, d.ocupacion.uppercase().take(50))
        canvas.drawRect(26f*SCALE, 208f*SCALE, 584f*SCALE, 226f*SCALE, whitePaint)
        text(pNormal, 30f, Y_PUESTO, d.puesto.uppercase())
        text(pBold, 30f, Y_EMPRESA, d.razonSocial.uppercase().take(70))
        d.rfc.uppercase().replace(" ","").take(15).forEachIndexed { i, c ->
            if (i < RFC_CENTERS.size) charInCell(RFC_CENTERS[i], RFC_Y, c.toString())
        }
        text(pBold, 30f, Y_CURSO, d.nombreCurso.uppercase().take(80))
        text(pNormal, 30f, Y_DURACION, d.duracionHoras)

        val (yi, mi, di) = parseFecha(d.fechaInicio)
        val (yf, mf, df) = parseFecha(d.fechaFin)
        fun drawDate(s: String, centers: FloatArray) {
            s.forEachIndexed { i, c -> if (i < centers.size) charInCell(centers[i], FECHA_Y, c.toString()) }
        }
        drawDate(yi, AÑO_INI_CENTERS); drawDate(mi, MES_INI_CENTERS); drawDate(di, DIA_INI_CENTERS)
        drawDate(yf, AÑO_FIN_CENTERS); drawDate(mf, MES_FIN_CENTERS); drawDate(df, DIA_FIN_CENTERS)

        text(paintText(8f), 30f, Y_AREA, d.areaTematica.uppercase().take(80))
        text(paintText(8f), 30f, Y_AGENTE, d.agenteCapacitador.uppercase().take(80))

        canvas.drawRect(63f*SCALE,  472f*SCALE, 201f*SCALE, 540f*SCALE, whitePaint)
        canvas.drawRect(218f*SCALE, 472f*SCALE, 372f*SCALE, 540f*SCALE, whitePaint)
        canvas.drawRect(388f*SCALE, 472f*SCALE, 542f*SCALE, 540f*SCALE, whitePaint)

        val logo = d.logoBitmap ?: loadAssetBitmap(context, LOGO_ASSET)
        logo?.let {
            val dst = RectF((SIG_INS_X-50f)*SCALE, (SIG_IMG_Y)*SCALE, (SIG_INS_X+50f)*SCALE, (SIG_IMG_Y+60f)*SCALE)
            canvas.drawBitmap(it, null, dst, null)
        }
        val firma = d.signatureBitmap ?: loadAssetBitmap(context, FIRMA_ASSET)
        firma?.let {
            val dst = RectF((SIG_INS_X-47f)*SCALE, (SIG_IMG_Y)*SCALE, (SIG_INS_X+48f)*SCALE, (SIG_IMG_Y+65f)*SCALE)
            canvas.drawBitmap(it, null, dst, null)
        }

        val pSmallName  = paintText(8f, center = true)
        val insLines = splitName(d.instructor.uppercase(), 26)
        canvas.drawText(insLines[0], SIG_INS_X*SCALE, SIG_NAME_Y1*SCALE, pSmallName)
        if (insLines.size > 1) canvas.drawText(insLines[1], SIG_INS_X*SCALE, SIG_NAME_Y2*SCALE, pSmallName)

        val patLines = splitName(d.representanteLegal.uppercase(), 28)
        canvas.drawText(patLines[0], SIG_PAT_X*SCALE, SIG_NAME_Y1*SCALE, pSmallName)
        if (patLines.size > 1) canvas.drawText(patLines[1], SIG_PAT_X*SCALE, SIG_NAME_Y2*SCALE, pSmallName)

        d.representanteTrabajadores?.let {
            val repLines = splitName(it.uppercase(), 26)
            canvas.drawText(repLines[0], SIG_REP_X*SCALE, SIG_NAME_Y1*SCALE, pSmallName)
            if (repLines.size > 1) canvas.drawText(repLines[1], SIG_REP_X*SCALE, SIG_NAME_Y2*SCALE, pSmallName)
        }
    }

    private fun splitName(name: String, maxChars: Int): List<String> {
        if (name.length <= maxChars) return listOf(name)
        val mid = name.lastIndexOf(' ', maxChars)
        return if (mid > 0) listOf(name.substring(0, mid), name.substring(mid + 1))
        else listOf(name.take(maxChars), name.drop(maxChars))
    }

    private fun loadAssetBitmap(context: Context, assetName: String): Bitmap? = try {
        context.assets.open(assetName).use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) { null }

    private fun sanitize(s: String): String {
        val map = mapOf('á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n', 'Á' to 'A', 'É' to 'E', 'Í' to 'I', 'Ó' to 'O', 'Ú' to 'U', 'Ü' to 'U', 'Ñ' to 'N')
        return s.map { map[it] ?: it }.joinToString("")
    }
}
