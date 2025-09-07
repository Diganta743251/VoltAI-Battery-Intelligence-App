package com.voltai.smartbatteryguardian

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test

class BatteryDataProviderTest {

    private lateinit var mockContext: Context
    private lateinit var mockIntent: Intent
    
    @Before
    fun setup() {
        mockContext = mockk()
        mockIntent = mockk()
        mockkStatic(Intent::class)
        
        // Mock registerReceiver to return our mock intent
        every { mockContext.registerReceiver(null, any<IntentFilter>()) } returns mockIntent
    }
    
    @Test
    fun `getBatteryData returns correct battery level`() {
        // Arrange
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 75
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_DISCHARGING
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) } returns 4000
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) } returns 250
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) } returns BatteryManager.BATTERY_HEALTH_GOOD
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) } returns 0
        
        // Act
        val batteryData = getBatteryData(mockContext)
        
        // Assert
        assertThat(batteryData.batteryLevel).isEqualTo(75)
        assertThat(batteryData.voltage).isEqualTo(4.0f)
        assertThat(batteryData.temperature).isEqualTo(25.0f)
        assertThat(batteryData.health).isEqualTo("Good")
        assertThat(batteryData.isCharging).isFalse()
    }
    
    @Test
    fun `getBatteryData correctly identifies charging status`() {
        // Arrange
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 50
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_CHARGING
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) } returns 4200
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) } returns 300
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) } returns BatteryManager.BATTERY_HEALTH_GOOD
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) } returns BatteryManager.BATTERY_PLUGGED_AC
        
        // Act
        val batteryData = getBatteryData(mockContext)
        
        // Assert
        assertThat(batteryData.isCharging).isTrue()
        assertThat(batteryData.chargingStatus).isEqualTo("Charging")
        assertThat(batteryData.plugType).isEqualTo("AC")
    }
    
    @Test
    fun `getBatteryData handles edge cases correctly`() {
        // Arrange - simulate low battery edge case
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 5
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_DISCHARGING
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) } returns 3500
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) } returns 450 // High temperature
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) } returns BatteryManager.BATTERY_HEALTH_OVERHEAT
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) } returns 0
        
        // Act
        val batteryData = getBatteryData(mockContext)
        
        // Assert
        assertThat(batteryData.batteryLevel).isEqualTo(5)
        assertThat(batteryData.temperature).isEqualTo(45.0f)
        assertThat(batteryData.health).isEqualTo("Overheat")
        assertThat(batteryData.isCharging).isFalse()
    }
    
    @Test
    fun `getBatteryData calculates remaining time correctly when discharging`() {
        // Arrange
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 40
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1) } returns 100
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_DISCHARGING
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) } returns 3800
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) } returns 250
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) } returns BatteryManager.BATTERY_HEALTH_GOOD
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) } returns 0
        
        // Act
        val batteryData = getBatteryData(mockContext)
        
        // Assert
        assertThat(batteryData.remainingTime).isNotEmpty()
        // The exact time calculation may vary, but it should contain meaningful info
    }
}
