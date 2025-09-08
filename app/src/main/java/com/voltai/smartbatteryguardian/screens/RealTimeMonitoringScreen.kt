package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

// Data Models for Real-time Monitoring
data class RealTimeMetrics(
    val batteryLevel: Int,
    val voltage: Float,
    val temperature: Float,
    val current: Int,
    val power: Float,
    val isCharging: Boolean,
    val chargingSpeed: String,
    val timeRemaining: String,
    val timestamp: Long
)

data class AlertData(
    val id: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isActive: Boolean,
    val icon: String
)

enum class AlertType {
    TEMPERATURE, VOLTAGE, CHARGING, HEALTH, PERFORMANCE
}

enum class AlertSeverity {
    INFO, WARNING, CRITICAL
}

data class GraphDataPoint(
    val timestamp: Long,
    val value: Float
)

data class MonitoringConfig(
    val updateInterval: Long = 1000L,
    val enableTemperatureAlerts: Boolean = true,
    val enableVoltageAlerts: Boolean = true,
    val enableChargingAlerts: Boolean = true,
    val temperatureThreshold: Float = 40f,
    val voltageThreshold: Float = 4.3f,
    val soundEnabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeMonitoringScreen() {
    val context = LocalContext.current
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📊 Dashboard", "📈 Graphs", "🚨 Alerts", "⚙️ Config")
    
    // Real-time data state
    var currentMetrics by remember { 
        mutableStateOf(generateSampleMetrics())
    }
    var batteryHistory by remember { 
        mutableStateOf(generateHistoryData())
    }
    var temperatureHistory by remember { 
        mutableStateOf(generateTemperatureHistory())
    }
    var voltageHistory by remember { 
        mutableStateOf(generateVoltageHistory())
    }
    var currentAlerts by remember { 
        mutableStateOf(generateSampleAlerts())
    }
    var monitoringConfig by remember { 
        mutableStateOf(MonitoringConfig())
    }
    
    var isMonitoring by remember { mutableStateOf(true) }
    
    // Simulate real-time updates
    LaunchedEffect(isMonitoring) {
        while (isMonitoring) {
            delay(monitoringConfig.updateInterval)
            
            // Update metrics with realistic variations
            currentMetrics = updateMetrics(currentMetrics)
            
            // Update history data
            batteryHistory = updateHistoryData(batteryHistory, currentMetrics.batteryLevel.toFloat())
            temperatureHistory = updateHistoryData(temperatureHistory, currentMetrics.temperature)
            voltageHistory = updateHistoryData(voltageHistory, currentMetrics.voltage)
            
            // Check for alerts
            val newAlerts = checkForAlerts(currentMetrics, monitoringConfig)
            if (newAlerts.isNotEmpty()) {
                currentAlerts = (currentAlerts + newAlerts).takeLast(10)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color(0xFFE8F5E8)
                    )
                )
            )
    ) {
        // Real-time Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2E7D32),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live indicator animation
                    LiveIndicator(isLive = isMonitoring)
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Real-Time Monitoring",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Live battery metrics & system alerts",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    
                    // Monitoring Controls
                    MonitoringControls(
                        isMonitoring = isMonitoring,
                        onToggleMonitoring = { isMonitoring = !isMonitoring },
                        alertCount = currentAlerts.count { it.isActive }
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Real-time Quick Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RealTimeStatCard(
                        "${currentMetrics.batteryLevel}%", 
                        "Battery", 
                        "🔋", 
                        getBatteryColor(currentMetrics.batteryLevel)
                    )
                    RealTimeStatCard(
                        "${currentMetrics.temperature.toInt()}°C", 
                        "Temperature", 
                        "🌡️", 
                        getTemperatureColor(currentMetrics.temperature)
                    )
                    RealTimeStatCard(
                        "${String.format("%.2f", currentMetrics.voltage)}V", 
                        "Voltage", 
                        "⚡", 
                        getVoltageColor(currentMetrics.voltage)
                    )
                    RealTimeStatCard(
                        "${String.format("%.1f", currentMetrics.power)}W", 
                        "Power", 
                        "💪", 
                        Color(0xFF9C27B0)
                    )
                }
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF2E7D32),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
        
        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item { RealTimeDashboard(currentMetrics) }
                    item { SystemStatusCard(currentMetrics) }
                    item { PowerAnalysisCard(currentMetrics) }
                }
                1 -> {
                    item { BatteryLevelChart(batteryHistory) }
                    item { TemperatureChart(temperatureHistory) }
                    item { VoltageChart(voltageHistory) }
                }
                2 -> {
                    items(currentAlerts.reversed()) { alert ->
                        AlertCard(alert)
                    }
                }
                3 -> {
                    item { 
                        MonitoringConfigCard(
                            config = monitoringConfig,
                            onConfigChange = { monitoringConfig = it }
                        ) 
                    }
                }
            }
        }
    }
}

@Composable
fun LiveIndicator(isLive: Boolean) {
    val pulseScale by animateFloatAsState(
        targetValue = if (isLive) 1.2f else 1.0f,
        animationSpec = if (isLive) infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ) else tween(300)
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isLive) 1f else 0.5f,
        animationSpec = if (isLive) infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ) else tween(300)
    )
    
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing circle
        Box(
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    this.alpha = alpha
                }
                .background(
                    if (isLive) Color(0xFF4CAF50) else Color(0xFFFF5722),
                    CircleShape
                )
        )
        
        Text(
            text = "📊",
            fontSize = 28.sp
        )
    }
}

@Composable
fun MonitoringControls(
    isMonitoring: Boolean,
    onToggleMonitoring: () -> Unit,
    alertCount: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Alert indicator
        if (alertCount > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF44336).copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚨",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$alertCount",
                        fontSize = 11.sp,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Play/Pause button
        Button(
            onClick = onToggleMonitoring,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = if (isMonitoring) "⏸️" else "▶️",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun RealTimeStatCard(value: String, label: String, icon: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RealTimeDashboard(metrics: RealTimeMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "📊 Live Dashboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Large battery level indicator
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BatteryLevelIndicator(
                    level = metrics.batteryLevel,
                    isCharging = metrics.isCharging
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Charging status
            if (metrics.isCharging) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔌", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Charging - ${metrics.chargingSpeed}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = metrics.timeRemaining,
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Real-time metrics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricDisplay("Current", "${metrics.current}mA", "⚡")
                MetricDisplay("Power", "${String.format("%.1f", metrics.power)}W", "💪")
                MetricDisplay("Updated", formatTime(metrics.timestamp), "🕒")
            }
        }
    }
}

@Composable
fun BatteryLevelIndicator(level: Int, isCharging: Boolean) {
    val animatedLevel by animateFloatAsState(
        targetValue = level / 100f,
        animationSpec = tween(1000, easing = EaseOutCubic)
    )
    
    Box(
        modifier = Modifier.size(150.dp),
        contentAlignment = Alignment.Center
    ) {
        // Battery outline with level fill
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val batteryWidth = size.width * 0.7f
            val batteryHeight = size.height * 0.8f
            val cornerRadius = 12.dp.toPx()
            
            // Battery body outline
            drawRoundRect(
                color = Color(0xFFE0E0E0),
                topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - batteryWidth) / 2,
                    (size.height - batteryHeight) / 2
                ),
                size = androidx.compose.ui.geometry.Size(batteryWidth, batteryHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth)
            )
            
            // Battery tip
            val tipWidth = batteryWidth * 0.2f
            val tipHeight = batteryHeight * 0.3f
            drawRoundRect(
                color = Color(0xFFE0E0E0),
                topLeft = androidx.compose.ui.geometry.Offset(
                    size.width / 2 + batteryWidth / 2 - tipWidth / 2,
                    size.height / 2 - tipHeight / 2
                ),
                size = androidx.compose.ui.geometry.Size(tipWidth, tipHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius / 2, cornerRadius / 2)
            )
            
            // Battery level fill
            val fillColor = when {
                level > 80 -> Color(0xFF4CAF50)
                level > 50 -> Color(0xFFFF9800)
                level > 20 -> Color(0xFFFF5722)
                else -> Color(0xFFF44336)
            }
            
            val fillHeight = batteryHeight * animatedLevel * 0.9f // Leave some margin
            val fillY = (size.height + batteryHeight) / 2 - fillHeight - strokeWidth
            
            drawRoundRect(
                color = fillColor,
                topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - batteryWidth) / 2 + strokeWidth,
                    fillY
                ),
                size = androidx.compose.ui.geometry.Size(
                    batteryWidth - strokeWidth * 2,
                    fillHeight
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius - strokeWidth, cornerRadius - strokeWidth)
            )
        }
        
        // Charging animation overlay
        if (isCharging) {
            val chargeAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Text(
                text = "⚡",
                fontSize = 32.sp,
                color = Color.White.copy(alpha = chargeAlpha)
            )
        }
        
        // Battery percentage text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${level}%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCharging) Color.White else Color(0xFF333333)
            )
        }
    }
}

@Composable
fun SystemStatusCard(metrics: RealTimeMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🔧 System Status",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status indicators
            StatusIndicator(
                label = "Temperature",
                value = "${metrics.temperature.toInt()}°C",
                status = getTemperatureStatus(metrics.temperature),
                icon = "🌡️"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatusIndicator(
                label = "Voltage",
                value = "${String.format("%.2f", metrics.voltage)}V",
                status = getVoltageStatus(metrics.voltage),
                icon = "⚡"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatusIndicator(
                label = "Charging System",
                value = if (metrics.isCharging) "Active" else "Idle",
                status = if (metrics.isCharging) "NORMAL" else "IDLE",
                icon = "🔌"
            )
        }
    }
}

@Composable
fun StatusIndicator(label: String, value: String, status: String, icon: String) {
    val statusColor = when (status) {
        "NORMAL", "IDLE" -> Color(0xFF4CAF50)
        "WARNING" -> Color(0xFFFF9800)
        "CRITICAL" -> Color(0xFFF44336)
        else -> Color(0xFF666666)
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = status,
                    fontSize = 10.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun BatteryLevelChart(history: List<GraphDataPoint>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🔋 Battery Level Trend",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple line chart representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📈",
                        fontSize = 48.sp
                    )
                    Text(
                        text = "Real-time battery level chart",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "${history.size} data points",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}

@Composable
fun TemperatureChart(history: List<GraphDataPoint>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🌡️ Temperature Trend",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 48.sp
                    )
                    Text(
                        text = "Temperature monitoring chart",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    if (history.isNotEmpty()) {
                        Text(
                            text = "Current: ${String.format("%.1f", history.last().value)}°C",
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoltageChart(history: List<GraphDataPoint>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "⚡ Voltage Trend",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 48.sp
                    )
                    Text(
                        text = "Voltage monitoring chart",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    if (history.isNotEmpty()) {
                        Text(
                            text = "Current: ${String.format("%.2f", history.last().value)}V",
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: AlertData) {
    val severityColor = when (alert.severity) {
        AlertSeverity.INFO -> Color(0xFF2196F3)
        AlertSeverity.WARNING -> Color(0xFFFF9800)
        AlertSeverity.CRITICAL -> Color(0xFFF44336)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isActive) 
                severityColor.copy(alpha = 0.05f) 
            else Color.White
        ),
        border = if (alert.isActive) 
            BorderStroke(2.dp, severityColor.copy(alpha = 0.3f)) 
        else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (alert.isActive) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = alert.icon, fontSize = 24.sp)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alert.message,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTime(alert.timestamp),
                    fontSize = 10.sp,
                    color = Color(0xFF999999)
                )
            }
            
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = severityColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = alert.severity.name,
                    fontSize = 10.sp,
                    color = severityColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun PowerAnalysisCard(metrics: RealTimeMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "💪 Power Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                PowerMetricItem("Power Draw", "${String.format("%.1f", abs(metrics.power))}W", "💪")
                PowerMetricItem("Efficiency", "92%", "📊")
                PowerMetricItem("Est. Runtime", "6h 23m", "⏱️")
            }
        }
    }
}

@Composable
fun PowerMetricItem(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun MonitoringConfigCard(
    config: MonitoringConfig,
    onConfigChange: (MonitoringConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "⚙️ Monitoring Configuration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Update interval
            ConfigSection(
                title = "Update Interval",
                description = "How often to refresh data"
            ) {
                Text(
                    text = "${config.updateInterval}ms",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Alert toggles
            ConfigToggle(
                title = "Temperature Alerts",
                isEnabled = config.enableTemperatureAlerts,
                onToggle = { onConfigChange(config.copy(enableTemperatureAlerts = it)) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ConfigToggle(
                title = "Voltage Alerts",
                isEnabled = config.enableVoltageAlerts,
                onToggle = { onConfigChange(config.copy(enableVoltageAlerts = it)) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ConfigToggle(
                title = "Sound Notifications",
                isEnabled = config.soundEnabled,
                onToggle = { onConfigChange(config.copy(soundEnabled = it)) }
            )
        }
    }
}

@Composable
fun ConfigSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
        
        content()
    }
}

@Composable
fun ConfigToggle(
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF2E7D32),
                checkedTrackColor = Color(0xFF2E7D32).copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun MetricDisplay(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
    }
}

// Utility functions
private fun generateSampleMetrics(): RealTimeMetrics {
    return RealTimeMetrics(
        batteryLevel = Random.nextInt(20, 100),
        voltage = 3.7f + Random.nextFloat() * 0.5f,
        temperature = 25f + Random.nextFloat() * 15f,
        current = Random.nextInt(-2000, 2000),
        power = Random.nextFloat() * 25f,
        isCharging = Random.nextBoolean(),
        chargingSpeed = "Fast Charging",
        timeRemaining = "2h 15m to full",
        timestamp = System.currentTimeMillis()
    )
}

private fun updateMetrics(current: RealTimeMetrics): RealTimeMetrics {
    val batteryChange = Random.nextInt(-2, 3)
    val newLevel = (current.batteryLevel + batteryChange).coerceIn(0, 100)
    
    return current.copy(
        batteryLevel = newLevel,
        voltage = current.voltage + Random.nextFloat() * 0.1f - 0.05f,
        temperature = current.temperature + Random.nextFloat() * 2f - 1f,
        current = current.current + Random.nextInt(-200, 200),
        power = abs(current.voltage * current.current / 1000f),
        timestamp = System.currentTimeMillis()
    )
}

private fun generateHistoryData(): List<GraphDataPoint> {
    val history = mutableListOf<GraphDataPoint>()
    val now = System.currentTimeMillis()
    
    repeat(50) { i ->
        history.add(
            GraphDataPoint(
                timestamp = now - (49 - i) * 1000,
                value = 50f + Random.nextFloat() * 40f
            )
        )
    }
    
    return history
}

private fun generateTemperatureHistory(): List<GraphDataPoint> {
    val history = mutableListOf<GraphDataPoint>()
    val now = System.currentTimeMillis()
    
    repeat(50) { i ->
        history.add(
            GraphDataPoint(
                timestamp = now - (49 - i) * 1000,
                value = 25f + Random.nextFloat() * 15f
            )
        )
    }
    
    return history
}

private fun generateVoltageHistory(): List<GraphDataPoint> {
    val history = mutableListOf<GraphDataPoint>()
    val now = System.currentTimeMillis()
    
    repeat(50) { i ->
        history.add(
            GraphDataPoint(
                timestamp = now - (49 - i) * 1000,
                value = 3.7f + Random.nextFloat() * 0.5f
            )
        )
    }
    
    return history
}

private fun updateHistoryData(current: List<GraphDataPoint>, newValue: Float): List<GraphDataPoint> {
    val updated = current.toMutableList()
    updated.add(
        GraphDataPoint(
            timestamp = System.currentTimeMillis(),
            value = newValue
        )
    )
    return updated.takeLast(50) // Keep only last 50 points
}

private fun generateSampleAlerts(): List<AlertData> {
    return listOf(
        AlertData(
            id = "1",
            type = AlertType.TEMPERATURE,
            severity = AlertSeverity.WARNING,
            title = "Temperature Warning",
            message = "Battery temperature reached 42°C during fast charging",
            timestamp = System.currentTimeMillis() - 300000, // 5 minutes ago
            isActive = true,
            icon = "🌡️"
        ),
        AlertData(
            id = "2",
            type = AlertType.VOLTAGE,
            severity = AlertSeverity.INFO,
            title = "Voltage Optimal",
            message = "Battery voltage stabilized at 4.1V",
            timestamp = System.currentTimeMillis() - 600000, // 10 minutes ago
            isActive = false,
            icon = "⚡"
        )
    )
}

private fun checkForAlerts(metrics: RealTimeMetrics, config: MonitoringConfig): List<AlertData> {
    val alerts = mutableListOf<AlertData>()
    
    // Temperature alerts
    if (config.enableTemperatureAlerts && metrics.temperature > config.temperatureThreshold) {
        alerts.add(
            AlertData(
                id = "temp_${System.currentTimeMillis()}",
                type = AlertType.TEMPERATURE,
                severity = AlertSeverity.WARNING,
                title = "High Temperature",
                message = "Battery temperature: ${metrics.temperature.toInt()}°C",
                timestamp = System.currentTimeMillis(),
                isActive = true,
                icon = "🔥"
            )
        )
    }
    
    // Voltage alerts
    if (config.enableVoltageAlerts && metrics.voltage > config.voltageThreshold) {
        alerts.add(
            AlertData(
                id = "volt_${System.currentTimeMillis()}",
                type = AlertType.VOLTAGE,
                severity = AlertSeverity.CRITICAL,
                title = "High Voltage",
                message = "Voltage: ${String.format("%.2f", metrics.voltage)}V",
                timestamp = System.currentTimeMillis(),
                isActive = true,
                icon = "⚡"
            )
        )
    }
    
    return alerts
}

private fun getBatteryColor(level: Int): Color {
    return when {
        level > 80 -> Color(0xFF4CAF50)
        level > 50 -> Color(0xFFFF9800)
        level > 20 -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }
}

private fun getTemperatureColor(temp: Float): Color {
    return when {
        temp < 30f -> Color(0xFF4CAF50)
        temp < 40f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

private fun getVoltageColor(voltage: Float): Color {
    return when {
        voltage >= 4.0f -> Color(0xFF4CAF50)
        voltage >= 3.6f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

private fun getTemperatureStatus(temp: Float): String {
    return when {
        temp < 30f -> "NORMAL"
        temp < 40f -> "WARNING"
        else -> "CRITICAL"
    }
}

private fun getVoltageStatus(voltage: Float): String {
    return when {
        voltage >= 3.6f -> "NORMAL"
        voltage >= 3.2f -> "WARNING"
        else -> "CRITICAL"
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
