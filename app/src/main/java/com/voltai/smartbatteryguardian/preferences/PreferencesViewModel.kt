package com.voltai.smartbatteryguardian.preferences

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
class PreferencesViewModel
@Inject
constructor(
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {
    val reduceMotion: StateFlow<Boolean> =
        dataStoreManager.reduceMotionFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setReduceMotion(enabled)
        }
    }
}
