package com.example.dc5control.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Employee(
    val id: Long? = null,
    @SerialName("employee_number") val employeeNumber: String = "",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    @SerialName("middle_name") val middleName: String? = null,
    val curp: String = "",
    val rfc: String = "",
    val nss: String? = null,
    val position: String = "",
    val department: String? = null,
    @SerialName("hire_date") val hireDate: String? = null,
    val active: Boolean = true,
    @SerialName("creatorEmail") val creatorEmail: String? = null
)

@Serializable
data class Instructor(
    val id: Long? = null,
    @SerialName("full_name") val fullName: String = "",
    @SerialName("stps_number") val stpsNumber: String? = null,
    val external: Boolean = false,
    val company: String? = null,
    @SerialName("creatorEmail") val creatorEmail: String? = null
)

@Serializable
data class Company(
    val id: Long? = null,
    val name: String = "",
    val rfc: String = "",
    val creatorEmail: String? = null
)

@Serializable
data class Course(
    val id: Long? = null,
    val name: String = "",
    val duration: Int = 8,
    @SerialName("thematic_area") val thematicArea: String = "",
    @SerialName("occupation_key") val occupationKey: String? = null,
    val description: String? = null,
    @SerialName("creatorEmail") val creatorEmail: String? = null
)

@Serializable
data class ThematicArea(
    val code: String = "",
    val description: String = ""
)

@Serializable
data class Occupation(
    val code: String = "",
    val description: String = ""
)

@Serializable
data class Training(
    val id: Long? = null,
    @SerialName("employee_id") val employeeId: Long,
    @SerialName("course_id") val courseId: Long,
    @SerialName("instructor_id") val instructorId: Long,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val hours: Int,
    val approved: Boolean = true,
    val score: Double? = null,
    @SerialName("creatorEmail") val creatorEmail: String? = null
)

@Serializable
data class DC3(
    val id: Long? = null,
    @SerialName("training_id") val trainingId: Long,
    @SerialName("dc3_number") val dc3Number: String? = null,
    val generated: String? = null,
    val pdf: String? = null,
    val docx: String? = null,
    val signed: Boolean = false,
    @SerialName("creatorEmail") val creatorEmail: String? = null
)

@Serializable
data class DC3Record(
    val id: Long? = null,
    @SerialName("worker_id") val workerId: String = "",
    @SerialName("worker_name") val workerName: String = "",
    @SerialName("course_name") val courseName: String = "",
    @SerialName("company_name") val companyName: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    @SerialName("creatorEmail") val creatorEmail: String? = null
)
