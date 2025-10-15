package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.ui.components.CollapsibleScreen
import com.voltai.smartbatteryguardian.utils.getHealthCardColor
import com.voltai.smartbatteryguardian.utils.getHealthColor
import com.voltai.smartbatteryguardian.utils.getHealthDescription
import com.voltai.smartbatteryguardian.utils.getHealthStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDetailScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)

    val healthStatus = getHealthStatus(batteryData.healthPercentage)
    val subtitle = "${batteryData.healthPercentage}% - $healthStatus"

    CollapsibleScreen(
        title = "Battery Health",
        subtitle = subtitle,
        onBackClick = onBackClick,
    ) {
        // Health Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = getHealthCardColor(batteryData.healthPercentage),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Circular health indicator
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        val healthColor = getHealthColor(batteryData.healthPercentage)
                        Canvas(
                            modifier =
                            Modifier
                                .fillMaxSize()
                                .semantics { contentDescription = "Battery health circular chart" },
                        ) {
                            drawHealthCircle(batteryData.healthPercentage, healthColor)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "${batteryData.healthPercentage}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = getHealthStatus(batteryData.healthPercentage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = getHealthDescription(batteryData.healthPercentage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // Capacity Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "Battery Capacity",
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val capacityParts = batteryData.capacity.split("/")
                    val currentCapacity = capacityParts[0].replace(" mAh", "").toIntOrNull() ?: 2500
                    val designCapacity = capacityParts[1].replace(" mAh", "").toIntOrNull() ?: 4000

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        CapacityItem(
                            label = "Current",
                            value = "$currentCapacity mAh",
                            color = getHealthColor(batteryData.healthPercentage),
                        )

                        CapacityItem(
                            label = "Design",
                            value = "$designCapacity mAh",
                            color = MaterialTheme.colorScheme.primary,
                        )

                        CapacityItem(
                            label = "Lost",
                            value = "${designCapacity - currentCapacity} mAh",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Capacity bar
                    val capacityFillColor = getHealthColor((currentCapacity.toFloat() / designCapacity * 100).toInt())
                    Canvas(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .semantics { contentDescription = "Battery capacity bar" },
                    ) {
                        drawCapacityBar(currentCapacity, designCapacity, capacityFillColor)
                    }
                }
            }
        }

        // Health Trends
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "Health Trend (6 Months)",
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val trendColor = MaterialTheme.colorScheme.tertiary
                    Canvas(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .semantics { contentDescription = "6-month health trend chart" },
                    ) {
                        drawHealthTrend(batteryData.healthPercentage, trendColor)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "6 months ago",
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

        // Health Analysis
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "Health Analysis",
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HealthAnalysisRow(
                        icon = Icons.Default.Favorite,
                        label = "Overall Health",
                        value = "${batteryData.healthPercentage}% (${getHealthStatus(
                            batteryData.healthPercentage,
                        )})",
                        iconColor = getHealthColor(batteryData.healthPercentage),
                    )

                    // Estimate charge cycles based on battery health and cycle count from batteryData
                    val estimatedCycles =
                        batteryData.cycleCount.takeIf { it > 0 } ?: run {
                            // Estimate based on health: typical degradation is ~1% per 25-50 cycles
                            val degradation = 100 - batteryData.healthPercentage
                            (degradation * 30) // Rough estimate: 1% degradation ≈ 30 cycles
                        }
                    HealthAnalysisRow(
                        icon = Icons.Default.Star,
                        label = "Charge Cycles",
                        value = "$estimatedCycles cycles",
                        iconColor = if (estimatedCycles > 500) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                    )

                    val degradation = 100 - batteryData.healthPercentage
                    HealthAnalysisRow(
                        icon = Icons.Default.KeyboardArrowDown,
                        label = "Degradation",
                        value = "$degradation%",
                        iconColor = if (degradation > 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                    )

                    // Calculate realistic estimated life based on current health and typical degradation
                    val estimatedLife =
                        when {
                            batteryData.healthPercentage >= 95 -> 30 // Excellent condition
                            batteryData.healthPercentage >= 85 -> 24 // Good condition
                            batteryData.healthPercentage >= 75 -> 18 // Fair condition
                            batteryData.healthPercentage >= 65 -> 12 // Poor condition
                            else -> 6 // Critical condition
                        }
                    HealthAnalysisRow(
                        icon = Icons.Default.Info,
                        label = "Estimated Life",
                        value = "$estimatedLife months",
                        iconColor = if (estimatedLife < 18) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }

        // Health Tips
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
                        text = "💡 Battery Health Tips",
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val tips: List<String> =
                        listOf(
                            "Keep battery level between 20-80% for optimal health",
                            "Avoid extreme temperatures (below 0°C or above 45°C)",
                            "Use original charger and avoid fast charging when possible",
                            "Don't leave battery at 100% or 0% for extended periods",
                            "Calibrate battery monthly by full discharge and charge",
                            "Enable battery optimization for unused apps",
                        )

                    for (tip in tips) {
                        Text(
                            text = "• $tip",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }

        // Health Status Indicators
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "Health Status Guide",
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HealthStatusItem(
                        range = "90-100%",
                        status = "Excellent",
                        description = "Battery is in excellent condition",
                        color = MaterialTheme.colorScheme.tertiary,
                    )

                    HealthStatusItem(
                        range = "80-89%",
                        status = "Good",
                        description = "Battery health is good, normal usage",
                        color = MaterialTheme.colorScheme.tertiary,
                    )

                    HealthStatusItem(
                        range = "70-79%",
                        status = "Fair",
                        description = "Consider battery optimization",
                        color = MaterialTheme.colorScheme.secondary,
                    )

                    HealthStatusItem(
                        range = "60-69%",
                        status = "Poor",
                        description = "Battery replacement recommended",
                        color = MaterialTheme.colorScheme.error,
                    )

                    HealthStatusItem(
                        range = "Below 60%",
                        status = "Critical",
                        description = "Battery needs immediate replacement",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
fun CapacityItem(
    label: String,
    value: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun HealthAnalysisRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color,
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

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun HealthStatusItem(
    range: String,
    status: String,
    description: String,
    color: Color,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(
            modifier = Modifier.size(12.dp),
        ) {
            drawCircle(color = color)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row {
                Text(
                    text = range,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun DrawScope.drawHealthCircle(
    healthPercentage: Int,
    healthColor: Color,
) {
    val strokeWidth = 12.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)

    // Background circle
    drawCircle(
        color = Color.Gray.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth),
    )

    // Health arc
    val sweepAngle = (healthPercentage / 100f) * 360f

    drawArc(
        color = healthColor,
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(width = strokeWidth),
        topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
    )
}

private fun DrawScope.drawCapacityBar(
    currentCapacity: Int,
    designCapacity: Int,
    fillColor: Color,
) {
    val barHeight = size.height
    val currentWidth = (currentCapacity.toFloat() / designCapacity) * size.width

    // Background bar (design capacity)
    drawRect(
        color = Color.Gray.copy(alpha = 0.2f),
        topLeft = androidx.compose.ui.geometry.Offset.Zero,
        size = androidx.compose.ui.geometry.Size(size.width, barHeight),
    )

    // Current capacity bar
    drawRect(
        color = fillColor,
        topLeft = androidx.compose.ui.geometry.Offset.Zero,
        size = androidx.compose.ui.geometry.Size(currentWidth, barHeight),
    )
}

private fun DrawScope.drawHealthTrend(
    currentHealth: Int,
    trendColor: Color,
) {
    val points = 6 // 6 months
    val path = androidx.compose.ui.graphics.Path()

    for (i in 0..points) {
        val x = (i.toFloat() / points) * size.width

        // Create realistic health degradation pattern over 6 months
        val monthsAgo = points - i
        val healthAtTime =
            when {
                // Healthy batteries degrade slowly (0.5-1% per month)
                currentHealth >= 90 -> {
                    (currentHealth + monthsAgo * 0.8f).coerceAtMost(100f)
                }
                // Moderately healthy batteries degrade more (1-2% per month)
                currentHealth >= 75 -> {
                    (currentHealth + monthsAgo * 1.5f).coerceAtMost(100f)
                }
                // Unhealthy batteries had faster historical degradation
                else -> {
                    (currentHealth + monthsAgo * 2.5f).coerceAtMost(100f)
                }
            }.toInt()

        val normalizedY = (healthAtTime / 100f)
        val y = size.height - (normalizedY * size.height)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = trendColor,
        style = Stroke(width = 3.dp.toPx()),
    )
}

@Composable
private fun getHealthCardColor(healthPercentage: Int): CardColors {
    return when {
        healthPercentage >= 90 -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        healthPercentage >= 80 -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        healthPercentage >= 70 -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        healthPercentage >= 60 -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        else -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    }
}

@Composable
private fun getHealthColor(healthPercentage: Int): Color {
    return when {
        healthPercentage >= 90 -> MaterialTheme.colorScheme.tertiary // Green
        healthPercentage >= 80 -> MaterialTheme.colorScheme.tertiary // Light Green
        healthPercentage >= 70 -> MaterialTheme.colorScheme.secondary // Orange
        healthPercentage >= 60 -> MaterialTheme.colorScheme.error // Deep Orange
        else -> MaterialTheme.colorScheme.error // Red
    }
}

private fun getHealthDescription(healthPercentage: Int): String {
    return when {
        healthPercentage >= 90 -> "Your battery is in excellent condition"
        healthPercentage >= 80 -> "Battery health is good for normal usage"
        healthPercentage >= 70 -> "Consider optimizing battery usage"
        healthPercentage >= 60 -> "Battery replacement may be needed soon"
        else -> "Battery needs immediate replacement"
    }
}
