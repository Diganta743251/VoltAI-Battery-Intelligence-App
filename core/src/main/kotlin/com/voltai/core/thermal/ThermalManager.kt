package com.voltai.core.thermal

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.os.Thermal
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class ThermalManager @Inject constructor(
    private val context: Context,
    private val thermalPreferences: ThermalPreferences
) : SensorEventListener {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    
    private val _thermalState = MutableStateFlow(ThermalState())
    val thermalState: StateFlow<ThermalState> = _thermalState.asStateFlow()
    
    private val _thermalHistory = MutableStateFlow<List<ThermalReading>>(emptyList())
    val thermalHistory: StateFlow<List<ThermalReading>> = _thermalHistory.asStateFlow()
    
    private val _thermalAlerts = MutableStateFlow<List<ThermalAlert>>(emptyList())
    val thermalAlerts: StateFlow<List<ThermalAlert>> = _thermalAlerts.asStateFlow()
    
    private var isMonitoring = false
    private var temperatureSensor: Sensor? = null
    private var ambientTemperatureSensor: Sensor? = null
    private var humiditySensor: Sensor? = null
    
    private val thermalReadings = mutableListOf<ThermalReading>()
    private val activeAlerts = mutableListOf<ThermalAlert>()
    
    companion object {
        private const val TEMPERATURE_THRESHOLD_WARNING = 35.0f
        private const val TEMPERATURE_THRESHOLD_CRITICAL = 40.0f
        private const val TEMPERATURE_THRESHOLD_EMERGENCY = 45.0f
        private const val THERMAL_MONITORING_INTERVAL = 10000L // 10 seconds
        private const val MAX_HISTORY_SIZE = 1000
        private const val COOLING_RECOMMENDATION_THRESHOLD = 38.0f
        private const val PERFORMANCE_THROTTLE_THRESHOLD = 42.0f
    }
    
    fun startThermalMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        initializeSensors()
        startSensorMonitoring()
        
        scope.launch {
            monitorThermalConditions()
        }
    }
    
    fun stopThermalMonitoring() {
        isMonitoring = false
        stopSensorMonitoring()
    }
    
    private fun initializeSensors() {
        // Initialize temperature sensors
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE)
        ambientTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
    }
    
    private fun startSensorMonitoring() {
        temperatureSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        ambientTemperatureSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        humiditySensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    
    private fun stopSensorMonitoring() {
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            val timestamp = System.currentTimeMillis()
            
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_TEMPERATURE -> {
                    updateDeviceTemperature(sensorEvent.values[0], timestamp)
                }
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    updateAmbientTemperature(sensorEvent.values[0], timestamp)
                }
                Sensor.TYPE_RELATIVE_HUMIDITY -> {
                    updateHumidity(sensorEvent.values[0], timestamp)
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes if needed
    }
    
    private suspend fun monitorThermalConditions() {
        while (isMonitoring) {
            try {
                val currentState = getCurrentThermalState()
                _thermalState.value = currentState
                
                // Add to history
                val reading = ThermalReading(
                    timestamp = System.currentTimeMillis(),
                    deviceTemperature = currentState.temperature,
                    ambientTemperature = currentState.ambientTemperature,
                    humidity = currentState.humidity,
                    thermalStatus = currentState.status,
                    cpuUsage = getCurrentCpuUsage(),
                    batteryTemperature = getBatteryTemperature()
                )
                
                addThermalReading(reading)
                
                // Check for thermal alerts
                checkThermalAlerts(currentState)
                
                // Apply thermal management policies
                applyThermalPolicies(currentState)
                
            } catch (e: Exception) {
                // Handle monitoring errors
                _thermalState.value = _thermalState.value.copy(
                    isError = true,
                    errorMessage = "Thermal monitoring error: ${e.message}"
                )
            }
            
            delay(THERMAL_MONITORING_INTERVAL)
        }
    }
    
    fun getCurrentThermalState(): ThermalState {
        val batteryTemp = getBatteryTemperature()
        val currentTemp = max(batteryTemp, _thermalState.value.temperature)
        
        val status = determineThermalStatus(currentTemp)
        val recommendations = generateCoolingRecommendations(currentTemp, status)
        
        return ThermalState(
            temperature = currentTemp,
            ambientTemperature = _thermalState.value.ambientTemperature,
            humidity = _thermalState.value.humidity,
            status = status,
            recommendations = recommendations,
            isThrottling = shouldThrottlePerformance(currentTemp),
            coolingActive = isCoolingActive(status),
            lastUpdate = LocalDateTime.now()
        )
    }
    
    private fun updateDeviceTemperature(temperature: Float, timestamp: Long) {
        _thermalState.value = _thermalState.value.copy(
            temperature = temperature,
            lastUpdate = LocalDateTime.now()
        )
    }
    
    private fun updateAmbientTemperature(temperature: Float, timestamp: Long) {
        _thermalState.value = _thermalState.value.copy(
            ambientTemperature = temperature,
            lastUpdate = LocalDateTime.now()
        )
    }
    
    private fun updateHumidity(humidity: Float, timestamp: Long) {
        _thermalState.value = _thermalState.value.copy(
            humidity = humidity,
            lastUpdate = LocalDateTime.now()
        )
    }
    
    private fun getBatteryTemperature(): Float {
        return try {
            val batteryStatus = context.registerReceiver(
                null, 
                android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            )
            val temperature = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            temperature / 10f // Convert from tenths of degrees to degrees Celsius
        } catch (e: Exception) {
            25.0f // Default temperature
        }
    }
    
    private fun getCurrentCpuUsage(): Float {
        return try {
            // Simplified CPU usage calculation
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            (usedMemory.toFloat() / totalMemory.toFloat()) * 100f
        } catch (e: Exception) {
            0f
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getThermalStatus(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                powerManager.currentThermalStatus
            } else {
                PowerManager.THERMAL_STATUS_NONE
            }
        } catch (e: Exception) {
            PowerManager.THERMAL_STATUS_NONE
        }
    }
    
    private fun determineThermalStatus(temperature: Float): ThermalStatus {
        return when {
            temperature >= TEMPERATURE_THRESHOLD_EMERGENCY -> ThermalStatus.EMERGENCY
            temperature >= TEMPERATURE_THRESHOLD_CRITICAL -> ThermalStatus.CRITICAL
            temperature >= TEMPERATURE_THRESHOLD_WARNING -> ThermalStatus.WARNING
            else -> ThermalStatus.NORMAL
        }
    }
    
    private fun shouldThrottlePerformance(temperature: Float): Boolean {
        return temperature >= PERFORMANCE_THROTTLE_THRESHOLD
    }
    
    private fun isCoolingActive(status: ThermalStatus): Boolean {
        return status != ThermalStatus.NORMAL
    }
    
    private fun generateCoolingRecommendations(temperature: Float, status: ThermalStatus): List<CoolingRecommendation> {
        val recommendations = mutableListOf<CoolingRecommendation>()
        
        when (status) {
            ThermalStatus.WARNING -> {
                recommendations.add(
                    CoolingRecommendation(
                        type = CoolingType.REDUCE_BRIGHTNESS,
                        title = "Reduce Screen Brightness",
                        description = "Lower screen brightness to reduce heat generation",
                        priority = RecommendationPriority.MEDIUM
                    )
                )
                recommendations.add(
                    CoolingRecommendation(
                        type = CoolingType.CLOSE_APPS,
                        title = "Close Background Apps",
                        description = "Close resource-intensive apps to reduce CPU load",
                        priority = RecommendationPriority.MEDIUM
                    )
                )
            }
            
            ThermalStatus.CRITICAL -> {
                recommendations.add(
                    CoolingRecommendation(
                        type = CoolingType.ENABLE_POWER_SAVER,
                        title = "Enable Battery Saver",
                        description = "Activate power saving mode to reduce heat generation",
                        priority = RecommendationPriority.HIGH
                    )
                )
                recommendations.add(
                    CoolingRecommendation(
                        type = CoolingType.PAUSE_CHARGING,
                        title = "Pause Charging",
                        description = "Temporarily pause charging to prevent overheating",
                        priority = RecommendationPriority.HIGH
                    )
                )
                recommendations.add(
                    CoolingRecommendation(
                        type = CoolingType.REMOVE_CASE,
                        title = "Remove Device Case",
                        description = "Remove any protective cases to improve heat dissipation",
                        priority = RecommendationPriority.MEDIUM
                    )
                )
            }
            
            ThermalStatus.EMERGENCY -> {
                recommendations.add(
                    CoolingRecommendation(
                        type = CoolingType.IMMEDIATE_COOLING,
                        title = "Cool Device Immediately",
                        description = "Move to a cooler environment and stop all intensive tasks",
                        priority = RecommendationPriority.CRITICAL
                    )
                )
                recommendations.add(
                    CoolingRecommendation(
                        type = CoolingType.SYSTEM_SHUTDOWN,
                        title = "Consider Device Rest",
                        description = "Consider turning off the device to prevent damage",
                        priority = RecommendationPriority.CRITICAL
                    )
                )
            }
            
            ThermalStatus.NORMAL -> {
                // No recommendations needed
            }
        }
        
        return recommendations
    }
    
    private fun checkThermalAlerts(state: ThermalState) {
        val now = LocalDateTime.now()
        
        // Clear old alerts
        activeAlerts.removeAll { alert -> 
            alert.timestamp.isBefore(now.minusMinutes(30)) 
        }
        
        // Check for new alerts
        when (state.status) {
            ThermalStatus.WARNING -> {
                if (!hasActiveAlert(ThermalAlertType.HIGH_TEMPERATURE)) {
                    val alert = ThermalAlert(
                        type = ThermalAlertType.HIGH_TEMPERATURE,
                        title = "Device Temperature Warning",
                        message = "Device temperature is ${String.format("%.1f", state.temperature)}°C",
                        severity = AlertSeverity.WARNING,
                        temperature = state.temperature,
                        timestamp = now,
                        recommendations = state.recommendations
                    )
                    activeAlerts.add(alert)
                }
            }
            
            ThermalStatus.CRITICAL -> {
                if (!hasActiveAlert(ThermalAlertType.CRITICAL_TEMPERATURE)) {
                    val alert = ThermalAlert(
                        type = ThermalAlertType.CRITICAL_TEMPERATURE,
                        title = "Critical Temperature Alert",
                        message = "Device temperature is critically high at ${String.format("%.1f", state.temperature)}°C",
                        severity = AlertSeverity.CRITICAL,
                        temperature = state.temperature,
                        timestamp = now,
                        recommendations = state.recommendations
                    )
                    activeAlerts.add(alert)
                }
            }
            
            ThermalStatus.EMERGENCY -> {
                if (!hasActiveAlert(ThermalAlertType.EMERGENCY_SHUTDOWN)) {
                    val alert = ThermalAlert(
                        type = ThermalAlertType.EMERGENCY_SHUTDOWN,
                        title = "Emergency Temperature Alert",
                        message = "Device temperature is dangerously high at ${String.format("%.1f", state.temperature)}°C",
                        severity = AlertSeverity.EMERGENCY,
                        temperature = state.temperature,
                        timestamp = now,
                        recommendations = state.recommendations
                    )
                    activeAlerts.add(alert)
                }
            }
            
            ThermalStatus.NORMAL -> {
                // Remove temperature-related alerts when temperature normalizes
                activeAlerts.removeAll { alert -> 
                    alert.type in listOf(
                        ThermalAlertType.HIGH_TEMPERATURE,
                        ThermalAlertType.CRITICAL_TEMPERATURE,
                        ThermalAlertType.EMERGENCY_SHUTDOWN
                    )
                }
            }
        }
        
        _thermalAlerts.value = activeAlerts.toList()
    }
    
    private fun hasActiveAlert(type: ThermalAlertType): Boolean {
        return activeAlerts.any { it.type == type }
    }
    
    private fun applyThermalPolicies(state: ThermalState) {
        when (state.status) {
            ThermalStatus.CRITICAL, ThermalStatus.EMERGENCY -> {
                // Broadcast thermal throttling event
                broadcastThermalEvent(state)
                
                // Request power saving mode if available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    requestPowerSaveMode(true)
                }
            }
            
            ThermalStatus.NORMAL -> {
                // Remove throttling
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    requestPowerSaveMode(false)
                }
            }
            
            else -> {
                // Intermediate thermal state - moderate throttling
            }
        }
    }
    
    private fun broadcastThermalEvent(state: ThermalState) {
        val intent = android.content.Intent("com.voltai.THERMAL_EVENT").apply {
            putExtra("temperature", state.temperature)
            putExtra("status", state.status.name)
            putExtra("throttling", state.isThrottling)
        }
        context.sendBroadcast(intent)
    }
    
    private fun requestPowerSaveMode(enable: Boolean) {
        // This would require system-level permissions
        // For now, we'll just broadcast the intent for other components to handle
        val intent = android.content.Intent("com.voltai.POWER_SAVE_REQUEST").apply {
            putExtra("enable", enable)
        }
        context.sendBroadcast(intent)
    }
    
    private fun addThermalReading(reading: ThermalReading) {
        thermalReadings.add(reading)
        
        // Maintain history size
        if (thermalReadings.size > MAX_HISTORY_SIZE) {
            thermalReadings.removeFirst()
        }
        
        _thermalHistory.value = thermalReadings.toList()
    }
    
    fun getThermalHistory(hours: Int = 24): List<ThermalReading> {
        val cutoffTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000)
        return thermalReadings.filter { it.timestamp >= cutoffTime }
    }
    
    fun getAverageTemperature(hours: Int = 1): Float {
        val recentReadings = getThermalHistory(hours)
        return if (recentReadings.isNotEmpty()) {
            recentReadings.map { it.deviceTemperature }.average().toFloat()
        } else {
            _thermalState.value.temperature
        }
    }
    
    fun getMaxTemperature(hours: Int = 1): Float {
        val recentReadings = getThermalHistory(hours)
        return if (recentReadings.isNotEmpty()) {
            recentReadings.maxOf { it.deviceTemperature }
        } else {
            _thermalState.value.temperature
        }
    }
    
    fun clearThermalAlerts() {
        activeAlerts.clear()
        _thermalAlerts.value = emptyList()
    }
}

// Data classes and enums for thermal management
data class ThermalState(
    val temperature: Float = 25.0f,
    val ambientTemperature: Float = 25.0f,
    val humidity: Float = 50.0f,
    val status: ThermalStatus = ThermalStatus.NORMAL,
    val recommendations: List<CoolingRecommendation> = emptyList(),
    val isThrottling: Boolean = false,
    val coolingActive: Boolean = false,
    val lastUpdate: LocalDateTime = LocalDateTime.now(),
    val isError: Boolean = false,
    val errorMessage: String? = null
)

data class ThermalReading(
    val timestamp: Long,
    val deviceTemperature: Float,
    val ambientTemperature: Float,
    val humidity: Float,
    val thermalStatus: ThermalStatus,
    val cpuUsage: Float,
    val batteryTemperature: Float
)

data class ThermalAlert(
    val type: ThermalAlertType,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val temperature: Float,
    val timestamp: LocalDateTime,
    val recommendations: List<CoolingRecommendation>
)

data class CoolingRecommendation(
    val type: CoolingType,
    val title: String,
    val description: String,
    val priority: RecommendationPriority
)

enum class ThermalStatus {
    NORMAL,
    WARNING,
    CRITICAL,
    EMERGENCY
}

enum class ThermalAlertType {
    HIGH_TEMPERATURE,
    CRITICAL_TEMPERATURE,
    EMERGENCY_SHUTDOWN,
    THERMAL_THROTTLING,
    COOLING_ACTIVATED
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL,
    EMERGENCY
}

enum class CoolingType {
    REDUCE_BRIGHTNESS,
    CLOSE_APPS,
    ENABLE_POWER_SAVER,
    PAUSE_CHARGING,
    REMOVE_CASE,
    IMMEDIATE_COOLING,
    SYSTEM_SHUTDOWN
}

enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
