package com.example.dc5control.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Worker(
    val id: String? = null,
    val name: String = "",
    val curp: String = "",
    val occupation: String = "",
    val position: String = "",
    val creatorEmail: String? = null
)

@Serializable
data class Company(
    val id: String? = null,
    val name: String = "",
    val rfc: String = "",
    val creatorEmail: String? = null
)

@Serializable
data class Course(
    val id: String? = null,
    val name: String = "",
    val durationHours: Int = 8,
    val thematicArea: String = "",
    val creatorEmail: String? = null
)

@Serializable
data class TrainingAgent(
    val id: String? = null,
    val name: String = "",
    val stps: String = "",
    val creatorEmail: String? = null
)

@Serializable
data class DC3Record(
    val id: String? = null,
    val workerId: String = "",
    val workerName: String = "",
    val courseName: String = "",
    val companyName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val representativePatron: String = "",
    val representativeWorkers: String = "",
    val creatorEmail: String? = null
)
