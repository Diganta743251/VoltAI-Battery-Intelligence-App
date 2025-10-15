package com.voltai.smartbatteryguardian.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.rememberBatteryData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Debug screen to verify that real battery data is being collected
 * This screen shows comprehensive battery information to ensure
 * we're getting actual device data, not static values
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryDataDebugScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    var refreshTrigger by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Battery Data Debug",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            refreshTrigger++
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                        )
                    }
                },
                colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Real-Time Battery Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Last Updated: ${formatTimestamp(batteryData.lastUpdated)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            // Group data by categories
            val dataCategories =
                listOf(
                    "Basic Battery Info" to
                        listOf(
                            "Battery Level" to "${batteryData.batteryLevel}%",
                            "Is Charging" to if (batteryData.isCharging) "Yes" else "No",
                            "Charging Status" to batteryData.chargingStatus,
                            "Remaining Time" to batteryData.remainingTime,
                            "Battery Used" to batteryData.batteryUsed,
                        ),
                    "Technical Data" to
                        listOf(
                            "Temperature" to "${batteryData.temperature}°C",
                            "Voltage" to "${batteryData.voltage}V",
                            "Current" to "${batteryData.current} mA",
                            "Health Percentage" to "${batteryData.healthPercentage}%",
                            "Capacity" to batteryData.capacity,
                            "Cycle Count" to "${batteryData.cycleCount}",
                        ),
                    "Power & Performance" to
                        listOf(
                            "Power Consumption" to "${String.format("%.3f", batteryData.powerConsumption)}W",
                            "Charging Speed" to batteryData.chargingSpeed,
                            "Estimated Life" to batteryData.estimatedLife,
                            "Power Source" to batteryData.powerSource,
                            "Battery Score" to "${batteryData.batteryScore}/100",
                        ),
                    "Screen Time" to
                        listOf(
                            "Screen On Time" to batteryData.screenOnTime,
                            "Screen Off Time" to batteryData.screenOffTime,
                        ),
                    "Device Info" to
                        listOf(
                            "Device Model" to batteryData.deviceModel,
                            "Android Version" to batteryData.androidVersion,
                        ),
                )

            items(dataCategories) { (categoryTitle, dataItems) ->
                DebugDataCategory(
                    title = categoryTitle,
                    items = dataItems,
                )
            }

            // AI Insights Section
            item {
                DebugAIInsights(
                    recommendations = batteryData.recommendations,
                    optimizationTips = batteryData.optimizationTips,
                )
            }

            // Data Quality Indicators
            item {
                DataQualityIndicators(batteryData = batteryData)
            }
        }
    }
}

@Composable
private fun DebugDataCategory(
    title: String,
    items: List<Pair<String, String>>,
) {
    Card(
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            items.forEach { (label, value) ->
                DebugDataRow(label = label, value = value)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DebugDataRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = value,
            style =
            MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
            ),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun DebugAIInsights(
    recommendations: List<String>,
    optimizationTips: List<String>,
) {
    Card(
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "AI Insights",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Recommendations:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )

            recommendations.forEach { recommendation ->
                Text(
                    text = "• $recommendation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Optimization Tips:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )

            optimizationTips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun DataQualityIndicators(batteryData: BatteryData) {
    Card(
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Data Quality Check",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            val qualityIndicators =
                listOf(
                    "Real Battery Level" to isRealBatteryData(batteryData.batteryLevel, batteryData.lastUpdated),
                    "Real Temperature" to isRealTemperatureData(batteryData.temperature),
                    "Real Voltage" to isRealVoltageData(batteryData.voltage),
                    "Real Current" to isRealCurrentData(batteryData.current),
                    "Dynamic Updates" to isDynamicData(batteryData.lastUpdated),
                    "Screen Time Access" to hasScreenTimeData(batteryData.screenOnTime),
                )

            qualityIndicators.forEach { (label, isReal) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )

                    Text(
                        text = if (isReal) "✓ REAL" else "⚠ STATIC",
                        style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        fontWeight = FontWeight.Bold,
                        color = if (isReal) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// Helper functions to detect if data is real or static
private fun isRealBatteryData(
    level: Int,
    lastUpdated: Long,
): Boolean {
    // Real battery data should be in reasonable range and recently updated
    return level in 1..100 && (System.currentTimeMillis() - lastUpdated) < 60000 // Updated within 1 minute
}

private fun isRealTemperatureData(temp: Float): Boolean {
    // Real temperature should be in reasonable range for mobile device
    return temp in 15f..60f && temp % 1 != 0f // Has decimal precision
}

private fun isRealVoltageData(voltage: Double): Boolean {
    // Real voltage should be in typical Li-ion range
    return voltage in 3.0..4.5 && voltage != 3.7 // Not exactly the default value
}

private fun isRealCurrentData(current: Int): Boolean {
    // Real current should have some variation and be non-zero when charging/discharging
    return current != 0
}

private fun isDynamicData(lastUpdated: Long): Boolean {
    // Data should be recently updated
    return (System.currentTimeMillis() - lastUpdated) < 30000 // Updated within 30 seconds
}

private fun hasScreenTimeData(screenTime: String): Boolean {
    // Should have actual screen time data, not default values
    return screenTime != "0h 0m" && screenTime != "Unknown" && screenTime != "Not available"
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
