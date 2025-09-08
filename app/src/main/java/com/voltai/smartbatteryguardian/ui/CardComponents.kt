package com.voltai.smartbatteryguardian.ui

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.BatteryData

// Get battery level color based on percentage
fun getBatteryLevelColor(level: Int): Color {
    return when {
        level > 80 -> Color(0xFF4CAF50)
        level > 50 -> Color(0xFFFF9800)
        level > 20 -> Color(0xFFFF5722)
        else -> Color(0xFFD32F2F)
    }
}

@Composable
fun BatteryInfoCardContent(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "${batteryData.batteryLevel}%",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = getBatteryLevelColor(batteryData.batteryLevel),
                letterSpacing = (-1).sp
            )
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (batteryData.isCharging) Color(0xFF4CAF50) else Color(0xFFF0F0F0)
            ) {
                Text(
                    text = batteryData.chargingStatus,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (batteryData.isCharging) Color.White else Color.Black
                )
            }
        }
        
        // Animated progress bar
        AnimatedBatteryBar(batteryData.batteryLevel)
        
        Text(
            text = batteryData.remainingTime,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AnimatedBatteryBar(batteryLevel: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = batteryLevel / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(
                Color(0xFFF0F0F0),
                RoundedCornerShape(4.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            getBatteryLevelColor(batteryLevel).copy(alpha = 0.8f),
                            getBatteryLevelColor(batteryLevel)
                        )
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
fun ElectricCurrentCardContent(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Electric Current",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        Column {
            Text(
                text = "${batteryData.current} mA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "${String.format("%.1f", batteryData.voltage)} V",
                fontSize = 11.sp,
                color = Color(0xFF666666)
            )
        }
        
        AnimatedWaveGraph()
    }
}

@Composable
fun OngoingCardContent(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Ongoing",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OngoingItem(
                icon = "📱",
                label = "Screen On",
                value = batteryData.screenOnTime
            )
            OngoingItem(
                icon = "🔋",
                label = "Used",
                value = batteryData.batteryUsed
            )
        }
    }
}

@Composable
fun OngoingItem(
    icon: String,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = icon,
            fontSize = 12.sp
        )
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = value,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun TemperatureCardContent(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Temperature",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        Column {
            Text(
                text = "${batteryData.temperature.toInt()}°C",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = when {
                    batteryData.temperature > 45f -> "Hot"
                    batteryData.temperature > 35f -> "Warm"
                    else -> "Normal"
                },
                fontSize = 11.sp,
                color = when {
                    batteryData.temperature > 45f -> Color(0xFFE57373)
                    batteryData.temperature > 35f -> Color(0xFFFFB74D)
                    else -> Color(0xFF81C784)
                }
            )
        }
        
        AnimatedTemperatureGraph(batteryData.temperature)
    }
}

@Composable
fun HealthCardContent(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Battery Health",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val animatedProgress by animateFloatAsState(
                targetValue = batteryData.healthPercentage / 100f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            
            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.size(32.dp),
                strokeWidth = 4.dp,
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE8F5E8)
            )
            
            Column {
                Text(
                    text = "${batteryData.healthPercentage}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = batteryData.capacity,
                    fontSize = 10.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
fun AverageUsageCardContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Average Usage",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OngoingItem(
                icon = "☀️",
                label = "Screen On",
                value = "~19.2%/h"
            )
            OngoingItem(
                icon = "🌙",
                label = "Screen Off",
                value = "~5.7%/h"
            )
        }
    }
}

@Composable
fun ChargeEstimationCardContent(batteryData: BatteryData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Charge Time",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OngoingItem(
                icon = "⚡",
                label = "Fast",
                value = "1h 36m"
            )
            OngoingItem(
                icon = "🐌",
                label = "Slow",
                value = "3h 42m"
            )
        }
    }
}

@Composable
fun HistoryCardContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "History",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        // Mini bar chart
        AnimatedBarChart()
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Sessions",
                    fontSize = 9.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "5",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Column {
                Text(
                    text = "Charged",
                    fontSize = 9.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "457%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun ToolsCardContent() {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolButton(icon = "🔗", label = "Bluetooth")
        ToolButton(icon = "📊", label = "Logs")
        ToolButton(icon = "⚙️", label = "Settings")
    }
}

@Composable
fun ToolButton(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF0F0F0),
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = icon,
                    fontSize = 14.sp
                )
            }
        }
        Text(
            text = label,
            fontSize = 8.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AIInsightsCardContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "AI Insights",
                fontSize = 11.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = "Battery optimization active. 23% improvement detected.",
            fontSize = 12.sp,
            color = Color.Black,
            lineHeight = 16.sp
        )
        
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF9C27B0).copy(alpha = 0.1f)
        ) {
            Text(
                text = "View Details",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 9.sp,
                color = Color(0xFF9C27B0),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun QuickActionsCardContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Quick Actions",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton("🔋", "Saver")
            QuickActionButton("📱", "Screen")
            QuickActionButton("🔊", "Sound")
        }
    }
}

@Composable
fun QuickActionButton(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = label,
            fontSize = 8.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun PowerModesCardContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Power Modes",
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PowerModeItem("⚡", "Performance", true)
            PowerModeItem("🔋", "Battery Saver", false)
            PowerModeItem("🌙", "Ultra Saver", false)
        }
    }
}

@Composable
fun PowerModeItem(icon: String, name: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, fontSize = 10.sp)
        Text(
            text = name,
            fontSize = 9.sp,
            color = if (isActive) Color(0xFF2196F3) else Color(0xFF666666),
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
        if (isActive) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF2196F3),
                modifier = Modifier.size(4.dp)
            ) {}
        }
    }
}

// Animated components
@Composable
fun AnimatedWaveGraph() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val path = Path()
        val points = listOf(0.2f, 0.4f, 0.8f, 0.5f, 0.9f, 0.3f, 0.7f)
        
        points.forEachIndexed { index, point ->
            val x = (index * size.width) / (points.size - 1)
            val y = size.height - (point * size.height)
            
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
fun AnimatedTemperatureGraph(temperature: Float) {
    val tempColor = when {
        temperature > 45f -> Color(0xFFE57373)
        temperature > 35f -> Color(0xFFFFB74D)
        else -> Color(0xFF81C784)
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        val points = listOf(0.4f, 0.3f, 0.5f, 0.4f, 0.6f, 0.7f, 0.5f)
        val path = Path()
        
        points.forEachIndexed { index, point ->
            val x = (index * size.width) / (points.size - 1)
            val y = size.height - (point * size.height)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = tempColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CardContent(
    cardType: com.voltai.smartbatteryguardian.ui.home.CardType,
    batteryData: BatteryData,
    isEditMode: Boolean
) {
    when (cardType) {
        com.voltai.smartbatteryguardian.ui.home.CardType.BATTERY_LEVEL -> BatteryInfoCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.CHARGING_STATUS -> OngoingCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.TEMPERATURE -> TemperatureCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.HEALTH -> HealthCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.CURRENT_VOLTAGE -> ElectricCurrentCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.SCREEN_TIME -> OngoingCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.APP_USAGE -> AverageUsageCardContent()
        com.voltai.smartbatteryguardian.ui.home.CardType.CHARGING_HISTORY -> HistoryCardContent()
        com.voltai.smartbatteryguardian.ui.home.CardType.AI_INSIGHTS -> AIInsightsCardContent()
        com.voltai.smartbatteryguardian.ui.home.CardType.POWER_MODES -> PowerModesCardContent()
        com.voltai.smartbatteryguardian.ui.home.CardType.QUICK_ACTIONS -> QuickActionsCardContent()
        com.voltai.smartbatteryguardian.ui.home.CardType.PERFORMANCE_CHART -> HistoryCardContent()
        com.voltai.smartbatteryguardian.ui.home.CardType.THERMAL_STATUS -> TemperatureCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.BATTERY_TIPS -> AIInsightsCardContent()
        com.voltai.smartbatteryguardian.ui.home.CardType.HISTORY -> com.voltai.smartbatteryguardian.ui.home.HistoryCardContent(batteryData, isEditMode)
        com.voltai.smartbatteryguardian.ui.home.CardType.OVERLAYS -> com.voltai.smartbatteryguardian.ui.home.OverlaysCardContent(batteryData, isEditMode)
        
        // Premium Advanced Cards
        com.voltai.smartbatteryguardian.ui.home.CardType.CHARGING_ANALYTICS -> ChargingAnalyticsCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.CHEMISTRY_LAB -> ChemistryLabCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.SYSTEM_OPTIMIZATION -> SystemOptimizationCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.PERFORMANCE_BENCHMARKS -> PerformanceBenchmarksCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.DATA_EXPORT_IMPORT -> com.voltai.smartbatteryguardian.ui.home.DataExportImportCardContent(batteryData, isEditMode)
        
        // New Premium Analytics Cards
        com.voltai.smartbatteryguardian.ui.home.CardType.BATTERY_HEALTH_ANALYTICS -> com.voltai.smartbatteryguardian.ui.home.EnhancedBatteryHealthAnalyticsCard(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.SMART_CHARGING -> com.voltai.smartbatteryguardian.ui.home.EnhancedSmartChargingCard(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.BATTERY_DIAGNOSTICS -> BatteryDiagnosticsCardContent(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.REAL_TIME_MONITORING -> com.voltai.smartbatteryguardian.ui.home.EnhancedRealTimeMonitoringCard(batteryData)
        com.voltai.smartbatteryguardian.ui.home.CardType.BATTERY_OPTIMIZATION -> BatteryOptimizationCardContent(batteryData)
    }
}

@Composable
fun AnimatedBarChart() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val data = listOf(0.3f, 0.6f, 0.4f, 0.8f, 0.5f, 0.9f, 0.7f)
        
        data.forEach { height ->
            val animatedHeight by animateFloatAsState(
                targetValue = height,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((animatedHeight * 32).dp)
                    .background(
                        Color(0xFF757575),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

// Premium Card Content Functions
@Composable
fun ChargingAnalyticsCardContent(batteryData: BatteryData) {
    Column {
        Text(
            text = "⚡ Charging Analytics",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Advanced insights",
            fontSize = 10.sp,
            color = Color(0xFF2196F3)
        )
        AnimatedWaveGraph()
    }
}

@Composable
fun ChemistryLabCardContent(batteryData: BatteryData) {
    Column {
        Text(
            text = "🔬 Chemistry Lab",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Molecular analysis",
            fontSize = 10.sp,
            color = Color(0xFF6366F1)
        )
        AnimatedWaveGraph()
    }
}

@Composable
fun SystemOptimizationCardContent(batteryData: BatteryData) {
    Column {
        Text(
            text = "🚀 System Optimizer",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "AI optimization",
            fontSize = 10.sp,
            color = Color(0xFF10B981)
        )
        AnimatedBarChart()
    }
}

@Composable
fun PerformanceBenchmarksCardContent(batteryData: BatteryData) {
    Column {
        Text(
            text = "📊 Benchmarks",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Performance tests",
            fontSize = 10.sp,
            color = Color(0xFFEF4444)
        )
        AnimatedBarChart()
    }
}

// New Premium Analytics Card Content Functions
@Composable
fun BatteryHealthAnalyticsCardContent(batteryData: BatteryData) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔋 Health Analytics",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "AI",
                    fontSize = 8.sp,
                    color = Color(0xFFB8860B),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Health Score: ${batteryData.healthPercentage}%",
            fontSize = 10.sp,
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = batteryData.healthPercentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Color(0xFF4CAF50),
            trackColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
        )
    }
}

@Composable
fun SmartChargingCardContent(batteryData: BatteryData) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🧠 Smart Charging",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF6A1B9A).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "PRO",
                    fontSize = 8.sp,
                    color = Color(0xFF6A1B9A),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (batteryData.isCharging) "Optimizing..." else "Ready",
            fontSize = 10.sp,
            color = if (batteryData.isCharging) Color(0xFF2196F3) else Color(0xFF666666)
        )
        AnimatedWaveGraph()
    }
}

@Composable
fun BatteryDiagnosticsCardContent(batteryData: BatteryData) {
    Column {
        Text(
            text = "🔧 Diagnostics",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Status:",
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = if (batteryData.healthPercentage > 80) "Excellent" else "Good",
                fontSize = 10.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cycles:",
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = "847",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RealTimeMonitoringCardContent(batteryData: BatteryData) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 Live Monitor",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = CircleShape,
                color = if (batteryData.isCharging) Color(0xFF4CAF50) else Color(0xFF666666),
                modifier = Modifier.size(6.dp)
            ) { }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${batteryData.current} mA",
                fontSize = 10.sp,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.1f", batteryData.voltage)}V",
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )
        }
        AnimatedWaveGraph()
    }
}

@Composable
fun BatteryOptimizationCardContent(batteryData: BatteryData) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Optimization",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF10B981).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "AI",
                    fontSize = 8.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Score: 89/100",
            fontSize = 10.sp,
            color = Color(0xFF10B981),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "3 suggestions available",
            fontSize = 9.sp,
            color = Color(0xFF666666)
        )
    }
}
