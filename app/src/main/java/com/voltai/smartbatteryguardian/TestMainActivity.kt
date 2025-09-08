package com.voltai.smartbatteryguardian

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas

class TestMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                VoltAITestApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltAITestApp() {
    val context = LocalContext.current
    var batteryInfo by remember { mutableStateOf(getSimpleBatteryInfo(context)) }
    var isDarkMode by remember { mutableStateOf(false) }

    // Auto-refresh battery info
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // Update every 2 seconds
            batteryInfo = getSimpleBatteryInfo(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Color(0xFF121212) else Color(0xFFF8F9FA)
            )
            .padding(16.dp)
    ) {
        // Top App Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "⚡ VoltAI Pro",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0066CC)
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFD700)
                    ) {
                        Text(
                            text = "DEMO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                IconButton(onClick = { isDarkMode = !isDarkMode }) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.Settings else Icons.Default.MoreVert,
                        contentDescription = "Toggle theme",
                        tint = if (isDarkMode) Color.White else Color.Black
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Battery Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0066CC).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Battery Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp)
                ) {
                    SimpleBatteryRing(
                        batteryLevel = batteryInfo.level,
                        isCharging = batteryInfo.isCharging
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${batteryInfo.level}%",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = getBatteryColor(batteryInfo.level)
                        )
                        
                        Text(
                            text = if (batteryInfo.isCharging) "⚡ Charging" else "🔋 Battery",
                            fontSize = 14.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Battery stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BatteryStatItem("Health", "${batteryInfo.health}%", "❤️")
                    BatteryStatItem("Temp", "${batteryInfo.temperature}°C", "🌡️")
                    BatteryStatItem("Voltage", "${batteryInfo.voltage}V", "⚡")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // AI Insights Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF6200EE).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "🧠", fontSize = 24.sp)
                    
                    Text(
                        text = "AI Battery Coach",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = getAIRecommendation(batteryInfo),
                    fontSize = 14.sp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { /* Show more insights */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View All Tips", color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Quick Actions Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "⚡", fontSize = 24.sp)
                    Text(
                        text = "Quick Battery Optimizations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionButton(
                        icon = "🔋",
                        label = "Power Saver",
                        onClick = { /* Enable power saving mode */ }
                    )
                    QuickActionButton(
                        icon = "📱",
                        label = "Screen",
                        onClick = { /* Optimize screen settings */ }
                    )
                    QuickActionButton(
                        icon = "📶",
                        label = "Network",
                        onClick = { /* Optimize connectivity */ }
                    )
                    QuickActionButton(
                        icon = "🚀",
                        label = "Apps",
                        onClick = { /* Close background apps */ }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Text(
            text = "VoltAI Pro - The Ultimate Battery Management App",
            fontSize = 12.sp,
            color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun QuickActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF4CAF50).copy(alpha = 0.2f),
            modifier = Modifier.size(50.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SimpleBatteryRing(
    batteryLevel: Int,
    isCharging: Boolean
) {
    val animatedLevel by animateFloatAsState(
        targetValue = batteryLevel / 100f,
        animationSpec = tween(1500, easing = EaseInOutCubic),
        label = "battery_level"
    )
    
    val chargingPulse by animateFloatAsState(
        targetValue = if (isCharging) 1f else 0f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "charging_pulse"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // Background ring
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(strokeWidth)
        )
        
        // Battery level arc
        val sweepAngle = animatedLevel * 360f
        val batteryColor = when {
            batteryLevel <= 20 -> Color(0xFFF44336) // Red
            batteryLevel <= 50 -> Color(0xFFFF9800) // Orange
            batteryLevel <= 80 -> Color(0xFF4CAF50) // Green
            else -> Color(0xFF2196F3) // Blue
        }
        
        // Charging glow effect
        if (isCharging) {
            drawCircle(
                color = batteryColor.copy(alpha = chargingPulse * 0.3f),
                radius = radius + 8.dp.toPx(),
                center = center,
                style = Stroke(6.dp.toPx())
            )
        }
        
        // Main battery arc
        drawArc(
            color = batteryColor,
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
fun BatteryStatItem(
    label: String,
    value: String,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0066CC)
        )
        
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

// Simplified data class
data class SimpleBatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val health: Int,
    val temperature: Float,
    val voltage: Float
)

// Simple battery info function
fun getSimpleBatteryInfo(context: Context): SimpleBatteryInfo {
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    
    return SimpleBatteryInfo(
        level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 85) ?: 85,
        isCharging = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING,
        health = ((batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0) / 7.0 * 100).toInt().coerceAtLeast(85),
        temperature = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320) ?: 320) / 10.0f,
        voltage = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000) ?: 4000) / 1000.0f
    )
}

fun getBatteryColor(level: Int): Color {
    return when {
        level <= 20 -> Color(0xFFF44336) // Red
        level <= 50 -> Color(0xFFFF9800) // Orange  
        level <= 80 -> Color(0xFF4CAF50) // Green
        else -> Color(0xFF2196F3) // Blue
    }
}

fun getAIRecommendation(batteryInfo: SimpleBatteryInfo): String {
    return when {
        batteryInfo.isCharging && batteryInfo.level > 85 -> 
            "🔋 Your battery is nearly full! Consider unplugging soon to maintain optimal battery health. The ideal charging range is 20-80%. Overcharging can reduce battery lifespan."
        
        batteryInfo.temperature > 35 -> 
            "🌡️ Battery temperature is elevated (${batteryInfo.temperature}°C). Consider closing resource-intensive apps, removing the phone case, and letting your device cool down to prevent damage."
        
        batteryInfo.level < 20 && !batteryInfo.isCharging -> 
            "⚠️ Low battery detected! Enable power saving mode, close unnecessary background apps, reduce brightness, and turn off GPS/WiFi if not needed."
        
        batteryInfo.level < 10 && !batteryInfo.isCharging -> 
            "🚨 Critical battery level! Enable ultra power saving mode immediately and find a charger. Your device may shut down soon."
        
        batteryInfo.health < 85 -> 
            "❤️ Battery health could be improved. Consider optimizing charging habits (avoid 0-100% cycles), use original chargers, and keep device at room temperature."
        
        batteryInfo.isCharging && batteryInfo.level < 20 -> 
            "⚡ Smart charging detected! Your battery is in the optimal charging range. Fast charging is most efficient when battery is below 50%."
        
        else -> 
            "✨ Your battery is performing excellently! Current usage patterns are optimal for maintaining long-term battery health. Keep up the good habits!"
    }
}
