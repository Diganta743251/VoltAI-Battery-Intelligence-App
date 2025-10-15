package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Battery Guru-style color-coded battery level bar chart
 * Shows battery level history with color-coded bars (green/yellow/red)
 */
@Composable
fun BatteryLevelBarChart(
    levels: List<Int>,
    modifier: Modifier = Modifier,
    maxBars: Int = 30,
) {
    val displayLevels =
        remember(levels) {
            levels.takeLast(maxBars)
        }

    if (displayLevels.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No data yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    //  Enhanced Battery Guru style animation
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "bar_chart_anim",
    )

    Canvas(
        modifier =
        modifier
            .fillMaxWidth()
            .height(80.dp),
    ) {
        val barCount = displayLevels.size
        val spacing = size.width * 0.02f
        val barWidth = (size.width - spacing * (barCount + 1)) / barCount
        val maxHeight = size.height * 0.9f

        displayLevels.forEachIndexed { index, level ->
            val barHeight = (level / 100f) * maxHeight * animProgress
            val x = spacing + index * (barWidth + spacing)
            val y = size.height - barHeight

            // Color based on battery level (Battery Guru style)
            val barColor =
                when {
                    level >= 70 -> Color(0xFF4CAF50) // Green
                    level >= 40 -> Color(0xFFFFC107) // Yellow/Amber
                    level >= 20 -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFFF44336) // Red
                }

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth * 0.25f, barWidth * 0.25f),
            )
        }
    }
}

/**
 * Battery Guru-style current draw waveform chart
 * Shows real-time current fluctuations
 */
@Composable
fun CurrentDrawWaveform(
    currentValues: List<Int>,
    modifier: Modifier = Modifier,
    showMinMax: Boolean = true,
) {
    val displayValues =
        remember(currentValues) {
            currentValues.takeLast(48)
        }

    if (displayValues.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No data yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Enhanced Battery Guru style animation
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "waveform_anim",
    )

    Column(modifier = modifier) {
        if (showMinMax && displayValues.isNotEmpty()) {
            val minVal = displayValues.minOrNull() ?: 0
            val maxVal = displayValues.maxOrNull() ?: 0
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Min: ${kotlin.math.abs(minVal)} mA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
                Text(
                    text = "Max: ${kotlin.math.abs(maxVal)} mA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
        }

        Canvas(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp),
        ) {
            val points = displayValues
            if (points.isEmpty()) return@Canvas

            val minVal = points.minOrNull() ?: 0
            val maxVal = points.maxOrNull() ?: 0
            val range = (maxVal - minVal).takeIf { it != 0 } ?: 1

            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
            val baseY = size.height * 0.5f

            // Draw baseline
            drawLine(
                color = surfaceVariant,
                start = Offset(0f, baseY),
                end = Offset(size.width * animProgress, baseY),
                strokeWidth = 1.dp.toPx(),
            )

            // Draw waveform
            val path = Path()
            points.forEachIndexed { i, v ->
                if (i >= points.size * animProgress) return@forEachIndexed

                val x = i * stepX
                val normalizedValue = (v - minVal).toFloat() / range.toFloat()
                val y = baseY - (normalizedValue - 0.5f) * size.height * 0.8f

                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

/**
 * Battery Guru-style charging/discharging sessions bar chart
 * Shows charge and discharge cycles over time
 */
@Composable
fun ChargingSessionsChart(
    sessions: List<ChargingSession>,
    modifier: Modifier = Modifier,
) {
    if (sessions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No sessions yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val displaySessions =
        remember(sessions) {
            sessions.takeLast(10)
        }

    val chargeColor = Color(0xFF4CAF50) // Green
    val dischargeColor = MaterialTheme.colorScheme.error

    // Enhanced Battery Guru style animation
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "sessions_anim",
    )

    Canvas(
        modifier =
        modifier
            .fillMaxWidth()
            .height(100.dp),
    ) {
        val barCount = displaySessions.size
        val spacing = size.width * 0.05f
        val barWidth = (size.width - spacing * (barCount + 1)) / barCount
        val maxDuration = displaySessions.maxOfOrNull { it.durationMinutes } ?: 1

        displaySessions.forEachIndexed { index, session ->
            val barHeight = (session.durationMinutes / maxDuration.toFloat()) * size.height * 0.9f * animProgress
            val x = spacing + index * (barWidth + spacing)
            val y = size.height - barHeight

            val barColor = if (session.isCharging) chargeColor else dischargeColor

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth * 0.2f, barWidth * 0.2f),
            )
        }
    }
}

/**
 * Data class for charging sessions
 */
data class ChargingSession(
    val isCharging: Boolean,
    val durationMinutes: Int,
    val startLevel: Int,
    val endLevel: Int,
    val timestamp: Long,
)
