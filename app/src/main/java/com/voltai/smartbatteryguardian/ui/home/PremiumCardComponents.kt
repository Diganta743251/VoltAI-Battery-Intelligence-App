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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.BatteryData

@Composable
fun PremiumBadge(
    modifier: Modifier = Modifier,
    text: String = "PRO",
    color: Color = Color(0xFFFFD700)
) {
    val shimmerEffect by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.9f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = shimmerEffect * 0.3f),
                            Color.Transparent,
                            Color.White.copy(alpha = shimmerEffect * 0.3f)
                        )
                    )
                )
        ) {
            Text(
                text = text,
                fontSize = 8.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun EnhancedBatteryHealthAnalyticsCard(batteryData: BatteryData) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔋 Health Analytics",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PremiumBadge(text = "AI", color = Color(0xFF6366F1))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedHealthRing(
                        healthPercentage = batteryData.healthPercentage,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = "${batteryData.healthPercentage}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = getHealthColor(batteryData.healthPercentage)
                        )
                        Text(
                            text = "Health Score",
                            fontSize = 10.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Degradation",
                    fontSize = 9.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "2.1%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF9800)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Prediction",
                    fontSize = 9.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "8 months",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun EnhancedSmartChargingCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🧠 Smart Charging",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            PremiumBadge(text = "PRO", color = Color(0xFF6A1B9A))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (batteryData.isCharging) {
            ChargingOptimizationIndicator()
        } else {
            ReadyToChargeIndicator()
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Efficiency",
                    fontSize = 9.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "94%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Next Window",
                    fontSize = 9.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "22:00",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun EnhancedRealTimeMonitoringCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Live Monitor",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                LiveIndicator(isActive = true)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mini real-time chart
        MiniRealtimeChart(modifier = Modifier.fillMaxWidth().height(24.dp))
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem("Current", "${batteryData.current} mA", Color(0xFF2196F3))
            MetricItem("Voltage", "${String.format("%.1f", batteryData.voltage)}V", Color(0xFFFF9800))
            MetricItem("Power", "18W", Color(0xFF4CAF50))
        }
    }
}

@Composable
private fun AnimatedHealthRing(
    healthPercentage: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = healthPercentage / 100f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "health"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Background circle
            drawCircle(
                color = Color(0xFFE8F5E8),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc
            drawArc(
                color = getHealthColor(healthPercentage),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun ChargingOptimizationIndicator() {
    val pulseAnimation by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    Color(0xFF4CAF50).copy(alpha = pulseAnimation - 0.8f),
                    CircleShape
                )
        )
        Text(
            text = "Optimizing charge...",
            fontSize = 10.sp,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ReadyToChargeIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color(0xFF2196F3), CircleShape)
        )
        Text(
            text = "Ready for smart charging",
            fontSize = 10.sp,
            color = Color(0xFF2196F3)
        )
    }
}

@Composable
private fun LiveIndicator(isActive: Boolean) {
    val alpha by rememberInfiniteTransition(label = "live").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live"
    )
    
    Surface(
        shape = CircleShape,
        color = if (isActive) Color(0xFF4CAF50).copy(alpha = alpha) else Color(0xFF666666),
        modifier = Modifier.size(8.dp)
    ) {}
}

@Composable
private fun MiniRealtimeChart(modifier: Modifier = Modifier) {
    val animatedPhase by rememberInfiniteTransition(label = "chart").animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chart"
    )
    
    Canvas(modifier = modifier) {
        val path = Path()
        val points = (0..20).map { index ->
            val x = (index * size.width) / 20
            val y = size.height / 2 + 
                   sin((index * 0.5f) + animatedPhase) * size.height * 0.3f
            Pair(x, y.toFloat())
        }
        
        points.forEachIndexed { index, (x, y) ->
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = Color(0xFF2196F3),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 8.sp,
            color = Color(0xFF666666)
        )
    }
}

private fun getHealthColor(healthPercentage: Int): Color {
    return when {
        healthPercentage >= 90 -> Color(0xFF4CAF50)
        healthPercentage >= 75 -> Color(0xFF8BC34A)
        healthPercentage >= 60 -> Color(0xFFFF9800)
        healthPercentage >= 40 -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }
}

private fun sin(x: Float): Float = kotlin.math.sin(x.toDouble()).toFloat()
