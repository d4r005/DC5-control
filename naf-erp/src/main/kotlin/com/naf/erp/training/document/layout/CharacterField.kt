package com.naf.erp.training.document.layout

data class CharacterField(
    val tag: String,
    val startX: Float,
    val y: Float,
    val boxWidth: Float,
    val characters: Int
)
