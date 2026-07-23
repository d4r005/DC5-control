package com.example.dc5control.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

/**
 * Serializador flexible que acepta String, Int, Float, Boolean o null
 * y siempre devuelve un String. Necesario porque la BD de Supabase
 * almacena duration_hours como entero en la tabla courses pero como
 * string en dc3_records.
 */
object AnyToStringSerializer : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("AnyToString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeString()
        val element = jsonDecoder.decodeJsonElement()
        return when {
            element is JsonPrimitive -> element.content
            element is JsonNull -> ""
            else -> element.toString()
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

@Serializable
data class Employee(
    val id: String? = null,
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
    val id: String? = null,
    val name: String = "",
    val stps: String = "",
    @SerialName("creator_email") val creatorEmail: String? = null
)

@Serializable
data class Company(
    val id: String? = null,
    val name: String = "",
    val rfc: String = "",
    @SerialName("representante_legal") val representanteLegal: String = "",
    @SerialName("representante_trabajadores") val representanteTrabajadores: String? = null,
    @SerialName("creator_email") val creatorEmail: String? = null
)

@Serializable
data class Course(
    val id: String? = null,
    val name: String = "",
    @Serializable(with = AnyToStringSerializer::class)
    @SerialName("duration_hours") val durationHours: String = "",
    @SerialName("thematic_area") val thematicArea: String? = null,
    @SerialName("creator_email") val creatorEmail: String? = null
)

@Serializable
data class DC3Record(
    val id: String? = null,
    @SerialName("worker_id") val workerId: String = "",
    @SerialName("worker_name") val workerName: String = "",
    @SerialName("worker_pos") val workerPos: String = "",
    @SerialName("course_name") val courseName: String = "",
    @Serializable(with = AnyToStringSerializer::class)
    @SerialName("duration_hours") val durationHours: String = "",
    @SerialName("thematic_area") val thematicArea: String? = null,
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

@Serializable
data class AgentDesign(
    val id: String? = null,
    @SerialName("creator_email") val creatorEmail: String? = null,
    @SerialName("logo_base64") val logoBase64: String? = null,
    @SerialName("logo_x") val logoX: Float? = null,
    @SerialName("logo_y") val logoY: Float? = null,
    @SerialName("logo_w") val logoW: Float? = null,
    @SerialName("logo_h") val logoH: Float? = null,
    @SerialName("firma_base64") val firmaBase64: String? = null,
    @SerialName("firma_x") val firmaX: Float? = null,
    @SerialName("firma_y") val firmaY: Float? = null,
    @SerialName("firma_w") val firmaW: Float? = null,
    @SerialName("firma_h") val firmaH: Float? = null,
    @SerialName("header_logo_base64") val headerLogoBase64: String? = null,
    @SerialName("header_slogan") val headerSlogan: String? = null,
    @SerialName("slogan") val slogan: String? = null,
    @SerialName("agent_name") val agentName: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
