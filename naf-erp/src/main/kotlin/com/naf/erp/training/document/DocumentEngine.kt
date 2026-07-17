package com.naf.erp.training.document

interface DocumentEngine {

    fun render(
        template: DocumentTemplate
    ): ByteArray

}
