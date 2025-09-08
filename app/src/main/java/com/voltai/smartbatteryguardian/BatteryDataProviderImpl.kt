package com.voltai.smartbatteryguardian

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.math.abs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

/**
 * Real implementation of BatteryDataProvider that monitors actual device battery
 */
@Singleton
class BatteryDataProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BatteryDataProvider {

    private val _batteryData = MutableStateFlow(createInitialBatteryData())
    override val batteryData: StateFlow<BatteryData> = _batteryData.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null
    private var previousLevel = -1
    private var chargingStartTime = 0L
    private val batteryHistory = mutableListOf<Float>()

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { updateBatteryData(it) }
            }
        }

        context.registerReceiver(batteryReceiver, filter)
        
        // Initial battery state query
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryStatus?.let { updateBatteryData(it) }
    }

    private fun updateBatteryData(intent: Intent) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // Basic battery info
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPercentage = if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else 0

        // Charging status
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargingStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            else -> "Unknown"
        }

        // Temperature (in tenths of degrees Celsius)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f

        // Voltage (in millivolts)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0

        // Current (requires API 21+)
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } else 0

        // Health
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val healthPercentage = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 100
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 85
            BatteryManager.BATTERY_HEALTH_DEAD -> 0
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 75
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 60
            BatteryManager.BATTERY_HEALTH_COLD -> 90
            else -> 80
        }

        // Capacity (requires API 21+)
        val capacity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val designCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            if (designCapacity > 0) "${designCapacity / 1000} mAh" else "Unknown"
        } else "Unknown"

        // Power source
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val powerSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery"
        }

        // Track battery history for analytics
        updateBatteryHistory(batteryPercentage.toFloat())

        // Calculate remaining time estimate
        val remainingTime = calculateRemainingTime(batteryPercentage, isCharging, abs(current))

        // Update state
        _batteryData.value = _batteryData.value.copy(
            batteryLevel = batteryPercentage,
            isCharging = isCharging,
            chargingStatus = chargingStatus,
            remainingTime = remainingTime,
            temperature = temperature,
            voltage = voltage,
            current = current,
            healthPercentage = healthPercentage,
            capacity = capacity,
            powerSource = powerSource,
            chargingSpeed = determineChargingSpeed(current),
            dailyUsagePattern = batteryHistory.takeLast(24),
            batteryScore = calculateBatteryScore(healthPercentage, temperature, batteryPercentage),
            lastUpdated = System.currentTimeMillis()
        )

        previousLevel = batteryPercentage
    }

    private fun updateBatteryHistory(level: Float) {
        batteryHistory.add(level)
        // Keep only last 24 hours of data (assuming updates every hour)
        if (batteryHistory.size > 24) {
            batteryHistory.removeAt(0)
        }
    }

    private fun calculateRemainingTime(level: Int, isCharging: Boolean, current: Int): String {
        if (current == 0) return "Unknown"
        
        return if (isCharging) {
            val remainingCapacity = 100 - level
            val hours = remainingCapacity * 60 / (current / 1000f).coerceAtLeast(500f)
            "${hours.toInt()}h ${((hours % 1) * 60).toInt()}m"
        } else {
            val hours = level * 60 / (abs(current) / 1000f).coerceAtLeast(200f)
            "${hours.toInt()}h ${((hours % 1) * 60).toInt()}m"
        }
    }

    private fun determineChargingSpeed(current: Int): String {
        val currentMa = abs(current)
        return when {
            currentMa > 3000 -> "Fast Charging"
            currentMa > 1500 -> "Quick Charging"
            currentMa > 500 -> "Normal"
            currentMa > 0 -> "Slow"
            else -> "Not Charging"
        }
    }

    private fun calculateBatteryScore(health: Int, temperature: Float, level: Int): Int {
        var score = 100
        
        // Health factor
        score = (score * health / 100.0).toInt()
        
        // Temperature factor
        when {
            temperature > 45 -> score -= 20
            temperature > 35 -> score -= 10
            temperature < 0 -> score -= 15
        }
        
        // Level factor (very low or very high levels affect score)
        when {
            level < 20 -> score -= 5
            level > 95 -> score -= 3
        }
        
        return score.coerceIn(0, 100)
    }

    private fun createInitialBatteryData(): BatteryData {
        return BatteryData(
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE}"
        )
    }

    override fun refreshData() {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        intent?.let { updateBatteryData(it) }
    }

    fun stopMonitoring() {
        batteryReceiver?.let { 
            context.unregisterReceiver(it)
            batteryReceiver = null
        }
    }
}

/**
 * Composable function to remember battery data from the provider
 */
@Composable
fun rememberBatteryData(context: Context): State<BatteryData> {
    val provider = remember { BatteryDataProviderImpl(context) }
    return provider.batteryData.collectAsState()
}
