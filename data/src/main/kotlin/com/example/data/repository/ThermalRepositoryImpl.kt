package com.voltai.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.voltai.domain.model.ThermalStatus
import com.voltai.domain.repository.ThermalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class ThermalRepositoryImpl @Inject constructor(
    private val context: Context
) : ThermalRepository {

    private val TAG = "ThermalRepositoryImpl"

    override fun getThermalStatus(): Flow<ThermalStatus> = flow {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        // Get battery temperature from system
        val batteryTemp = getBatteryTemperature()
        
        // Get CPU temperature from thermal zones
        val cpuTemp = getCpuTemperature()
        
        // Estimate GPU temperature (usually similar to CPU + offset)
        val gpuTemp = cpuTemp + 5.0f
        
        // Estimate skin temperature (usually lower than internal components)
        val skinTemp = (cpuTemp + batteryTemp) / 2.0f - 2.0f
        
        // Get thermal status from PowerManager (API 29+)
        val thermalStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            powerManager.currentThermalStatus
        } else {
            PowerManager.THERMAL_STATUS_NONE
        }

        emit(ThermalStatus(
            cpuTemperature = cpuTemp,
            gpuTemperature = gpuTemp,
            batteryTemperature = batteryTemp,
            skinTemperature = skinTemp,
            thermalStatus = thermalStatus
        ))
    }

    private fun getBatteryTemperature(): Float {
        return try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val temperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            temperature / 10.0f // Convert from decidegrees to Celsius
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get battery temperature", e)
            30.0f // Default fallback
        }
    }

    private fun getCpuTemperature(): Float {
        // Try to read CPU temperature from thermal zones
        val thermalPaths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone1/temp"
        )

        for (path in thermalPaths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val tempString = file.readText().trim()
                    val temp = tempString.toIntOrNull()
                    if (temp != null) {
                        // Temperature is usually in millidegrees, convert to Celsius
                        val celsius = when {
                            temp > 1000 -> temp / 1000.0f // millidegrees to Celsius
                            temp > 100 -> temp / 10.0f    // decidegrees to Celsius
                            else -> temp.toFloat()        // already in Celsius
                        }
                        
                        // Sanity check: CPU temp should be between 20-100°C
                        if (celsius in 20.0f..100.0f) {
                            Log.d(TAG, "CPU temperature from $path: ${celsius}°C")
                            return celsius
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Could not read temperature from $path: ${e.message}")
            }
        }

        // Fallback: estimate based on battery temperature
        val batteryTemp = getBatteryTemperature()
        val estimatedCpuTemp = batteryTemp + 5.0f // CPU usually runs hotter than battery
        Log.d(TAG, "Using estimated CPU temperature: ${estimatedCpuTemp}°C")
        return estimatedCpuTemp
    }
}
