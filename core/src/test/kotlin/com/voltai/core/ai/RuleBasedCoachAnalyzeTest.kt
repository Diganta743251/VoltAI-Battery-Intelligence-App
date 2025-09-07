package com.voltai.core.ai

import com.voltai.domain.model.BatteryLog
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedCoachAnalyzeTest {

    @Test
    fun detects_overcharge_when_battery_full_and_charging() {
        val logs = listOf(
            BatteryLog(
                timestamp = System.currentTimeMillis(),
                batteryLevel = 100,
                voltage = 4.2f,
                temperature = 35f,
                chargingStatus = "",
                chargingType = "",
                current = -200f // charging by convention
            )
        )
        val tips = RuleBasedCoach.getBatteryTips(logs)
        assertTrue(tips.any { it.contains("unplug", ignoreCase = true) || it.contains("100%", ignoreCase = true) })
    }
}