package com.voltai.smartbatteryguardian.viewmodels

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.core.permissions.UsageStatsPermission
import com.voltai.domain.model.AppUsage
import com.voltai.domain.repository.AppUsageRepository
import com.voltai.smartbatteryguardian.models.AppUsageData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppUsageUiViewModel
@Inject
constructor(
    private val repository: AppUsageRepository,
    @ApplicationContext private val context: Context,
    private val usagePermission: UsageStatsPermission,
) : ViewModel() {
    private val _apps = MutableStateFlow<List<AppUsageData>>(emptyList())
    val apps: StateFlow<List<AppUsageData>> = _apps.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        refreshPermission()
        load()
    }

    fun load() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                refreshPermission()
                if (!_hasPermission.value) {
                    _apps.value = emptyList()
                    _loading.value = false
                    return@launch
                }
                repository.getAppUsageStats()
                    .catch { e ->
                        _error.value = e.message ?: "Failed to load app usage"
                        _apps.value = emptyList()
                        _loading.value = false
                    }
                    .collect { list ->
                        _apps.value = mapToUi(list)
                        _loading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _apps.value = emptyList()
                _loading.value = false
            }
        }
    }

    fun refreshPermission() {
        _hasPermission.value = usagePermission.isGranted()
    }

    private fun mapToUi(list: List<AppUsage>): List<AppUsageData> {
        if (list.isEmpty()) return emptyList()
        return try {
            val pm: PackageManager = context.packageManager

            // Normalize estimated battery drain to sum up to ~100%
            val totalDrain = list.sumOf { it.estimatedBatteryDrain }
            val apps =
                list.mapNotNull { a ->
                    try {
                        val icon =
                            try {
                                pm.getApplicationIcon(a.packageName)
                            } catch (_: Exception) {
                                null
                            }
                        val normalizedBattery = if (totalDrain > 0) (a.estimatedBatteryDrain / totalDrain * 100.0) else 0.0
                        val categoryColor = Color(0xFF607D8B)
                        val openCount = (a.totalTimeInForeground / (5 * 60 * 1000L)).toInt().coerceAtLeast(0)
                        val avgSession =
                            (a.totalTimeInForeground / (openCount.coerceAtLeast(1))).coerceAtLeast(
                                60_000L,
                            )
                        AppUsageData(
                            packageName = a.packageName,
                            appName = a.appName,
                            icon = icon,
                            batteryUsage = normalizedBattery,
                            totalTimeInForeground = a.totalTimeInForeground,
                            lastTimeUsed = 0L,
                            usageToday = a.totalTimeInForeground,
                            usageYesterday = 0L,
                            usageThisWeek = a.totalTimeInForeground,
                            openCount = openCount,
                            avgSessionTime = avgSession,
                            peakUsageHour = 0,
                            categoryColor = categoryColor,
                        )
                    } catch (e: Exception) {
                        null // Skip apps that fail to map
                    }
                }
            apps
                .distinctBy { it.packageName } // Remove duplicates
                .sortedByDescending { it.totalTimeInForeground }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
