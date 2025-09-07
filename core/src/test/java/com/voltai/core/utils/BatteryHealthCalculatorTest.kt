package com.voltai.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BatteryHealthCalculatorTest {

    @Test
    fun `calculateHealthScore returns 100 for new battery conditions`() {
        // Arrange
        val voltage = 4.2f
        val temperature = 25f
        val cycleCount = 0
        
        // Act
        val healthScore = BatteryHealthCalculator.calculateHealthScore(
            voltage = voltage,
            temperature = temperature,
            cycleCount = cycleCount
        )
        
        // Assert
        assertThat(healthScore).isEqualTo(100)
    }
    
    @Test
    fun `calculateHealthScore decreases with high temperature`() {
        // Arrange
        val voltage = 4.2f
        val normalTemp = 25f
        val highTemp = 45f
        val cycleCount = 0
        
        // Act
        val normalTempScore = BatteryHealthCalculator.calculateHealthScore(voltage, normalTemp, cycleCount)
        val highTempScore = BatteryHealthCalculator.calculateHealthScore(voltage, highTemp, cycleCount)
        
        // Assert
        assertThat(highTempScore).isLessThan(normalTempScore)
    }
    
    @Test
    fun `calculateHealthScore decreases with low voltage`() {
        // Arrange
        val normalVoltage = 4.2f
        val lowVoltage = 3.5f
        val temperature = 25f
        val cycleCount = 0
        
        // Act
        val normalVoltageScore = BatteryHealthCalculator.calculateHealthScore(normalVoltage, temperature, cycleCount)
        val lowVoltageScore = BatteryHealthCalculator.calculateHealthScore(lowVoltage, temperature, cycleCount)
        
        // Assert
        assertThat(lowVoltageScore).isLessThan(normalVoltageScore)
    }
    
    @Test
    fun `calculateHealthScore decreases with high cycle count`() {
        // Arrange
        val voltage = 4.2f
        val temperature = 25f
        val lowCycleCount = 100
        val highCycleCount = 1000
        
        // Act
        val lowCycleScore = BatteryHealthCalculator.calculateHealthScore(voltage, temperature, lowCycleCount)
        val highCycleScore = BatteryHealthCalculator.calculateHealthScore(voltage, temperature, highCycleCount)
        
        // Assert
        assertThat(highCycleScore).isLessThan(lowCycleScore)
    }
    
    @Test
    fun `calculateHealthScore never returns negative values`() {
        // Arrange - extreme conditions
        val voltage = 3.0f
        val temperature = 60f
        val cycleCount = 5000
        
        // Act
        val healthScore = BatteryHealthCalculator.calculateHealthScore(voltage, temperature, cycleCount)
        
        // Assert
        assertThat(healthScore).isAtLeast(0)
    }
    
    @Test
    fun `calculateHealthScore never exceeds 100`() {
        // Arrange - perfect conditions
        val voltage = 4.5f
        val temperature = 20f
        val cycleCount = 0
        
        // Act
        val healthScore = BatteryHealthCalculator.calculateHealthScore(voltage, temperature, cycleCount)
        
        // Assert
        assertThat(healthScore).isAtMost(100)
    }
    
    @Test
    fun `estimateBatteryLifespan returns reasonable estimates`() {
        // Arrange
        val goodHealthScore = 90
        val poorHealthScore = 30
        
        // Act
        val goodLifespan = BatteryHealthCalculator.estimateBatteryLifespan(goodHealthScore)
        val poorLifespan = BatteryHealthCalculator.estimateBatteryLifespan(poorHealthScore)
        
        // Assert
        assertThat(goodLifespan).isGreaterThan(poorLifespan)
        assertThat(goodLifespan).isAtLeast(1) // At least 1 year
        assertThat(poorLifespan).isAtLeast(0) // Could be immediate replacement needed
    }
    
    @Test
    fun `getHealthRecommendations provides appropriate advice`() {
        // Arrange
        val excellentHealth = 95
        val goodHealth = 75
        val poorHealth = 40
        val criticalHealth = 15
        
        // Act
        val excellentRecommendations = BatteryHealthCalculator.getHealthRecommendations(excellentHealth)
        val goodRecommendations = BatteryHealthCalculator.getHealthRecommendations(goodHealth)
        val poorRecommendations = BatteryHealthCalculator.getHealthRecommendations(poorHealth)
        val criticalRecommendations = BatteryHealthCalculator.getHealthRecommendations(criticalHealth)
        
        // Assert
        assertThat(excellentRecommendations).isNotEmpty()
        assertThat(goodRecommendations).isNotEmpty()
        assertThat(poorRecommendations).isNotEmpty()
        assertThat(criticalRecommendations).isNotEmpty()
        
        // Critical health should have more urgent recommendations
        assertThat(criticalRecommendations.size).isGreaterThan(excellentRecommendations.size)
    }
}
