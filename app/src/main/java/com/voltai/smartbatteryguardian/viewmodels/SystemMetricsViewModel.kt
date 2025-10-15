package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.smartbatteryguardian.data.database.dao.BatteryDao
import com.voltai.smartbatteryguardian.data.database.dao.SystemPerformanceSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SystemMetricsViewModel
@Inject
constructor(
    private val batteryDao: BatteryDao,
) : ViewModel() {
    private val _systemSummary = MutableStateFlow<SystemPerformanceSummary?>(null)
    val systemSummary: StateFlow<SystemPerformanceSummary?> = _systemSummary

    private val last24hStart: Long
        get() = System.currentTimeMillis() - 24 * 60 * 60 * 1000L

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                val summary = batteryDao.getSystemPerformanceSummary(last24hStart)
                _systemSummary.value = summary
            } catch (e: Exception) {
                // Keep null on failure; UI will show placeholders
            }
        }
    }
}
