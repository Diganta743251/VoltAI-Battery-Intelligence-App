package com.voltai.smartbatteryguardian.screens.power

import androidx.compose.ui.graphics.Color

data class PowerProfile(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val color: Color,
    val batterySavingsPercent: Int,
    val performanceImpact: PerformanceImpact,
    val features: List<PowerFeature>,
    val isActive: Boolean,
    val isCustom: Boolean,
    val aiOptimized: Boolean,
    val estimatedExtraHours: Double,
)

data class PowerFeature(
    val name: String,
    val description: String,
    val enabled: Boolean,
    val impact: String,
    val category: FeatureCategory,
)

enum class PerformanceImpact { NONE, LOW, MEDIUM, HIGH }

enum class FeatureCategory { DISPLAY, CPU, CONNECTIVITY, BACKGROUND, THERMAL }

data class ThermalData(
    val currentTemp: Double,
    val maxTemp: Double,
    val thermalState: ThermalState,
    val coolingActive: Boolean,
    val temperatureHistory: List<TempPoint>,
    val hotspots: List<ComponentTemp>,
)

data class TempPoint(
    val timestamp: Long,
    val temperature: Double,
    val cpuUsage: Double,
)

data class ComponentTemp(
    val component: String,
    val temperature: Double,
    val status: String,
    val icon: String,
)

enum class ThermalState { COOL, NORMAL, WARM, HOT, CRITICAL, WARNING }

data class AdaptiveOptimization(
    val isEnabled: Boolean,
    val learningProgress: Double,
    val optimizationScore: Int,
    val adaptations: List<Adaptation>,
    val nextOptimization: String,
    val dataPoints: Int,
)

data class Adaptation(
    val type: String,
    val description: String,
    val impact: String,
    val confidence: Double,
    val timestamp: Long,
)

data class PowerInsight(
    val title: String,
    val description: String,
    val recommendation: String,
    val priority: InsightPriority,
    val potentialSaving: String,
    val icon: String,
    val aiGenerated: Boolean,
)

enum class InsightPriority { CRITICAL, HIGH, MEDIUM, LOW, OPTIMIZATION }
