package com.voltai.smartbatteryguardian.data.export

import android.content.Context
import com.voltai.smartbatteryguardian.data.database.BatteryRepository
import com.voltai.smartbatteryguardian.data.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive data export manager with multiple format support
 * Handles CSV, JSON, and cloud backup functionality
 */
@Singleton
class DataExportManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val repository: BatteryRepository,
) {
    private val _exportState = MutableStateFlow(ExportState.IDLE)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    enum class ExportState {
        IDLE,
        PREPARING,
        EXPORTING,
        COMPLETED,
        ERROR,
    }

    enum class ExportFormat {
        CSV,
        JSON,
        BACKUP,
    }

    data class ExportConfig(
        val format: ExportFormat,
        val includeReadings: Boolean = true,
        val includeChargingSessions: Boolean = true,
        val includeAppUsage: Boolean = true,
        val includeHealthRecords: Boolean = true,
        val includePredictions: Boolean = true,
        val includeSystemMetrics: Boolean = false,
        val dateRange: DateRange? = null,
        val outputPath: String? = null,
    )

    data class DateRange(
        val startDate: Date,
        val endDate: Date,
    )

    data class ExportResult(
        val success: Boolean,
        val filePath: String?,
        val fileSize: Long,
        val recordCount: Int,
        val errorMessage: String?,
    )

    /**
     * Export data based on configuration
     */
    suspend fun exportData(config: ExportConfig): ExportResult =
        withContext(Dispatchers.IO) {
            try {
                _exportState.value = ExportState.PREPARING
                _exportProgress.value = 0f

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val filename =
                    when (config.format) {
                        ExportFormat.CSV -> "voltai_battery_data_$timestamp.csv"
                        ExportFormat.JSON -> "voltai_battery_data_$timestamp.json"
                        ExportFormat.BACKUP -> "voltai_backup_$timestamp.json"
                    }

                val outputFile = File(context.getExternalFilesDir(null), filename)

                _exportState.value = ExportState.EXPORTING

                when (config.format) {
                    ExportFormat.CSV -> exportToCsv(config, outputFile)
                    ExportFormat.JSON -> exportToJson(config, outputFile)
                    ExportFormat.BACKUP -> exportToBackup(config, outputFile)
                }

                _exportState.value = ExportState.COMPLETED
                _exportProgress.value = 1f

                ExportResult(
                    success = true,
                    filePath = outputFile.absolutePath,
                    fileSize = outputFile.length(),
                    recordCount = countRecords(config),
                    errorMessage = null,
                )
            } catch (e: Exception) {
                _exportState.value = ExportState.ERROR
                ExportResult(
                    success = false,
                    filePath = null,
                    fileSize = 0L,
                    recordCount = 0,
                    errorMessage = e.message,
                )
            }
        }

    /**
     * Export data to CSV format
     */
    private suspend fun exportToCsv(
        config: ExportConfig,
        outputFile: File,
    ) {
        FileOutputStream(outputFile).use { fos ->
            val writer = fos.bufferedWriter()

            var recordsWritten = 0
            val totalRecords = countRecords(config)

            // Export battery readings
            if (config.includeReadings) {
                writer.write("=== BATTERY READINGS ===\n")
                writer.write(
                    "Timestamp,Battery Level,Is Charging,Voltage,Current,Temperature,Health,Technology,Charging Speed,Charging Type,Plug Type,Power Source\n",
                )

                val readings = getBatteryReadings(config.dateRange)
                readings.forEach { reading ->
                    writer.write(
                        "${formatTimestamp(
                            reading.timestamp,
                        )},${reading.batteryLevel},${reading.isCharging},${reading.voltage},${reading.current},${reading.temperature},${reading.health},${reading.technology},${reading.chargingSpeed},${reading.chargingType ?: ""},${reading.plugType ?: ""},${reading.powerSource ?: ""}\n",
                    )
                    recordsWritten++
                    _exportProgress.value = recordsWritten.toFloat() / totalRecords
                }
                writer.write("\n")
            }

            // Export charging sessions
            if (config.includeChargingSessions) {
                writer.write("=== CHARGING SESSIONS ===\n")
                writer.write(
                    "Start Time,End Time,Start Level,End Level,Duration,Charger Type,Max Power,Avg Power,Efficiency,Energy Consumed\n",
                )

                val sessions = getChargingSessions(config.dateRange)
                sessions.forEach { session ->
                    writer.write(
                        "${formatTimestamp(
                            session.startTime,
                        )},${session.endTime?.let { formatTimestamp(it) } ?: ""},${session.startLevel},${session.endLevel ?: ""},${session.duration},${session.chargerType},${session.maxPower},${session.avgPower},${session.efficiency},${session.energyConsumed}\n",
                    )
                    recordsWritten++
                    _exportProgress.value = recordsWritten.toFloat() / totalRecords
                }
                writer.write("\n")
            }

            // Export app usage stats
            if (config.includeAppUsage) {
                writer.write("=== APP USAGE STATS ===\n")
                writer.write(
                    "Timestamp,Package Name,App Name,Category,Battery Usage,Foreground Time,Background Time,CPU Usage,Network Usage\n",
                )

                val appStats = getAppUsageStats(config.dateRange)
                appStats.forEach { stat ->
                    writer.write(
                        "${formatTimestamp(
                            stat.timestamp,
                        )},${stat.packageName},${stat.appName},${stat.category},${stat.batteryUsage},${stat.foregroundTime},${stat.backgroundTime},${stat.cpuUsage},${stat.networkUsage}\n",
                    )
                    recordsWritten++
                    _exportProgress.value = recordsWritten.toFloat() / totalRecords
                }
                writer.write("\n")
            }

            // Export health records
            if (config.includeHealthRecords) {
                writer.write("=== BATTERY HEALTH RECORDS ===\n")
                writer.write(
                    "Timestamp,Health Percentage,Capacity Retained,Cycle Count,Temperature Avg,Voltage Avg,Degradation Rate,Prediction Score\n",
                )

                val healthRecords = getBatteryHealthRecords(config.dateRange)
                healthRecords.forEach { record ->
                    writer.write(
                        "${formatTimestamp(
                            record.timestamp,
                        )},${record.healthPercentage},${record.capacityRetained},${record.cycleCount},${record.temperatureAvg},${record.voltageAvg},${record.degradationRate},${record.predictionScore}\n",
                    )
                    recordsWritten++
                    _exportProgress.value = recordsWritten.toFloat() / totalRecords
                }
                writer.write("\n")
            }

            // Export AI predictions
            if (config.includePredictions) {
                writer.write("=== AI PREDICTIONS ===\n")
                writer.write(
                    "Timestamp,Prediction Type,Target Variable,Predicted Value,Confidence,Time Horizon,Model Version,Accuracy\n",
                )

                val predictions = getAIPredictions(config.dateRange)
                predictions.forEach { prediction ->
                    writer.write(
                        "${formatTimestamp(
                            prediction.timestamp,
                        )},${prediction.predictionType},${prediction.targetVariable},${prediction.predictedValue},${prediction.confidence},${prediction.timeHorizon},${prediction.modelVersion},${prediction.accuracy ?: ""}\n",
                    )
                    recordsWritten++
                    _exportProgress.value = recordsWritten.toFloat() / totalRecords
                }
                writer.write("\n")
            }

            // Export system metrics (optional)
            if (config.includeSystemMetrics) {
                writer.write("=== SYSTEM METRICS ===\n")
                writer.write(
                    "Timestamp,CPU Usage,Memory Usage,Storage Usage,Network Usage,Thermal State,Battery Temp,Performance Score,Stability Score,Efficiency Score\n",
                )

                val systemMetrics = getSystemMetrics(config.dateRange)
                systemMetrics.forEach { metric ->
                    writer.write(
                        "${formatTimestamp(
                            metric.timestamp,
                        )},${metric.cpuUsage},${metric.memoryUsage},${metric.storageUsage},${metric.networkUsage},${metric.thermalState},${metric.batteryTemperature},${metric.performanceScore},${metric.stabilityScore},${metric.efficiencyScore}\n",
                    )
                    recordsWritten++
                    _exportProgress.value = recordsWritten.toFloat() / totalRecords
                }
            }

            writer.flush()
        }
    }

    /**
     * Export data to JSON format
     */
    private suspend fun exportToJson(
        config: ExportConfig,
        outputFile: File,
    ) {
        val exportData = mutableMapOf<String, Any>()

        // Metadata
        exportData["metadata"] =
            mapOf(
                "exportDate" to Date(),
                "appVersion" to "1.0",
                "dataVersion" to "1.0",
                "deviceInfo" to getDeviceInfo(),
            )

        var recordsProcessed = 0
        val totalRecords = countRecords(config)

        // Export each data type
        if (config.includeReadings) {
            exportData["batteryReadings"] = getBatteryReadings(config.dateRange)
            recordsProcessed += (exportData["batteryReadings"] as List<*>).size
            _exportProgress.value = recordsProcessed.toFloat() / totalRecords
        }

        if (config.includeChargingSessions) {
            exportData["chargingSessions"] = getChargingSessions(config.dateRange)
            recordsProcessed += (exportData["chargingSessions"] as List<*>).size
            _exportProgress.value = recordsProcessed.toFloat() / totalRecords
        }

        if (config.includeAppUsage) {
            exportData["appUsageStats"] = getAppUsageStats(config.dateRange)
            recordsProcessed += (exportData["appUsageStats"] as List<*>).size
            _exportProgress.value = recordsProcessed.toFloat() / totalRecords
        }

        if (config.includeHealthRecords) {
            exportData["healthRecords"] = getBatteryHealthRecords(config.dateRange)
            recordsProcessed += (exportData["healthRecords"] as List<*>).size
            _exportProgress.value = recordsProcessed.toFloat() / totalRecords
        }

        if (config.includePredictions) {
            exportData["aiPredictions"] = getAIPredictions(config.dateRange)
            recordsProcessed += (exportData["aiPredictions"] as List<*>).size
            _exportProgress.value = recordsProcessed.toFloat() / totalRecords
        }

        if (config.includeSystemMetrics) {
            exportData["systemMetrics"] = getSystemMetrics(config.dateRange)
            recordsProcessed += (exportData["systemMetrics"] as List<*>).size
            _exportProgress.value = recordsProcessed.toFloat() / totalRecords
        }

        // Write JSON to file
        outputFile.writeText(json.encodeToString(exportData))
    }

    /**
     * Export complete backup with all data and settings
     */
    private suspend fun exportToBackup(
        config: ExportConfig,
        outputFile: File,
    ) {
        val backupData = mutableMapOf<String, Any>()

        // Complete backup includes everything
        backupData["metadata"] =
            mapOf(
                "backupDate" to Date(),
                "appVersion" to "1.0",
                "backupVersion" to "1.0",
                "deviceInfo" to getDeviceInfo(),
            )

        // Include all data types for backup
        backupData["batteryReadings"] = getBatteryReadings(null)
        backupData["chargingSessions"] = getChargingSessions(null)
        backupData["appUsageStats"] = getAppUsageStats(null)
        backupData["healthRecords"] = getBatteryHealthRecords(null)
        backupData["aiPredictions"] = getAIPredictions(null)
        backupData["systemMetrics"] = getSystemMetrics(null)

        // Include user preferences and settings
        backupData["userPreferences"] = getUserPreferences() ?: UserPreferences()
        backupData["powerProfiles"] = getPowerProfiles()

        // Write backup to file
        outputFile.writeText(json.encodeToString(backupData))
        _exportProgress.value = 1f
    }

    // Data fetching methods
    private suspend fun getBatteryReadings(dateRange: DateRange?): List<BatteryReading> {
        // Implementation would fetch from repository with date filtering
        return emptyList() // Placeholder
    }

    private suspend fun getChargingSessions(dateRange: DateRange?): List<ChargingSession> {
        // Implementation would fetch from repository
        return emptyList() // Placeholder
    }

    private suspend fun getAppUsageStats(dateRange: DateRange?): List<AppUsageStats> {
        // Implementation would fetch from repository
        return emptyList() // Placeholder
    }

    private suspend fun getBatteryHealthRecords(dateRange: DateRange?): List<BatteryHealthRecord> {
        // Implementation would fetch from repository
        return emptyList() // Placeholder
    }

    private suspend fun getAIPredictions(dateRange: DateRange?): List<AIPrediction> {
        // Implementation would fetch from repository
        return emptyList() // Placeholder
    }

    private suspend fun getSystemMetrics(dateRange: DateRange?): List<SystemMetrics> {
        // Implementation would fetch from repository
        return emptyList() // Placeholder
    }

    private suspend fun getUserPreferences(): UserPreferences? {
        // Implementation would fetch user preferences
        return null // Placeholder
    }

    private suspend fun getPowerProfiles(): List<PowerProfile> {
        // Implementation would fetch power profiles
        return emptyList() // Placeholder
    }

    // Helper methods
    private suspend fun countRecords(config: ExportConfig): Int {
        var count = 0

        if (config.includeReadings) count += getBatteryReadings(config.dateRange).size
        if (config.includeChargingSessions) count += getChargingSessions(config.dateRange).size
        if (config.includeAppUsage) count += getAppUsageStats(config.dateRange).size
        if (config.includeHealthRecords) count += getBatteryHealthRecords(config.dateRange).size
        if (config.includePredictions) count += getAIPredictions(config.dateRange).size
        if (config.includeSystemMetrics) count += getSystemMetrics(config.dateRange).size

        return count
    }

    private fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    }

    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "androidVersion" to android.os.Build.VERSION.RELEASE,
            "sdkVersion" to android.os.Build.VERSION.SDK_INT.toString(),
        )
    }
}
