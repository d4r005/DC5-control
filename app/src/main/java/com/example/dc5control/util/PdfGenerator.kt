package com.example.dc5control.util

import android.content.Context
import android.graphics.Bitmap
import com.example.dc5control.data.model.Course
import com.example.dc5control.data.model.Employee
import com.example.dc5control.data.model.Instructor
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Generador de constancias DC-3 con Apache PDFBox 3.0.1.
 *
 * Coordenadas calibradas sobre la plantilla oficial STPS (612 × 792 pts / Letter).
 * Coloca el archivo en: app/src/main/assets/plantilla_dc3.pdf
 */
object PdfGenerator {

    private const val TEMPLATE_ASSET = "plantilla_dc3.pdf"

    // ── Fuentes ────────────────────────────────────────────────────────────────
    private val FONT_REGULAR get() = PDType1Font(FontName.HELVETICA)
    private val FONT_BOLD    get() = PDType1Font(FontName.HELVETICA_BOLD)

    // ── Coordenadas X de los 18 boxes del CURP (extraídas del PDF real) ────────
    private val CURP_BOX_CENTERS = floatArrayOf(
        32.2f, 47.7f, 63.1f, 78.6f, 93.8f, 109.2f, 124.8f, 140.1f, 155.4f,
        170.8f, 186.2f, 201.6f, 216.9f, 232.3f, 247.6f, 263.0f, 278.4f, 293.8f
    )
    private const val CURP_Y_TEXT = 598.5f   // baseline de texto dentro del box

    // ── Coordenadas X de los 15 boxes del RFC empresa ─────────────────────────
    // RFC tiene 12 chars + guión + 3 homoclave → los 15 boxes coinciden
    private val RFC_BOX_CENTERS = floatArrayOf(
        35.1f, 52.6f, 66.5f, 81.1f, 95.9f, 110.2f, 124.5f, 138.8f, 153.1f,
        167.3f, 181.6f, 195.9f, 210.2f, 227.8f, 245.3f
    )
    private const val RFC_Y_TEXT = 486.0f    // baseline de texto dentro del box

    // ── Y-posiciones de cada campo (coordenada baseline del texto, eje Y ↑) ───
    private const val Y_NOMBRE_TRABAJADOR  = 626.0f  // fila nombre, entre header(650) y línea(617)
    private const val Y_OCUPACION          = 598.5f  // misma fila que CURP, columna derecha
    private const val X_OCUPACION          = 307.0f
    private const val Y_PUESTO             = 570.0f  // fila puesto
    private const val Y_NOMBRE_EMPRESA     = 516.5f  // entre header empresa(542) y línea(502)
    private const val Y_NOMBRE_CURSO       = 432.0f  // entre header curso(453) y línea(423)
    private const val Y_DURACION           = 406.0f  // fila duración/periodo
    private const val X_DURACION           = 31.0f
    // Periodo: De [Año][Mes][Día] a [Año][Mes][Día]
    // Extraído de pdfminer: Año izq x≈278, Mes izq x≈331, Día izq x≈374, Año der x≈455, Mes der x≈515, Día der x≈558
    private const val Y_FECHA              = 406.0f
    private const val X_FECHA_INI_ANIO    = 254.0f
    private const val X_FECHA_INI_MES     = 308.0f
    private const val X_FECHA_INI_DIA     = 352.0f
    private const val X_FECHA_FIN_ANIO    = 432.0f
    private const val X_FECHA_FIN_MES     = 492.0f
    private const val X_FECHA_FIN_DIA     = 536.0f
    private const val Y_AREA_TEMATICA     = 381.0f  // debajo label área y0=387
    private const val Y_AGENTE_NOMBRE     = 356.0f  // debajo label agente y0=363
    // Firmas: 3 columnas — Instructor (izq), Patrón (centro), Representante (der)
    private const val Y_FIRMA             = 248.0f  // justo sobre la línea de firma y≈258
    private const val X_FIRMA_INSTRUCTOR  = 64.0f
    private const val X_FIRMA_PATRON      = 228.0f
    private const val X_FIRMA_NOMBRE_INS  = 80.0f   // x del nombre escrito bajo la firma
    private const val Y_FIRMA_NOMBRE      = 240.0f  // baseline nombre bajo línea de firma

    // ── Tamaños de fuente ──────────────────────────────────────────────────────
    private const val SIZE_NORMAL  = 9f
    private const val SIZE_BOLD    = 10f
    private const val SIZE_SMALL   = 8f
    private const val SIZE_CHAR    = 8f   // letras individuales en boxes

    // ─────────────────────────────────────────────────────────────────────────
    //  API PÚBLICA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera un PDF DC-3 individual llenando la plantilla oficial STPS.
     */
    fun generateDC3(
        context: Context,
        employee: Employee,
        course: Course,
        instructor: Instructor,
        companyName: String,
        companyRfc: String,
        startDate: String,   // "dd/MM/yyyy"
        endDate: String,     // "dd/MM/yyyy"
        signatureBitmap: Bitmap? = null
    ): File {
        val document = loadTemplate(context)
        try {
            val page = document.getPage(0)

            PDPageContentStream(document, page, AppendMode.APPEND, true, true).use { cs ->
                llenarAnverso(cs, employee, course, instructor,
                    companyName, companyRfc, startDate, endDate)
            }

            if (signatureBitmap != null) {
                insertarFirma(document, page, signatureBitmap)
            }

            val safeName = employee.curp.ifBlank { "${employee.lastName}_${employee.firstName}" }
                .replace(Regex("[^A-Za-z0-9_]"), "_")
            val outFile = File(context.getExternalFilesDir(null), "DC3_$safeName.pdf")
            document.save(outFile)
            return outFile
        } finally {
            document.close()
        }
    }

    /**
     * Genera constancias DC-3 en lote para una lista de empleados.
     */
    fun generateDC3Batch(
        context: Context,
        employees: List<Employee>,
        course: Course,
        instructor: Instructor,
        companyName: String,
        companyRfc: String,
        startDate: String,
        endDate: String,
        signatureBitmap: Bitmap? = null
    ): List<File> = employees.map { emp ->
        generateDC3(context, emp, course, instructor,
            companyName, companyRfc, startDate, endDate, signatureBitmap)
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LLENADO DEL ANVERSO
    // ─────────────────────────────────────────────────────────────────────────

    private fun llenarAnverso(
        cs: PDPageContentStream,
        employee: Employee,
        course: Course,
        instructor: Instructor,
        companyName: String,
        companyRfc: String,
        startDate: String,
        endDate: String
    ) {
        // 1 ── NOMBRE DEL TRABAJADOR ──────────────────────────────────────────
        escribir(cs, FONT_BOLD, SIZE_BOLD, 31.0f, Y_NOMBRE_TRABAJADOR,
            formatNombre(employee))

        // 2 ── CURP (un carácter por box) ─────────────────────────────────────
        escribirEnBoxes(cs, employee.curp.uppercase().take(18), CURP_BOX_CENTERS, CURP_Y_TEXT)

        // 3 ── OCUPACIÓN ESPECÍFICA ────────────────────────────────────────────
        escribir(cs, FONT_REGULAR, SIZE_SMALL, X_OCUPACION, Y_OCUPACION,
            employee.position.uppercase().take(60))

        // 4 ── PUESTO ─────────────────────────────────────────────────────────
        // (ya viene impreso "AUXILIAR DE RECURSOS HUMANOS" en la plantilla de ejemplo)
        // Lo sobreescribimos con el puesto real del empleado
        escribir(cs, FONT_REGULAR, SIZE_NORMAL, 31.0f, Y_PUESTO,
            employee.position.uppercase())

        // 5 ── NOMBRE DE LA EMPRESA ────────────────────────────────────────────
        escribir(cs, FONT_BOLD, SIZE_BOLD, 31.0f, Y_NOMBRE_EMPRESA,
            companyName.uppercase())

        // 6 ── RFC EMPRESA (un carácter por box, max 15) ───────────────────────
        val rfcClean = companyRfc.uppercase().replace("-", "").take(15)
        escribirEnBoxes(cs, rfcClean, RFC_BOX_CENTERS, RFC_Y_TEXT)

        // 7 ── NOMBRE DEL CURSO ────────────────────────────────────────────────
        escribir(cs, FONT_BOLD, SIZE_BOLD, 31.0f, Y_NOMBRE_CURSO,
            course.name.uppercase())

        // 8 ── DURACIÓN EN HORAS ───────────────────────────────────────────────
        escribir(cs, FONT_REGULAR, SIZE_NORMAL, X_DURACION, Y_DURACION,
            "${course.duration} hrs")

        // 9 ── PERIODO (Año/Mes/Día inicio → Año/Mes/Día fin) ──────────────────
        val (dIni, mIni, aIni) = parseFecha(startDate)
        val (dFin, mFin, aFin) = parseFecha(endDate)
        escribir(cs, FONT_REGULAR, SIZE_CHAR, X_FECHA_INI_ANIO, Y_FECHA, aIni)
        escribir(cs, FONT_REGULAR, SIZE_CHAR, X_FECHA_INI_MES,  Y_FECHA, mIni)
        escribir(cs, FONT_REGULAR, SIZE_CHAR, X_FECHA_INI_DIA,  Y_FECHA, dIni)
        escribir(cs, FONT_REGULAR, SIZE_CHAR, X_FECHA_FIN_ANIO, Y_FECHA, aFin)
        escribir(cs, FONT_REGULAR, SIZE_CHAR, X_FECHA_FIN_MES,  Y_FECHA, mFin)
        escribir(cs, FONT_REGULAR, SIZE_CHAR, X_FECHA_FIN_DIA,  Y_FECHA, dFin)

        // 10 ─ ÁREA TEMÁTICA ──────────────────────────────────────────────────
        escribir(cs, FONT_REGULAR, SIZE_NORMAL, 31.0f, Y_AREA_TEMATICA,
            course.thematicArea.uppercase())

        // 11 ─ NOMBRE DEL AGENTE CAPACITADOR ──────────────────────────────────
        escribir(cs, FONT_REGULAR, SIZE_NORMAL, 31.0f, Y_AGENTE_NOMBRE,
            instructor.fullName.uppercase())

        // 12 ─ NOMBRE DEL INSTRUCTOR BAJO LA LÍNEA DE FIRMA ───────────────────
        escribir(cs, FONT_REGULAR, SIZE_SMALL, X_FIRMA_NOMBRE_INS, Y_FIRMA_NOMBRE,
            instructor.fullName.uppercase())
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Escribe texto en coordenadas absolutas dentro del PDPageContentStream. */
    private fun escribir(
        cs: PDPageContentStream,
        font: PDType1Font,
        size: Float,
        x: Float,
        y: Float,
        text: String
    ) {
        if (text.isBlank()) return
        cs.beginText()
        cs.setFont(font, size)
        cs.newLineAtOffset(x, y)
        cs.showText(sanitize(text))
        cs.endText()
    }

    /** Escribe un carácter por box, centrado horizontalmente en cada celda. */
    private fun escribirEnBoxes(
        cs: PDPageContentStream,
        text: String,
        centers: FloatArray,
        yText: Float
    ) {
        val font = FONT_REGULAR
        val size = SIZE_CHAR
        cs.setFont(font, size)
        text.forEachIndexed { i, char ->
            if (i >= centers.size) return
            val charStr = sanitize(char.toString())
            if (charStr.isBlank()) return@forEachIndexed
            // Centrar el carácter: medir su ancho y desplazar la mitad
            val charWidthPts = font.getStringWidth(charStr) / 1000f * size
            val x = centers[i] - charWidthPts / 2f
            cs.beginText()
            cs.newLineAtOffset(x, yText)
            cs.showText(charStr)
            cs.endText()
        }
    }

    /** Carga la plantilla DC-3 desde assets. Si no existe, devuelve un A4 en blanco. */
    private fun loadTemplate(context: Context): PDDocument {
        return try {
            context.assets.open(TEMPLATE_ASSET).use { stream ->
                Loader.loadPDF(stream.readBytes())
            }
        } catch (e: Exception) {
            PDDocument().also { doc ->
                doc.addPage(PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER))
            }
        }
    }

    /** Inserta la imagen de la firma del instructor en el área de firmas. */
    private fun insertarFirma(document: PDDocument, page: PDPage, bitmap: Bitmap) {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val pdImage = PDImageXObject.createFromByteArray(document, stream.toByteArray(), "firma")
            val fw = 120f
            val fh = (pdImage.height.toFloat() / pdImage.width.toFloat()) * fw

            PDPageContentStream(document, page, AppendMode.APPEND, true, true).use { cs ->
                cs.drawImage(pdImage, X_FIRMA_INSTRUCTOR, Y_FIRMA, fw, fh)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** "Apellido Paterno Materno Nombre(s)" */
    private fun formatNombre(e: Employee): String = buildString {
        append(e.lastName.uppercase())
        if (!e.middleName.isNullOrBlank()) { append(" "); append(e.middleName.uppercase()) }
        append(" ")
        append(e.firstName.uppercase())
    }.trim()

    /**
     * Descompone "dd/MM/yyyy" → Triple(día, mes, año).
     * También acepta "yyyy-MM-dd". Si el formato no se reconoce devuelve ("","","").
     */
    private fun parseFecha(fecha: String): Triple<String, String, String> {
        val slashParts = fecha.split("/")
        if (slashParts.size == 3) return Triple(slashParts[0], slashParts[1], slashParts[2])
        val dashParts = fecha.split("-")
        if (dashParts.size == 3) return Triple(dashParts[2], dashParts[1], dashParts[0])
        return Triple("", "", "")
    }

    /**
     * Elimina caracteres que PDType1Font (WIN-1252) no puede representar.
     * Reemplaza acentos, ñ, ü por sus equivalentes ASCII para evitar crash.
     */
    private fun sanitize(text: String): String = text
        .replace("Á", "A").replace("É", "E").replace("Í", "I")
        .replace("Ó", "O").replace("Ú", "U").replace("Ñ", "N")
        .replace("Ü", "U").replace("á", "a").replace("é", "e")
        .replace("í", "i").replace("ó", "o").replace("ú", "u")
        .replace("ñ", "n").replace("ü", "u")
        .filter { it.code in 32..255 }
}
