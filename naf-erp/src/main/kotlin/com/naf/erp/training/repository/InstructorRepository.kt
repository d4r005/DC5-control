package com.naf.erp.training.repository

import com.naf.erp.training.entity.Instructor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InstructorRepository : JpaRepository<Instructor, Long> {
    fun findByFullNameContainingIgnoreCase(name: String): List<Instructor>
}