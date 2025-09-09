package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.rememberBatteryData
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryHealthScreen() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Battery Health Analysis",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Comprehensive health monitoring",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Overall Health Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = getHealthCardColor(batteryData.healthPercentage).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Overall Health Score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(150.dp)
                ) {
                    HealthScoreRing(batteryData.healthPercentage)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${batteryData.healthPercentage}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = getHealthCardColor(batteryData.healthPercentage)
                        )
                        Text(
                            text = getHealthStatus(batteryData.healthPercentage),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = getHealthAdvice(batteryData.healthPercentage),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Health Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthMetricCard(
                title = "Charge Cycles",
                value = "${Random.nextInt(200, 800)}",
                subtitle = "Est. cycles used",
                icon = "🔄",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )

            HealthMetricCard(
                title = "Capacity",
                value = "${Random.nextInt(2800, 4200)} mAh",
                subtitle = "Current capacity",
                icon = "⚡",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthMetricCard(
                title = "Temperature",
                value = "${batteryData.temperature}°C",
                subtitle = getTempHealthStatus(batteryData.temperature),
                icon = "🌡️",
                color = getTempHealthColor(batteryData.temperature),
                modifier = Modifier.weight(1f)
            )

            HealthMetricCard(
                title = "Voltage",
                value = "${batteryData.voltage}V",
                subtitle = getVoltageStatus(batteryData.voltage.toFloat()),
                icon = "⚡",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Charging History Analysis
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🔋 Charging Analysis",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                ChargingAnalysisItem(
                    title = "Optimal Charging Window",
                    subtitle = "20% - 80% charging maintains battery health",
                    progress = 0.75f,
                    color = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ChargingAnalysisItem(
                    title = "Deep Discharge Avoidance",
                    subtitle = "Avoiding drops below 20% extends lifespan",
                    progress = 0.85f,
                    color = Color(0xFF2196F3)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ChargingAnalysisItem(
                    title = "Heat Management",
                    subtitle = "Keeping temperature under 35°C is ideal",
                    progress = getTempHealthProgress(batteryData.temperature),
                    color = getTempHealthColor(batteryData.temperature)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Battery Degradation Timeline
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "📈 Health Timeline",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                HealthTimelineItem(
                    period = "Last 30 days",
                    change = "-2%",
                    description = "Normal degradation rate",
                    isPositive = false
                )

                HealthTimelineItem(
                    period = "Last 90 days",
                    change = "-5%",
                    description = "Within expected range",
                    isPositive = false
                )

                HealthTimelineItem(
                    period = "Since purchase",
                    change = "-${100 - batteryData.healthPercentage}%",
                    description = getDegradationAssessment(batteryData.healthPercentage),
                    isPositive = false
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Health Recommendations
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "💡 Health Optimization Tips",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                val tips = getHealthTips(batteryData)
                tips.forEach { tip ->
                    HealthTipItem(tip)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Implement auto optimization */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Health Optimizations")
                }
            }
        }
    }
}

@Composable
fun HealthScoreRing(healthPercentage: Int) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val strokeWidth = 12.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Background ring
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(strokeWidth)
        )

        // Health ring
        val healthColor = getHealthCardColor(healthPercentage)
        val sweepAngle = (healthPercentage / 100f) * 360f

        drawArc(
            color = healthColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(
                size.width - strokeWidth,
                size.height - strokeWidth
            )
        )
    }
}

@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChargingAnalysisItem(
    title: String,
    subtitle: String,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HealthTimelineItem(
    period: String,
    change: String,
    description: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF5722),
            modifier = Modifier.size(8.dp)
        ) {}

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = period,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = change,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF5722)
        )
    }

    if (period != "Since purchase") {
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun HealthTipItem(tip: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = tip,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun getHealthCardColor(health: Int): Color {
    return when {
        health >= 85 -> Color(0xFF4CAF50)
        health >= 70 -> Color(0xFFFF9800)
        health >= 50 -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }
}

private fun getHealthStatus(health: Int): String {
    return when {
        health >= 90 -> "Excellent"
        health >= 80 -> "Very Good"
        health >= 70 -> "Good"
        health >= 60 -> "Fair"
        health >= 50 -> "Poor"
        else -> "Critical"
    }
}

private fun getHealthAdvice(health: Int): String {
    return when {
        health >= 85 -> "Your battery is in excellent condition! Continue your current charging habits to maintain optimal health."
        health >= 70 -> "Good battery health. Consider avoiding overnight charging and extreme temperatures to prevent further degradation."
        health >= 50 -> "Battery health is declining. Avoid deep discharges and consider replacing the battery within 6-12 months."
        else -> "Critical battery health. Immediate battery replacement recommended for optimal device performance."
    }
}

private fun getTempHealthStatus(temp: Float): String {
    return when {
        temp < 20f -> "Cool"
        temp < 30f -> "Optimal"
        temp < 35f -> "Normal"
        temp < 40f -> "Warm"
        else -> "Hot"
    }
}

private fun getTempHealthColor(temp: Float): Color {
    return when {
        temp < 30f -> Color(0xFF4CAF50)
        temp < 35f -> Color(0xFF8BC34A)
        temp < 40f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

private fun getTempHealthProgress(temp: Float): Float {
    return when {
        temp < 30f -> 1.0f
        temp < 35f -> 0.8f
        temp < 40f -> 0.5f
        else -> 0.2f
    }
}

private fun getVoltageStatus(voltage: Float): String {
    return when {
        voltage < 3.5f -> "Very Low"
        voltage < 3.7f -> "Low"
        voltage < 4.0f -> "Normal"
        voltage < 4.2f -> "Good"
        else -> "High"
    }
}

private fun getDegradationAssessment(health: Int): String {
    val degradation = 100 - health
    return when {
        degradation <= 10 -> "Excellent preservation"
        degradation <= 20 -> "Normal wear"
        degradation <= 30 -> "Moderate degradation"
        else -> "Significant wear"
    }
}

private fun getHealthTips(batteryData: com.voltai.smartbatteryguardian.BatteryData): List<String> {
    val tips = mutableListOf<String>()

    if (batteryData.temperature > 35f) {
        tips.add("Reduce device temperature by closing intensive apps and removing cases during charging")
    }

    if (batteryData.healthPercentage < 80) {
        tips.add("Avoid charging to 100% regularly - try to stay between 20-80% for optimal longevity")
        tips.add("Use original or certified chargers to prevent voltage irregularities")
    }

    tips.add("Enable adaptive charging if available to reduce overnight charging stress")
    tips.add("Avoid exposing your device to extreme temperatures (below 0°C or above 35°C)")
    tips.add("Consider enabling battery optimization for apps you don't use frequently")

    if (batteryData.current < -1500) {
        tips.add("High power consumption detected - reduce screen brightness and close background apps")
    }

    return tips
}
