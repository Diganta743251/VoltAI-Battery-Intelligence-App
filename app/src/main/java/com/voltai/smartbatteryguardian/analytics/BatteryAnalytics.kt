package com.voltai.smartbatteryguardian.analytics

import android.content.Context
import androidx.compose.runtime.*
import com.voltai.smartbatteryguardian.database.BatteryDatabase
import com.voltai.smartbatteryguardian.database.BatteryLogDao
import com.voltai.smartbatteryguardian.database.ChargingSessionDao
import com.voltai.smartbatteryguardian.database.AppUsageDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

data class BatteryHistoryPoint(
    val timestamp: Long,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val temperature: Float,
    val screenOn: Boolean,
    val usage: Float, // Power consumption in mW
)

data class DailyBatteryStats(
    val date: String,
    val totalUsage: Float,
    val screenOnTime: Int, // minutes
    val chargingTime: Int, // minutes
    val averageTemperature: Float,
    val batteryDrops: Int,
    val efficiencyScore: Float,
)

data class WeeklyAnalysis(
    val week: String,
    val averageScreenTime: Int,
    val averageChargingCycles: Float,
    val powerEfficiencyTrend: Float, // -1 to 1, negative means getting worse
    val healthTrend: Float,
    val topPowerConsumers: List<Pair<String, Float>>,
)

data class BatteryInsight(
    val title: String,
    val description: String,
    val category: InsightCategory,
    val severity: InsightSeverity,
    val improvement: String?,
    val confidence: Float,
)

enum class InsightCategory {
    USAGE_PATTERN,
    CHARGING_BEHAVIOR,
    TEMPERATURE_MANAGEMENT,
    HEALTH_DEGRADATION,
    PERFORMANCE_OPTIMIZATION,
}

enum class InsightSeverity {
    INFO,
    WARNING,
    CRITICAL,
}

class BatteryAnalyticsEngine(private val context: Context) {
    private val database = BatteryDatabase.getInstance(context)
    private val batteryLogDao: BatteryLogDao = database.batteryLogDao()
    private val chargingSessionDao: ChargingSessionDao = database.chargingSessionDao()
    private val appUsageDao: AppUsageDao = database.appUsageDao()

    fun generateDailyStats(days: Int = 7): List<DailyBatteryStats> = runBlocking {
        val stats = mutableListOf<DailyBatteryStats>()
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        for (dayIndex in 0 until days) {
            val dayStart = now - (dayIndex + 1) * 24 * 60 * 60 * 1000L
            val dayEnd = now - dayIndex * 24 * 60 * 60 * 1000L

            val logs = batteryLogDao.getLogsBetween(dayStart, dayEnd)
            val sessions = chargingSessionDao.getCompletedSessions(100)
                .filter { it.startTime in dayStart..dayEnd }

            if (logs.isEmpty()) {
                // No data for this day - show zero stats
                stats.add(
                    DailyBatteryStats(
                        date = dateFormat.format(Date(dayStart)),
                        totalUsage = 0f,
                        screenOnTime = 0,
                        chargingTime = 0,
                        averageTemperature = 0f,
                        batteryDrops = 0,
                        efficiencyScore = 0f,
                    )
                )
                continue
            }

            // Calculate actual total battery usage
            val firstLevel = logs.firstOrNull()?.batteryLevel ?: 100
            val lastLevel = logs.lastOrNull()?.batteryLevel ?: 100
            val totalDrop = (firstLevel - lastLevel).coerceAtLeast(0)

            // Calculate charging time from actual sessions
            val chargingTimeMillis = sessions.sumOf { (it.endTime ?: it.startTime) - it.startTime }
            val chargingMinutes = (chargingTimeMillis / (1000 * 60)).toInt()

            // Calculate average temperature from logs
            val avgTemp = logs.map { it.temperature }.average().toFloat()

            // Count battery drops (discharge events)
            var batteryDropCount = 0
            logs.windowed(2).forEach { (prev, current) ->
                if (!prev.isCharging && !current.isCharging && prev.batteryLevel > current.batteryLevel) {
                    batteryDropCount++
                }
            }

            // Efficiency score based on temperature and charging behavior
            val tempScore = when {
                avgTemp > 40f -> 60f
                avgTemp > 35f -> 75f
                avgTemp > 30f -> 85f
                else -> 95f
            }
            val healthScore = logs.lastOrNull()?.healthPercentage?.toFloat() ?: 100f
            val efficiencyScore = (tempScore + healthScore) / 2f

            stats.add(
                DailyBatteryStats(
                    date = dateFormat.format(Date(dayStart)),
                    totalUsage = totalDrop.toFloat(),
                    screenOnTime = 0, // Would need screen-on tracking
                    chargingTime = chargingMinutes,
                    averageTemperature = avgTemp,
                    batteryDrops = batteryDropCount,
                    efficiencyScore = efficiencyScore,
                )
            )
        }

        return@runBlocking stats.reversed() // Return chronological order
    }

    fun generateWeeklyAnalysis(): List<WeeklyAnalysis> = runBlocking {
        val now = System.currentTimeMillis()
        val thisWeekStart = now - 7 * 24 * 60 * 60 * 1000L
        val lastWeekStart = now - 14 * 24 * 60 * 60 * 1000L

        val thisWeekLogs = batteryLogDao.getLogsBetween(thisWeekStart, now)
        val lastWeekLogs = batteryLogDao.getLogsBetween(lastWeekStart, thisWeekStart)

        val thisWeekSessions = chargingSessionDao.getCompletedSessions(100)
            .filter { it.startTime >= thisWeekStart }
        val lastWeekSessions = chargingSessionDao.getCompletedSessions(100)
            .filter { it.startTime in lastWeekStart until thisWeekStart }

        fun analyzeWeek(
            week: String,
            logs: List<com.voltai.smartbatteryguardian.database.BatteryLogEntity>,
            sessions: List<com.voltai.smartbatteryguardian.database.ChargingSessionEntity>
        ): WeeklyAnalysis {
            val chargingCycles = sessions.size / 7f

            // Calculate health trend from logs
            val healthValues = logs.map { it.healthPercentage }
            val healthTrend = if (healthValues.size > 1) {
                (healthValues.last() - healthValues.first()) / 100f
            } else {
                0f
            }

            // Estimate power efficiency trend based on temperature and health
            val avgTemp = logs.map { it.temperature }.average().toFloat()
            val efficiencyTrend = when {
                avgTemp < 30f && healthTrend >= 0 -> 0.1f
                avgTemp < 35f -> 0.0f
                avgTemp < 40f -> -0.05f
                else -> -0.15f
            }

            // Estimate top power consumers (typical distribution)
            val topPowerConsumers = listOf(
                "Screen" to 30f,
                "Apps" to 25f,
                "System" to 15f,
                "Network" to 12f,
                "GPS" to 8f,
            )

            return WeeklyAnalysis(
                week = week,
                averageScreenTime = 0, // Would need screen-on tracking
                averageChargingCycles = chargingCycles,
                powerEfficiencyTrend = efficiencyTrend,
                healthTrend = healthTrend,
                topPowerConsumers = topPowerConsumers,
            )
        }

        return@runBlocking listOf(
            analyzeWeek("This Week", thisWeekLogs, thisWeekSessions),
            analyzeWeek("Last Week", lastWeekLogs, lastWeekSessions),
        )
    }

    fun generateBatteryInsights(): List<BatteryInsight> = runBlocking {
        val insights = mutableListOf<BatteryInsight>()
        val now = System.currentTimeMillis()
        val weekAgo = now - 7 * 24 * 60 * 60 * 1000L
        val monthAgo = now - 30 * 24 * 60 * 60 * 1000L

        val recentLogs = batteryLogDao.getLogsBetween(weekAgo, now)
        val monthLogs = batteryLogDao.getLogsBetween(monthAgo, now)
        val sessions = chargingSessionDao.getCompletedSessions(50)

        if (recentLogs.isEmpty()) {
            // No data available
            return@runBlocking emptyList()
        }

        // Temperature Management - analyze actual temperature data
        val highTempCount = recentLogs.count { it.temperature > 40f }
        if (highTempCount > 0) {
            val maxTemp = recentLogs.maxOfOrNull { it.temperature } ?: 0f
            insights.add(
                BatteryInsight(
                    title = "Temperature Alert",
                    description = "Battery temperature exceeded 40°C on $highTempCount occasion(s) this week. Peak temperature was ${maxTemp.toInt()}°C.",
                    category = InsightCategory.TEMPERATURE_MANAGEMENT,
                    severity = if (maxTemp > 45f) InsightSeverity.CRITICAL else InsightSeverity.WARNING,
                    improvement = "Reduce fast charging frequency and take cooling breaks during intensive tasks.",
                    confidence = 0.95f,
                ),
            )
        }

        // Health Degradation - calculate actual health trend
        if (monthLogs.size > 10) {
            val oldHealth = monthLogs.take(5).map { it.healthPercentage }.average()
            val newHealth = monthLogs.takeLast(5).map { it.healthPercentage }.average()
            val healthChange = newHealth - oldHealth

            if (abs(healthChange) > 1) {
                insights.add(
                    BatteryInsight(
                        title = "Battery Health Trend",
                        description = "Battery health has ${if (healthChange < 0) "decreased" else "improved"} by ${abs(healthChange).toInt()}% over the last month.",
                        category = InsightCategory.HEALTH_DEGRADATION,
                        severity = if (healthChange < -3) InsightSeverity.WARNING else InsightSeverity.INFO,
                        improvement = "Avoid deep discharges below 20% and limit charging above 85% when possible.",
                        confidence = 0.85f,
                    ),
                )
            }
        }

        // Charging Behavior - analyze actual charging patterns
        val nightSessions = sessions.count { session ->
            val hour = Calendar.getInstance().apply { timeInMillis = session.startTime }.get(Calendar.HOUR_OF_DAY)
            hour >= 22 || hour <= 6
        }
        val nightChargingRate = if (sessions.isNotEmpty()) nightSessions.toFloat() / sessions.size else 0f

        if (nightChargingRate > 0.7f) {
            insights.add(
                BatteryInsight(
                    title = "Overnight Charging Pattern",
                    description = "You charge overnight in ${(nightChargingRate * 100).toInt()}% of sessions. While convenient, this can impact long-term battery health.",
                    category = InsightCategory.CHARGING_BEHAVIOR,
                    severity = InsightSeverity.WARNING,
                    improvement = "Use adaptive charging or unplug at 85% to preserve battery longevity.",
                    confidence = 0.90f,
                ),
            )
        }

        // Overcharging detection
        val overchargeSessions = sessions.count { (it.endLevel ?: 0) > 95 }
        if (overchargeSessions > sessions.size / 2) {
            insights.add(
                BatteryInsight(
                    title = "Frequent Overcharging",
                    description = "${overchargeSessions} of ${sessions.size} recent charging sessions exceeded 95%. This can accelerate battery degradation.",
                    category = InsightCategory.CHARGING_BEHAVIOR,
                    severity = InsightSeverity.WARNING,
                    improvement = "Set a charging limit to 85% or use smart charging features.",
                    confidence = 0.92f,
                ),
            )
        }

        // Deep discharge detection
        val deepDischargeCount = recentLogs.count { it.batteryLevel < 15 }
        if (deepDischargeCount > 3) {
            insights.add(
                BatteryInsight(
                    title = "Deep Discharge Warning",
                    description = "Battery dropped below 15% on $deepDischargeCount occasion(s) this week. Deep discharges can reduce battery lifespan.",
                    category = InsightCategory.USAGE_PATTERN,
                    severity = InsightSeverity.WARNING,
                    improvement = "Try to charge when battery reaches 20-30% to maximize longevity.",
                    confidence = 0.88f,
                ),
            )
        }

        return@runBlocking insights
    }

    fun getChargingPatternAnalysis(): ChargingAnalysis = runBlocking {
        val sessions = chargingSessionDao.getCompletedSessions(30)

        if (sessions.isEmpty()) {
            return@runBlocking ChargingAnalysis(
                averageChargingSpeed = 0,
                optimalChargingWindows = listOf("22:00-23:30", "06:00-07:30"),
                chargingEfficiency = 0f,
                heatGenerationRisk = 0f,
                recommendedChargingLimit = 85,
            )
        }

        // Calculate average charging speed from actual sessions
        val avgSpeed = sessions.mapNotNull { session ->
            val duration = ((session.endTime ?: 0L) - session.startTime) / (1000.0 * 60 * 60) // hours
            val chargeGain = (session.endLevel ?: 0) - session.startLevel
            if (duration > 0 && chargeGain > 0) {
                // Estimate watts (assuming ~4000mAh battery at 3.7V nominal)
                val mah = (chargeGain / 100.0) * 4000
                val wattHours = (mah * 3.7) / 1000.0
                (wattHours / duration).toInt()
            } else null
        }.average().toInt()

        // Calculate efficiency based on temperature during charging
        val avgChargingTemp = sessions.map { it.averageTemperature }.average().toFloat()
        val efficiency = when {
            avgChargingTemp < 30f -> 92f
            avgChargingTemp < 35f -> 85f
            avgChargingTemp < 40f -> 75f
            else -> 65f
        }

        // Heat risk based on average charging temperature
        val heatRisk = when {
            avgChargingTemp < 30f -> 0.2f
            avgChargingTemp < 35f -> 0.4f
            avgChargingTemp < 40f -> 0.6f
            else -> 0.8f
        }

        // Recommend charging limit based on observed patterns
        val avgEndLevel = sessions.mapNotNull { it.endLevel }.average().toInt()
        val recommendedLimit = when {
            avgEndLevel > 95 -> 85 // User overcharges, recommend lower limit
            avgEndLevel > 90 -> 88
            else -> 90
        }

        return@runBlocking ChargingAnalysis(
            averageChargingSpeed = avgSpeed,
            optimalChargingWindows = listOf("22:00-23:30", "06:00-07:30"),
            chargingEfficiency = efficiency,
            heatGenerationRisk = heatRisk,
            recommendedChargingLimit = recommendedLimit,
        )
    }

    fun getPowerConsumptionBreakdown(): List<PowerConsumption> = runBlocking {
        val appUsages = appUsageDao.getTopApps(10)
        
        if (appUsages.isEmpty()) {
            // Return typical distribution when no app usage data
            return@runBlocking listOf(
                PowerConsumption("Display", 32f, "Screen brightness, refresh rate"),
                PowerConsumption("Apps", 25f, "App processing, background tasks"),
                PowerConsumption("System", 15f, "Android system, services"),
                PowerConsumption("Network", 12f, "WiFi, Mobile data, Bluetooth"),
                PowerConsumption("GPS", 8f, "Location services, navigation"),
                PowerConsumption("Other", 8f, "Camera, audio, sensors, misc"),
            )
        }

        // Calculate total battery usage
        val totalUsage = appUsages.sumOf { it.batteryUsage }
        
        if (totalUsage < 0.01) {
            // Not enough data
            return@runBlocking listOf(
                PowerConsumption("Display", 30f, "Screen brightness, refresh rate"),
                PowerConsumption("Apps", 28f, "App processing, background tasks"),
                PowerConsumption("System", 18f, "Android system, services"),
                PowerConsumption("Network", 10f, "WiFi, Mobile data, Bluetooth"),
                PowerConsumption("GPS", 7f, "Location services, navigation"),
                PowerConsumption("Other", 7f, "Camera, audio, sensors, misc"),
            )
        }

        // Build consumption breakdown from actual app usage
        val breakdown = mutableListOf<PowerConsumption>()
        
        // Display typically accounts for largest share
        breakdown.add(PowerConsumption("Display", 30f, "Screen brightness, refresh rate"))
        
        // Add top apps
        appUsages.take(5).forEach { app ->
            val percentage = ((app.batteryUsage / totalUsage) * 40f).toFloat() // Scale to 40% of total
            if (percentage > 1f) {
                breakdown.add(PowerConsumption(app.appName, percentage, "App usage"))
            }
        }
        
        // Add system categories
        breakdown.add(PowerConsumption("System", 12f, "Android system, services"))
        breakdown.add(PowerConsumption("Network", 8f, "WiFi, Mobile data, Bluetooth"))
        
        // Normalize to 100%
        val currentTotal = breakdown.sumOf { it.percentage.toDouble() }
        if (currentTotal > 0) {
            val normalized = breakdown.map {
                it.copy(percentage = (it.percentage / currentTotal.toFloat()) * 100f)
            }
            return@runBlocking normalized
        }
        
        return@runBlocking breakdown
    }

    fun getOptimizationRecommendations(): List<OptimizationTip> {
        return listOf(
            OptimizationTip(
                "Adaptive Brightness",
                "Enable adaptive brightness to save 10-15% battery",
                "High",
                "15%",
            ),
            OptimizationTip(
                "Background App Refresh",
                "Disable for unused apps to save 8-12% daily",
                "Medium",
                "10%",
            ),
            OptimizationTip(
                "Screen Timeout",
                "Reduce screen timeout to 30 seconds",
                "Medium",
                "5%",
            ),
            OptimizationTip(
                "Location Accuracy",
                "Use battery-optimized location mode",
                "High",
                "12%",
            ),
            OptimizationTip(
                "Push Notifications",
                "Reduce unnecessary notification syncing",
                "Low",
                "3%",
            ),
        )
    }
}

data class ChargingAnalysis(
    val averageChargingSpeed: Int, // watts
    val optimalChargingWindows: List<String>,
    val chargingEfficiency: Float, // percentage
    val heatGenerationRisk: Float, // 0-1 scale
    val recommendedChargingLimit: Int, // percentage
)

data class PowerConsumption(
    val component: String,
    val percentage: Float,
    val description: String,
)

data class OptimizationTip(
    val title: String,
    val description: String,
    val priority: String,
    val estimatedSavings: String,
)

@Composable
fun rememberBatteryAnalytics(context: Context): BatteryAnalyticsEngine {
    return remember { BatteryAnalyticsEngine(context) }
}
