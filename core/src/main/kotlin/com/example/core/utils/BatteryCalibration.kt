package com.voltai.core.utils

import android.content.Context
import android.os.BatteryManager
import android.os.Build

object BatteryCalibration {
    
    fun getCurrentBatteryCapacity(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            try {
                // Try to get the actual capacity in microampere-hours
                val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                if (capacity > 0) {
                    capacity / 1000 // Convert to mAh
                } else {
                    // Fallback to estimated capacity
                    getEstimatedCurrentCapacity(context)
                }
            } catch (e: Exception) {
                getEstimatedCurrentCapacity(context)
            }
        } else {
            getEstimatedCurrentCapacity(context)
        }
    }
    
    fun getEstimatedDesignCapacity(context: Context): Int {
        // This is a rough estimation based on common device types
        // In a real app, you'd maintain a database of device-specific capacities
        return when {
            isTablet(context) -> 8000 // Typical tablet capacity
            isLargePhone(context) -> 4500 // Large phone capacity
            else -> 3000 // Standard phone capacity
        }
    }
    
    private fun getEstimatedCurrentCapacity(context: Context): Int {
        // Estimate based on design capacity and assumed degradation
        val designCapacity = getEstimatedDesignCapacity(context)
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val energy = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
                if (energy > 0) {
                    // Convert from nWh to mAh (approximate)
                    (energy / 3700000).coerceAtLeast(designCapacity * 60 / 100) // At least 60% of design
                } else {
                    (designCapacity * 85 / 100) // Assume 85% health if no data
                }
            } catch (e: Exception) {
                (designCapacity * 85 / 100)
            }
        } else {
            (designCapacity * 85 / 100)
        }
    }
    
    private fun isTablet(context: Context): Boolean {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val screenHeightDp = displayMetrics.heightPixels / displayMetrics.density
        val screenSizeInches = kotlin.math.sqrt((screenWidthDp * screenWidthDp + screenHeightDp * screenHeightDp).toDouble()) / 160.0
        return screenSizeInches >= 7.0
    }
    
    private fun isLargePhone(context: Context): Boolean {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val screenHeightDp = displayMetrics.heightPixels / displayMetrics.density
        val screenSizeInches = kotlin.math.sqrt((screenWidthDp * screenWidthDp + screenHeightDp * screenHeightDp).toDouble()) / 160.0
        return screenSizeInches >= 6.0 && screenSizeInches < 7.0
    }
}
