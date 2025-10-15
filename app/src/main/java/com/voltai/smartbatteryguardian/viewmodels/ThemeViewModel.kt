package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.data.preferences.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel
@Inject
constructor(
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {
    val selectedTheme: StateFlow<String> =
        dataStoreManager.selectedThemeFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = "electric_blue",
            )

    val reduceMotion: StateFlow<Boolean> =
        dataStoreManager.reduceMotionFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

    fun setSelectedTheme(themeId: String) {
        viewModelScope.launch {
            dataStoreManager.setSelectedTheme(themeId)
            // If E-Paper is selected, automatically reduce motion for eye comfort
            if (themeId == "e_paper") {
                dataStoreManager.setReduceMotion(true)
            }
        }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setReduceMotion(enabled)
        }
    }
}
