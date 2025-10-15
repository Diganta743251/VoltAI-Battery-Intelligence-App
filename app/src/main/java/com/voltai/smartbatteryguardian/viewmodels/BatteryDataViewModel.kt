package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.data.battery.BatteryDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel that exposes the single source of truth for battery data
 */
@HiltViewModel
class BatteryDataViewModel
@Inject
constructor(
    private val batteryDataManager: BatteryDataManager,
) : ViewModel() {
    val batteryData: StateFlow<BatteryData> = batteryDataManager.batteryData

    override fun onCleared() {
        super.onCleared()
        batteryDataManager.cleanup()
    }
}
