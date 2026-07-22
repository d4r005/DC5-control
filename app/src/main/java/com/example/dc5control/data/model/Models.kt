package com.example.dc5control.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Employee(
    val id: Long? = null,
    @SerialName("apellido_paterno") val apellidoPaterno: String = "",
    @SerialName("apellido_materno") val apellidoMaterno: String = "",
    val nombres: String = "",
    val name: String = "",
    val curp: String = "",
    val occupation: String = "",
    val position: String = "",
    @SerialName("photo_url") val photoUrl: String? = null,
    val active: Boolean = true,
    @SerialName("creator_email") val creatorEmail: String? = null
)

@Serializable
data class Agent(
    val id: Long? = null,
    val name: String = "",
    val stps: String = "",
    @SerialName("creator_email") val creatorEmail: String? = null
)

@Serializable
data class Company(
    val id: Long? = null,
    val name: String = "",
    val rfc: String = "",
    @SerialName("representante_legal") val representanteLegal: String = "",
    @SerialName("representante_trabajadores") val representanteTrabajadores: String? = null,
    @SerialName("creator_email") val creatorEmail: String? = null
)

@Serializable
data class Course(
    val id: Long? = null,
    val name: String = "",
    @SerialName("duration_hours") val durationHours: String = "",
    @SerialName("thematic_area") val thematicArea: String = "",
    @SerialName("creator_email") val creatorEmail: String? = null
)

@Serializable
data class DC3Record(
    val id: Long? = null,
    @SerialName("worker_id") val workerId: String = "",
    @SerialName("worker_name") val workerName: String = "",
    @SerialName("worker_pos") val workerPos: String = "",
    @SerialName("course_name") val courseName: String = "",
    @SerialName("duration_hours") val durationHours: String = "",
    @SerialName("thematic_area") val thematicArea: String = "",
    @SerialName("company_name") val companyName: String = "",
    @SerialName("agent_name") val agentName: String = "",
    @SerialName("agent_stps") val agentStps: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    @SerialName("result_text") val resultText: String = "Acreditado",
    @SerialName("creator_email") val creatorEmail: String? = null
)

@Serializable
data class User(
    val name: String,
    val email: String,
    val role: String,
    val password: String = ""
)
