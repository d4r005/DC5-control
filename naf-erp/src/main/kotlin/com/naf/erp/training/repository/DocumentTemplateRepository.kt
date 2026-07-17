package com.naf.erp.training.repository

import com.naf.erp.training.entity.DocumentTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentTemplateRepository : JpaRepository<DocumentTemplate, Long> {

    fun findByCodeAndActiveTrue(code: String): DocumentTemplate?

}
