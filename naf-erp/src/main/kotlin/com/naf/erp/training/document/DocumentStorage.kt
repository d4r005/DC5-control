package com.naf.erp.training.document

interface DocumentStorage {

    fun save(
        fileName: String,
        bytes: ByteArray
    ): String

}
