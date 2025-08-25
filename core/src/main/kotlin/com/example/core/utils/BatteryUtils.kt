package com.voltai.core.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.voltai.domain.model.BatteryStatus

object BatteryUtils {

    fun getBatteryStatus(context: Context?): BatteryStatus {
        if (context == null) {
            return BatteryStatus(
                percentage = 0,
                status = "Unknown",
                voltage = 0f,
                temperature = 0f,
                chargingType = "Unknown",
                current = 0f
            )
        }

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val percentage = if (scale > 0) (level * 100 / scale) else 0
        
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val statusString = getBatteryStatusString(status)
        
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: 0
        val voltageFloat = voltage / 1000f // Convert to volts
        
        val temperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: 0
        val temperatureFloat = temperature / 10f // Convert to Celsius
        
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargingType = getChargingTypeString(plugged)
        
        // Get current measurement (API 21+)
        val current = getCurrentMeasurement(batteryManager)

        return BatteryStatus(
            percentage = percentage,
            batteryLevel = percentage,
            status = statusString,
            chargingStatus = statusString,
            voltage = voltageFloat,
            temperature = temperatureFloat,
            chargingType = chargingType,
            current = current
        )
    }

    private fun getCurrentMeasurement(batteryManager: BatteryManager?): Float {
        return try {
            if (batteryManager != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Get instantaneous current in microamperes
                val currentNowMicroAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                
                // Convert microamperes to milliamperes
                val currentMilliAmps = currentNowMicroAmps / 1000f
                
                Log.d("BatteryUtils", "Current measurement: ${currentMilliAmps}mA")
                currentMilliAmps
            } else {
                Log.w("BatteryUtils", "BatteryManager not available or API level too low for current measurement")
                0f
            }
        } catch (e: Exception) {
            Log.e("BatteryUtils", "Failed to get current measurement", e)
            0f
        }
    }

    fun getBatteryHealth(batteryStatus: Int, batteryLevel: Int): String {
        return when (batteryStatus) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }

    private fun getBatteryStatusString(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }
    }

    private fun getChargingTypeString(plugged: Int): String {
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not Charging"
        }
    }

    fun estimateChargingCycles(currentCapacity: Int, designCapacity: Int): Int {
        return if (designCapacity > 0) {
            (currentCapacity.toDouble() / designCapacity * 100).toInt()
        } else {
            Log.e("BatteryUtils", "Design capacity must be greater than zero.")
            0
        }
    }

    fun calculateBatteryTemperature(temperature: Int): Float {
        return temperature / 10.0f // Convert to Celsius
    }

    fun isFastCharging(chargingType: Int): Boolean {
        // Fast charging detection would require additional logic
        // For now, assume AC charging might be fast charging
        return chargingType == BatteryManager.BATTERY_PLUGGED_AC
    }

    fun isWirelessCharging(chargingType: Int): Boolean {
        return chargingType == BatteryManager.BATTERY_PLUGGED_WIRELESS
    }
}
