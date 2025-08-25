package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.rememberBatteryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryInfoDetailScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Battery Info",
                    fontWeight = FontWeight.Bold
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Battery Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${batteryData.batteryLevel}%",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = batteryData.chargingStatus,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = batteryData.remainingTime,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Battery Level Visualization
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Battery Level History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Battery level bars (enhanced version)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            drawBatteryHistoryBars(batteryData.batteryLevel)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "24h ago",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Now",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Detailed Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Battery Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        BatteryDetailRow(
                            icon = Icons.Default.Star,
                            label = "Current Level",
                            value = "${batteryData.batteryLevel}%",
                            iconColor = getBatteryColor(batteryData.batteryLevel)
                        )
                        
                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Status",
                            value = batteryData.chargingStatus,
                            iconColor = if (batteryData.isCharging) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                        
                        BatteryDetailRow(
                            icon = Icons.Default.Warning,
                            label = "Health",
                            value = "${batteryData.health} (${batteryData.healthPercentage}%)",
                            iconColor = getHealthColor(batteryData.healthPercentage)
                        )
                        
                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Temperature",
                            value = "${String.format("%.1f", batteryData.temperature)}°C",
                            iconColor = getTemperatureColor(batteryData.temperature)
                        )
                        
                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Voltage",
                            value = "${String.format("%.3f", batteryData.voltage)}V",
                            iconColor = Color(0xFF2196F3)
                        )
                        
                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Current",
                            value = "${batteryData.current}mA",
                            iconColor = if (batteryData.current > 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
                        )
                        
                        BatteryDetailRow(
                            icon = Icons.Default.Info,
                            label = "Capacity",
                            value = batteryData.capacity,
                            iconColor = Color(0xFF9C27B0)
                        )
                    }
                }
            }
            
            // Usage Statistics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Usage Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            UsageStatItem(
                                label = "Screen On",
                                value = batteryData.screenOnTime,
                                color = Color(0xFFFF9800)
                            )
                            
                            UsageStatItem(
                                label = "Screen Off", 
                                value = batteryData.screenOffTime,
                                color = Color(0xFF9C27B0)
                            )
                            
                            UsageStatItem(
                                label = "Battery Used",
                                value = batteryData.batteryUsed,
                                color = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun UsageStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun DrawScope.drawBatteryHistoryBars(currentLevel: Int) {
    val barCount = 24
    val barWidth = size.width / (barCount + (barCount - 1) * 0.1f)
    val barSpacing = barWidth * 0.1f
    val maxBarHeight = size.height
    
    for (i in 0 until barCount) {
        val x = i * (barWidth + barSpacing)
        
        // Simulate battery level changes over 24 hours
        val hourlyLevel = when {
            i < 6 -> currentLevel + kotlin.random.Random.nextInt(-5, 15) // Night charging
            i < 12 -> currentLevel + kotlin.random.Random.nextInt(-20, 5) // Morning usage
            i < 18 -> currentLevel + kotlin.random.Random.nextInt(-15, 10) // Afternoon
            else -> currentLevel + kotlin.random.Random.nextInt(-10, 5) // Evening
        }.coerceIn(0, 100)
        
        val barHeight = (hourlyLevel / 100f) * maxBarHeight
        val y = maxBarHeight - barHeight
        
        val barColor = getBatteryColor(hourlyLevel)
        
        drawRect(
            color = barColor,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
        )
    }
}

private fun getBatteryColor(level: Int): Color {
    return when {
        level > 80 -> Color(0xFF4CAF50) // Green
        level > 50 -> Color(0xFF2196F3) // Blue  
        level > 20 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun getHealthColor(healthPercentage: Int): Color {
    return when {
        healthPercentage > 80 -> Color(0xFF4CAF50) // Green
        healthPercentage > 60 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun getTemperatureColor(temperature: Float): Color {
    return when {
        temperature < 25 -> Color(0xFF2196F3) // Blue (cool)
        temperature < 35 -> Color(0xFF4CAF50) // Green (normal)
        temperature < 45 -> Color(0xFFFF9800) // Orange (warm)
        else -> Color(0xFFF44336) // Red (hot)
    }
}