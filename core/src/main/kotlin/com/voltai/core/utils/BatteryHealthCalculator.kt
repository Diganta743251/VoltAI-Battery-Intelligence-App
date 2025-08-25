package com.voltai.core.utils

import kotlin.math.max

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
}

enum class HealthStatus(val displayName: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor"),
    CRITICAL("Critical")
}