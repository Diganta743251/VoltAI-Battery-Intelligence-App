package com.voltai.smartbatteryguardian.data.entities

import kotlinx.serialization.Serializable
import java.util.*

/**
 * Data entities for export/import functionality
 */

@Serializable
data class BatteryReading(
    val timestamp: Long,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val voltage: Float,
    val current: Float,
    val temperature: Float,
    val health: String,
    val technology: String,
    val chargingSpeed: String? = null,
    val chargingType: String? = null,
    val plugType: String? = null,
    val powerSource: String? = null
)

@Serializable
data class ChargingSession(
    val startTime: Long,
    val endTime: Long? = null,
    val startLevel: Int,
    val endLevel: Int? = null,
    val duration: Long,
    val chargerType: String,
    val maxPower: Float,
    val avgPower: Float,
    val efficiency: Float,
    val energyConsumed: Float
)

@Serializable
data class AppUsageStats(
    val timestamp: Long,
    val packageName: String,
    val appName: String,
    val category: String,
    val batteryUsage: Float,
    val foregroundTime: Long,
    val backgroundTime: Long,
    val cpuUsage: Float,
    val networkUsage: Long
)

@Serializable
data class BatteryHealthRecord(
    val timestamp: Long,
    val healthPercentage: Float,
    val capacityRetained: Float,
    val cycleCount: Int,
    val temperatureAvg: Float,
    val voltageAvg: Float,
    val degradationRate: Float,
    val predictionScore: Float
)

@Serializable
data class AIPrediction(
    val timestamp: Long,
    val predictionType: String,
    val targetVariable: String,
    val predictedValue: Float,
    val confidence: Float,
    val timeHorizon: Long,
    val modelVersion: String,
    val accuracy: Float? = null
)

@Serializable
data class SystemMetrics(
    val timestamp: Long,
    val cpuUsage: Float,
    val memoryUsage: Float,
    val storageUsage: Float,
    val networkUsage: Long,
    val thermalState: String,
    val batteryTemperature: Float,
    val performanceScore: Float,
    val stabilityScore: Float,
    val efficiencyScore: Float
)

@Serializable
data class UserPreferences(
    val theme: String = "light",
    val notifications: Boolean = true,
    val autoOptimization: Boolean = false,
    val dataCollection: Boolean = true,
    val premiumFeatures: Boolean = false,
    val language: String = "en",
    val units: String = "metric"
)

@Serializable
data class PowerProfile(
    val id: String,
    val name: String,
    val description: String,
    val settings: Map<String, String>,
    val isActive: Boolean = false,
    val createdAt: Long,
    val modifiedAt: Long
)
