package com.naf.erp.training.repository

import com.naf.erp.training.entity.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<Course, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<Course>
}