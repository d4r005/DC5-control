package com.naf.erp.training.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name="trainings")
class Training(

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    var id:Long=0,

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="employee_id")
    lateinit var employee:Employee,

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="course_id")
    lateinit var course:Course,

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="instructor_id")
    lateinit var instructor:Instructor,

    var startDate:LocalDate?=null,

    var endDate:LocalDate?=null,

    var hours:Int=0,

    var approved:Boolean=false,

    var score:Double=0.0
)