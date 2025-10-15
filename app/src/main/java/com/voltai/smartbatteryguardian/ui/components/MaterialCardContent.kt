package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.ui.home.CardSize
import com.voltai.smartbatteryguardian.ui.home.CardType
import com.voltai.smartbatteryguardian.ui.theme.LocalPremiumColors
import kotlin.math.roundToInt

// Spacing extension is defined at the bottom via data class Spacing; removed older LocalSpacing to avoid conflicts

/**
 * Simplified Material Design 3 compliant card content implementation.
 *
 * Removes excessive animations, custom gradients, and hardcoded colors,
 * replacing them with semantic Material 3 tokens.
 */
private enum class CardContentVariant { Compact, Regular, Expanded }

@Composable
fun MaterialCardContent(
    cardType: CardType,
    batteryData: BatteryData,
    size: CardSize,
    isEditMode: Boolean = false,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
) {
    val variant =
        remember(size) {
            when {
                size.rows >= 2 -> CardContentVariant.Expanded
                size.columns >= 2 -> CardContentVariant.Regular
                else -> CardContentVariant.Compact
            }
        }
    Surface(
        modifier = modifier,
        color =
        if (isEditMode) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        } else {
            Color.Transparent
        },
    ) {
        when (cardType) {
            CardType.BATTERY_LEVEL -> BatteryLevelCard(batteryData, variant, reduceMotion)
            CardType.CHARGING_STATUS -> ChargingStatusCard(batteryData, variant)
            CardType.TEMPERATURE -> TemperatureCard(batteryData, variant, reduceMotion)
            CardType.HEALTH -> HealthCard(batteryData, variant, reduceMotion)
            CardType.CURRENT_VOLTAGE -> CurrentVoltageCard(batteryData, variant)
            CardType.SCREEN_TIME -> ScreenTimeCard(batteryData, variant)
            CardType.APP_USAGE -> AppUsageCard(batteryData, variant)
            CardType.AI_INSIGHTS -> AIInsightsCard(batteryData)
            CardType.POWER_MODES -> PowerModesCard(batteryData)
            CardType.QUICK_ACTIONS -> QuickActionsCard(batteryData)
            CardType.HISTORY -> HistoryCard(batteryData, variant)
            CardType.OVERLAYS -> OverlaysCard(batteryData)
            // Premium Cards
            CardType.CHARGING_ANALYTICS -> PremiumCard("Charging Analytics", "⚡")
            CardType.CHEMISTRY_LAB -> PremiumCard("Chemistry Lab", "🔬")
            CardType.SYSTEM_OPTIMIZATION -> PremiumCard("System Optimizer", "🚀")
            CardType.PERFORMANCE_BENCHMARKS -> PremiumCard("Benchmarks", "📊")
            CardType.DATA_EXPORT_IMPORT -> PremiumCard("Data Backup", "💾")
            CardType.BATTERY_HEALTH_ANALYTICS -> PremiumCard("Health Analytics", "📈")
            CardType.SMART_CHARGING -> PremiumCard("Smart Charging", "🧠")
            CardType.BATTERY_DIAGNOSTICS -> PremiumCard("Diagnostics", "🔍")
            CardType.REAL_TIME_MONITORING -> PremiumCard("Real-time Monitor", "📡")
            CardType.BATTERY_OPTIMIZATION -> PremiumCard("Optimization", "⚡")
            else -> DefaultCard(cardType.name)
        }
    }
}

@Composable
private fun AnimatedHealthRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
) {
    val density = LocalDensity.current
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    // Always use static ring - no continuous animations
    val stroke = with(density) { 6.dp.toPx() }
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f - stroke / 2f
        // Track
        drawCircle(
            color = surfaceVariantColor,
            radius = radius,
            style = Stroke(width = stroke),
        )
        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun CardHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MetricPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun MiniSparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val data = values
    val shadowColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val step = if (data.size > 1) w / (data.size - 1) else w
        if (data.isNotEmpty()) {
            val path = Path()
            val shadow = Path()
            data.forEachIndexed { i, v ->
                val x = i * step
                val y = h - (v.coerceIn(0f, 1f) * h)
                if (i == 0) {
                    path.moveTo(x, y)
                    shadow.moveTo(x, y + 1f)
                } else {
                    path.lineTo(x, y)
                    shadow.lineTo(x, y + 1f)
                }
            }
            // subtle shadow for 3D depth
            drawPath(
                path = shadow,
                color = shadowColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = StrokeCap.Round),
            )
            drawPath(
                path = path,
                color = lineColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun MiniBars(
    values: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val data = values
    val topHighlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    val bottomShadow = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barWidth = w / (data.size * 1.8f)
        val gap = barWidth * 0.8f
        var x = 0f
        data.forEach { v ->
            val barH = (v.coerceIn(0f, 1f) * h)
            // vertical gradient for subtle 3D
            drawRoundRect(
                brush =
                Brush.verticalGradient(
                    colors =
                    listOf(
                        topHighlight,
                        barColor,
                        bottomShadow,
                    ),
                ),
                topLeft = Offset(x, h - barH),
                size = androidx.compose.ui.geometry.Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(x = barWidth / 3f, y = barWidth / 3f),
            )
            x += barWidth + gap
        }
    }
}

@Composable
private fun BatteryLevelCard(
    batteryData: BatteryData,
    variant: CardContentVariant,
    reduceMotion: Boolean,
) {
    val level = batteryData.batteryLevel.coerceIn(0, 100)
    val statusText = if (batteryData.isCharging) "Charging" else "Discharging"
    val levelColor =
        when {
            level >= 80 -> Color(0xFF4CAF50)
            level >= 50 -> Color(0xFF8BC34A)
            level >= 20 -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }

    // Get battery level history from ViewModel for Battery Guru-style chart
    val readingsVm: com.voltai.smartbatteryguardian.viewmodels.BatteryReadingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val levelHistory by readingsVm.levelSeries.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header with time remaining on top right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "BATTERY INFO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
            )

            // Time remaining on top right
            val remaining = batteryData.remainingTime?.trim().orEmpty()
            if (remaining.isNotEmpty() && !remaining.equals("unknown", ignoreCase = true) && variant != CardContentVariant.Compact) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (batteryData.isCharging) "Time to full" else "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                    )
                    Text(
                        text = remaining,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Level percentage with status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "$level",
                    style =
                    when (variant) {
                        CardContentVariant.Compact -> MaterialTheme.typography.displaySmall
                        else -> MaterialTheme.typography.displayMedium
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = levelColor,
                    letterSpacing = (-1).sp,
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color =
                if (batteryData.isCharging) {
                    Color(
                        0xFF4CAF50,
                    ).copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier =
                        Modifier
                            .size(6.dp)
                            .background(
                                color =
                                if (batteryData.isCharging) {
                                    Color(
                                        0xFF4CAF50,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                shape = CircleShape,
                            ),
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color =
                        if (batteryData.isCharging) {
                            Color(
                                0xFF2E7D32,
                            )
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Battery Guru-style 15-bar chart with dates - fills available space
        Battery30DayBarChart(
            levelHistory = levelHistory,
            currentLevel = level,
            isCharging = batteryData.isCharging,
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f), // Fill remaining space in card
        )
    }
}

/**
 * Battery Guru-style 15-bar chart with dates, borders, and color-coded segments
 */
@Composable
private fun Battery30DayBarChart(
    levelHistory: List<Int>,
    currentLevel: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
) {
    val barCount = 15
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // Sample 30-day data into 15 bars (2 days per bar average)
    val chartData =
        if (levelHistory.isNotEmpty()) {
            val segmentSize = (levelHistory.size.toFloat() / barCount).coerceAtLeast(1f).toInt()
            levelHistory.chunked(segmentSize).take(barCount).map { segment ->
                segment.average().toInt().coerceIn(0, 100)
            }
        } else {
            List(barCount) { index ->
                if (index >= barCount - 3) currentLevel else 50
            }
        }

    // Generate dates for each bar (going back 30 days, 2 days per bar)
    val dates =
        remember {
            List(barCount) { index ->
                val daysAgo = (barCount - 1 - index) * 2
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                val month = calendar.get(java.util.Calendar.MONTH) + 1
                "%02d/%02d".format(day, month)
            }
        }

    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textStyle =
        androidx.compose.ui.text.TextStyle(
            fontSize = 8.sp,
            color = textColor,
        )

    Canvas(modifier = modifier) {
        val dateHeight = with(density) { 16.dp.toPx() }
        val chartHeight = size.height - dateHeight
        val totalWidth = size.width
        val barWidth = totalWidth / (barCount * 1.4f)
        val gap = (totalWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)
        val borderWidth = 1.5f

        chartData.forEachIndexed { index, level ->
            val x = index * (barWidth + gap)
            val maxBarHeight = chartHeight * 0.92f
            val barHeight = (level / 100f) * maxBarHeight
            val y = chartHeight - barHeight

            // Color for segments based on level
            val barColor =
                when {
                    level >= 80 -> Color(0xFF4CAF50) // Green
                    level >= 50 -> Color(0xFF8BC34A) // Light green
                    level >= 20 -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFFF44336) // Red
                }

            // Draw bar background (empty/unfilled portion) with border
            drawRoundRect(
                color = borderColor.copy(alpha = 0.15f),
                topLeft = Offset(x, chartHeight * 0.08f),
                size = androidx.compose.ui.geometry.Size(barWidth, maxBarHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            )

            // Draw filled portion with color-coded segments
            if (barHeight > 0) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                )
            }

            // Draw border around entire bar
            drawRoundRect(
                color = borderColor.copy(alpha = 0.5f),
                topLeft = Offset(x, chartHeight * 0.08f),
                size = androidx.compose.ui.geometry.Size(barWidth, maxBarHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = borderWidth),
            )

            // Draw date at bottom - show every other date to avoid crowding
            if (index % 2 == 0 || barCount <= 10) {
                val dateText = dates[index]
                val textLayoutResult =
                    textMeasurer.measure(
                        text = androidx.compose.ui.text.AnnotatedString(dateText),
                        style = textStyle,
                    )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft =
                    Offset(
                        x + (barWidth - textLayoutResult.size.width) / 2f,
                        chartHeight + 2.dp.toPx(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun BatteryCapsuleBar(
    level: Int,
    charging: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
    segmentCount: Int = 20,
    reduceMotion: Boolean = false,
) {
    // Animated glint (disabled when reduceMotion)
    val shift: Float =
        if (!reduceMotion) {
            val glint = rememberInfiniteTransition(label = "capsule_glint")
            glint.animateFloat(
                initialValue = -0.4f,
                targetValue = 1.4f,
                animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = if (charging) 1400 else 2600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "capsule_shift",
            ).value
        } else {
            0f
        }

    val glintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barWidth = w / (segmentCount * 1.8f)
        val gap = barWidth * 0.8f
        val radius = barWidth
        var x = 0f
        val activeSegments = ((level / 100f) * segmentCount).toInt().coerceIn(0, segmentCount)
        repeat(segmentCount) { idx ->
            val color = if (idx < activeSegments) activeColor else inactiveColor
            drawRoundRect(
                color = color,
                topLeft = Offset(x, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            )
            x += barWidth + gap
        }

        if (!reduceMotion) {
            // Moving shine overlay
            val startX = w * (shift - 0.15f)
            val endX = w * (shift + 0.15f)
            drawRect(
                brush =
                Brush.linearGradient(
                    colors = listOf(Color.Transparent, glintColor, Color.Transparent),
                    start = Offset(startX, 0f),
                    end = Offset(endX, h),
                ),
                size = size,
            )
        }
    }
}

@Composable
private fun ChargingStatusCard(
    batteryData: BatteryData,
    variant: CardContentVariant = CardContentVariant.Regular,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
        Arrangement.spacedBy(
            when (variant) {
                CardContentVariant.Compact -> 8.dp
                CardContentVariant.Regular -> 10.dp
                CardContentVariant.Expanded -> 12.dp
            },
        ),
    ) {
        // Header
        if (variant != CardContentVariant.Compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Charging status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // Status with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val statusIcon = if (batteryData.isCharging) "⚡" else "🔋"
            Text(
                text = statusIcon,
                style = MaterialTheme.typography.displaySmall,
                fontSize =
                when (variant) {
                    CardContentVariant.Compact -> 28.sp
                    else -> 32.sp
                },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (batteryData.isCharging) "Charging" else "Discharging",
                    style =
                    when (variant) {
                        CardContentVariant.Compact -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleLarge
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                if (batteryData.isCharging && variant != CardContentVariant.Compact) {
                    Text(
                        text = batteryData.chargingSpeed,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }

        // Current and Voltage pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val current = "${kotlin.math.abs(batteryData.current)} mA"
            val voltage = "${(batteryData.voltage * 1000).roundToInt()} mV"
            MetricPill(current, modifier = Modifier.weight(1f))
            MetricPill(voltage, modifier = Modifier.weight(1f))
        }

        // Power indicator
        if (variant != CardContentVariant.Compact) {
            val watts = (kotlin.math.abs(batteryData.current).toDouble() / 1000.0) * batteryData.voltage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Power",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (watts > 0) String.format("%.1f W", watts) else "— W",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun TemperatureCard(
    batteryData: BatteryData,
    variant: CardContentVariant = CardContentVariant.Regular,
    reduceMotion: Boolean = false,
) {
    val tempColor =
        when {
            batteryData.temperature > 45f -> Color(0xFFF44336)
            batteryData.temperature > 35f -> Color(0xFFFF9800)
            else -> Color(0xFF4CAF50)
        }

    val tempStatus =
        when {
            batteryData.temperature > 45f -> "Hot"
            batteryData.temperature > 35f -> "Warm"
            batteryData.temperature < 10f -> "Cold"
            else -> "Normal"
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
        Arrangement.spacedBy(
            when (variant) {
                CardContentVariant.Compact -> 8.dp
                CardContentVariant.Regular -> 10.dp
                CardContentVariant.Expanded -> 12.dp
            },
        ),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Temperature",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp),
            )
        }

        // Temperature display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "${batteryData.temperature.toInt()}",
                        style =
                        when (variant) {
                            CardContentVariant.Compact -> MaterialTheme.typography.headlineLarge
                            else -> MaterialTheme.typography.displaySmall
                        },
                        fontWeight = FontWeight.Bold,
                        color = tempColor,
                        letterSpacing = (-0.5).sp,
                    )
                    Text(
                        text = "°C",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier =
                        Modifier
                            .size(8.dp)
                            .background(color = tempColor, shape = CircleShape),
                    )
                    Text(
                        text = tempStatus,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            }

            Text(
                text = "🌡️",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 48.sp,
            )
        }

        // Temperature gauge
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val progress = (batteryData.temperature / 60f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = progress,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        when (variant) {
                            CardContentVariant.Compact -> 8.dp
                            CardContentVariant.Regular -> 10.dp
                            CardContentVariant.Expanded -> 12.dp
                        },
                    ),
                color = tempColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            if (variant != CardContentVariant.Compact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "0°",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                    Text(
                        text = "60°",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthCard(
    batteryData: BatteryData,
    variant: CardContentVariant = CardContentVariant.Regular,
    reduceMotion: Boolean = false,
) {
    val healthColor =
        when {
            batteryData.healthPercentage >= 85 -> Color(0xFF4CAF50)
            batteryData.healthPercentage >= 70 -> Color(0xFF8BC34A)
            batteryData.healthPercentage >= 50 -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }

    val healthStatus =
        when {
            batteryData.healthPercentage >= 85 -> "Excellent"
            batteryData.healthPercentage >= 70 -> "Good"
            batteryData.healthPercentage >= 50 -> "Fair"
            else -> "Poor"
        }

    // Parse capacity string to calculate mAh-based percentage
    val capacityPercentage =
        try {
            val capacityText = batteryData.capacity
            // Extract current and design capacity (e.g., "2800 / 3500 mAh")
            val parts = capacityText.split("/")
            if (parts.size == 2) {
                val current = parts[0].trim().replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                val design = parts[1].trim().replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                if (design > 0) ((current.toFloat() / design) * 100).toInt() else batteryData.healthPercentage
            } else {
                batteryData.healthPercentage
            }
        } catch (e: Exception) {
            batteryData.healthPercentage
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "BATTERY HEALTH",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp),
            )
        }

        // Large centered health ring with text inside - fills available space
        Box(
            contentAlignment = Alignment.Center,
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f), // Fill remaining vertical space
        ) {
            // Health ring - sized to fit within available space
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                Modifier
                    .fillMaxHeight(0.90f) // Use 90% of available height
                    .aspectRatio(1f), // Keep circular
            ) {
                AnimatedHealthRing(
                    progress = capacityPercentage / 100f,
                    color = healthColor,
                    modifier = Modifier.fillMaxSize(),
                    reduceMotion = true,
                )

                // Only percentage in circle center
                Text(
                    text = "$capacityPercentage%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = healthColor,
                    letterSpacing = (-1).sp,
                )
            }
        }

        // Bottom capacity info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Current capacity",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
            )
            Text(
                text = batteryData.capacity,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CurrentVoltageCard(
    batteryData: BatteryData,
    variant: CardContentVariant = CardContentVariant.Regular,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
        Arrangement.spacedBy(
            when (variant) {
                CardContentVariant.Compact -> 8.dp
                CardContentVariant.Regular -> 10.dp
                CardContentVariant.Expanded -> 12.dp
            },
        ),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Electric current",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp),
            )
        }

        // Current display
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val currentVal = kotlin.math.abs(batteryData.current)
            Text(
                text = if (currentVal == 0) "—" else "$currentVal",
                style =
                when (variant) {
                    CardContentVariant.Compact -> MaterialTheme.typography.headlineLarge
                    else -> MaterialTheme.typography.displaySmall
                },
                fontWeight = FontWeight.Bold,
                color = if (batteryData.isCharging) Color(0xFF4CAF50) else Color(0xFFFF9800),
                letterSpacing = (-0.5).sp,
            )
            Text(
                text = "mA",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }

        // Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier =
                Modifier
                    .size(8.dp)
                    .background(
                        color = if (batteryData.isCharging) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        shape = CircleShape,
                    ),
            )
            Text(
                text = if (batteryData.isCharging) "Charging" else "Discharging",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }

        // Voltage and Power metrics
        if (variant != CardContentVariant.Compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val voltage = if (batteryData.voltage <= 0f) "—" else String.format("%.2f V", batteryData.voltage)
                val wattsVal = (kotlin.math.abs(batteryData.current).toDouble() / 1000.0) * batteryData.voltage
                val watts = if (wattsVal <= 0) "—" else String.format("%.1f W", wattsVal)

                MetricPill(voltage, modifier = Modifier.weight(1f))
                MetricPill(watts, modifier = Modifier.weight(1f))
            }
        }
    }
}

// Simplified implementations for other card types
@Composable
private fun ScreenTimeCard(
    batteryData: BatteryData,
    variant: CardContentVariant = CardContentVariant.Regular,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Header
        if (variant != CardContentVariant.Compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Screen time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // Screen On
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Screen On",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                Text(
                    text = batteryData.screenOnTime,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "🔆",
                style = MaterialTheme.typography.displaySmall,
                fontSize = 36.sp,
            )
        }

        // Screen Off
        if (variant != CardContentVariant.Compact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Screen Off",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                    Text(
                        text = batteryData.screenOffTime,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = "🌙",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 28.sp,
                )
            }
        }

        // Battery used
        if (variant == CardContentVariant.Expanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Battery used",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                Text(
                    text = batteryData.batteryUsed,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ScreenTimeItem(
    icon: String,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    explanation: String = "",
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = icon, style = MaterialTheme.typography.bodyMedium)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
            if (explanation.isNotEmpty()) {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun AppUsageCard(
    batteryData: BatteryData,
    variant: CardContentVariant = CardContentVariant.Regular,
) {
    // Get top battery-consuming apps from ViewModel
    val appUsageVm: com.voltai.smartbatteryguardian.viewmodels.AppUsageUiViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val appUsageList by appUsageVm.apps.collectAsState()

    val topApps =
        remember(appUsageList) {
            appUsageList.sortedByDescending { it.batteryUsage }.take(5)
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement =
        Arrangement.spacedBy(
            when (variant) {
                CardContentVariant.Compact -> 6.dp
                else -> 8.dp
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardHeader("Top Apps")
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

        if (topApps.isNotEmpty()) {
            val displayCount =
                when (variant) {
                    CardContentVariant.Compact -> 2
                    CardContentVariant.Regular -> 3
                    CardContentVariant.Expanded -> 6
                }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until minOf(displayCount, topApps.size)) {
                    val app = topApps[i]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${app.batteryUsage.toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "No app data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Grant usage access",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = icon, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AppUsageItem(
    name: String,
    percentage: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = percentage,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun AIInsightsCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "AI",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "AI Insights",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = "View Details",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Text(
                text = "Score ${batteryData.batteryScore}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun HistoryCard(
    batteryData: BatteryData,
    variant: CardContentVariant = CardContentVariant.Regular,
) {
    // Get battery readings history for proper charging/discharging sessions
    val readingsVm: com.voltai.smartbatteryguardian.viewmodels.BatteryReadingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val readings by readingsVm.last24hReadings.collectAsState()

    // Process readings into charging sessions
    val sessions =
        remember(readings) {
            if (readings.isEmpty()) {
                emptyList()
            } else {
                val sessionsList = mutableListOf<ChargingSession>()
                var currentSession: ChargingSession? = null

                readings.forEachIndexed { index, reading ->
                    val isCharging = reading.current > 0

                    if (currentSession == null) {
                        // Start new session
                        currentSession =
                            ChargingSession(
                                isCharging = isCharging,
                                durationMinutes = 0,
                                startLevel = reading.batteryLevel,
                                endLevel = reading.batteryLevel,
                                timestamp = reading.timestamp,
                            )
                    } else if (currentSession!!.isCharging != isCharging) {
                        // Session type changed, save and start new
                        sessionsList.add(currentSession!!)
                        currentSession =
                            ChargingSession(
                                isCharging = isCharging,
                                durationMinutes = 0,
                                startLevel = reading.batteryLevel,
                                endLevel = reading.batteryLevel,
                                timestamp = reading.timestamp,
                            )
                    } else {
                        // Continue current session
                        val duration = ((reading.timestamp - currentSession!!.timestamp) / (60 * 1000)).toInt()
                        currentSession =
                            currentSession!!.copy(
                                durationMinutes = duration.coerceAtLeast(1),
                                endLevel = reading.batteryLevel,
                            )
                    }
                }

                // Add last session
                currentSession?.let { sessionsList.add(it) }
                sessionsList.takeLast(10)
            }
        }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardHeader("History")
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

        // Charging/Discharging sessions chart
        if (sessions.isNotEmpty()) {
            ChargingSessionsChart(
                sessions = sessions,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true), // Fill all remaining space
            )

            if (variant == CardContentVariant.Expanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier =
                                Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape),
                            )
                            Text(
                                text = "Charging",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier =
                                Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFF44336), CircleShape),
                            )
                            Text(
                                text = "Discharging",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = "${sessions.size} sessions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            // Empty-state placeholder - fills available space
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "No history data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Data will appear after 24h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun PowerModesCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardHeader("Power modes")
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PowerModeItem("⚡", "Performance", true)
            PowerModeItem("🔋", "Balanced", false)
            PowerModeItem("🍃", "Power Saver", false)
        }
    }
}

@Composable
private fun PowerModeItem(
    icon: String,
    label: String,
    isActive: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = icon, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
        )
        if (isActive) {
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(6.dp),
            ) {}
        }
    }
}

@Composable
private fun QuickActionsCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardHeader("Quick actions")
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

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
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = icon, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OverlaysCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardHeader("Overlays")
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "📊", style = MaterialTheme.typography.headlineMedium)
                Column {
                    Text(
                        text = "Battery Overlay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Real-time battery stats",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Text(
            text = "Tap to configure overlays",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PremiumCard(
    title: String,
    icon: String,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = "PRO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = "Tap to unlock with Premium",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun DefaultCard(cardName: String) {
    Box(
        modifier =
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cardName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun getBatteryLevelColor(level: Int): androidx.compose.ui.graphics.Color {
    val premiumColors = LocalPremiumColors.current
    return when {
        level > 80 -> premiumColors.batteryGreen
        level > 50 -> premiumColors.batteryYellow
        level > 20 -> premiumColors.warning
        else -> premiumColors.batteryRed
    }
}

// Extension property for consistent spacing
val MaterialTheme.spacing: Spacing
    @Composable get() = Spacing()

data class Spacing(
    val extraSmall: androidx.compose.ui.unit.Dp = 4.dp,
    val small: androidx.compose.ui.unit.Dp = 8.dp,
    val medium: androidx.compose.ui.unit.Dp = 16.dp,
    val large: androidx.compose.ui.unit.Dp = 24.dp,
    val extraLarge: androidx.compose.ui.unit.Dp = 32.dp,
)
