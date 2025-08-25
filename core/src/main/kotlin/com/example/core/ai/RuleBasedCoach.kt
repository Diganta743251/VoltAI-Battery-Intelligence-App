package com.voltai.core.ai

import android.os.BatteryManager
import com.voltai.data.local.BatteryLogEntity

object RuleBasedCoach {

    fun getBatteryTips(batteryLogs: List<BatteryLogEntity>): List<String> {
        val tips = mutableListOf<String>()

        if (batteryLogs.isEmpty()) {
            tips.add("Keep using the app to gather enough data for personalized tips!")
            return tips
        }

        // Tip 1: Fast drain rate
        val recentLogs = batteryLogs.take(5) // Consider last 5 logs for drain rate
        if (recentLogs.size >= 2) {
            val firstLog = recentLogs.last()
            val lastLog = recentLogs.first()
            val timeDiffHours = (lastLog.timestamp - firstLog.timestamp) / (1000.0 * 60 * 60)
            val batteryDrop = firstLog.batteryPercentage - lastLog.batteryPercentage

            if (timeDiffHours > 0 && batteryDrop / timeDiffHours > 10) { // More than 10% drop per hour
                tips.add("Your battery seems to be draining quickly. Check background apps or adjust screen brightness.")
            }
        }

        // Tip 2: Frequent overcharging (simplified: if last log is full and charging)
        val lastLog = batteryLogs.first()
        if (lastLog.batteryPercentage == 100 && lastLog.status == BatteryManager.BATTERY_STATUS_FULL) {
            tips.add("Consider unplugging your device once it reaches 100% to prolong battery health.")
        }

        // Tip 3: High temperature events (simplified: if last log temperature is high)
        if (lastLog.temperature > 40) { // Above 40°C
            tips.add("Your device temperature is high. Avoid using it while charging or in direct sunlight.")
        }

        if (tips.isEmpty()) {
            tips.add("Your battery health looks good! Keep up the good habits.")
        }

        return tips
    }
}
