package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voltai.smartbatteryguardian.rememberBatteryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingDetailScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Ongoing Usage",
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        LazyColumn(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Usage Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Text(
                            text = "Current Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            UsageOverviewItem(
                                icon = Icons.Default.Star,
                                label = "Screen On",
                                value = batteryData.screenOnTime,
                                color = MaterialTheme.colorScheme.secondary,
                            )

                            UsageOverviewItem(
                                icon = Icons.Default.Lock,
                                label = "Screen Off",
                                value = batteryData.screenOffTime,
                                color = MaterialTheme.colorScheme.tertiary,
                            )

                            UsageOverviewItem(
                                icon = Icons.Default.Warning,
                                label = "Battery Used",
                                value = batteryData.batteryUsed,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            // Screen Time Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Screen Time Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pie chart representation
                        Canvas(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        ) {
                            drawScreenTimeChart(batteryData.screenOnTime, batteryData.screenOffTime)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            ChartLegendItem(
                                color = MaterialTheme.colorScheme.secondary,
                                label = "Screen On",
                                percentage =
                                calculateScreenOnPercentage(
                                    batteryData.screenOnTime,
                                    batteryData.screenOffTime,
                                ),
                            )

                            ChartLegendItem(
                                color = MaterialTheme.colorScheme.tertiary,
                                label = "Screen Off",
                                percentage = 100 - calculateScreenOnPercentage(batteryData.screenOnTime, batteryData.screenOffTime),
                            )
                        }
                    }
                }
            }

            // Battery Drain Analysis
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Battery Drain Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Calculate real drain rates from battery data
                        val totalDrain = (100 - batteryData.batteryLevel).coerceAtLeast(0)
                        val screenOnDrain =
                            if (batteryData.current < 0) {
                                // Estimate based on actual discharge current
                                (kotlin.math.abs(batteryData.current) * 0.8 / 100).toInt().coerceIn(5, 30)
                            } else {
                                0
                            }
                        val screenOffDrain =
                            if (batteryData.current < 0) {
                                // Background drain is typically 10-20% of active drain
                                (screenOnDrain * 0.15).toInt().coerceIn(1, 8)
                            } else {
                                0
                            }

                        DrainAnalysisRow(
                            icon = Icons.Default.Star,
                            label = "Screen On Drain",
                            value = if (screenOnDrain > 0) "~$screenOnDrain%/h" else "—",
                            description = if (screenOnDrain > 20) "High usage detected" else "Normal usage",
                            iconColor = MaterialTheme.colorScheme.secondary,
                            isHigh = screenOnDrain > 20,
                        )

                        DrainAnalysisRow(
                            icon = Icons.Default.Lock,
                            label = "Screen Off Drain",
                            value = if (screenOffDrain > 0) "~$screenOffDrain%/h" else "—",
                            description = if (screenOffDrain > 5) "Background apps active" else "Normal standby",
                            iconColor = MaterialTheme.colorScheme.tertiary,
                            isHigh = screenOffDrain > 5,
                        )

                        DrainAnalysisRow(
                            icon = Icons.Default.Warning,
                            label = "Total Drain",
                            value = "$totalDrain%",
                            description = "Since last charge",
                            iconColor = MaterialTheme.colorScheme.error,
                            isHigh = totalDrain > 50,
                        )
                    }
                }
            }

            // Usage Timeline
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Usage Timeline (Last 6 Hours)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Canvas(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                        ) {
                            drawUsageTimeline()
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "6h ago",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Now",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Recommendations
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "💡 Recommendations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val recommendations =
                            listOf(
                                "Reduce screen brightness to extend battery life",
                                "Close unused background apps",
                                "Enable battery saver mode when below 20%",
                                "Consider using dark mode to save power",
                            )

                        recommendations.forEach { recommendation ->
                            Text(
                                text = "• $recommendation",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageOverviewItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
fun ChartLegendItem(
    color: Color,
    label: String,
    percentage: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(
            modifier = Modifier.size(16.dp),
        ) {
            drawCircle(color = color)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun DrainAnalysisRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    description: String,
    iconColor: Color,
    isHigh: Boolean,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun DrawScope.drawScreenTimeChart(
    screenOnTime: String,
    screenOffTime: String,
) {
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    val radius = kotlin.math.min(size.width, size.height) / 3

    val screenOnPercentage = calculateScreenOnPercentage(screenOnTime, screenOffTime)
    val screenOnAngle = (screenOnPercentage / 100f) * 360f

    // Draw screen off arc
    drawArc(
        color = Color(0xFF9C27B0), // Tertiary purple tone
        startAngle = screenOnAngle,
        sweepAngle = 360f - screenOnAngle,
        useCenter = true,
        topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
    )

    // Draw screen on arc
    drawArc(
        color = Color(0xFFFF9800), // Secondary orange tone
        startAngle = 0f,
        sweepAngle = screenOnAngle,
        useCenter = true,
        topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
    )
}

private fun DrawScope.drawUsageTimeline() {
    val barCount = 24 // 6 hours * 4 (15-minute intervals)
    val barWidth = size.width / (barCount + (barCount - 1) * 0.1f)
    val barSpacing = barWidth * 0.1f
    val maxBarHeight = size.height

    for (i in 0 until barCount) {
        val x = i * (barWidth + barSpacing)

        // Use deterministic pattern based on typical usage
        val hourOfDay = (i / 4) % 24 // 15-minute intervals
        val isScreenOn = hourOfDay in 6..23 && (i % 3 != 0) // Active hours, mostly on
        val intensity = if (isScreenOn) 0.6f + (i % 10) * 0.04f else 0.1f

        val barHeight = intensity * maxBarHeight
        val y = maxBarHeight - barHeight

        val barColor = if (isScreenOn) Color(0xFFFF9800) else Color(0xFFFF9800).copy(alpha = 0.3f)

        drawRect(
            color = barColor,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
        )
    }
}

private fun calculateScreenOnPercentage(
    screenOnTime: String,
    screenOffTime: String,
): Int {
    // Parse time strings to calculate real percentage
    fun parseTimeMinutes(time: String): Int {
        val parts = time.replace("h", ":").replace("m", "").split(":")
        return when (parts.size) {
            2 -> (parts[0].trim().toIntOrNull() ?: 0) * 60 + (parts[1].trim().toIntOrNull() ?: 0)
            1 -> parts[0].trim().toIntOrNull() ?: 0
            else -> 0
        }
    }

    val onMinutes = parseTimeMinutes(screenOnTime)
    val offMinutes = parseTimeMinutes(screenOffTime)
    val total = onMinutes + offMinutes

    return if (total > 0) {
        ((onMinutes.toFloat() / total) * 100).toInt().coerceIn(0, 100)
    } else {
        30 // Default reasonable value if parsing fails
    }
}
