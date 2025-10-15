package com.voltai.smartbatteryguardian.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val OVERLAY_DATASTORE = "overlay_settings"

val Context.overlayDataStore: DataStore<Preferences> by preferencesDataStore(name = OVERLAY_DATASTORE)

@Singleton
class OverlayPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // Keys
    private object Keys {
        val OVERLAY_TYPE = stringPreferencesKey("overlay_type") // "BATTERY_SAVER" | "AOD"
        val ENABLED = booleanPreferencesKey("overlay_enabled")
        val SHOW_RING = booleanPreferencesKey("show_ring")
        val SHOW_STATS = booleanPreferencesKey("show_stats")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_TIPS = booleanPreferencesKey("show_tips")
        val RING_SIZE = floatPreferencesKey("ring_size") // 0f..1f
        val OPACITY = floatPreferencesKey("overlay_opacity") // 0f..1f
        val ACCENT_COLOR = longPreferencesKey("accent_color") // ARGB long
        val BACKGROUND_COLOR = longPreferencesKey("background_color") // ARGB long
        val AOD_BRIGHTNESS = intPreferencesKey("aod_brightness") // 0..100
    }

    data class OverlaySettings(
        val overlayType: String = "BATTERY_SAVER",
        val enabled: Boolean = false,
        val showRing: Boolean = true,
        val showStats: Boolean = true,
        val showClock: Boolean = true,
        val showTips: Boolean = false,
        val ringSize: Float = 0.7f,
        val opacity: Float = 0.9f,
        val accentColor: Long = 0xFF4CAF50,
        val backgroundColor: Long = 0x00000000,
        val aodBrightness: Int = 20,
    )

    val settings: Flow<OverlaySettings> = context.overlayDataStore.data.map { p ->
        OverlaySettings(
            overlayType = p[Keys.OVERLAY_TYPE] ?: "BATTERY_SAVER",
            enabled = p[Keys.ENABLED] ?: false,
            showRing = p[Keys.SHOW_RING] ?: true,
            showStats = p[Keys.SHOW_STATS] ?: true,
            showClock = p[Keys.SHOW_CLOCK] ?: true,
            showTips = p[Keys.SHOW_TIPS] ?: false,
            ringSize = p[Keys.RING_SIZE] ?: 0.7f,
            opacity = p[Keys.OPACITY] ?: 0.9f,
            accentColor = p[Keys.ACCENT_COLOR] ?: 0xFF4CAF50,
            backgroundColor = p[Keys.BACKGROUND_COLOR] ?: 0x00000000,
            aodBrightness = p[Keys.AOD_BRIGHTNESS] ?: 20,
        )
    }

    suspend fun setOverlayType(value: String) {
        context.overlayDataStore.edit { it[Keys.OVERLAY_TYPE] = value }
    }
    suspend fun setEnabled(value: Boolean) {
        context.overlayDataStore.edit { it[Keys.ENABLED] = value }
    }
    suspend fun setShowRing(value: Boolean) {
        context.overlayDataStore.edit { it[Keys.SHOW_RING] = value }
    }
    suspend fun setShowStats(value: Boolean) {
        context.overlayDataStore.edit { it[Keys.SHOW_STATS] = value }
    }
    suspend fun setShowClock(value: Boolean) {
        context.overlayDataStore.edit { it[Keys.SHOW_CLOCK] = value }
    }
    suspend fun setShowTips(value: Boolean) {
        context.overlayDataStore.edit { it[Keys.SHOW_TIPS] = value }
    }
    suspend fun setRingSize(value: Float) {
        context.overlayDataStore.edit { it[Keys.RING_SIZE] = value.coerceIn(0f, 1f) }
    }
    suspend fun setOpacity(value: Float) {
        context.overlayDataStore.edit { it[Keys.OPACITY] = value.coerceIn(0f, 1f) }
    }
    suspend fun setAccentColor(argb: Long) {
        context.overlayDataStore.edit { it[Keys.ACCENT_COLOR] = argb }
    }
    suspend fun setBackgroundColor(argb: Long) {
        context.overlayDataStore.edit { it[Keys.BACKGROUND_COLOR] = argb }
    }
    suspend fun setAodBrightness(value: Int) {
        context.overlayDataStore.edit { it[Keys.AOD_BRIGHTNESS] = value.coerceIn(0, 100) }
    }
}
