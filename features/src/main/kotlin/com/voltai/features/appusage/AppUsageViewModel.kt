package com.voltai.features.appusage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.domain.model.AppUsage
import com.voltai.domain.repository.AppUsageRepository
import com.voltai.core.permissions.UsageStatsPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppUsageViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val usageStatsPermission: UsageStatsPermission
) : ViewModel() {

    private val _appUsage = MutableStateFlow<List<AppUsage>>(emptyList())
    val appUsage: StateFlow<List<AppUsage>> = _appUsage.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _totalScreenTime = MutableStateFlow(0L)
    val totalScreenTime: StateFlow<Long> = _totalScreenTime.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        refreshPermission()
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            refreshPermission()
            if (!_hasPermission.value) {
                _appUsage.value = emptyList()
                _totalScreenTime.value = 0L
                _isLoading.value = false
                return@launch
            }
            appUsageRepository.getAppUsageStats()
                .catch { e ->
                    _error.value = e.message ?: "Failed to load app usage"
                    _appUsage.value = emptyList()
                    _totalScreenTime.value = 0L
                }
                .collect { usage ->
                    // Sort by foreground time by default for consistent UX with dashboard
                    val sorted = usage.sortedByDescending { it.totalTimeInForeground }
                    _appUsage.value = sorted
                    _totalScreenTime.value = sorted.sumOf { it.totalTimeInForeground }
                }
            _isLoading.value = false
        }
    }

    fun refreshPermission() {
        _hasPermission.value = usageStatsPermission.isGranted()
    }