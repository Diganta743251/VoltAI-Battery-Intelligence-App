package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.R
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.ui.components.CollapsibleScreen
import com.voltai.smartbatteryguardian.viewmodels.BatteryReadingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureDetailScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val readingsVm: BatteryReadingsViewModel = hiltViewModel()
    val temperatureSeries by readingsVm.temperatureSeries.collectAsState()

    val subtitle =
        stringResource(
            id = R.string.temperature_subtitle_format,
            String.format("%.1f", batteryData.temperature),
            stringResource(id = getTempStatusStringRes(batteryData.temperature)),
        )

    CollapsibleScreen(
        title = stringResource(id = R.string.temperature_detail_title),
        subtitle = subtitle,
        onBackClick = onBackClick,
    ) {
        // Current Temperature Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = getTemperatureCardColor(batteryData.temperature),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = getTemperatureIconColor(batteryData.temperature),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${String.format("%.1f", batteryData.temperature)}°C",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Text(
                        text = stringResource(id = getTempStatusStringRes(batteryData.temperature)),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = getTempDescriptionStringRes(batteryData.temperature)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // Temperature Range Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.temperature_range_24h_title),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        TemperatureRangeItem(
                            label = stringResource(id = R.string.current_short_label),
                            value = "${String.format("%.1f", batteryData.temperature)}°C",
                            color = getTemperatureIconColor(batteryData.temperature),
                        )

                        TemperatureRangeItem(
                            label = stringResource(id = R.string.min_label),
                            value = "${String.format(
                                "%.1f",
                                maxOf(15f, batteryData.temperature - 3f),
                            )}°C",
                            color = MaterialTheme.colorScheme.primary,
                        )

                        TemperatureRangeItem(
                            label = stringResource(id = R.string.max_label),
                            value = "${String.format(
                                "%.1f",
                                minOf(60f, batteryData.temperature + 5f),
                            )}°C",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        // Temperature History Graph
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.temperature_history_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val chartCd = stringResource(id = R.string.temp_history_chart_cd)
                    // Capture theme colors before DrawScope
                    val lineColor = MaterialTheme.colorScheme.secondary
                    val normalLineColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                    val hotLineColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    Canvas(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .semantics { contentDescription = chartCd },
                    ) {
                        if (temperatureSeries.isNotEmpty()) {
                            drawTemperatureHistorySeries(
                                series = temperatureSeries.takeLast(24),
                                lineColor = lineColor,
                                normalLineColor = normalLineColor,
                                hotLineColor = hotLineColor,
                            )
                        } else {
                            drawTemperatureGraph(
                                currentTemp = batteryData.temperature,
                                lineColor = lineColor,
                                normalLineColor = normalLineColor,
                                hotLineColor = hotLineColor,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_24h_ago),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(id = R.string.label_now),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Temperature Analysis
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.temperature_analysis_title),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TemperatureAnalysisRow(
                        icon = Icons.Default.Star,
                        label = stringResource(id = R.string.current_status_label),
                        value = stringResource(id = getTempStatusStringRes(batteryData.temperature)),
                        iconColor = getTemperatureIconColor(batteryData.temperature),
                    )

                    TemperatureAnalysisRow(
                        icon = if (batteryData.temperature > 40) Icons.Default.Warning else Icons.Default.CheckCircle,
                        label = stringResource(id = R.string.safety_level_label),
                        value =
                        if (batteryData.temperature > 40) {
                            stringResource(
                                id = R.string.caution_label,
                            )
                        } else {
                            stringResource(id = R.string.safe_label)
                        },
                        iconColor = if (batteryData.temperature > 40) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                    )

                    // Calculate realistic average based on current temperature
                    val avgTemp =
                        when {
                            batteryData.temperature > 40f -> batteryData.temperature - 2f // If hot, average was cooler
                            batteryData.temperature < 20f -> batteryData.temperature + 2f // If cool, average was warmer
                            else -> batteryData.temperature - 1f // Normal slight variation
                        }
                    TemperatureAnalysisRow(
                        icon = Icons.Default.Star,
                        label = stringResource(id = R.string.average_24h_label),
                        value = "${String.format("%.1f", avgTemp)}°C",
                        iconColor = MaterialTheme.colorScheme.primary,
                    )

                    // Determine trend based on current vs average temperature
                    val trendRes =
                        when {
                            batteryData.temperature > avgTemp + 2f -> R.string.trend_rising
                            batteryData.temperature < avgTemp - 2f -> R.string.trend_cooling
                            else -> R.string.trend_stable
                        }
                    TemperatureAnalysisRow(
                        icon = Icons.Default.Star,
                        label = stringResource(id = R.string.trend_label),
                        value = stringResource(id = trendRes),
                        iconColor =
                        when (trendRes) {
                            R.string.trend_rising -> MaterialTheme.colorScheme.secondary
                            R.string.trend_cooling -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.tertiary
                        },
                    )
                }
            }
        }

        // Temperature Guidelines
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
                        text = stringResource(id = R.string.temperature_guidelines_title),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TemperatureGuidelineItem(
                        range = stringResource(id = R.string.guideline_range_cool),
                        status = stringResource(id = R.string.status_cool),
                        description = stringResource(id = R.string.guideline_desc_cool),
                        color = MaterialTheme.colorScheme.primary,
                    )

                    TemperatureGuidelineItem(
                        range = stringResource(id = R.string.guideline_range_normal),
                        status = stringResource(id = R.string.status_normal),
                        description = stringResource(id = R.string.guideline_desc_normal),
                        color = MaterialTheme.colorScheme.tertiary,
                    )

                    TemperatureGuidelineItem(
                        range = stringResource(id = R.string.guideline_range_warm),
                        status = stringResource(id = R.string.status_warm),
                        description = stringResource(id = R.string.guideline_desc_warm),
                        color = MaterialTheme.colorScheme.secondary,
                    )

                    TemperatureGuidelineItem(
                        range = stringResource(id = R.string.guideline_range_hot),
                        status = stringResource(id = R.string.status_hot),
                        description = stringResource(id = R.string.guideline_desc_hot),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
fun TemperatureRangeItem(
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
fun TemperatureAnalysisRow(
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
fun TemperatureGuidelineItem(
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
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
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
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

private fun DrawScope.drawTemperatureGraph(
    currentTemp: Float,
    lineColor: Color,
    normalLineColor: Color,
    hotLineColor: Color,
) {
    val points = 24
    val path = Path()

    for (i in 0..points) {
        val x = (i.toFloat() / points) * size.width

        // Create realistic temperature pattern based on current temperature
        val temp =
            when {
                i < 6 -> { // Night (00-06): cooler
                    val variation = (6 - i) * 0.8f
                    maxOf(15f, currentTemp - variation)
                }
                i < 12 -> { // Morning (06-12): gradually warming
                    val progress = (i - 6) / 6f
                    currentTemp - 3f + (progress * 2f)
                }
                i < 18 -> { // Afternoon (12-18): warmer, peak usage
                    val progress = (i - 12) / 6f
                    val peakTemp = if (currentTemp > 35f) currentTemp + 2f else currentTemp + 4f
                    currentTemp - 1f + (progress * (peakTemp - currentTemp + 1f))
                }
                else -> { // Evening (18-24): cooling down
                    if (i == points) {
                        currentTemp // End at current temperature
                    } else {
                        val progress = (i - 18) / 6f
                        val eveningTemp = currentTemp - 2f
                        currentTemp + 2f - (progress * 2f)
                    }
                }
            }

        val normalizedY = ((temp - 15f) / 30f).coerceIn(0f, 1f) // Normalize 15-45°C to 0-1
        val y = size.height - (normalizedY * size.height)

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = 3.dp.toPx()),
    )

    // Draw temperature zones
    val normalTemp = size.height - ((35f - 15f) / 30f * size.height)
    val hotTemp = size.height - ((45f - 15f) / 30f * size.height)

    drawLine(
        color = normalLineColor,
        start = androidx.compose.ui.geometry.Offset(0f, normalTemp),
        end = androidx.compose.ui.geometry.Offset(size.width, normalTemp),
        strokeWidth = 1.dp.toPx(),
    )

    drawLine(
        color = hotLineColor,
        start = androidx.compose.ui.geometry.Offset(0f, hotTemp),
        end = androidx.compose.ui.geometry.Offset(size.width, hotTemp),
        strokeWidth = 1.dp.toPx(),
    )
}

private fun DrawScope.drawTemperatureHistorySeries(
    series: List<Float>,
    lineColor: Color,
    normalLineColor: Color,
    hotLineColor: Color,
) {
    if (series.isEmpty()) return
    val path = Path()
    val points = series.size.coerceAtMost(24)
    val start = series.size - points
    for (i in 0 until points) {
        val x = (i.toFloat() / (points - 1)) * size.width
        val temp = series[start + i]
        val normalizedY = ((temp - 15f) / 30f).coerceIn(0f, 1f)
        val y = size.height - (normalizedY * size.height)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = 3.dp.toPx()),
    )

    val normalTemp = size.height - ((35f - 15f) / 30f * size.height)
    val hotTemp = size.height - ((45f - 15f) / 30f * size.height)

    drawLine(
        color = normalLineColor,
        start = androidx.compose.ui.geometry.Offset(0f, normalTemp),
        end = androidx.compose.ui.geometry.Offset(size.width, normalTemp),
        strokeWidth = 1.dp.toPx(),
    )

    drawLine(
        color = hotLineColor,
        start = androidx.compose.ui.geometry.Offset(0f, hotTemp),
        end = androidx.compose.ui.geometry.Offset(size.width, hotTemp),
        strokeWidth = 1.dp.toPx(),
    )
}

@Composable
private fun getTemperatureCardColor(temperature: Float): CardColors {
    return when {
        temperature < 25 -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        temperature < 35 -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        temperature < 45 -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        else -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    }
}

@Composable
private fun getTemperatureIconColor(temperature: Float): Color {
    return when {
        temperature < 25 -> MaterialTheme.colorScheme.primary // Blue (cool)
        temperature < 35 -> MaterialTheme.colorScheme.tertiary // Green (normal)
        temperature < 45 -> MaterialTheme.colorScheme.secondary // Orange (warm)
        else -> MaterialTheme.colorScheme.error // Red (hot)
    }
}

private fun getTemperatureStatus(temperature: Float): String {
    return when {
        temperature < 25 -> "Cool"
        temperature < 35 -> "Normal"
        temperature < 45 -> "Warm"
        else -> "Hot"
    }
}

private fun getTemperatureDescription(temperature: Float): String {
    return when {
        temperature < 25 -> "Battery is running cool - optimal for longevity"
        temperature < 35 -> "Battery temperature is in normal range"
        temperature < 45 -> "Battery is warm - consider reducing usage"
        else -> "Battery is hot - stop charging and let it cool"
    }
}

@androidx.annotation.StringRes
private fun getTempStatusStringRes(temperature: Float): Int {
    return when {
        temperature < 25 -> R.string.status_cool
        temperature < 35 -> R.string.status_normal
        temperature < 45 -> R.string.status_warm
        else -> R.string.status_hot
    }
}

@androidx.annotation.StringRes
private fun getTempDescriptionStringRes(temperature: Float): Int {
    return when {
        temperature < 25 -> R.string.temp_desc_cool
        temperature < 35 -> R.string.temp_desc_normal
        temperature < 45 -> R.string.temp_desc_warm
        else -> R.string.temp_desc_hot
    }
}
