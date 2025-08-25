package com.voltai.domain.model

data class AppUsage(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val estimatedBatteryDrain: Double
)
