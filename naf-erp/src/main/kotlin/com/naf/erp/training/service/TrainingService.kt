package com.naf.erp.training.service

import com.naf.erp.training.entity.Training
import com.naf.erp.training.repository.TrainingRepository
import org.springframework.stereotype.Service

@Service
class TrainingService(
    private val repository: TrainingRepository
) {

    fun history(employeeId: Long): List<Training> =
        repository.employeeHistory(employeeId)

    fun pending(): List<Training> =
        repository.findByApprovedFalse()

    fun approved(): List<Training> =
        repository.completed()

    fun save(training: Training): Training =
        repository.save(training)

}
