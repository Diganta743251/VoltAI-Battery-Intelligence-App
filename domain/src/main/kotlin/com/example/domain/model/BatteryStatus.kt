package com.voltai.domain.model

data class BatteryStatus(
    val percentage: Int,
    val batteryLevel: Int = percentage,
    val level: Int = percentage, // Add level for compatibility
    val status: String,
    val chargingStatus: String = status,
    val isCharging: Boolean = false,
    val voltage: Float,
    val temperature: Float,
    val current: Float = 0f,
    val currentNow: Int = (current * 1000).toInt(), // Convert to microamps
    val chargingType: String = "",
    val health: Int = 100,
    val technology: String = "Li-ion",
    val capacity: Int = 0,
    val cycleCount: Int = 0
)
