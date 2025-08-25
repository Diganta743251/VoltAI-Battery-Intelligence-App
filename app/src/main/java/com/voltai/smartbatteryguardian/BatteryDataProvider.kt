package com.voltai.smartbatteryguardian

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlin.random.Random

data class BatteryData(
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val chargingStatus: String = "Unknown",
    val temperature: Float = 0f,
    val voltage: Float = 0f,
    val current: Int = 0,
    val health: String = "Unknown",
    val healthPercentage: Int = 100,
    val capacity: String = "Unknown",
    val screenOnTime: String = "0s",
    val screenOffTime: String = "0s",
    val batteryUsed: String = "0%",
    val remainingTime: String = "Unknown"
)

class BatteryDataProvider(private val context: Context) {
    
    fun getBatteryData(): BatteryData {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryLevel = (level * 100 / scale.toFloat()).toInt()
        
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val chargingStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
        
        val temperature = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val voltage = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
        
        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthString = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
        
        // Simulate some dynamic data
        val current = if (isCharging) Random.nextInt(500, 2000) else -Random.nextInt(100, 1000)
        val healthPercentage = when (healthString) {
            "Good" -> Random.nextInt(80, 100)
            "Overheat" -> Random.nextInt(60, 80)
            else -> Random.nextInt(40, 70)
        }
        
        // Calculate remaining time based on battery level and usage
        val remainingHours = if (isCharging) {
            (100 - batteryLevel) / 20 // Rough charging time
        } else {
            batteryLevel / 15 // Rough discharge time
        }
        val remainingTime = if (remainingHours > 24) {
            "${remainingHours / 24}d ${remainingHours % 24}h ${Random.nextInt(0, 60)}m left"
        } else {
            "${remainingHours}h ${Random.nextInt(0, 60)}m left"
        }
        
        // Simulate screen time data
        val screenOnMinutes = Random.nextInt(30, 300)
        val screenOffMinutes = Random.nextInt(100, 600)
        val screenOnTime = "${screenOnMinutes / 60}h ${screenOnMinutes % 60}m ${Random.nextInt(0, 60)}s"
        val screenOffTime = "${screenOffMinutes / 60}h ${screenOffMinutes % 60}m ${Random.nextInt(0, 60)}s"
        
        val batteryUsedPercent = 100 - batteryLevel
        val batteryUsed = "$batteryUsedPercent% • ${batteryUsedPercent * 30} mAh"
        
        val capacity = "${Random.nextInt(2500, 3000)}/${Random.nextInt(4000, 5000)} mAh"
        
        return BatteryData(
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            chargingStatus = chargingStatus,
            temperature = temperature,
            voltage = voltage,
            current = current,
            health = healthString,
            healthPercentage = healthPercentage,
            capacity = capacity,
            screenOnTime = screenOnTime,
            screenOffTime = screenOffTime,
            batteryUsed = batteryUsed,
            remainingTime = remainingTime
        )
    }
}

@Composable
fun rememberBatteryData(context: Context): State<BatteryData> {
    val batteryData = remember { mutableStateOf(BatteryData()) }
    val dataProvider = remember { BatteryDataProvider(context) }
    
    LaunchedEffect(Unit) {
        while (true) {
            batteryData.value = dataProvider.getBatteryData()
            delay(5000) // Update every 5 seconds
        }
    }
    
    return batteryData
}