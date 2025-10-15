package com.voltai.smartbatteryguardian.data.battery

import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.data.database.dao.BatteryDao
import com.voltai.smartbatteryguardian.data.database.entities.BatteryReading
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Single Source of Truth for all battery-related data and calculations
 * Centralizes battery monitoring, capacity detection, and intelligent predictions
 */
@Singleton
class BatteryDataManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val batteryDao: BatteryDao,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private val _batteryData = MutableStateFlow(createInitialBatteryData())
    val batteryData: StateFlow<BatteryData> = _batteryData.asStateFlow()

    // Battery capacity tracking
    private var designCapacityMah: Int = 0
    private var currentCapacityMah: Int = 0

    // Charging/discharging tracking
    private var chargingStartTime: Long = 0
    private var chargingStartLevel: Int = 0
    private var dischargingStartTime: Long = 0
    private var dischargingStartLevel: Int = 0

    // Historical tracking
    private val recentReadings = mutableListOf<BatteryReading>()
    private var batteryReceiver: BroadcastReceiver? = null

    init {
        detectBatteryCapacity()
        startMonitoring()
        loadHistoricalData()
    }

    /**
     * Detect actual battery capacity using multiple methods
     */
    private fun detectBatteryCapacity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Method 1: Get charge counter (current capacity)
                val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                if (chargeCounterUah > 0) {
                    currentCapacityMah = chargeCounterUah / 1000
                }

                // Method 2: Get energy counter (if available - API 28+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val energyCounterNwh =
                        batteryManager.getLongProperty(
                            BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER,
                        )
                    if (energyCounterNwh > 0) {
                        // Convert nWh to mAh (assuming 3.7V nominal)
                        val capacityFromEnergy = (energyCounterNwh / 1000000 / 3.7).toInt()
                        if (capacityFromEnergy > 0 && currentCapacityMah > 0) {
                            currentCapacityMah = (currentCapacityMah + capacityFromEnergy) / 2
                        }
                    }
                }

                // Estimate design capacity from current charge
                val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

                if (level > 0 && scale > 0 && currentCapacityMah > 0) {
                    val percentage = (level * 100) / scale
                    if (percentage > 10) { // More accurate when battery is not too low
                        designCapacityMah = (currentCapacityMah * 100) / percentage
                    }
                }

                // Fallback to common capacities if detection fails
                if (designCapacityMah == 0) {
                    designCapacityMah = estimateCapacityByDevice()
                }
            } catch (e: Exception) {
                designCapacityMah = estimateCapacityByDevice()
            }
        } else {
            designCapacityMah = estimateCapacityByDevice()
        }
    }

    /**
     * Estimate battery capacity based on device model
     */
    private fun estimateCapacityByDevice(): Int {
        val model = Build.MODEL.lowercase(Locale.ROOT)
        return when {
            model.contains("pixel 8") -> 4575
            model.contains("pixel 7") -> 4355
            model.contains("pixel 6") -> 4614
            model.contains("samsung galaxy s24") -> 4000
            model.contains("samsung galaxy s23") -> 3900
            model.contains("samsung galaxy s22") -> 3700
            model.contains("oneplus 12") -> 5400
            model.contains("oneplus 11") -> 5000
            model.contains("xiaomi 14") -> 4610
            model.contains("iphone") -> 3200
            else -> 4000 // Default estimate
        }
    }

    private fun startMonitoring() {
        val filter =
            IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }

        batteryReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?,
                ) {
                    intent?.let { updateBatteryData(it) }
                }
            }

        context.registerReceiver(batteryReceiver, filter)

        // Initial update
        val initialIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        initialIntent?.let { updateBatteryData(it) }
    }

    private fun loadHistoricalData() {
        scope.launch {
            try {
                // Load last 24 hours of readings
                val now = System.currentTimeMillis()
                val oneDayAgo = now - (24 * 60 * 60 * 1000)
                val readings = batteryDao.getReadingsInRange(oneDayAgo, now)

                recentReadings.clear()
                recentReadings.addAll(readings.takeLast(100))
            } catch (e: Exception) {
                // Silent fail - no historical data available
            }
        }
    }

    private fun updateBatteryData(intent: Intent) {
        // Extract raw battery data
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPercentage =
            if (level != -1 && scale != -1) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                0
            }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargingStatus =
            when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                else -> "Unknown"
            }

        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0

        val current =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val ua = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    if (ua == Int.MIN_VALUE) 0 else ua / 1000 // Convert to mA
                } catch (e: Exception) {
                    0
                }
            } else {
                0
            }

        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val healthPercentage = calculateHealthPercentage(health)

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val powerSource =
            when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Battery"
            }

        // Track charging/discharging sessions
        trackChargingSession(isCharging, batteryPercentage)

        // Calculate intelligent metrics
        val remainingTime = calculateAccurateRemainingTime(batteryPercentage, isCharging, current)
        val chargingSpeed = determineChargingSpeed(current, voltage, isCharging)
        val capacity = if (designCapacityMah > 0) "$designCapacityMah mAh" else "Unknown"
        val cycleCount = estimateCycleCount()
        val screenTime = getScreenTime()
        val batteryUsedPercent = "${100 - batteryPercentage}%"
        val powerConsumption = calculatePowerConsumption(current, voltage)
        val estimatedLife = calculateEstimatedLife(batteryPercentage, isCharging, current)
        val batteryScore = calculateBatteryScore(healthPercentage, temperature, batteryPercentage)
        val recommendations = generateRecommendations(batteryPercentage, temperature, healthPercentage, isCharging)

        // Save to database (async)
        saveReading(batteryPercentage, temperature, voltage, current, isCharging)

        // Update state
        _batteryData.value =
            _batteryData.value.copy(
                batteryLevel = batteryPercentage,
                isCharging = isCharging,
                chargingStatus = chargingStatus,
                remainingTime = remainingTime,
                batteryUsed = batteryUsedPercent,
                temperature = temperature,
                voltage = voltage,
                current = current,
                healthPercentage = healthPercentage,
                capacity = capacity,
                cycleCount = cycleCount,
                screenOnTime = screenTime,
                powerConsumption = powerConsumption,
                chargingSpeed = chargingSpeed,
                estimatedLife = estimatedLife,
                powerSource = powerSource,
                batteryScore = batteryScore,
                recommendations = recommendations,
                lastUpdated = System.currentTimeMillis(),
            )
    }

    /**
     * Calculate accurate remaining time using historical data and current trend
     */
    private fun calculateAccurateRemainingTime(
        level: Int,
        isCharging: Boolean,
        current: Int,
    ): String {
        if (current == 0) {
            // Use historical average if current is not available
            return calculateRemainingTimeFromHistory(level, isCharging)
        }

        val currentMa = abs(current)

        return if (isCharging) {
            val remainingPercent = 100 - level
            if (remainingPercent <= 0) return "Full"

            // For charging, use actual current reading with safety margins
            // Average charging typically slows down above 80%
            val effectiveCurrent = if (level > 80) currentMa * 0.6f else currentMa.toFloat()

            if (effectiveCurrent < 100) return "Calculating..."

            val hoursToFull = (remainingPercent * (designCapacityMah / 100.0f)) / effectiveCurrent
            formatTime(hoursToFull)
        } else {
            // Discharging
            if (level <= 0) return "0h 0m"

            // Use average discharge rate from recent history for better accuracy
            val avgDischargeRate = calculateAverageDischargeRate() ?: currentMa.toFloat()

            if (avgDischargeRate < 50) return "Calculating..."

            val hoursToDead = (level * (designCapacityMah / 100.0f)) / avgDischargeRate
            formatTime(hoursToDead)
        }
    }

    /**
     * Calculate remaining time from historical patterns
     */
    private fun calculateRemainingTimeFromHistory(
        level: Int,
        isCharging: Boolean,
    ): String {
        if (recentReadings.size < 2) return "Calculating..."

        // Find recent trend
        val recentTrend = recentReadings.takeLast(10)
        if (recentTrend.isEmpty()) return "Calculating..."

        val chargingReadings = recentTrend.filter { it.current > 0 }
        val dischargingReadings = recentTrend.filter { it.current < 0 }

        return if (isCharging) {
            if (chargingReadings.size < 2) {
                "Calculating..."
            } else {
                val avgRate = chargingReadings.map { abs(it.current) }.average().toFloat()
                val remainingPercent = 100 - level
                val hours = (remainingPercent * (designCapacityMah / 100.0f)) / avgRate.coerceAtLeast(100f)
                formatTime(hours)
            }
        } else {
            if (dischargingReadings.size < 2) {
                "Calculating..."
            } else {
                val avgRate = dischargingReadings.map { abs(it.current) }.average().toFloat()
                val hours = (level * (designCapacityMah / 100.0f)) / avgRate.coerceAtLeast(50f)
                formatTime(hours)
            }
        }
    }

    private fun calculateAverageDischargeRate(): Float? {
        val dischargingReadings = recentReadings.filter { it.current < 0 }.takeLast(20)
        return if (dischargingReadings.size >= 3) {
            dischargingReadings.map { abs(it.current).toFloat() }.average().toFloat()
        } else {
            null
        }
    }

    private fun formatTime(hours: Float): String {
        if (hours.isNaN() || hours.isInfinite()) return "Calculating..."

        val h = hours.toInt()
        val m = ((hours - h) * 60).toInt()

        return when {
            h >= 100 -> "> 4 days"
            h >= 48 -> "${h / 24} days"
            h >= 24 -> "1 day ${h % 24}h"
            h > 0 -> "${h}h ${m}m"
            m > 0 -> "${m}m"
            else -> "< 1m"
        }
    }

    private fun trackChargingSession(
        isCharging: Boolean,
        level: Int,
    ) {
        val now = System.currentTimeMillis()

        if (isCharging && chargingStartTime == 0L) {
            // Start of charging session
            chargingStartTime = now
            chargingStartLevel = level
        } else if (!isCharging && chargingStartTime != 0L) {
            // End of charging session
            chargingStartTime = 0L
        }

        if (!isCharging && dischargingStartTime == 0L) {
            // Start of discharging session
            dischargingStartTime = now
            dischargingStartLevel = level
        } else if (isCharging && dischargingStartTime != 0L) {
            // End of discharging session - this is a charge cycle
            dischargingStartTime = 0L
        }
    }

    private fun estimateCycleCount(): Int {
        // Estimate from database readings
        // A cycle = discharge from 100% to 0% (or equivalent partial cycles)
        return try {
            // This should be calculated from historical data
            // For now, return 0 as placeholder
            0
        } catch (e: Exception) {
            0
        }
    }

    private fun determineChargingSpeed(
        current: Int,
        voltage: Double,
        isCharging: Boolean,
    ): String {
        if (!isCharging) return "Not Charging"

        val power = (abs(current) / 1000.0) * voltage // Watts

        return when {
            power >= 20.0 -> "Super Fast (${power.toInt()}W)"
            power >= 15.0 -> "Fast (${power.toInt()}W)"
            power >= 10.0 -> "Quick (${power.toInt()}W)"
            power >= 5.0 -> "Normal (${power.toInt()}W)"
            power > 0 -> "Slow (${power.toInt()}W)"
            else -> "Not Charging"
        }
    }

    private fun calculateHealthPercentage(health: Int): Int {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 100
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 85
            BatteryManager.BATTERY_HEALTH_DEAD -> 0
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 75
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 60
            BatteryManager.BATTERY_HEALTH_COLD -> 90
            else -> 80
        }
    }

    private fun calculatePowerConsumption(
        current: Int,
        voltage: Double,
    ): Double {
        return (abs(current).toDouble() / 1000.0) * voltage
    }

    private fun calculateEstimatedLife(
        level: Int,
        isCharging: Boolean,
        current: Int,
    ): String {
        return if (!isCharging && current < 0) {
            calculateAccurateRemainingTime(level, false, current)
        } else {
            "Charging"
        }
    }

    private fun calculateBatteryScore(
        health: Int,
        temperature: Float,
        level: Int,
    ): Int {
        var score = health

        when {
            temperature > 45 -> score -= 20
            temperature > 40 -> score -= 10
            temperature < 0 -> score -= 15
        }

        when {
            level < 15 -> score -= 5
            level > 95 -> score -= 3
        }

        return score.coerceIn(0, 100)
    }

    private fun generateRecommendations(
        level: Int,
        temperature: Float,
        health: Int,
        isCharging: Boolean,
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (temperature > 40) {
            recommendations.add("Battery is hot. Remove case and stop charging if possible.")
        }
        if (temperature < 10) {
            recommendations.add("Battery is cold. Warm up device for optimal performance.")
        }
        if (level < 20 && !isCharging) {
            recommendations.add("Battery low. Consider charging soon.")
        }
        if (level > 90 && isCharging) {
            recommendations.add("Battery nearly full. Unplug to preserve battery health.")
        }
        if (health < 80) {
            recommendations.add("Battery health is degraded. Consider battery replacement.")
        }

        return recommendations
    }

    private fun getScreenTime(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startTime = calendar.timeInMillis
                val endTime = System.currentTimeMillis()

                val usageStats =
                    usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        endTime,
                    )

                var totalScreenTime = 0L
                for (usageStat in usageStats) {
                    totalScreenTime += usageStat.totalTimeInForeground
                }

                val hours = totalScreenTime / (1000 * 60 * 60)
                val minutes = (totalScreenTime % (1000 * 60 * 60)) / (1000 * 60)
                "${hours}h ${minutes}m"
            } else {
                "0h 0m"
            }
        } catch (e: Exception) {
            "0h 0m"
        }
    }

    private fun saveReading(
        level: Int,
        temperature: Float,
        voltage: Double,
        current: Int,
        isCharging: Boolean,
    ) {
        scope.launch {
            try {
                val reading =
                    BatteryReading(
                        timestamp = System.currentTimeMillis(),
                        batteryLevel = level,
                        temperature = temperature.toDouble(),
                        voltage = voltage,
                        current = current.toDouble(),
                        isCharging = isCharging,
                        capacity = if (designCapacityMah > 0) designCapacityMah.toDouble() else 0.0,
                        chargingType = if (isCharging) "AC" else null,
                        health = "Good",
                        technology = "Li-ion",
                        plugType = if (isCharging) "AC" else null,
                        powerSource = if (isCharging) "AC" else "Battery",
                        chargingSpeed = if (isCharging) (abs(current) / 1000.0) * voltage else 0.0,
                        estimatedTimeRemaining = null,
                        cycleCount = null,
                        wearLevel = null,
                    )
                batteryDao.insertReading(reading)

                // Update in-memory cache
                recentReadings.add(reading)
                if (recentReadings.size > 100) {
                    recentReadings.removeAt(0)
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }

    private fun createInitialBatteryData(): BatteryData {
        return BatteryData(
            batteryLevel = 0,
            isCharging = false,
            chargingStatus = "Unknown",
            remainingTime = "Calculating...",
            batteryUsed = "0%",
            temperature = 25.0f,
            voltage = 3.7,
            current = 0,
            healthPercentage = 100,
            capacity = "Unknown",
            cycleCount = 0,
            screenOnTime = "0h 0m",
            screenOffTime = "24h 0m",
            powerConsumption = 0.0,
            chargingSpeed = "Not Charging",
            estimatedLife = "Unknown",
            powerSource = "Battery",
            batteryScore = 100,
            recommendations = emptyList(),
            lastUpdated = System.currentTimeMillis(),
        )
    }

    fun cleanup() {
        batteryReceiver?.let {
            context.unregisterReceiver(it)
        }
    }
}
