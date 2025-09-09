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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
// import com.voltai.ui.designsystem.VoltAITheme
// import com.voltai.ui.designsystem.getBatteryLevelColor
import java.text.SimpleDateFormat
import java.util.*

class PremiumMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PremiumBatteryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumBatteryApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var batteryInfo by remember { mutableStateOf(getBatteryInfo(context)) }
    var isDarkMode by remember { mutableStateOf(false) }

    // Auto-refresh battery info
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // Update every 2 seconds
            batteryInfo = getBatteryInfo(context)
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top App Bar with theme toggle
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚡ VoltAI Pro",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Premium badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFD700)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isDarkMode = !isDarkMode }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Main Content
            when (selectedTab) {
                0 -> PremiumDashboardScreen(batteryInfo)
                1 -> AnalyticsScreen(batteryInfo)
                2 -> HealthScreen(batteryInfo)
                3 -> OptimizationScreen(batteryInfo)
                4 -> SettingsScreen()
            }

            // Bottom Navigation
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    "Dashboard" to Icons.Default.Dashboard,
                    "Analytics" to Icons.Default.Analytics,
                    "Health" to Icons.Default.Favorite,
                    "Optimize" to Icons.Default.Speed,
                    "Settings" to Icons.Default.Settings
                )

                tabs.forEachIndexed { index, (title, icon) ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (selectedTab == index) MaterialTheme.colorScheme.primary 
                                      else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumDashboardScreen(batteryInfo: BatteryInfo) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Hero Battery Display
            PremiumBatteryHeroCard(batteryInfo)
        }
        
        item {
            // Quick Stats Row
            QuickStatsRow(batteryInfo)
        }
        
        item {
            // Live Charging Graph
            LiveChargingGraphCard(batteryInfo)
        }
        
        item {
            // AI Insights Card
            AIInsightsCard(batteryInfo)
        }
        
        item {
            // App Usage Impact
            AppUsageImpactCard()
        }
        
        item {
            // Battery History
            BatteryHistoryCard()
        }
    }
}

@Composable
fun PremiumBatteryHeroCard(batteryInfo: BatteryInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
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
                    modifier = Modifier.size(160.dp)
                ) {
                    PremiumAnimatedBatteryRing(
                        batteryLevel = batteryInfo.level,
                        isCharging = batteryInfo.isCharging,
                        size = 160.dp
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${batteryInfo.level}%",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                        color = getBatteryColor(batteryInfo.level)
                        )
                        
                        Text(
                            text = if (batteryInfo.isCharging) "Charging" else "Discharging",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time remaining
                TimeRemainingCard(batteryInfo)
            }
        }
    }
}

@Composable
fun PremiumAnimatedBatteryRing(
    batteryLevel: Int,
    isCharging: Boolean,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = batteryLevel / 100f,
        animationSpec = tween(1000, easing = EaseInOutCubic),
        label = "battery_level"
    )
    
    val chargingAnimation by animateFloatAsState(
        targetValue = if (isCharging) 1f else 0f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "charging_animation"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val strokeWidth = 12.dp.toPx()
        val radius = (size.toPx() - strokeWidth) / 2
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        
        // Background ring
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        
        // Battery level ring
        val sweepAngle = animatedLevel * 360f
        val batteryColor = when {
            batteryLevel <= 20 -> Color(0xFFF44336)
            batteryLevel <= 50 -> Color(0xFFFF9800)
            batteryLevel <= 80 -> Color(0xFF4CAF50)
            else -> Color(0xFF2196F3)
        }
        
        // Charging glow effect
        if (isCharging) {
            drawCircle(
                color = batteryColor.copy(alpha = chargingAnimation * 0.3f),
                radius = radius + 8.dp.toPx(),
                center = center,
                style = Stroke(4.dp.toPx())
            )
        }
        
        // Main battery ring
        drawArc(
            color = batteryColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth)
        )
    }
}

@Composable
fun QuickStatsRow(batteryInfo: BatteryInfo) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(
            listOf(
                QuickStat("Temperature", "${batteryInfo.temperature}°C", Icons.Default.Thermostat, Color(0xFFFF5722)),
                QuickStat("Voltage", "${batteryInfo.voltage}V", Icons.Default.ElectricBolt, Color(0xFF673AB7)),
                QuickStat("Health", "${batteryInfo.health}%", Icons.Default.Favorite, Color(0xFF4CAF50)),
                QuickStat("Current", "${batteryInfo.current}mA", Icons.Default.PowerSettingsNew, Color(0xFF2196F3)),
                QuickStat("Capacity", "${batteryInfo.capacity}mAh", Icons.Default.Battery6Bar, Color(0xFF9C27B0))
            )
        ) { stat ->
            QuickStatCard(stat)
        }
    }
}

@Composable
fun QuickStatCard(stat: QuickStat) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = stat.title,
                tint = stat.color,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = stat.value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = stat.title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LiveChargingGraphCard(batteryInfo: BatteryInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Charging Pattern",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Live",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier
                        .background(
                            Color(0xFF4CAF50).copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simplified graph visualization
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📊 Live charging curve\n${if (batteryInfo.isCharging) "▲ Charging at ${batteryInfo.current}mA" else "▼ Discharging"}",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AIInsightsCard(batteryInfo: BatteryInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6200EE).copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Insights",
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "AI Battery Coach",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val aiInsight = getAIInsight(batteryInfo)
            
            Text(
                text = aiInsight,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { /* Navigate to AI tips */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View All Tips")
            }
        }
    }
}

@Composable
fun AppUsageImpactCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Battery Draining Apps",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock app usage data
            val appUsage = listOf(
                AppUsage("📱 Chrome", 25, Color(0xFFFF5722)),
                AppUsage("🎮 PUBG Mobile", 18, Color(0xFFF44336)),
                AppUsage("📺 YouTube", 15, Color(0xFFFF9800)),
                AppUsage("💬 WhatsApp", 12, Color(0xFF4CAF50))
            )
            
            appUsage.forEach { app ->
                AppUsageItem(app)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AppUsageItem(app: AppUsage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = app.name,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.2f),
                        RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(app.percentage / 100f)
                        .background(
                            app.color,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
            
            Text(
                text = "${app.percentage}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(35.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun BatteryHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "24-Hour History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TextButton(onClick = { /* Navigate to full history */ }) {
                    Text("View All")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock battery history visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(12) { index ->
                    val height = (30 + (index * 5) % 60).dp
                    val color = when {
                        index < 4 -> Color(0xFF4CAF50)
                        index < 8 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(height)
                            .background(
                                color.copy(alpha = 0.7f),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "00:00",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "12:00",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "24:00",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TimeRemainingCard(batteryInfo: BatteryInfo) {
    val timeRemaining = calculateTimeRemaining(batteryInfo)
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (batteryInfo.isCharging) "Time to full" else "Time remaining",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = timeRemaining,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Placeholder screens for other tabs
@Composable
fun AnalyticsScreen(batteryInfo: BatteryInfo) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📊 Advanced Analytics",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "• Battery degradation tracking\n• Charging cycle analysis\n• Usage pattern insights\n• Performance benchmarks\n• Health predictions",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HealthScreen(batteryInfo: BatteryInfo) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "❤️ Battery Health",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Current Health: ${batteryInfo.health}%\nStatus: Excellent\nCharge Cycles: 245\nCapacity Remaining: 92%",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OptimizationScreen(batteryInfo: BatteryInfo) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "⚡ Smart Optimization",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { /* Run optimization */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("🚀 Optimize Now", fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "• Close background apps\n• Adjust screen brightness\n• Enable battery saver\n• Optimize network usage\n• Clear cache files",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "⚙️ Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val settings = listOf(
                        "🔔 Notifications",
                        "🎨 Themes & Appearance", 
                        "📊 Data & Privacy",
                        "⚡ Battery Alerts",
                        "🔄 Auto Optimization",
                        "💾 Backup & Sync",
                        "ℹ️ About VoltAI Pro"
                    )
                    
                    settings.forEach { setting ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = setting,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        if (setting != settings.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

// Data classes and utility functions
data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val health: Int,
    val temperature: Float,
    val voltage: Float,
    val current: Int,
    val capacity: Int,
    val status: String
)

data class QuickStat(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

data class AppUsage(
    val name: String,
    val percentage: Int,
    val color: Color
)

fun getBatteryInfo(context: Context): BatteryInfo {
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    
    return BatteryInfo(
        level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0,
        isCharging = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING,
        health = ((batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0) / 7.0 * 100).toInt(),
        temperature = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0f,
        voltage = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000.0f,
        current = kotlin.random.Random.nextInt(-2000, 1000), // Mock current
        capacity = 4500, // Mock capacity
        status = getStatusText(batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1)
    )
}

fun getStatusText(status: Int): String {
    return when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
        else -> "Unknown"
    }
}

fun calculateTimeRemaining(batteryInfo: BatteryInfo): String {
    val remainingPercent = if (batteryInfo.isCharging) (100 - batteryInfo.level) else batteryInfo.level
    val avgUsage = if (batteryInfo.isCharging) 20 else -15 // Mock usage rate per hour
    val hoursRemaining = (remainingPercent / kotlin.math.abs(avgUsage)).coerceAtLeast(0.1)
    
    val hours = hoursRemaining.toInt()
    val minutes = ((hoursRemaining - hours) * 60).toInt()
    
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

fun getBatteryColor(level: Int): Color {
    return when {
        level <= 20 -> Color(0xFFF44336) // Red
        level <= 50 -> Color(0xFFFF9800) // Orange  
        level <= 80 -> Color(0xFF4CAF50) // Green
        else -> Color(0xFF2196F3) // Blue
    }
}

fun getAIInsight(batteryInfo: BatteryInfo): String {
    return when {
        batteryInfo.isCharging && batteryInfo.level > 85 -> 
            "🔋 Consider unplugging soon to maintain battery health. Optimal charging is between 20-80%."
        
        batteryInfo.temperature > 40 -> 
            "🌡️ Your battery is running hot (${batteryInfo.temperature}°C). Consider closing intensive apps or cooling down your device."
        
        batteryInfo.level < 20 && !batteryInfo.isCharging -> 
            "⚠️ Low battery detected. Enable power saving mode and close unnecessary apps to extend usage time."
        
        batteryInfo.health < 80 -> 
            "❤️ Battery health is below optimal. Consider replacing your battery or adjusting charging habits."
        
        else -> 
            "✨ Your battery is performing well! Current usage patterns are optimal for long-term health."
    }
}
