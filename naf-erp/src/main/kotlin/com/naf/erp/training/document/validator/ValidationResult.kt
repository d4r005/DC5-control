package com.naf.erp.training.document.validator

data class ValidationResult(
    val valid: Boolean,
    val errors: MutableList<String> = mutableListOf()
)
