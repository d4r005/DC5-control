package com.naf.erp.training.dto

data class DC3Response(
    val id: Long,
    val number: String,
    val employee: String,
    val course: String,
    val generated: String,
    val pdf: String,
    val docx: String
)
