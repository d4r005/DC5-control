package com.naf.erp.training.service

import com.naf.erp.training.entity.Course
import com.naf.erp.training.repository.CourseRepository
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val repository: CourseRepository
) {

    fun all(): List<Course> = repository.findAll()

    fun save(course: Course): Course =
        repository.save(course)

}
