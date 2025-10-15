package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.smartbatteryguardian.data.database.dao.BatteryDao
import com.voltai.smartbatteryguardian.data.database.entities.BatteryReading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BatteryReadingsViewModel
@Inject
constructor(
    private val batteryDao: BatteryDao,
) : ViewModel() {
    private val last24hStart: Long
        get() = System.currentTimeMillis() - 24 * 60 * 60 * 1000L

    val last24hReadings: StateFlow<List<BatteryReading>> =
        batteryDao.getBatteryReadingsSince(last24hStart)
            .map { it.sortedBy { r -> r.timestamp } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val levelSeries: StateFlow<List<Int>> =
        last24hReadings.map { readings -> readings.map { it.batteryLevel } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val temperatureSeries: StateFlow<List<Float>> =
        last24hReadings.map { readings -> readings.map { it.temperature.toFloat() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentSeries: StateFlow<List<Int>> =
        last24hReadings.map { readings -> readings.map { it.current.toInt() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
