package com.voltai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.*

@Composable
fun AnimatedBatteryRing(
    batteryLevel: Float,
    isCharging: Boolean = false,
    modifier: Modifier = Modifier,
    size: Int = 120,
    strokeWidth: Float = 12f
) {
    // Animation for battery level
    val animatedLevel by animateFloatAsState(
        targetValue = batteryLevel,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "battery_level"
    )
    
    // Charging glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "charging_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    // Pulse animation for charging
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Charging rotation animation
    val chargingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "charging_rotation"
    )
    
    // Particle animation
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_offset"
    )
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = if (isCharging) pulseScale else 1f
                    scaleY = if (isCharging) pulseScale else 1f
                }
        ) {
            val center = this.center
            val radius = (this.size.minDimension / 2) - strokeWidth
            
            // Background ring
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Battery level arc
            val sweepAngle = 360f * (animatedLevel / 100f)
            val batteryColor = getBatteryColor(animatedLevel, isCharging)
            
            // Gradient for battery ring
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    batteryColor.copy(alpha = 0.3f),
                    batteryColor,
                    batteryColor.copy(alpha = 0.8f)
                ),
                center = center
            )
            
            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Charging glow effect
            if (isCharging) {
                // Outer glow ring with pulsing effect
                drawCircle(
                    color = Color(0xFF4CAF50).copy(alpha = glowAlpha * 0.4f),
                    radius = radius + strokeWidth * 0.8f,
                    center = center,
                    style = Stroke(width = strokeWidth * 0.2f)
                )
                
                // Inner glow ring
                drawCircle(
                    color = Color(0xFF4CAF50).copy(alpha = glowAlpha * 0.2f),
                    radius = radius - strokeWidth * 0.3f,
                    center = center,
                    style = Stroke(width = strokeWidth * 0.1f)
                )
                
                // Rotating energy particles
                rotate(chargingRotation, pivot = center) {
                    for (i in 0..11) {
                        val angle = i * 30f * PI / 180f
                        val particleRadius = radius + strokeWidth * 0.6f
                        val particleX = center.x + cos(angle).toFloat() * particleRadius
                        val particleY = center.y + sin(angle).toFloat() * particleRadius
                        
                        // Particle trail effect
                        val trailAlpha = (glowAlpha * 0.8f) * (1f - (i % 4) * 0.2f)
                        drawCircle(
                            color = Color(0xFFFFEB3B).copy(alpha = trailAlpha),
                            radius = (2 + sin(particleOffset * PI * 2 + i).toFloat()).dp.toPx(),
                            center = Offset(particleX, particleY)
                        )
                    }
                }
                
                // Lightning bolt in center
                val lightningPath = Path().apply {
                    val centerX = center.x
                    val centerY = center.y
                    val scale = 0.3f
                    
                    moveTo(centerX - 8.dp.toPx() * scale, centerY - 12.dp.toPx() * scale)
                    lineTo(centerX + 4.dp.toPx() * scale, centerY - 2.dp.toPx() * scale)
                    lineTo(centerX - 2.dp.toPx() * scale, centerY - 2.dp.toPx() * scale)
                    lineTo(centerX + 8.dp.toPx() * scale, centerY + 12.dp.toPx() * scale)
                    lineTo(centerX - 4.dp.toPx() * scale, centerY + 2.dp.toPx() * scale)
                    lineTo(centerX + 2.dp.toPx() * scale, centerY + 2.dp.toPx() * scale)
                    close()
                }
                
                drawPath(
                    path = lightningPath,
                    color = Color(0xFFFFEB3B).copy(alpha = glowAlpha),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                
                drawPath(
                    path = lightningPath,
                    color = Color(0xFFFFEB3B).copy(alpha = glowAlpha * 0.3f)
                )
            }
            
            // Battery level indicator dot
            if (animatedLevel > 0) {
                val angle = (-90f + sweepAngle) * Math.PI / 180
                val dotX = center.x + (radius * cos(angle)).toFloat()
                val dotY = center.y + (radius * sin(angle)).toFloat()
                
                drawCircle(
                    color = batteryColor,
                    radius = strokeWidth / 3,
                    center = Offset(dotX, dotY)
                )
            }
        }
        
        // Battery percentage text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${animatedLevel.toInt()}%",
                fontSize = (size / 5).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (isCharging) {
                Text(
                    text = "Charging",
                    fontSize = (size / 10).sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun getBatteryColor(level: Float, isCharging: Boolean): Color {
    return when {
        isCharging -> Color(0xFF4CAF50) // Green for charging
        level > 50 -> Color(0xFF4CAF50) // Green for good level
        level > 20 -> Color(0xFFFF9800) // Orange for medium level
        else -> Color(0xFFF44336) // Red for low level
    }
}

@Composable
fun CompactBatteryRing(
    batteryLevel: Float,
    isCharging: Boolean = false,
    modifier: Modifier = Modifier,
    size: Int = 60
) {
    val animatedLevel by animateFloatAsState(
        targetValue = batteryLevel,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "compact_battery_level"
    )
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val radius = (this.size.minDimension / 2) - 6f
            val strokeWidth = 6f
            
            // Background ring
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Battery level arc
            val sweepAngle = 360f * (animatedLevel / 100f)
            val batteryColor = getBatteryColor(animatedLevel, isCharging)
            
            drawArc(
                color = batteryColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Text(
            text = "${animatedLevel.toInt()}%",
            fontSize = (size / 6).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}