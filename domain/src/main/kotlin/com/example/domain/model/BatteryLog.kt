package com.voltai.domain.model

data class BatteryLog(
    val id: Long = 0,
    val timestamp: Long,
    val batteryLevel: Int,
    val voltage: Float,
    val temperature: Float,
    val chargingStatus: String,
    val chargingType: String,
    val current: Float
)
