package com.naf.erp.training.entity

import jakarta.persistence.*

@Entity
@Table(name = "document_templates")
class DocumentTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var code: String = "",
    var name: String = "",
    var version: Int = 1,
    var active: Boolean = true,
    var file: String = ""
)
