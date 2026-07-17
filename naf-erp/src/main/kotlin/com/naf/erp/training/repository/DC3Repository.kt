package com.naf.erp.training.repository

import com.naf.erp.training.entity.DC3
import com.naf.erp.training.entity.Training
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DC3Repository : JpaRepository<DC3, Long> {

    fun existsByTraining(training: Training): Boolean

    fun findByTraining(training: Training): DC3?

}