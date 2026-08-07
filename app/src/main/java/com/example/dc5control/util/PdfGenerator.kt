package com.example.dc5control.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.dc5control.data.model.Agent
import com.example.dc5control.data.model.Course
import com.example.dc5control.data.model.Employee
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
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
    val fechaInicio: String,
    val fechaFin: String,
    val areaTematica: String,
    val agenteCapacitador: String,
    val stpsAgente: String,
    val instructor: String,
    val representanteLegal: String = "",
    val representanteTrabajadores: String? = null,
    val qrUrl: String? = null,
    val logoX: Float? = null, val logoY: Float? = null, val logoW: Float? = null, val logoH: Float? = null,
    val firmaX: Float? = null, val firmaY: Float? = null, val firmaW: Float? = null, val firmaH: Float? = null,
    val headerLogoX: Float? = null, val headerLogoY: Float? = null, val headerLogoW: Float? = null, val headerLogoH: Float? = null,
    val headerSloganX: Float? = null, val headerSloganY: Float? = null, val headerSloganSize: Float? = null,
    val sloganX: Float? = null, val sloganY: Float? = null, val sloganSize: Float? = null,
    val qrX: Float? = null, val qrY: Float? = null, val qrSz: Float? = null,
    val folio: String? = null,
    val folioX: Float? = null, val folioY: Float? = null, val folioSize: Float? = null
)

object PdfGenerator {
    private const val TAG = "PdfGenerator"
    private const val TEMPLATE_ASSET = "plantilla_dc3.pdf"
    private const val PH = 792f

    // Extract raw image bytes from a base64 data URI string
    // This bypasses Bitmap decode/encode to preserve original quality and transparency
    fun base64ToImageBytes(base64: String): ByteArray? {
        return try {
            val data = base64.substringAfter("base64,")
            Base64.decode(data, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding base64 image", e)
            null
        }
    }

    // Keep for backward compatibility (DC3HistoryScreen)
    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val data = base64.substringAfter("base64,")
            val bytes = Base64.decode(data, Base64.NO_WRAP)
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) { null }
    }

    private fun getTemplateStream(context: Context, url: String?): java.io.InputStream {
        if (url.isNullOrBlank()) {
            return context.assets.open(TEMPLATE_ASSET)
        }
        
        return try {
            // Implementación simple de descarga (bloqueante, pero corre en hilo de generación)
            val client = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bytes = response.body?.bytes()
                if (bytes != null) return java.io.ByteArrayInputStream(bytes)
            }
            context.assets.open(TEMPLATE_ASSET)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading remote template: ${e.message}")
            context.assets.open(TEMPLATE_ASSET)
        }
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
        design: com.example.dc5control.data.model.AgentDesign? = null,
        employeePhotoBase64: String? = null,
        qrUrl: String? = null,
        folio: String? = null
    ): File {
        PDFBoxResourceLoader.init(context)
        val inputStream = getTemplateStream(context, design?.dc3TemplateUrl)
        val document = PDDocument.load(inputStream)
        inputStream.close()

        val font = PDType1Font.HELVETICA
        val fontB = PDType1Font.HELVETICA_BOLD
        val fontI = PDType1Font.TIMES_ITALIC

        // Helper: create PDImageXObject directly from base64 (preserves original quality)
        fun embedImage(base64: String?, tag: String): PDImageXObject? {
            if (base64.isNullOrBlank()) return null
            return try {
                val bytes = base64ToImageBytes(base64) ?: return null
                PDImageXObject.createFromByteArray(document, bytes, tag)
            } catch (e: Exception) {
                Log.e(TAG, "Error embedding image '$tag'", e)
                null
            }
        }

        // --- PAGINA 1 ---
        val page = document.getPage(0)
        val cs = PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)

        fun rect(x: Float, yFitz: Float, w: Float, h: Float) {
            cs.setNonStrokingColor(1f, 1f, 1f)
            cs.addRect(x, PH - yFitz - h, w, h)
            cs.fill()
            cs.setNonStrokingColor(0f, 0f, 0f)
        }

        fun text(x: Float, yFitz: Float, t: String, sz: Float = 9f, bold: Boolean = false) {
            if (t.isBlank()) return
            cs.beginText()
            cs.setFont(if (bold) fontB else font, sz)
            cs.newLineAtOffset(x, PH - yFitz)
            cs.showText(sanitize(t).uppercase())
            cs.endText()
        }

        fun textCentered(xC: Float, yF: Float, t: String, sz: Float = 9f, bold: Boolean = false, italic: Boolean = false, subtractSize: Boolean = false) {
            if (t.isBlank()) return
            val f = when { italic -> fontI; bold -> fontB; else -> font }
            val st = sanitize(t).uppercase()
            val w = f.getStringWidth(st) / 1000 * sz
            val y = if (subtractSize) PH - yF - sz else PH - yF
            cs.beginText()
            cs.setFont(f, sz)
            cs.newLineAtOffset(xC - (w/2f), y)
            cs.showText(st)
            cs.endText()
        }

        fun cell(ch: String, xC: Float, yF: Float, sz: Float = 8f) {
            if (ch.isBlank()) return
            val w = font.getStringWidth(ch) / 1000 * sz
            cs.beginText()
            cs.setFont(font, sz)
            cs.newLineAtOffset(xC - (w/2f), PH - yF)
            cs.showText(ch.uppercase())
            cs.endText()
        }

        // --- DIBUJAR IMAGENES (base64 directo, igual que la web) ---

        // Foto del trabajador
        if (!employeePhotoBase64.isNullOrBlank()) {
            embedImage(employeePhotoBase64, "p")?.let { img ->
                cs.drawImage(img, 24f, PH - 126f, 72f, 90f)
            }
        }

        // Header logo
        embedImage(design?.headerLogoBase64, "h")?.let { img ->
            val hlw = design?.headerLogoW ?: 120f
            val hlh = design?.headerLogoH ?: 55f
            val hlx = design?.headerLogoX ?: 30f
            val hly = design?.headerLogoY ?: 10f
            cs.drawImage(img, hlx, PH - hly - hlh, hlw, hlh)
        }

        // Header slogan — web resta el font size: y = PH - y - hss
        design?.headerSlogan?.takeIf { it.isNotBlank() }?.let {
            textCentered(design.headerSloganX ?: 306f, design.headerSloganY ?: 18f, it, design.headerSloganSize ?: 9f, italic = true, subtractSize = true)
        }

        // Slogan — web resta el font size: y = PH - y - ss
        design?.slogan?.takeIf { it.isNotBlank() }?.let {
            textCentered(design.sloganX ?: 30f, design.sloganY ?: 445f, it, design.sloganSize ?: 7f, subtractSize = true)
        }

        // --- TEXTO ---

        // 1. Nombre
        val workerFullName = "${employee.apellidoPaterno} ${employee.apellidoMaterno ?: ""} ${employee.nombres}".trim()
        text(30f, 172.5f, workerFullName, 9f, true)

        // 2. CURP
        val curpC = floatArrayOf(32.0f,47.4f,62.8f,78.2f,93.5f,108.8f,124.2f,139.6f,155.0f,170.3f,185.8f,201.2f,216.6f,231.9f,247.2f,262.6f,278.0f,293.3f)
        employee.curp.replace(" ","").take(18).forEachIndexed { i, c -> if (i < curpC.size) cell(c.toString(), curpC[i], 196f) }

        // 3. Ocupacion
        text(307f, 199f, (employee.occupation.ifBlank { employee.position }).take(50), 9f)

        // 4. Puesto
        rect(26f, 213f, 559f, 13f)
        text(30f, 221f, employee.position, 9f)

        // 5. Empresa
        rect(26f, 271f, 559f, 17f)
        text(30f, 283f, companyName, 9f, true)

        // 6. RFC
        val rfcC = floatArrayOf(34.9f,52.1f,66.0f,80.8f,95.4f,109.8f,124.0f,138.3f,152.8f,167.0f,181.2f,195.5f,209.8f,227.4f,245.1f)
        val cleanRfc = companyRfc.uppercase().replace(" ", "")
        cleanRfc.take(15).forEachIndexed { i, c -> if (i < rfcC.size) cell(c.toString(), rfcC[i], 311f) }

        // 7. Curso
        rect(26f, 355f, 559f, 11f)
        text(30f, 363f, course.name, 9f)

        // 8. Duracion
        rect(26f, 381f, 50f, 13f)
        text(30f, 391f, course.durationHours, 8f)

        // 9. Fechas
        val (fStart, fEnd) = parseFechas(startDate, endDate)
        val aI = floatArrayOf(260.2f, 276.1f, 292.2f, 308.4f); val mI = floatArrayOf(326.9f, 348.2f); val dI = floatArrayOf(369.6f, 390.7f)
        val aF = floatArrayOf(432.9f, 452.4f, 471.9f, 491.4f); val mF = floatArrayOf(511.8f, 532.8f); val dF = floatArrayOf(554.2f, 575.7f)
        fStart.a.forEachIndexed { i,c -> if(i<4 && c!=' ') cell(c.toString(), aI[i], 389f) }
        fStart.m.forEachIndexed { i,c -> if(i<2 && c!=' ') cell(c.toString(), mI[i], 389f) }
        fStart.d.forEachIndexed { i,c -> if(i<2 && c!=' ') cell(c.toString(), dI[i], 389f) }
        fEnd.a.forEachIndexed { i,c -> if(i<4 && c!=' ') cell(c.toString(), aF[i], 389f) }
        fEnd.m.forEachIndexed { i,c -> if(i<2 && c!=' ') cell(c.toString(), mF[i], 389f) }
        fEnd.d.forEachIndexed { i,c -> if(i<2 && c!=' ') cell(c.toString(), dF[i], 389f) }

        // 10. Area Tematica
        rect(26f, 407f, 559f, 11f)
        text(30f, 415f, course.thematicArea ?: "", 8f)

        // 11. Agente
        rect(26f, 431f, 559f, 11f)
        text(30f, 440f, "${agent.name}  ${agent.stps}", 8f)

        // 12. Firmas — limpiar zonas
        rect(63f, 511f, 137f, 27f); rect(218f, 511f, 153f, 27f); rect(389f, 511f, 153f, 27f)

        val sIX = 132f; val sPX = 295f; val sRX = 465f; val sY1 = 522f; val sY2 = 532f

        // Logo con opacidad 0.8 (igual que web)
        embedImage(design?.logoBase64, "l")?.let { img ->
            val lw = design?.logoW ?: 115f
            val lh = design?.logoH ?: 65f
            val lx = design?.logoX ?: 58f
            val ly = design?.logoY ?: 454f
            val gs = PDExtendedGraphicsState().apply { nonStrokingAlphaConstant = 0.8f }
            cs.setGraphicsStateParameters(gs)
            cs.drawImage(img, lx, PH - ly - lh, lw, lh)
            val gsReset = PDExtendedGraphicsState().apply { nonStrokingAlphaConstant = 1.0f }
            cs.setGraphicsStateParameters(gsReset)
        }

        // Firma
        embedImage(design?.firmaBase64, "s")?.let { img ->
            val fw = design?.firmaW ?: 85f
            val fh = design?.firmaH ?: 60f
            val fx = design?.firmaX ?: 88f
            val fy = design?.firmaY ?: 457f
            cs.drawImage(img, fx, PH - fy - fh, fw, fh)
        }

        // QR Code
        qrUrl?.let { url ->
            QRGenerator.generateQR(url)?.let { qrBitmap ->
                try {
                    val stream = ByteArrayOutputStream()
                    qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val img = PDImageXObject.createFromByteArray(document, stream.toByteArray(), "qr")
                    val qsz = design?.qrSz ?: 60f
                    val qx = design?.qrX ?: 480f
                    val qy = design?.qrY ?: 60f
                    cs.drawImage(img, qx, PH - qy - qsz, qsz, qsz)
                } catch (e: Exception) {
                    Log.e(TAG, "Error embedding QR", e)
                }
            }
        }

        // Folio
        folio?.let { f ->
            val fx = design?.dc3FolioX ?: 480f
            val fy = design?.dc3FolioY ?: 740f
            val fsz = design?.dc3FolioSz ?: 9f
            cs.beginText()
            cs.setFont(fontB, fsz)
            cs.setNonStrokingColor(0, 0, 0)
            cs.newLineAtOffset(fx, PH - fy - fsz)
            cs.showText(f)
            cs.endText()
        }

        // Nombres en zonas de firma
        val insL = splitName(agent.name, 26); textCentered(sIX, sY1, insL[0], 8f)
        if (insL.size > 1) textCentered(sIX, sY2, insL[1], 8f)

        val patL = splitName(companyPatron, 28); textCentered(sPX, sY1, patL[0], 8f)
        if (patL.size > 1) textCentered(sPX, sY2, patL[1], 8f)

        companyRepresentante?.let { r ->
            val repL = splitName(r, 26); textCentered(sRX, sY1, repL[0], 8f)
            if (repL.size > 1) textCentered(sRX, sY2, repL[1], 8f)
        }
        cs.close()

        // --- PAGINA 2 (Reverso) ---
        // Espejo exacto del layout web: eslogan arriba (centrado) y logo debajo
        // (centrado), sin encimarse, con espacio antes de que arranque el
        // catálogo STPS impreso.
        if (document.numberOfPages > 1) {
            val pageR = document.getPage(1)
            val csR = PDPageContentStream(document, pageR, PDPageContentStream.AppendMode.APPEND, true, true)
            val pageCenterX = 306f

            design?.headerSlogan?.takeIf { it.isNotBlank() }?.let {
                val sz = (design.headerSloganSize ?: 9f) * 0.8f
                val st = sanitize(it)
                val w = fontI.getStringWidth(st) / 1000 * sz
                val sloganTopMargin = 22f // distancia del borde superior a la linea base del eslogan
                csR.beginText()
                csR.setFont(fontI, sz)
                csR.newLineAtOffset(pageCenterX - w / 2, PH - sloganTopMargin)
                csR.showText(st)
                csR.endText()
            }

            embedImage(design?.headerLogoBase64, "hR")?.let { img ->
                val hlw = (design?.headerLogoW ?: 120f) * 0.7f
                val hlh = (design?.headerLogoH ?: 55f) * 0.7f
                val logoTopMargin = 34f // distancia del borde superior al TOP del logo (debajo del eslogan)
                csR.drawImage(img, pageCenterX - hlw / 2, PH - logoTopMargin - hlh, hlw, hlh)
            }

            csR.close()
        }

        val name = "DC3_${sanitize(workerFullName.replace(" ","_"))}.pdf"
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val out = File(downloadsDir, name)

        Log.d(TAG, "Saving temporary PDF to: ${out.absolutePath}")
        document.save(out)
        document.close()
        return out
    }

    private data class Fecha(val a: String, val m: String, val d: String)

    private fun parseFechas(start: String, end: String): Pair<Fecha, Fecha> {
        fun parse(f: String): Fecha {
            if (f.contains('-')) {
                val parts = f.split('-')
                return Fecha(parts.getOrElse(0) { "    " }.padEnd(4, ' '), parts.getOrElse(1) { "  " }.padEnd(2, ' '), parts.getOrElse(2) { "  " }.padEnd(2, ' '))
            } else {
                val parts = f.split('/')
                return Fecha(parts.getOrElse(2) { "    " }.padEnd(4, ' '), parts.getOrElse(1) { "  " }.padEnd(2, ' '), parts.getOrElse(0) { "  " }.padEnd(2, ' '))
            }
        }
        return Pair(parse(start), parse(end))
    }

    private fun splitName(n: String, m: Int): List<String> {
        if (n.length <= m) return listOf(n)
        val idx = n.lastIndexOf(' ', m)
        return if (idx > 0) listOf(n.substring(0, idx), n.substring(idx + 1)) else listOf(n.take(m), n.drop(m))
    }

    fun openPdf(c: Context, f: File) {
        try {
            Log.d(TAG, "Opening PDF: ${f.absolutePath}")
            val uri = FileProvider.getUriForFile(c, "${c.packageName}.fileprovider", f)
            c.startActivity(Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error opening PDF: ${e.message}", e)
        }
    }

    fun saveToDownloads(c: Context, sf: File): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return try {
            val resolver = c.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, sf.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    sf.inputStream().use { inputStream ->
                        val bytes = inputStream.readBytes()
                        Log.d(TAG, "Writing ${bytes.size} bytes to Downloads via Uri: $uri")
                        outputStream.write(bytes)
                    }
                }
                true
            } else {
                Log.e(TAG, "Failed to insert record into MediaStore.Downloads")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to Downloads: ${e.message}", e)
            false
        }
    }

    private fun sanitize(s: String): String {
        val map = mapOf('á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n', 'Á' to 'A', 'É' to 'E', 'Í' to 'I', 'Ó' to 'O', 'Ú' to 'U', 'Ü' to 'U', 'Ñ' to 'N')
        return s.map { map[it] ?: it }.joinToString("")
    }

    
    fun mergePdfs(files: List<File>, outputFile: File): File? {
        val srcDocs = mutableListOf<PDDocument>()
        return try {
            val mergedDoc = PDDocument()
            for (file in files) {
                val srcDoc = PDDocument.load(file)
                srcDocs.add(srcDoc)
                for (page in srcDoc.pages) {
                    // Clonar la pagina para que no dependa del documento origen
                    val clonedPage = mergedDoc.importPage(page)
                    // importPage clona la pagina pero pierde el rotation
                    clonedPage.rotation = page.rotation
                }
            }
            mergedDoc.save(outputFile)
            mergedDoc.close()
            Log.d(TAG, "Merged ${files.size} PDFs into ${outputFile.name}")
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error merging PDFs: ${e.message}", e)
            null
        } finally {
            srcDocs.forEach { runCatching { it.close() } }
        }
    }

}