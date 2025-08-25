package com.voltai.smartbatteryguardian

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay

class VoltAIApp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setContent {
            com.voltai.ui.theme.VoltAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VoltAIMainScreen()
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltAIMainScreen() {
    val context = LocalContext.current
    var batteryInfo by remember { mutableStateOf(getBatteryInfo(context)) }
    
    // Auto-refresh battery info
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Update every 5 seconds
            batteryInfo = getBatteryInfo(context)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE3F2FD)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚡ VoltAI",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0066CC)
            )
            
            Text(
                text = "Smart Battery Guardian",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Animated Battery Ring
        AnimatedBatteryRing(
            batteryLevel = batteryInfo.level,
            isCharging = batteryInfo.isCharging
        )
        
        // Battery Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${batteryInfo.level}%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = getBatteryColor(batteryInfo.level)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (batteryInfo.isCharging) {
                        Text(
                            text = "⚡ Charging",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            text = "🔋 On Battery",
                            color = Color(0xFF666666),
                            fontSize = 16.sp
                        )
                    }
                }
                
                Text(
                    text = batteryInfo.status,
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )
            }
        }
        
        // Quick Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickStatCard(
                title = "Health",
                value = "${batteryInfo.health}%",
                icon = "❤️",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            QuickStatCard(
                title = "Temp",
                value = "${batteryInfo.temperature}°C",
                icon = "🌡️",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            QuickStatCard(
                title = "Voltage",
                value = "${batteryInfo.voltage}V",
                icon = "⚡",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action Button
        Button(
            onClick = { 
                batteryInfo = getBatteryInfo(context)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0066CC)
            )
        ) {
            Text(
                text = "🔄 Refresh Battery Status",
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        Text(
            text = "VoltAI v1.0.0 - Smart Battery Management",
            fontSize = 12.sp,
            color = Color(0xFF888888)
        )
    }
}

@Composable
fun AnimatedBatteryRing(
    batteryLevel: Int,
    isCharging: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "battery")
    val animatedLevel by animateFloatAsState(
        targetValue = batteryLevel.toFloat(),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "level"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val radius = size.minDimension / 2.5f
            
            // Background ring
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = radius,
                center = center,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Battery level arc
            val sweepAngle = 360f * (animatedLevel / 100f)
            drawArc(
                color = getBatteryColor(batteryLevel.toInt()),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Charging glow
            if (isCharging) {
                drawCircle(
                    color = Color(0xFF4CAF50).copy(alpha = glowAlpha * 0.3f),
                    radius = radius + 8.dp.toPx(),
                    center = center,
                    style = Stroke(width = 16.dp.toPx())
                )
            }
        }
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$batteryLevel%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = getBatteryColor(batteryLevel)
            )
            if (isCharging) {
                Text(
                    text = "⚡",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0066CC)
            )
        }
    }
}

fun getBatteryColor(level: Int): Color {
    return when {
        level > 80 -> Color(0xFF4CAF50) // Green
        level > 50 -> Color(0xFF2196F3) // Blue
        level > 20 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

data class BatteryInfoUi(
    val level: Int,
    val isCharging: Boolean,
    val health: Int,
    val temperature: Int,
    val voltage: Float,
    val status: String
)

fun getBatteryInfo(context: Context): BatteryInfoUi {
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
    val batteryPct = (level * 100 / scale.coerceAtLeast(1))
    
    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                     status == BatteryManager.BATTERY_STATUS_FULL
    
    val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: 0
    val healthPct = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> 100
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> 75
        BatteryManager.BATTERY_HEALTH_DEAD -> 0
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 60
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 50
        BatteryManager.BATTERY_HEALTH_COLD -> 80
        else -> 95
    }
    
    val temperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
    val tempCelsius = temperature / 10
    
    val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    val voltageFloat = voltage / 1000f
    
    val statusText = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
        BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
        else -> "Unknown"
    }
    
    return BatteryInfoUi(
        level = batteryPct,
        isCharging = isCharging,
        health = healthPct,
        temperature = tempCelsius,
        voltage = voltageFloat,
        status = statusText
    )
}