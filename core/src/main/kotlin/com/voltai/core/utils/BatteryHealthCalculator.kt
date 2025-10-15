package com.voltai.core.utils

import kotlin.math.max
import kotlin.math.roundToInt

object BatteryHealthCalculator {
    
    /**
     * Calculate battery health based on multiple factors
     * Formula from design files:
     * health = baseCapacity% - (cycles × 0.05%) - (temp > 35°C ? (temp - 35) × 0.02% : 0) - (lowVoltage ? 3–5%)
     */
    fun calculateHealth(
        currentCapacity: Float,
        designCapacity: Float,
        cycleCount: Int,
        temperature: Float,
        voltage: Int,
        normalVoltage: Int = 3700 // mV
    ): Float {
        // Base capacity percentage
        val baseCapacity = (currentCapacity / designCapacity) * 100f
        
        // Cycle degradation (0.05% per cycle)
        val cycleDegradation = cycleCount * 0.05f
        
        // Temperature degradation (if temp > 35°C)
        val tempDegradation = if (temperature > 35f) {
            (temperature - 35f) * 0.02f
        } else 0f
        
        // Voltage degradation (3-5% if low voltage)
        val voltageDegradation = if (voltage < normalVoltage * 0.9) {
            if (voltage < normalVoltage * 0.8) 5f else 3f
        } else 0f
        
        // Calculate final health
        val health = baseCapacity - cycleDegradation - tempDegradation - voltageDegradation
        
        // Ensure health is between 0 and 100
        return max(0f, health.coerceAtMost(100f))
    }
    
    /**
     * Get health status based on percentage
     */
    fun getHealthStatus(healthPercentage: Float): HealthStatus {
        return when {
            healthPercentage >= 80f -> HealthStatus.EXCELLENT
            healthPercentage >= 60f -> HealthStatus.GOOD
            healthPercentage >= 40f -> HealthStatus.FAIR
            healthPercentage >= 20f -> HealthStatus.POOR
            else -> HealthStatus.CRITICAL
        }
    }
    
    /**
     * Get health color based on percentage
     */
    fun getHealthColor(healthPercentage: Float): Long {
        return when {
            healthPercentage >= 80f -> 0xFF4CAF50 // Green
            healthPercentage >= 60f -> 0xFF8BC34A // Light Green
            healthPercentage >= 40f -> 0xFFFF9800 // Orange
            healthPercentage >= 20f -> 0xFFFF5722 // Deep Orange
            else -> 0xFFF44336 // Red
        }
    }
    
    /**
     * Estimate remaining battery life in years
     */
    fun estimateRemainingLife(
        currentHealth: Float,
        cycleCount: Int,
        averageCyclesPerMonth: Float = 30f
    ): Float {
        // Assume battery is considered "end of life" at 80% health
        val targetHealth = 80f
        
        if (currentHealth <= targetHealth) return 0f
        
        val healthToLose = currentHealth - targetHealth
        val degradationPerCycle = 0.05f // From formula
        val cyclesRemaining = healthToLose / degradationPerCycle
        
        return cyclesRemaining / (averageCyclesPerMonth * 12f)
    }

    // Legacy wrappers expected by unit tests
    fun calculateHealthScore(
        voltage: Float,
        temperature: Float,
        cycleCount: Int,
    ): Int {
        // Start with perfect score
        var score = 100f
        // Cycle degradation (0.05% per cycle)
        score -= (cycleCount * 0.05f)
        // Temperature degradation for temps > 35°C
        if (temperature > 35f) {
            score -= (temperature - 35f) * 0.02f
        }
        // Voltage degradation using nominal 4.0V
        val nominalV = 4.0f
        if (voltage < nominalV * 0.9f) {
            score -= if (voltage < nominalV * 0.8f) 5f else 3f
        }
        return score.coerceIn(0f, 100f).toInt()
    }

    fun estimateBatteryLifespan(healthScore: Int): Int {
        // Simple mapping: scale 0..100 to 0..4 years (at least 0)
        val years = (healthScore / 20f) - 1f // 90 -> 3.5, 30 -> 0.5
        return years.coerceAtLeast(0f).roundToInt()
    }

    fun getHealthRecommendations(healthScore: Int): List<String> {
        return when {
            healthScore >= 90 -> listOf(
                "Keep battery between 20% and 80%",
                "Avoid extreme temperatures"
            )
            healthScore >= 70 -> listOf(
                "Reduce background app usage",
                "Use optimized charging when possible"
            )
            healthScore >= 40 -> listOf(
                "Avoid frequent full discharges",
                "Limit high-performance gaming sessions"
            )
            else -> listOf(
                "Consider battery calibration",
                "Minimize fast charging",
                "Replace battery soon if degradation impacts daily use"
            )
        }
    }
}

enum class HealthStatus(val displayName: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor"),
    CRITICAL("Critical")
}