package com.voltai.core.health

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class BatteryHealthAnalyzer @Inject constructor(
    private val context: Context
) {
    fun analyzeBatteryHealth(): BatteryHealthData {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val temperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        
        val batteryPercentage = (level * 100) / scale.coerceAtLeast(1)
        val tempCelsius = temperature / 10.0f
        val voltageFloat = voltage / 1000.0f
        
        val healthStatus = mapHealthStatus(health)
        val degradation = calculateDegradation(health, tempCelsius, voltageFloat)
        val recommendations = generateHealthRecommendations(healthStatus, tempCelsius, degradation)
        
        return BatteryHealthData(
            healthPercentage = mapHealthToPercentage(health),
            healthStatus = healthStatus,
            degradationLevel = degradation,
            temperature = tempCelsius,
            voltage = voltageFloat,
            cycleCount = estimateCycleCount(health, degradation),
            capacity = batteryPercentage,
            recommendations = recommendations,
            lastUpdate = System.currentTimeMillis()
        )
    }
    
    private fun mapHealthStatus(health: Int): HealthStatus {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> HealthStatus.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> HealthStatus.OVERHEATING
            BatteryManager.BATTERY_HEALTH_DEAD -> HealthStatus.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> HealthStatus.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> HealthStatus.UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> HealthStatus.COLD
            else -> HealthStatus.UNKNOWN
        }
    }
    
    private fun mapHealthToPercentage(health: Int): Int {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 100
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 75
            BatteryManager.BATTERY_HEALTH_DEAD -> 0
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 60
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 50
            BatteryManager.BATTERY_HEALTH_COLD -> 80
            else -> 95
        }
    }
    
    private fun calculateDegradation(health: Int, temperature: Float, voltage: Float): Float {
        var degradation = 0.0f
        
        // Health-based degradation
        degradation += when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 0.0f
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 0.25f
            BatteryManager.BATTERY_HEALTH_DEAD -> 1.0f
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 0.4f
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 0.5f
            BatteryManager.BATTERY_HEALTH_COLD -> 0.2f
            else -> 0.05f
        }
        
        // Temperature-based degradation
        if (temperature > 40) degradation += 0.1f
        if (temperature > 45) degradation += 0.2f
        
        // Voltage-based degradation (assuming 3.7V nominal)
        val voltageDiff = abs(voltage - 3.7f)
        if (voltageDiff > 0.5f) degradation += 0.1f
        
        return degradation.coerceIn(0.0f, 1.0f)
    }
    
    private fun estimateCycleCount(health: Int, degradation: Float): Int {
        // Rough estimation based on health and degradation
        val baseCycles = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 0
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 500
            BatteryManager.BATTERY_HEALTH_DEAD -> 2000
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 800
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 1000
            BatteryManager.BATTERY_HEALTH_COLD -> 300
            else -> 100
        }
        
        return (baseCycles + (degradation * 1000).toInt()).coerceIn(0, 3000)
    }
    
    private fun generateHealthRecommendations(
        status: HealthStatus,
        temperature: Float,
        degradation: Float
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        when (status) {
            HealthStatus.GOOD -> {
                if (degradation > 0.3f) {
                    recommendations.add("Battery showing signs of wear - consider calibration")
                }
            }
            HealthStatus.OVERHEATING -> {
                recommendations.add("Battery overheating - stop charging immediately")
                recommendations.add("Remove from heat source and allow cooling")
            }
            HealthStatus.DEAD -> {
                recommendations.add("Battery is dead - replacement required")
            }
            HealthStatus.OVER_VOLTAGE -> {
                recommendations.add("Over-voltage detected - stop charging")
                recommendations.add("Check charger compatibility")
            }
            HealthStatus.COLD -> {
                recommendations.add("Battery too cold - warm to room temperature")
            }
            HealthStatus.UNSPECIFIED_FAILURE -> {
                recommendations.add("Battery system failure detected")
                recommendations.add("Consider professional diagnosis")
            }
            HealthStatus.UNKNOWN -> {
                recommendations.add("Unable to determine battery health")
            }
        }
        
        if (temperature > 40) {
            recommendations.add("High temperature detected - reduce usage")
        }
        
        if (degradation > 0.5f) {
            recommendations.add("Significant battery degradation - consider replacement")
        }
        
        return recommendations
    }
}

data class BatteryHealthData(
    val healthPercentage: Int,
    val healthStatus: HealthStatus,
    val degradationLevel: Float,
    val temperature: Float,
    val voltage: Float,
    val cycleCount: Int,
    val capacity: Int,
    val recommendations: List<String>,
    val lastUpdate: Long
)

enum class HealthStatus {
    GOOD,
    OVERHEATING,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN
}
