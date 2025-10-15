package com.voltai.smartbatteryguardian.analytics

import android.content.Context
import androidx.work.WorkManager
import com.voltai.smartbatteryguardian.data.database.VoltAIDatabase
import com.voltai.smartbatteryguardian.data.database.entities.*
import com.voltai.smartbatteryguardian.ml.BatteryHealthPredictor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*
import kotlin.random.Random

/**
 * Advanced Analytics Engine with Predictive Modeling
 * Provides comprehensive battery analytics, usage pattern analysis, and AI-driven recommendations
 */
@Singleton
class AdvancedAnalyticsEngine @Inject constructor(
    private val context: Context,
    private val database: VoltAIDatabase,
    private val batteryHealthPredictor: BatteryHealthPredictor
) {
    
    private val analyticsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Caching for performance
    private val predictionCache = mutableMapOf<String, CachedPrediction>()
    private val patternCache = mutableMapOf<String, CachedPattern>()
    
    companion object {
        private const val PREDICTION_CACHE_DURATION = 300_000L // 5 minutes
        private const val PATTERN_CACHE_DURATION = 900_000L // 15 minutes
        private const val MIN_DATA_POINTS = 10
        private const val CONFIDENCE_THRESHOLD = 0.7f
    }
    
    /**
     * Generate comprehensive battery analytics report
     */
    suspend fun generateAnalyticsReport(): AnalyticsReport = withContext(Dispatchers.IO) {
        val batteryData = database.batteryDao().getAllBatteryData()
        val chargingSessions = database.batteryDao().getChargingSessions()
        val usageData = database.batteryDao().getAppUsageData()
        
        val currentTime = System.currentTimeMillis()
        
        // Generate different analytics components
        val healthAnalytics = generateHealthAnalytics(batteryData)
        val chargingAnalytics = generateChargingAnalytics(chargingSessions)
        val usagePatterns = generateUsagePatterns(batteryData, usageData)
        val predictions = generatePredictions(batteryData)
        val recommendations = generateRecommendations(healthAnalytics, chargingAnalytics, usagePatterns)
        val trends = generateTrends(batteryData)
        
        AnalyticsReport(
            timestamp = currentTime,
            healthAnalytics = healthAnalytics,
            chargingAnalytics = chargingAnalytics,
            usagePatterns = usagePatterns,
            predictions = predictions,
            recommendations = recommendations,
            trends = trends,
            dataQuality = assessDataQuality(batteryData),
            confidenceScore = calculateOverallConfidence(batteryData.size)
        )
    }
    
    /**
     * Generate health analytics with degradation analysis
     */
    private suspend fun generateHealthAnalytics(batteryData: List<BatteryDataEntity>): HealthAnalytics {
        val recentData = batteryData.takeLast(100) // Last 100 readings
        
        val avgCapacity = recentData.mapNotNull { it.capacity }.average().takeIf { !it.isNaN() } ?: 100.0
        val avgTemperature = recentData.map { it.temperature }.average()
        val avgVoltage = recentData.map { it.voltage }.average()
        
        // Calculate degradation rate
        val degradationRate = calculateDegradationRate(batteryData)
        val healthScore = calculateHealthScore(avgCapacity, avgTemperature, avgVoltage, degradationRate)
        
        // Predict remaining lifespan
        val estimatedLifespan = predictBatteryLifespan(degradationRate, avgCapacity)
        
        // Temperature analysis
        val temperatureAnalysis = analyzeBatteryTemperature(recentData)
        
        return HealthAnalytics(
            overallHealthScore = healthScore,
            currentCapacity = avgCapacity,
            averageTemperature = avgTemperature,
            averageVoltage = avgVoltage,
            degradationRate = degradationRate,
            estimatedLifespanMonths = estimatedLifespan,
            temperatureAnalysis = temperatureAnalysis,
            riskFactors = identifyHealthRiskFactors(recentData),
            recommendations = generateHealthRecommendations(healthScore, temperatureAnalysis)
        )
    }
    
    /**
     * Generate charging behavior analytics
     */
    private suspend fun generateChargingAnalytics(chargingSessions: List<ChargingSessionEntity>): ChargingAnalytics {
        if (chargingSessions.isEmpty()) {
            return ChargingAnalytics.empty()
        }
        
        val recentSessions = chargingSessions.takeLast(50)
        
        // Calculate charging statistics
        val avgChargingTime = recentSessions.map { it.duration }.average()
        val avgChargingSpeed = recentSessions.mapNotNull { it.averagePower }.average()
        val chargingFrequency = calculateChargingFrequency(recentSessions)
        
        // Analyze charging patterns
        val chargingPatterns = analyzeChargingPatterns(recentSessions)
        val chargingEfficiency = calculateChargingEfficiency(recentSessions)
        
        // Optimal charging recommendations
        val optimalTimes = findOptimalChargingTimes(recentSessions)
        
        return ChargingAnalytics(
            averageChargingTime = avgChargingTime,
            averageChargingSpeed = avgChargingSpeed,
            chargingFrequency = chargingFrequency,
            chargingEfficiency = chargingEfficiency,
            chargingPatterns = chargingPatterns,
            optimalChargingTimes = optimalTimes,
            totalChargingSessions = recentSessions.size,
            fastChargingSessions = recentSessions.count { it.chargingType == "fast" },
            slowChargingSessions = recentSessions.count { it.chargingType == "slow" }
        )
    }
    
    /**
     * Generate usage pattern analysis
     */
    private suspend fun generateUsagePatterns(
        batteryData: List<BatteryDataEntity>,
        usageData: List<AppUsageEntity>
    ): UsagePatterns {
        
        val dailyPatterns = analyzeDailyUsagePatterns(batteryData)
        val weeklyPatterns = analyzeWeeklyUsagePatterns(batteryData)
        val appUsagePatterns = analyzeAppUsagePatterns(usageData)
        val screenTimePatterns = analyzeScreenTimePatterns(batteryData)
        
        return UsagePatterns(
            dailyPatterns = dailyPatterns,
            weeklyPatterns = weeklyPatterns,
            appUsagePatterns = appUsagePatterns,
            screenTimePatterns = screenTimePatterns,
            peakUsageHours = findPeakUsageHours(batteryData),
            lowUsageHours = findLowUsageHours(batteryData),
            averageDailyUsage = calculateAverageDailyUsage(batteryData)
        )
    }
    
    /**
     * Generate AI-powered predictions
     */
    private suspend fun generatePredictions(batteryData: List<BatteryDataEntity>): BatteryPredictions {
        val cacheKey = "predictions_${batteryData.size}"
        val cached = predictionCache[cacheKey]
        
        if (cached != null && System.currentTimeMillis() - cached.timestamp < PREDICTION_CACHE_DURATION) {
            return cached.predictions
        }
        
        val predictions = if (batteryData.size >= MIN_DATA_POINTS) {
            // Use ML model for predictions
            val mlPredictions = batteryHealthPredictor.predictBatteryHealth(
                batteryData.takeLast(50).map { it.level }
            )
            
            BatteryPredictions(
                nextChargingTime = predictNextChargingTime(batteryData),
                batteryDepleteTime = predictBatteryDepletion(batteryData),
                chargingTimeToFull = predictChargingTimeToFull(batteryData),
                healthDeclineRate = mlPredictions.healthDeclineRate,
                optimalChargingWindows = predictOptimalChargingWindows(batteryData),
                riskAssessment = assessBatteryRisks(batteryData),
                confidenceScore = mlPredictions.confidence,
                nextMaintenanceDate = predictMaintenanceDate(batteryData)
            )
        } else {
            BatteryPredictions.default()
        }
        
        // Cache predictions
        predictionCache[cacheKey] = CachedPrediction(predictions, System.currentTimeMillis())
        
        return predictions
    }
    
    /**
     * Generate personalized recommendations
     */
    private suspend fun generateRecommendations(
        healthAnalytics: HealthAnalytics,
        chargingAnalytics: ChargingAnalytics,
        usagePatterns: UsagePatterns
    ): List<PersonalizedRecommendation> {
        
        val recommendations = mutableListOf<PersonalizedRecommendation>()
        
        // Health-based recommendations
        if (healthAnalytics.overallHealthScore < 80) {
            recommendations.add(
                PersonalizedRecommendation(
                    id = "health_improvement",
                    title = "Improve Battery Health",
                    description = "Your battery health is declining. Consider charging between 20-80% to extend lifespan.",
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.HEALTH,
                    potentialImpact = "Extend battery life by 30%",
                    actionSteps = listOf(
                        "Avoid charging to 100% daily",
                        "Don't let battery drop below 20%",
                        "Use slow charging when possible"
                    ),
                    estimatedBenefit = 0.3f,
                    confidence = 0.85f
                )
            )
        }
        
        // Temperature-based recommendations
        if (healthAnalytics.averageTemperature > 40) {
            recommendations.add(
                PersonalizedRecommendation(
                    id = "temperature_management",
                    title = "Reduce Battery Temperature",
                    description = "High temperatures are damaging your battery. Consider cooling strategies.",
                    priority = RecommendationPriority.HIGH,
                    category = RecommendationCategory.TEMPERATURE,
                    potentialImpact = "Prevent thermal damage",
                    actionSteps = listOf(
                        "Avoid direct sunlight",
                        "Remove phone case while charging",
                        "Close resource-intensive apps"
                    ),
                    estimatedBenefit = 0.4f,
                    confidence = 0.9f
                )
            )
        }
        
        // Charging pattern recommendations
        if (chargingAnalytics.chargingFrequency > 3) {
            recommendations.add(
                PersonalizedRecommendation(
                    id = "charging_optimization",
                    title = "Optimize Charging Pattern",
                    description = "Frequent charging cycles may reduce battery lifespan.",
                    priority = RecommendationPriority.MEDIUM,
                    category = RecommendationCategory.CHARGING,
                    potentialImpact = "Reduce charging cycles by 25%",
                    actionSteps = listOf(
                        "Charge less frequently",
                        "Use battery saver mode",
                        "Optimize app usage"
                    ),
                    estimatedBenefit = 0.25f,
                    confidence = 0.75f
                )
            )
        }
        
        // Usage-based recommendations
        val peakUsage = usagePatterns.peakUsageHours.firstOrNull()
        if (peakUsage != null) {
            recommendations.add(
                PersonalizedRecommendation(
                    id = "usage_optimization",
                    title = "Optimize Peak Usage Time",
                    description = "High usage during ${peakUsage}:00 hour is draining your battery quickly.",
                    priority = RecommendationPriority.LOW,
                    category = RecommendationCategory.USAGE,
                    potentialImpact = "Save 15% daily battery",
                    actionSteps = listOf(
                        "Reduce screen brightness during peak hours",
                        "Close unnecessary apps",
                        "Enable power saving mode"
                    ),
                    estimatedBenefit = 0.15f,
                    confidence = 0.7f
                )
            )
        }
        
        return recommendations.sortedByDescending { it.priority.ordinal * it.estimatedBenefit }
    }
    
    /**
     * Generate trend analysis
     */
    private suspend fun generateTrends(batteryData: List<BatteryDataEntity>): BatteryTrends {
        val last30Days = batteryData.filter { 
            System.currentTimeMillis() - it.timestamp < 30 * 24 * 60 * 60 * 1000L 
        }
        
        val healthTrend = calculateHealthTrend(last30Days)
        val chargingTrend = calculateChargingTrend(last30Days)
        val temperatureTrend = calculateTemperatureTrend(last30Days)
        val usageTrend = calculateUsageTrend(last30Days)
        
        return BatteryTrends(
            healthTrend = healthTrend,
            chargingTrend = chargingTrend,
            temperatureTrend = temperatureTrend,
            usageTrend = usageTrend,
            overallTrend = calculateOverallTrend(healthTrend, chargingTrend, temperatureTrend, usageTrend)
        )
    }
    
    // Helper functions for calculations
    private fun calculateDegradationRate(batteryData: List<BatteryDataEntity>): Double {
        if (batteryData.size < MIN_DATA_POINTS) return 0.0
        
        val capacityData = batteryData.mapNotNull { 
            it.capacity?.let { cap -> it.timestamp to cap }
        }.sortedBy { it.first }
        
        if (capacityData.size < MIN_DATA_POINTS) return 0.0
        
        // Linear regression to find degradation rate
        val n = capacityData.size
        val sumX = capacityData.sumOf { it.first }
        val sumY = capacityData.sumOf { it.second }
        val sumXY = capacityData.sumOf { it.first * it.second }
        val sumX2 = capacityData.sumOf { it.first * it.first }
        
        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        
        // Convert to percentage per month
        val msPerMonth = 30 * 24 * 60 * 60 * 1000L
        return slope * msPerMonth
    }
    
    private fun calculateHealthScore(
        capacity: Double,
        temperature: Double,
        voltage: Double,
        degradationRate: Double
    ): Double {
        val capacityScore = (capacity / 100.0) * 40 // 40% weight
        val temperatureScore = maxOf(0.0, (50 - temperature) / 50.0) * 30 // 30% weight
        val voltageScore = minOf(1.0, voltage / 4.2) * 20 // 20% weight
        val degradationScore = maxOf(0.0, (1.0 - abs(degradationRate) / 10.0)) * 10 // 10% weight
        
        return minOf(100.0, maxOf(0.0, capacityScore + temperatureScore + voltageScore + degradationScore))
    }
    
    private fun predictBatteryLifespan(degradationRate: Double, currentCapacity: Double): Int {
        if (degradationRate >= 0) return 36 // If no degradation, assume 3 years
        
        val monthsToReach80Percent = ((currentCapacity - 80) / abs(degradationRate)).toInt()
        return maxOf(6, minOf(60, monthsToReach80Percent)) // Between 6 months and 5 years
    }
    
    private fun analyzeBatteryTemperature(batteryData: List<BatteryDataEntity>): TemperatureAnalysis {
        val temperatures = batteryData.map { it.temperature }
        
        return TemperatureAnalysis(
            average = temperatures.average(),
            maximum = temperatures.maxOrNull() ?: 0.0,
            minimum = temperatures.minOrNull() ?: 0.0,
            variance = calculateVariance(temperatures),
            overheatingEvents = temperatures.count { it > 45 },
            coolingEvents = temperatures.count { it < 15 }
        )
    }
    
    private fun calculateVariance(values: List<Double>): Double {
        val mean = values.average()
        return values.map { (it - mean).pow(2) }.average()
    }
    
    private fun identifyHealthRiskFactors(batteryData: List<BatteryDataEntity>): List<HealthRiskFactor> {
        val riskFactors = mutableListOf<HealthRiskFactor>()
        
        // High temperature risk
        val highTempEvents = batteryData.count { it.temperature > 45 }
        if (highTempEvents > batteryData.size * 0.1) {
            riskFactors.add(HealthRiskFactor("HIGH_TEMPERATURE", "Frequent overheating", 0.8f))
        }
        
        // Deep discharge risk
        val deepDischarges = batteryData.count { it.level < 10 }
        if (deepDischarges > batteryData.size * 0.05) {
            riskFactors.add(HealthRiskFactor("DEEP_DISCHARGE", "Frequent deep discharges", 0.7f))
        }
        
        // Fast charging risk
        val fastChargingEvents = batteryData.count { it.chargingSpeed > 15 }
        if (fastChargingEvents > batteryData.size * 0.3) {
            riskFactors.add(HealthRiskFactor("FAST_CHARGING", "Excessive fast charging", 0.6f))
        }
        
        return riskFactors
    }
    
    private fun generateHealthRecommendations(
        healthScore: Double,
        temperatureAnalysis: TemperatureAnalysis
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (healthScore < 70) {
            recommendations.add("Consider battery replacement soon")
            recommendations.add("Avoid extreme temperatures")
            recommendations.add("Use original charger only")
        }
        
        if (temperatureAnalysis.average > 35) {
            recommendations.add("Keep device cool during charging")
            recommendations.add("Remove case while charging")
        }
        
        return recommendations
    }
    
    private fun calculateChargingFrequency(sessions: List<ChargingSessionEntity>): Double {
        if (sessions.isEmpty()) return 0.0
        
        val timeSpan = sessions.maxOf { it.startTime } - sessions.minOf { it.startTime }
        val days = timeSpan / (24 * 60 * 60 * 1000.0)
        
        return if (days > 0) sessions.size / days else 0.0
    }
    
    private fun analyzeChargingPatterns(sessions: List<ChargingSessionEntity>): ChargingPatterns {
        val hourlyDistribution = sessions.groupBy { 
            Date(it.startTime).hours 
        }.mapValues { it.value.size }
        
        val weeklyDistribution = sessions.groupBy {
            Calendar.getInstance().apply { timeInMillis = it.startTime }.get(Calendar.DAY_OF_WEEK)
        }.mapValues { it.value.size }
        
        return ChargingPatterns(
            hourlyDistribution = hourlyDistribution,
            weeklyDistribution = weeklyDistribution,
            mostCommonHour = hourlyDistribution.maxByOrNull { it.value }?.key ?: 0,
            leastCommonHour = hourlyDistribution.minByOrNull { it.value }?.key ?: 0
        )
    }
    
    private fun calculateChargingEfficiency(sessions: List<ChargingSessionEntity>): Double {
        val validSessions = sessions.filter { it.energyAdded > 0 && it.duration > 0 }
        if (validSessions.isEmpty()) return 0.0
        
        return validSessions.map { it.energyAdded / it.duration }.average()
    }
    
    private fun findOptimalChargingTimes(sessions: List<ChargingSessionEntity>): List<TimeRange> {
        // Analyze efficiency by hour
        val hourlyEfficiency = sessions.groupBy { 
            Date(it.startTime).hours 
        }.mapValues { hourSessions ->
            hourSessions.map { it.energyAdded / maxOf(1.0, it.duration) }.average()
        }
        
        // Find top 3 most efficient hours
        return hourlyEfficiency.toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { TimeRange(it.first, it.first + 1) }
    }
    
    private suspend fun analyzeDailyUsagePatterns(batteryData: List<BatteryDataEntity>): DailyPatterns {
        val hourlyUsage = batteryData.groupBy { 
            Date(it.timestamp).hours 
        }.mapValues { entries ->
            entries.map { it.level }.let { levels ->
                if (levels.size > 1) levels.first() - levels.last() else 0
            }
        }
        
        return DailyPatterns(
            hourlyUsage = hourlyUsage,
            peakHours = hourlyUsage.filter { it.value > 5 }.keys.toList(),
            lowHours = hourlyUsage.filter { it.value < 2 }.keys.toList()
        )
    }
    
    private suspend fun analyzeWeeklyUsagePatterns(batteryData: List<BatteryDataEntity>): WeeklyPatterns {
        val dailyUsage = batteryData.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.DAY_OF_WEEK)
        }.mapValues { entries ->
            entries.sumOf { maxOf(0, it.level - (entries.minByOrNull { e -> e.timestamp }?.level ?: it.level)) }
        }
        
        return WeeklyPatterns(
            dailyUsage = dailyUsage,
            weekdayAverage = dailyUsage.filterKeys { it in 2..6 }.values.average(),
            weekendAverage = dailyUsage.filterKeys { it in listOf(1, 7) }.values.average()
        )
    }
    
    private suspend fun analyzeAppUsagePatterns(usageData: List<AppUsageEntity>): AppUsagePatterns {
        val topApps = usageData.groupBy { it.packageName }
            .mapValues { it.value.sumOf { app -> app.usageTime } }
            .toList()
            .sortedByDescending { it.second }
            .take(10)
        
        return AppUsagePatterns(
            topApps = topApps,
            totalUsageTime = usageData.sumOf { it.usageTime },
            averageSessionDuration = usageData.map { it.usageTime }.average()
        )
    }
    
    private suspend fun analyzeScreenTimePatterns(batteryData: List<BatteryDataEntity>): ScreenTimePatterns {
        // This is a simplified implementation
        // In reality, you'd track screen on/off events
        return ScreenTimePatterns(
            averageDailyScreenTime = 6.5, // hours
            longestSession = 3.2, // hours
            screenOnEvents = batteryData.size / 10 // approximate
        )
    }
    
    private fun findPeakUsageHours(batteryData: List<BatteryDataEntity>): List<Int> {
        return batteryData.groupBy { Date(it.timestamp).hours }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
    
    private fun findLowUsageHours(batteryData: List<BatteryDataEntity>): List<Int> {
        return batteryData.groupBy { Date(it.timestamp).hours }
            .mapValues { it.value.size }
            .toList()
            .sortedBy { it.second }
            .take(3)
            .map { it.first }
    }
    
    private fun calculateAverageDailyUsage(batteryData: List<BatteryDataEntity>): Double {
        val dailyUsage = batteryData.groupBy { 
            it.timestamp / (24 * 60 * 60 * 1000L)
        }.mapValues { entries ->
            val levels = entries.map { it.level }.sorted()
            if (levels.isNotEmpty()) levels.last() - levels.first() else 0
        }
        
        return dailyUsage.values.average()
    }
    
    private suspend fun predictNextChargingTime(batteryData: List<BatteryDataEntity>): Long {
        val currentLevel = batteryData.lastOrNull()?.level ?: 50
        val avgDrainRate = calculateAverageDrainRate(batteryData)
        
        val timeToLowBattery = if (avgDrainRate > 0) {
            ((currentLevel - 20) / avgDrainRate) * 60 * 60 * 1000L // Convert to ms
        } else {
            8 * 60 * 60 * 1000L // Default to 8 hours
        }
        
        return System.currentTimeMillis() + timeToLowBattery.toLong()
    }
    
    private fun calculateAverageDrainRate(batteryData: List<BatteryDataEntity>): Double {
        if (batteryData.size < 2) return 1.0 // Default drain rate
        
        val drainRates = batteryData.windowed(2).map { (prev, curr) ->
            val timeDiff = (curr.timestamp - prev.timestamp) / (60 * 60 * 1000.0) // hours
            val levelDiff = prev.level - curr.level
            if (timeDiff > 0 && levelDiff > 0) levelDiff / timeDiff else 0.0
        }.filter { it > 0 }
        
        return drainRates.average().takeIf { !it.isNaN() } ?: 1.0
    }
    
    private suspend fun predictBatteryDepletion(batteryData: List<BatteryDataEntity>): Long {
        val currentLevel = batteryData.lastOrNull()?.level ?: 50
        val drainRate = calculateAverageDrainRate(batteryData)
        
        val hoursRemaining = if (drainRate > 0) currentLevel / drainRate else 10.0
        return System.currentTimeMillis() + (hoursRemaining * 60 * 60 * 1000).toLong()
    }
    
    private suspend fun predictChargingTimeToFull(batteryData: List<BatteryDataEntity>): Double {
        val currentLevel = batteryData.lastOrNull()?.level ?: 50
        val avgChargingSpeed = calculateAverageChargingSpeed(batteryData)
        
        val remainingCapacity = 100 - currentLevel
        return if (avgChargingSpeed > 0) remainingCapacity / avgChargingSpeed else 2.0 // hours
    }
    
    private fun calculateAverageChargingSpeed(batteryData: List<BatteryDataEntity>): Double {
        val chargingSessions = batteryData.windowed(2).mapNotNull { (prev, curr) ->
            if (curr.level > prev.level) { // Charging detected
                val timeDiff = (curr.timestamp - prev.timestamp) / (60 * 60 * 1000.0) // hours
                val levelDiff = curr.level - prev.level
                if (timeDiff > 0) levelDiff / timeDiff else null
            } else null
        }
        
        return chargingSessions.average().takeIf { !it.isNaN() } ?: 15.0 // Default 15% per hour
    }
    
    private suspend fun predictOptimalChargingWindows(batteryData: List<BatteryDataEntity>): List<ChargingWindow> {
        // Analyze historical charging patterns
        val chargingTimes = batteryData.windowed(2).mapNotNull { (prev, curr) ->
            if (curr.level > prev.level) Date(curr.timestamp).hours else null
        }
        
        val commonHours = chargingTimes.groupingBy { it }.eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(3)
        
        return commonHours.map { (hour, frequency) ->
            ChargingWindow(
                startHour = hour,
                endHour = (hour + 2) % 24,
                efficiency = frequency / chargingTimes.size.toDouble(),
                recommendation = when {
                    hour in 22..23 || hour in 0..6 -> "Optimal for battery health"
                    hour in 7..9 || hour in 17..19 -> "Good for daily routine"
                    else -> "Standard charging window"
                }
            )
        }
    }
    
    private suspend fun assessBatteryRisks(batteryData: List<BatteryDataEntity>): RiskAssessment {
        val recentData = batteryData.takeLast(50)
        
        val temperatureRisk = recentData.count { it.temperature > 45 } / recentData.size.toDouble()
        val deepDischargeRisk = recentData.count { it.level < 10 } / recentData.size.toDouble()
        val overchargeRisk = recentData.count { it.level == 100 } / recentData.size.toDouble()
        
        val overallRisk = (temperatureRisk * 0.4 + deepDischargeRisk * 0.4 + overchargeRisk * 0.2)
        
        return RiskAssessment(
            overallRisk = overallRisk,
            temperatureRisk = temperatureRisk,
            deepDischargeRisk = deepDischargeRisk,
            overchargeRisk = overchargeRisk,
            riskLevel = when {
                overallRisk < 0.1 -> "Low"
                overallRisk < 0.3 -> "Medium"
                else -> "High"
            },
            recommendations = generateRiskRecommendations(temperatureRisk, deepDischargeRisk, overchargeRisk)
        )
    }
    
    private fun generateRiskRecommendations(
        temperatureRisk: Double,
        deepDischargeRisk: Double,
        overchargeRisk: Double
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (temperatureRisk > 0.2) {
            recommendations.add("Monitor device temperature during use and charging")
            recommendations.add("Avoid direct sunlight and hot environments")
        }
        
        if (deepDischargeRisk > 0.1) {
            recommendations.add("Avoid letting battery drop below 20%")
            recommendations.add("Enable low battery warnings")
        }
        
        if (overchargeRisk > 0.3) {
            recommendations.add("Avoid keeping device at 100% charge for extended periods")
            recommendations.add("Use optimized charging features")
        }
        
        return recommendations
    }
    
    private suspend fun predictMaintenanceDate(batteryData: List<BatteryDataEntity>): Long {
        val healthScore = calculateHealthScore(80.0, 30.0, 4.0, -0.5) // Example values
        
        val monthsUntilMaintenance = when {
            healthScore > 90 -> 12
            healthScore > 80 -> 6
            healthScore > 70 -> 3
            else -> 1
        }
        
        return System.currentTimeMillis() + (monthsUntilMaintenance * 30 * 24 * 60 * 60 * 1000L)
    }
    
    private fun calculateHealthTrend(batteryData: List<BatteryDataEntity>): TrendDirection {
        if (batteryData.size < MIN_DATA_POINTS) return TrendDirection.STABLE
        
        val recent = batteryData.takeLast(10)
        val older = batteryData.dropLast(10).takeLast(10)
        
        val recentAvg = recent.mapNotNull { it.capacity }.average()
        val olderAvg = older.mapNotNull { it.capacity }.average()
        
        return when {
            recentAvg.isNaN() || olderAvg.isNaN() -> TrendDirection.STABLE
            recentAvg > olderAvg + 2 -> TrendDirection.IMPROVING
            recentAvg < olderAvg - 2 -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }
    
    private fun calculateChargingTrend(batteryData: List<BatteryDataEntity>): TrendDirection {
        // Analyze charging frequency trends
        return TrendDirection.STABLE // Simplified
    }
    
    private fun calculateTemperatureTrend(batteryData: List<BatteryDataEntity>): TrendDirection {
        if (batteryData.size < MIN_DATA_POINTS) return TrendDirection.STABLE
        
        val recent = batteryData.takeLast(10).map { it.temperature }.average()
        val older = batteryData.dropLast(10).takeLast(10).map { it.temperature }.average()
        
        return when {
            recent > older + 2 -> TrendDirection.DECLINING // Higher temp is worse
            recent < older - 2 -> TrendDirection.IMPROVING // Lower temp is better
            else -> TrendDirection.STABLE
        }
    }
    
    private fun calculateUsageTrend(batteryData: List<BatteryDataEntity>): TrendDirection {
        // Analyze usage intensity trends
        return TrendDirection.STABLE // Simplified
    }
    
    private fun calculateOverallTrend(
        healthTrend: TrendDirection,
        chargingTrend: TrendDirection,
        temperatureTrend: TrendDirection,
        usageTrend: TrendDirection
    ): TrendDirection {
        val trends = listOf(healthTrend, chargingTrend, temperatureTrend, usageTrend)
        val improvingCount = trends.count { it == TrendDirection.IMPROVING }
        val decliningCount = trends.count { it == TrendDirection.DECLINING }
        
        return when {
            improvingCount > decliningCount -> TrendDirection.IMPROVING
            decliningCount > improvingCount -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }
    
    private fun assessDataQuality(batteryData: List<BatteryDataEntity>): DataQuality {
        val totalPoints = batteryData.size
        val validPoints = batteryData.count { 
            it.level in 0..100 && it.temperature > -10 && it.temperature < 80 
        }
        
        val completeness = if (totalPoints > 0) validPoints.toDouble() / totalPoints else 0.0
        val timeSpan = if (batteryData.size >= 2) {
            batteryData.maxOf { it.timestamp } - batteryData.minOf { it.timestamp }
        } else 0L
        
        val daysCovered = timeSpan / (24 * 60 * 60 * 1000L)
        
        return DataQuality(
            completeness = completeness,
            totalDataPoints = totalPoints,
            daysCovered = daysCovered.toInt(),
            quality = when {
                completeness > 0.9 && daysCovered > 7 -> "High"
                completeness > 0.7 && daysCovered > 3 -> "Medium"
                else -> "Low"
            }
        )
    }
    
    private fun calculateOverallConfidence(dataPoints: Int): Double {
        return minOf(1.0, dataPoints / 100.0) // Confidence increases with more data points
    }
    
    // Data classes for analytics results
    data class CachedPrediction(
        val predictions: BatteryPredictions,
        val timestamp: Long
    )
    
    data class CachedPattern(
        val patterns: UsagePatterns,
        val timestamp: Long
    )
    
    enum class TrendDirection {
        IMPROVING, STABLE, DECLINING
    }
    
    enum class RecommendationPriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    enum class RecommendationCategory {
        HEALTH, CHARGING, TEMPERATURE, USAGE, PERFORMANCE
    }
}

// Data classes for analytics results
data class AnalyticsReport(
    val timestamp: Long,
    val healthAnalytics: HealthAnalytics,
    val chargingAnalytics: ChargingAnalytics,
    val usagePatterns: UsagePatterns,
    val predictions: BatteryPredictions,
    val recommendations: List<PersonalizedRecommendation>,
    val trends: BatteryTrends,
    val dataQuality: DataQuality,
    val confidenceScore: Double
)

data class HealthAnalytics(
    val overallHealthScore: Double,
    val currentCapacity: Double,
    val averageTemperature: Double,
    val averageVoltage: Double,
    val degradationRate: Double,
    val estimatedLifespanMonths: Int,
    val temperatureAnalysis: TemperatureAnalysis,
    val riskFactors: List<HealthRiskFactor>,
    val recommendations: List<String>
)

data class TemperatureAnalysis(
    val average: Double,
    val maximum: Double,
    val minimum: Double,
    val variance: Double,
    val overheatingEvents: Int,
    val coolingEvents: Int
)

data class HealthRiskFactor(
    val type: String,
    val description: String,
    val severity: Float
)

data class ChargingAnalytics(
    val averageChargingTime: Double,
    val averageChargingSpeed: Double,
    val chargingFrequency: Double,
    val chargingEfficiency: Double,
    val chargingPatterns: ChargingPatterns,
    val optimalChargingTimes: List<TimeRange>,
    val totalChargingSessions: Int,
    val fastChargingSessions: Int,
    val slowChargingSessions: Int
) {
    companion object {
        fun empty() = ChargingAnalytics(
            averageChargingTime = 0.0,
            averageChargingSpeed = 0.0,
            chargingFrequency = 0.0,
            chargingEfficiency = 0.0,
            chargingPatterns = ChargingPatterns(emptyMap(), emptyMap(), 0, 0),
            optimalChargingTimes = emptyList(),
            totalChargingSessions = 0,
            fastChargingSessions = 0,
            slowChargingSessions = 0
        )
    }
}

data class ChargingPatterns(
    val hourlyDistribution: Map<Int, Int>,
    val weeklyDistribution: Map<Int, Int>,
    val mostCommonHour: Int,
    val leastCommonHour: Int
)

data class TimeRange(
    val startHour: Int,
    val endHour: Int
)

data class UsagePatterns(
    val dailyPatterns: DailyPatterns,
    val weeklyPatterns: WeeklyPatterns,
    val appUsagePatterns: AppUsagePatterns,
    val screenTimePatterns: ScreenTimePatterns,
    val peakUsageHours: List<Int>,
    val lowUsageHours: List<Int>,
    val averageDailyUsage: Double
)

data class DailyPatterns(
    val hourlyUsage: Map<Int, Int>,
    val peakHours: List<Int>,
    val lowHours: List<Int>
)

data class WeeklyPatterns(
    val dailyUsage: Map<Int, Int>,
    val weekdayAverage: Double,
    val weekendAverage: Double
)

data class AppUsagePatterns(
    val topApps: List<Pair<String, Long>>,
    val totalUsageTime: Long,
    val averageSessionDuration: Double
)

data class ScreenTimePatterns(
    val averageDailyScreenTime: Double,
    val longestSession: Double,
    val screenOnEvents: Int
)

data class BatteryPredictions(
    val nextChargingTime: Long,
    val batteryDepleteTime: Long,
    val chargingTimeToFull: Double,
    val healthDeclineRate: Double,
    val optimalChargingWindows: List<ChargingWindow>,
    val riskAssessment: RiskAssessment,
    val confidenceScore: Float,
    val nextMaintenanceDate: Long
) {
    companion object {
        fun default() = BatteryPredictions(
            nextChargingTime = System.currentTimeMillis() + 8 * 60 * 60 * 1000L,
            batteryDepleteTime = System.currentTimeMillis() + 10 * 60 * 60 * 1000L,
            chargingTimeToFull = 2.0,
            healthDeclineRate = 0.1,
            optimalChargingWindows = emptyList(),
            riskAssessment = RiskAssessment.default(),
            confidenceScore = 0.5f,
            nextMaintenanceDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L
        )
    }
}

data class ChargingWindow(
    val startHour: Int,
    val endHour: Int,
    val efficiency: Double,
    val recommendation: String
)

data class RiskAssessment(
    val overallRisk: Double,
    val temperatureRisk: Double,
    val deepDischargeRisk: Double,
    val overchargeRisk: Double,
    val riskLevel: String,
    val recommendations: List<String>
) {
    companion object {
        fun default() = RiskAssessment(
            overallRisk = 0.1,
            temperatureRisk = 0.05,
            deepDischargeRisk = 0.03,
            overchargeRisk = 0.02,
            riskLevel = "Low",
            recommendations = emptyList()
        )
    }
}

data class PersonalizedRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val priority: AdvancedAnalyticsEngine.RecommendationPriority,
    val category: AdvancedAnalyticsEngine.RecommendationCategory,
    val potentialImpact: String,
    val actionSteps: List<String>,
    val estimatedBenefit: Float,
    val confidence: Float
)

data class BatteryTrends(
    val healthTrend: AdvancedAnalyticsEngine.TrendDirection,
    val chargingTrend: AdvancedAnalyticsEngine.TrendDirection,
    val temperatureTrend: AdvancedAnalyticsEngine.TrendDirection,
    val usageTrend: AdvancedAnalyticsEngine.TrendDirection,
    val overallTrend: AdvancedAnalyticsEngine.TrendDirection
)

data class DataQuality(
    val completeness: Double,
    val totalDataPoints: Int,
    val daysCovered: Int,
    val quality: String
)
