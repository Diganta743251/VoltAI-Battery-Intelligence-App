package com.voltai.core.ai

import com.voltai.domain.model.BatteryLog
import org.junit.Assert.*
import org.junit.Test

class RuleBasedCoachTest {

    @Test
    fun negative_current_indicates_charging() {
        val log = BatteryLog(
            timestamp = System.currentTimeMillis(),
            batteryLevel = 50,
            voltage = 3.9f,
            temperature = 32f,
            chargingStatus = "",
            chargingType = "",
            current = -200f
        )
        assertTrue(log.current < 0f)
    }

    @Test
    fun positive_current_indicates_discharging() {
        val log = BatteryLog(
            timestamp = System.currentTimeMillis(),
            batteryLevel = 50,
            voltage = 3.9f,
            temperature = 32f,
            chargingStatus = "",
            chargingType = "",
            current = 80f
        )
        assertTrue(log.current > 0f)
    }

    @Test
    fun zero_current_is_neutral() {
        val log = BatteryLog(
            timestamp = System.currentTimeMillis(),
            batteryLevel = 50,
            voltage = 3.9f,
            temperature = 32f,
            chargingStatus = "",
            chargingType = "",
            current = 0f
        )
        assertEquals(0f, log.current, 0.001f)
    }
}