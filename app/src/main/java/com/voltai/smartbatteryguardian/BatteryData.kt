package com.voltai.smartbatteryguardian

import androidx.annotation.Keep

/**
 * Comprehensive battery data model containing all battery-related information
 */
@Keep
data class BatteryData(
    // Basic Battery Info
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val chargingStatus: String = "Not Charging",
    val remainingTime: String = "Unknown",
    val batteryUsed: String = "0%",
    
    // Technical Data
    val temperature: Float = 25.0f,
    val voltage: Double = 3.7,
    val current: Int = 0,
    val healthPercentage: Int = 100,
    val capacity: String = "Unknown",
    val cycleCount: Int = 0,
    
    // Screen Time Data
    val screenOnTime: String = "0h 0m",
    val screenOffTime: String = "24h 0m",
    
    // Advanced Metrics
    val powerConsumption: Double = 0.0,
    val chargingSpeed: String = "Normal",
    val estimatedLife: String = "Unknown",
    val powerSource: String = "Battery",
    
    // Historical Data
    val dailyUsagePattern: List<Float> = emptyList(),
    val weeklyStats: WeeklyStats = WeeklyStats(),
    val monthlyTrends: MonthlyTrends = MonthlyTrends(),
    
    // AI Insights
    val batteryScore: Int = 85,
    val recommendations: List<String> = emptyList(),
    val optimizationTips: List<String> = emptyList(),
    
    // System Info
    val deviceModel: String = "Unknown",
    val androidVersion: String = "Unknown",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Keep
data class WeeklyStats(
    val averageBatteryLife: String = "Unknown",
    val averageChargingCycles: Float = 0.0f,
    val powerEfficiencyTrend: Float = 0.0f,
    val totalScreenTime: String = "0h 0m"
)

@Keep
data class MonthlyTrends(
    val batteryHealthTrend: Float = 0.0f,
    val usagePatternChange: Float = 0.0f,
    val chargingHabitsScore: Int = 85
)

@Keep
data class DailyStats(
    val date: String = "",
    val batteryUsage: Float = 0.0f,
    val screenTime: Float = 0.0f,
    val chargingCycles: Int = 0,
    val averageTemperature: Float = 25.0f
)
