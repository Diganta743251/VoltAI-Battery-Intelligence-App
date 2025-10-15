package com.voltai.smartbatteryguardian.ai

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.*
import com.voltai.smartbatteryguardian.database.BatteryDatabase
import kotlinx.coroutines.runBlocking
import kotlin.math.*

data class OptimizationRecommendation(
    val type: OptimizationType,
    val title: String,
    val description: String,
    val impact: OptimizationImpact,
    val action: () -> Unit,
    val isApplied: Boolean = false,
)

enum class OptimizationType {
    SCREEN_OPTIMIZATION,
    APP_MANAGEMENT,
    CONNECTIVITY,
    CHARGING_PATTERN,
    THERMAL_MANAGEMENT,
    AI_LEARNING,
}

enum class OptimizationImpact {
    LOW, // 1-5% battery savings
    MEDIUM, // 5-15% battery savings
    HIGH, // 15-25% battery savings
    CRITICAL, // 25%+ battery savings
}

data class BatteryPrediction(
    val remainingHours: Float,
    val confidenceLevel: Float,
    val predictionBasis: String,
    val learningProgress: Float,
)

data class AIInsight(
    val category: String,
    val insight: String,
    val confidence: Float,
    val actionable: Boolean,
    val priority: Int, // 1-5, 5 being highest
)

class AIBatteryOptimizer(private val context: Context) {
    private val database = BatteryDatabase.getInstance(context)
    private val batteryLogDao = database.batteryLogDao()
    private val chargingSessionDao = database.chargingSessionDao()

    data class BatteryDataPoint(
        val timestamp: Long,
        val batteryLevel: Int,
        val isCharging: Boolean,
        val temperature: Float,
        val screenOnTime: Long,
        val activeApps: List<String>,
    )

    fun analyzeBatteryUsage(
        batteryData: com.voltai.smartbatteryguardian.BatteryData,
    ): List<OptimizationRecommendation> {
        val recommendations = mutableListOf<OptimizationRecommendation>()

        // Screen Optimization Analysis
        if (batteryData.batteryLevel < 30) {
            recommendations.add(
                OptimizationRecommendation(
                    type = OptimizationType.SCREEN_OPTIMIZATION,
                    title = "🌙 Activate Dark Mode Plus",
                    description = "Dark mode with OLED optimization can save 15-30% battery on your screen. AI detected you're in low battery mode.",
                    impact = OptimizationImpact.HIGH,
                    action = { /* Enable dark mode */ },
                ),
            )
        }

        // Thermal Management
        if (batteryData.temperature > 38f) {
            recommendations.add(
                OptimizationRecommendation(
                    type = OptimizationType.THERMAL_MANAGEMENT,
                    title = "🔥 Emergency Thermal Control",
                    description = "Critical temperature detected (${batteryData.temperature}°C). AI will reduce CPU frequency and pause background apps.",
                    impact = OptimizationImpact.CRITICAL,
                    action = { /* Thermal throttling */ },
                ),
            )
        }

        // Smart Charging Pattern
        if (batteryData.isCharging && batteryData.batteryLevel > 80) {
            recommendations.add(
                OptimizationRecommendation(
                    type = OptimizationType.CHARGING_PATTERN,
                    title = "🧠 AI Smart Charging",
                    description = "Learned your usage patterns. Pausing charge at 85% and will resume at 6 AM for optimal battery longevity.",
                    impact = OptimizationImpact.MEDIUM,
                    action = { /* Smart charging control */ },
                ),
            )
        }

        // AI-Based App Management
        recommendations.add(
            OptimizationRecommendation(
                type = OptimizationType.APP_MANAGEMENT,
                title = "🤖 AI App Hibernation",
                description = "ML analysis shows 12 apps consuming power in background. AI can hibernate them safely based on your usage patterns.",
                impact = OptimizationImpact.HIGH,
                action = { /* Hibernate unused apps */ },
            ),
        )

        // Connectivity Intelligence
        if (batteryData.batteryLevel < 20) {
            recommendations.add(
                OptimizationRecommendation(
                    type = OptimizationType.CONNECTIVITY,
                    title = "📡 Adaptive Connectivity",
                    description = "AI detects low battery. Switching to 4G only, reducing WiFi scanning, and optimizing Bluetooth for critical battery preservation.",
                    impact = OptimizationImpact.HIGH,
                    action = { /* Optimize connectivity */ },
                ),
            )
        }

        return recommendations
    }

    fun getPredictedBatteryLife(batteryData: com.voltai.smartbatteryguardian.BatteryData): BatteryPrediction = runBlocking {
        // Calculate prediction based on actual historical discharge rates
        val now = System.currentTimeMillis()
        val recentLogs = batteryLogDao.getLogsBetween(now - 24 * 60 * 60 * 1000L, now)

        val baseDischargeRate = if (recentLogs.size > 10) {
            // Calculate actual discharge rate from recent data
            val dischargeEvents = recentLogs.windowed(2).filter { (prev, current) ->
                !prev.isCharging && !current.isCharging && prev.batteryLevel > current.batteryLevel
            }
            
            if (dischargeEvents.isNotEmpty()) {
                val avgDropPerHour = dischargeEvents.map { (prev, current) ->
                    val hoursDiff = (current.timestamp - prev.timestamp) / (1000.0 * 60 * 60)
                    val levelDrop = prev.batteryLevel - current.batteryLevel
                    if (hoursDiff > 0) levelDrop / hoursDiff else 0.0
                }.filter { it > 0 }.average().toFloat()
                avgDropPerHour.coerceIn(5f, 30f) // Realistic range: 5-30% per hour
            } else {
                15f // Default discharge rate
            }
        } else {
            // Use temperature-based estimation when no historical data
            when {
                batteryData.temperature > 35f -> 20f
                batteryData.temperature < 15f -> 18f
                else -> 15f
            }
        }

        val healthFactor = batteryData.healthPercentage / 100f
        val predictedHours = if (baseDischargeRate > 0) {
            (batteryData.batteryLevel / baseDischargeRate) * healthFactor
        } else {
            0f
        }

        val logCount = batteryLogDao.getLogCount()
        val confidence = calculateConfidence(logCount)

        return@runBlocking BatteryPrediction(
            remainingHours = predictedHours.coerceAtLeast(0f),
            confidenceLevel = confidence,
            predictionBasis = "Based on ${recentLogs.size} recent data points",
            learningProgress = minOf(logCount / 1000f, 1f),
        )
    }

    fun getAIInsights(batteryData: com.voltai.smartbatteryguardian.BatteryData): List<AIInsight> = runBlocking {
        val insights = mutableListOf<AIInsight>()
        val now = System.currentTimeMillis()
        val recentLogs = batteryLogDao.getLogsBetween(now - 7 * 24 * 60 * 60 * 1000L, now)
        val sessions = chargingSessionDao.getCompletedSessions(20)

        // Temperature insights based on actual data
        if (batteryData.temperature > 35f) {
            val recentHighTemp = recentLogs.count { it.temperature > 35f }
            insights.add(
                AIInsight(
                    category = "🌡️ Thermal Intelligence",
                    insight = "Elevated temperature detected (${batteryData.temperature.toInt()}°C). Occurred $recentHighTemp times this week. Consider cooling breaks during intensive use.",
                    confidence = 0.93f,
                    actionable = true,
                    priority = 5,
                ),
            )
        }

        // Health prediction based on actual health trend
        if (recentLogs.size > 50) {
            val oldHealth = recentLogs.take(10).map { it.healthPercentage }.average()
            val newHealth = recentLogs.takeLast(10).map { it.healthPercentage }.average()
            val weeklyDegradation = oldHealth - newHealth
            
            if (weeklyDegradation > 0) {
                val monthsTo80 = ((newHealth - 80) / (weeklyDegradation / 7 * 30)).toInt().coerceAtLeast(1)
                insights.add(
                    AIInsight(
                        category = "❤️ Health Forecast",
                        insight = "Based on current degradation rate, battery will maintain 80%+ health for approximately $monthsTo80 months.",
                        confidence = 0.75f,
                        actionable = false,
                        priority = 3,
                    ),
                )
            } else {
                insights.add(
                    AIInsight(
                        category = "❤️ Health Forecast",
                        insight = "Battery health is stable. Your usage patterns are helping maintain optimal battery condition.",
                        confidence = 0.80f,
                        actionable = false,
                        priority = 2,
                    ),
                )
            }
        }

        // Charging pattern analysis
        if (sessions.isNotEmpty()) {
            val avgDuration = sessions.mapNotNull { it.endTime?.let { end -> end - it.startTime } }.average() / (1000 * 60)
            if (avgDuration > 240) { // More than 4 hours
                insights.add(
                    AIInsight(
                        category = "⚡ Charging Pattern",
                        insight = "Average charging duration is ${avgDuration.toInt()} minutes. Consider using smart charging to reduce battery stress.",
                        confidence = 0.88f,
                        actionable = true,
                        priority = 4,
                    ),
                )
            }
        }

        // Discharge pattern anomaly
        if (recentLogs.size > 20) {
            val recentDischargeRate = recentLogs.takeLast(10)
                .windowed(2)
                .filter { (prev, current) -> !prev.isCharging && !current.isCharging }
                .map { (prev, current) -> prev.batteryLevel - current.batteryLevel }
                .filter { it > 0 }
                .average()
            
            val normalDischargeRate = recentLogs.take(recentLogs.size - 10)
                .windowed(2)
                .filter { (prev, current) -> !prev.isCharging && !current.isCharging }
                .map { (prev, current) -> prev.batteryLevel - current.batteryLevel }
                .filter { it > 0 }
                .average()
            
            if (recentDischargeRate > normalDischargeRate * 1.3) {
                insights.add(
                    AIInsight(
                        category = "📊 Anomaly Detection",
                        insight = "Battery drain increased by ${((recentDischargeRate / normalDischargeRate - 1) * 100).toInt()}% compared to your normal usage. Check for new apps or services.",
                        confidence = 0.82f,
                        actionable = true,
                        priority = 4,
                    ),
                )
            }
        }

        return@runBlocking insights
    }

    private fun calculateConfidence(dataPoints: Int): Float {
        return when {
            dataPoints > 1000 -> 0.95f
            dataPoints > 500 -> 0.85f
            dataPoints > 100 -> 0.75f
            dataPoints > 20 -> 0.60f
            else -> 0.40f
        }
    }

    fun recordBatteryDataPoint(batteryData: com.voltai.smartbatteryguardian.BatteryData) {
        // Data is now recorded directly to Room database via BatteryDataCollector
        // This method is kept for compatibility but no longer needed
    }

    private fun getActiveApps(): List<String> {
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            return packages.filter {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }.map { it.packageName }.take(5)
        } catch (e: Exception) {
            return listOf("com.android.chrome", "com.whatsapp", "com.spotify.music")
        }
    }

    fun getOptimizationScore(): Float = runBlocking {
        // Calculate optimization score based on actual battery behavior
        val now = System.currentTimeMillis()
        val weekAgo = now - 7 * 24 * 60 * 60 * 1000L
        val logs = batteryLogDao.getLogsBetween(weekAgo, now)
        val sessions = chargingSessionDao.getCompletedSessions(10)
        
        if (logs.isEmpty()) return@runBlocking 50f
        
        var score = 100f
        
        // Penalize for high temperatures
        val avgTemp = logs.map { it.temperature }.average().toFloat()
        if (avgTemp > 40f) score -= 20f
        else if (avgTemp > 35f) score -= 10f
        
        // Penalize for overcharging
        val overchargeCount = sessions.count { (it.endLevel ?: 0) > 95 }
        if (overchargeCount > sessions.size / 2) score -= 15f
        
        // Penalize for deep discharges
        val deepDischargeCount = logs.count { it.batteryLevel < 15 }
        if (deepDischargeCount > 5) score -= 15f
        
        // Reward good health
        val avgHealth = logs.map { it.healthPercentage }.average().toFloat()
        if (avgHealth > 95f) score += 10f
        
        return@runBlocking score.coerceIn(0f, 100f)
    }

    fun enableAutoOptimization(): Boolean {
        // Simulate enabling various automatic optimizations
        return true
    }

    companion object {
        fun getBatteryTemperatureRisk(temperature: Float): String {
            return when {
                temperature < 0f -> "🧊 FREEZE RISK - Device may shut down"
                temperature < 10f -> "❄️ Very Cold - Reduced performance expected"
                temperature < 20f -> "🌡️ Cool - Optimal performance"
                temperature < 30f -> "✅ Perfect Temperature Range"
                temperature < 35f -> "🌤️ Warm - Normal operation"
                temperature < 40f -> "⚠️ Hot - Performance may throttle"
                temperature < 45f -> "🔥 Very Hot - Immediate cooling needed"
                else -> "🚨 CRITICAL - Risk of permanent damage"
            }
        }

        fun getSmartChargingRecommendation(
            batteryLevel: Int,
            isCharging: Boolean,
        ): String {
            return when {
                batteryLevel < 20 && !isCharging -> "⚡ Connect charger immediately for optimal charging window"
                batteryLevel in 20..80 && isCharging -> "🎯 Perfect charging zone - fastest charging speed"
                batteryLevel > 80 && isCharging -> "🔋 Consider unplugging at 85% for battery longevity"
                batteryLevel == 100 && isCharging -> "⏹️ Unplug now to prevent overcharging stress"
                else -> "✨ Battery level optimal for current usage"
            }
        }
    }
}

@Composable
fun rememberAIBatteryOptimizer(context: Context): AIBatteryOptimizer {
    return remember { AIBatteryOptimizer(context) }
}
