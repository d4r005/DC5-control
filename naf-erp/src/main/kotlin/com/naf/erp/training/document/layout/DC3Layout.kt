package com.naf.erp.training.document.layout

object DC3Layout {
    val trabajador = DC3Field(
        tag = "trabajador",
        x = 120f,
        y = 150f,
        width = 360f,
        fontSize = 9,
        alignment = Alignment.LEFT
    )

    val empresa = DC3Field(
        tag = "empresa",
        x = 120f,
        y = 310f,
        width = 380f,
        fontSize = 9,
        alignment = Alignment.LEFT
    )

    val curso = DC3Field(
        tag = "curso",
        x = 120f,
        y = 430f,
        width = 380f,
        fontSize = 9,
        alignment = Alignment.LEFT
    )

    val curp = CharacterField(
        tag = "curp",
        startX = 145f,
        y = 182f,
        boxWidth = 12f,
        characters = 18
    )

    val rfc = CharacterField(
        tag = "rfc",
        startX = 145f,
        y = 335f,
        boxWidth = 12f,
        characters = 13
    )

    val fechaInicio = DateField(
        tag = "fechaInicio",
        year = CharacterField(
            tag = "anio",
            startX = 315f,
            y = 468f,
            boxWidth = 11f,
            characters = 4
        ),
        month = CharacterField(
            tag = "mes",
            startX = 370f,
            y = 468f,
            boxWidth = 11f,
            characters = 2
        ),
        day = CharacterField(
            tag = "dia",
            startX = 398f,
            y = 468f,
            boxWidth = 11f,
            characters = 2
        )
    )
}
