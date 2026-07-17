package com.example.dc5control.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Worker(
    val name: String = "",
    val curp: String = "",
    val occupation: String = "",
    val position: String = "",
    val creator_email: String? = null
)

@Serializable
data class Company(
    val name: String = "",
    val rfc: String = "",
    val creator_email: String? = null
)

@Serializable
data class Course(
    val name: String = "",
    val duration_hours: Int = 8,
    val creator_email: String? = null
)

@Serializable
data class TrainingAgent(
    val name: String = "",
    val stps: String = "",
    val creator_email: String? = null
)

@Serializable
data class DC3Record(
    val worker_id: String = "",
    val worker_name: String = "",
    val course_name: String = "",
    val company_name: String = "",
    val start_date: String = "",
    val end_date: String = "",
    val creator_email: String? = null
)
