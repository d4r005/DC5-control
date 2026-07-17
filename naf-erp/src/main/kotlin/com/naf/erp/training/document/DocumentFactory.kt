package com.naf.erp.training.document

interface DocumentFactory<T> {

    fun create(data: T): DocumentTemplate

}
