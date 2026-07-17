// ============================================================
// PdfGenerator.kt
// Rellena la plantilla oficial DC-3 STPS con datos reales.
// Usa PDFBox 3.0.1 (ya declarado en build.gradle.kts).
//
// Coordenadas calibradas con pymupdf sobre el PDF oficial STPS
// (612 × 792 pts / Letter). Origen: esquina superior-izquierda.
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

    private const val TEMPLATE_ASSET = "plantilla_dc3.pdf"

    // ── Fuentes ──────────────────────────────────────────────────────────────
    private val FONT_REGULAR get() = PDType1Font(FontName.HELVETICA)
    private val FONT_BOLD    get() = PDType1Font(FontName.HELVETICA_BOLD)

    // ── Coordenadas CURP: 18 celdas individuales ─────────────────────────────
    // Centros X de cada celda (extraídos del PDF oficial con pymupdf)
    private val CURP_XS = floatArrayOf(
        31.9f, 47.3f, 62.8f, 78.3f, 93.5f, 108.9f, 124.4f, 139.7f, 155.1f,
        170.5f, 185.8f, 201.2f, 216.6f, 231.9f, 247.3f, 262.7f, 278.0f, 293.4f
    )
    private const val CURP_Y = 195.0f   // Y baseline (coords PDFBox: origen abajo)

    // ── Coordenadas RFC: 15 celdas individuales ───────────────────────────────
    private val RFC_XS = floatArrayOf(
        34.8f, 52.2f, 66.1f, 80.8f, 95.6f, 109.8f, 124.1f, 138.4f, 152.7f,
        167.0f, 181.3f, 195.6f, 209.8f, 227.5f, 245.0f
    )
    private const val RFC_Y = 308.0f

    // ── Coordenadas de FECHA (periodo de ejecución) ───────────────────────────
    // Fila de dígitos y ≈ 389 (PDFBox, Y desde abajo = 792 - 389 = 403)
    private val AÑO_INI = floatArrayOf(275.6f, 291.8f, 308.0f, 324.5f)
    private val MES_INI = floatArrayOf(348.0f, 369.4f)
    private val DIA_INI = floatArrayOf(390.4f, 411.9f)
    private val AÑO_FIN = floatArrayOf(452.1f, 468.3f, 484.5f, 501.0f)
    private val MES_FIN = floatArrayOf(532.5f, 554.0f)
    private val DIA_FIN = floatArrayOf(575.5f, 597.0f)
    private const val FECHA_Y = 389.0f  // Y baseline dígitos de fecha

    // ── Otros campos (Y en coords fitz; PDFBox Y = 792 - Y_fitz) ─────────────
    // Nota: insert_text de fitz y showText de PDFBox usan el mismo baseline
    // porque ambos referencian desde la esquina inferior izquierda del glyph.
    private const val Y_NOMBRE_TRAB   = 170.0f
    private const val Y_OCUPACION     = 193.0f
    private const val X_OCUPACION     = 305.0f
    private const val Y_PUESTO        = 223.0f
    private const val Y_EMPRESA       = 282.0f
    private const val Y_CURSO         = 365.0f
    private const val Y_DURACION      = 391.0f
    private const val Y_AREA          = 416.0f
    private const val Y_AGENTE        = 438.0f
    private const val Y_FIRMA_NOMBRE  = 530.0f
    private const val X_FIRMA_INS     = 70.0f
    private const val X_FIRMA_PAT     = 245.0f
    private const val X_FIRMA_REP     = 415.0f

    // ── Tamaños de fuente ─────────────────────────────────────────────────────
    private const val FS_NORMAL = 9f
    private const val FS_SMALL  = 8f
    private const val FS_BOX    = 7f   // letras individuales en celdas CURP/RFC/fecha

    // ─────────────────────────────────────────────────────────────────────────
    //  API PÚBLICA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera el PDF DC-3 llenando la plantilla oficial STPS.
     * Devuelve el File resultante guardado en el directorio externo de la app.
     */
    fun generate(context: Context, data: DC3FormData): File {
        val doc = loadTemplate(context)
        try {
            val page = doc.getPage(0)
            PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
                fillPage(cs, data)
            }
            
            // Insertar Logo (detrás/debajo de la firma, más pequeño)
            data.logoBitmap?.let {
                insertImage(doc, doc.getPage(0), it, x = 75f, y = 505f, w = 80f, h = 30f)
            }
            
            // Insertar Firma
            data.signatureBitmap?.let {
                insertImage(doc, doc.getPage(0), it, x = 50f, y = 490f, w = 130f, h = 40f)
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
     * Versión simplificada para generar DC-3 desde modelos de datos (Employee, Course, Instructor).
     */
    fun generateDC3(
        context: Context,
        employee: com.example.dc5control.data.model.Employee,
        course: com.example.dc5control.data.model.Course,
        instructor: com.example.dc5control.data.model.Instructor,
        companyName: String,
        companyRfc: String,
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
            agenteCapacitador = instructor.company ?: instructor.fullName,
            instructor = instructor.fullName,
            patron = companyName,
            signatureBitmap = signatureBitmap,
            logoBitmap = logoBitmap
        )
        return generate(context, data)
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LLENADO DE LA PÁGINA
    // ─────────────────────────────────────────────────────────────────────────

    private fun fillPage(cs: PDPageContentStream, d: DC3FormData) {
        val PH = 792f  // alto página Letter

        // Helper: escribe en coordenadas PyMuPDF-style (Y desde arriba)
        // PDFBox usa Y desde abajo → convertimos: y_pdfbox = PH - y_fitz
        fun w(font: PDType1Font, size: Float, x: Float, yFitz: Float, text: String) {
            if (text.isBlank()) return
            cs.beginText()
            cs.setFont(font, size)
            cs.newLineAtOffset(x, PH - yFitz)
            cs.showText(sanitize(text))
            cs.endText()
        }

        // Escribe caracteres individuales en celdas (centrado visual -2.5pt)
        fun boxes(font: PDType1Font, size: Float, chars: String, xs: FloatArray, yFitz: Float) {
            chars.forEachIndexed { i, c ->
                if (i < xs.size) w(font, size, xs[i] - 2.5f, yFitz, c.toString())
            }
        }

        // Parsear fecha "dd/mm/yyyy" → (año, mes, día) como listas de chars
        fun parseFecha(f: String): Triple<String, String, String> {
            if (f.length < 10) return Triple("", "", "")
            val parts = f.split("/")
            return Triple(
                parts.getOrElse(2) { "    " }.padStart(4, '0'),  // año
                parts.getOrElse(1) { "  " }.padStart(2, '0'),    // mes
                parts.getOrElse(0) { "  " }.padStart(2, '0')     // día
            )
        }

        // ── 1. Nombre trabajador ─────────────────────────────────────────────
        w(FONT_BOLD, FS_NORMAL, 30f, Y_NOMBRE_TRAB, d.nombreTrabajador.uppercase())

        // ── 2. CURP (carácter por celda) ─────────────────────────────────────
        boxes(FONT_REGULAR, FS_BOX, d.curp.uppercase().take(18), CURP_XS, CURP_Y)

        // ── 3. Ocupación ─────────────────────────────────────────────────────
        w(FONT_REGULAR, FS_BOX, X_OCUPACION, Y_OCUPACION, d.ocupacion.uppercase().take(50))

        // ── 4. Puesto (sobreescribe el ejemplo de la plantilla) ───────────────
        // Primero borramos con rect blanco — lo hacemos desde el ContentStream
        cs.setNonStrokingColor(1f, 1f, 1f)
        cs.addRect(25f, PH - 231f, 560f, 18f)
        cs.fill()
        cs.setNonStrokingColor(0f, 0f, 0f)
        w(FONT_REGULAR, FS_NORMAL, 30f, Y_PUESTO, d.puesto.uppercase())

        // ── 5. Nombre empresa ─────────────────────────────────────────────────
        w(FONT_BOLD, FS_NORMAL, 30f, Y_EMPRESA, d.razonSocial.uppercase().take(70))

        // ── 6. RFC (carácter por celda) ───────────────────────────────────────
        val rfcClean = d.rfc.uppercase().replace("-", "").replace(" ", "").take(15)
        boxes(FONT_REGULAR, FS_BOX, rfcClean, RFC_XS, RFC_Y)

        // ── 7. Nombre curso ───────────────────────────────────────────────────
        w(FONT_BOLD, FS_SMALL, 30f, Y_CURSO, d.nombreCurso.uppercase().take(80))

        // ── 8. Duración ───────────────────────────────────────────────────────
        w(FONT_REGULAR, FS_SMALL, 30f, Y_DURACION, d.duracionHoras)

        // ── 9. Periodo (dígitos individuales) ────────────────────────────────
        val (yi, mi, di) = parseFecha(d.fechaInicio)
        val (yf, mf, df) = parseFecha(d.fechaFin)
        boxes(FONT_REGULAR, FS_BOX, yi, AÑO_INI, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, mi, MES_INI, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, di, DIA_INI, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, yf, AÑO_FIN, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, mf, MES_FIN, FECHA_Y)
        boxes(FONT_REGULAR, FS_BOX, df, DIA_FIN, FECHA_Y)

        // ── 10. Área temática ─────────────────────────────────────────────────
        w(FONT_REGULAR, FS_SMALL, 30f, Y_AREA, d.areaTematica.take(80))

        // ── 11. Agente capacitador ────────────────────────────────────────────
        w(FONT_REGULAR, FS_SMALL, 30f, Y_AGENTE, d.agenteCapacitador.uppercase().take(70))

        // ── 12. Nombres en área de firmas ─────────────────────────────────────
        w(FONT_REGULAR, FS_SMALL, X_FIRMA_INS, Y_FIRMA_NOMBRE, d.instructor.uppercase())
        w(FONT_REGULAR, FS_SMALL, X_FIRMA_PAT, Y_FIRMA_NOMBRE, d.patron.uppercase())
        d.representante?.let { rep ->
            if (rep.isNotBlank()) w(FONT_REGULAR, FS_SMALL, X_FIRMA_REP, Y_FIRMA_NOMBRE, rep.uppercase())
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadTemplate(context: Context): PDDocument {
        val bytes = context.assets.open(TEMPLATE_ASSET).use { it.readBytes() }
        return Loader.loadPDF(bytes)
    }

    private fun insertImage(doc: PDDocument, page: org.apache.pdfbox.pdmodel.PDPage, bmp: Bitmap, x: Float, y: Float, w: Float, h: Float) {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 90, baos)
        val img = PDImageXObject.createFromByteArray(doc, baos.toByteArray(), "img_${System.currentTimeMillis()}")
        val PH = 792f
        PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
            cs.drawImage(img, x, PH - y, w, h)
        }
    }

    /** Elimina acentos y caracteres no ASCII para evitar crash con PDType1Font */
    private fun sanitize(s: String): String {
        val map = mapOf(
            'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n',
            'Á' to 'A', 'É' to 'E', 'Í' to 'I', 'Ó' to 'O', 'Ú' to 'U', 'Ü' to 'U', 'Ñ' to 'N'
        )
        return s.map { map[it] ?: it }.joinToString("")
            .filter { it.code in 32..126 }
    }
}
