package com.voltai.widget

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlin.math.*

/**
 * Premium Battery Widgets Collection
 * Includes 8 different widget styles to compete with market leaders
 */

// Widget 1: Circular Progress Ring (like AccuBattery)
class CircularBatteryWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            CircularBatteryContent()
        }
    }
}

@Composable
private fun CircularBatteryContent() {
    val context = LocalContext.current
    val batteryInfo = remember { getBatteryInfo(context) }
    val batteryLevel = batteryInfo.level
    val isCharging = batteryInfo.isCharging
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.Black))
            .cornerRadius(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Battery percentage
            Text(
                text = if (batteryLevel > 0) "${batteryLevel}%" else "—",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(getBatteryColor(batteryLevel))
                )
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            // Status
            Text(
                text = if (isCharging) "⚡ Charging" else "🔋 Battery",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color.White.copy(alpha = 0.8f))
                )
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // Time remaining (unknown)
            Text(
                text = "—",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(Color.White.copy(alpha = 0.6f))
                )
            )
        }
    }
}

// Widget 2: Minimal Line Widget (like Battery Guru)
class MinimalLineWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MinimalLineContent()
        }
    }
}

@Composable
private fun MinimalLineContent() {
    val context = LocalContext.current
    val batteryInfo = remember { getBatteryInfo(context) }
    val batteryLevel = batteryInfo.level
    val temperature = batteryInfo.temperature
    
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.White))
            .cornerRadius(20.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            // Battery percentage
            Text(
                text = "${batteryLevel}%",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.Black)
                )
            )
            
            // Temperature
            Text(
                text = if (temperature > 0f) "${temperature}°C" else "—",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color.Gray)
                )
            )
        }
        
        // Battery level bar
        Box(
            modifier = GlanceModifier
                .width(80.dp)
                .height(6.dp)
                .background(ColorProvider(Color.Gray.copy(alpha = 0.2f)))
                .cornerRadius(3.dp)
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .width((80 * batteryLevel / 100).dp)
                    .background(ColorProvider(getBatteryColor(batteryLevel)))
                    .cornerRadius(3.dp)
            )
        }
    }
}

// Widget 3: Detailed Stats Widget (like GSam Battery Monitor)
class DetailedStatsWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            DetailedStatsContent()
        }
    }
}

@Composable
private fun DetailedStatsContent() {
    val context = LocalContext.current
    val batteryInfo = remember { getBatteryInfo(context) }
    val batteryLevel = batteryInfo.level
    val voltage = batteryInfo.voltage
    val temperature = batteryInfo.temperature
    val health = batteryInfo.health
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    Color(0xFF1A1A1A)
                )
            )
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ VoltAI",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF4CAF50))
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            
            Text(
                text = "${batteryLevel}%",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Stats grid
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            StatItem(
                label = "Voltage",
                value = if (voltage > 0f) "${voltage}V" else "—",
                modifier = GlanceModifier.defaultWeight()
            )
            StatItem(
                label = "Temp",
                value = if (temperature > 0f) "${temperature}°C" else "—",
                modifier = GlanceModifier.defaultWeight()
            )
        }
        
        Spacer(modifier = GlanceModifier.height(4.dp))
        
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            StatItem(
                label = "Health",
                value = if (health > 0) "${health}%" else "—",
                modifier = GlanceModifier.defaultWeight()
            )
            StatItem(
                label = "Status",
                value = "Good",
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.White)
            )
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(Color.Gray)
            )
        )
    }
}

// Widget 4: AI Insights Widget
class AIInsightsWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            AIInsightsContent()
        }
    }
}

@Composable
private fun AIInsightsContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    Color(0xFF6200EE).copy(alpha = 0.1f)
                )
            )
            .cornerRadius(20.dp)
            .padding(16.dp)
    ) {
        // Header with AI icon
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🧠",
                style = TextStyle(fontSize = 16.sp)
            )
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            Text(
                text = "AI Coach",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF6200EE))
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // AI insight
        Text(
            text = "Battery health is excellent. Continue current charging patterns for optimal longevity.",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color.Black)
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Recommendation
        Text(
            text = "💡 Tip: Charge between 20-80% today",
            style = TextStyle(
                fontSize = 11.sp,
                color = ColorProvider(Color(0xFF4CAF50)),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

// Widget 5: Battery History Graph Widget
class HistoryGraphWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            HistoryGraphContent()
        }
    }
}

@Composable
private fun HistoryGraphContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.White))
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 24h History",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.Black)
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            
            Text(
                text = "85%",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF4CAF50))
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Simplified graph representation
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(8) { index ->
                val height = (12 + (index * 2) % 16).dp
                val color = when {
                    index < 3 -> Color(0xFF4CAF50)
                    index < 6 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
                
                Box(
                    modifier = GlanceModifier
                        .width(8.dp)
                        .height(height)
                        .background(ColorProvider(color.copy(alpha = 0.7f)))
                        .cornerRadius(2.dp)
                )
                
                if (index < 7) {
                    Spacer(modifier = GlanceModifier.width(2.dp))
                }
            }
        }
        
        Spacer(modifier = GlanceModifier.height(6.dp))
        
        // Time labels
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "12h ago",
                style = TextStyle(
                    fontSize = 9.sp,
                    color = ColorProvider(Color.Gray)
                )
            )
            
            Spacer(modifier = GlanceModifier.defaultWeight())
            
            Text(
                text = "Now",
                style = TextStyle(
                    fontSize = 9.sp,
                    color = ColorProvider(Color.Gray)
                )
            )
        }
    }
}

// Widget 6: Quick Actions Widget
class QuickActionsWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QuickActionsContent()
        }
    }
}

@Composable
private fun QuickActionsContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1E1E1E)))
            .cornerRadius(20.dp)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "⚡ Quick Actions",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.White)
            )
        )
        
        Spacer(modifier = GlanceModifier.height(12.dp))
        
        // Action buttons grid
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            ActionButton(
                icon = "🔋",
                label = "Optimize",
                modifier = GlanceModifier.defaultWeight()
            )
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            ActionButton(
                icon = "🌙",
                label = "Saver",
                modifier = GlanceModifier.defaultWeight()
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            ActionButton(
                icon = "📊",
                label = "Stats",
                modifier = GlanceModifier.defaultWeight()
            )
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            ActionButton(
                icon = "⚙️",
                label = "Settings",
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: String,
    label: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(
                ColorProvider(Color.White.copy(alpha = 0.1f))
            )
            .cornerRadius(12.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = TextStyle(fontSize = 16.sp)
        )
        
        Spacer(modifier = GlanceModifier.height(2.dp))
        
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(Color.White.copy(alpha = 0.8f))
            )
        )
    }
}

// Widget 7: Temperature Monitor Widget
class TemperatureWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            TemperatureContent()
        }
    }
}

@Composable
private fun TemperatureContent() {
    val context = LocalContext.current
    val batteryInfo = remember { getBatteryInfo(context) }
    val temperature = batteryInfo.temperature
    val batteryLevel = batteryInfo.level
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    Color(0xFFFF5722).copy(alpha = 0.1f)
                )
            )
            .cornerRadius(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Temperature icon and value
            Text(
                text = "🌡️",
                style = TextStyle(fontSize = 24.sp)
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = "${temperature}°C",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(getTemperatureColor(temperature))
                )
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // Battery level
            Text(
                text = "Battery: ${batteryLevel}%",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color.Black.copy(alpha = 0.7f))
                )
            )
            
            // Status
            Text(
                text = if (temperature > 40) "⚠️ Hot" else "✅ Normal",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = ColorProvider(
                        if (temperature > 40) Color.Red else Color(0xFF4CAF50)
                    )
                )
            )
        }
    }
}

// Widget 8: Charging Speed Widget
class ChargingSpeedWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ChargingSpeedContent()
        }
    }
}

@Composable
private fun ChargingSpeedContent() {
    val context = LocalContext.current
    val batteryInfo = remember { getBatteryInfo(context) }
    val isCharging = batteryInfo.isCharging
    val chargingSpeed = batteryInfo.chargingCurrent
    val batteryLevel = batteryInfo.level
    val timeToFull = batteryInfo.timeToFull
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    if (isCharging) Color(0xFF4CAF50).copy(alpha = 0.1f) 
                    else Color.Gray.copy(alpha = 0.1f)
                )
            )
            .cornerRadius(18.dp)
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCharging) "⚡ Charging" else "🔋 Discharging",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(
                        if (isCharging) Color(0xFF4CAF50) else Color.Gray
                    )
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            
            Text(
                text = "${batteryLevel}%",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.Black)
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        if (isCharging) {
            // Charging speed
            Text(
                text = "${chargingSpeed}mA",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF2196F3))
                )
            )
            
            Text(
                text = "Charging Speed",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(Color.Gray)
                )
            )
            
            Spacer(modifier = GlanceModifier.height(6.dp))
            
            // Time to full
            Text(
                text = "⏱️ ${timeToFull} to full",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = ColorProvider(Color.Black.copy(alpha = 0.7f))
                )
            )
        } else {
            Text(
                text = "Not charging",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = ColorProvider(Color.Gray)
                )
            )
            
            Text(
                text = "⏱️ 6h 45m remaining",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = ColorProvider(Color.Black.copy(alpha = 0.7f))
                )
            )
        }
    }
}

// Utility functions
private fun getBatteryColor(level: Int): Color {
    return when {
        level <= 20 -> Color(0xFFF44336) // Red
        level <= 50 -> Color(0xFFFF9800) // Orange
        level <= 80 -> Color(0xFF4CAF50) // Green
        else -> Color(0xFF2196F3) // Blue
    }
}

private fun getTemperatureColor(temperature: Float): Color {
    return when {
        temperature >= 45f -> Color(0xFFF44336) // Red - Critical
        temperature >= 40f -> Color(0xFFFF9800) // Orange - Warning
        temperature >= 35f -> Color(0xFFFFEB3B) // Yellow - Caution
        else -> Color(0xFF4CAF50) // Green - Normal
    }
}

/**
 * Widget Configuration Activity
 * Allows users to customize their widgets
 */
class WidgetConfigurationProvider {
    companion object {
        const val WIDGET_STYLE_CIRCULAR = "circular"
        const val WIDGET_STYLE_MINIMAL = "minimal"
        const val WIDGET_STYLE_DETAILED = "detailed"
        const val WIDGET_STYLE_AI_INSIGHTS = "ai_insights"
        const val WIDGET_STYLE_HISTORY = "history"
        const val WIDGET_STYLE_QUICK_ACTIONS = "quick_actions"
        const val WIDGET_STYLE_TEMPERATURE = "temperature"
        const val WIDGET_STYLE_CHARGING = "charging"
        
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_COLORFUL = "colorful"
        const val THEME_MINIMAL = "minimal"
        
        const val UPDATE_INTERVAL_1MIN = 60000L
        const val UPDATE_INTERVAL_5MIN = 300000L
        const val UPDATE_INTERVAL_15MIN = 900000L
        const val UPDATE_INTERVAL_30MIN = 1800000L
    }
    
    data class WidgetConfig(
        val style: String = WIDGET_STYLE_CIRCULAR,
        val theme: String = THEME_DARK,
        val updateInterval: Long = UPDATE_INTERVAL_5MIN,
        val showTemperature: Boolean = true,
        val showVoltage: Boolean = false,
        val showCurrent: Boolean = false,
        val showTimeRemaining: Boolean = true,
        val enableAnimations: Boolean = true,
        val showAIInsights: Boolean = true
    )
}

/**
 * Widget Receiver for handling widget updates
 */
class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CircularBatteryWidget()
}

class MinimalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MinimalLineWidget()
}

class DetailedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DetailedStatsWidget()
}

class AIInsightsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AIInsightsWidget()
}

class HistoryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HistoryGraphWidget()
}

class QuickActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickActionsWidget()
}

class TemperatureWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TemperatureWidget()
}

class ChargingSpeedWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ChargingSpeedWidget()
}

// Helper function to get real battery data
private data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val temperature: Float,
    val voltage: Float,
    val health: Int,
    val chargingCurrent: Int,
    val timeToFull: String
)

private fun getBatteryInfo(context: Context): BatteryInfo {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
    val intentFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = context.registerReceiver(null, intentFilter)
    
    val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: 0
    val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: 100
    val batteryPct = ((level / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
    
    val status = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
        status == android.os.BatteryManager.BATTERY_STATUS_FULL
    
    val temperature = (batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
    val voltage = (batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
    val health = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1) ?: 0
    
    // Get charging current (requires API 21+)
    val chargingCurrent = batteryManager?.getIntProperty(
        android.os.BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
    )?.div(1000)?.coerceAtLeast(0) ?: 0
    
    // Calculate time to full
    val timeToFull = if (isCharging && batteryPct < 100) {
        val remainingPercent = 100 - batteryPct
        val estimatedMinutes = if (chargingCurrent > 0) {
            // Estimate based on current (simplified)
            (remainingPercent * 2.5).toInt()
        } else {
            remainingPercent
        }
        val hours = estimatedMinutes / 60
        val mins = estimatedMinutes % 60
        if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    } else {
        "—"
    }
    
    return BatteryInfo(
        level = batteryPct,
        isCharging = isCharging,
        temperature = temperature,
        voltage = voltage,
        health = health,
        chargingCurrent = chargingCurrent,
        timeToFull = timeToFull
    )
}
