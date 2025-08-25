package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
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
import com.voltai.smartbatteryguardian.rememberBatteryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OngoingDetailScreen(
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
                    "Ongoing Usage",
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
            // Usage Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Current Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            UsageOverviewItem(
                                icon = Icons.Default.Star,
                                label = "Screen On",
                                value = batteryData.screenOnTime,
                                color = Color(0xFFFF9800)
                            )
                            
                            UsageOverviewItem(
                                icon = Icons.Default.Lock,
                                label = "Screen Off",
                                value = batteryData.screenOffTime,
                                color = Color(0xFF9C27B0)
                            )
                            
                            UsageOverviewItem(
                                icon = Icons.Default.Warning,
                                label = "Battery Used",
                                value = batteryData.batteryUsed,
                                color = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
            
            // Screen Time Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Screen Time Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Pie chart representation
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            drawScreenTimeChart(batteryData.screenOnTime, batteryData.screenOffTime)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ChartLegendItem(
                                color = Color(0xFFFF9800),
                                label = "Screen On",
                                percentage = calculateScreenOnPercentage(batteryData.screenOnTime, batteryData.screenOffTime)
                            )
                            
                            ChartLegendItem(
                                color = Color(0xFF9C27B0),
                                label = "Screen Off", 
                                percentage = 100 - calculateScreenOnPercentage(batteryData.screenOnTime, batteryData.screenOffTime)
                            )
                        }
                    }
                }
            }
            
            // Battery Drain Analysis
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Battery Drain Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val screenOnDrain = kotlin.random.Random.nextInt(15, 25)
                        val screenOffDrain = kotlin.random.Random.nextInt(3, 8)
                        
                        DrainAnalysisRow(
                            icon = Icons.Default.Star,
                            label = "Screen On Drain",
                            value = "~$screenOnDrain%/h",
                            description = "High usage detected",
                            iconColor = Color(0xFFFF9800),
                            isHigh = screenOnDrain > 20
                        )
                        
                        DrainAnalysisRow(
                            icon = Icons.Default.Lock,
                            label = "Screen Off Drain",
                            value = "~$screenOffDrain%/h",
                            description = if (screenOffDrain > 5) "Background apps active" else "Normal standby",
                            iconColor = Color(0xFF9C27B0),
                            isHigh = screenOffDrain > 5
                        )
                        
                        val totalDrain = (100 - batteryData.batteryLevel)
                        DrainAnalysisRow(
                            icon = Icons.Default.Warning,
                            label = "Total Drain",
                            value = "$totalDrain%",
                            description = "Since last charge",
                            iconColor = Color(0xFFF44336),
                            isHigh = totalDrain > 50
                        )
                    }
                }
            }
            
            // Usage Timeline
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Usage Timeline (Last 6 Hours)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            drawUsageTimeline()
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "6h ago",
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
            
            // Recommendations
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "💡 Recommendations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val recommendations = listOf(
                            "Reduce screen brightness to extend battery life",
                            "Close unused background apps",
                            "Enable battery saver mode when below 20%",
                            "Consider using dark mode to save power"
                        )
                        
                        recommendations.forEach { recommendation ->
                            Text(
                                text = "• $recommendation",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageOverviewItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ChartLegendItem(
    color: Color,
    label: String,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier.size(16.dp)
        ) {
            drawCircle(color = color)
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DrainAnalysisRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    description: String,
    iconColor: Color,
    isHigh: Boolean
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
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isHigh) Color(0xFFFF5722) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isHigh) Color(0xFFFF5722) else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun DrawScope.drawScreenTimeChart(screenOnTime: String, screenOffTime: String) {
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    val radius = kotlin.math.min(size.width, size.height) / 3
    
    val screenOnPercentage = calculateScreenOnPercentage(screenOnTime, screenOffTime)
    val screenOnAngle = (screenOnPercentage / 100f) * 360f
    
    // Draw screen off arc
    drawArc(
        color = Color(0xFF9C27B0),
        startAngle = screenOnAngle,
        sweepAngle = 360f - screenOnAngle,
        useCenter = true,
        topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
    
    // Draw screen on arc
    drawArc(
        color = Color(0xFFFF9800),
        startAngle = 0f,
        sweepAngle = screenOnAngle,
        useCenter = true,
        topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
}

private fun DrawScope.drawUsageTimeline() {
    val barCount = 24 // 6 hours * 4 (15-minute intervals)
    val barWidth = size.width / (barCount + (barCount - 1) * 0.1f)
    val barSpacing = barWidth * 0.1f
    val maxBarHeight = size.height
    
    for (i in 0 until barCount) {
        val x = i * (barWidth + barSpacing)
        
        // Simulate usage intensity (0 = screen off, 1 = screen on)
        val isScreenOn = kotlin.random.Random.nextBoolean()
        val intensity = if (isScreenOn) kotlin.random.Random.nextFloat() else 0.1f
        
        val barHeight = intensity * maxBarHeight
        val y = maxBarHeight - barHeight
        
        val barColor = if (isScreenOn) Color(0xFFFF9800) else Color(0xFF9C27B0).copy(alpha = 0.3f)
        
        drawRect(
            color = barColor,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
        )
    }
}

private fun calculateScreenOnPercentage(screenOnTime: String, screenOffTime: String): Int {
    // Simple calculation based on time strings
    // In a real app, you'd parse the actual time values
    return kotlin.random.Random.nextInt(20, 40) // Simulate 20-40% screen on time
}