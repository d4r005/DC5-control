package com.naf.erp.training.document.renderer

import com.naf.erp.training.document.layout.DC3Field
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.springframework.stereotype.Service

@Service
class TextRenderer {

    fun render(
        stream: PDPageContentStream,
        field: DC3Field,
        text: String
    ) {
        stream.beginText()
        // La lógica de posicionamiento y alineación se implementará aquí
        stream.endText()
    }
}
