package com.voltai.smartbatteryguardian.data.database.entities

import androidx.room.*
import java.util.Date

// Future-ready battery data entities with comprehensive tracking
@Entity(tableName = "battery_readings")
data class BatteryReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryLevel: Int,
    val isCharging: Boolean,
    val chargingType: String?,
    val voltage: Double,
    val current: Double,
    val temperature: Double,
    val capacity: Double,
    val health: String,
    val technology: String,
    val plugType: String?,
    val powerSource: String?,
    val chargingSpeed: Double,
    val estimatedTimeRemaining: Long?,
    val cycleCount: Int?,
    val wearLevel: Double?
)

@Entity(tableName = "charging_sessions")
data class ChargingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val startLevel: Int,
    val endLevel: Int?,
    val chargerType: String,
    val maxPower: Double,
    val avgPower: Double,
    val maxTemperature: Double,
    val avgTemperature: Double,
    val efficiency: Double,
    val energyConsumed: Double,
    val location: String?,
    val duration: Long,
    val interruptionCount: Int = 0,
    val peakChargingSpeed: Double,
    val thermalThrottling: Boolean = false
)

@Entity(tableName = "app_usage_stats")
data class AppUsageStats(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String,
    val appName: String,
    val category: String,
    val foregroundTime: Long,
    val backgroundTime: Long,
    val batteryUsage: Double,
    val cpuUsage: Double,
    val memoryUsage: Long,
    val networkUsage: Long,
    val wakelocksHeld: Int,
    val notificationCount: Int,
    val launchCount: Int,
    val crashCount: Int = 0,
    val dataUsage: Long = 0,
    val gpsUsage: Long = 0
)

@Entity(tableName = "battery_health_history")
data class BatteryHealthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val healthPercentage: Double,
    val capacityRetained: Double,
    val cycleCount: Int,
    val degradationRate: Double,
    val impedance: Double?,
    val voltageStability: Double?,
    val temperatureStress: Double?,
    val chargingStress: Double?,
    val ageInDays: Int,
    val predictedLifespan: Int?,
    val recommendationScore: Double,
    val calibrationNeeded: Boolean = false,
    val criticalIssues: String?
)

@Entity(tableName = "power_profiles")
data class PowerProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val isActive: Boolean = false,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val cpuGovernor: String?,
    val maxCpuFreq: Int?,
    val screenBrightness: Int?,
    val screenTimeout: Int?,
    val wifiEnabled: Boolean = true,
    val bluetoothEnabled: Boolean = true,
    val locationEnabled: Boolean = true,
    val syncEnabled: Boolean = true,
    val backgroundAppLimits: String?,
    val notificationFilters: String?,
    val thermalThreshold: Double?,
    val batteryThreshold: Int?,
    val automationRules: String?,
    val estimatedSavings: Double?
)

@Entity(tableName = "optimization_logs")
data class OptimizationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val optimizationType: String,
    val action: String,
    val targetApp: String?,
    val beforeValue: String?,
    val afterValue: String?,
    val impactScore: Double,
    val success: Boolean,
    val errorMessage: String?,
    val userTriggered: Boolean = false,
    val aiConfidence: Double?,
    val batteryImpact: Double?,
    val performanceImpact: Double?
)

@Entity(tableName = "system_metrics")
data class SystemMetrics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val cpuUsage: Double,
    val memoryUsage: Double,
    val storageUsage: Double,
    val networkUsage: Double,
    val thermalState: String,
    val batteryTemperature: Double,
    val systemTemperature: Double?,
    val performanceScore: Int,
    val stabilityScore: Int,
    val efficiencyScore: Int,
    val backgroundProcesses: Int,
    val foregroundApps: Int,
    val wakelocksActive: Int,
    val networkConnections: Int,
    val locationRequests: Int
)

@Entity(tableName = "benchmark_results")
data class BenchmarkResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val benchmarkType: String,
    val testName: String,
    val score: Int,
    val maxPossibleScore: Int,
    val duration: Long,
    val deviceInfo: String,
    val androidVersion: String,
    val appVersion: String,
    val batteryLevelStart: Int,
    val batteryLevelEnd: Int,
    val temperatureStart: Double,
    val temperatureEnd: Double,
    val cpuScore: Int?,
    val memoryScore: Int?,
    val storageScore: Int?,
    val batteryScore: Int?,
    val thermalScore: Int?,
    val notes: String?
)

@Entity(tableName = "ai_predictions")
data class AIPrediction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val predictionType: String,
    val targetVariable: String,
    val predictedValue: Double,
    val confidence: Double,
    val timeHorizon: Long,
    val inputFeatures: String,
    val modelVersion: String,
    val accuracy: Double?,
    val actualValue: Double?,
    val errorRate: Double?,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val userId: String?
)

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey
    val id: Int = 1,
    val theme: String = "auto",
    val language: String = "en",
    val units: String = "metric",
    val notificationsEnabled: Boolean = true,
    val batteryAlertsEnabled: Boolean = true,
    val healthNotificationsEnabled: Boolean = true,
    val aiRecommendationsEnabled: Boolean = true,
    val backgroundMonitoringEnabled: Boolean = true,
    val dataCollectionEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true,
    val autoOptimizationEnabled: Boolean = false,
    val thermalProtectionEnabled: Boolean = true,
    val chargingAlertsEnabled: Boolean = false,
    val darkModeScheduled: Boolean = false,
    val premiumUnlocked: Boolean = false,
    val dataRetentionDays: Int = 30,
    val exportFormat: String = "csv",
    val backupEnabled: Boolean = false,
    val lastBackupTime: Long = 0
)

// Data classes for complex relationships
@Entity(
    tableName = "app_battery_impact",
    foreignKeys = [
        ForeignKey(
            entity = AppUsageStats::class,
            parentColumns = ["id"],
            childColumns = ["appUsageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("appUsageId")]
)
data class AppBatteryImpact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appUsageId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryDrainRate: Double,
    val screenOnContribution: Double,
    val backgroundContribution: Double,
    val networkContribution: Double,
    val locationContribution: Double,
    val wakelockContribution: Double,
    val cpuContribution: Double,
    val totalImpactScore: Double,
    val efficiency: Double,
    val recommendation: String?
)

@Entity(tableName = "charging_analytics")
data class ChargingAnalytics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dailyChargingSessions: Int,
    val totalChargingTime: Long,
    val avgChargingSpeed: Double,
    val maxChargingSpeed: Double,
    val chargingEfficiency: Double,
    val batteryHealthImpact: Double,
    val thermalImpact: Double,
    val costEstimate: Double?,
    val carbonFootprint: Double?,
    val optimalChargingHours: String?,
    val chargingPatternScore: Double,
    val recommendations: String?
)

// Type converters for complex data types
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
