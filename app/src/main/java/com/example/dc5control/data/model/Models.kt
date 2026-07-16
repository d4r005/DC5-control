package com.example.dc5control.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Worker(
    val name: String,
    val curp: String,
    val occupation: String,
    val position: String
)

@Serializable
data class Company(
    val businessName: String,
    val rfc: String
)

@Serializable
data class Course(
    val name: String,
    val durationHours: Int,
    val thematicArea: String
)

@Serializable
data class TrainingAgent(
    val name: String,
    val stpsRegistry: String
)

@Serializable
data class DC3Record(
    val workerId: String,
    val companyName: String,
    val courseName: String,
    val agentName: String,
    val startDate: String,
    val endDate: String
)
