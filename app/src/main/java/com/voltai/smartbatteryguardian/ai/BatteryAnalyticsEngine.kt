package com.voltai.smartbatteryguardian.ai

import android.content.Context
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Advanced battery analytics engine with predictive capabilities
 */
@Singleton
class BatteryAnalyticsEngine
@Inject
constructor(
    private val context: Context,
    private val batteryLogDao: BatteryLogDao,
    private val chargingSessionDao: ChargingSessionDao,
    private val appUsageDao: AppUsageDao,
) {
    data class BatteryInsights(
        val batteryScore: Int,
        val healthTrend: HealthTrend,
        val chargingEfficiency: Double,
        val temperatureAnalysis: TemperatureAnalysis,
        val usagePatterns: UsagePatterns,
        val recommendations: List<Recommendation>,
        val predictions: BatteryPredictions,
    )

    data class HealthTrend(
        val currentHealth: Int,
        val weeklyChange: Double,
        val monthlyChange: Double,
        val trend: TrendDirection,
    )

    data class TemperatureAnalysis(
        val averageTemp: Float,
        val maxTemp: Float,
        val overheatingEvents: Int,
        val thermalRisk: RiskLevel,
    )

    data class UsagePatterns(
        val dailyPattern: List<Float>,
        val chargingHabits: ChargingHabits,
        val heavyUsagePeriods: List<UsagePeriod>,
    )

    data class ChargingHabits(
        val averageSessionDuration: Long,
        val preferredChargingHours: List<Int>,
        val overchargingRisk: RiskLevel,
        val chargingEfficiencyScore: Int,
    )

    data class UsagePeriod(
        val startHour: Int,
        val endHour: Int,
        val intensityLevel: Int, // 1-5
    )

    data class Recommendation(
        val id: String,
        val title: String,
        val description: String,
        val priority: Priority,
        val category: RecommendationCategory,
        val estimatedImpact: String,
        val actionRequired: Boolean = false,
    )

    data class BatteryPredictions(
        val estimatedLifespan: EstimatedLifespan,
        val nextChargingRecommendation: String,
        val capacityDegradation: CapacityDegradation,
        val optimalChargingSchedule: List<ChargingWindow>,
    )

    data class EstimatedLifespan(
        val remainingMonths: Int,
        val confidence: Double,
        val factors: List<LifespanFactor>,
    )

    data class CapacityDegradation(
        val currentCapacityPercent: Double,
        val monthlyDegradationRate: Double,
        val projectedCapacityIn6Months: Double,
    )

    data class ChargingWindow(
        val startHour: Int,
        val endHour: Int,
        val priority: Priority,
        val reason: String,
    )

    data class LifespanFactor(
        val factor: String,
        val impact: Double, // -1.0 to 1.0
        val description: String,
    )

    enum class TrendDirection { IMPROVING, STABLE, DECLINING, CRITICAL }

    enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

    enum class Priority { LOW, MEDIUM, HIGH, URGENT }

    enum class RecommendationCategory {
        CHARGING,
        TEMPERATURE,
        USAGE,
        HEALTH,
        PERFORMANCE,
        SETTINGS,
    }

    suspend fun generateInsights(currentBatteryData: BatteryData): BatteryInsights {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7L * 24 * 60 * 60 * 1000)
        val monthAgo = now - (30L * 24 * 60 * 60 * 1000)

        return BatteryInsights(
            batteryScore = calculateOverallBatteryScore(currentBatteryData),
            healthTrend = analyzeHealthTrend(weekAgo, monthAgo),
            chargingEfficiency = calculateChargingEfficiency(),
            temperatureAnalysis = analyzeTemperaturePatterns(weekAgo),
            usagePatterns = analyzeUsagePatterns(weekAgo),
            recommendations = generateRecommendations(currentBatteryData, weekAgo),
            predictions = generatePredictions(currentBatteryData, monthAgo),
        )
    }

    private fun calculateOverallBatteryScore(batteryData: BatteryData): Int {
        var score = 100

        // Health factor (40% weight)
        val healthScore = (batteryData.healthPercentage * 0.4).toInt()
        score = minOf(score, (score * healthScore / 100.0).toInt())

        // Temperature factor (25% weight)
        val tempScore =
            when {
                batteryData.temperature > 45f -> 60
                batteryData.temperature > 40f -> 80
                batteryData.temperature > 35f -> 90
                else -> 100
            }
        score = (score * (1.0 - 0.25) + tempScore * 0.25).toInt()

        // Charging behavior factor (20% weight)
        val chargingScore =
            when {
                batteryData.batteryLevel > 95 && batteryData.isCharging -> 85 // Overcharging penalty
                batteryData.batteryLevel < 10 -> 90 // Deep discharge penalty
                else -> 100
            }
        score = (score * 0.8 + chargingScore * 0.2).toInt()

        // Usage intensity factor (15% weight)
        val currentScore =
            when {
                abs(batteryData.current) > 3000 -> 80 // High current draw
                abs(batteryData.current) > 2000 -> 90
                else -> 100
            }
        score = (score * 0.85 + currentScore * 0.15).toInt()

        return score.coerceIn(0, 100)
    }

    private suspend fun analyzeHealthTrend(
        weekAgo: Long,
        monthAgo: Long,
    ): HealthTrend {
        val recentLogs = batteryLogDao.getLogsBetween(weekAgo, System.currentTimeMillis())
        val monthlyLogs = batteryLogDao.getLogsBetween(monthAgo, System.currentTimeMillis())

        val currentHealth = recentLogs.lastOrNull()?.healthPercentage ?: 100
        val weekOldHealth = recentLogs.firstOrNull()?.healthPercentage ?: currentHealth
        val monthOldHealth = monthlyLogs.firstOrNull()?.healthPercentage ?: currentHealth

        val weeklyChange = currentHealth - weekOldHealth.toDouble()
        val monthlyChange = currentHealth - monthOldHealth.toDouble()

        val trend =
            when {
                monthlyChange < -5 -> TrendDirection.CRITICAL
                monthlyChange < -2 -> TrendDirection.DECLINING
                monthlyChange > 1 -> TrendDirection.IMPROVING
                else -> TrendDirection.STABLE
            }

        return HealthTrend(currentHealth, weeklyChange, monthlyChange, trend)
    }

    private suspend fun calculateChargingEfficiency(): Double {
        val sessions = chargingSessionDao.getCompletedSessions(20)
        if (sessions.isEmpty()) return 0.85 // Default efficiency

        val efficiencies =
            sessions.mapNotNull { session ->
                session.endLevel?.let { endLevel ->
                    val chargeGain = endLevel - session.startLevel
                    val duration = (session.endTime ?: 0) - session.startTime
                    val hours = duration / (1000.0 * 60 * 60)

                    if (hours > 0 && chargeGain > 0) {
                        // Efficiency = charge gained per hour, normalized
                        val efficiency = chargeGain / hours
                        minOf(efficiency / 50.0, 1.0) // Normalize to 0-1 scale
                    } else {
                        null
                    }
                }
            }

        return efficiencies.average().coerceIn(0.0, 1.0)
    }

    private suspend fun analyzeTemperaturePatterns(weekAgo: Long): TemperatureAnalysis {
        val logs = batteryLogDao.getLogsBetween(weekAgo, System.currentTimeMillis())

        if (logs.isEmpty()) {
            return TemperatureAnalysis(25f, 25f, 0, RiskLevel.LOW)
        }

        val temperatures = logs.map { it.temperature }
        val averageTemp = temperatures.average().toFloat()
        val maxTemp = temperatures.maxOrNull() ?: 25f
        val overheatingEvents = temperatures.count { it > 45f }

        val thermalRisk =
            when {
                maxTemp > 50f || overheatingEvents > 10 -> RiskLevel.CRITICAL
                maxTemp > 45f || overheatingEvents > 5 -> RiskLevel.HIGH
                maxTemp > 40f || overheatingEvents > 0 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }

        return TemperatureAnalysis(averageTemp, maxTemp, overheatingEvents, thermalRisk)
    }

    private suspend fun analyzeUsagePatterns(weekAgo: Long): UsagePatterns {
        val logs = batteryLogDao.getLogsBetween(weekAgo, System.currentTimeMillis())
        val sessions = chargingSessionDao.getCompletedSessions(10)

        // Analyze daily discharge pattern (24-hour pattern)
        val hourlyDischarge = FloatArray(24) { 0f }
        val hourlyCounts = IntArray(24) { 0 }

        logs.windowed(2).forEach { (prev, current) ->
            if (!prev.isCharging && !current.isCharging) {
                val hour =
                    java.time.Instant.ofEpochMilli(current.timestamp)
                        .atZone(java.time.ZoneId.systemDefault()).hour
                val discharge = prev.batteryLevel - current.batteryLevel
                if (discharge > 0 && hour in 0..23) {
                    hourlyDischarge[hour] = hourlyDischarge[hour] + discharge
                    hourlyCounts[hour] = hourlyCounts[hour] + 1
                }
            }
        }

        val dailyPattern =
            hourlyDischarge.mapIndexed { index, total ->
                if (hourlyCounts[index] > 0) total / hourlyCounts[index] else 0f
            }

        // Analyze charging habits
        val chargingHabits =
            if (sessions.isNotEmpty()) {
                val avgDuration =
                    sessions.mapNotNull { session ->
                        session.endTime?.let { endTime ->
                            endTime - session.startTime
                        }
                    }.average().toLong()

                val chargingHours =
                    sessions.map { session ->
                        java.time.Instant.ofEpochMilli(session.startTime)
                            .atZone(java.time.ZoneId.systemDefault()).hour
                    }.groupingBy { it }.eachCount()

                val preferredHours =
                    chargingHours.entries
                        .sortedByDescending { it.value }
                        .take(3)
                        .map { it.key }

                val overchargingRisk =
                    if (sessions.any {
                            (it.endLevel ?: 0) > 95 &&
                                ((it.endTime ?: 0) - it.startTime) > 2 * 60 * 60 * 1000
                        }
                    ) {
                        RiskLevel.HIGH
                    } else {
                        RiskLevel.LOW
                    }

                val efficiencyScore = (calculateChargingEfficiency() * 100).toInt()

                ChargingHabits(avgDuration, preferredHours, overchargingRisk, efficiencyScore)
            } else {
                ChargingHabits(0L, emptyList(), RiskLevel.LOW, 85)
            }

        // Identify heavy usage periods
        val heavyUsagePeriods =
            dailyPattern.mapIndexedNotNull { hour, discharge ->
                if (discharge > 3f) { // More than 3% per hour
                    val intensity = minOf(5, (discharge / 2).toInt() + 1)
                    UsagePeriod(hour, hour + 1, intensity)
                } else {
                    null
                }
            }

        return UsagePatterns(dailyPattern, chargingHabits, heavyUsagePeriods)
    }

    private suspend fun generateRecommendations(
        batteryData: BatteryData,
        weekAgo: Long,
    ): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()

        // Temperature-based recommendations
        if (batteryData.temperature > 45f) {
            recommendations.add(
                Recommendation(
                    id = "temp_high",
                    title = "Device Overheating",
                    description = "Your device temperature is ${batteryData.temperature.toInt()}°C. Remove case, avoid direct sunlight, and close heavy apps.",
                    priority = Priority.URGENT,
                    category = RecommendationCategory.TEMPERATURE,
                    estimatedImpact = "+15% battery lifespan",
                    actionRequired = true,
                ),
            )
        }

        // Charging behavior recommendations
        if (batteryData.batteryLevel > 95 && batteryData.isCharging) {
            recommendations.add(
                Recommendation(
                    id = "overcharge_warning",
                    title = "Avoid Overcharging",
                    description = "Unplug your charger to prevent battery degradation. Optimal range is 20-80%.",
                    priority = Priority.HIGH,
                    category = RecommendationCategory.CHARGING,
                    estimatedImpact = "+20% battery lifespan",
                    actionRequired = true,
                ),
            )
        }

        // Health-based recommendations
        if (batteryData.healthPercentage < 80) {
            recommendations.add(
                Recommendation(
                    id = "health_declining",
                    title = "Battery Health Declining",
                    description = "Consider battery replacement or enable aggressive power saving modes.",
                    priority = Priority.HIGH,
                    category = RecommendationCategory.HEALTH,
                    estimatedImpact = "Restore full performance",
                    actionRequired = false,
                ),
            )
        }

        // Usage pattern recommendations
        val recentSessions = chargingSessionDao.getCompletedSessions(5)
        if (recentSessions.any { (it.endTime ?: 0) - it.startTime > 8 * 60 * 60 * 1000 }) {
            recommendations.add(
                Recommendation(
                    id = "long_charging",
                    title = "Optimize Charging Duration",
                    description = "Long charging sessions detected. Use smart charging or set charging limits.",
                    priority = Priority.MEDIUM,
                    category = RecommendationCategory.CHARGING,
                    estimatedImpact = "+10% charging efficiency",
                    actionRequired = false,
                ),
            )
        }

        return recommendations.sortedByDescending { it.priority.ordinal }
    }

    private suspend fun generatePredictions(
        batteryData: BatteryData,
        monthAgo: Long,
    ): BatteryPredictions {
        val logs = batteryLogDao.getLogsBetween(monthAgo, System.currentTimeMillis())

        // Estimate battery lifespan
        val healthDegradation =
            if (logs.size > 100) {
                val oldestHealth = logs.first().healthPercentage
                val latestHealth = logs.last().healthPercentage
                val monthlyDegradation = (oldestHealth - latestHealth).toDouble()
                kotlin.math.max(monthlyDegradation, 0.5) // Minimum 0.5% per month
            } else {
                1.0
            }

        val remainingMonths = ((batteryData.healthPercentage - 60) / healthDegradation).toInt()
        val confidence = if (logs.size > 200) 0.85 else 0.65

        val lifespanFactors =
            listOf(
                LifespanFactor(
                    "Temperature Management",
                    if (batteryData.temperature > 40f) -0.3 else 0.1,
                    "High temperatures accelerate battery degradation",
                ),
                LifespanFactor(
                    "Charging Habits",
                    if (batteryData.batteryLevel > 90 && batteryData.isCharging) -0.2 else 0.1,
                    "Optimal charging range is 20-80%",
                ),
                LifespanFactor(
                    "Usage Intensity",
                    if (abs(batteryData.current) > 2000) -0.1 else 0.05,
                    "High current draw affects battery longevity",
                ),
            )

        val estimatedLifespan =
            EstimatedLifespan(
                remainingMonths = maxOf(remainingMonths, 1),
                confidence = confidence,
                factors = lifespanFactors,
            )

        // Capacity degradation prediction
        val currentCapacity = batteryData.healthPercentage / 100.0
        val degradationRate = healthDegradation / 100.0
        val projectedCapacity = currentCapacity - (degradationRate * 6)

        val capacityDegradation =
            CapacityDegradation(
                currentCapacityPercent = currentCapacity,
                monthlyDegradationRate = degradationRate,
                projectedCapacityIn6Months = kotlin.math.max(projectedCapacity, 0.6),
            )

        // Next charging recommendation
        val nextChargingRecommendation =
            when {
                batteryData.batteryLevel < 20 -> "Charge now to prevent deep discharge"
                batteryData.batteryLevel > 80 && batteryData.isCharging -> "Unplug to prevent overcharging"
                batteryData.batteryLevel in 20..80 -> "Battery level optimal"
                else -> "Consider charging soon"
            }

        // Optimal charging schedule
        val currentHour = java.time.LocalDateTime.now().hour
        val chargingWindows =
            listOf(
                ChargingWindow(22, 6, Priority.HIGH, "Overnight slow charging"),
                ChargingWindow(12, 14, Priority.MEDIUM, "Midday top-up if needed"),
                ChargingWindow(18, 20, Priority.LOW, "Evening preparation"),
            ).filter { it.startHour != currentHour }

        return BatteryPredictions(
            estimatedLifespan = estimatedLifespan,
            nextChargingRecommendation = nextChargingRecommendation,
            capacityDegradation = capacityDegradation,
            optimalChargingSchedule = chargingWindows,
        )
    }

    /**
     * Generate combined analytics flow
     */
    fun getAnalyticsFlow(): Flow<BatteryInsights?> {
        return combine(
            batteryLogDao.getRecentLogs(1),
            chargingSessionDao.getRecentSessions(1),
        ) { logs, sessions ->
            logs.firstOrNull()?.let { latestLog ->
                val batteryData =
                    BatteryData(
                        batteryLevel = latestLog.batteryLevel,
                        isCharging = latestLog.isCharging,
                        temperature = latestLog.temperature,
                        voltage = latestLog.voltage,
                        current = latestLog.current,
                        healthPercentage = latestLog.healthPercentage,
                    )
                generateInsights(batteryData)
            }
        }
    }
}
