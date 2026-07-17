// ============================================================
// PdfGenerator.kt
// Genera el DC-3 usando Android PdfRenderer + Canvas + PdfDocument.
//
// La plantilla se renderiza a bitmap con PdfRenderer (API 21+),
// luego se dibuja texto e imágenes sobre ese bitmap con Canvas,
// y finalmente se empaqueta en un PdfDocument de Android.
//
// Ventaja: coordenadas IDÉNTICAS a PyMuPDF (Y desde arriba),
// sin problemas de transformaciones de PDFBox.
//
// Escala: la plantilla es 612×792 pts PDF.
// Renderizamos a 2× (1224×1584 px) para nitidez.
// Todas las coordenadas están en pts del PDF; se multiplican × SCALE.
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
    val patron: String = "",
    val representante: String? = null,
    val signatureBitmap: Bitmap? = null,
    val logoBitmap: Bitmap? = null
)

object PdfGenerator {

    private const val TAG = "PdfGenerator"
    private const val TEMPLATE_ASSET = "plantilla_dc3.pdf"
    private const val LOGO_ASSET     = "logo_luber.png"
    private const val FIRMA_ASSET    = "firma_instructor.png"

    // PDF pts: 612×792. Render a 2× para nitidez.
    private const val PDF_W  = 612f
    private const val PDF_H  = 792f
    private const val SCALE  = 2f
    private val BMP_W get() = (PDF_W * SCALE).toInt()
    private val BMP_H get() = (PDF_H * SCALE).toInt()

    // ── Fuentes ──────────────────────────────────────────────────────────────
    private fun paintText(size: Float, bold: Boolean = false, color: Int = Color.BLACK) = Paint().apply {
        this.color = color
        textSize = size * SCALE
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        isAntiAlias = true
    }

    // ── Coordenadas (en pts PDF, Y desde arriba) ─────────────────────────────
    // CURP: 18 celdas
    private val CURP_XS = floatArrayOf(
        24.2f,39.6f,55.0f,70.6f,85.9f,101.1f,116.7f,132.1f,147.4f,
        162.8f,178.1f,193.5f,208.9f,224.3f,239.6f,255.0f,270.3f,285.7f
    )
    private const val CURP_Y1 = 185.8f  // top de celda
    private const val CURP_Y2 = 201.2f  // bottom de celda

    // RFC: 16 celdas (x0 de cada separador)
    private val RFC_XS = floatArrayOf(
        24.2f,45.4f,59.1f,73.2f,88.3f,102.8f,116.9f,131.3f,
        145.5f,159.9f,174.1f,188.5f,202.6f,217.1f,237.9f,252.1f
    )
    private const val RFC_Y1 = 300.3f
    private const val RFC_Y2 = 315.2f

    // FECHA: separadores exactos de la fila de dígitos
    // Año inicio: celdas [2]-[5], Mes [6]-[7], Día [8]-[9]
    // 'a' separator: [10], Año fin: [11]-[14], Mes [15]-[16], Día [17]-[18]
    private val FECHA_SEPS = floatArrayOf(
        24.2f,180.6f,252.1f,267.7f,283.6f,300.1f,316.0f,
        337.1f,358.9f,379.8f,401.0f,422.8f,442.2f,462.0f,
        481.4f,501.1f,522.0f,543.1f,564.9f,586.1f
    )
    private const val FECHA_Y1 = 380.3f
    private const val FECHA_Y2 = 394.0f

    // Índices de las 16 celdas de fecha (dentro de FECHA_SEPS):
    //  [2]...[18] → índices 2..18 en el array de 20 seps (19 celdas)
    //  Año_ini: seps[2]-seps[3], seps[3]-[4], seps[4]-[5], seps[5]-[6]
    //  Mes_ini: seps[6]-[7], seps[7]-[8]
    //  Día_ini: seps[8]-[9], seps[9]-[10]
    //  (a):     seps[10]-[11]
    //  Año_fin: seps[11]-[12], seps[12]-[13], seps[13]-[14], seps[14]-[15]
    //  Mes_fin: seps[15]-[16], seps[16]-[17]
    //  Día_fin: seps[17]-[18], seps[18]-[19]
    private val AÑO_INI_IDX = intArrayOf(2, 3, 4, 5)
    private val MES_INI_IDX = intArrayOf(6, 7)
    private val DIA_INI_IDX = intArrayOf(8, 9)
    private val AÑO_FIN_IDX = intArrayOf(11, 12, 13, 14)
    private val MES_FIN_IDX = intArrayOf(15, 16)
    private val DIA_FIN_IDX = intArrayOf(17, 18)

    // Texto libre
    private const val Y_NOMBRE_TRAB = 170.0f
    private const val Y_PUESTO      = 221.0f
    private const val Y_EMPRESA     = 280.0f
    private const val Y_CURSO       = 363.0f
    private const val Y_DURACION    = 388.0f
    private const val Y_AREA        = 414.0f
    private const val Y_AGENTE      = 437.0f

    // Sección de firmas
    private const val SIG_Y1 = 443.4f
    private const val SIG_Y2 = 539.9f
    // Columna instructor: X=24 a ~215 → logo centrado en X=131
    private const val LOGO_X = 74f;  private const val LOGO_Y = 454f
    private const val LOGO_W = 115f; private const val LOGO_H = 65f   // ratio 1.77:1
    // Firma encima del logo
    private const val FIRMA_X = 89f; private const val FIRMA_Y = 457f
    private const val FIRMA_W = 85f; private const val FIRMA_H = 60f  // ratio 1.42:1

    // Nombre instructor bajo firma
    private const val X_INS = 28f
    private const val X_PAT = 222f
    private const val X_REP = 385f
    private const val Y_NAME_L1 = 527f
    private const val Y_NAME_L2 = 535f

    // ─────────────────────────────────────────────────────────────────────────
    //  API PÚBLICA
    // ─────────────────────────────────────────────────────────────────────────

    fun generate(context: Context, data: DC3FormData): File {
        // 1. Copiar plantilla a fichero temporal (PdfRenderer necesita File)
        val tmpFile = File(context.cacheDir, "dc3_template_tmp.pdf")
        context.assets.open(TEMPLATE_ASSET).use { input ->
            FileOutputStream(tmpFile).use { output -> input.copyTo(output) }
        }

        // 2. Renderizar plantilla a bitmap con PdfRenderer
        val pfd = ParcelFileDescriptor.open(tmpFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val pdfPage = renderer.openPage(0)

        val bmp = Bitmap.createBitmap(BMP_W, BMP_H, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)
        pdfPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        pdfPage.close()
        renderer.close()
        pfd.close()

        // 3. Dibujar sobre el bitmap
        val canvas = Canvas(bmp)
        drawContent(context, canvas, data)

        // 4. Empaquetar en PdfDocument de Android
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(BMP_W, BMP_H, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        page.canvas.drawBitmap(bmp, 0f, 0f, null)
        pdfDoc.finishPage(page)

        val name = "DC3_${sanitize(data.curp.ifBlank { data.nombreTrabajador })}.pdf"
        val out = File(context.getExternalFilesDir(null), name)
        FileOutputStream(out).use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        Log.d(TAG, "PDF generado: ${out.absolutePath}")
        return out
    }

    fun generateDC3(
        context: Context,
        employee: com.example.dc5control.data.model.Employee,
        course: com.example.dc5control.data.model.Course,
        instructor: com.example.dc5control.data.model.Instructor,
        companyName: String,
        companyRfc: String,
        companyPatron: String = "",
        companyRepresentante: String? = null,
        startDate: String,
        endDate: String,
        signatureBitmap: Bitmap? = null,
        logoBitmap: Bitmap? = null
    ): File = generate(context, DC3FormData(
        nombreTrabajador = "${employee.lastName} ${employee.firstName} ${employee.middleName ?: ""}".trim(),
        curp             = employee.curp,
        ocupacion        = employee.position,
        puesto           = employee.position,
        razonSocial      = companyName,
        rfc              = companyRfc,
        nombreCurso      = course.name,
        duracionHoras    = "${course.duration} HORAS",
        fechaInicio      = startDate,
        fechaFin         = endDate,
        areaTematica     = course.thematicArea,
        agenteCapacitador = "${instructor.fullName} ${instructor.stpsNumber ?: ""}".trim(),
        instructor       = instructor.fullName,
        patron           = companyPatron,
        representante    = companyRepresentante,
        signatureBitmap  = signatureBitmap,
        logoBitmap       = logoBitmap
    ))

    // ─────────────────────────────────────────────────────────────────────────
    //  DIBUJO SOBRE CANVAS
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawContent(context: Context, canvas: Canvas, d: DC3FormData) {
        val pNormal  = paintText(9f, bold = false)
        val pBold    = paintText(9f, bold = true)
        val pSmall   = paintText(8f)
        val pBox     = paintText(7f)

        // Helper: texto en coordenadas PDF pts → px
        fun text(paint: Paint, x: Float, yBaseline: Float, t: String) {
            if (t.isBlank()) return
            canvas.drawText(sanitize(t), x * SCALE, yBaseline * SCALE, paint)
        }

        // Helper: caracter centrado en una celda definida por x0..x1
        fun charInCell(x0: Float, x1: Float, yBaseline: Float, ch: String) {
            if (ch.isBlank()) return
            val cw = pBox.measureText(ch)
            val cx = ((x0 + x1) / 2f * SCALE) - cw / 2f
            canvas.drawText(ch, cx, yBaseline * SCALE, pBox)
        }

        // Parsear "dd/MM/yyyy" → (año4, mes2, dia2)
        fun parseFecha(f: String): Triple<String, String, String> {
            val p = f.split("/")
            return Triple(
                p.getOrElse(2) { "" }.padStart(4, '0'),
                p.getOrElse(1) { "" }.padStart(2, '0'),
                p.getOrElse(0) { "" }.padStart(2, '0')
            )
        }

        // ── Nombre trabajador ─────────────────────────────────────────────────
        text(pBold, 30f, Y_NOMBRE_TRAB, d.nombreTrabajador.uppercase())

        // ── CURP (carácter por celda) ─────────────────────────────────────────
        val curp = d.curp.uppercase().take(18)
        val CURP_Y_BASE = (CURP_Y1 + CURP_Y2) / 2f + 2.5f  // baseline centrada
        curp.forEachIndexed { i, c ->
            if (i < CURP_XS.size - 1)
                charInCell(CURP_XS[i], CURP_XS[i + 1], CURP_Y_BASE, c.toString())
        }

        // ── Puesto ────────────────────────────────────────────────────────────
        // Borrar texto de ejemplo de la plantilla
        val erasePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        canvas.drawRect(25f * SCALE, 209f * SCALE, 586f * SCALE, 228f * SCALE, erasePaint)
        text(pNormal, 30f, Y_PUESTO, d.puesto.uppercase())

        // ── Empresa ───────────────────────────────────────────────────────────
        text(pBold, 30f, Y_EMPRESA, d.razonSocial.uppercase().take(70))

        // ── RFC (carácter por celda) ──────────────────────────────────────────
        val RFC_Y_BASE = (RFC_Y1 + RFC_Y2) / 2f + 2.5f
        val rfcClean = d.rfc.uppercase().replace(" ", "").take(15)
        rfcClean.forEachIndexed { i, c ->
            if (i < RFC_XS.size - 1)
                charInCell(RFC_XS[i], RFC_XS[i + 1], RFC_Y_BASE, c.toString())
        }

        // ── Curso ─────────────────────────────────────────────────────────────
        text(pBold, 30f, Y_CURSO, d.nombreCurso.uppercase().take(80))

        // ── Duración ──────────────────────────────────────────────────────────
        text(pNormal, 30f, Y_DURACION, d.duracionHoras)

        // ── FECHA (carácter por celda) ────────────────────────────────────────
        val FECHA_Y_BASE = (FECHA_Y1 + FECHA_Y2) / 2f + 2.5f
        val (yi, mi, di) = parseFecha(d.fechaInicio)
        val (yf, mf, df) = parseFecha(d.fechaFin)

        fun drawFecha(chars: String, idxArray: IntArray) {
            chars.forEachIndexed { i, c ->
                if (i < idxArray.size) {
                    val si = idxArray[i]
                    charInCell(FECHA_SEPS[si], FECHA_SEPS[si + 1], FECHA_Y_BASE, c.toString())
                }
            }
        }
        drawFecha(yi, AÑO_INI_IDX)
        drawFecha(mi, MES_INI_IDX)
        drawFecha(di, DIA_INI_IDX)
        drawFecha(yf, AÑO_FIN_IDX)
        drawFecha(mf, MES_FIN_IDX)
        drawFecha(df, DIA_FIN_IDX)

        // ── Área temática ─────────────────────────────────────────────────────
        text(pSmall, 30f, Y_AREA, d.areaTematica.take(80))

        // ── Agente capacitador ────────────────────────────────────────────────
        text(pSmall, 30f, Y_AGENTE, d.agenteCapacitador.uppercase().take(70))

        // ── LOGO (fondo, en columna instructor) ───────────────────────────────
        val logoBmp = d.logoBitmap ?: loadAssetBitmap(context, LOGO_ASSET)
        logoBmp?.let {
            val dst = RectF(LOGO_X * SCALE, LOGO_Y * SCALE,
                            (LOGO_X + LOGO_W) * SCALE, (LOGO_Y + LOGO_H) * SCALE)
            canvas.drawBitmap(it, null, dst, null)
        }

        // ── FIRMA (encima del logo) ───────────────────────────────────────────
        val firmaBmp = d.signatureBitmap ?: loadAssetBitmap(context, FIRMA_ASSET)
        firmaBmp?.let {
            val dst = RectF(FIRMA_X * SCALE, FIRMA_Y * SCALE,
                            (FIRMA_X + FIRMA_W) * SCALE, (FIRMA_Y + FIRMA_H) * SCALE)
            canvas.drawBitmap(it, null, dst, null)
        }

        // ── Nombres en área de firmas ─────────────────────────────────────────
        val insLines = splitName(d.instructor.uppercase(), 24)
        if (insLines.size > 1) {
            text(pBox, X_INS, Y_NAME_L1, insLines[0])
            text(pBox, X_INS, Y_NAME_L2, insLines[1])
        } else {
            text(pBox, X_INS, Y_NAME_L2, insLines[0])
        }

        if (d.patron.isNotBlank()) {
            val patLines = splitName(d.patron.uppercase(), 26)
            if (patLines.size > 1) {
                text(pBox, X_PAT, Y_NAME_L1, patLines[0])
                text(pBox, X_PAT, Y_NAME_L2, patLines[1])
            } else {
                text(pBox, X_PAT, Y_NAME_L2, patLines[0])
            }
        }

        d.representante?.let { rep ->
            if (rep.isNotBlank()) {
                val repLines = splitName(rep.uppercase(), 24)
                if (repLines.size > 1) {
                    text(pBox, X_REP, Y_NAME_L1, repLines[0])
                    text(pBox, X_REP, Y_NAME_L2, repLines[1])
                } else {
                    text(pBox, X_REP, Y_NAME_L2, repLines[0])
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun splitName(name: String, maxChars: Int): List<String> {
        if (name.length <= maxChars) return listOf(name)
        val mid = name.lastIndexOf(' ', maxChars)
        return if (mid > 0) listOf(name.substring(0, mid), name.substring(mid + 1))
        else listOf(name.take(maxChars), name.drop(maxChars))
    }

    private fun loadAssetBitmap(context: Context, assetName: String): Bitmap? = try {
        context.assets.open(assetName).use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) {
        Log.w(TAG, "Asset no encontrado: $assetName")
        null
    }

    private fun sanitize(s: String): String {
        val map = mapOf(
            'á' to 'a','é' to 'e','í' to 'i','ó' to 'o','ú' to 'u','ü' to 'u','ñ' to 'n',
            'Á' to 'A','É' to 'E','Í' to 'I','Ó' to 'O','Ú' to 'U','Ü' to 'U','Ñ' to 'N'
        )
        return s.map { map[it] ?: it }.joinToString("")
    }
}
