package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.rememberBatteryData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDetailScreen(
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
                    "Battery Health",
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
            // Health Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = getHealthCardColor(batteryData.healthPercentage)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Circular health indicator
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                drawHealthCircle(batteryData.healthPercentage)
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${batteryData.healthPercentage}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = batteryData.health,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = getHealthDescription(batteryData.healthPercentage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Capacity Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Battery Capacity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val capacityParts = batteryData.capacity.split("/")
                        val currentCapacity = capacityParts[0].replace(" mAh", "").toIntOrNull() ?: 2500
                        val designCapacity = capacityParts[1].replace(" mAh", "").toIntOrNull() ?: 4000
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CapacityItem(
                                label = "Current",
                                value = "${currentCapacity} mAh",
                                color = getHealthColor(batteryData.healthPercentage)
                            )
                            
                            CapacityItem(
                                label = "Design",
                                value = "${designCapacity} mAh",
                                color = Color(0xFF2196F3)
                            )
                            
                            CapacityItem(
                                label = "Lost",
                                value = "${designCapacity - currentCapacity} mAh",
                                color = Color(0xFFFF5722)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Capacity bar
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                        ) {
                            drawCapacityBar(currentCapacity, designCapacity)
                        }
                    }
                }
            }
            
            // Health Trends
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Health Trend (6 Months)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            drawHealthTrend(batteryData.healthPercentage)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "6 months ago",
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
            
            // Health Analysis
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Health Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HealthAnalysisRow(
                            icon = Icons.Default.Favorite,
                            label = "Overall Health",
                            value = "${batteryData.healthPercentage}% (${batteryData.health})",
                            iconColor = getHealthColor(batteryData.healthPercentage)
                        )
                        
                        val chargeCycles = kotlin.random.Random.nextInt(200, 800)
                        HealthAnalysisRow(
                            icon = Icons.Default.Star,
                            label = "Charge Cycles",
                            value = "$chargeCycles cycles",
                            iconColor = if (chargeCycles > 500) Color(0xFFFF9800) else Color(0xFF4CAF50)
                        )
                        
                        val degradation = 100 - batteryData.healthPercentage
                        HealthAnalysisRow(
                            icon = Icons.Default.KeyboardArrowDown,
                            label = "Degradation",
                            value = "$degradation%",
                            iconColor = if (degradation > 20) Color(0xFFFF5722) else Color(0xFF4CAF50)
                        )
                        
                        val estimatedLife = kotlin.random.Random.nextInt(12, 36)
                        HealthAnalysisRow(
                            icon = Icons.Default.Info,
                            label = "Estimated Life",
                            value = "$estimatedLife months",
                            iconColor = if (estimatedLife < 18) Color(0xFFFF9800) else Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            // Health Tips
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
                            text = "💡 Battery Health Tips",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val tips = listOf(
                            "Keep battery level between 20-80% for optimal health",
                            "Avoid extreme temperatures (below 0°C or above 45°C)",
                            "Use original charger and avoid fast charging when possible",
                            "Don't leave battery at 100% or 0% for extended periods",
                            "Calibrate battery monthly by full discharge and charge",
                            "Enable battery optimization for unused apps"
                        )
                        
                        tips.forEach { tip ->
                            Text(
                                text = "• $tip",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Health Status Indicators
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Health Status Guide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HealthStatusItem(
                            range = "90-100%",
                            status = "Excellent",
                            description = "Battery is in excellent condition",
                            color = Color(0xFF4CAF50)
                        )
                        
                        HealthStatusItem(
                            range = "80-89%",
                            status = "Good",
                            description = "Battery health is good, normal usage",
                            color = Color(0xFF8BC34A)
                        )
                        
                        HealthStatusItem(
                            range = "70-79%",
                            status = "Fair",
                            description = "Consider battery optimization",
                            color = Color(0xFFFF9800)
                        )
                        
                        HealthStatusItem(
                            range = "60-69%",
                            status = "Poor",
                            description = "Battery replacement recommended",
                            color = Color(0xFFFF5722)
                        )
                        
                        HealthStatusItem(
                            range = "Below 60%",
                            status = "Critical",
                            description = "Battery needs immediate replacement",
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CapacityItem(
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

@Composable
fun HealthAnalysisRow(
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
fun HealthStatusItem(
    range: String,
    status: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier.size(12.dp)
        ) {
            drawCircle(color = color)
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row {
                Text(
                    text = range,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun DrawScope.drawHealthCircle(healthPercentage: Int) {
    val strokeWidth = 12.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    
    // Background circle
    drawCircle(
        color = Color.Gray.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth)
    )
    
    // Health arc
    val sweepAngle = (healthPercentage / 100f) * 360f
    val healthColor = getHealthColor(healthPercentage)
    
    drawArc(
        color = healthColor,
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(width = strokeWidth),
        topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
}

private fun DrawScope.drawCapacityBar(currentCapacity: Int, designCapacity: Int) {
    val barHeight = size.height
    val currentWidth = (currentCapacity.toFloat() / designCapacity) * size.width
    
    // Background bar (design capacity)
    drawRect(
        color = Color.Gray.copy(alpha = 0.2f),
        topLeft = androidx.compose.ui.geometry.Offset.Zero,
        size = androidx.compose.ui.geometry.Size(size.width, barHeight)
    )
    
    // Current capacity bar
    drawRect(
        color = getHealthColor((currentCapacity.toFloat() / designCapacity * 100).toInt()),
        topLeft = androidx.compose.ui.geometry.Offset.Zero,
        size = androidx.compose.ui.geometry.Size(currentWidth, barHeight)
    )
}

private fun DrawScope.drawHealthTrend(currentHealth: Int) {
    val points = 6 // 6 months
    val path = androidx.compose.ui.graphics.Path()
    
    for (i in 0..points) {
        val x = (i.toFloat() / points) * size.width
        
        // Simulate health degradation over time
        val monthsAgo = points - i
        val healthAtTime = (currentHealth + monthsAgo * kotlin.random.Random.nextInt(1, 4)).coerceAtMost(100)
        
        val normalizedY = (healthAtTime / 100f)
        val y = size.height - (normalizedY * size.height)
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    drawPath(
        path = path,
        color = Color(0xFF4CAF50),
        style = Stroke(width = 3.dp.toPx())
    )
}

@Composable
private fun getHealthCardColor(healthPercentage: Int): CardColors {
    return when {
        healthPercentage >= 90 -> CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
        healthPercentage >= 80 -> CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
        healthPercentage >= 70 -> CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        healthPercentage >= 60 -> CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2))
        else -> CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    }
}

private fun getHealthColor(healthPercentage: Int): Color {
    return when {
        healthPercentage >= 90 -> Color(0xFF4CAF50) // Green
        healthPercentage >= 80 -> Color(0xFF8BC34A) // Light Green
        healthPercentage >= 70 -> Color(0xFFFF9800) // Orange
        healthPercentage >= 60 -> Color(0xFFFF5722) // Deep Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun getHealthDescription(healthPercentage: Int): String {
    return when {
        healthPercentage >= 90 -> "Your battery is in excellent condition"
        healthPercentage >= 80 -> "Battery health is good for normal usage"
        healthPercentage >= 70 -> "Consider optimizing battery usage"
        healthPercentage >= 60 -> "Battery replacement may be needed soon"
        else -> "Battery needs immediate replacement"
    }
}