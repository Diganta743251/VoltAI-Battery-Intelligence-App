package com.voltai.service

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.voltai.domain.model.BatteryLog
import com.voltai.domain.model.BatteryStatus
import com.voltai.domain.repository.BatteryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryDataCollector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryRepository: BatteryRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _currentBatteryStatus = MutableStateFlow<BatteryStatus?>(null)
    val currentBatteryStatus: StateFlow<BatteryStatus?> = _currentBatteryStatus.asStateFlow()
    
    private val _isCollecting = MutableStateFlow(false)
    val isCollecting: StateFlow<Boolean> = _isCollecting.asStateFlow()
    
    fun startCollection() {
        if (_isCollecting.value) return
        
        _isCollecting.value = true
        scope.launch {
            while (_isCollecting.value) {
                try {
                    val batteryStatus = getCurrentBatteryInfo()
                    _currentBatteryStatus.value = batteryStatus
                    
                    // Save to database
                    val batteryLog = BatteryLog(
                        id = System.currentTimeMillis(),
                        timestamp = System.currentTimeMillis(),
                        batteryLevel = batteryStatus.batteryLevel,
                        voltage = batteryStatus.voltage,
                        temperature = batteryStatus.temperature,
                        chargingStatus = batteryStatus.chargingStatus,
                        chargingType = batteryStatus.chargingType,
                        current = batteryStatus.current
                    )
                    
                    batteryRepository.insertBatteryLog(batteryLog)
                    
                } catch (e: Exception) {
                    // Log error but continue collection
                }
                
                delay(30_000) // Collect every 30 seconds
            }
        }
    }
    
    fun stopCollection() {
        _isCollecting.value = false
    }
    
    private fun getCurrentBatteryInfo(): BatteryStatus {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)
        
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercentage = if (level != -1 && scale != -1) {
            (level * 100) / scale
        } else {
            0
        }
        
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val chargingStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            else -> "Unknown"
        }
        
        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargingType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Unknown"
        }
        
        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        
        // Get current using BatteryManager (API 21+)
        val currentNow = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000f // Convert to mA
        } catch (e: Exception) {
            0f
        }
        
        return BatteryStatus(
            percentage = batteryPercentage,
            batteryLevel = batteryPercentage,
            status = chargingStatus,
            chargingStatus = chargingStatus,
            voltage = voltage / 1000f, // Convert to V
            temperature = temperature / 10f, // Convert to Celsius
            current = currentNow,
            chargingType = chargingType
        )
    }
    
    fun getScreenOnTime(): String {
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (24 * 60 * 60 * 1000) // Last 24 hours
            
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            
            val totalScreenTime = usageStats.sumOf { it.totalTimeInForeground }
            val hours = totalScreenTime / (1000 * 60 * 60)
            val minutes = (totalScreenTime % (1000 * 60 * 60)) / (1000 * 60)
            
            "${hours}h ${minutes}m"
        } catch (e: Exception) {
            "N/A"
        }
    }
    
    fun getBatteryHealth(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            
            val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
            when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> 100
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> 80
                BatteryManager.BATTERY_HEALTH_DEAD -> 0
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 70
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 50
                BatteryManager.BATTERY_HEALTH_COLD -> 85
                else -> 95
            }
        } catch (e: Exception) {
            95
        }
    }
}