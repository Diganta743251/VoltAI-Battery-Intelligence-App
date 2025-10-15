package com.voltai.core.charging

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import com.voltai.core.health.BatteryHealthAnalyzer
import com.voltai.core.health.BatteryHealthData
import com.voltai.core.thermal.ThermalManager
import com.voltai.core.thermal.ThermalState

@Singleton
class SmartChargingScheduler @Inject constructor(
    private val context: Context,
    private val chargingPreferences: ChargingPreferences,
    private val batteryHealthAnalyzer: BatteryHealthAnalyzer,
    private val thermalManager: ThermalManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _schedulerState = MutableStateFlow(ChargingSchedulerState())
    val schedulerState: StateFlow<ChargingSchedulerState> = _schedulerState.asStateFlow()
    
    private val _chargingRecommendations = MutableStateFlow<List<ChargingRecommendation>>(emptyList())
    val chargingRecommendations: StateFlow<List<ChargingRecommendation>> = _chargingRecommendations.asStateFlow()
    
    private var isActive = false
    
    companion object {
        private const val OPTIMAL_CHARGE_START = 20
        private const val OPTIMAL_CHARGE_END = 80
        private const val HEALTH_PRESERVATION_END = 85
        private const val EMERGENCY_CHARGE_END = 95
        private const val TEMPERATURE_THRESHOLD = 35.0f
        private const val CHARGING_ANALYSIS_INTERVAL = 30000L // 30 seconds
    }
    
    fun startScheduler() {
        if (isActive) return
        
        isActive = true
        scope.launch {
            monitorChargingConditions()
        }
    }
    
    fun stopScheduler() {
        isActive = false
    }
    
    private suspend fun monitorChargingConditions() {
        while (isActive) {
            try {
                val currentBatteryInfo = getCurrentBatteryInfo()
                val userPreferences = chargingPreferences.getPreferences()
                val healthData = batteryHealthAnalyzer.analyzeBatteryHealth()
                val thermalData = thermalManager.getCurrentThermalState()
                
                // Analyze current charging conditions
                val analysis = analyzeChargingConditions(
                    currentBatteryInfo,
                    userPreferences,
                    healthData,
                    thermalData
                )
                
                // Update scheduler state
                _schedulerState.value = analysis.schedulerState
                _chargingRecommendations.value = analysis.recommendations
                
                // Execute charging decisions if connected
                if (currentBatteryInfo.isPluggedIn) {
                    executeChargingDecision(analysis)
                }
                
            } catch (e: Exception) {
                // Handle errors gracefully
                _schedulerState.value = _schedulerState.value.copy(
                    isError = true,
                    errorMessage = "Scheduler error: ${e.message}"
                )
            }
            
            delay(CHARGING_ANALYSIS_INTERVAL)
        }
    }
    
    private fun analyzeChargingConditions(
        batteryInfo: BatteryInfo,
        preferences: ChargingUserPreferences,
        healthData: BatteryHealthData,
        thermalData: ThermalState
    ): ChargingAnalysis {
        
        val recommendations = mutableListOf<ChargingRecommendation>()
        val now = LocalDateTime.now()
        
        // Determine optimal charging strategy
        val strategy = determineChargingStrategy(batteryInfo, preferences, healthData, thermalData)
        
        // Calculate charging schedule
        val schedule = calculateOptimalChargingSchedule(batteryInfo, preferences, strategy)
        
        // Generate recommendations
        recommendations.addAll(generateChargingRecommendations(batteryInfo, preferences, healthData, thermalData))
        
        // Determine charging action
        val shouldCharge = shouldStartCharging(batteryInfo, preferences, schedule, thermalData)
        val shouldPause = shouldPauseCharging(batteryInfo, preferences, thermalData)
        
        val schedulerState = ChargingSchedulerState(
            isActive = isActive,
            currentStrategy = strategy,
            schedule = schedule,
            shouldCharge = shouldCharge,
            shouldPause = shouldPause,
            nextScheduledTime = schedule.nextChargingWindow?.startTime,
            batteryLevel = batteryInfo.level,
            temperature = batteryInfo.temperature,
            isPluggedIn = batteryInfo.isPluggedIn,
            estimatedChargingTime = calculateEstimatedChargingTime(batteryInfo, strategy),
            lastUpdate = now
        )
        
        return ChargingAnalysis(schedulerState, recommendations)
    }
    
    private fun determineChargingStrategy(
        batteryInfo: BatteryInfo,
        preferences: ChargingUserPreferences,
        healthData: BatteryHealthData,
        thermalData: ThermalState
    ): ChargingStrategy {
        
        // Emergency charging if battery is critically low
        if (batteryInfo.level <= 5) {
            return ChargingStrategy.EMERGENCY
        }
        
        // Thermal protection - slow charging if device is hot
        if (thermalData.temperature > TEMPERATURE_THRESHOLD) {
            return ChargingStrategy.THERMAL_PROTECTION
        }
        
        // Health preservation for aging batteries
        if (healthData.healthPercentage < 80) {
            return ChargingStrategy.HEALTH_PRESERVATION
        }
        
        // Adaptive fast charging for healthy batteries
        if (healthData.healthPercentage >= 90 && batteryInfo.level < 50) {
            return ChargingStrategy.ADAPTIVE_FAST
        }
        
        // Default optimized charging
        return when (preferences.chargingMode) {
            ChargingMode.BALANCED -> ChargingStrategy.BALANCED
            ChargingMode.PERFORMANCE -> ChargingStrategy.PERFORMANCE
            ChargingMode.LONGEVITY -> ChargingStrategy.LONGEVITY
            ChargingMode.CUSTOM -> ChargingStrategy.CUSTOM
        }
    }
    
    private fun calculateOptimalChargingSchedule(
        batteryInfo: BatteryInfo,
        preferences: ChargingUserPreferences,
        strategy: ChargingStrategy
    ): ChargingSchedule {
        
        val now = LocalDateTime.now()
        val chargingWindows = mutableListOf<ChargingWindow>()
        
        // Calculate based on user's typical usage patterns
        preferences.chargingTimeWindows.forEach { window ->
            val startTime = LocalDateTime.of(now.toLocalDate(), window.startTime)
            val endTime = LocalDateTime.of(now.toLocalDate(), window.endTime)
            
            // Adjust for next day if time has passed
            val adjustedStartTime = if (startTime.isBefore(now)) startTime.plusDays(1) else startTime
            val adjustedEndTime = if (endTime.isBefore(adjustedStartTime)) endTime.plusDays(1) else endTime
            
            val targetLevel = calculateTargetLevel(strategy, preferences)
            val estimatedDuration = calculateChargingDuration(batteryInfo.level, targetLevel, strategy)
            
            chargingWindows.add(
                ChargingWindow(
                    startTime = adjustedStartTime,
                    endTime = adjustedEndTime,
                    targetLevel = targetLevel,
                    estimatedDuration = estimatedDuration,
                    priority = window.priority
                )
            )
        }
        
        // Sort by priority and time
        chargingWindows.sortWith(compareBy<ChargingWindow> { it.priority }.thenBy { it.startTime })
        
        return ChargingSchedule(
            windows = chargingWindows,
            nextChargingWindow = chargingWindows.firstOrNull { it.startTime.isAfter(now) },
            strategy = strategy,
            createdAt = now
        )
    }
    
    private fun calculateTargetLevel(strategy: ChargingStrategy, preferences: ChargingUserPreferences): Int {
        return when (strategy) {
            ChargingStrategy.EMERGENCY -> EMERGENCY_CHARGE_END
            ChargingStrategy.HEALTH_PRESERVATION -> HEALTH_PRESERVATION_END
            ChargingStrategy.LONGEVITY -> OPTIMAL_CHARGE_END
            ChargingStrategy.BALANCED -> min(preferences.maxChargeLevel, 85)
            ChargingStrategy.PERFORMANCE -> min(preferences.maxChargeLevel, 95)
            ChargingStrategy.ADAPTIVE_FAST -> min(preferences.maxChargeLevel, 90)
            ChargingStrategy.THERMAL_PROTECTION -> min(preferences.maxChargeLevel, 80)
            ChargingStrategy.CUSTOM -> preferences.customTargetLevel
        }
    }
    
    private fun calculateChargingDuration(currentLevel: Int, targetLevel: Int, strategy: ChargingStrategy): Long {
        val levelDifference = targetLevel - currentLevel
        if (levelDifference <= 0) return 0L
        
        // Charging speed in percentage per minute based on strategy
        val chargingSpeed = when (strategy) {
            ChargingStrategy.EMERGENCY -> 2.0 // Fast charging
            ChargingStrategy.ADAPTIVE_FAST -> 1.8
            ChargingStrategy.PERFORMANCE -> 1.5
            ChargingStrategy.BALANCED -> 1.2
            ChargingStrategy.LONGEVITY -> 0.8 // Slow charging for health
            ChargingStrategy.HEALTH_PRESERVATION -> 0.9
            ChargingStrategy.THERMAL_PROTECTION -> 0.6 // Very slow for cooling
            ChargingStrategy.CUSTOM -> 1.0
        }
        
        return (levelDifference / chargingSpeed * 60 * 1000).toLong() // Convert to milliseconds
    }
    
    private fun shouldStartCharging(
        batteryInfo: BatteryInfo,
        preferences: ChargingUserPreferences,
        schedule: ChargingSchedule,
        thermalData: ThermalState
    ): Boolean {
        
        if (!batteryInfo.isPluggedIn) return false
        if (batteryInfo.level >= preferences.maxChargeLevel) return false
        if (thermalData.temperature > TEMPERATURE_THRESHOLD + 5) return false // Too hot
        
        val now = LocalDateTime.now()
        
        // Emergency charging
        if (batteryInfo.level <= preferences.emergencyChargeThreshold) return true
        
        // Check if within scheduled charging window
        schedule.nextChargingWindow?.let { window ->
            if (now.isAfter(window.startTime) && now.isBefore(window.endTime)) {
                return true
            }
        }
        
        // Immediate charging if enabled and below minimum level
        if (preferences.enableImmediateCharging && batteryInfo.level <= preferences.minChargeLevel) {
            return true
        }
        
        return false
    }
    
    private fun shouldPauseCharging(
        batteryInfo: BatteryInfo,
        preferences: ChargingUserPreferences,
        thermalData: ThermalState
    ): Boolean {
        
        if (!batteryInfo.isCharging) return false
        
        // Pause if too hot
        if (thermalData.temperature > TEMPERATURE_THRESHOLD + 3) return true
        
        // Pause if target level reached
        if (batteryInfo.level >= preferences.maxChargeLevel) return true
        
        // Pause if outside charging window (for scheduled charging)
        if (preferences.enableScheduledCharging) {
            val now = LocalDateTime.now()
            val currentTime = now.toLocalTime()
            
            val inWindow = preferences.chargingTimeWindows.any { window ->
                currentTime.isAfter(window.startTime) && currentTime.isBefore(window.endTime)
            }
            
            if (!inWindow) return true
        }
        
        return false
    }
    
    private fun generateChargingRecommendations(
        batteryInfo: BatteryInfo,
        preferences: ChargingUserPreferences,
        healthData: BatteryHealthData,
        thermalData: ThermalState
    ): List<ChargingRecommendation> {
        
        val recommendations = mutableListOf<ChargingRecommendation>()
        val now = LocalDateTime.now()
        
        // Temperature recommendations
        if (thermalData.temperature > TEMPERATURE_THRESHOLD) {
            recommendations.add(
                ChargingRecommendation(
                    type = RecommendationType.THERMAL_WARNING,
                    title = "Device Running Hot",
                    description = "Device temperature is ${String.format("%.1f", thermalData.temperature)}°C. Consider cooling down before charging.",
                    priority = RecommendationPriority.HIGH,
                    actionText = "Cool Down",
                    createdAt = now
                )
            )
        }
        
        // Battery health recommendations
        if (healthData.healthPercentage < 80) {
            recommendations.add(
                ChargingRecommendation(
                    type = RecommendationType.HEALTH_OPTIMIZATION,
                    title = "Battery Health Declining",
                    description = "Your battery health is at ${healthData.healthPercentage}%. Consider using slower charging speeds.",
                    priority = RecommendationPriority.MEDIUM,
                    actionText = "Optimize Settings",
                    createdAt = now
                )
            )
        }
        
        // Charging pattern recommendations
        if (batteryInfo.level < OPTIMAL_CHARGE_START) {
            recommendations.add(
                ChargingRecommendation(
                    type = RecommendationType.CHARGING_PATTERN,
                    title = "Low Battery Level",
                    description = "Battery is at ${batteryInfo.level}%. Consider charging to extend device life.",
                    priority = if (batteryInfo.level < 10) RecommendationPriority.HIGH else RecommendationPriority.MEDIUM,
                    actionText = "Charge Now",
                    createdAt = now
                )
            )
        }
        
        if (batteryInfo.level > OPTIMAL_CHARGE_END && batteryInfo.isCharging) {
            recommendations.add(
                ChargingRecommendation(
                    type = RecommendationType.CHARGING_PATTERN,
                    title = "Optimal Charge Level Reached",
                    description = "Battery is at ${batteryInfo.level}%. Consider unplugging to preserve battery health.",
                    priority = RecommendationPriority.LOW,
                    actionText = "Unplug",
                    createdAt = now
                )
            )
        }
        
        // Scheduled charging recommendations
        if (!preferences.enableScheduledCharging && healthData.cycleCount > 100) {
            recommendations.add(
                ChargingRecommendation(
                    type = RecommendationType.FEATURE_SUGGESTION,
                    title = "Enable Scheduled Charging",
                    description = "Scheduled charging can help preserve battery health by avoiding overnight overcharging.",
                    priority = RecommendationPriority.LOW,
                    actionText = "Enable",
                    createdAt = now
                )
            )
        }
        
        return recommendations
    }
    
    private fun executeChargingDecision(analysis: ChargingAnalysis) {
        val state = analysis.schedulerState
        
        // This would integrate with system-level charging controls
        // For now, we'll log the decisions and update notifications
        when {
            state.shouldCharge && !state.shouldPause -> {
                // Signal to start/continue charging
                notifyChargingDecision(ChargingDecision.START_CHARGING, state)
            }
            state.shouldPause -> {
                // Signal to pause charging
                notifyChargingDecision(ChargingDecision.PAUSE_CHARGING, state)
            }
            else -> {
                // Continue current state
                notifyChargingDecision(ChargingDecision.MAINTAIN_CURRENT, state)
            }
        }
    }
    
    private fun notifyChargingDecision(decision: ChargingDecision, state: ChargingSchedulerState) {
        // Send broadcast or notification about charging decision
        val intent = Intent("com.voltai.CHARGING_DECISION").apply {
            putExtra("decision", decision.name)
            putExtra("battery_level", state.batteryLevel)
            putExtra("temperature", state.temperature)
            putExtra("strategy", state.currentStrategy.name)
        }
        context.sendBroadcast(intent)
    }
    
    private fun getCurrentBatteryInfo(): BatteryInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryStatus = context.registerReceiver(null, 
            android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val temperature = (batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val voltage = (batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        
        val batteryLevel = (level * 100) / scale
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
        val isPluggedIn = plugged > 0
        
        return BatteryInfo(
            level = batteryLevel,
            temperature = temperature,
            voltage = voltage,
            isCharging = isCharging,
            isPluggedIn = isPluggedIn,
            health = 100, // Default to good health
            status = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                else -> "Unknown"
            }
        )
    }
    
    private fun calculateEstimatedChargingTime(batteryInfo: BatteryInfo, strategy: ChargingStrategy): Long {
        if (!batteryInfo.isPluggedIn || !batteryInfo.isCharging) return 0L
        
        val targetLevel = when (strategy) {
            ChargingStrategy.LONGEVITY -> OPTIMAL_CHARGE_END
            ChargingStrategy.HEALTH_PRESERVATION -> HEALTH_PRESERVATION_END
            else -> 90 // Default target
        }
        
        return calculateChargingDuration(batteryInfo.level, targetLevel, strategy)
    }
}

enum class ChargingDecision {
    START_CHARGING,
    PAUSE_CHARGING,
    MAINTAIN_CURRENT
}
