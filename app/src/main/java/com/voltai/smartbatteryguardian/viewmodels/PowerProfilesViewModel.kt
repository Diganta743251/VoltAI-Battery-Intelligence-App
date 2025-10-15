package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.smartbatteryguardian.data.database.dao.BatteryDao
import com.voltai.smartbatteryguardian.data.database.entities.PowerProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PowerProfilesViewModel
@Inject
constructor(
    private val batteryDao: BatteryDao,
) : ViewModel() {
    val profiles: StateFlow<List<PowerProfile>> =
        batteryDao.getAllPowerProfiles()
            .map { it.sortedWith(compareByDescending<PowerProfile> { p -> p.isActive }.thenBy { it.name }) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Initialize default profiles if empty
        viewModelScope.launch {
            val existingProfiles = batteryDao.getAllPowerProfilesOnce()
            if (existingProfiles.isEmpty()) {
                createDefaultProfiles()
            }
        }
    }

    private suspend fun createDefaultProfiles() {
        val defaultProfiles =
            listOf(
                PowerProfile(
                    name = "Balanced",
                    description = "Optimal balance between performance and battery life",
                    isActive = true,
                    isCustom = false,
                    cpuGovernor = "interactive",
                    maxCpuFreq = null,
                    screenBrightness = null,
                    screenTimeout = 30000,
                    wifiEnabled = true,
                    bluetoothEnabled = true,
                    locationEnabled = true,
                    syncEnabled = true,
                    backgroundAppLimits = null,
                    notificationFilters = null,
                    thermalThreshold = 45.0,
                    batteryThreshold = 20,
                    automationRules = null,
                    estimatedSavings = 15.0,
                ),
                PowerProfile(
                    name = "Power Saver",
                    description = "Maximize battery life",
                    isActive = false,
                    isCustom = false,
                    cpuGovernor = "powersave",
                    maxCpuFreq = null,
                    screenBrightness = 50,
                    screenTimeout = 15000,
                    wifiEnabled = true,
                    bluetoothEnabled = false,
                    locationEnabled = false,
                    syncEnabled = false,
                    backgroundAppLimits = "aggressive",
                    notificationFilters = null,
                    thermalThreshold = 42.0,
                    batteryThreshold = 30,
                    automationRules = null,
                    estimatedSavings = 35.0,
                ),
                PowerProfile(
                    name = "Performance",
                    description = "Maximum performance",
                    isActive = false,
                    isCustom = false,
                    cpuGovernor = "performance",
                    maxCpuFreq = null,
                    screenBrightness = 100,
                    screenTimeout = 60000,
                    wifiEnabled = true,
                    bluetoothEnabled = true,
                    locationEnabled = true,
                    syncEnabled = true,
                    backgroundAppLimits = null,
                    notificationFilters = null,
                    thermalThreshold = 50.0,
                    batteryThreshold = 10,
                    automationRules = null,
                    estimatedSavings = -10.0,
                ),
            )
        defaultProfiles.forEach { batteryDao.insertPowerProfile(it) }
    }

    fun setActive(profileId: Long) {
        viewModelScope.launch {
            val all = profiles.value
            val target = all.firstOrNull { it.id == profileId }
            if (target != null) {
                batteryDao.deactivateAllProfiles()
                batteryDao.updatePowerProfile(target.copy(isActive = true))
            }
        }
    }
}
