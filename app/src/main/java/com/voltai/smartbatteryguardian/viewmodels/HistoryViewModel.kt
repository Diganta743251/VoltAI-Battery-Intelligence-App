package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.smartbatteryguardian.data.database.dao.BatteryDao
import com.voltai.smartbatteryguardian.data.database.entities.ChargingSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel
@Inject
constructor(
    private val batteryDao: BatteryDao,
) : ViewModel() {
    // Expose recent charging sessions from Room as a StateFlow
    val sessions: StateFlow<List<ChargingSession>> =
        batteryDao.getRecentChargingSessions(limit = 100)
            .map { it.sortedByDescending { s -> s.startTime } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )
}
