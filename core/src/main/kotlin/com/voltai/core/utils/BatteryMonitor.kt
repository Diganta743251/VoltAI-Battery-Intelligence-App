package com.voltai.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class BatteryStatus(
    val level: Int = 0,
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val isCharging: Boolean = false,
    val chargingType: ChargingType = ChargingType.NONE,
    val health: Int = BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val technology: String = "Unknown",
    val currentNow: Int = 0, // microamps
    val capacity: Int = 0,
    val cycleCount: Int = 0
)

enum class ChargingType {
    NONE,
    AC,
    USB,
    WIRELESS
}

class BatteryMonitor(private val context: Context) {
    
    fun getBatteryStatusFlow(): Flow<BatteryStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val batteryStatus = extractBatteryStatus(intent)
                    trySend(batteryStatus)
                }
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        
        // Send initial status
        val initialIntent = context.registerReceiver(null, filter)
        initialIntent?.let {
            val initialStatus = extractBatteryStatus(it)
            trySend(initialStatus)
        }
        
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.distinctUntilChanged()
    
    private fun extractBatteryStatus(intent: Intent): BatteryStatus {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val chargingType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> ChargingType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> ChargingType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingType.WIRELESS
            else -> ChargingType.NONE
        }
        
        // Get additional battery info using BatteryManager
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentNow = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } catch (e: Exception) {
            0
        }
        
        val capacity = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            level * 100 / scale
        }
        
        val cycleCount = try {
            // BATTERY_PROPERTY_CYCLE_COUNT is not available on all Android versions
            // Use reflection or return 0 as fallback
            0
        } catch (e: Exception) {
            0
        }
        
        return BatteryStatus(
            level = level * 100 / scale,
            temperature = temperature,
            voltage = voltage,
            isCharging = isCharging,
            chargingType = chargingType,
            health = health,
            technology = technology,
            currentNow = currentNow,
            capacity = capacity,
            cycleCount = cycleCount
        )
    }
    
    fun getCurrentBatteryStatus(): BatteryStatus {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        return intent?.let { extractBatteryStatus(it) } ?: BatteryStatus()
    }
}