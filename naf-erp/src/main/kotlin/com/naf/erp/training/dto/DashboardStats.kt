package com.naf.erp.training.dto

data class DashboardStats(
    val generados: Long,
    val pendientes: Long,
    val vencidos: Long,
    val porVencer: Long
)
