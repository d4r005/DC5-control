package com.naf.erp.training.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "generated_documents")
class GeneratedDocument(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var templateId: Long = 0, // Referencia a la versión exacta
    var templateCode: String = "",
    var employeeId: Long = 0,
    var generated: LocalDateTime = LocalDateTime.now(),
    var pdf: String = "",
    var docx: String = "",
    var hash: String = ""
)
