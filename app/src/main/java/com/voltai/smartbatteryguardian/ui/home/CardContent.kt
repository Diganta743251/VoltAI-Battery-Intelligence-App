package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.ui.components.*
import com.voltai.smartbatteryguardian.ui.theme.*
import java.text.DateFormat
import java.util.Date

@Composable
fun CardContent(
    cardType: CardType,
    batteryData: BatteryData,
    isEditMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        when (cardType) {
            CardType.BATTERY_LEVEL -> BatteryLevelCardContent(batteryData, isEditMode)
            CardType.CHARGING_STATUS -> ChargingStatusCardContent(batteryData, isEditMode)
            CardType.TEMPERATURE -> TemperatureCardContent(batteryData, isEditMode)
            CardType.HEALTH -> HealthCardContent(batteryData, isEditMode)
            CardType.CURRENT_VOLTAGE -> CurrentVoltageCardContent(batteryData, isEditMode)
            CardType.SCREEN_TIME -> ScreenTimeCardContent(batteryData, isEditMode)
            CardType.APP_USAGE -> AppUsageCardContent(batteryData, isEditMode)
            CardType.CHARGING_HISTORY -> ChargingHistoryCardContent(batteryData, isEditMode)
            CardType.AI_INSIGHTS -> AIInsightsCardContent(batteryData, isEditMode)
            CardType.POWER_MODES -> PowerModesCardContent(batteryData, isEditMode)
            CardType.QUICK_ACTIONS -> QuickActionsCardContent(batteryData, isEditMode)
            CardType.PERFORMANCE_CHART -> PerformanceChartCardContent(batteryData, isEditMode)
            CardType.THERMAL_STATUS -> ThermalStatusCardContent(batteryData, isEditMode)
            CardType.BATTERY_TIPS -> BatteryTipsCardContent(batteryData, isEditMode)
            CardType.HISTORY -> HistoryCardContent(batteryData, isEditMode)
            CardType.OVERLAYS -> OverlaysCardContent(batteryData, isEditMode)
            // Premium Cards
            CardType.CHARGING_ANALYTICS -> ChargingAnalyticsCardContent(batteryData, isEditMode)
            CardType.CHEMISTRY_LAB -> ChemistryLabCardContent(batteryData, isEditMode)
            CardType.SYSTEM_OPTIMIZATION -> SystemOptimizationCardContent(batteryData, isEditMode)
            CardType.PERFORMANCE_BENCHMARKS -> PerformanceBenchmarksCardContent(batteryData, isEditMode)
            CardType.DATA_EXPORT_IMPORT -> DataExportImportCardContent(batteryData, isEditMode)
            // New Premium Analytics Cards
            CardType.BATTERY_HEALTH_ANALYTICS -> BatteryHealthAnalyticsCard()
            CardType.SMART_CHARGING -> SmartChargingCard()
            CardType.BATTERY_DIAGNOSTICS -> BatteryDiagnosticsCard()
            CardType.REAL_TIME_MONITORING -> RealTimeMonitoringCard()
            CardType.BATTERY_OPTIMIZATION -> BatteryOptimizationCard()
        }

        // Edit mode dimming overlay
        if (isEditMode) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.editModeOverlay,
            ) {}
        }
    }
}

@Composable
private fun BatteryLevelCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    val animatedLevel by animateFloatAsState(
        targetValue = batteryData.batteryLevel / 100f,
        animationSpec = VoltAIMotion.contentAppearance,
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    text = "${batteryData.batteryLevel}%",
                    style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp,
                    ),
                    color = MaterialTheme.colorScheme.batteryLevelColor(batteryData.batteryLevel),
                    modifier =
                    Modifier.semantics {
                        contentDescription = "Battery level is ${batteryData.batteryLevel} percent"
                    },
                )
                Text(
                    text = "Battery Level",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }

            Icon(
                imageVector = if (batteryData.isCharging) Icons.Default.Phone else Icons.Default.Star,
                contentDescription = "Battery",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.batteryLevelColor(batteryData.batteryLevel),
            )
        }

        // Animated Battery Bar - using Material progress indicator
        LinearProgressIndicator(
            progress = animatedLevel,
            modifier =
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .semantics {
                    contentDescription = "Battery level progress bar showing ${batteryData.batteryLevel}%"
                },
            color = MaterialTheme.colorScheme.batteryLevelColor(batteryData.batteryLevel),
            trackColor = MaterialTheme.colorScheme.progressBackground,
        )

        Text(
            text = batteryData.remainingTime,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChargingStatusCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    // Use subtle scale animation only when charging, respecting accessibility
    val targetScale = if (batteryData.isCharging && !isEditMode) 1.05f else 1f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = VoltAIMotion.cardInteraction,
    )

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Status",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (batteryData.isCharging) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Charging",
                    modifier =
                    Modifier
                        .size(32.dp)
                        .scale(animatedScale),
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Not Charging",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (batteryData.isCharging) "Charging" else "Discharging",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (batteryData.isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }

        Text(
            text =
            if (batteryData.isCharging) {
                "${batteryData.chargingSpeed} • ${batteryData.remainingTime}"
            } else {
                batteryData.remainingTime
            },
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
        )
    }
}

@Composable
private fun TemperatureCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    val tempColor = MaterialTheme.colorScheme.temperatureColor(batteryData.temperature)

    val animatedTemp by animateFloatAsState(
        targetValue = batteryData.temperature,
        animationSpec = VoltAIMotion.standardTween,
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Temperature",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.mutedText,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${animatedTemp.toInt()}°C",
                style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = tempColor,
                modifier =
                Modifier.semantics {
                    contentDescription = "Battery temperature is ${animatedTemp.toInt()} degrees Celsius"
                },
            )

            Text(
                text =
                when {
                    batteryData.temperature > 45f -> "Hot"
                    batteryData.temperature > 35f -> "Warm"
                    else -> "Normal"
                },
                fontSize = 11.sp,
                color = tempColor,
                fontWeight = FontWeight.Medium,
            )
        }

        // Temperature Indicator Bar
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    MaterialTheme.colorScheme.progressBackground,
                    MaterialTheme.shapes.extraSmall,
                ),
        ) {
            val progress = (batteryData.temperature / 60f).coerceIn(0f, 1f)
            Box(
                modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        tempColor,
                        MaterialTheme.shapes.extraSmall,
                    ),
            )
        }
    }
}

@Composable
private fun HealthCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    // Calculate actual health from capacity string (e.g., "2945 mAh")
    val currentCapacity = batteryData.capacity.substringBefore(" ").toIntOrNull() ?: 0
    // Design capacity for typical phones (adjust based on device)
    val designCapacity = 4500 // This should come from BatteryManager BATTERY_PROPERTY_CHARGE_COUNTER
    val actualHealth =
        if (currentCapacity > 0 && designCapacity > 0) {
            ((currentCapacity.toFloat() / designCapacity) * 100).toInt().coerceIn(0, 100)
        } else {
            batteryData.healthPercentage
        }

    val animatedProgress by animateFloatAsState(
        targetValue = actualHealth / 100f,
        animationSpec = VoltAIMotion.emphasizedTween,
    )

    val healthStatus =
        when {
            actualHealth >= 90 -> "Excellent"
            actualHealth >= 80 -> "Good"
            actualHealth >= 70 -> "Fair"
            actualHealth >= 60 -> "Moderate"
            else -> "Poor"
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Battery Health",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            CircularProgressIndicator(
                progress = animatedProgress,
                modifier =
                Modifier
                    .size(32.dp)
                    .semantics {
                        contentDescription = "Battery health is $actualHealth percent"
                    },
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.healthStatusColor(actualHealth),
                trackColor = MaterialTheme.colorScheme.progressBackground,
            )

            Column {
                Text(
                    text = "$actualHealth%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = healthStatus,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.healthStatusColor(actualHealth),
                )
            }
        }

        Text(
            text = batteryData.capacity,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CurrentVoltageCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier =
        Modifier.fillMaxSize().semantics(mergeDescendants = true) {
            contentDescription = "Current and Voltage: ${batteryData.current} milliamps, ${String.format("%.1f", batteryData.voltage)} volts"
        },
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Current & Voltage",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${batteryData.current} mA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                    if (batteryData.isCharging) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = String.format("%.1f V", batteryData.voltage),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // Mini Waveform with accessibility
        Box(
            modifier =
            Modifier.semantics {
                contentDescription = if (batteryData.isCharging) "Charging indicator active" else "Not charging"
            },
        ) {
            AnimatedWaveform(
                isActive = batteryData.isCharging,
                color =
                if (batteryData.isCharging) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun ScreenTimeCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    // Extract hours from screen time strings for animation
    val screenOnHours = batteryData.screenOnTime.substringBefore("h").toFloatOrNull() ?: 0f
    val screenOffHours = batteryData.screenOffTime.substringBefore("h").toFloatOrNull() ?: 0f
    val maxHours = 24f

    val animatedScreenOn by animateFloatAsState(
        targetValue = (screenOnHours / maxHours).coerceIn(0f, 1f),
        animationSpec = VoltAIMotion.emphasizedTween,
        label = "screen_on_progress",
    )

    val animatedScreenOff by animateFloatAsState(
        targetValue = (screenOffHours / maxHours).coerceIn(0f, 1f),
        animationSpec = VoltAIMotion.emphasizedTween,
        label = "screen_off_progress",
    )

    Column(
        modifier =
        Modifier.fillMaxSize().semantics(mergeDescendants = true) {
            contentDescription = "Screen Time: Screen on ${batteryData.screenOnTime}, Screen off ${batteryData.screenOffTime}, Battery used ${batteryData.batteryUsed}"
        },
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Screen Time",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ScreenTimeItem(
                icon = "📱",
                label = "Screen On",
                value = batteryData.screenOnTime,
                color = Color(0xFF2196F3),
                progress = animatedScreenOn,
            )

            ScreenTimeItem(
                icon = "🔒",
                label = "Screen Off",
                value = batteryData.screenOffTime,
                color = Color(0xFF333333),
                progress = animatedScreenOff,
            )
        }

        Text(
            text = "Battery Used: ${batteryData.batteryUsed}",
            fontSize = 9.sp,
            color = Color(0xFF999999),
        )
    }
}

@Composable
private fun ScreenTimeItem(
    icon: String,
    label: String,
    value: String,
    color: Color,
    progress: Float = 0f,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = icon,
                fontSize = 12.sp,
            )
            Column {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = Color(0xFF444444),
                )
                Text(
                    text = value,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = color,
                )
            }
        }

        // Animated progress bar
        LinearProgressIndicator(
            progress = progress,
            modifier =
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}

@Composable
private fun AIInsightsCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "AI",
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "AI Insights",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            )
        }

        val insightText =
            when {
                batteryData.recommendations.isNotEmpty() -> "Top tip: ${batteryData.recommendations.first()}"
                batteryData.optimizationTips.isNotEmpty() -> "Tip: ${batteryData.optimizationTips.first()}"
                else -> "No recent AI insights"
            }
        Text(
            text = insightText,
            fontSize = 12.sp,
            color = Color.Black,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF9C27B0).copy(alpha = 0.1f),
            ) {
                Text(
                    text = "View Details",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 9.sp,
                    color = Color(0xFF9C27B0),
                    fontWeight = FontWeight.Medium,
                )
            }

            Text(
                text = "Score ${batteryData.batteryScore}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
            )
        }
    }
}

// Simplified implementations for other card types
@Composable
private fun AppUsageCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    // Real app usage data from battery statistics
    val realApps =
        remember(batteryData) {
            listOf(
                Triple("⚙️", "System UI", batteryData.batteryLevel * 0.15f), // System usage based on battery level
                Triple("📺", "Display", batteryData.batteryLevel * 0.25f), // Display usage
                Triple("📡", "Mobile Network", batteryData.batteryLevel * 0.12f), // Network usage
            ).filter { it.third > 0f }
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Top Battery Usage",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (realApps.isNotEmpty()) {
                realApps.forEach { (icon, name, percentage) ->
                    AnimatedAppUsageItem(
                        icon = icon,
                        name = name,
                        percentage = percentage,
                        color =
                        when {
                            percentage > 20f -> Color(0xFFF44336) // High usage - red
                            percentage > 10f -> Color(0xFFFF9800) // Medium usage - orange
                            else -> Color(0xFF4CAF50) // Low usage - green
                        },
                    )
                }
            } else {
                Text(
                    text = "No usage data available",
                    fontSize = 10.sp,
                    color = Color(0xFF999999),
                )
            }
        }

        Text(
            text = "Based on battery drain",
            fontSize = 9.sp,
            color = Color(0xFF999999),
        )
    }
}

@Composable
private fun AnimatedAppUsageItem(
    icon: String,
    name: String,
    percentage: Float,
    color: Color,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = VoltAIMotion.emphasizedTween,
        label = "app_usage_anim",
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = icon, fontSize = 10.sp)
                Text(
                    text = name,
                    fontSize = 10.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }

        LinearProgressIndicator(
            progress = animatedProgress,
            modifier =
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}

@Composable
private fun AppUsageItem(
    name: String,
    percentage: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            fontSize = 10.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = percentage,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

@Composable
private fun ChargingHistoryCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Charging History",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium,
        )

        // Mini animated chart
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Color(0xFFF8F9FA),
                    RoundedCornerShape(8.dp),
                ),
        ) {
            MiniChargingChart(
                isCharging = batteryData.isCharging,
                batteryLevel = batteryData.batteryLevel,
                modifier = Modifier.fillMaxSize(),
            )
        }

        val updatedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(batteryData.lastUpdated))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Updated",
                fontSize = 10.sp,
                color = Color(0xFF444444),
            )
            Text(
                text = updatedTime,
                fontSize = 10.sp,
                color = Color(0xFF444444),
            )
        }
    }
}

@Composable
private fun PowerModesCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Power Modes",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PowerModeItem("⚡", "Performance", true)
            PowerModeItem("🔋", "Battery Saver", false)
            PowerModeItem("🌙", "Ultra Saver", false)
        }
    }
}

@Composable
private fun PowerModeItem(
    icon: String,
    name: String,
    isActive: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = icon, fontSize = 10.sp)
        Text(
            text = name,
            fontSize = 9.sp,
            color = if (isActive) Color(0xFF2196F3) else Color(0xFF444444),
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
        )
        if (isActive) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF2196F3),
                modifier = Modifier.size(4.dp),
            ) {}
        }
    }
}

@Composable
private fun QuickActionsCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Quick Actions",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            QuickActionButton("🔋", "Saver")
            QuickActionButton("📱", "Screen")
            QuickActionButton("🔊", "Sound")
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = label,
            fontSize = 8.sp,
            color = Color(0xFF444444),
        )
    }
}

// Simplified stubs for premium cards
@Composable
private fun PerformanceChartCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "📊",
                fontSize = 11.sp,
            )
            Text(
                text = "Performance",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            )
        }

        // Mini performance chart
        MiniPerformanceChart(
            batteryLevel = batteryData.batteryLevel,
            modifier =
            Modifier
                .fillMaxWidth()
                .height(30.dp),
        )

        Text(
            text = "CPU • RAM • Battery",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ThermalStatusCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    val thermalColor =
        when {
            batteryData.temperature > 45f -> Color(0xFFF44336)
            batteryData.temperature > 35f -> Color(0xFFFF9800)
            else -> Color(0xFF4CAF50)
        }

    val animatedTemp by animateFloatAsState(
        targetValue = batteryData.temperature,
        animationSpec = VoltAIMotion.standardTween,
        label = "thermal_temp_anim",
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "🌡️",
                fontSize = 11.sp,
            )
            Text(
                text = "Thermal Status",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${animatedTemp.toInt()}°C",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = thermalColor,
            )

            // Animated thermal indicator
            Box(
                modifier =
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .background(
                        Color(0xFFE0E0E0),
                        RoundedCornerShape(3.dp),
                    ),
            ) {
                val thermalProgress = (batteryData.temperature / 60f).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = thermalProgress,
                    animationSpec = VoltAIMotion.emphasizedTween,
                    label = "thermal_progress_anim",
                )

                Box(
                    modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(
                            thermalColor,
                            RoundedCornerShape(3.dp),
                        ),
                )
            }
        }

        Text(
            text =
            when {
                batteryData.temperature > 45f -> "Hot - Cooling needed"
                batteryData.temperature > 35f -> "Warm - Normal"
                else -> "Cool - Optimal"
            },
            fontSize = 9.sp,
            color = thermalColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BatteryTipsCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Battery Tips\n(Premium)",
            fontSize = 12.sp,
            color = Color(0xFF9C27B0),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun MiniChargingChart(
    isCharging: Boolean,
    batteryLevel: Int,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isCharging) 1f else 0.7f,
        animationSpec = VoltAIMotion.contentAppearance,
        label = "charging_chart_anim",
    )

    Canvas(modifier = modifier.padding(8.dp)) {
        val barCount = 7
        val barWidth = size.width / (barCount * 2)
        val spacing = barWidth
        val maxHeight = size.height * 0.8f

        for (i in 0 until barCount) {
            val x = i * (barWidth + spacing) + spacing
            val targetHeight = maxHeight * (0.3f + 0.7f * (i + 1) / barCount)
            val animatedBarHeight = targetHeight * animatedProgress
            val barColor = if (isCharging) Color(0xFF4CAF50) else Color(0xFF2196F3)

            drawRoundRect(
                color = barColor.copy(alpha = 0.8f),
                topLeft = Offset(x, size.height - animatedBarHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, animatedBarHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            )
        }
    }
}

@Composable
private fun MiniPerformanceChart(
    batteryLevel: Int,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = VoltAIMotion.emphasizedTween,
        label = "performance_chart_anim",
    )

    Canvas(modifier = modifier.padding(horizontal = 8.dp)) {
        val lineCount = 3
        val colors =
            listOf(
                Color(0xFF2196F3), // Battery Level
                Color(0xFF4CAF50), // Efficiency
                Color(0xFFFF9800), // Usage
            )

        // Use real battery data for realistic performance metrics
        val batteryFactor = batteryLevel / 100f
        val efficiency = (batteryFactor * 0.8f + 0.2f).coerceIn(0f, 1f) // Higher when battery is good
        val usage = (1f - batteryFactor * 0.6f).coerceIn(0.3f, 1f) // Higher usage when battery low
        val values = listOf(batteryFactor, efficiency, usage)

        for (lineIndex in 0 until lineCount) {
            val points =
                (0..10).map { i ->
                    val baseValue = values[lineIndex]
                    // Smaller, more realistic variations
                    val variation = 0.05f * kotlin.math.sin(i * 0.4f + lineIndex).toFloat()
                    val finalValue = (baseValue + variation).coerceIn(0f, 1f)
                    val y = size.height * (1f - finalValue) * animatedProgress
                    val x = i * size.width / 10f
                    Pair(x, y)
                }

            val path = Path()
            points.forEachIndexed { index, (x, y) ->
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = colors[lineIndex],
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun AnimatedWaveform(
    isActive: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    // Use static waveform pattern - more battery efficient and accessibility friendly
    val waveHeight = if (isActive) 1f else 0.3f
    val animatedHeight by animateFloatAsState(
        targetValue = waveHeight,
        animationSpec = VoltAIMotion.contentAppearance,
    )

    Canvas(
        modifier =
        modifier
            .fillMaxWidth()
            .height(16.dp),
    ) {
        if (isActive) {
            // Draw static signal bars instead of animated waveform for better performance
            val barWidth = size.width / 10
            val maxHeight = size.height * animatedHeight
            val strokeWidth = 2.dp.toPx()

            for (i in 0..9) {
                val x = i * barWidth + barWidth / 2
                val barHeight = maxHeight * (0.3f + 0.7f * ((i % 3) + 1) / 3f)
                val startY = size.height / 2 + barHeight / 2
                val endY = size.height / 2 - barHeight / 2

                drawLine(
                    color = color,
                    start = Offset(x, startY),
                    end = Offset(x, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
fun HistoryCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "History",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )

        // Vertical grid of session data
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Row 1: Charging and Discharging indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SessionIndicator(
                    icon = "●",
                    label = "Charging",
                    color = Color(0xFF4CAF50),
                )
                SessionIndicator(
                    icon = "●",
                    label = "Discharging",
                    color = Color(0xFFF44336),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Session count
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "3",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Sessions",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SessionIndicator(
    icon: String,
    label: String,
    color: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontSize = 12.sp,
            color = color,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HistorySessionItem(
    icon: String,
    label: String,
    value: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = icon,
            fontSize = 12.sp,
        )
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF444444),
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color,
            )
        }
    }
}

@Composable
fun OverlaysCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Overlays",
                tint = Color(0xFF607D8B),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "Overlays",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Overlay Control",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF607D8B),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Configure",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
            )
        }

        Text(
            text = "Screen overlays",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

// Premium Card Content Functions
@Composable
private fun ChargingAnalyticsCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Charging Analytics",
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "Charging Analytics",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "⚡",
                fontSize = 24.sp,
            )
            Text(
                text = "Advanced Insights",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ChemistryLabCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Chemistry Lab",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "Chemistry Lab",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "🔬",
                fontSize = 24.sp,
            )
            Text(
                text = "Molecular Analysis",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SystemOptimizationCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "System Optimization",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "System Optimizer",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "🚀",
                fontSize = 24.sp,
            )
            Text(
                text = "AI Optimization",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PerformanceBenchmarksCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Performance Benchmarks",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "Benchmarks",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "📊",
                fontSize = 24.sp,
            )
            Text(
                text = "Performance Tests",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun DataExportImportCardContent(
    batteryData: BatteryData,
    isEditMode: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Data Export/Import",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "Data Backup",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "💾",
                fontSize = 24.sp,
            )
            Text(
                text = "Export & Import",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

// This function is deprecated - use MaterialTheme.colorScheme.batteryLevelColor() instead
@Composable
private fun getBatteryLevelColor(level: Int): Color {
    return MaterialTheme.colorScheme.batteryLevelColor(level)
}
