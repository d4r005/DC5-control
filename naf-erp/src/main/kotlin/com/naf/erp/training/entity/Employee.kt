package com.naf.erp.training.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "employees")
class Employee(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "employee_number", unique = true)
    var employeeNumber: String = "",

    @Column(name = "first_name")
    var firstName: String = "",

    @Column(name = "last_name")
    var lastName: String = "",

    @Column(name = "middle_name")
    var middleName: String = "",

    @Column(length = 18)
    var curp: String = "",

    @Column(length = 13)
    var rfc: String = "",

    @Column(length = 15)
    var nss: String = "",

    var position: String = "",

    var department: String = "",

    var hireDate: LocalDate? = null,

    var active: Boolean = true
)