package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.ui.components.FilterChipsRow
import com.voltai.smartbatteryguardian.utils.getBatteryColor
import com.voltai.smartbatteryguardian.utils.getHealthColor
import com.voltai.smartbatteryguardian.utils.getHealthStatus
import com.voltai.smartbatteryguardian.utils.getTemperatureColor
import com.voltai.smartbatteryguardian.viewmodels.BatteryReadingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryInfoDetailScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val readingsVm: BatteryReadingsViewModel = hiltViewModel()
    val levelSeries by readingsVm.levelSeries.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Battery Info",
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
            // Header: big percent and status - Battery Guru style
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "${batteryData.batteryLevel}",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-1).sp,
                        )
                        Text(
                            text = "%",
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendDot(color = Color(0xFF4CAF50), label = "Charging")
                        LegendDot(color = MaterialTheme.colorScheme.onSurfaceVariant, label = "Discharging")
                        LegendDot(color = Color(0xFFF44336), label = "High usage")
                    }
                }
            }

            // Battery history capsule chart with range chips
            item {
                var range by remember { mutableStateOf(0) } // 0: 24h, 1: Since unplug
                val series =
                    remember(levelSeries, batteryData.batteryLevel, range) {
                        val base = if (levelSeries.isNotEmpty()) levelSeries else List(24) { batteryData.batteryLevel }
                        if (range == 0) base.takeLast(24) else base.takeLast(24) // placeholder since unplug logic
                    }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CapsuleLevelChart(levels = series)
                        Spacer(Modifier.height(12.dp))
                        RangeChips(selected = range, onSelectedChange = { range = it })
                    }
                }
            }

            // Alarms row
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: navigate to alarms */ }
                        .semantics { contentDescription = "Alarms" },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = "Alarms", style = MaterialTheme.typography.titleSmall)
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Estimated remaining time block
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Estimated remaining time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = batteryData.remainingTime,
                        style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoCardMini(
                            title = "Screen On",
                            value = batteryData.screenOnTime,
                            subtitle = "Remaining",
                            modifier = Modifier.weight(1f),
                        )
                        InfoCardMini(
                            title = "Screen Off",
                            value = batteryData.screenOffTime,
                            subtitle = "Remaining",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Detailed Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Battery Details",
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BatteryDetailRow(
                            icon = Icons.Default.Star,
                            label = "Current Level",
                            value = "${batteryData.batteryLevel}%",
                            iconColor = getBatteryColor(batteryData.batteryLevel),
                        )

                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Status",
                            value = batteryData.chargingStatus,
                            iconColor = if (batteryData.isCharging) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        )

                        BatteryDetailRow(
                            icon = Icons.Default.Warning,
                            label = "Health",
                            value = "${getHealthStatus(
                                batteryData.healthPercentage,
                            )} (${batteryData.healthPercentage}%)",
                            iconColor = getHealthColor(batteryData.healthPercentage),
                        )

                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Temperature",
                            value = "${String.format("%.1f", batteryData.temperature)}°C",
                            iconColor = getTemperatureColor(batteryData.temperature),
                        )

                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Voltage",
                            value = "${String.format("%.3f", batteryData.voltage)}V",
                            iconColor = MaterialTheme.colorScheme.primary,
                        )

                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Current",
                            value = "${batteryData.current}mA",
                            iconColor = if (batteryData.current > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        )

                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Capacity",
                            value = batteryData.capacity,
                            iconColor = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }

            // Usage Statistics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Usage Statistics",
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            UsageStatItem(
                                label = "Screen On",
                                value = batteryData.screenOnTime,
                                color = MaterialTheme.colorScheme.secondary,
                            )

                            UsageStatItem(
                                label = "Screen Off",
                                value = batteryData.screenOffTime,
                                color = MaterialTheme.colorScheme.secondary,
                            )

                            UsageStatItem(
                                label = "Battery Used",
                                value = batteryData.batteryUsed,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryDetailRow(
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
fun UsageStatItem(
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

private fun DrawScope.drawBatteryHistoryBars(levels: List<Int>) {
    if (levels.isEmpty()) return
    val barCount = levels.size.coerceAtMost(24)
    val barWidth = size.width / (barCount + (barCount - 1) * 0.2f)
    val barSpacing = barWidth * 0.2f
    val maxBarHeight = size.height

    val startIndex = levels.size - barCount
    for (i in 0 until barCount) {
        val x = i * (barWidth + barSpacing)
        val level = levels[startIndex + i].coerceIn(0, 100)
        val barHeight = (level / 100f) * maxBarHeight
        val y = maxBarHeight - barHeight

        val barColor = com.voltai.smartbatteryguardian.utils.getBatteryColor(level)

        drawRect(
            color = barColor,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
        )
    }
}

@Composable
private fun LegendDot(
    color: Color,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier =
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CapsuleLevelChart(
    levels: List<Int>,
    modifier: Modifier = Modifier,
) {
    val barCount = 30 // Show 30 bars for 30-day view

    // Sample data into 30 bars (1 day per bar)
    val chartData =
        remember(levels) {
            if (levels.isNotEmpty()) {
                val segmentSize = (levels.size.toFloat() / barCount).coerceAtLeast(1f).toInt()
                levels.chunked(segmentSize).take(barCount).map { segment ->
                    segment.average().toInt().coerceIn(0, 100)
                }
            } else {
                List(barCount) { 50 }
            }
        }

    // Capture theme-dependent colors before entering DrawScope
    val gridColorTheme = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val w = size.width
        val h = size.height
        val count = chartData.size
        if (count == 0) return@Canvas

        val totalGaps = count - 1
        val gapWidth = w * 0.01f
        val totalGapWidth = totalGaps * gapWidth
        val barWidth = (w - totalGapWidth) / count

        // Grid lines for reference
        val gridColor = gridColorTheme
        for (i in 1..4) {
            val y = h * i / 5f
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(w, y),
                strokeWidth = 1f,
            )
        }

        // Draw bars - no animation as requested
        var x = 0f
        chartData.forEachIndexed { index, level ->
            val vv = level.coerceIn(0, 100)
            val barHeight = (vv / 100f) * h * 0.9f

            // Battery Guru style color coding
            val levelColor =
                when {
                    vv >= 80 -> Color(0xFF4CAF50) // Green - normal/good
                    vv >= 50 -> Color(0xFFFFB300) // Yellow/Amber - low usage
                    vv >= 20 -> Color(0xFFFF6B00) // Orange - moderate
                    else -> Color(0xFFF44336) // Red - heavy usage/critical
                }

            val y = h - barHeight
            drawRoundRect(
                color = levelColor.copy(alpha = if (index >= count - 3) 1f else 0.75f),
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth * 0.25f, barWidth * 0.25f),
            )

            x += barWidth + gapWidth
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeChips(
    selected: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChipsRow(
        options = listOf(0, 1),
        selectedOption = selected,
        onSelectedChange = onSelectedChange,
        labelOf = { idx -> if (idx == 0) "Last 24h" else "Since unplug" },
        modifier = modifier,
    )
}

@Composable
private fun InfoCardMini(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
