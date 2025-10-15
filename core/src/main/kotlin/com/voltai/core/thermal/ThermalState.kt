package com.voltai.core.thermal

import java.time.LocalDateTime

data class ThermalState(
    val temperature: Float = 0.0f,
    val ambientTemperature: Float = 0.0f,
    val humidity: Float = 0.0f,
    val status: ThermalStatus = ThermalStatus.NORMAL,
    val recommendations: List<CoolingRecommendation> = emptyList(),
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
    val type: ThermalAlertType,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val temperature: Float,
    val timestamp: LocalDateTime,
    val recommendations: List<CoolingRecommendation>
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
    WARNING,
    CRITICAL,
    EMERGENCY
}
