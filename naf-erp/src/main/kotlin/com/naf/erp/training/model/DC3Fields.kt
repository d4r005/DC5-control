package com.naf.erp.training.model

data class DC3Fields(
    val trabajador: String,
    val curp: String,
    val ocupacion: String,
    val puesto: String,
    val empresa: String,
    val rfc: String,
    val curso: String,
    val horas: String,
    val fechaInicio: String,
    val fechaFin: String,
    val areaTematica: String,
    val instructor: String,
    val registroSTPS: String,
    val representantePatron: String,
    val representanteTrabajadores: String
) {
    fun toMap() = mapOf(
        "trabajador" to trabajador,
        "curp" to curp,
        "ocupacion" to ocupacion,
        "puesto" to puesto,
        "empresa" to empresa,
        "rfc" to rfc,
        "curso" to curso,
        "horas" to horas,
        "fecha_inicio" to fechaInicio,
        "fecha_fin" to fechaFin,
        "area" to areaTematica,
        "instructor" to instructor,
        "registro" to registroSTPS,
        "patron" to representantePatron,
        "trabajadores" to representanteTrabajadores
    )
}
