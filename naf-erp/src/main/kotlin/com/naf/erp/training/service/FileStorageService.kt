package com.naf.erp.training.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Service
class FileStorageService(
    @Value("\${dc3.output}")
    private val folder: String
) {

    init {
        val path = File(folder)
        if (!path.exists()) {
            path.mkdirs()
        }
    }

    fun save(
        bytes: ByteArray,
        name: String
    ): String {
        val file = Paths.get(folder, name)
        Files.write(file, bytes)
        return file.toString()
    }

    fun getFile(fileName: String): File {
        return File(folder, fileName)
    }
}
