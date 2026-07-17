package com.naf.erp.training.service

import com.naf.erp.training.entity.Instructor
import com.naf.erp.training.repository.InstructorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class InstructorService(
    private val instructorRepository: InstructorRepository
) {
    fun find(id: Long): Instructor =
        instructorRepository.findById(id)
            .orElseThrow { RuntimeException("Instructor no encontrado") }

    fun all(): List<Instructor> = instructorRepository.findAll()

    fun save(instructor: Instructor): Instructor = instructorRepository.save(instructor)

    fun delete(id: Long) = instructorRepository.deleteById(id)
}
