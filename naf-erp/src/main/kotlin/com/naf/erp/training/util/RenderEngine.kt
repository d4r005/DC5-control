package com.naf.erp.training.util

import com.naf.erp.training.model.DC3Coordinates
import com.naf.erp.training.model.FieldPosition

class RenderEngine {

    fun write(
        page: Any, // Se asume una interfaz o clase con drawText
        text: String,
        field: FieldPosition
    ) {
        // En una implementación real, aquí se llamaría al método draw de la librería PDF (iText, PDFBox, etc.)
        // page.drawText(text, field.x.toFloat(), field.y.toFloat())
    }

}
