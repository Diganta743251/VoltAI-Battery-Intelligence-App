package com.voltai.domain.model

data class ChargingCycle(
    val cycleId: Long,
    val startTime: Long,
    val endTime: Long,
    val startPercentage: Int,
    val endPercentage: Int,
    val maxTemperature: Int,
    val maxCurrent: Int
)
