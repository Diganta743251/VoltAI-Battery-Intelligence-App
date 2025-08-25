package com.voltai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun LiveCurrentGraph(
    data: List<Float>,
    modifier: Modifier = Modifier,
    isCharging: Boolean = false,
    maxDataPoints: Int = 50
) {
    var animatedData by remember { mutableStateOf(emptyList<Float>()) }
    
    // Animate data points entry
    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            val limitedData = data.takeLast(maxDataPoints)
            animatedData = limitedData
        }
    }
    
    // Advanced animations
    val infiniteTransition = rememberInfiniteTransition(label = "graph_animations")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    // Shimmer effect for real-time data
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    
    // Wave animation for charging
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        if (animatedData.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 20f
        val graphWidth = width - (padding * 2)
        val graphHeight = height - (padding * 2)
        
        // Draw grid lines
        drawGrid(width, height, padding)
        
        // Calculate data bounds
        val maxValue = animatedData.maxOrNull() ?: 1f
        val minValue = animatedData.minOrNull() ?: 0f
        val range = max(maxValue - minValue, 1f)
        
        // Create path for the graph line
        val path = Path()
        val points = mutableListOf<Offset>()
        
        animatedData.forEachIndexed { index, value ->
            val x = padding + (index.toFloat() / (animatedData.size - 1).coerceAtLeast(1)) * graphWidth
            val normalizedValue = (value - minValue) / range
            val y = padding + graphHeight - (normalizedValue * graphHeight)
            
            points.add(Offset(x, y))
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Draw gradient fill under the graph
        if (points.size > 1) {
            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, height - padding)
                lineTo(points.first().x, height - padding)
                close()
            }
            
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    getGraphColor(isCharging).copy(alpha = if (isCharging) pulseAlpha * 0.4f else 0.3f),
                    Color.Transparent
                ),
                startY = padding,
                endY = height - padding
            )
            
            drawPath(
                path = fillPath,
                brush = gradient
            )
        }
        
        // Average line
        val avg = animatedData.average().toFloat()
        val normalizedAvg = (avg - minValue) / range
        val avgY = padding + graphHeight - (normalizedAvg * graphHeight)
        drawLine(
            color = getGraphColor(isCharging).copy(alpha = 0.4f),
            start = Offset(padding, avgY),
            end = Offset(width - padding, avgY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )

        // Draw the main graph line
        drawPath(
            path = path,
            color = getGraphColor(isCharging).copy(alpha = if (isCharging) pulseAlpha else 1f),
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        
        // Draw min/max markers
        val minIdx = animatedData.indexOf(minValue)
        val maxIdx = animatedData.indexOf(maxValue)
        if (minIdx >= 0 && maxIdx >= 0) {
            val minX = padding + (minIdx.toFloat() / (animatedData.size - 1).coerceAtLeast(1)) * graphWidth
            val maxX = padding + (maxIdx.toFloat() / (animatedData.size - 1).coerceAtLeast(1)) * graphWidth
            val minY = padding + graphHeight - (((minValue - minValue) / range) * graphHeight)
            val maxY = padding + graphHeight - (((maxValue - minValue) / range) * graphHeight)
            drawCircle(Color.Red.copy(alpha = 0.6f), radius = 5f, center = Offset(maxX, maxY))
            drawCircle(Color.Cyan.copy(alpha = 0.6f), radius = 5f, center = Offset(minX, minY))
        }
        
        // Draw data points
        points.forEach { point ->
            drawCircle(
                color = getGraphColor(isCharging),
                radius = 3f,
                center = point
            )
        }
        
        // Draw current value indicator (last point)
        if (points.isNotEmpty()) {
            val lastPoint = points.last()
            drawCircle(
                color = getGraphColor(isCharging),
                radius = 6f,
                center = lastPoint,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = getGraphColor(isCharging),
                radius = 3f,
                center = lastPoint
            )
        }
    }
}

private fun DrawScope.drawGrid(width: Float, height: Float, padding: Float) {
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val strokeWidth = 1f
    
    // Horizontal grid lines
    for (i in 0..4) {
        val y = padding + (i * (height - 2 * padding) / 4)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = strokeWidth
        )
    }
    
    // Vertical grid lines
    for (i in 0..6) {
        val x = padding + (i * (width - 2 * padding) / 6)
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, height - padding),
            strokeWidth = strokeWidth
        )
    }
}

private fun getGraphColor(isCharging: Boolean): Color {
    return if (isCharging) {
        Color(0xFF4CAF50) // Green for charging
    } else {
        Color(0xFF2196F3) // Blue for discharging
    }
}

@Composable
fun MiniCurrentGraph(
    data: List<Float>,
    modifier: Modifier = Modifier,
    isCharging: Boolean = false
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        if (data.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 4f
        
        val maxValue = data.maxOrNull() ?: 1f
        val minValue = data.minOrNull() ?: 0f
        val range = max(maxValue - minValue, 1f)
        
        val path = Path()
        
        data.forEachIndexed { index, value ->
            val x = padding + (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * (width - 2 * padding)
            val normalizedValue = (value - minValue) / range
            val y = padding + height - padding - (normalizedValue * (height - 2 * padding))
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = getGraphColor(isCharging),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}