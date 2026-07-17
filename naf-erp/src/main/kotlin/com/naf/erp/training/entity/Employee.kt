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
    var employeeNumber: String? = null,

    @Column(name = "first_name")
    var firstName: String? = null,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "middle_name")
    var middleName: String? = null,

    @Column(length = 18)
    var curp: String? = null,

    @Column(length = 13)
    var rfc: String? = null,

    @Column(length = 15)
    var nss: String? = null,

    var position: String? = null,

    var department: String? = null,

    var hireDate: LocalDate? = null,

    var active: Boolean = true
)