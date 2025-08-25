package com.voltai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun AnimatedBatteryIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    isCharging: Boolean = false,
    batteryLevel: Float = 100f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "battery_animation")
    
    val chargingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "charging_alpha"
    )
    
    val sparkleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_rotation"
    )
    
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val batteryWidth = size.width * 0.7f
        val batteryHeight = size.height * 0.5f
        val batteryX = (size.width - batteryWidth) / 2
        val batteryY = (size.height - batteryHeight) / 2
        
        // Battery outline
        drawRoundRect(
            color = tint,
            topLeft = Offset(batteryX, batteryY),
            size = Size(batteryWidth, batteryHeight),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        
        // Battery terminal
        drawRoundRect(
            color = tint,
            topLeft = Offset(batteryX + batteryWidth, batteryY + batteryHeight * 0.3f),
            size = Size(3.dp.toPx(), batteryHeight * 0.4f),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
        
        // Battery fill based on level
        val fillWidth = (batteryWidth - 4.dp.toPx()) * (batteryLevel / 100f)
        if (fillWidth > 0) {
            val fillColor = when {
                batteryLevel > 60 -> Color(0xFF4CAF50)
                batteryLevel > 20 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
            
            drawRoundRect(
                color = if (isCharging) fillColor.copy(alpha = chargingAlpha) else fillColor,
                topLeft = Offset(batteryX + 2.dp.toPx(), batteryY + 2.dp.toPx()),
                size = Size(fillWidth, batteryHeight - 4.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx())
            )
        }
        
        // Charging lightning bolt
        if (isCharging) {
            rotate(sparkleRotation, pivot = center) {
                val lightningPath = Path().apply {
                    moveTo(center.x - 3.dp.toPx(), center.y - 6.dp.toPx())
                    lineTo(center.x + 2.dp.toPx(), center.y - 1.dp.toPx())
                    lineTo(center.x - 1.dp.toPx(), center.y - 1.dp.toPx())
                    lineTo(center.x + 3.dp.toPx(), center.y + 6.dp.toPx())
                    lineTo(center.x - 2.dp.toPx(), center.y + 1.dp.toPx())
                    lineTo(center.x + 1.dp.toPx(), center.y + 1.dp.toPx())
                    close()
                }
                
                drawPath(
                    path = lightningPath,
                    color = Color(0xFFFFEB3B).copy(alpha = chargingAlpha),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun AnimatedThermometerIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    temperature: Float = 25f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "thermometer_animation")
    
    val mercuryLevel by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = temperature / 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mercury_level"
    )
    
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val thermometerWidth = 4.dp.toPx()
        val thermometerHeight = size.height * 0.7f
        val bulbRadius = 6.dp.toPx()
        
        val centerX = size.width / 2
        val topY = size.height * 0.1f
        val bottomY = size.height * 0.8f
        
        // Thermometer tube
        drawLine(
            color = tint,
            start = Offset(centerX, topY),
            end = Offset(centerX, bottomY),
            strokeWidth = thermometerWidth,
            cap = StrokeCap.Round
        )
        
        // Thermometer bulb
        drawCircle(
            color = tint,
            radius = bulbRadius,
            center = Offset(centerX, bottomY + bulbRadius)
        )
        
        // Mercury fill
        val mercuryHeight = thermometerHeight * mercuryLevel
        val mercuryColor = when {
            temperature > 40 -> Color(0xFFF44336)
            temperature > 30 -> Color(0xFFFF9800)
            else -> Color(0xFF2196F3)
        }
        
        drawLine(
            color = mercuryColor,
            start = Offset(centerX, bottomY),
            end = Offset(centerX, bottomY - mercuryHeight),
            strokeWidth = thermometerWidth * 0.6f,
            cap = StrokeCap.Round
        )
        
        // Temperature markings
        for (i in 1..4) {
            val markY = topY + (thermometerHeight / 5) * i
            drawLine(
                color = tint.copy(alpha = 0.6f),
                start = Offset(centerX + thermometerWidth, markY),
                end = Offset(centerX + thermometerWidth + 3.dp.toPx(), markY),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun AnimatedHealthIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    healthPercentage: Float = 100f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "health_animation")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val shieldPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            lineTo(size.width * 0.2f, size.height * 0.3f)
            lineTo(size.width * 0.2f, size.height * 0.6f)
            quadraticBezierTo(
                size.width * 0.2f, size.height * 0.9f,
                size.width * 0.5f, size.height * 0.9f
            )
            quadraticBezierTo(
                size.width * 0.8f, size.height * 0.9f,
                size.width * 0.8f, size.height * 0.6f
            )
            lineTo(size.width * 0.8f, size.height * 0.3f)
            close()
        }
        
        // Shield outline
        drawPath(
            path = shieldPath,
            color = tint,
            style = Stroke(width = strokeWidth)
        )
        
        // Health indicator
        val healthColor = when {
            healthPercentage > 80 -> Color(0xFF4CAF50)
            healthPercentage > 50 -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }
        
        // Animated health fill
        val fillHeight = (size.height * 0.6f) * (healthPercentage / 100f)
        drawRect(
            color = healthColor.copy(alpha = 0.3f),
            topLeft = Offset(size.width * 0.25f, size.height * 0.7f - fillHeight),
            size = Size(size.width * 0.5f, fillHeight)
        )
        
        // Pulse effect for good health
        if (healthPercentage > 80) {
            drawPath(
                path = shieldPath,
                color = healthColor.copy(alpha = 0.2f),
                style = Stroke(width = strokeWidth * pulseScale)
            )
        }
        
        // Cross symbol
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.35f),
            end = Offset(size.width * 0.5f, size.height * 0.55f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.4f, size.height * 0.45f),
            end = Offset(size.width * 0.6f, size.height * 0.45f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun AnimatedGraphIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    isAnimating: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "graph_animation")
    
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "graph_progress"
    )
    
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        
        // Axes
        drawLine(
            color = tint,
            start = Offset(size.width * 0.1f, size.height * 0.9f),
            end = Offset(size.width * 0.9f, size.height * 0.9f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.1f, size.height * 0.9f),
            end = Offset(size.width * 0.1f, size.height * 0.1f),
            strokeWidth = strokeWidth
        )
        
        // Data points
        val dataPoints = listOf(0.8f, 0.6f, 0.7f, 0.4f, 0.5f, 0.3f)
        val pointSpacing = (size.width * 0.7f) / (dataPoints.size - 1)
        
        for (i in 0 until dataPoints.size - 1) {
            val progress = if (isAnimating) {
                ((animationProgress * dataPoints.size) - i).coerceIn(0f, 1f)
            } else 1f
            
            if (progress > 0f) {
                val startX = size.width * 0.1f + i * pointSpacing
                val startY = size.height * 0.9f - (dataPoints[i] * size.height * 0.7f)
                val endX = size.width * 0.1f + (i + 1) * pointSpacing
                val endY = size.height * 0.9f - (dataPoints[i + 1] * size.height * 0.7f)
                
                val currentEndX = startX + (endX - startX) * progress
                val currentEndY = startY + (endY - startY) * progress
                
                drawLine(
                    color = tint,
                    start = Offset(startX, startY),
                    end = Offset(currentEndX, currentEndY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                
                // Data point circles
                drawCircle(
                    color = tint,
                    radius = 2.dp.toPx(),
                    center = Offset(startX, startY)
                )
                
                if (progress >= 1f && i == dataPoints.size - 2) {
                    drawCircle(
                        color = tint,
                        radius = 2.dp.toPx(),
                        center = Offset(endX, endY)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedAppUsageIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    usageLevel: Float = 0.5f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "app_usage_animation")
    
    val barAnimations = (0..3).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000 + index * 200,
                    easing = EaseInOutCubic
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }
    
    Canvas(modifier = modifier.size(24.dp)) {
        val barWidth = size.width * 0.15f
        val barSpacing = size.width * 0.05f
        val maxBarHeight = size.height * 0.7f
        
        barAnimations.forEachIndexed { index, animatedValue ->
            val barHeight = maxBarHeight * animatedValue.value * (0.3f + index * 0.2f)
            val barX = size.width * 0.1f + index * (barWidth + barSpacing)
            val barY = size.height * 0.9f - barHeight
            
            val barColor = when (index) {
                0 -> Color(0xFF4CAF50)
                1 -> Color(0xFF2196F3)
                2 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
            
            drawRoundRect(
                color = barColor.copy(alpha = 0.8f),
                topLeft = Offset(barX, barY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }
        
        // Usage indicator line
        val indicatorY = size.height * 0.9f - (maxBarHeight * usageLevel)
        drawLine(
            color = tint.copy(alpha = 0.6f),
            start = Offset(size.width * 0.05f, indicatorY),
            end = Offset(size.width * 0.95f, indicatorY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }
}

@Composable
fun AnimatedPowerSaverIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    isActive: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "power_saver_animation")
    
    val leafRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leaf_rotation"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        
        // Leaf shape
        rotate(leafRotation, pivot = center) {
            val leafPath = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.1f)
                quadraticBezierTo(
                    size.width * 0.8f, size.height * 0.3f,
                    size.width * 0.7f, size.height * 0.6f
                )
                quadraticBezierTo(
                    size.width * 0.5f, size.height * 0.9f,
                    size.width * 0.3f, size.height * 0.6f
                )
                quadraticBezierTo(
                    size.width * 0.2f, size.height * 0.3f,
                    size.width * 0.5f, size.height * 0.1f
                )
            }
            
            // Leaf fill with glow effect if active
            if (isActive) {
                drawPath(
                    path = leafPath,
                    color = Color(0xFF4CAF50).copy(alpha = glowAlpha)
                )
            }
            
            // Leaf outline
            drawPath(
                path = leafPath,
                color = tint,
                style = Stroke(width = strokeWidth)
            )
            
            // Leaf vein
            drawLine(
                color = tint,
                start = Offset(size.width * 0.5f, size.height * 0.2f),
                end = Offset(size.width * 0.5f, size.height * 0.7f),
                strokeWidth = strokeWidth * 0.5f
            )
            
            // Side veins
            for (i in 1..3) {
                val veinY = size.height * (0.2f + i * 0.15f)
                val veinLength = 4.dp.toPx() * (1f - i * 0.2f)
                
                drawLine(
                    color = tint.copy(alpha = 0.7f),
                    start = Offset(size.width * 0.5f, veinY),
                    end = Offset(size.width * 0.5f + veinLength, veinY - veinLength * 0.5f),
                    strokeWidth = strokeWidth * 0.3f
                )
                
                drawLine(
                    color = tint.copy(alpha = 0.7f),
                    start = Offset(size.width * 0.5f, veinY),
                    end = Offset(size.width * 0.5f - veinLength, veinY - veinLength * 0.5f),
                    strokeWidth = strokeWidth * 0.3f
                )
            }
        }
    }
}