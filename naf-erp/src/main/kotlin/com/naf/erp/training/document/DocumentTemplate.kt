package com.naf.erp.training.document

data class DocumentTemplate(
    val name: String,
    val file: String,
    val fields: MutableMap<String, String> = mutableMapOf()
)
