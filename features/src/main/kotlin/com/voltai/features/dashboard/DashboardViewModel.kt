package com.voltai.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.core.utils.BatteryHealthCalculator
import com.voltai.domain.model.BatteryStatus
import com.voltai.domain.repository.BatteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val batteryMonitor: com.voltai.domain.repository.IBatteryMonitor,
    private val currentRepository: com.voltai.domain.repository.ICurrentRepository,
    private val appUsageRepository: com.voltai.domain.repository.AppUsageRepository,
    private val usageStatsPermission: com.voltai.core.permissions.UsageStatsPermission
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
        private const val WINDOW_MS = 15 * 60 * 1000L // 15 minutes
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
    
    private val _batteryTips = MutableStateFlow<List<String>>(emptyList())
    val batteryTips: StateFlow<List<String>> = _batteryTips.asStateFlow()
    
    private val _batteryForecast = MutableStateFlow("")
    val batteryForecast: StateFlow<String> = _batteryForecast.asStateFlow()
    
    private val _thermalData = MutableStateFlow(25f)
    val thermalData: StateFlow<Float> = _thermalData.asStateFlow()

    // Imperative triggers from UI
    fun requestUsageStatsPermission() {
        _permissionRequests.tryEmit(Unit)
    }

    fun refreshUsageStatsPermission() {
        viewModelScope.launch {
            _hasUsageStatsPermission.value = usageStatsPermission.isGranted()
            if (_hasUsageStatsPermission.value) {
                observeUsageStats()
            }
        }
    }
    
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

    // Dashboard extra data
    private val _screenTimeMinutes = MutableStateFlow(0)
    val screenTimeMinutes: StateFlow<Int> = _screenTimeMinutes.asStateFlow()

    private val _usageTodayMah = MutableStateFlow(0)
    val usageTodayMah: StateFlow<Int> = _usageTodayMah.asStateFlow()

    private val _appUsage = MutableStateFlow<List<com.voltai.domain.model.AppUsage>>(emptyList())
    val appUsage: StateFlow<List<com.voltai.domain.model.AppUsage>> = _appUsage.asStateFlow()

    private val _usageError = MutableStateFlow<String?>(null)
    val usageError: StateFlow<String?> = _usageError.asStateFlow()

    private val _healthHistory = MutableStateFlow<List<Int>>(emptyList())
    val healthHistory: StateFlow<List<Int>> = _healthHistory.asStateFlow()

    // Usage Stats permission state for UI
    private val _hasUsageStatsPermission = MutableStateFlow(false)
    val hasUsageStatsPermission: StateFlow<Boolean> = _hasUsageStatsPermission.asStateFlow()

    // One-shot UI event to open settings
    private val _permissionRequests = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val permissionRequests: SharedFlow<Unit> = _permissionRequests.asSharedFlow()

    // Track usage stats collection to avoid duplicate collectors
    private var usageStatsJob: kotlinx.coroutines.Job? = null
    
    init {
        startBatteryMonitoring()
        startCurrentPersistence()
        generateBatteryTips()
        observeCurrentBatteryStatus()
        observeUsageStats()
        observeTodayUsage()
        observeHealthHistory()
        // Optionally expose permission state if UI wants to reactively show a banner/button
        _hasUsageStatsPermission.value = usageStatsPermission.isGranted()
    }
    
    private fun startBatteryMonitoring() {
        viewModelScope.launch {
            batteryMonitor.observeBatteryLogs()
                .collect { log ->
                    val status = com.voltai.domain.model.BatteryStatus(
                        percentage = log.batteryLevel,
                        status = if (log.current < 0f) "Charging" else "Discharging",
                        isCharging = log.current < 0f,
                        voltage = log.voltage,
                        temperature = log.temperature,
                        current = log.current,
                        chargingType = log.chargingType,
                        health = 0,
                        technology = "",
                        capacity = 0,
                        cycleCount = 0
                    )
                    _batteryStatus.value = status
                    _thermalData.value = status.temperature
                    appendCurrentSample(log.current)
                    updateBatteryForecast(status)
                    // persist graph samples
                    viewModelScope.launch { currentRepository.save(log) }
                    // persist full log for history
                    viewModelScope.launch { batteryRepository.insertBatteryLog(log) }
                }
        }
    }
    
    private fun observeCurrentBatteryStatus() {
        viewModelScope.launch {
            batteryRepository.getCurrentBatteryStatus()
                .filterNotNull()
                .collect { status ->
                    _batteryStatus.value = status
                    _thermalData.value = status.temperature
                    // naive health calculation demo
                    val healthPct = BatteryHealthCalculator.calculateHealth(
                        currentCapacity = _batteryHealth.value.currentCapacity,
                        designCapacity = _batteryHealth.value.designCapacity,
                        cycleCount = _batteryHealth.value.cycleCount,
                        temperature = status.temperature,
                        voltage = (status.voltage * 1000).toInt()
                    ).toInt()
                    _batteryHealth.value = _batteryHealth.value.copy(healthPercentage = healthPct)
                }
        }
    }
    
    // Feed current graph with real-time samples
    private fun appendCurrentSample(milliAmps: Float) {
        val updated = (_currentData.value + milliAmps)
            .takeLast(com.voltai.domain.DomainConstants.CURRENT_WINDOW_SIZE)
        _currentData.value = updated
    }

    private fun startCurrentPersistence() {
        viewModelScope.launch {
            currentRepository.getRecentLogs(com.voltai.domain.DomainConstants.CURRENT_WINDOW_SIZE)
                .collect { logs ->
                    _currentData.value = logs.map { it.current }
                }
        }
    }

    private fun observeUsageStats() {
        // Cancel previous collection if any to avoid duplicates
        usageStatsJob?.cancel()
        usageStatsJob = viewModelScope.launch {
            if (usageStatsPermission.isGranted()) {
                _usageError.value = null
                appUsageRepository.getAppUsageStats()
                    .catch { e ->
                        _usageError.value = e.message ?: "Failed to load usage stats"
                        _appUsage.value = emptyList()
                        _screenTimeMinutes.value = 0
                    }
                    .collect { usage ->
                        _appUsage.value = usage
                        val totalScreenTimeMs = usage.sumOf { it.totalTimeInForeground }
                        _screenTimeMinutes.value = (totalScreenTimeMs / 60000L).toInt()
                    }
            } else {
                _appUsage.value = emptyList()
                _screenTimeMinutes.value = 0
            }
        }
    }

    private fun observeTodayUsage() {
        viewModelScope.launch {
            // Approximate daily mAh usage using current logs in the last 24h
            currentRepository.getRecentLogs(com.voltai.domain.DomainConstants.CURRENT_WINDOW_SIZE)
                .collect { logs ->
                    val now = System.currentTimeMillis()
                    val since = now - DAY_MS
                    val dayLogs = logs.filter { it.timestamp in since..now }
                    // Simple trapezoidal approximation: sum(|current|) over samples scaled by sample interval
                    // If timestamps are irregular, assume median interval of 1 min
                    val medianIntervalMs = 60_000
                    val mah = dayLogs.map { kotlin.math.abs(it.current) }
                        .sum() * (medianIntervalMs / 3600000f)
                    _usageTodayMah.value = mah.toInt()
                }
        }
    }

    private fun observeHealthHistory() {
        viewModelScope.launch {
            // Derive pseudo health% from historical status to fill the sparkline until a true API exists
            batteryRepository.getAllBatteryLogs()
                .map { logs ->
                    val now = System.currentTimeMillis()
                    val sevenDaysAgo = now - 7L * DAY_MS
                    val daily = (0..6).map { d ->
                        val dayStart = now - (6 - d) * DAY_MS
                        val dayEnd = dayStart + DAY_MS
                        val dayLogs = logs.filter { it.timestamp in dayStart until dayEnd }
                        // Proxy: higher temps/currents slightly reduce estimated health for the day
                        val avgTemp = dayLogs.map { it.temperature }.ifEmpty { listOf(25f) }.average().toFloat()
                        val avgCurrent = dayLogs.map { kotlin.math.abs(it.current) }.ifEmpty { listOf(0f) }.average().toFloat()
                        val penalty = ((avgTemp - 25f) * 0.3f + avgCurrent * 0.02f).coerceAtLeast(0f)
                        (100f - penalty).toInt().coerceIn(80, 100)
                    }
                    daily
                }
                .collect { history -> _healthHistory.value = history }
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