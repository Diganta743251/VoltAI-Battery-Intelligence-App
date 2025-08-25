package com.voltai.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.core.utils.BatteryMonitor
import com.voltai.domain.model.BatteryStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val batteryMonitor: BatteryMonitor,
    private val currentRepository: com.voltai.data.repository.CurrentRepository
) : ViewModel() {
    
    private val _batteryStatus = MutableStateFlow(
        com.voltai.domain.model.BatteryStatus(
            percentage = 0,
            status = "",
            voltage = 0f,
            temperature = 0f
        )
    )
    val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus.asStateFlow()
    
    private val _currentData = MutableStateFlow<List<Float>>(emptyList())
    val currentData: StateFlow<List<Float>> = _currentData.asStateFlow()

    companion object {
        const val CURRENT_WINDOW_SIZE = 50
        private const val WINDOW_MS = 15 * 60 * 1000L // 15 minutes
    }
    
    private val _batteryTips = MutableStateFlow<List<String>>(emptyList())
    val batteryTips: StateFlow<List<String>> = _batteryTips.asStateFlow()
    
    private val _batteryForecast = MutableStateFlow<String>("")
    val batteryForecast: StateFlow<String> = _batteryForecast.asStateFlow()
    
    private val _thermalData = MutableStateFlow<Float>(25f)
    val thermalData: StateFlow<Float> = _thermalData.asStateFlow()
    
    // Battery Health Data
    data class BatteryHealth(
        val currentCapacity: Float,
        val designCapacity: Float,
        val healthPercentage: Int,
        val cycleCount: Int
    )
    
    private val _batteryHealth = MutableStateFlow(
        BatteryHealth(
            currentCapacity = 3820f,
            designCapacity = 4000f,
            healthPercentage = 95,
            cycleCount = 247
        )
    )
    val batteryHealth: StateFlow<BatteryHealth> = _batteryHealth.asStateFlow()
    
    init {
        startBatteryMonitoring()
        startCurrentPersistence()
        generateBatteryTips()
    }
    
    private fun startBatteryMonitoring() {
        viewModelScope.launch {
            batteryMonitor.getBatteryStatusFlow()
                .map { coreStatus ->
                    // Map core BatteryStatus to domain BatteryStatus used by UI
                    com.voltai.domain.model.BatteryStatus(
                        percentage = coreStatus.level,
                        status = if (coreStatus.isCharging) "Charging" else "Discharging",
                        isCharging = coreStatus.isCharging,
                        voltage = coreStatus.voltage.toFloat() / 1000f, // mV->V if needed
                        temperature = coreStatus.temperature,
                        current = -(coreStatus.currentNow / 1000f), // Standardize: + = discharging, - = charging
                        chargingType = coreStatus.chargingType.name,
                        health = coreStatus.health,
                        technology = coreStatus.technology,
                        capacity = coreStatus.capacity,
                        cycleCount = coreStatus.cycleCount
                    ) to (-(coreStatus.currentNow / 1000f))
                }
                .collect { (status, currentMa) ->
                    _batteryStatus.value = status
                    _thermalData.value = status.temperature
                    appendCurrentSample(currentMa)
                    updateBatteryForecast(status)
                }
        }
    }
    
    // Feed current graph with real-time samples derived from BatteryMonitor
    private fun appendCurrentSample(milliAmps: Float) {
        val updated = (_currentData.value + milliAmps).takeLast(CURRENT_WINDOW_SIZE)
        _currentData.value = updated
    }

    private fun startCurrentPersistence() {
        viewModelScope.launch {
            // Persist and observe recent samples window (15 min)
            launch {
                // Start collecting BatteryMonitor in repository to persist samples
                currentRepository.currentSamples.collect { /* side-effect persistence */ }
            }
            // Observe recent samples for graph window
            currentRepository.getRecentSamples(WINDOW_MS).collect { samples ->
                _currentData.value = samples.takeLast(CURRENT_WINDOW_SIZE)
            }
        }
    }
    
    private fun generateBatteryTips() {
        val tips = listOf(
            "Enable dark mode to save battery",
            "Reduce screen brightness for longer battery life",
            "Close unused apps running in background",
            "Turn off location services when not needed",
            "Use Wi-Fi instead of mobile data when possible",
            "Enable battery saver mode at 20%",
            "Avoid extreme temperatures",
            "Don't let battery drain completely"
        )
        _batteryTips.value = tips.shuffled().take(3)
    }
    
    private fun updateBatteryForecast(status: BatteryStatus) {
        val level = status.level
        val charging = status.isCharging
        val forecast = when {
            charging -> "Fully charged in ${Random.nextInt(30, 120)} minutes"
            level > 50 -> "Battery will last ${Random.nextInt(4, 8)} hours"
            level > 20 -> "Battery will last ${Random.nextInt(2, 4)} hours"
            else -> "Battery critically low - charge soon"
        }
        _batteryForecast.value = forecast
    }
}