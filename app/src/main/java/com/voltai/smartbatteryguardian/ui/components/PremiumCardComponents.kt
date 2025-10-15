package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun BatteryHealthAnalyticsCard() {
    PremiumCardBase(
        title = "Health Analytics",
        gradient =
        Brush.linearGradient(
            colors =
            listOf(
                Color(0xFF4CAF50),
                Color(0xFF81C784),
            ),
        ),
        icon = "🧠",
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Animated Health Ring
            Box(
                modifier =
                Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                AnimatedHealthRing(healthPercentage = 0f)
            }

            Text(
                text = "—",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricChip("—", "🔄")
                MetricChip("—", "📉")
            }
        }
    }
}

@Composable
fun SmartChargingCard() {
    PremiumCardBase(
        title = "Smart Charging",
        gradient =
        Brush.linearGradient(
            colors =
            listOf(
                Color(0xFF2196F3),
                Color(0xFF64B5F6),
            ),
        ),
        icon = "⚡",
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Charging Optimization Indicator (neutral)
            ChargingOptimizationIndicator(isOptimizing = false)

            Text(
                text = "—",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricChip("—", "🌡️")
                MetricChip("—", "💰")
            }
        }
    }
}

@Composable
fun BatteryDiagnosticsCard() {
    PremiumCardBase(
        title = "Diagnostics",
        gradient =
        Brush.linearGradient(
            colors =
            listOf(
                Color(0xFFFF9800),
                Color(0xFFFFB74D),
            ),
        ),
        icon = "🔧",
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Diagnostic Status (neutral)
            DiagnosticStatusIndicator(status = "")

            Text(
                text = "—",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricChip("—", "✅")
                MetricChip("—", "🎯")
            }
        }
    }
}

@Composable
fun RealTimeMonitoringCard() {
    PremiumCardBase(
        title = "Live Monitor",
        gradient =
        Brush.linearGradient(
            colors =
            listOf(
                Color(0xFF9C27B0),
                Color(0xFFBA68C8),
            ),
        ),
        icon = "📊",
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Live Metrics Chart
            LiveMetricsChart()

            Text(
                text = "—",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricChip("—", "🔴")
                MetricChip("—", "⚡")
            }
        }
    }
}

@Composable
fun BatteryOptimizationCard() {
    PremiumCardBase(
        title = "Optimization",
        gradient =
        Brush.linearGradient(
            colors =
            listOf(
                Color(0xFF673AB7),
                Color(0xFF9575CD),
            ),
        ),
        icon = "🎯",
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Optimization Progress (neutral)
            OptimizationProgress(progress = 0f)

            Text(
                text = "—",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricChip("—", "🤖")
                MetricChip("—", "📈")
            }
        }
    }
}

@Composable
private fun PremiumCardBase(
    title: String,
    gradient: Brush,
    icon: String,
    content: @Composable () -> Unit,
) {
    // Replace infinite shimmer with static premium indicator - battery efficient
    val premiumAlpha = 0.1f // Static alpha for premium overlay

    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .background(gradient),
        ) {
            // Premium Badge
            Box(
                modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(24.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "💎",
                    fontSize = 12.sp,
                )
            }

            // Static premium overlay - no battery drain
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = premiumAlpha),
                    ),
            )

            // Title
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
            )

            // Icon
            Text(
                text = icon,
                fontSize = 32.sp,
                modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
            )

            // Content
            Box(
                modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(top = 20.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun AnimatedHealthRing(healthPercentage: Float) {
    // Simple Material 3 progress indicator - battery efficient
    val animatedProgress by animateFloatAsState(
        targetValue = healthPercentage / 100f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "health_ring",
    )

    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Use standard Material 3 CircularProgressIndicator
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.size(80.dp),
            color = Color.White,
            strokeWidth = 8.dp,
        )

        // Health percentage text
        Text(
            text = "${(healthPercentage).toInt()}%",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ChargingOptimizationIndicator(isOptimizing: Boolean) {
    // Replace infinite rotation with static indicator - respects reduced motion preference
    val isReducedMotionPreferred = com.voltai.smartbatteryguardian.ui.theme.isReducedMotionPreferred()
    val rotation by animateFloatAsState(
        targetValue = if (isOptimizing && !isReducedMotionPreferred) 45f else 0f,
        animationSpec = tween(300),
        label = "rotation",
    )

    // Simple Material 3 approach - no custom Canvas
    Box(
        modifier =
        Modifier
            .size(60.dp)
            .rotate(rotation)
            .background(
                Color.White.copy(alpha = 0.15f),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isOptimizing) "⚡" else "⏸️",
            fontSize = 24.sp,
            color = Color.White,
        )
    }
}

@Composable
private fun DiagnosticStatusIndicator(status: String) {
    // Replace infinite pulse with accessibility-aware scaling
    val isReducedMotionPreferred = com.voltai.smartbatteryguardian.ui.theme.isReducedMotionPreferred()
    val pulse by animateFloatAsState(
        targetValue = if (status == "Active" && !isReducedMotionPreferred) 1.1f else 1.0f,
        animationSpec = tween(200),
        label = "pulse",
    )

    Box(
        modifier =
        Modifier
            .size(60.dp)
            .scale(pulse),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
            Modifier
                .size(50.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✅",
                fontSize = 24.sp,
            )
        }
    }
}

@Composable
private fun LiveMetricsChart() {
    // Replace infinite animation with static chart - much more battery efficient
    val animatedHeight = 0.6f // Static height for chart bars

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(5) { index ->
            Box(
                modifier =
                Modifier
                    .width(8.dp)
                    .height((20 + index * 5 + animatedHeight * 20).dp)
                    .background(
                        Color.White.copy(alpha = 0.7f),
                        RoundedCornerShape(4.dp),
                    ),
            )
        }
    }
}

@Composable
private fun OptimizationProgress(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "optimization_progress",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
            Modifier
                .size(60.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

                // Background
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = radius,
                    style = Stroke(strokeWidth),
                )

                // Progress
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    topLeft =
                    androidx.compose.ui.geometry.Offset(
                        center.x - radius,
                        center.y - radius,
                    ),
                )
            }

            Text(
                text = "🎯",
                fontSize = 20.sp,
            )
        }
    }
}

@Composable
private fun MetricChip(
    text: String,
    icon: String,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = icon,
                fontSize = 12.sp,
            )
            Text(
                text = text,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
