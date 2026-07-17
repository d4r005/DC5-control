package com.naf.erp.training.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name="dc3")
class DC3(

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    var id:Long=0,

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="training_id")
    lateinit var training:Training,

    var dc3Number:String="",

    var generated:LocalDateTime=LocalDateTime.now(),

    var pdf:String="",

    var docx:String="",

    var signed:Boolean=false
)