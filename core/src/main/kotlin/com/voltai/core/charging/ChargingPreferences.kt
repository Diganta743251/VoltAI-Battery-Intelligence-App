package com.voltai.core.charging

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.chargingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "charging_preferences"
)

@Singleton
class ChargingPreferences @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.chargingDataStore
    
    companion object {
        private val CHARGING_MODE = stringPreferencesKey("charging_mode")
        private val MAX_CHARGE_LEVEL = intPreferencesKey("max_charge_level")
        private val MIN_CHARGE_LEVEL = intPreferencesKey("min_charge_level")
        private val CUSTOM_TARGET_LEVEL = intPreferencesKey("custom_target_level")
        private val EMERGENCY_CHARGE_THRESHOLD = intPreferencesKey("emergency_charge_threshold")
        private val ENABLE_SCHEDULED_CHARGING = booleanPreferencesKey("enable_scheduled_charging")
        private val ENABLE_IMMEDIATE_CHARGING = booleanPreferencesKey("enable_immediate_charging")
        private val ENABLE_THERMAL_PROTECTION = booleanPreferencesKey("enable_thermal_protection")
        private val ENABLE_HEALTH_OPTIMIZATION = booleanPreferencesKey("enable_health_optimization")
        private val CHARGING_TIME_WINDOWS = stringPreferencesKey("charging_time_windows")
        private val ADAPTIVE_CHARGING_ENABLED = booleanPreferencesKey("adaptive_charging_enabled")
        private val OVERNIGHT_CHARGING_LIMIT = booleanPreferencesKey("overnight_charging_limit")
        private val WEEKEND_CHARGING_SCHEDULE = booleanPreferencesKey("weekend_charging_schedule")
    }
    
    suspend fun savePreferences(preferences: ChargingUserPreferences) {
        dataStore.edit { prefs ->
            prefs[CHARGING_MODE] = preferences.chargingMode.name
            prefs[MAX_CHARGE_LEVEL] = preferences.maxChargeLevel
            prefs[MIN_CHARGE_LEVEL] = preferences.minChargeLevel
            prefs[CUSTOM_TARGET_LEVEL] = preferences.customTargetLevel
            prefs[EMERGENCY_CHARGE_THRESHOLD] = preferences.emergencyChargeThreshold
            prefs[ENABLE_SCHEDULED_CHARGING] = preferences.enableScheduledCharging
            prefs[ENABLE_IMMEDIATE_CHARGING] = preferences.enableImmediateCharging
            prefs[ENABLE_THERMAL_PROTECTION] = preferences.enableThermalProtection
            prefs[ENABLE_HEALTH_OPTIMIZATION] = preferences.enableHealthOptimization
            prefs[ADAPTIVE_CHARGING_ENABLED] = preferences.adaptiveChargingEnabled
            prefs[OVERNIGHT_CHARGING_LIMIT] = preferences.overnightChargingLimit
            prefs[WEEKEND_CHARGING_SCHEDULE] = preferences.weekendChargingSchedule
            
            // Serialize charging time windows
            val windowsJson = serializeChargingWindows(preferences.chargingTimeWindows)
            prefs[CHARGING_TIME_WINDOWS] = windowsJson
        }
    }
    
    suspend fun getPreferences(): ChargingUserPreferences {
        return dataStore.data.map { preferences ->
            ChargingUserPreferences(
                chargingMode = ChargingMode.valueOf(
                    preferences[CHARGING_MODE] ?: ChargingMode.BALANCED.name
                ),
                maxChargeLevel = preferences[MAX_CHARGE_LEVEL] ?: 85,
                minChargeLevel = preferences[MIN_CHARGE_LEVEL] ?: 20,
                customTargetLevel = preferences[CUSTOM_TARGET_LEVEL] ?: 80,
                emergencyChargeThreshold = preferences[EMERGENCY_CHARGE_THRESHOLD] ?: 10,
                enableScheduledCharging = preferences[ENABLE_SCHEDULED_CHARGING] ?: false,
                enableImmediateCharging = preferences[ENABLE_IMMEDIATE_CHARGING] ?: true,
                enableThermalProtection = preferences[ENABLE_THERMAL_PROTECTION] ?: true,
                enableHealthOptimization = preferences[ENABLE_HEALTH_OPTIMIZATION] ?: true,
                adaptiveChargingEnabled = preferences[ADAPTIVE_CHARGING_ENABLED] ?: true,
                overnightChargingLimit = preferences[OVERNIGHT_CHARGING_LIMIT] ?: true,
                weekendChargingSchedule = preferences[WEEKEND_CHARGING_SCHEDULE] ?: false,
                chargingTimeWindows = deserializeChargingWindows(
                    preferences[CHARGING_TIME_WINDOWS] ?: ""
                )
            )
        }.first()
    }
    
    fun getPreferencesFlow(): Flow<ChargingUserPreferences> {
        return dataStore.data.map { preferences ->
            ChargingUserPreferences(
                chargingMode = ChargingMode.valueOf(
                    preferences[CHARGING_MODE] ?: ChargingMode.BALANCED.name
                ),
                maxChargeLevel = preferences[MAX_CHARGE_LEVEL] ?: 85,
                minChargeLevel = preferences[MIN_CHARGE_LEVEL] ?: 20,
                customTargetLevel = preferences[CUSTOM_TARGET_LEVEL] ?: 80,
                emergencyChargeThreshold = preferences[EMERGENCY_CHARGE_THRESHOLD] ?: 10,
                enableScheduledCharging = preferences[ENABLE_SCHEDULED_CHARGING] ?: false,
                enableImmediateCharging = preferences[ENABLE_IMMEDIATE_CHARGING] ?: true,
                enableThermalProtection = preferences[ENABLE_THERMAL_PROTECTION] ?: true,
                enableHealthOptimization = preferences[ENABLE_HEALTH_OPTIMIZATION] ?: true,
                adaptiveChargingEnabled = preferences[ADAPTIVE_CHARGING_ENABLED] ?: true,
                overnightChargingLimit = preferences[OVERNIGHT_CHARGING_LIMIT] ?: true,
                weekendChargingSchedule = preferences[WEEKEND_CHARGING_SCHEDULE] ?: false,
                chargingTimeWindows = deserializeChargingWindows(
                    preferences[CHARGING_TIME_WINDOWS] ?: ""
                )
            )
        }
    }
    
    suspend fun updateChargingMode(mode: ChargingMode) {
        dataStore.edit { preferences ->
            preferences[CHARGING_MODE] = mode.name
        }
    }
    
    suspend fun updateMaxChargeLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[MAX_CHARGE_LEVEL] = level.coerceIn(50, 100)
        }
    }
    
    suspend fun updateMinChargeLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[MIN_CHARGE_LEVEL] = level.coerceIn(10, 30)
        }
    }
    
    suspend fun addChargingTimeWindow(window: ChargingTimeWindow) {
        val currentPreferences = getPreferences()
        val updatedWindows = currentPreferences.chargingTimeWindows.toMutableList()
        updatedWindows.add(window)
        
        dataStore.edit { preferences ->
            preferences[CHARGING_TIME_WINDOWS] = serializeChargingWindows(updatedWindows)
        }
    }
    
    suspend fun removeChargingTimeWindow(windowId: String) {
        val currentPreferences = getPreferences()
        val updatedWindows = currentPreferences.chargingTimeWindows.filter { it.id != windowId }
        
        dataStore.edit { preferences ->
            preferences[CHARGING_TIME_WINDOWS] = serializeChargingWindows(updatedWindows)
        }
    }
    
    suspend fun enableScheduledCharging(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLE_SCHEDULED_CHARGING] = enabled
        }
    }
    
    suspend fun enableThermalProtection(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLE_THERMAL_PROTECTION] = enabled
        }
    }
    
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    private fun serializeChargingWindows(windows: List<ChargingTimeWindow>): String {
        // Simple JSON-like serialization for time windows
        return windows.joinToString(separator = "|") { window ->
            "${window.id},${window.startTime},${window.endTime},${window.priority.name},${window.enabled},${window.name}"
        }
    }
    
    private fun deserializeChargingWindows(serialized: String): List<ChargingTimeWindow> {
        if (serialized.isBlank()) {
            return getDefaultChargingWindows()
        }
        
        return try {
            serialized.split("|").mapNotNull { windowStr ->
                val parts = windowStr.split(",")
                if (parts.size >= 6) {
                    ChargingTimeWindow(
                        id = parts[0],
                        startTime = LocalTime.parse(parts[1]),
                        endTime = LocalTime.parse(parts[2]),
                        priority = ChargingPriority.valueOf(parts[3]),
                        enabled = parts[4].toBoolean(),
                        name = parts[5]
                    )
                } else null
            }
        } catch (e: Exception) {
            getDefaultChargingWindows()
        }
    }
    
    private fun getDefaultChargingWindows(): List<ChargingTimeWindow> {
        return listOf(
            ChargingTimeWindow(
                id = "overnight",
                startTime = LocalTime.of(22, 0),
                endTime = LocalTime.of(7, 0),
                priority = ChargingPriority.NORMAL,
                enabled = true,
                name = "Overnight Charging"
            ),
            ChargingTimeWindow(
                id = "morning",
                startTime = LocalTime.of(7, 0),
                endTime = LocalTime.of(9, 0),
                priority = ChargingPriority.LOW,
                enabled = false,
                name = "Morning Boost"
            ),
            ChargingTimeWindow(
                id = "afternoon",
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(16, 0),
                priority = ChargingPriority.LOW,
                enabled = false,
                name = "Afternoon Charge"
            )
        )
    }
}

// Data classes are defined in ChargingTypes.kt to avoid redeclarations

// Extension function to get data store first value
private suspend fun <T> Flow<T>.first(): T {
    var result: T? = null
    collect { value ->
        if (result == null) {
            result = value
            return@collect
        }
    }
    return result!!
}
