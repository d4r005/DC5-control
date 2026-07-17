package com.naf.erp.training.service

import com.naf.erp.training.entity.Employee
import com.naf.erp.training.repository.EmployeeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EmployeeService(
    private val employeeRepository: EmployeeRepository
) {

    fun all(): List<Employee> =
        employeeRepository.findAll()

    fun find(id: Long): Employee =
        employeeRepository.findById(id)
            .orElseThrow {
                RuntimeException("Empleado no encontrado")
            }

    fun save(employee: Employee): Employee =
        employeeRepository.save(employee)

    fun delete(id: Long) =
        employeeRepository.deleteById(id)

}
