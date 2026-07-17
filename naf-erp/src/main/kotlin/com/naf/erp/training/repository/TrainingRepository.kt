package com.naf.erp.training.repository

import com.naf.erp.training.entity.Employee
import com.naf.erp.training.entity.Training
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TrainingRepository : JpaRepository<Training, Long> {

    fun findByEmployee(employee: Employee): List<Training>

    fun findByApprovedTrue(): List<Training>

    fun findByApprovedFalse(): List<Training>

    @Query("""
        SELECT t
        FROM Training t
        WHERE t.approved = true
    """)
    fun completed(): List<Training>

    @Query("""
        SELECT t
        FROM Training t
        WHERE t.employee.id = :employeeId
        ORDER BY t.startDate DESC
    """)
    fun employeeHistory(employeeId: Long): List<Training>

    @Query("""
        SELECT t
        FROM Training t
        WHERE t.approved = true
        AND NOT EXISTS (
            SELECT d
            FROM DC3 d
            WHERE d.training = t
        )
    """)
    fun trainingsWithoutDC3(): List<Training>

    fun countByApprovedTrue(): Long

    @Query("SELECT COUNT(t) FROM Training t WHERE t.approved = true AND t.endDate < :date")
    fun countVencidos(date: java.time.LocalDate): Long

    @Query("SELECT COUNT(t) FROM Training t WHERE t.approved = true AND t.endDate BETWEEN :start AND :end")
    fun countPorVencer(start: java.time.LocalDate, end: java.time.LocalDate): Long
}
