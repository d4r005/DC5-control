package com.naf.erp.training.document

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class LocalStorage(
    @Value("\${dc3.output}")
    private val folder: String
) : DocumentStorage {

    override fun save(
        fileName: String,
        bytes: ByteArray
    ): String {
        val file = File(folder, fileName)
        file.writeBytes(bytes)
        return file.absolutePath
    }

}
