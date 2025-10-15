package com.voltai.smartbatteryguardian.data

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import androidx.annotation.RequiresApi
import com.voltai.smartbatteryguardian.data.database.BatteryRepository
import com.voltai.smartbatteryguardian.data.database.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Enhanced battery data collector with real sensor integration
 * Collects comprehensive battery, system, and usage metrics
 */
@Singleton
class BatteryDataCollector
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val repository: BatteryRepository,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val usageStatsManager =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        } else {
            null
        }

    // Sensor data
    private var currentAcceleration = FloatArray(3)
    private var currentGyroscope = FloatArray(3)
    private var currentMagnetometer = FloatArray(3)
    private var ambientTemperature = 0f
    private var lightLevel = 0f
    private var proximity = 0f

    init {
        initializeSensors()
    }

    /**
     * Initialize sensor listeners for enhanced data collection
     */
    private fun initializeSensors() {
        // Register for battery-relevant sensors
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * Collect comprehensive battery data
     */
    suspend fun collectBatteryData(): BatteryReading =
        withContext(Dispatchers.IO) {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPercentage = if (level >= 0 && scale > 0) (level * 100) / scale else -1

            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
            val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
            val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
            val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

            BatteryReading(
                batteryLevel = batteryPercentage,
                isCharging = isCharging,
                chargingType = getChargingType(plugged),
                voltage = voltage / 1000.0, // Convert to volts
                current = getCurrentCurrent(),
                temperature = temperature / 10.0, // Convert to Celsius
                capacity = getBatteryCapacity(),
                health = mapBatteryHealth(health),
                technology = technology,
                plugType = getPlugType(plugged),
                powerSource = getPowerSource(isCharging, plugged),
                chargingSpeed = getChargingSpeed(isCharging),
                estimatedTimeRemaining = getEstimatedTimeRemainingMillis(batteryPercentage, isCharging),
                cycleCount = getBatteryCycleCount(),
                wearLevel = getBatteryWearLevel(),
            )
        }

    /**
     * Collect comprehensive system metrics
     */
    suspend fun collectSystemMetrics(): SystemMetrics =
        withContext(Dispatchers.IO) {
            SystemMetrics(
                cpuUsage = getCpuUsage(),
                memoryUsage = getMemoryUsage(),
                storageUsage = getStorageUsage(),
                networkUsage = getNetworkUsage(),
                thermalState = getThermalState(),
                batteryTemperature = getCurrentBatteryTemperature(),
                systemTemperature = getSystemTemperature(),
                performanceScore = calculatePerformanceScore(),
                stabilityScore = calculateStabilityScore(),
                efficiencyScore = calculateEfficiencyScore(),
                backgroundProcesses = getBackgroundProcessCount(),
                foregroundApps = getForegroundAppCount(),
                wakelocksActive = getActiveWakelocksCount(),
                networkConnections = getNetworkConnectionsCount(),
                locationRequests = getLocationRequestsCount(),
            )
        }

    /**
     * Collect app usage statistics
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun collectAppUsageStats(): List<AppUsageStats> =
        withContext(Dispatchers.IO) {
            if (usageStatsManager == null) return@withContext emptyList()

            val endTime = System.currentTimeMillis()
            val startTime = endTime - (24 * 60 * 60 * 1000) // Last 24 hours

            val usageStats =
                usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime,
                )

            usageStats.map { stat ->
                AppUsageStats(
                    packageName = stat.packageName,
                    appName = getAppName(stat.packageName),
                    category = getAppCategory(stat.packageName),
                    foregroundTime = stat.totalTimeInForeground,
                    backgroundTime = 0L, // Usage API doesn't provide background time directly
                    batteryUsage = estimateAppBatteryUsage(stat.packageName, stat.totalTimeInForeground),
                    cpuUsage = 0.0, // Placeholder - would need profiling APIs
                    memoryUsage = 0L, // Placeholder - would need ActivityManager
                    networkUsage = getAppDataUsage(stat.packageName).toLong(),
                    wakelocksHeld = 0, // Placeholder - would need system access
                    notificationCount = 0, // Placeholder - would need notification access
                    launchCount = 1, // Simplified - actual count would need usage events
                    crashCount = 0,
                    dataUsage = getAppDataUsage(stat.packageName).toLong(),
                    gpsUsage = 0L, // Placeholder - would need location usage tracking
                )
            }
        }

    /**
     * Real-time battery monitoring flow
     */
    fun getBatteryFlow(): Flow<BatteryReading> =
        flow {
            while (true) {
                val reading = collectBatteryData()
                emit(reading)
                repository.recordBatteryReading(reading)
                kotlinx.coroutines.delay(5000) // Every 5 seconds
            }
        }.flowOn(Dispatchers.IO)

    /**
     * Real-time system metrics flow
     */
    fun getSystemMetricsFlow(): Flow<SystemMetrics> =
        flow {
            while (true) {
                val metrics = collectSystemMetrics()
                emit(metrics)
                repository.recordSystemMetrics(metrics)
                kotlinx.coroutines.delay(30000) // Every 30 seconds
            }
        }.flowOn(Dispatchers.IO)

    // Sensor event handling
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    currentAcceleration = it.values.clone()
                }
                Sensor.TYPE_GYROSCOPE -> {
                    currentGyroscope = it.values.clone()
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    currentMagnetometer = it.values.clone()
                }
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    ambientTemperature = it.values[0]
                }
                Sensor.TYPE_LIGHT -> {
                    lightLevel = it.values[0]
                }
                Sensor.TYPE_PROXIMITY -> {
                    proximity = it.values[0]
                }
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int,
    ) {
        // Handle accuracy changes if needed
    }

    // Private helper methods
    private fun mapBatteryHealth(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failed"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }

    /**
     * Gets current battery current in milliamps (mA)
     * @return Current in mA. Negative values indicate charging, positive indicate discharging.
     * NOTE: Returns fallback value of -500.0 mA if API unavailable (pre-Lollipop or on error)
     */
    private fun getCurrentCurrent(): Double {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val ua = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                if (ua == Int.MIN_VALUE) 0.0 else ua / 1000.0 // mA
            } else {
                // FALLBACK: Pre-Lollipop devices - typical discharge current estimate
                -500.0
            }
        } catch (e: Exception) {
            // FALLBACK: Default to typical discharge current on error
            -500.0
        }
    }

    /**
     * Gets battery capacity in mAh
     * @return Battery capacity in mAh.
     * NOTE: Returns fallback value of 4000.0 mAh if real capacity unavailable.
     * This is a common battery capacity for modern smartphones but NOT device-specific.
     */
    private fun getBatteryCapacity(): Double {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000.0 // Convert to mAh
            } else {
                // FALLBACK: Try reading from system files (usually requires root)
                val capacityFile = File("/sys/class/power_supply/battery/capacity")
                if (capacityFile.exists()) {
                    capacityFile.readText().trim().toDoubleOrNull() ?: 4000.0
                } else {
                    // FALLBACK: Generic smartphone battery capacity estimate
                    4000.0
                }
            }
        } catch (e: Exception) {
            // FALLBACK: Generic smartphone battery capacity estimate
            4000.0
        }
    }

    /**
     * Estimates charging speed in watts
     * @param isCharging Whether device is currently charging
     * @return Charging speed in watts, or 0.0 if not charging
     * NOTE: Uses nominal voltage of 3.7V for estimation when real voltage unavailable.
     * Falls back to 15W (typical fast charging) on error.
     */
    private fun getChargingSpeed(isCharging: Boolean): Double {
        return if (isCharging) {
            try {
                // Estimate charging speed based on current and voltage
                val current = kotlin.math.abs(getCurrentCurrent())
                // HEURISTIC: Using nominal Li-ion voltage since real voltage may not be available
                val voltage = 3.7
                (current * voltage) / 1000.0 // Convert to watts
            } catch (e: Exception) {
                // FALLBACK: Typical fast charging speed
                15.0
            }
        } else {
            0.0
        }
    }

    private fun getChargingType(plugged: Int): String? {
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> null
        }
    }

    private fun getPlugType(plugged: Int): String? {
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> null
        }
    }

    private fun getPowerSource(
        isCharging: Boolean,
        plugged: Int,
    ): String? {
        return if (isCharging) {
            when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Charger"
                else -> "External Power"
            }
        } else {
            "Battery"
        }
    }

    private fun getEstimatedTimeRemainingMillis(
        batteryLevel: Int,
        isCharging: Boolean,
    ): Long? {
        return try {
            if (isCharging) {
                val remainingCapacity = 100 - batteryLevel
                val chargingRate = kotlin.math.abs(getCurrentCurrent()) / 1000.0 // Convert to A
                if (chargingRate > 0) {
                    val hoursRemaining = (remainingCapacity * getBatteryCapacity() / 100.0) / (chargingRate * 1000.0)
                    (hoursRemaining * 60 * 60 * 1000).toLong() // Convert to milliseconds
                } else {
                    null
                }
            } else {
                val dischargeRate = kotlin.math.abs(getCurrentCurrent()) / 1000.0
                if (dischargeRate > 0) {
                    val hoursRemaining = (batteryLevel * getBatteryCapacity() / 100.0) / (dischargeRate * 1000.0)
                    (hoursRemaining * 60 * 60 * 1000).toLong() // Convert to milliseconds
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getBatteryCycleCount(): Int? {
        return try {
            // Try to read from system files (usually requires root)
            val cycleFile = File("/sys/class/power_supply/battery/cycle_count")
            if (cycleFile.exists()) {
                cycleFile.readText().trim().toIntOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculates battery wear level as percentage
     * @return Wear level (0-100%) or null if cannot be determined
     * NOTE: Uses generic 4000 mAh design capacity - NOT device-specific.
     * Results are estimates only until real design capacity is available.
     */
    private fun getBatteryWearLevel(): Double? {
        return try {
            // HEURISTIC: Generic design capacity - should be replaced with device-specific value
            val designCapacity = 4000.0 // mAh
            val currentCapacity = getBatteryCapacity()
            if (currentCapacity > 0) {
                100.0 - (currentCapacity / designCapacity * 100.0)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getCpuUsage(): Double {
        return try {
            val stat1 = readCpuStat()
            Thread.sleep(100) // Small delay
            val stat2 = readCpuStat()

            val idle = stat2[3] - stat1[3]
            val total = stat2.sum() - stat1.sum()

            if (total > 0) {
                100.0 - (idle * 100.0 / total)
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun readCpuStat(): LongArray {
        return try {
            RandomAccessFile("/proc/stat", "r").use { file ->
                val line = file.readLine()
                val tokens = line.split(" ").filter { it.isNotEmpty() }
                longArrayOf(
                    tokens[1].toLong(), // user
                    tokens[2].toLong(), // nice
                    tokens[3].toLong(), // system
                    tokens[4].toLong(), // idle
                    tokens[5].toLongOrNull() ?: 0L, // iowait
                    tokens[6].toLongOrNull() ?: 0L, // irq
                    tokens[7].toLongOrNull() ?: 0L, // softirq
                )
            }
        } catch (e: Exception) {
            longArrayOf(0, 0, 0, 0, 0, 0, 0)
        }
    }

    private fun getMemoryUsage(): Double {
        return try {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val used = memoryInfo.totalMem - memoryInfo.availMem
            (used.toDouble() / memoryInfo.totalMem.toDouble()) * 100.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun getStorageUsage(): Double {
        return try {
            val statFs = StatFs(context.filesDir.absolutePath)
            val totalBytes = statFs.totalBytes.toDouble()
            val availableBytes = statFs.availableBytes.toDouble()
            val usedBytes = totalBytes - availableBytes
            (usedBytes / totalBytes) * 100.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun getNetworkUsage(): Double {
        return try {
            val rxBytes = TrafficStats.getTotalRxBytes().toDouble()
            val txBytes = TrafficStats.getTotalTxBytes().toDouble()
            val totalBytes = rxBytes + txBytes
            // Return MB per hour (simplified)
            totalBytes / (1024 * 1024)
        } catch (e: Exception) {
            0.0
        }
    }

    private fun getThermalState(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Normal"
                PowerManager.THERMAL_STATUS_LIGHT -> "Light"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
                PowerManager.THERMAL_STATUS_SEVERE -> "Severe"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown"
                else -> "Unknown"
            }
        } else {
            "Normal" // Default for older versions
        }
    }

    private fun getCurrentBatteryTemperature(): Double {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            temperature / 10.0 // Convert to Celsius
        } catch (e: Exception) {
            25.0 // Default temperature
        }
    }

    private fun getSystemTemperature(): Double? {
        return if (ambientTemperature > 0) ambientTemperature.toDouble() else null
    }

    private fun calculatePerformanceScore(): Int {
        return try {
            val cpuScore = (100 - getCpuUsage()).toInt()
            val memoryScore = (100 - getMemoryUsage()).toInt()
            val thermalScore =
                when (getThermalState()) {
                    "Normal" -> 100
                    "Light" -> 80
                    "Moderate" -> 60
                    "Severe" -> 40
                    else -> 20
                }
            (cpuScore + memoryScore + thermalScore) / 3
        } catch (e: Exception) {
            75 // Default score
        }
    }

    private fun calculateStabilityScore(): Int {
        // Simplified stability calculation based on sensor data
        val accelerationMagnitude =
            sqrt(
                currentAcceleration[0].pow(2) +
                    currentAcceleration[1].pow(2) +
                    currentAcceleration[2].pow(2),
            )

        return when {
            accelerationMagnitude < 10.5 -> 95 // Device is stable
            accelerationMagnitude < 12.0 -> 85 // Minor movement
            accelerationMagnitude < 15.0 -> 70 // Moderate movement
            else -> 50 // Significant movement
        }
    }

    private fun calculateEfficiencyScore(): Int {
        return try {
            val batteryTemp = getCurrentBatteryTemperature()
            val cpuUsage = getCpuUsage()

            val tempScore =
                when {
                    batteryTemp < 30 -> 100
                    batteryTemp < 35 -> 90
                    batteryTemp < 40 -> 70
                    batteryTemp < 45 -> 50
                    else -> 30
                }

            val cpuScore = (100 - cpuUsage).toInt()

            (tempScore + cpuScore) / 2
        } catch (e: Exception) {
            75
        }
    }

    private fun getBackgroundProcessCount(): Int {
        return try {
            activityManager.runningAppProcesses?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun getForegroundAppCount(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activityManager.appTasks?.size ?: 0
            } else {
                1 // Default to 1 for older versions
            }
        } catch (e: Exception) {
            1
        }
    }

    private fun getActiveWakelocksCount(): Int {
        // This would require system-level access to get exact wakelocks
        // Return estimated count based on running processes
        return getBackgroundProcessCount() / 3
    }

    private fun getNetworkConnectionsCount(): Int {
        // Simplified network connections count
        return try {
            5 // Default estimate
        } catch (e: Exception) {
            0
        }
    }

    private fun getLocationRequestsCount(): Int {
        // This would require examining location requests from LocationManager
        return 0 // Placeholder
    }

    // App-specific helper methods
    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    /**
     * Estimates app battery usage based on foreground time
     * @param packageName App package name
     * @param timeUsed Foreground time in milliseconds
     * @return Estimated battery usage in percentage
     * NOTE: Uses simplified heuristic of 0.05% per hour. This is a rough estimate
     * and does not account for CPU intensity, network usage, or wake locks.
     * Should be replaced with BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER on supported devices.
     */
    private fun estimateAppBatteryUsage(
        packageName: String,
        timeUsed: Long,
    ): Double {
        // HEURISTIC: Simplified linear estimation based on foreground time
        // Real implementation should use BatteryStats API or BATTERY_PROPERTY_ENERGY_COUNTER
        return (timeUsed / (1000 * 60 * 60)) * 0.05 // 0.05% per hour of usage
    }

    private fun getAppDataUsage(packageName: String): Double {
        return try {
            val uid = context.packageManager.getPackageUid(packageName, 0)
            val rxBytes = TrafficStats.getUidRxBytes(uid).toDouble()
            val txBytes = TrafficStats.getUidTxBytes(uid).toDouble()
            (rxBytes + txBytes) / (1024 * 1024) // Convert to MB
        } catch (e: Exception) {
            0.0
        }
    }

    private fun getAppCategory(packageName: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val packageManager = context.packageManager
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                when (appInfo.category) {
                    ApplicationInfo.CATEGORY_GAME -> "Game"
                    ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                    ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                    ApplicationInfo.CATEGORY_MAPS -> "Maps"
                    ApplicationInfo.CATEGORY_NEWS -> "News"
                    else -> "Other"
                }
            } else {
                "Other"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }

    private fun getAppPermissions(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            packageInfo.requestedPermissions?.joinToString(",") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun getAppVersion(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getAppInstallDate(packageName: String): Long {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.firstInstallTime
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Clean up resources when collector is no longer needed
     */
    fun cleanup() {
        sensorManager.unregisterListener(this)
    }
}
