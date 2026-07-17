package com.naf.erp.training.repository

import com.naf.erp.training.entity.Employee
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface EmployeeRepository : JpaRepository<Employee, Long> {
    fun findByEmployeeNumber(employeeNumber: String): Employee?
    fun findAllByActiveTrue(): List<Employee>
    override fun findAll(pageable: Pageable): Page<Employee>

    @Query("""
        SELECT e
        FROM Employee e
        WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    fun search(search: String): List<Employee>
}
