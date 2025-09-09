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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.ui.animation.AnimationSpecs
import com.voltai.smartbatteryguardian.ui.components.*
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun CardContent(
    cardType: CardType,
    batteryData: BatteryData,
    isEditMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.05f))
            )
        }
    }
}

@Composable
private fun BatteryLevelCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    val animatedLevel by animateFloatAsState(
        targetValue = batteryData.batteryLevel / 100f,
        animationSpec = AnimationSpecs.bouncy
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "${batteryData.batteryLevel}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = getBatteryLevelColor(batteryData.batteryLevel),
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "Battery Level",
                    fontSize = 12.sp,
                    color = Color(0xFF444444),
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = if (batteryData.isCharging) Icons.Default.Phone else Icons.Default.Star,
                contentDescription = "Battery",
                modifier = Modifier.size(28.dp),
                tint = getBatteryLevelColor(batteryData.batteryLevel)
            )
        }

        // Animated Battery Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    Color(0xFFF0F0F0),
                    RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedLevel)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                getBatteryLevelColor(batteryData.batteryLevel).copy(alpha = 0.8f),
                                getBatteryLevelColor(batteryData.batteryLevel)
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }

        Text(
            text = batteryData.remainingTime,
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ChargingStatusCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    val pulsing by rememberInfiniteTransition().animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Status",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp)
            ) {
                if (batteryData.isCharging) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Charging",
                        modifier = Modifier
                            .size(24.dp)
                            .scale(pulsing),
                        tint = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = batteryData.chargingStatus,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (batteryData.isCharging) Color(0xFF4CAF50) else Color(0xFF444444),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = if (batteryData.isCharging) "Plugged In" else "On Battery",
            fontSize = 10.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TemperatureCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    val tempColor = when {
        batteryData.temperature > 45f -> Color(0xFFE57373)
        batteryData.temperature > 35f -> Color(0xFFFFB74D)
        else -> Color(0xFF81C784)
    }

    val animatedTemp by animateFloatAsState(
        targetValue = batteryData.temperature,
        animationSpec = AnimationSpecs.smooth
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Temperature",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${animatedTemp.toInt()}°C",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = tempColor
            )

            Text(
                text = when {
                    batteryData.temperature > 45f -> "Hot"
                    batteryData.temperature > 35f -> "Warm"
                    else -> "Normal"
                },
                fontSize = 11.sp,
                color = tempColor,
                fontWeight = FontWeight.Medium
            )
        }

        // Temperature Indicator Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Color(0xFFF0F0F0),
                    RoundedCornerShape(2.dp)
                )
        ) {
            val progress = (batteryData.temperature / 60f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        tempColor,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun HealthCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    val animatedProgress by animateFloatAsState(
        targetValue = batteryData.healthPercentage / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Battery Health",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.size(32.dp),
                strokeWidth = 4.dp,
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE8F5E8)
            )

            Column {
                Text(
                    text = "${batteryData.healthPercentage}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Good",
                    fontSize = 10.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = batteryData.capacity,
            fontSize = 10.sp,
            color = Color(0xFF999999)
        )
    }
}

@Composable
private fun CurrentVoltageCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Current & Voltage",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${batteryData.current} mA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (batteryData.isCharging) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
                Text(
                    text = String.format("%.1f V", batteryData.voltage),
                    fontSize = 14.sp,
                    color = Color(0xFF444444),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Mini Waveform
        AnimatedWaveform(
            isActive = batteryData.isCharging,
            color = if (batteryData.isCharging) Color(0xFF4CAF50) else Color(0xFFFF9800)
        )
    }
}

@Composable
private fun ScreenTimeCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Screen Time",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScreenTimeItem(
                icon = "📱",
                label = "Screen On",
                value = batteryData.screenOnTime,
                color = Color(0xFF2196F3)
            )

            ScreenTimeItem(
                icon = "🔒",
                label = "Screen Off",
                value = batteryData.screenOffTime,
                color = Color(0xFF333333)
            )
        }

        Text(
            text = "Battery Used: ${batteryData.batteryUsed}",
            fontSize = 9.sp,
            color = Color(0xFF999999)
        )
    }
}

@Composable
private fun ScreenTimeItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = icon,
            fontSize = 12.sp
        )
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF444444)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun AIInsightsCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "AI",
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "AI Insights",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "Battery optimization active. 23% improvement detected.",
            fontSize = 12.sp,
            color = Color.Black,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF9C27B0).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "View Details",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 9.sp,
                    color = Color(0xFF9C27B0),
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "+23%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

// Simplified implementations for other card types
@Composable
private fun AppUsageCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Top Apps",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AppUsageItem("Chrome", "25%", Color(0xFFE57373))
            AppUsageItem("YouTube", "18%", Color(0xFFFFB74D))
            AppUsageItem("Instagram", "12%", Color(0xFF81C784))
        }

        Text(
            text = "Last 24 hours",
            fontSize = 9.sp,
            color = Color(0xFF999999)
        )
    }
}

@Composable
private fun AppUsageItem(name: String, percentage: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 10.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percentage,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun ChargingHistoryCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Charging History",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )

        // Mini chart placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Color(0xFFF8F9FA),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chart",
                fontSize = 10.sp,
                color = Color(0xFF444444)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "3 sessions",
                fontSize = 10.sp,
                color = Color(0xFF444444)
            )
            Text(
                text = "Today",
                fontSize = 10.sp,
                color = Color(0xFF444444)
            )
        }
    }
}

@Composable
private fun PowerModesCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Power Modes",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PowerModeItem("⚡", "Performance", true)
            PowerModeItem("🔋", "Battery Saver", false)
            PowerModeItem("🌙", "Ultra Saver", false)
        }
    }
}

@Composable
private fun PowerModeItem(icon: String, name: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, fontSize = 10.sp)
        Text(
            text = name,
            fontSize = 9.sp,
            color = if (isActive) Color(0xFF2196F3) else Color(0xFF444444),
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
        if (isActive) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF2196F3),
                modifier = Modifier.size(4.dp)
            ) {}
        }
    }
}

@Composable
private fun QuickActionsCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Quick Actions",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton("🔋", "Saver")
            QuickActionButton("📱", "Screen")
            QuickActionButton("🔊", "Sound")
        }
    }
}

@Composable
private fun QuickActionButton(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = label,
            fontSize = 8.sp,
            color = Color(0xFF444444)
        )
    }
}

// Simplified stubs for premium cards
@Composable
private fun PerformanceChartCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Performance Chart\n(Premium)",
            fontSize = 12.sp,
            color = Color(0xFF9C27B0),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ThermalStatusCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Thermal Status\n(Premium)",
            fontSize = 12.sp,
            color = Color(0xFF9C27B0),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BatteryTipsCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Battery Tips\n(Premium)",
            fontSize = 12.sp,
            color = Color(0xFF9C27B0),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AnimatedWaveform(
    isActive: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedValue by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        if (isActive) {
            val path = Path()
            val points = 20
            val width = size.width
            val height = size.height
            val amplitude = height * 0.3f
            val frequency = 4f

            for (i in 0 until points) {
                val x = (i.toFloat() / (points - 1)) * width
                val phase = animatedValue * 2 * PI + (x / width) * frequency * 2 * PI
                val y = height / 2 + amplitude * sin(phase).toFloat()

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun HistoryCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "History",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "History",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium
            )
        }

        // Show recent session info
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HistorySessionItem(
                icon = "🔋",
                label = "Last Session",
                value = "2h 15m",
                color = Color(0xFF4CAF50)
            )
            HistorySessionItem(
                icon = "⚡",
                label = "Charged",
                value = "+45%",
                color = Color(0xFF2196F3)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "5 sessions",
                fontSize = 9.sp,
                color = Color(0xFF999999)
            )
            Text(
                text = "Today",
                fontSize = 9.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
private fun HistorySessionItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = icon,
            fontSize = 12.sp
        )
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF444444)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun OverlaysCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Overlays",
                tint = Color(0xFF607D8B),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Overlays",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Overlay Control",
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF607D8B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Configure",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444)
            )
        }

        Text(
            text = "Screen overlays",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

// Premium Card Content Functions
@Composable
private fun ChargingAnalyticsCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Charging Analytics",
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Charging Analytics",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚡",
                fontSize = 24.sp
            )
            Text(
                text = "Advanced Insights",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChemistryLabCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Chemistry Lab",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Chemistry Lab",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🔬",
                fontSize = 24.sp
            )
            Text(
                text = "Molecular Analysis",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SystemOptimizationCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "System Optimization",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "System Optimizer",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🚀",
                fontSize = 24.sp
            )
            Text(
                text = "AI Optimization",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PerformanceBenchmarksCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Performance Benchmarks",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Benchmarks",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "📊",
                fontSize = 24.sp
            )
            Text(
                text = "Performance Tests",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DataExportImportCardContent(batteryData: BatteryData, isEditMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Data Export/Import",
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Data Backup",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "💾",
                fontSize = 24.sp
            )
            Text(
                text = "Export & Import",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Premium feature",
            fontSize = 9.sp,
            color = Color(0xFF999999),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

private fun getBatteryLevelColor(level: Int): Color {
    return when {
        level > 80 -> Color(0xFF4CAF50)
        level > 50 -> Color(0xFFFF9800)
        level > 20 -> Color(0xFFFF5722)
        else -> Color(0xFFD32F2F)
    }
}
