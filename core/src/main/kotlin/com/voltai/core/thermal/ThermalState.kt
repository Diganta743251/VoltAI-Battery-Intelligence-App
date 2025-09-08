package com.voltai.core.thermal

import java.time.LocalDateTime

data class ThermalState(
    val temperature: Float = 0.0f,
    val ambientTemperature: Float = 0.0f,
    val humidity: Float = 0.0f,
    val status: ThermalStatus = ThermalStatus.NORMAL,
    val recommendations: List<String> = emptyList(),
    val isThrottling: Boolean = false,
    val coolingActive: Boolean = false,
    val lastUpdate: LocalDateTime = LocalDateTime.now(),
    val isError: Boolean = false,
    val errorMessage: String? = null
)

data class ThermalReading(
    val timestamp: Long,
    val deviceTemperature: Float,
    val ambientTemperature: Float,
    val humidity: Float,
    val thermalStatus: ThermalStatus,
    val cpuUsage: Float,
    val batteryTemperature: Float
)

data class ThermalAlert(
    val id: String,
    val timestamp: Long,
    val severity: AlertSeverity,
    val temperature: Float,
    val message: String,
    val recommendations: List<String>
)

enum class ThermalStatus {
    NORMAL,
    WARNING,
    CRITICAL,
    EMERGENCY
}

enum class AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
