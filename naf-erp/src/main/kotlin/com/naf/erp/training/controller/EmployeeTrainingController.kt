package com.naf.erp.training.controller

import com.naf.erp.training.entity.Employee
import com.naf.erp.training.entity.Training
import com.naf.erp.training.service.EmployeeService
import com.naf.erp.training.service.TrainingService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employees")
class EmployeeTrainingController(
    private val employeeService: EmployeeService,
    private val trainingService: TrainingService
) {

    @GetMapping
    fun getAllEmployees(): List<Employee> = employeeService.all()

    @PostMapping
    fun saveEmployee(@RequestBody employee: Employee): Employee = employeeService.save(employee)

    @GetMapping("/{id}/history")
    fun getEmployeeHistory(@PathVariable id: Long): List<Training> = trainingService.history(id)

    @PostMapping("/trainings")
    fun saveTraining(@RequestBody training: Training): Training = trainingService.save(training)
}
