package com.naf.erp.training.controller

import com.naf.erp.training.dto.DC3Response
import com.naf.erp.training.mapper.DC3Mapper
import com.naf.erp.training.service.DC3Service
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dc3")
class DC3Controller(
    private val dc3Service: DC3Service
) {

    @GetMapping
    fun list(): List<DC3Response> {
        return dc3Service.all().map { DC3Mapper.map(it) }
    }

    @PostMapping("/generate/{trainingId}")
    fun generate(@PathVariable trainingId: Long): ResponseEntity<DC3Response> {
        val dc3 = dc3Service.generate(trainingId)
        return ResponseEntity.ok(DC3Mapper.map(dc3))
    }

    @GetMapping("/pdf/{id}")
    fun pdf(@PathVariable id: Long): ResponseEntity<FileSystemResource> {
        val pdf = dc3Service.pdf(id)
        val resource = FileSystemResource(pdf)

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"DC3.pdf\""
            )
            .contentType(MediaType.APPLICATION_PDF)
            .body(resource)
    }

    @GetMapping("/docx/{id}")
    fun docx(@PathVariable id: Long): ResponseEntity<FileSystemResource> {
        val docx = dc3Service.docx(id)
        val resource = FileSystemResource(docx)

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"DC3.docx\""
            )
            .body(resource)
    }

    @PostMapping("/reprint/{id}")
    fun reprint(@PathVariable id: Long): DC3Response {
        return DC3Mapper.map(dc3Service.reprint(id))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = dc3Service.delete(id)
}
