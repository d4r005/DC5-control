package com.naf.erp.training.document.renderer

import com.naf.erp.training.document.layout.DateField
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DateRenderer(
    private val characterRenderer: CharacterRenderer
) {

    fun render(
        stream: PDPageContentStream,
        field: DateField,
        date: LocalDate
    ) {
        characterRenderer.render(
            stream,
            field.year,
            date.year.toString()
        )
        characterRenderer.render(
            stream,
            field.month,
            "%02d".format(date.monthValue)
        )
        characterRenderer.render(
            stream,
            field.day,
            "%02d".format(date.dayOfMonth)
        )
    }
}
