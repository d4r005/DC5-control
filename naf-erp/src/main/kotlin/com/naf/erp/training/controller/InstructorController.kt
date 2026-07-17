package com.naf.erp.training.controller

import com.naf.erp.training.entity.Instructor
import com.naf.erp.training.service.InstructorService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/instructors")
class InstructorController(
    private val instructorService: InstructorService
) {
    @GetMapping
    fun all(): List<Instructor> = instructorService.all()

    @PostMapping
    fun save(@RequestBody instructor: Instructor): Instructor = instructorService.save(instructor)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = instructorService.delete(id)
}
