package com.naf.erp.training.controller

import com.naf.erp.training.entity.Course
import com.naf.erp.training.service.CourseService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/courses")
class CourseController(
    private val courseService: CourseService
) {
    @GetMapping
    fun all(): List<Course> = courseService.all()

    @PostMapping
    fun save(@RequestBody course: Course): Course = courseService.save(course)
}
