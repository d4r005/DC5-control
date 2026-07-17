package com.naf.erp.training.document.renderer

import com.naf.erp.training.document.layout.CharacterField
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.springframework.stereotype.Service

@Service
class CharacterRenderer {

    fun render(
        stream: PDPageContentStream,
        field: CharacterField,
        value: String
    ) {
        value.take(field.characters)
            .forEachIndexed { index, c ->
                stream.beginText()
                stream.newLineAtOffset(
                    field.startX + (field.boxWidth * index),
                    field.y
                )
                stream.showText(c.toString())
                stream.endText()
            }
    }
}
