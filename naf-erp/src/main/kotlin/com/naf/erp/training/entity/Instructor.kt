package com.naf.erp.training.entity

import jakarta.persistence.*

@Entity
@Table(name = "instructors")
class Instructor(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "full_name")
    var fullName: String = "",

    @Column(name = "stps_number")
    var stpsNumber: String? = null,

    var external: Boolean = false,

    var company: String? = null
)