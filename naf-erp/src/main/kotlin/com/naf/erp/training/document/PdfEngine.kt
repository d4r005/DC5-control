package com.naf.erp.training.document

import org.springframework.stereotype.Service

@Service
class PdfEngine : DocumentEngine {

    override fun render(template: DocumentTemplate): ByteArray {
        // Implementación para generar PDF basado en el PDF oficial y coordenadas
        TODO("Implementación de renderizado PDF oficial")
    }

}
