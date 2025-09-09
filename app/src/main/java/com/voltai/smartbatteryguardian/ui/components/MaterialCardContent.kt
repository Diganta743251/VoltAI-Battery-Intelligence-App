package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.ui.home.CardType

/**
 * Simplified Material Design 3 compliant card content implementation.
 * 
 * Removes excessive animations, custom gradients, and hardcoded colors,
 * replacing them with semantic Material 3 tokens.
 */
@Composable
fun MaterialCardContent(
    cardType: CardType,
    batteryData: BatteryData,
    isEditMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (isEditMode) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        } else {
            Color.Transparent
        }
    ) {
        when (cardType) {
            CardType.BATTERY_LEVEL -> BatteryLevelCard(batteryData)
            CardType.CHARGING_STATUS -> ChargingStatusCard(batteryData)
            CardType.TEMPERATURE -> TemperatureCard(batteryData)
            CardType.HEALTH -> HealthCard(batteryData)
            CardType.CURRENT_VOLTAGE -> CurrentVoltageCard(batteryData)
            CardType.SCREEN_TIME -> ScreenTimeCard(batteryData)
            CardType.APP_USAGE -> AppUsageCard(batteryData)
            CardType.AI_INSIGHTS -> AIInsightsCard(batteryData)
            CardType.POWER_MODES -> PowerModesCard(batteryData)
            CardType.QUICK_ACTIONS -> QuickActionsCard(batteryData)
            CardType.HISTORY -> HistoryCard(batteryData)
            CardType.OVERLAYS -> OverlaysCard(batteryData)
            // Premium Cards
            CardType.CHARGING_ANALYTICS -> PremiumCard("Charging Analytics", "⚡")
            CardType.CHEMISTRY_LAB -> PremiumCard("Chemistry Lab", "🔬")
            CardType.SYSTEM_OPTIMIZATION -> PremiumCard("System Optimizer", "🚀")
            CardType.PERFORMANCE_BENCHMARKS -> PremiumCard("Benchmarks", "📊")
            CardType.DATA_EXPORT_IMPORT -> PremiumCard("Data Backup", "💾")
            CardType.BATTERY_HEALTH_ANALYTICS -> PremiumCard("Health Analytics", "📈")
            CardType.SMART_CHARGING -> PremiumCard("Smart Charging", "🧠")
            CardType.BATTERY_DIAGNOSTICS -> PremiumCard("Diagnostics", "🔍")
            CardType.REAL_TIME_MONITORING -> PremiumCard("Real-time Monitor", "📡")
            CardType.BATTERY_OPTIMIZATION -> PremiumCard("Optimization", "⚡")
            else -> DefaultCard(cardType.name)
        }
    }
}

@Composable
private fun BatteryLevelCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${batteryData.batteryLevel}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = getBatteryLevelColor(batteryData.batteryLevel)
                )
                Text(
                    text = "Battery Level",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = if (batteryData.isCharging) Icons.Default.Phone else Icons.Default.Star,
                contentDescription = "Battery",
                tint = getBatteryLevelColor(batteryData.batteryLevel)
            )
        }
        
        // Simple progress indicator
        LinearProgressIndicator(
            progress = batteryData.batteryLevel / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = getBatteryLevelColor(batteryData.batteryLevel),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Text(
            text = batteryData.remainingTime,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChargingStatusCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Status",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (batteryData.isCharging) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Charging",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Text(
            text = batteryData.chargingStatus,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = if (batteryData.isCharging) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        
        Text(
            text = if (batteryData.isCharging) "Plugged In" else "On Battery",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TemperatureCard(batteryData: BatteryData) {
    val tempColor = when {
        batteryData.temperature > 45f -> MaterialTheme.colorScheme.error
        batteryData.temperature > 35f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Temperature",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${batteryData.temperature.toInt()}°C",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = tempColor
            )
            
            Text(
                text = when {
                    batteryData.temperature > 45f -> "Hot"
                    batteryData.temperature > 35f -> "Warm"
                    else -> "Normal"
                },
                style = MaterialTheme.typography.labelMedium,
                color = tempColor
            )
        }
        
        // Temperature indicator
        val progress = (batteryData.temperature / 60f).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = tempColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun HealthCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Battery Health",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(
                progress = batteryData.healthPercentage / 100f,
                modifier = Modifier.size(40.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Column {
                Text(
                    text = "${batteryData.healthPercentage}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Good",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Text(
            text = batteryData.capacity,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CurrentVoltageCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Current & Voltage",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${batteryData.current} mA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (batteryData.isCharging) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.tertiary
                }
            )
            Text(
                text = String.format("%.1f V", batteryData.voltage),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Simplified implementations for other card types
@Composable
private fun ScreenTimeCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Screen Time",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ScreenTimeItem("📱", "Screen On", batteryData.screenOnTime, MaterialTheme.colorScheme.primary)
            ScreenTimeItem("🔒", "Screen Off", batteryData.screenOffTime, MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Text(
            text = "Battery Used: ${batteryData.batteryUsed}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScreenTimeItem(icon: String, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.bodyMedium)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun AppUsageCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Top Apps",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppUsageItem("Chrome", "25%", MaterialTheme.colorScheme.error)
            AppUsageItem("YouTube", "18%", MaterialTheme.colorScheme.tertiary)
            AppUsageItem("Instagram", "12%", MaterialTheme.colorScheme.primary)
        }
        
        Text(
            text = "Last 24 hours",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppUsageItem(name: String, percentage: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percentage,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun AIInsightsCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "AI",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "AI Insights",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "Battery optimization active. 23% improvement detected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "View Details",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Text(
                text = "+23%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PowerModesCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Power Modes",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PowerModeItem("⚡", "Performance", true)
            PowerModeItem("🔋", "Battery Saver", false)
            PowerModeItem("🌙", "Ultra Saver", false)
        }
    }
}

@Composable
private fun PowerModeItem(icon: String, name: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.bodySmall)
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
        if (isActive) {
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(4.dp)
            ) {}
        }
    }
}

@Composable
private fun QuickActionsCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton("🔋", "Saver")
            QuickActionButton("📱", "Screen")
            QuickActionButton("🔊", "Sound")
        }
    }
}

@Composable
private fun QuickActionButton(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "History",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "History",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            HistorySessionItem("🔋", "Last Session", "2h 15m", MaterialTheme.colorScheme.primary)
            HistorySessionItem("⚡", "Charged", "+45%", MaterialTheme.colorScheme.secondary)
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "5 sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HistorySessionItem(icon: String, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.bodyMedium)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun OverlaysCard(batteryData: BatteryData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Overlays",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Overlays",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Overlay Control",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Configure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = "Screen overlays",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PremiumCard(title: String, icon: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Premium Feature",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = "Unlock with Premium",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun DefaultCard(cardName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cardName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getBatteryLevelColor(level: Int): androidx.compose.ui.graphics.Color {
    return when {
        level > 80 -> Color(0xFF4CAF50) // Keep this for now, will be replaced with theme tokens
        level > 50 -> Color(0xFFFF9800)
        level > 20 -> Color(0xFFFF5722)
        else -> Color(0xFFD32F2F)
    }
}

// Extension property for consistent spacing
val MaterialTheme.spacing: Spacing
    @Composable get() = Spacing()

data class Spacing(
    val extraSmall: androidx.compose.ui.unit.Dp = 4.dp,
    val small: androidx.compose.ui.unit.Dp = 8.dp,
    val medium: androidx.compose.ui.unit.Dp = 16.dp,
    val large: androidx.compose.ui.unit.Dp = 24.dp,
    val extraLarge: androidx.compose.ui.unit.Dp = 32.dp
)
