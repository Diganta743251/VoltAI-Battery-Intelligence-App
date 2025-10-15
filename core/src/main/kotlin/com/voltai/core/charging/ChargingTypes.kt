package com.voltai.core.charging

import java.time.LocalDateTime
import java.time.LocalTime

// Data classes for charging management
data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val isPluggedIn: Boolean,
    val temperature: Float,
    val voltage: Float,
    val health: Int,
    val status: String
)

data class ChargingSchedulerState(
    val isActive: Boolean = false,
    val currentStrategy: ChargingStrategy = ChargingStrategy.BALANCED,
    val schedule: ChargingSchedule? = null,
    val shouldCharge: Boolean = false,
    val shouldPause: Boolean = false,
    val nextScheduledTime: LocalDateTime? = null,
    val batteryLevel: Int = 0,
    val temperature: Float = 0.0f,
    val isPluggedIn: Boolean = false,
    val estimatedChargingTime: Long = 0L,
    val lastUpdate: LocalDateTime = LocalDateTime.now(),
    val isError: Boolean = false,
    val errorMessage: String? = null
)

data class ChargingSchedule(
    val windows: List<ChargingWindow>,
    val nextChargingWindow: ChargingWindow?,
    val strategy: ChargingStrategy,
    val createdAt: LocalDateTime
)

data class ChargingWindow(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val targetLevel: Int,
    val estimatedDuration: Long,
    val priority: ChargingPriority
)

data class ChargingTimeWindow(
    val id: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val priority: ChargingPriority,
    val enabled: Boolean = true,
    val name: String = ""
)

data class ChargingUserPreferences(
    val chargingMode: ChargingMode = ChargingMode.BALANCED,
    val maxChargeLevel: Int = 85,
    val minChargeLevel: Int = 20,
    val customTargetLevel: Int = 80,
    val emergencyChargeThreshold: Int = 10,
    val enableScheduledCharging: Boolean = false,
    val enableImmediateCharging: Boolean = true,
    val chargingTimeWindows: List<ChargingTimeWindow> = emptyList(),
    val enableThermalProtection: Boolean = true,
    val enableHealthOptimization: Boolean = true,
    val adaptiveChargingEnabled: Boolean = true,
    val overnightChargingLimit: Boolean = true,
    val weekendChargingSchedule: Boolean = false
)

data class ChargingRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val actionText: String,
    val createdAt: LocalDateTime
)

data class ChargingAnalysis(
    val schedulerState: ChargingSchedulerState,
    val recommendations: List<ChargingRecommendation>
)

// Enums
enum class ChargingStrategy {
    BALANCED,
    PERFORMANCE,
    LONGEVITY,
    EMERGENCY,
    HEALTH_PRESERVATION,
    ADAPTIVE_FAST,
    THERMAL_PROTECTION,
    CUSTOM
}

enum class ChargingMode {
    BALANCED,
    PERFORMANCE,
    LONGEVITY,
    CUSTOM
}

enum class ChargingPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

enum class RecommendationType {
    THERMAL_WARNING,
    HEALTH_OPTIMIZATION,
    CHARGING_PATTERN,
    FEATURE_SUGGESTION,
    EMERGENCY_ACTION
}

enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
