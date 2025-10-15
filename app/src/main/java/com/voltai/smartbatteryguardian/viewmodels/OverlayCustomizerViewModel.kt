package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.smartbatteryguardian.preferences.OverlayPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverlayCustomizerViewModel @Inject constructor(
    private val repo: OverlayPreferencesRepository,
) : ViewModel() {

    data class UiState(
        val overlayType: String,
        val enabled: Boolean,
        val showRing: Boolean,
        val showStats: Boolean,
        val showClock: Boolean,
        val showTips: Boolean,
        val ringSize: Float,
        val opacity: Float,
        val accentColor: Long,
        val backgroundColor: Long,
        val aodBrightness: Int,
    )

    val uiState: StateFlow<UiState> = repo.settings
        .map { s ->
            UiState(
                overlayType = s.overlayType,
                enabled = s.enabled,
                showRing = s.showRing,
                showStats = s.showStats,
                showClock = s.showClock,
                showTips = s.showTips,
                ringSize = s.ringSize,
                opacity = s.opacity,
                accentColor = s.accentColor,
                backgroundColor = s.backgroundColor,
                aodBrightness = s.aodBrightness,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState(
            overlayType = "BATTERY_SAVER",
            enabled = false,
            showRing = true,
            showStats = true,
            showClock = true,
            showTips = false,
            ringSize = 0.7f,
            opacity = 0.9f,
            accentColor = 0xFF4CAF50,
            backgroundColor = 0x00000000,
            aodBrightness = 20,
        ))

    fun setOverlayType(value: String) = viewModelScope.launch { repo.setOverlayType(value) }
    fun setEnabled(value: Boolean) = viewModelScope.launch { repo.setEnabled(value) }
    fun setShowRing(value: Boolean) = viewModelScope.launch { repo.setShowRing(value) }
    fun setShowStats(value: Boolean) = viewModelScope.launch { repo.setShowStats(value) }
    fun setShowClock(value: Boolean) = viewModelScope.launch { repo.setShowClock(value) }
    fun setShowTips(value: Boolean) = viewModelScope.launch { repo.setShowTips(value) }
    fun setRingSize(value: Float) = viewModelScope.launch { repo.setRingSize(value) }
    fun setOpacity(value: Float) = viewModelScope.launch { repo.setOpacity(value) }
    fun setAccentColor(value: Long) = viewModelScope.launch { repo.setAccentColor(value) }
    fun setBackgroundColor(value: Long) = viewModelScope.launch { repo.setBackgroundColor(value) }
    fun setAodBrightness(value: Int) = viewModelScope.launch { repo.setAodBrightness(value) }
}
