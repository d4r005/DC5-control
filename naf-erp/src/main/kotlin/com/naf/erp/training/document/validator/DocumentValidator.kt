package com.naf.erp.training.document.validator

interface DocumentValidator<T> {

    fun validate(data: T): ValidationResult

}
