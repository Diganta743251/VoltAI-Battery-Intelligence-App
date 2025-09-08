package com.voltai.smartbatteryguardian.models

import androidx.compose.ui.graphics.Color
import android.graphics.drawable.Drawable

/**
 * Common data models for premium features
 */

// App Usage Models
data class AppUsageData(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val batteryUsage: Double,
    val totalTimeInForeground: Long,
    val lastTimeUsed: Long,
    val usageToday: Long,
    val usageYesterday: Long,
    val usageThisWeek: Long,
    val openCount: Int,
    val avgSessionTime: Long,
    val peakUsageHour: Int,
    val categoryColor: Color
)

// Optimization Models
enum class OptimizationCategory {
    BATTERY, CPU, MEMORY, NETWORK
}

enum class OptimizationImpact {
    HIGH, MEDIUM, LOW
}

data class OptimizationSuggestion(
    val title: String,
    val description: String,
    val category: OptimizationCategory,
    val impact: OptimizationImpact,
    val estimatedSavings: String
)

// Charging Models
enum class RecommendationPriority {
    URGENT, HIGH, MEDIUM, LOW, INFO
}

data class ChargingProfile(
    val name: String,
    val sessions: Int,
    val avgTime: Long,
    val avgSpeed: Double,
    val efficiency: Double
)

// Battery Health Models
data class BatteryHealthData(
    val healthPercentage: Int,
    val degradationRate: Double,
    val cycleCount: Int,
    val voltageData: List<Double>,
    val temperatureData: List<Double>,
    val capacity: Double,
    val designCapacity: Double
)

// Charging Analytics Models
data class ChargingSession(
    val startTime: Long,
    val endTime: Long,
    val startPercentage: Int,
    val endPercentage: Int,
    val chargingSpeed: Double,
    val temperature: Double,
    val efficiency: Double
)

// Smart Charging Models
data class SmartChargingRecommendation(
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val estimatedSavings: String,
    val nextOptimalTime: Long?
)

// Diagnostics Models
data class DiagnosticResult(
    val testName: String,
    val status: DiagnosticStatus,
    val details: String,
    val severity: DiagnosticSeverity
)

enum class DiagnosticStatus {
    PASSED, FAILED, WARNING, RUNNING
}

enum class DiagnosticSeverity {
    CRITICAL, HIGH, MEDIUM, LOW, INFO
}

// Real-time Monitoring Models
data class RealTimeMetrics(
    val timestamp: Long,
    val batteryLevel: Int,
    val voltage: Double,
    val current: Double,
    val temperature: Double,
    val power: Double,
    val isCharging: Boolean
)

// Performance Benchmark Models
data class BenchmarkResult(
    val testName: String,
    val score: Double,
    val unit: String,
    val category: BenchmarkCategory,
    val timestamp: Long
)

enum class BenchmarkCategory {
    CPU, GPU, MEMORY, STORAGE, BATTERY, OVERALL
}
