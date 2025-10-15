package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.ui.components.FilterChipsRow
import com.voltai.smartbatteryguardian.viewmodels.BatteryReadingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricCurrentDetailScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val readingsVm: BatteryReadingsViewModel = hiltViewModel()
    val currentSeries by readingsVm.currentSeries.collectAsState()
    var mode by remember { mutableStateOf(CurrentMode.fromCurrent(batteryData.current)) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Electric current",
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
            // Mode chips (Charging / Discharging)
            item {
                ModeChips(
                    selected = mode,
                    onSelect = { mode = it },
                )
            }

            // Time range pill for Charging mode (start -> end)
            item {
                val now = System.currentTimeMillis()
                val start = now - 60_000L
                val fmt = remember { SimpleDateFormat("h:mm a d MMMM", Locale.getDefault()) }
                if (mode == CurrentMode.Charging) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = fmt.format(Date(start)))
                            Text(text = "→")
                            Text(text = fmt.format(Date(now)))
                        }
                    }
                }
            }

            // Big current value and subtext (Power • Voltage)
            item {
                val hasRealtime = batteryData.voltage > 0.0
                val magnitude = abs(batteryData.current)
                val powerWatts = abs(batteryData.current) * batteryData.voltage / 1000.0
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (hasRealtime) "$magnitude mA" else "— mA",
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Text(
                        text =
                        if (hasRealtime) {
                            String.format(
                                Locale.getDefault(),
                                "%.1f W • %.0f mV",
                                powerWatts,
                                batteryData.voltage * 1000,
                            )
                        } else {
                            "— W • — mV"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Line chart with dashed grid and axes labels + range chips below
            item {
                var window by remember { mutableStateOf(CurrentWindow.W_1M) }
                val samples =
                    remember(currentSeries, window) {
                        currentSeries.takeLast(window.samples).map { abs(it) }
                    }
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CurrentLineChart(samples = samples, window = window, mode = mode)
                        Spacer(Modifier.height(12.dp))
                        RangeChipsCurrent(selected = window, onSelectedChange = { window = it })
                    }
                }
            }

            // Bottom stats: Average / Minimum / Maximum
            item {
                val values = currentSeries.takeLast(120).map { abs(it) }
                val hasStats = values.isNotEmpty()
                val avg = if (hasStats) values.average() else 0.0
                val min = if (hasStats) values.minOrNull() ?: 0 else 0
                val max = if (hasStats) values.maxOrNull() ?: 0 else 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatColumn(
                        title = "Average",
                        value = if (hasStats) String.format(Locale.getDefault(), "%d", avg.toInt()) else "—",
                        unit = "mA",
                    )
                    VerticalDivider()
                    StatColumn(title = "Minimum", value = if (hasStats) "$min" else "—", unit = "mA")
                    VerticalDivider()
                    StatColumn(title = "Maximum", value = if (hasStats) "$max" else "—", unit = "mA")
                }
            }
        }
    }
}

// Helper composables and enums
enum class CurrentMode {
    Charging,
    Discharging,
    ;

    companion object {
        fun fromCurrent(current: Int): CurrentMode {
            return if (current > 0) Charging else Discharging
        }
    }
}

enum class CurrentWindow(val samples: Int) {
    W_1M(60),
    W_10M(600),
    W_1H(3600),
    W_6H(21600),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeChips(
    selected: CurrentMode,
    onSelect: (CurrentMode) -> Unit,
) {
    FilterChipsRow(
        options = listOf(CurrentMode.Charging, CurrentMode.Discharging),
        selectedOption = selected,
        onSelectedChange = onSelect,
        labelOf = { mode -> if (mode == CurrentMode.Charging) "Charging" else "Discharging" },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeChipsCurrent(
    selected: CurrentWindow,
    onSelectedChange: (CurrentWindow) -> Unit,
) {
    FilterChipsRow(
        options = listOf(CurrentWindow.W_1M, CurrentWindow.W_10M, CurrentWindow.W_1H, CurrentWindow.W_6H),
        selectedOption = selected,
        onSelectedChange = onSelectedChange,
        labelOf = { w ->
            when (w) {
                CurrentWindow.W_1M -> "1m"
                CurrentWindow.W_10M -> "10m"
                CurrentWindow.W_1H -> "1h"
                CurrentWindow.W_6H -> "6h"
            }
        },
    )
}

@Composable
fun CurrentLineChart(
    samples: List<Int>,
    window: CurrentWindow,
    mode: CurrentMode,
) {
    val maxVal = (samples.maxOrNull() ?: 0).coerceAtLeast(1)
    val step = ((kotlin.math.ceil((maxVal / 4.0) / 100.0) * 100).toInt()).coerceAtLeast(100)
    val top = step * 4
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val lineColor = if (mode == CurrentMode.Charging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f)
    val strokeWidth =
        when (window) {
            CurrentWindow.W_1M -> 3.dp
            CurrentWindow.W_10M -> 2.5.dp
            CurrentWindow.W_1H, CurrentWindow.W_6H -> 2.dp
        }
    val grid =
        when (window) {
            CurrentWindow.W_1M -> 4
            CurrentWindow.W_10M -> 5
            CurrentWindow.W_1H -> 6
            CurrentWindow.W_6H -> 6
        }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val chartHeight = 180.dp
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Y-axis labels
            Column(
                modifier = Modifier.width(60.dp).height(chartHeight),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                listOf(top, top - step, top - 2 * step, top - 3 * step, 0).forEach { v ->
                    Text(
                        text = "$v mA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))

            // Chart canvas with overlay message when no data
            Box(
                modifier =
                Modifier
                    .weight(1f)
                    .height(chartHeight),
            ) {
                Canvas(
                    modifier =
                    Modifier
                        .matchParentSize()
                        .semantics { contentDescription = "Electric current chart" },
                ) {
                    val w = size.width
                    val h = size.height
                    // Horizontal dashed grid
                    repeat(grid + 1) { i ->
                        val y = h - (i / grid.toFloat()) * h
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dash,
                        )
                    }

                    // Smooth line path
                    if (samples.size >= 2) {
                        val points =
                            samples.mapIndexed { i, v ->
                                val x = i / (samples.size - 1).toFloat() * w
                                val y = h - (v / top.toFloat()).coerceIn(0f, 1f) * h
                                Offset(x, y)
                            }
                        val path =
                            Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val midX = (p0.x + p1.x) / 2f
                                    val midY = (p0.y + p1.y) / 2f
                                    quadraticBezierTo(p0.x, p0.y, midX, midY)
                                }
                                lineTo(points.last().x, points.last().y)
                            }
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                        )
                        // Last point dot
                        val last = points.last()
                        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = last)
                    }
                }

                if (samples.size < 2) {
                    Text(
                        text = "Data not available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }

        // X axis labels (seconds/minutes)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labelsFor(window).forEach { lbl ->
                Text(
                    text = lbl,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun labelsFor(window: CurrentWindow): List<String> =
    when (window) {
        CurrentWindow.W_1M -> listOf("60s", "50s", "40s", "30s", "20s", "10s", "0s")
        CurrentWindow.W_10M -> listOf("10m", "8m", "6m", "4m", "2m", "1m", "0m")
        CurrentWindow.W_1H -> listOf("60m", "50m", "40m", "30m", "20m", "10m", "0m")
        CurrentWindow.W_6H -> listOf("6h", "5h", "4h", "3h", "2h", "1h", "0h")
    }

@Composable
fun StatColumn(
    title: String,
    value: String,
    unit: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    height: Dp = 40.dp,
    thickness: Dp = 1.dp,
) {
    Box(
        modifier =
        modifier.size(
            width = thickness,
            height = height,
        ).background(color, RoundedCornerShape(thickness / 2)),
    )
}

@Composable
fun CurrentAnalysisRow(
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

private fun DrawScope.drawCurrentWaveform(
    current: Int,
    waveColor: Color,
    centerLineColor: Color =
        Color.Gray.copy(
            alpha = 0.3f,
        ),
) {
    val path = Path()
    val points = 100
    val amplitude = size.height * 0.3f
    val centerY = size.height / 2f
    val frequency = if (current > 0) 3.0 else 2.0 // Different frequency for charging vs discharging

    for (i in 0..points) {
        val x = (i.toFloat() / points) * size.width
        val normalizedCurrent = (kotlin.math.abs(current) / 2000f).coerceIn(0.1f, 1.0f)
        val y = centerY + (sin(2 * PI * frequency * i / points) * amplitude * normalizedCurrent).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = waveColor,
        style = Stroke(width = 4.dp.toPx()),
    )

    // Draw center line
    drawLine(
        color = centerLineColor,
        start = androidx.compose.ui.geometry.Offset(0f, centerY),
        end = androidx.compose.ui.geometry.Offset(size.width, centerY),
        strokeWidth = 1.dp.toPx(),
    )
}

private fun DrawScope.drawCurrentHistoryFallback(
    currentCurrent: Int,
    seriesColor: Color,
    zeroLineColor: Color,
) {
    val points = 24 // 24 hours
    val path = Path()

    for (i in 0..points) {
        val x = (i.toFloat() / points) * size.width

        // Create realistic current pattern based on actual current state
        val historicalCurrent =
            when {
                i < 6 -> { // Night (00-06): typically charging or standby
                    if (currentCurrent > 0) {
                        // If currently charging, assume charging overnight
                        (currentCurrent * 0.8f + (6 - i) * 100).toInt()
                    } else {
                        // If not charging, minimal drain during night
                        -kotlin.math.abs(currentCurrent * 0.3f).toInt()
                    }
                }
                i < 12 -> { // Morning (06-12): moderate usage
                    val usageFactor = (i - 6) / 6f // Gradually increasing usage
                    -kotlin.math.abs(currentCurrent * (0.5f + usageFactor * 0.3f)).toInt()
                }
                i < 18 -> { // Afternoon (12-18): peak usage
                    val peakFactor = if (i == 15) 1.2f else 1.0f // Peak at 3pm
                    -kotlin.math.abs(currentCurrent * peakFactor).toInt()
                }
                else -> { // Evening (18-24): decreasing usage, possible charging
                    if (i == points) {
                        currentCurrent // End at current value
                    } else {
                        val eveningProgress = (i - 18) / 6f
                        if (currentCurrent > 0) {
                            // If charging now, gradually transition to charging
                            (-kotlin.math.abs(currentCurrent * 0.7f) * (1 - eveningProgress) + currentCurrent * eveningProgress).toInt()
                        } else {
                            // Gradually decreasing usage in evening
                            -kotlin.math.abs(currentCurrent * (1 - eveningProgress * 0.5f)).toInt()
                        }
                    }
                }
            }

        val normalizedY = (historicalCurrent + 1500f) / 3000f // Normalize to 0-1
        val y = size.height - (normalizedY * size.height)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = seriesColor,
        style = Stroke(width = 3.dp.toPx()),
    )

    // Draw zero line
    val zeroY = size.height - (1500f / 3000f * size.height)
    drawLine(
        color = zeroLineColor,
        start = androidx.compose.ui.geometry.Offset(0f, zeroY),
        end = androidx.compose.ui.geometry.Offset(size.width, zeroY),
        strokeWidth = 1.dp.toPx(),
    )
}

private fun DrawScope.drawCurrentHistorySeries(
    series: List<Int>,
    seriesColor: Color,
    zeroLineColor: Color,
) {
    if (series.isEmpty()) return
    val path = Path()
    val points = series.size.coerceAtMost(24)
    val start = series.size - points
    for (i in 0 until points) {
        val x = (i.toFloat() / (points - 1)) * size.width
        val value = series[start + i].toFloat() // mA
        val normalizedY = ((value + 1500f) / 3000f).coerceIn(0f, 1f)
        val y = size.height - (normalizedY * size.height)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    drawPath(
        path = path,
        color = seriesColor,
        style = Stroke(width = 3.dp.toPx()),
    )

    // Zero line at 0 mA
    val zeroY = size.height - (1500f / 3000f * size.height)
    drawLine(
        color = Color.Gray.copy(alpha = 0.5f),
        start = androidx.compose.ui.geometry.Offset(0f, zeroY),
        end = androidx.compose.ui.geometry.Offset(size.width, zeroY),
        strokeWidth = 1.dp.toPx(),
    )
}
