package com.voltai.core.analytics

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class BatteryAnalyticsEngine @Inject constructor(
    private val context: Context
) {
    
    private val _analyticsData = MutableStateFlow(BatteryAnalytics())
    val analyticsData: Flow<BatteryAnalytics> = _analyticsData.asStateFlow()
    
    // Historical data storage (simplified - in production use Room database)
    private val batteryHistoryData = mutableListOf<BatteryDataPoint>()
    private val chargingCycles = mutableListOf<ChargingCycle>()
    private val usagePatterns = mutableListOf<UsagePattern>()
    
    companion object {
        private const val MAX_HISTORY_POINTS = 10000
        private const val DEGRADATION_THRESHOLD = 0.8f
        private const val CYCLE_COUNT_WEIGHT = 0.3f
        private const val TEMPERATURE_WEIGHT = 0.4f
        private const val VOLTAGE_WEIGHT = 0.3f
    }
    
    fun addBatteryDataPoint(
        timestamp: Long,
        batteryLevel: Int,
        voltage: Float,
        temperature: Float,
        isCharging: Boolean,
        current: Int = 0
    ) {
        val dataPoint = BatteryDataPoint(
            timestamp = timestamp,
            batteryLevel = batteryLevel,
            voltage = voltage,
            temperature = temperature,
            isCharging = isCharging,
            current = current
        )
        
        // Add to history and maintain size limit
        batteryHistoryData.add(dataPoint)
        if (batteryHistoryData.size > MAX_HISTORY_POINTS) {
            batteryHistoryData.removeAt(0)
        }
        
        // Update analytics
        updateAnalytics()
    }
    
    private fun updateAnalytics() {
        if (batteryHistoryData.isEmpty()) return
        
        val analytics = BatteryAnalytics(
            totalCycleCount = calculateCycleCount(),
            averageChargingTime = calculateAverageChargingTime(),
            averageDischargingTime = calculateAverageDischargingTime(),
            batteryHealthScore = calculateBatteryHealthScore(),
            estimatedCapacityMah = estimateBatteryCapacity(),
            degradationRate = calculateDegradationRate(),
            optimalChargingPattern = analyzeOptimalChargingPattern(),
            temperatureAnalysis = analyzeTemperaturePatterns(),
            usageRecommendations = generateUsageRecommendations(),
            predictionAccuracy = calculatePredictionAccuracy(),
            nextMaintenanceDate = predictMaintenanceDate(),
            powerConsumptionTrends = analyzePowerConsumption(),
            chargingEfficiency = calculateChargingEfficiency()
        )
        
        _analyticsData.value = analytics
    }
    
    private fun calculateCycleCount(): Int {
        var cycleCount = 0
        var chargeAccumulated = 0
        
        for (i in 1 until batteryHistoryData.size) {
            val current = batteryHistoryData[i]
            val previous = batteryHistoryData[i - 1]
            
            if (current.isCharging && current.batteryLevel > previous.batteryLevel) {
                chargeAccumulated += (current.batteryLevel - previous.batteryLevel)
                if (chargeAccumulated >= 100) {
                    cycleCount++
                    chargeAccumulated -= 100
                }
            }
        }
        
        return cycleCount
    }
    
    private fun calculateAverageChargingTime(): Long {
        val chargingSessions = mutableListOf<Long>()
        var sessionStart: Long? = null
        
        batteryHistoryData.forEach { dataPoint ->
            if (dataPoint.isCharging && sessionStart == null) {
                sessionStart = dataPoint.timestamp
            } else if (!dataPoint.isCharging && sessionStart != null) {
                chargingSessions.add(dataPoint.timestamp - sessionStart!!)
                sessionStart = null
            }
        }
        
        return if (chargingSessions.isNotEmpty()) {
            chargingSessions.average().toLong()
        } else 0L
    }
    
    private fun calculateAverageDischargingTime(): Long {
        val dischargingSessions = mutableListOf<Long>()
        var sessionStart: Long? = null
        
        batteryHistoryData.forEach { dataPoint ->
            if (!dataPoint.isCharging && sessionStart == null) {
                sessionStart = dataPoint.timestamp
            } else if (dataPoint.isCharging && sessionStart != null) {
                dischargingSessions.add(dataPoint.timestamp - sessionStart!!)
                sessionStart = null
            }
        }
        
        return if (dischargingSessions.isNotEmpty()) {
            dischargingSessions.average().toLong()
        } else 0L
    }
    
    private fun calculateBatteryHealthScore(): Float {
        if (batteryHistoryData.size < 10) return 100f
        
        val recentData = batteryHistoryData.takeLast(100)
        
        // Analyze voltage patterns
        val avgVoltage = recentData.map { it.voltage }.average().toFloat()
        val voltageScore = when {
            avgVoltage >= 4.0f -> 100f
            avgVoltage >= 3.8f -> 80f
            avgVoltage >= 3.6f -> 60f
            avgVoltage >= 3.4f -> 40f
            else -> 20f
        }
        
        // Analyze temperature patterns
        val avgTemperature = recentData.map { it.temperature }.average().toFloat()
        val temperatureScore = when {
            avgTemperature <= 30f -> 100f
            avgTemperature <= 35f -> 80f
            avgTemperature <= 40f -> 60f
            avgTemperature <= 45f -> 40f
            else -> 20f
        }
        
        // Analyze charging patterns
        val cycleScore = max(100f - (calculateCycleCount() * 0.01f), 20f)
        
        // Weighted health score
        return (voltageScore * VOLTAGE_WEIGHT + 
                temperatureScore * TEMPERATURE_WEIGHT + 
                cycleScore * CYCLE_COUNT_WEIGHT)
    }
    
    private fun estimateBatteryCapacity(): Int {
        // Simplified capacity estimation based on charging patterns
        val fullChargingSessions = findFullChargingSessions()
        
        if (fullChargingSessions.isEmpty()) return 3000 // Default estimate
        
        val averageChargeTime = fullChargingSessions.map { it.duration }.average()
        val averageCurrent = fullChargingSessions.map { it.averageCurrent }.average()
        
        // Rough estimate: capacity = current * time (in hours)
        return (averageCurrent * (averageChargeTime / 3600000.0)).toInt() // Convert ms to hours
    }
    
    private fun calculateDegradationRate(): Float {
        if (batteryHistoryData.size < 100) return 0f
        
        val oldCapacity = estimateBatteryCapacityForPeriod(0, batteryHistoryData.size / 2)
        val newCapacity = estimateBatteryCapacityForPeriod(batteryHistoryData.size / 2, batteryHistoryData.size)
        
        return if (oldCapacity > 0) {
            ((oldCapacity - newCapacity) / oldCapacity.toFloat()) * 100f
        } else 0f
    }
    
    private fun estimateBatteryCapacityForPeriod(startIndex: Int, endIndex: Int): Int {
        val periodData = batteryHistoryData.subList(startIndex, endIndex)
        // Simplified capacity estimation for a specific period
        return 3000 // Placeholder implementation
    }
    
    private fun analyzeOptimalChargingPattern(): OptimalChargingPattern {
        val chargingData = batteryHistoryData.filter { it.isCharging }
        
        if (chargingData.isEmpty()) {
            return OptimalChargingPattern(
                recommendedStartLevel = 20,
                recommendedEndLevel = 80,
                optimalChargingSpeed = ChargingSpeed.NORMAL,
                bestChargingTimes = emptyList()
            )
        }
        
        // Analyze when users typically charge
        val chargingHours = chargingData.groupBy { 
            java.util.Calendar.getInstance().apply { 
                timeInMillis = it.timestamp 
            }.get(java.util.Calendar.HOUR_OF_DAY) 
        }
        
        val bestChargingTimes = chargingHours.entries
            .sortedByDescending { it.value.size }
            .take(3)
            .map { "${it.key}:00 - ${it.key + 1}:00" }
        
        return OptimalChargingPattern(
            recommendedStartLevel = 20,
            recommendedEndLevel = 80,
            optimalChargingSpeed = ChargingSpeed.NORMAL,
            bestChargingTimes = bestChargingTimes
        )
    }
    
    private fun analyzeTemperaturePatterns(): TemperatureAnalysis {
        val temperatures = batteryHistoryData.map { it.temperature }
        
        return TemperatureAnalysis(
            averageTemperature = temperatures.average().toFloat(),
            maxTemperature = temperatures.maxOrNull() ?: 0f,
            minTemperature = temperatures.minOrNull() ?: 0f,
            temperatureVariation = temperatures.let { temps ->
                val avg = temps.average()
                sqrt(temps.map { (it - avg).pow(2) }.average()).toFloat()
            },
            overheatingEvents = temperatures.count { it > 45f },
            coolingRecommendation = if (temperatures.average() > 35f) {
                "Consider using device in cooler environment"
            } else {
                "Temperature levels are normal"
            }
        )
    }
    
    private fun generateUsageRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val healthScore = calculateBatteryHealthScore()
        val avgTemp = batteryHistoryData.map { it.temperature }.average().toFloat()
        val cycleCount = calculateCycleCount()
        
        when {
            healthScore < 50f -> {
                recommendations.add("⚠️ Battery health is declining. Consider professional inspection.")
                recommendations.add("🔄 Replace battery if device performance is significantly affected.")
            }
            healthScore < 70f -> {
                recommendations.add("📱 Avoid deep discharges (below 20%)")
                recommendations.add("🔋 Try to keep battery between 20-80%")
            }
            else -> {
                recommendations.add("✅ Battery health is good. Continue current usage patterns.")
            }
        }
        
        if (avgTemp > 35f) {
            recommendations.add("🌡️ Device running warm. Remove case during charging.")
            recommendations.add("❄️ Avoid direct sunlight and hot environments.")
        }
        
        if (cycleCount > 500) {
            recommendations.add("🔄 High cycle count detected. Monitor battery performance closely.")
        }
        
        return recommendations
    }
    
    private fun calculatePredictionAccuracy(): Float {
        // Simplified prediction accuracy calculation
        // In production, this would compare actual vs predicted battery life
        return 85f // Placeholder
    }
    
    private fun predictMaintenanceDate(): Long {
        val healthScore = calculateBatteryHealthScore()
        val degradationRate = calculateDegradationRate()
        
        // Predict when battery health will drop below 80%
        val daysUntilMaintenance = if (degradationRate > 0) {
            ((healthScore - 80f) / degradationRate) * 30 // Rough estimate
        } else {
            365 // Default to 1 year if no degradation detected
        }
        
        return System.currentTimeMillis() + (daysUntilMaintenance.toLong() * 24 * 60 * 60 * 1000)
    }
    
    private fun analyzePowerConsumption(): PowerConsumptionTrends {
        val consumptionData = batteryHistoryData.filter { !it.isCharging }
        
        if (consumptionData.isEmpty()) {
            return PowerConsumptionTrends(
                averagePowerDraw = 0f,
                peakConsumptionHours = emptyList(),
                lowConsumptionHours = emptyList(),
                screenOnPowerDraw = 0f,
                screenOffPowerDraw = 0f
            )
        }
        
        // Calculate power consumption trends
        val hourlyConsumption = consumptionData.groupBy { 
            java.util.Calendar.getInstance().apply { 
                timeInMillis = it.timestamp 
            }.get(java.util.Calendar.HOUR_OF_DAY) 
        }
        
        val avgConsumption = hourlyConsumption.values.map { hourData ->
            hourData.sumOf { abs(it.current) } / hourData.size
        }.average().toFloat()
        
        return PowerConsumptionTrends(
            averagePowerDraw = avgConsumption,
            peakConsumptionHours = hourlyConsumption.entries
                .sortedByDescending { it.value.sumOf { data -> abs(data.current) } }
                .take(3)
                .map { "${it.key}:00" },
            lowConsumptionHours = hourlyConsumption.entries
                .sortedBy { it.value.sumOf { data -> abs(data.current) } }
                .take(3)
                .map { "${it.key}:00" },
            screenOnPowerDraw = avgConsumption * 1.5f, // Estimated
            screenOffPowerDraw = avgConsumption * 0.3f // Estimated
        )
    }
    
    private fun calculateChargingEfficiency(): Float {
        val chargingSessions = findFullChargingSessions()
        
        if (chargingSessions.isEmpty()) return 90f // Default
        
        val avgEfficiency = chargingSessions.map { session ->
            // Simplified efficiency calculation
            val energyIn = session.averageCurrent * (session.duration / 3600000f) // Wh
            val energyStored = (session.endLevel - session.startLevel) * 0.01f * 3000 * 3.7f / 1000f // Wh
            
            if (energyIn > 0) (energyStored / energyIn) * 100f else 90f
        }.average().toFloat()
        
        return avgEfficiency.coerceIn(50f, 100f)
    }
    
    private fun findFullChargingSessions(): List<ChargingSession> {
        val sessions = mutableListOf<ChargingSession>()
        var sessionStart: BatteryDataPoint? = null
        
        batteryHistoryData.forEach { dataPoint ->
            if (dataPoint.isCharging && sessionStart == null) {
                sessionStart = dataPoint
            } else if (!dataPoint.isCharging && sessionStart != null) {
                val start = sessionStart!!
                if (dataPoint.batteryLevel - start.batteryLevel >= 20) { // Significant charge
                    sessions.add(
                        ChargingSession(
                            startTime = start.timestamp,
                            endTime = dataPoint.timestamp,
                            startLevel = start.batteryLevel,
                            endLevel = dataPoint.batteryLevel,
                            duration = dataPoint.timestamp - start.timestamp,
                            averageCurrent = batteryHistoryData
                                .filter { it.timestamp >= start.timestamp && it.timestamp <= dataPoint.timestamp }
                                .map { it.current }
                                .average()
                                .toInt()
                        )
                    )
                }
                sessionStart = null
            }
        }
        
        return sessions
    }
    
    // ML-powered battery life prediction
    fun predictBatteryLife(currentLevel: Int): BatteryLifePrediction {
        val recentConsumption = batteryHistoryData
            .takeLast(50)
            .filter { !it.isCharging }
            .zipWithNext { a, b -> 
                if (a.batteryLevel > b.batteryLevel) a.batteryLevel - b.batteryLevel else 0
            }
            .filter { it > 0 }
            .average()
        
        val avgConsumptionPerHour = if (recentConsumption > 0) recentConsumption else 5.0 // Default 5%/hour
        val hoursRemaining = currentLevel / avgConsumptionPerHour
        
        return BatteryLifePrediction(
            estimatedHoursRemaining = hoursRemaining,
            confidenceScore = if (batteryHistoryData.size > 100) 0.85f else 0.6f,
            basedOnUsagePattern = true,
            factors = listOf(
                "Based on last ${batteryHistoryData.takeLast(50).size} data points",
                "Average consumption: ${String.format("%.1f", avgConsumptionPerHour)}%/hour",
                "Current level: $currentLevel%"
            )
        )
    }
}

// Data classes
data class BatteryDataPoint(
    val timestamp: Long,
    val batteryLevel: Int,
    val voltage: Float,
    val temperature: Float,
    val isCharging: Boolean,
    val current: Int
)

data class BatteryAnalytics(
    val totalCycleCount: Int = 0,
    val averageChargingTime: Long = 0L,
    val averageDischargingTime: Long = 0L,
    val batteryHealthScore: Float = 100f,
    val estimatedCapacityMah: Int = 3000,
    val degradationRate: Float = 0f,
    val optimalChargingPattern: OptimalChargingPattern = OptimalChargingPattern(),
    val temperatureAnalysis: TemperatureAnalysis = TemperatureAnalysis(),
    val usageRecommendations: List<String> = emptyList(),
    val predictionAccuracy: Float = 0f,
    val nextMaintenanceDate: Long = 0L,
    val powerConsumptionTrends: PowerConsumptionTrends = PowerConsumptionTrends(),
    val chargingEfficiency: Float = 90f
)

data class OptimalChargingPattern(
    val recommendedStartLevel: Int = 20,
    val recommendedEndLevel: Int = 80,
    val optimalChargingSpeed: ChargingSpeed = ChargingSpeed.NORMAL,
    val bestChargingTimes: List<String> = emptyList()
)

data class TemperatureAnalysis(
    val averageTemperature: Float = 0f,
    val maxTemperature: Float = 0f,
    val minTemperature: Float = 0f,
    val temperatureVariation: Float = 0f,
    val overheatingEvents: Int = 0,
    val coolingRecommendation: String = ""
)

data class PowerConsumptionTrends(
    val averagePowerDraw: Float = 0f,
    val peakConsumptionHours: List<String> = emptyList(),
    val lowConsumptionHours: List<String> = emptyList(),
    val screenOnPowerDraw: Float = 0f,
    val screenOffPowerDraw: Float = 0f
)

data class ChargingSession(
    val startTime: Long,
    val endTime: Long,
    val startLevel: Int,
    val endLevel: Int,
    val duration: Long,
    val averageCurrent: Int
)

data class ChargingCycle(
    val cycleNumber: Int,
    val startDate: Long,
    val endDate: Long,
    val efficiency: Float
)

data class UsagePattern(
    val hour: Int,
    val averageConsumption: Float,
    val peakConsumption: Float
)

data class BatteryLifePrediction(
    val estimatedHoursRemaining: Double,
    val confidenceScore: Float,
    val basedOnUsagePattern: Boolean,
    val factors: List<String>
)

enum class ChargingSpeed {
    SLOW, NORMAL, FAST, SUPER_FAST
}
