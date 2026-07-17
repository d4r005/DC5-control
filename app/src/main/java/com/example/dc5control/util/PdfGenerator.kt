// ============================================================
// PdfGenerator.kt
// Rellena la plantilla oficial DC-3 STPS con datos reales.
// PDFBox 3.0.1 — coordenadas calibradas con PyMuPDF sobre el
// PDF oficial STPS (612×792 pts / Letter).
//
// Origen PyMuPDF: esquina sup-izq (Y crece hacia abajo).
// PDFBox:         esquina inf-izq (Y crece hacia arriba).
// Conversión: y_pdfbox = PAGE_H - y_fitz
// Para imágenes: y_pdfbox = PAGE_H - y_fitz - h
// ============================================================

package com.example.dc5control.util

import android.content.Context
import android.graphics.Bitmap
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.ByteArrayOutputStream
import java.io.File

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

    private const val TEMPLATE_ASSET = "plantilla_dc3.pdf"
    private const val LOGO_ASSET     = "logo_luber.png"
    private const val FIRMA_ASSET    = "firma_instructor.png"
    private const val PAGE_H         = 792f

    // ── Tamaños de fuente ─────────────────────────────────────────────────────
    private const val FS_NORMAL = 9f
    private const val FS_SMALL  = 8f
    private const val FS_BOX    = 7f

    // ── CURP: 18 celdas (extraídas con pymupdf) ───────────────────────────────
    private val CURP_XS = floatArrayOf(
        31.9f, 47.3f, 62.8f, 78.3f, 93.5f, 108.9f, 124.4f, 139.7f, 155.1f,
        170.5f, 185.8f, 201.2f, 216.6f, 231.9f, 247.3f, 262.7f, 278.0f, 293.4f
    )
    private const val CURP_Y = 195.0f

    // ── RFC: 15 celdas ────────────────────────────────────────────────────────
    private val RFC_XS = floatArrayOf(
        35.5f, 50.0f, 64.5f, 79.0f, 93.5f, 108.0f, 122.5f, 137.0f, 151.5f,
        166.0f, 180.5f, 195.0f, 209.5f, 224.0f, 238.5f
    )
    private const val RFC_Y = 308.0f

    // ── FECHA: centros de celda – 2.5pt para centrar glifo de ~4pt ───────────
    // Orden: Año(4) | Mes(2) | Día(2)   para inicio y fin
    private val AÑO_INI = floatArrayOf(257.4f, 273.1f, 289.4f, 305.6f)
    private val MES_INI = floatArrayOf(324.1f, 345.5f)
    private val DIA_INI = floatArrayOf(366.9f, 387.9f)
    private val AÑO_FIN = floatArrayOf(430.0f, 449.6f, 469.2f, 488.7f)
    private val MES_FIN = floatArrayOf(509.1f, 530.0f)
    private val DIA_FIN = floatArrayOf(551.5f, 573.0f)
    private const val FECHA_Y = 389.0f

    // ── Coordenadas de texto ──────────────────────────────────────────────────
    private const val Y_NOMBRE_TRAB  = 170.0f
    private const val Y_PUESTO       = 223.0f
    private const val Y_EMPRESA      = 282.0f
    private const val Y_AREA         = 416.0f
    private const val Y_AGENTE       = 438.0f
    private const val Y_CURSO        = 365.0f
    private const val Y_DURACION     = 391.0f

    // ── Área de firmas (sección Y=443–540, sin divisiones verticales internas) ─
    // Columnas aproximadas según posición de etiquetas:
    //   Instructor:     X=24  – X≈215   (centro ~120)
    //   Patrón:         X=216 – X≈400   (centro ~310)
    //   Representante:  X=401 – X=586   (centro ~493)
    private const val X_FIRMA_INS = 26.0f
    private const val X_FIRMA_PAT = 222.0f
    private const val X_FIRMA_REP = 385.0f
    private const val Y_FIRMA_L1  = 528.0f   // primera línea del nombre
    private const val Y_FIRMA_L2  = 536.0f   // segunda línea

    // Logo LUBER: fondo semitransparente centrado en columna instructor
    // W=105, H=59 (ratio 1.78:1 preservado), X centrado en col (center=120→X=68)
    private const val LOGO_X = 68f;  private const val LOGO_Y = 453f
    private const val LOGO_W = 105f; private const val LOGO_H = 59f

    // Firma azul: encima del logo, ligeramente solapando
    // W=85, H=60 (ratio 1.42:1), X=55 para que quede centrada visualmente
    private const val FIRMA_X = 55f;  private const val FIRMA_Y = 455f
    private const val FIRMA_W = 85f;  private const val FIRMA_H = 60f

    // ─────────────────────────────────────────────────────────────────────────
    fun generate(context: Context, data: DC3FormData): File {
        val doc = loadTemplate(context)
        try {
            val page = doc.getPage(0)
            // 1. Texto
            PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
                fillPage(cs, data)
            }
            // 2. Logo (fondo — primero para que quede detrás)
            val logoBmp = data.logoBitmap ?: loadAssetBitmap(context, LOGO_ASSET)
            logoBmp?.let { insertImage(doc, page, it, LOGO_X, LOGO_Y, LOGO_W, LOGO_H) }

            // 3. Firma (encima del logo — se inserta después)
            val firmaBmp = data.signatureBitmap ?: loadAssetBitmap(context, FIRMA_ASSET)
            firmaBmp?.let { insertImage(doc, page, it, FIRMA_X, FIRMA_Y, FIRMA_W, FIRMA_H) }

            val out = File(
                context.getExternalFilesDir(null),
                "DC3_${sanitize(data.curp.ifBlank { data.nombreTrabajador })}.pdf"
            )
            doc.save(out)
            return out
        } finally {
            doc.close()
        }
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
    ): File = generate(
        context,
        DC3FormData(
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
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    private fun fillPage(cs: PDPageContentStream, d: DC3FormData) {

        fun w(font: PDType1Font, size: Float, x: Float, yFitz: Float, text: String) {
            if (text.isBlank()) return
            cs.beginText()
            cs.setFont(font, size)
            cs.newLineAtOffset(x, PAGE_H - yFitz)
            cs.showText(sanitize(text))
            cs.endText()
        }

        fun boxes(font: PDType1Font, size: Float, chars: String, xs: FloatArray, yFitz: Float) {
            chars.forEachIndexed { i, c -> if (i < xs.size) w(font, size, xs[i], yFitz, c.toString()) }
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

        val FONT_R = PDType1Font(FontName.HELVETICA)
        val FONT_B = PDType1Font(FontName.HELVETICA_BOLD)

        // Nombre del trabajador
        w(FONT_B, FS_NORMAL, 30f, Y_NOMBRE_TRAB, d.nombreTrabajador.uppercase())

        // CURP celda por celda
        boxes(FONT_R, FS_BOX, d.curp.uppercase().take(18), CURP_XS, CURP_Y)

        // Puesto: borrar texto de ejemplo y reescribir
        cs.setNonStrokingColor(1f, 1f, 1f)
        cs.addRect(25f, PAGE_H - 231f, 560f, 18f)
        cs.fill()
        cs.setNonStrokingColor(0f, 0f, 0f)
        w(FONT_R, FS_NORMAL, 30f, Y_PUESTO, d.puesto.uppercase())

        // Empresa
        w(FONT_B, FS_NORMAL, 30f, Y_EMPRESA, d.razonSocial.uppercase().take(70))

        // RFC celda por celda
        val rfcClean = d.rfc.uppercase().replace("-", "").replace(" ", "").take(15)
        boxes(FONT_R, FS_BOX, rfcClean, RFC_XS, RFC_Y)

        // Curso
        w(FONT_B, FS_SMALL, 30f, Y_CURSO, d.nombreCurso.uppercase().take(80))

        // Duración
        w(FONT_R, FS_SMALL, 30f, Y_DURACION, d.duracionHoras)

        // Fecha — dígito por celda
        val (yi, mi, di) = parseFecha(d.fechaInicio)
        val (yf, mf, df) = parseFecha(d.fechaFin)
        boxes(FONT_R, FS_BOX, yi, AÑO_INI, FECHA_Y)
        boxes(FONT_R, FS_BOX, mi, MES_INI, FECHA_Y)
        boxes(FONT_R, FS_BOX, di, DIA_INI, FECHA_Y)
        boxes(FONT_R, FS_BOX, yf, AÑO_FIN, FECHA_Y)
        boxes(FONT_R, FS_BOX, mf, MES_FIN, FECHA_Y)
        boxes(FONT_R, FS_BOX, df, DIA_FIN, FECHA_Y)

        // Área temática
        w(FONT_R, FS_SMALL, 30f, Y_AREA, d.areaTematica.take(80))

        // Agente capacitador
        w(FONT_R, FS_SMALL, 30f, Y_AGENTE, d.agenteCapacitador.uppercase().take(70))

        // ── Nombres en área de firmas ─────────────────────────────────────────
        // Instructor (hasta 2 líneas)
        val insLines = splitName(d.instructor.uppercase(), 24)
        w(FONT_R, FS_BOX, X_FIRMA_INS, if (insLines.size > 1) Y_FIRMA_L1 else Y_FIRMA_L2, insLines[0])
        if (insLines.size > 1) w(FONT_R, FS_BOX, X_FIRMA_INS, Y_FIRMA_L2, insLines[1])

        // Patrón
        if (d.patron.isNotBlank()) {
            val patLines = splitName(d.patron.uppercase(), 26)
            w(FONT_R, FS_BOX, X_FIRMA_PAT, if (patLines.size > 1) Y_FIRMA_L1 else Y_FIRMA_L2, patLines[0])
            if (patLines.size > 1) w(FONT_R, FS_BOX, X_FIRMA_PAT, Y_FIRMA_L2, patLines[1])
        }

        // Representante
        d.representante?.let { rep ->
            if (rep.isNotBlank()) {
                val repLines = splitName(rep.uppercase(), 24)
                w(FONT_R, FS_BOX, X_FIRMA_REP, if (repLines.size > 1) Y_FIRMA_L1 else Y_FIRMA_L2, repLines[0])
                if (repLines.size > 1) w(FONT_R, FS_BOX, X_FIRMA_REP, Y_FIRMA_L2, repLines[1])
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    private fun splitName(name: String, maxChars: Int): List<String> {
        if (name.length <= maxChars) return listOf(name)
        val mid = name.lastIndexOf(' ', maxChars)
        return if (mid > 0) listOf(name.substring(0, mid), name.substring(mid + 1))
        else listOf(name.take(maxChars), name.drop(maxChars))
    }

    private fun loadTemplate(context: Context): PDDocument {
        val bytes = context.assets.open(TEMPLATE_ASSET).use { it.readBytes() }
        return Loader.loadPDF(bytes)
    }

    private fun loadAssetBitmap(context: Context, assetName: String): Bitmap? = try {
        context.assets.open(assetName).use { android.graphics.BitmapFactory.decodeStream(it) }
    } catch (e: Exception) {
        android.util.Log.w("PdfGenerator", "Asset no encontrado: $assetName — ${e.message}")
        null
    }

    private fun insertImage(
        doc: PDDocument,
        page: org.apache.pdfbox.pdmodel.PDPage,
        bmp: Bitmap, x: Float, y: Float, w: Float, h: Float
    ) {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val img = PDImageXObject.createFromByteArray(doc, baos.toByteArray(), "img")
        PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
            // PDFBox Y=0 es abajo → y_pdfbox = PAGE_H - y_fitz - h
            cs.drawImage(img, x, PAGE_H - y - h, w, h)
        }
    }

    private fun sanitize(s: String): String {
        val map = mapOf(
            'á' to 'a','é' to 'e','í' to 'i','ó' to 'o','ú' to 'u','ü' to 'u','ñ' to 'n',
            'Á' to 'A','É' to 'E','Í' to 'I','Ó' to 'O','Ú' to 'U','Ü' to 'U','Ñ' to 'N'
        )
        return s.map { map[it] ?: it }.joinToString("").filter { it.code in 32..126 }
    }
}
