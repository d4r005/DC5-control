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
    @SerialName("cedula_profesional") val cedulaProfesional: String? = null,
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
    @SerialName("stps_id") val stpsId: String? = null,
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
    @SerialName("document_type") val documentType: String = "DC3",
    val folio: String? = null,
    @SerialName("folio_dc3") val folioDc3: String? = null,
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
    @SerialName("header_logo_x") val headerLogoX: Float? = null,
    @SerialName("header_logo_y") val headerLogoY: Float? = null,
    @SerialName("header_logo_w") val headerLogoW: Float? = null,
    @SerialName("header_logo_h") val headerLogoH: Float? = null,
    @SerialName("header_slogan") val headerSlogan: String? = null,
    @SerialName("header_slogan_x") val headerSloganX: Float? = null,
    @SerialName("header_slogan_y") val headerSloganY: Float? = null,
    @SerialName("header_slogan_size") val headerSloganSize: Float? = null,
    @SerialName("header_slogan_font") val headerSloganFont: String? = null,
    @SerialName("slogan") val slogan: String? = null,
    @SerialName("slogan_x") val sloganX: Float? = null,
    @SerialName("slogan_y") val sloganY: Float? = null,
    @SerialName("slogan_size") val sloganSize: Float? = null,
    @SerialName("slogan_font") val sloganFont: String? = null,
    @SerialName("agent_name") val agentName: String? = null,
    @SerialName("diploma_template_base64") val diplomaTemplateBase64: String? = null,
    @SerialName("dip_worker_x") val dipWorkerX: Float? = null,
    @SerialName("dip_worker_y") val dipWorkerY: Float? = null,
    @SerialName("dip_worker_sz") val dipWorkerSz: Float? = null,
    @SerialName("dip_course_x") val dipCourseX: Float? = null,
    @SerialName("dip_course_y") val dipCourseY: Float? = null,
    @SerialName("dip_course_sz") val dipCourseSz: Float? = null,
    @SerialName("dip_duration_x") val dipDurationX: Float? = null,
    @SerialName("dip_duration_y") val dipDurationY: Float? = null,
    @SerialName("dip_duration_sz") val dipDurationSz: Float? = null,
    @SerialName("dip_date_x") val dipDateX: Float? = null,
    @SerialName("dip_date_y") val dipDateY: Float? = null,
    @SerialName("dip_date_sz") val dipDateSz: Float? = null,
    @SerialName("dip_agent_x") val dipAgentX: Float? = null,
    @SerialName("dip_agent_y") val dipAgentY: Float? = null,
    @SerialName("dip_agent_sz") val dipAgentSz: Float? = null,
    @SerialName("dip_stps_x") val dipStpsX: Float? = null,
    @SerialName("dip_stps_y") val dipStpsY: Float? = null,
    @SerialName("dip_stps_sz") val dipStpsSz: Float? = null,
    @SerialName("dip_folio_x") val dipFolioX: Float? = null,
    @SerialName("dip_folio_y") val dipFolioY: Float? = null,
    @SerialName("dip_folio_sz") val dipFolioSz: Float? = null,
    @SerialName("dip_cedula_x") val dipCedulaX: Float? = null,
    @SerialName("dip_cedula_y") val dipCedulaY: Float? = null,
    @SerialName("dip_cedula_sz") val dipCedulaSz: Float? = null,
    @SerialName("qr_x") val qrX: Float? = null,
    @SerialName("qr_y") val qrY: Float? = null,
    @SerialName("qr_sz") val qrSz: Float? = null,
    @SerialName("dip_qr_x") val dipQrX: Float? = null,
    @SerialName("dip_qr_y") val dipQrY: Float? = null,
    @SerialName("dip_qr_sz") val dipQrSz: Float? = null,
    @SerialName("dc3_folio_x") val dc3FolioX: Float? = null,
    @SerialName("dc3_folio_y") val dc3FolioY: Float? = null,
    @SerialName("dc3_folio_sz") val dc3FolioSz: Float? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
