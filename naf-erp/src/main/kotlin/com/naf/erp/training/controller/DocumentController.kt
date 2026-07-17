package com.naf.erp.training.controller

import com.naf.erp.training.document.DocumentManager
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/documents")
class DocumentController(
    private val manager: DocumentManager
) {

    @PostMapping("/{code}")
    fun generate(
        @PathVariable code: String,
        @RequestBody fields: MutableMap<String, String>
    ): ByteArray {
        return manager.generate(
            code,
            fields
        )
    }

}
