package com.naf.erp.training.entity

import jakarta.persistence.*

@Entity
@Table(name="courses")
class Course(

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    var id:Long=0,

    var name:String="",

    var duration:Int=0,

    var thematicArea:String="",

    var occupationKey:String="",

    @Column(columnDefinition="TEXT")
    var description:String=""
)