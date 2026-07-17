package com.naf.erp.training.model

data class FieldPosition(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

object DC3Coordinates {
    val TRABAJADOR = FieldPosition(
        112,
        148,
        390,
        18
    )

    val CURP = FieldPosition(
        145,
        188,
        240,
        18
    )

    val PUESTO = FieldPosition(
        120,
        250,
        360,
        18
    )
}
