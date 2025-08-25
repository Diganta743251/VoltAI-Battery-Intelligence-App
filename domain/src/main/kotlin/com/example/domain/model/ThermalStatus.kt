package com.voltai.domain.model

data class ThermalStatus(
    val cpuTemperature: Float,
    val gpuTemperature: Float,
    val batteryTemperature: Float,
    val skinTemperature: Float,
    val thermalStatus: Int,
    val thermalState: String = when {
        thermalStatus >= 4 -> "Critical"
        thermalStatus >= 3 -> "Severe"
        thermalStatus >= 2 -> "Moderate"
        thermalStatus >= 1 -> "Light"
        else -> "None"
    },
    val coolingRecommendation: String = when {
        batteryTemperature > 45f -> "Stop charging and let device cool"
        batteryTemperature > 40f -> "Reduce usage and avoid direct sunlight"
        batteryTemperature > 35f -> "Monitor temperature closely"
        else -> "Temperature is normal"
    }
)
