package com.voltai.core.thermal

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThermalPreferences @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "thermal_preferences", 
        Context.MODE_PRIVATE
    )

    var thermalThreshold: Float
        get() = prefs.getFloat("thermal_threshold", 40.0f)
        set(value) = prefs.edit().putFloat("thermal_threshold", value).apply()

    var enableThermalMonitoring: Boolean
        get() = prefs.getBoolean("enable_thermal_monitoring", true)
        set(value) = prefs.edit().putBoolean("enable_thermal_monitoring", value).apply()

    var thermalAlertEnabled: Boolean
        get() = prefs.getBoolean("thermal_alert_enabled", true)
        set(value) = prefs.edit().putBoolean("thermal_alert_enabled", value).apply()

    var coolingPolicy: CoolingPolicy
        get() = CoolingPolicy.valueOf(
            prefs.getString("cooling_policy", CoolingPolicy.BALANCED.name) ?: CoolingPolicy.BALANCED.name
        )
        set(value) = prefs.edit().putString("cooling_policy", value.name).apply()
}

enum class CoolingPolicy {
    PERFORMANCE,
    BALANCED, 
    AGGRESSIVE
}
