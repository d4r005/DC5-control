package com.naf.erp.training.service

import org.springframework.stereotype.Service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.springframework.stereotype.Service
import java.io.File

@Service
class PdfService {

    fun fill(
        templatePath: String,
        outputPath: String,
        data: Map<String, String> // Asumimos que data contiene los textos para los campos
    ) {
        val document = PDDocument.load(File(templatePath))
        val page = document.getPage(0)

        val stream = PDPageContentStream(
            document,
            page,
            AppendMode.APPEND,
            true
        )

        stream.beginText()
        stream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 9f)
        stream.newLineAtOffset(110f, 145f)
        stream.showText("NORTH AMERICA FLOORING")

        stream.endText()
        stream.close()
        document.save("DC3.pdf")
        document.close()
    }

    fun convert(docx: String): String {
        TODO("Sustituido por lógica de llenado de PDF oficial")
    }

}
