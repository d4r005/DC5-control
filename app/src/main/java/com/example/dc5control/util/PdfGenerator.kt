// ============================================================
// PdfGenerator.kt
// Rellena la plantilla oficial DC-3 STPS con datos reales.
// Usa PDFBox 3.0.1 (ya declarado en build.gradle.kts).
//
// Coordenadas calibradas con pymupdf sobre el PDF oficial STPS
// (612 × 792 pts / Letter). Origen PyMuPDF: esquina sup-izq.
// PDFBox usa Y desde abajo → y_pdfbox = 792 - y_fitz
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

/**
 * Datos necesarios para generar una constancia DC-3.
 * fechaInicio / fechaFin → formato "dd/mm/yyyy"
 */
data class DC3FormData(
    val nombreTrabajador: String,
    val curp: String,
    val ocupacion: String,
    val puesto: String,
    val razonSocial: String,
    val rfc: String,
    val nombreCurso: String,
    val duracionHoras: String,
    val fechaInicio: String,   // "dd/mm/yyyy"
    val fechaFin: String,      // "dd/mm/yyyy"
    val areaTematica: String,
    val agenteCapacitador: String,
    val instructor: String,
    val patron: String,
    val representante: String? = null,
    val signatureBitmap: Bitmap? = null,
    val logoBitmap: Bitmap? = null
)

object PdfGenerator {

    private const val TEMPLATE_ASSET   = "plantilla_dc3.pdf"
    private const val LOGO_ASSET       = "logo_luber.png"
    private const val FIRMA_ASSET      = "firma_instructor.png"

    // ── Fuentes ──────────────────────────────────────────────────────────────
    private val FONT_REGULAR get() = PDType1Font(FontName.HELVETICA)
    private val FONT_BOLD    get() = PDType1Font(FontName.HELVETICA_BOLD)

    // ── Coordenadas CURP: 18 celdas individuales ─────────────────────────────
    private val CURP_XS = floatArrayOf(
        31.9f, 47.3f, 62.8f, 78.3f, 93.5f, 108.9f, 124.4f, 139.7f, 155.1f,
        170.5f, 185.8f, 201.2f, 216.6f, 231.9f, 247.3f, 262.7f, 278.0f, 293.4f
    )
    private const val CURP_Y = 195.0f

    // ── Coordenadas RFC: 15 celdas individuales ───────────────────────────────
    private val RFC_XS = floatArrayOf(
        35.5f, 50.0f, 64.5f, 79.0f, 93.5f, 108.0f, 122.5f, 137.0f, 151.5f,
        166.0f, 180.5f, 195.0f, 209.5f, 224.0f, 238.5f
    )
    private const val RFC_Y = 308.0f

    // ── Coordenadas de FECHA — centros exactos de celda (pymupdf) ────────────
    // Orden en formulario: Año (4) | Mes (2) | Día (2) — para inicio y fin
    private val AÑO_INI = floatArrayOf(259.9f, 275.6f, 291.9f, 308.1f)
    private val MES_INI = floatArrayOf(326.6f, 348.0f)
    private val DIA_INI = floatArrayOf(369.4f, 390.4f)
    private val AÑO_FIN = floatArrayOf(432.5f, 452.1f, 471.7f, 491.2f)
    private val MES_FIN = floatArrayOf(511.6f, 532.5f)
    private val DIA_FIN = floatArrayOf(554.0f, 575.5f)
    private const val FECHA_Y = 389.0f

    // ── Coordenadas campos de texto ───────────────────────────────────────────
    private const val Y_NOMBRE_TRAB   = 170.0f
    private const val Y_OCUPACION     = 193.0f
    private const val X_OCUPACION     = 305.0f
    private const val Y_PUESTO        = 223.0f
    private const val Y_EMPRESA       = 282.0f
    private const val Y_CURSO         = 365.0f
    private const val Y_DURACION      = 391.0f
    private const val Y_AREA          = 416.0f
    private const val Y_AGENTE        = 438.0f

    // Área de firmas: sección Y=443–540 (fitz), columnas X≈25-180 / 181-400 / 401-586
    // Nombre instructor: Y=535 (sobre línea "Nombre y firma" que está en Y=540)
    private const val Y_FIRMA_NOMBRE  = 535.0f
    private const val X_FIRMA_INS     = 26.0f
    private const val X_FIRMA_PAT     = 222.0f
    private const val X_FIRMA_REP     = 385.0f

    // Logo LUBER en columna instructor — cubre toda la sección
    // Fitz: X=25-175 (150pt ancho), Y=446-530 (84pt alto)
    private const val LOGO_X = 25f;  private const val LOGO_Y = 446f
    private const val LOGO_W = 150f; private const val LOGO_H = 84f

    // Firma azul — encima del logo, centrada en columna instructor
    // Fitz: X=40-150 (110pt), Y=453-513 (60pt)
    private const val FIRMA_X = 40f;  private const val FIRMA_Y = 453f
    private const val FIRMA_W = 110f; private const val FIRMA_H = 60f

    // ── Tamaños de fuente ─────────────────────────────────────────────────────
    private const val FS_NORMAL = 9f
    private const val FS_SMALL  = 8f
    private const val FS_BOX    = 7f

    // ─────────────────────────────────────────────────────────────────────────
    //  API PÚBLICA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera el PDF DC-3 llenando la plantilla oficial STPS.
     * Carga firma y logo desde assets (firma_instructor.png / logo_luber.png).
     * Si se pasan bitmaps como parámetros, los usa en su lugar.
     */
    fun generate(context: Context, data: DC3FormData): File {
        val doc = loadTemplate(context)
        try {
            val page = doc.getPage(0)

            // Rellenar campos de texto
            PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
                fillPage(cs, data)
            }

            // Logo LUBER (fondo, primero)
            val logoBmp = data.logoBitmap ?: loadAssetBitmap(context, LOGO_ASSET)
            logoBmp?.let {
                insertImage(doc, page, it, LOGO_X, LOGO_Y, LOGO_W, LOGO_H)
            }

            // Firma azul (encima del logo)
            val firmaBmp = data.signatureBitmap ?: loadAssetBitmap(context, FIRMA_ASSET)
            firmaBmp?.let {
                insertImage(doc, page, it, FIRMA_X, FIRMA_Y, FIRMA_W, FIRMA_H)
            }

            val name = "DC3_${sanitize(data.curp.ifBlank { data.nombreTrabajador })}.pdf"
            val out = File(context.getExternalFilesDir(null), name)
            doc.save(out)
            return out
        } finally {
            doc.close()
        }
    }

    /**
     * Versión simplificada para generar DC-3 desde modelos de datos.
     */
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
    ): File {
        val data = DC3FormData(
            nombreTrabajador = "${employee.lastName} ${employee.firstName} ${employee.middleName ?: ""}".trim(),
            curp = employee.curp,
            ocupacion = employee.position,
            puesto = employee.position,
            razonSocial = companyName,
            rfc = companyRfc,
            nombreCurso = course.name,
            duracionHoras = "${course.duration} HORAS",
            fechaInicio = startDate,
            fechaFin = endDate,
            areaTematica = course.thematicArea,
            agenteCapacitador = "${instructor.fullName} ${instructor.stpsNumber ?: ""}".trim(),
            instructor = instructor.fullName,
            patron = companyPatron,
            representante = companyRepresentante,
            signatureBitmap = signatureBitmap,
            logoBitmap = logoBitmap
        )
        return generate(context, data)
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LLENADO DE LA PÁGINA
    // ─────────────────────────────────────────────────────────────────────────

    private fun fillPage(cs: PDPageContentStream, d: DC3FormData) {
        val PH = 792f

        fun w(font: PDType1Font, size: Float, x: Float, yFitz: Float, text: String) {
            if (text.isBlank()) return
            cs.beginText()
            cs.setFont(font, size)
            cs.newLineAtOffset(x, PH - yFitz)
            cs.showText(sanitize(text))
            cs.endText()
        }

        fun boxes(font: PDType1Font, size: Float, chars: String, xs: FloatArray, yFitz: Float) {
            chars.forEachIndexed { i, c ->
                if (i < xs.size) w(font, size, xs[i], yFitz, c.toString())
            }
        }

        // Parsear "dd/mm/yyyy" → Triple(año4, mes2, dia2)
        fun parseFecha(f: String): Triple<String, String, String> {
            if (f.length < 10) return Triple("", "", "")
            val parts = f.split("/")
            return Triple(
                parts.getOrElse(2) { "" }.padStart(4, '0'),
                parts.getOrElse(1) { "" }.padStart(2, '0'),
                parts.getOrElse(0) { "" }.padStart(2, '0')
            )
        }

        // 1. Nombre trabajador
        w(FONT_BOLD, FS_NORMAL, 30f, Y_NOMBRE_TRAB, d.nombreTrabajador.uppercase())

        // 2. CURP (carácter por celda)
        boxes(FONT_REGULAR, FS_BOX, d.curp.uppercase().take(18), CURP_XS, CURP_Y)

        // 3. Ocupación
        w(FONT_REGULAR, FS_BOX, X_OCUPACION, Y_OCUPACION, d.ocupacion.uppercase().take(50))

        // 4. Puesto (borrar texto de ejemplo de la plantilla + reescribir)
        cs.setNonStrokingColor(1f, 1f, 1f)
        cs.addRect(25f, PH - 231f, 560f, 18f)
        cs.fill()
        cs.setNonStrokingColor(0f, 0f, 0f)
        w(FONT_REGULAR, FS_NORMAL, 30f, Y_PUESTO, d.puesto.uppercase())

        // 5. Empresa
        w(FONT_BOLD, FS_NORMAL, 30f, Y_EMPRESA, d.razonSocial.uppercase().take(70))

        // 6. RFC (carácter por celda)
        val rfcClean = d.rfc.uppercase().replace("-", "").replace(" ", "").take(15)
        boxes(FONT_REGULAR, FS_BOX, rfcClean, RFC_XS, RFC_Y)

        // 7. Nombre curso
        w(FONT_BOLD, FS_SMALL, 30f, Y_CURSO, d.nombreCurso.uppercase().take(80))

        // 8. Duración
        w(FONT_REGULAR, FS_SMALL, 30f, Y_DURACION, d.duracionHoras)

        // 9. Fecha (dígito por celda)
        val (yi, mi, di) = parseFecha(d.fechaInicio)
        val (yf, mf, df) = parseFecha(d.fechaFin)
        boxes(FONT_REGULAR, FS_BOX, yi, AÑO_INI, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, mi, MES_INI, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, di, DIA_INI, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, yf, AÑO_FIN, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, mf, MES_FIN, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, df, DIA_FIN, FECHA_Y)

        // 10. Área temática
        w(FONT_REGULAR, FS_SMALL, 30f, Y_AREA, d.areaTematica.take(80))

        // 11. Agente capacitador
        w(FONT_REGULAR, FS_SMALL, 30f, Y_AGENTE, d.agenteCapacitador.uppercase().take(70))

        // 12. Nombres en área de firmas
        // Instructor: texto en dos líneas si es largo (max ~20 chars por línea en col 155pt)
        val insLines = splitName(d.instructor.uppercase(), maxChars = 22)
        w(FONT_REGULAR, FS_BOX, X_FIRMA_INS, Y_FIRMA_NOMBRE - (if (insLines.size > 1) 7f else 0f), insLines[0])
        if (insLines.size > 1) w(FONT_REGULAR, FS_BOX, X_FIRMA_INS, Y_FIRMA_NOMBRE, insLines[1])

        // Patrón
        if (d.patron.isNotBlank()) {
            val patLines = splitName(d.patron.uppercase(), maxChars = 25)
            w(FONT_REGULAR, FS_BOX, X_FIRMA_PAT, Y_FIRMA_NOMBRE - (if (patLines.size > 1) 7f else 0f), patLines[0])
            if (patLines.size > 1) w(FONT_REGULAR, FS_BOX, X_FIRMA_PAT, Y_FIRMA_NOMBRE, patLines[1])
        }

        // Representante
        d.representante?.let { rep ->
            if (rep.isNotBlank()) {
                val repLines = splitName(rep.uppercase(), maxChars = 22)
                w(FONT_REGULAR, FS_BOX, X_FIRMA_REP, Y_FIRMA_NOMBRE - (if (repLines.size > 1) 7f else 0f), repLines[0])
                if (repLines.size > 1) w(FONT_REGULAR, FS_BOX, X_FIRMA_REP, Y_FIRMA_NOMBRE, repLines[1])
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Divide un nombre largo en máximo 2 líneas de maxChars caracteres */
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

    /** Carga un bitmap desde assets, retorna null si no existe */
    private fun loadAssetBitmap(context: Context, assetName: String): Bitmap? {
        return try {
            context.assets.open(assetName).use {
                android.graphics.BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            android.util.Log.w("PdfGenerator", "Asset no encontrado: $assetName")
            null
        }
    }

    private fun insertImage(
        doc: PDDocument, page: org.apache.pdfbox.pdmodel.PDPage,
        bmp: Bitmap, x: Float, y: Float, w: Float, h: Float
    ) {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 90, baos)
        val img = PDImageXObject.createFromByteArray(doc, baos.toByteArray(), "img")
        val PH = 792f
        PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
            cs.drawImage(img, x, PH - y - h, w, h)
        }
    }

    /** Elimina acentos y caracteres no ASCII para PDType1Font */
    private fun sanitize(s: String): String {
        val map = mapOf(
            'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n',
            'Á' to 'A', 'É' to 'E', 'Í' to 'I', 'Ó' to 'O', 'Ú' to 'U', 'Ü' to 'U', 'Ñ' to 'N'
        )
        return s.map { map[it] ?: it }.joinToString("").filter { it.code in 32..126 }
    }
}
