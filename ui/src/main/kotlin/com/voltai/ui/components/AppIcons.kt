package com.voltai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BatteryIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Battery body
        drawRoundRect(
            color = tint,
            topLeft = Offset(width * 0.1f, height * 0.25f),
            size = Size(width * 0.7f, height * 0.5f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Battery tip
        drawRoundRect(
            color = tint,
            topLeft = Offset(width * 0.8f, height * 0.4f),
            size = Size(width * 0.1f, height * 0.2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FlashIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.5f.dp.toPx()
        val width = size.width
        val height = size.height
        
        val path = Path().apply {
            moveTo(width * 0.25f, height * 0.1f)
            lineTo(width * 0.5f, height * 0.9f)
            lineTo(width * 0.35f, height * 0.6f)
            lineTo(width * 0.75f, height * 0.6f)
            lineTo(width * 0.6f, height * 0.3f)
            close()
        }
        
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ThermometerIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Thermometer bulb
        drawCircle(
            color = tint,
            radius = width * 0.15f,
            center = Offset(width * 0.5f, height * 0.8f),
            style = Stroke(width = strokeWidth)
        )
        
        // Thermometer tube
        drawLine(
            color = tint,
            start = Offset(width * 0.5f, height * 0.2f),
            end = Offset(width * 0.5f, height * 0.65f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // Temperature marks
        for (i in 0..3) {
            val y = height * (0.25f + i * 0.1f)
            drawLine(
                color = tint,
                start = Offset(width * 0.4f, y),
                end = Offset(width * 0.6f, y),
                strokeWidth = strokeWidth * 0.7f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun HealthIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.5f.dp.toPx()
        val width = size.width
        val height = size.height
        
        val path = Path().apply {
            moveTo(width * 0.5f, height * 0.1f)
            lineTo(width * 0.2f, height * 0.3f)
            lineTo(width * 0.2f, height * 0.8f)
            lineTo(width * 0.5f, height * 0.9f)
            lineTo(width * 0.8f, height * 0.8f)
            lineTo(width * 0.8f, height * 0.3f)
            lineTo(width * 0.5f, height * 0.1f)
            
            // Shield line
            moveTo(width * 0.5f, height * 0.9f)
            lineTo(width * 0.5f, height * 0.35f)
        }
        
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun GraphIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Axes
        drawLine(
            color = tint,
            start = Offset(width * 0.1f, height * 0.9f),
            end = Offset(width * 0.9f, height * 0.9f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(width * 0.1f, height * 0.9f),
            end = Offset(width * 0.1f, height * 0.1f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // Graph line
        val points = listOf(
            Offset(width * 0.2f, height * 0.8f),
            Offset(width * 0.35f, height * 0.6f),
            Offset(width * 0.5f, height * 0.4f),
            Offset(width * 0.65f, height * 0.3f),
            Offset(width * 0.8f, height * 0.2f)
        )
        
        for (i in 0 until points.size - 1) {
            drawLine(
                color = tint,
                start = points[i],
                end = points[i + 1],
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SettingsIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        val radius = width * 0.15f
        
        // Center circle
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidth)
        )
        
        // Gear teeth
        for (i in 0..7) {
            val angle = i * 45f * Math.PI / 180
            val startRadius = width * 0.25f
            val endRadius = width * 0.35f
            
            val startX = centerX + (startRadius * kotlin.math.cos(angle)).toFloat()
            val startY = centerY + (startRadius * kotlin.math.sin(angle)).toFloat()
            val endX = centerX + (endRadius * kotlin.math.cos(angle)).toFloat()
            val endY = centerY + (endRadius * kotlin.math.sin(angle)).toFloat()
            
            drawLine(
                color = tint,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun PowerIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.5f.dp.toPx()
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        
        // Power button circle
        drawCircle(
            color = tint,
            radius = width * 0.35f,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidth)
        )
        
        // Power line
        drawLine(
            color = tint,
            start = Offset(centerX, centerY - width * 0.2f),
            end = Offset(centerX, centerY + width * 0.1f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun CalendarIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp), onDraw = {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        // Calendar body
        drawRoundRect(
            color = tint,
            topLeft = Offset(width * 0.1f, height * 0.2f),
            size = Size(width * 0.8f, height * 0.7f),
            cornerRadius = CornerRadius(width * 0.1f),
            style = Stroke(width = strokeWidth)
        )
        // Header line
        drawLine(
            color = tint,
            start = Offset(width * 0.1f, height * 0.35f),
            end = Offset(width * 0.9f, height * 0.35f),
            strokeWidth = strokeWidth
        )
        // Top corners (tabs)
        drawLine(
            color = tint,
            start = Offset(width * 0.25f, height * 0.2f),
            end = Offset(width * 0.25f, height * 0.35f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(width * 0.75f, height * 0.2f),
            end = Offset(width * 0.75f, height * 0.35f),
            strokeWidth = strokeWidth
        )
        // Date circle
        drawCircle(
            color = tint,
            radius = width * 0.1f,
            center = Offset(width * 0.5f, height * 0.55f)
        )
    })
}

@Composable
fun ExportIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Arrow up
        val path = Path().apply {
            moveTo(width * 0.5f, height * 0.2f)
            lineTo(width * 0.3f, height * 0.4f)
            moveTo(width * 0.5f, height * 0.2f)
            lineTo(width * 0.7f, height * 0.4f)
            moveTo(width * 0.5f, height * 0.2f)
            lineTo(width * 0.5f, height * 0.7f)
        }
        
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Base line
        drawLine(
            color = tint,
            start = Offset(width * 0.2f, height * 0.8f),
            end = Offset(width * 0.8f, height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun SunIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        
        // Sun center
        drawCircle(
            color = tint,
            radius = width * 0.15f,
            center = Offset(centerX, centerY)
        )
        
        // Sun rays
        for (i in 0 until 8) {
            val angle = i * 45f * kotlin.math.PI / 180
            val innerRadius = width * 0.25f
            val outerRadius = width * 0.4f
            
            val innerX = centerX + (innerRadius * kotlin.math.cos(angle)).toFloat()
            val innerY = centerY + (innerRadius * kotlin.math.sin(angle)).toFloat()
            val outerX = centerX + (outerRadius * kotlin.math.cos(angle)).toFloat()
            val outerY = centerY + (outerRadius * kotlin.math.sin(angle)).toFloat()
            
            drawLine(
                color = tint,
                start = Offset(innerX, innerY),
                end = Offset(outerX, outerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun MoonIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Crescent moon
        drawArc(
            color = tint,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(width * 0.2f, height * 0.2f),
            size = Size(width * 0.6f, width * 0.6f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FlashOnIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val width = size.width
        val height = size.height
        
        val path = Path().apply {
            moveTo(width * 0.25f, height * 0.1f)
            lineTo(width * 0.5f, height * 0.9f)
            lineTo(width * 0.35f, height * 0.6f)
            lineTo(width * 0.75f, height * 0.6f)
            lineTo(width * 0.6f, height * 0.3f)
            close()
        }
        
        drawPath(
            path = path,
            color = tint
        )
    }
}

@Composable
fun AccessTimeIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        
        // Clock circle
        drawCircle(
            color = tint,
            radius = width * 0.4f,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidth)
        )
        
        // Clock hands
        drawLine(
            color = tint,
            start = Offset(centerX, centerY),
            end = Offset(centerX, centerY - width * 0.25f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(centerX, centerY),
            end = Offset(centerX + width * 0.15f, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun MemoryIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Memory chip outline
        drawRoundRect(
            color = tint,
            topLeft = Offset(width * 0.2f, height * 0.15f),
            size = Size(width * 0.6f, height * 0.7f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Memory lines
        for (i in 0..2) {
            val y = height * (0.3f + i * 0.15f)
            drawLine(
                color = tint,
                start = Offset(width * 0.3f, y),
                end = Offset(width * 0.7f, y),
                strokeWidth = strokeWidth * 0.7f,
                cap = StrokeCap.Round
            )
        }
        
        // Side connectors
        for (i in 0..3) {
            val y = height * (0.25f + i * 0.15f)
            drawLine(
                color = tint,
                start = Offset(width * 0.15f, y),
                end = Offset(width * 0.2f, y),
                strokeWidth = strokeWidth * 0.7f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = Offset(width * 0.8f, y),
                end = Offset(width * 0.85f, y),
                strokeWidth = strokeWidth * 0.7f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun LightbulbIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        val centerX = width / 2
        
        // Bulb shape
        drawCircle(
            color = tint,
            radius = width * 0.25f,
            center = Offset(centerX, height * 0.35f),
            style = Stroke(width = strokeWidth)
        )
        
        // Base
        drawRoundRect(
            color = tint,
            topLeft = Offset(width * 0.35f, height * 0.6f),
            size = Size(width * 0.3f, height * 0.15f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Screw lines
        drawLine(
            color = tint,
            start = Offset(width * 0.35f, height * 0.65f),
            end = Offset(width * 0.65f, height * 0.65f),
            strokeWidth = strokeWidth * 0.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(width * 0.35f, height * 0.7f),
            end = Offset(width * 0.65f, height * 0.7f),
            strokeWidth = strokeWidth * 0.5f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun Brightness6Icon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        
        // Center circle
        drawCircle(
            color = tint,
            radius = width * 0.15f,
            center = Offset(centerX, centerY)
        )
        
        // Brightness rays (6 rays)
        for (i in 0 until 6) {
            val angle = i * 60f * kotlin.math.PI / 180
            val innerRadius = width * 0.25f
            val outerRadius = width * 0.4f
            
            val innerX = centerX + (innerRadius * kotlin.math.cos(angle)).toFloat()
            val innerY = centerY + (innerRadius * kotlin.math.sin(angle)).toFloat()
            val outerX = centerX + (outerRadius * kotlin.math.cos(angle)).toFloat()
            val outerY = centerY + (outerRadius * kotlin.math.sin(angle)).toFloat()
            
            drawLine(
                color = tint,
                start = Offset(innerX, innerY),
                end = Offset(outerX, outerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun AnalyticsIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Bar chart
        val barWidth = width * 0.12f
        val bars = listOf(0.4f, 0.7f, 0.5f, 0.8f, 0.6f)
        
        bars.forEachIndexed { index, barHeight ->
            val x = width * (0.15f + index * 0.15f)
            val barTop = height * (0.9f - barHeight * 0.6f)
            
            drawRoundRect(
                color = tint,
                topLeft = Offset(x, barTop),
                size = Size(barWidth, height * 0.9f - barTop),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun GetAppIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Download arrow
        val path = Path().apply {
            moveTo(width * 0.5f, height * 0.2f)
            lineTo(width * 0.5f, height * 0.7f)
            moveTo(width * 0.3f, height * 0.5f)
            lineTo(width * 0.5f, height * 0.7f)
            lineTo(width * 0.7f, height * 0.5f)
        }
        
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Base line
        drawLine(
            color = tint,
            start = Offset(width * 0.2f, height * 0.8f),
            end = Offset(width * 0.8f, height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun SupportIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        // Head
        drawCircle(
            color = tint,
            radius = 6.dp.toPx(),
            center = Offset(12.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        // Body
        drawLine(
            color = tint,
            start = Offset(12.dp.toPx(), 14.dp.toPx()),
            end = Offset(12.dp.toPx(), 20.dp.toPx()),
            strokeWidth = strokeWidth
        )
        // Arms
        drawLine(
            color = tint,
            start = Offset(12.dp.toPx(), 16.dp.toPx()),
            end = Offset(8.dp.toPx(), 19.dp.toPx()),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(12.dp.toPx(), 16.dp.toPx()),
            end = Offset(16.dp.toPx(), 19.dp.toPx()),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun EyeIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Eye outline
        drawOval(
            color = tint,
            topLeft = Offset(width * 0.15f, height * 0.35f),
            size = Size(width * 0.7f, height * 0.3f),
            style = Stroke(width = strokeWidth)
        )
        
        // Eye ball
        drawCircle(
            color = tint,
            radius = width * 0.08f,
            center = Offset(width * 0.5f, height * 0.5f)
        )
    }
}

@Composable
fun RefreshIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        
        // Circular arrow
        drawArc(
            color = tint,
            startAngle = 30f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(width * 0.1f, height * 0.1f),
            size = Size(width * 0.8f, height * 0.8f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Arrow head
        val arrowSize = width * 0.15f
        drawLine(
            color = tint,
            start = Offset(width * 0.75f, height * 0.25f),
            end = Offset(width * 0.75f - arrowSize, height * 0.25f - arrowSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(width * 0.75f, height * 0.25f),
            end = Offset(width * 0.75f + arrowSize, height * 0.25f - arrowSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun ScheduleIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        
        // Clock face
        drawCircle(
            color = tint,
            radius = width * 0.4f,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidth)
        )
        
        // Clock hands
        drawLine(
            color = tint,
            start = Offset(centerX, centerY),
            end = Offset(centerX, centerY - width * 0.2f),
            strokeWidth = strokeWidth * 1.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(centerX, centerY),
            end = Offset(centerX + width * 0.25f, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun BackArrowIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(size.width * 0.7f, size.height * 0.3f),
            end = Offset(size.width * 0.3f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.3f, size.height * 0.5f),
            end = Offset(size.width * 0.7f, size.height * 0.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun AccessibilityIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Canvas(modifier = modifier.size(24.dp), onDraw = {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        val centerX = width / 2
        // Head
        drawCircle(
            color = tint,
            radius = width * 0.15f,
            center = Offset(centerX, height * 0.2f),
            style = Stroke(width = strokeWidth)
        )
        // Body (chair)
        val chairPath = Path().apply {
            moveTo(width * 0.3f, height * 0.35f)
            lineTo(width * 0.7f, height * 0.35f)
            lineTo(width * 0.6f, height * 0.7f)
            lineTo(width * 0.4f, height * 0.7f)
            close()
        }
        drawPath(path = chairPath, color = tint, style = Stroke(width = strokeWidth))
        // Wheel
        drawCircle(
            color = tint,
            radius = width * 0.1f,
            center = Offset(width * 0.5f, height * 0.8f),
            style = Stroke(width = strokeWidth)
        )
        // Armrest
        drawLine(
            color = tint,
            start = Offset(width * 0.2f, height * 0.35f),
            end = Offset(width * 0.3f, height * 0.7f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(width * 0.8f, height * 0.35f),
            end = Offset(width * 0.7f, height * 0.7f),
            strokeWidth = strokeWidth
        )
    })
}

@Composable
fun CheckIcon(
    tint: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier, onDraw = {
        val strokeWidth = size.width * 0.08f
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.5f)
            lineTo(size.width * 0.4f, size.height * 0.7f)
            lineTo(size.width * 0.8f, size.height * 0.3f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    })
}

@Composable
fun ShieldIcon(
    tint: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier, onDraw = {
        val strokeWidth = size.width * 0.06f
        val centerX = size.width / 2f
        
        // Shield outline
        val shieldPath = Path().apply {
            moveTo(centerX, size.height * 0.1f)
            cubicTo(
                size.width * 0.8f, size.height * 0.1f,
                size.width * 0.8f, size.height * 0.4f,
                centerX, size.height * 0.9f
            )
            cubicTo(
                size.width * 0.2f, size.height * 0.4f,
                size.width * 0.2f, size.height * 0.1f,
                centerX, size.height * 0.1f
            )
            close()
        }
        
        drawPath(
            path = shieldPath,
            color = tint,
            style = Stroke(width = strokeWidth)
        )
        
        // Inner checkmark
        val checkPath = Path().apply {
            moveTo(size.width * 0.35f, size.height * 0.5f)
            lineTo(size.width * 0.45f, size.height * 0.6f)
            lineTo(size.width * 0.65f, size.height * 0.4f)
        }
        
        drawPath(
            path = checkPath,
            color = tint,
            style = Stroke(
                width = strokeWidth * 0.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    })
}

// Helper extension functions for math operations
private fun Float.toRadians() = this * kotlin.math.PI.toFloat() / 180f
private fun Float.cos() = kotlin.math.cos(this.toDouble()).toFloat()
private fun Float.sin() = kotlin.math.sin(this.toDouble()).toFloat()