package com.voltai.features.analytics

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsDashboard @Inject constructor(
    private val context: Context,
    private val dataExporter: DataExporter,
    private val chartGenerator: ChartGenerator
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    private val _exportState = MutableStateFlow(ExportState())
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()
    
    fun loadDashboardData(timeRange: TimeRange = TimeRange.LAST_7_DAYS) {
        scope.launch {
            try {
                _dashboardState.value = _dashboardState.value.copy(isLoading = true)
                
                val analytics = calculateAnalytics(timeRange)
                val charts = generateCharts(analytics)
                val insights = generateInsights(analytics)
                
                _dashboardState.value = DashboardState(
                    isLoading = false,
                    timeRange = timeRange,
                    analytics = analytics,
                    charts = charts,
                    insights = insights,
                    lastUpdate = LocalDateTime.now()
                )
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = "Failed to load dashboard data: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun calculateAnalytics(timeRange: TimeRange): BatteryAnalytics {
        // In a real implementation, this would query the battery log database
        return BatteryAnalytics(
            averageBatteryLevel = 75.5f,
            averageTemperature = 32.1f,
            averageVoltage = 3.85f,
            totalChargingCycles = 45,
            screenOnTime = "6h 32m",
            batteryHealthScore = 94,
            estimatedBatteryLife = "2.5 years",
            powerEfficiencyScore = 87,
            chargingPatternScore = 92,
            usagePatternScore = 78,
            dailyUsageData = generateMockDailyData(),
            hourlyUsageData = generateMockHourlyData(),
            temperatureTrends = generateMockTemperatureData(),
            chargingHistory = generateMockChargingData()
        )
    }
    
    private suspend fun generateCharts(analytics: BatteryAnalytics): List<ChartData> {
        return listOf(
            ChartData(
                type = ChartType.LINE_CHART,
                title = "Battery Level Over Time",
                data = analytics.dailyUsageData.map { ChartPoint(it.timestamp.toFloat(), it.batteryLevel) },
                color = "#4CAF50",
                yAxisLabel = "Battery Level (%)"
            ),
            ChartData(
                type = ChartType.BAR_CHART,
                title = "Hourly Usage Pattern",
                data = analytics.hourlyUsageData.map { ChartPoint(it.hour.toFloat(), it.usagePercentage) },
                color = "#2196F3",
                yAxisLabel = "Usage (%)"
            ),
            ChartData(
                type = ChartType.LINE_CHART,
                title = "Temperature Trends",
                data = analytics.temperatureTrends.map { ChartPoint(it.timestamp.toFloat(), it.temperature) },
                color = "#FF5722",
                yAxisLabel = "Temperature (°C)"
            ),
            ChartData(
                type = ChartType.PIE_CHART,
                title = "Charging Sources",
                data = listOf(
                    ChartPoint(1f, 45f, "AC Charger"),
                    ChartPoint(2f, 35f, "USB"),
                    ChartPoint(3f, 20f, "Wireless")
                ),
                color = "#9C27B0",
                yAxisLabel = "Percentage"
            )
        )
    }
    
    private fun generateInsights(analytics: BatteryAnalytics): List<Insight> {
        val insights = mutableListOf<Insight>()
        
        // Battery health insights
        if (analytics.batteryHealthScore >= 90) {
            insights.add(Insight(
                type = InsightType.POSITIVE,
                title = "Excellent Battery Health",
                description = "Your battery health score is ${analytics.batteryHealthScore}%. Keep up the good charging habits!",
                actionText = "View Tips",
                priority = InsightPriority.LOW
            ))
        } else if (analytics.batteryHealthScore < 80) {
            insights.add(Insight(
                type = InsightType.WARNING,
                title = "Battery Health Declining",
                description = "Your battery health has dropped to ${analytics.batteryHealthScore}%. Consider optimizing your charging patterns.",
                actionText = "Optimize Charging",
                priority = InsightPriority.HIGH
            ))
        }
        
        // Temperature insights
        if (analytics.averageTemperature > 35f) {
            insights.add(Insight(
                type = InsightType.WARNING,
                title = "High Average Temperature",
                description = "Your device runs at an average of ${String.format("%.1f", analytics.averageTemperature)}°C. This may affect battery longevity.",
                actionText = "Cooling Tips",
                priority = InsightPriority.MEDIUM
            ))
        }
        
        // Usage pattern insights
        if (analytics.usagePatternScore < 70) {
            insights.add(Insight(
                type = InsightType.SUGGESTION,
                title = "Optimize Usage Patterns",
                description = "Your usage pattern score is ${analytics.usagePatternScore}%. Consider adjusting screen brightness and app usage.",
                actionText = "View Recommendations",
                priority = InsightPriority.MEDIUM
            ))
        }
        
        // Charging pattern insights
        if (analytics.chargingPatternScore >= 90) {
            insights.add(Insight(
                type = InsightType.POSITIVE,
                title = "Great Charging Habits",
                description = "Your charging pattern score is ${analytics.chargingPatternScore}%. You're maintaining optimal charging practices.",
                actionText = "Learn More",
                priority = InsightPriority.LOW
            ))
        }
        
        return insights
    }
    
    fun exportData(format: ExportFormat, timeRange: TimeRange) {
        scope.launch {
            try {
                _exportState.value = _exportState.value.copy(isExporting = true)
                
                val file = when (format) {
                    ExportFormat.CSV -> dataExporter.exportToCSV(timeRange)
                    ExportFormat.JSON -> dataExporter.exportToJSON(timeRange)
                    ExportFormat.PDF_REPORT -> dataExporter.exportToPDF(timeRange)
                }
                
                _exportState.value = ExportState(
                    isExporting = false,
                    lastExportFile = file,
                    lastExportTime = LocalDateTime.now()
                )
                
                // Share the file
                shareExportedFile(file)
                
            } catch (e: Exception) {
                _exportState.value = _exportState.value.copy(
                    isExporting = false,
                    isError = true,
                    errorMessage = "Export failed: ${e.message}"
                )
            }
        }
    }
    
    private fun shareExportedFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = when (file.extension) {
                "csv" -> "text/csv"
                "json" -> "application/json"
                "pdf" -> "application/pdf"
                else -> "application/octet-stream"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "VoltAI Battery Report")
            putExtra(Intent.EXTRA_TEXT, "Battery analytics report generated by VoltAI")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, "Share Battery Report")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
    
    private fun generateMockDailyData(): List<DailyUsageData> {
        return (0..6).map { dayOffset ->
            val date = LocalDateTime.now().minusDays(dayOffset.toLong())
            DailyUsageData(
                timestamp = date.toEpochSecond(java.time.ZoneOffset.UTC),
                batteryLevel = (70..95).random().toFloat(),
                screenOnTime = (4..8).random(),
                chargingCycles = if ((0..2).random() == 0) 1 else 0
            )
        }
    }
    
    private fun generateMockHourlyData(): List<HourlyUsageData> {
        return (0..23).map { hour ->
            val usage = when (hour) {
                in 0..6 -> (5..15).random() // Low usage during sleep
                in 7..9, in 17..20 -> (40..80).random() // High usage during commute/evening
                in 10..16 -> (20..40).random() // Moderate usage during work
                in 21..23 -> (25..50).random() // Evening usage
                else -> (10..30).random()
            }
            
            HourlyUsageData(
                hour = hour,
                usagePercentage = usage.toFloat()
            )
        }
    }
    
    private fun generateMockTemperatureData(): List<TemperatureData> {
        return (0..23).map { hourOffset ->
            val timestamp = LocalDateTime.now().minusHours(hourOffset.toLong())
            TemperatureData(
                timestamp = timestamp.toEpochSecond(java.time.ZoneOffset.UTC),
                temperature = (28f..38f).random()
            )
        }
    }
    
    private fun generateMockChargingData(): List<ChargingData> {
        return listOf(
            ChargingData(
                startTime = LocalDateTime.now().minusHours(2).toEpochSecond(java.time.ZoneOffset.UTC),
                endTime = LocalDateTime.now().minusHours(1).toEpochSecond(java.time.ZoneOffset.UTC),
                startLevel = 25,
                endLevel = 85,
                chargingType = "AC"
            ),
            ChargingData(
                startTime = LocalDateTime.now().minusHours(8).toEpochSecond(java.time.ZoneOffset.UTC),
                endTime = LocalDateTime.now().minusHours(6).toEpochSecond(java.time.ZoneOffset.UTC),
                startLevel = 15,
                endLevel = 90,
                chargingType = "USB"
            )
        )
    }
    
    fun refreshData() {
        loadDashboardData(_dashboardState.value.timeRange)
    }
    
    fun setTimeRange(timeRange: TimeRange) {
        if (timeRange != _dashboardState.value.timeRange) {
            loadDashboardData(timeRange)
        }
    }
}

// Data classes for analytics dashboard
data class DashboardState(
    val isLoading: Boolean = false,
    val timeRange: TimeRange = TimeRange.LAST_7_DAYS,
    val analytics: BatteryAnalytics = BatteryAnalytics(),
    val charts: List<ChartData> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val lastUpdate: LocalDateTime = LocalDateTime.now(),
    val isError: Boolean = false,
    val errorMessage: String? = null
)

data class ExportState(
    val isExporting: Boolean = false,
    val lastExportFile: File? = null,
    val lastExportTime: LocalDateTime? = null,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

data class BatteryAnalytics(
    val averageBatteryLevel: Float = 0f,
    val averageTemperature: Float = 0f,
    val averageVoltage: Float = 0f,
    val totalChargingCycles: Int = 0,
    val screenOnTime: String = "",
    val batteryHealthScore: Int = 100,
    val estimatedBatteryLife: String = "",
    val powerEfficiencyScore: Int = 0,
    val chargingPatternScore: Int = 0,
    val usagePatternScore: Int = 0,
    val dailyUsageData: List<DailyUsageData> = emptyList(),
    val hourlyUsageData: List<HourlyUsageData> = emptyList(),
    val temperatureTrends: List<TemperatureData> = emptyList(),
    val chargingHistory: List<ChargingData> = emptyList()
)

data class ChartData(
    val type: ChartType,
    val title: String,
    val data: List<ChartPoint>,
    val color: String,
    val yAxisLabel: String
)

data class ChartPoint(
    val x: Float,
    val y: Float,
    val label: String = ""
)

data class Insight(
    val type: InsightType,
    val title: String,
    val description: String,
    val actionText: String? = null,
    val priority: InsightPriority = InsightPriority.MEDIUM
)

data class DailyUsageData(
    val timestamp: Long,
    val batteryLevel: Float,
    val screenOnTime: Int,
    val chargingCycles: Int
)

data class HourlyUsageData(
    val hour: Int,
    val usagePercentage: Float
)

data class TemperatureData(
    val timestamp: Long,
    val temperature: Float
)

data class ChargingData(
    val startTime: Long,
    val endTime: Long,
    val startLevel: Int,
    val endLevel: Int,
    val chargingType: String
)

enum class TimeRange {
    LAST_24_HOURS,
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_3_MONTHS,
    LAST_YEAR
}

enum class ExportFormat {
    CSV,
    JSON,
    PDF_REPORT
}

enum class ChartType {
    LINE_CHART,
    BAR_CHART,
    PIE_CHART,
    AREA_CHART
}

enum class InsightType {
    POSITIVE,
    WARNING,
    SUGGESTION,
    INFORMATION
}

enum class InsightPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
