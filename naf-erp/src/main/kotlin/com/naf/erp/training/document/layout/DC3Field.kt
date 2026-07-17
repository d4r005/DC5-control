package com.naf.erp.training.document.layout

enum class Alignment {
    LEFT,
    CENTER,
    RIGHT
}

data class DC3Field(
    val tag: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val fontSize: Int,
    val alignment: Alignment
)
