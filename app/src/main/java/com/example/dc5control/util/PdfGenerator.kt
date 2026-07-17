package com.example.dc5control.util

import android.content.Context
import com.example.dc5control.data.model.Course
import com.example.dc5control.data.model.Employee
import com.example.dc5control.data.model.Instructor
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

/**
 * Generador de constancias DC-3 usando Apache PDFBox 3.0.1.
 * Carga una plantilla PDF desde assets y escribe los datos del trabajador,
 * curso, empresa e instructor sobre ella.
 *
 * Coloca tu plantilla oficial STPS en:
 *   app/src/main/assets/plantilla_dc3.pdf
 */
object PdfGenerator {

    private const val TEMPLATE_ASSET = "plantilla_dc3.pdf"

    /**
     * Genera un PDF DC-3 individual para un empleado.
     *
     * @param context      Contexto de la app (para acceder a assets y almacenamiento)
     * @param employee     Datos del trabajador (nombre, CURP, RFC, puesto, ocupación)
     * @param course       Datos del curso (nombre, duración, área temática)
     * @param instructor   Datos del agente capacitador
     * @param companyName   Nombre o razón social de la empresa
     * @param companyRfc    RFC de la empresa
     * @param startDate     Fecha de inicio (formato dd/MM/yyyy)
     * @param endDate       Fecha de fin (formato dd/MM/yyyy)
     * @param signatureBitmap  Bitmap de la firma del instructor (opcional, null = sin firma)
     * @return              El archivo PDF generado
     */
    fun generateDC3(
        context: Context,
        employee: Employee,
        course: Course,
        instructor: Instructor,
        companyName: String,
        companyRfc: String,
        startDate: String,
        endDate: String,
        signatureBitmap: Bitmap? = null
    ): File {

        // 1. Cargar la plantilla PDF desde assets
        val document: PDDocument = loadTemplate(context)

        try {
            // Asumimos que los datos van en la primera página (anverso)
            val page = document.getPage(0)

            // 2. Preparar el flujo de contenido para escribir sobre el PDF
            PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true
            ).use { cs ->

                // --- NOMBRE DEL TRABAJADOR ---
                cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10f)
                cs.beginText()
                cs.newLineAtOffset(120f, 650f)
                cs.showText(formatNombreTrabajador(employee))
                cs.endText()

                // --- CURP ---
                cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 9f)
                cs.beginText()
                cs.newLineAtOffset(120f, 620f)
                cs.showText(employee.curp.uppercase())
                cs.endText()

                // --- RFC DEL TRABAJADOR ---
                cs.beginText()
                cs.newLineAtOffset(350f, 620f)
                cs.showText(employee.rfc.uppercase())
                cs.endText()

                // --- PUESTO / OCUPACIÓN ---
                cs.beginText()
                cs.newLineAtOffset(120f, 590f)
                cs.showText(employee.position.uppercase())
                cs.endText()

                // --- NOMBRE DE LA EMPRESA ---
                cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10f)
                cs.beginText()
                cs.newLineAtOffset(120f, 540f)
                cs.showText(companyName.uppercase())
                cs.endText()

                // --- RFC DE LA EMPRESA ---
                cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 9f)
                cs.beginText()
                cs.newLineAtOffset(120f, 510f)
                cs.showText(companyRfc.uppercase().replace("-", ""))
                cs.endText()

                // --- NOMBRE DEL CURSO ---
                cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10f)
                cs.beginText()
                cs.newLineAtOffset(120f, 460f)
                cs.showText(course.name.uppercase())
                cs.endText()

                // --- DURACIÓN EN HORAS ---
                cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 9f)
                cs.beginText()
                cs.newLineAtOffset(120f, 430f)
                cs.showText("${course.duration} horas")
                cs.endText()

                // --- ÁREA TEMÁTICA ---
                cs.beginText()
                cs.newLineAtOffset(300f, 430f)
                cs.showText(course.thematicArea.uppercase())
                cs.endText()

                // --- PERIODO: FECHA INICIO Y FIN ---
                cs.beginText()
                cs.newLineAtOffset(120f, 400f)
                cs.showText("$startDate - $endDate")
                cs.endText()

                // --- NOMBRE DEL AGENTE CAPACITADOR ---
                cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9f)
                cs.beginText()
                cs.newLineAtOffset(120f, 350f)
                cs.showText(instructor.fullName.uppercase())
                cs.endText()

                // --- NÚMERO DE REGISTRO STPS ---
                cs.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 8f)
                cs.beginText()
                cs.newLineAtOffset(120f, 330f)
                cs.showText(instructor.stpsNumber ?: "")
                cs.endText()
            }

            // 3. Insertar la firma del instructor si está disponible
            if (signatureBitmap != null) {
                insertarFirma(document, page, signatureBitmap)
            }

            // 4. Guardar el documento generado
            val fileName = "DC3_${employee.curp.ifEmpty { employee.lastName + "_" + employee.firstName }}.pdf"
            val outFile = File(context.getExternalFilesDir(null), fileName)
            document.save(outFile)

            return outFile

        } finally {
            document.close()
        }
    }

    /**
     * Genera constancias DC-3 en lote para una lista de empleados.
     * Retorna la lista de archivos PDF generados.
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
    ): List<File> {
        return employees.map { employee ->
            generateDC3(
                context, employee, course, instructor,
                companyName, companyRfc, startDate, endDate, signatureBitmap
            )
        }
    }

    // ====== HELPERS ======

    /**
     * Carga la plantilla PDF desde los assets de la app.
     * Si no existe la plantilla, crea un PDF en blanco de tamaño A4.
     */
    private fun loadTemplate(context: Context): PDDocument {
        return try {
            val assetManager = context.assets
            val inputStream: InputStream = assetManager.open(TEMPLATE_ASSET)
            val bytes = inputStream.readBytes()
            inputStream.close()
            Loader.loadPDF(bytes)
        } catch (e: Exception) {
            // Si no hay plantilla, crear un documento A4 en blanco
            org.apache.pdfbox.pdmodel.PDDocument().also { doc ->
                val page = org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4)
                doc.addPage(page)
            }
        }
    }

    /**
     * Formatea el nombre completo del trabajador: Apellido Paterno + Materno + Nombre(s)
     */
    private fun formatNombreTrabajador(employee: Employee): String {
        return buildString {
            append(employee.lastName.uppercase())
            if (!employee.middleName.isNullOrBlank()) {
                append(" ")
                append(employee.middleName.uppercase())
            }
            append(" ")
            append(employee.firstName.uppercase())
        }
    }

    /**
     * Inserta la imagen de la firma del instructor en el PDF.
     * La firma se coloca en la posición típica de firma del agente capacitador.
     */
    private fun insertarFirma(document: PDDocument, page: org.apache.pdfbox.pdmodel.PDPage, bitmap: Bitmap) {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageData = stream.toByteArray()
            stream.close()

            val pdImage = PDImageXObject.createFromByteArray(document, imageData, "firma")

            PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true
            ).use { cs ->
                // Posición de la firma (esquina inferior izquierda del área de firma)
                val x = 120f
                val y = 260f
                val width = 150f
                val height = (pdImage.height.toFloat() / pdImage.width.toFloat()) * width
                cs.drawImage(pdImage, x, y, width, height)
            }
        } catch (e: Exception) {
            // Si falla la inserción de la firma, continuamos sin ella
            e.printStackTrace()
        }
    }
}
