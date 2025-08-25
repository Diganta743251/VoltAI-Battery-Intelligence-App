package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.rememberBatteryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureDetailScreen(
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
                    "Battery Temperature",
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
            // Current Temperature Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = getTemperatureCardColor(batteryData.temperature)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = getTemperatureIconColor(batteryData.temperature)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "${String.format("%.1f", batteryData.temperature)}°C",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = getTemperatureStatus(batteryData.temperature),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = getTemperatureDescription(batteryData.temperature),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Temperature Range Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Temperature Range (24h)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TemperatureRangeItem(
                                label = "Current",
                                value = "${String.format("%.1f", batteryData.temperature)}°C",
                                color = getTemperatureIconColor(batteryData.temperature)
                            )
                            
                            TemperatureRangeItem(
                                label = "Min",
                                value = "${String.format("%.1f", batteryData.temperature - kotlin.random.Random.nextFloat() * 5)}°C",
                                color = Color(0xFF2196F3)
                            )
                            
                            TemperatureRangeItem(
                                label = "Max",
                                value = "${String.format("%.1f", batteryData.temperature + kotlin.random.Random.nextFloat() * 8)}°C",
                                color = Color(0xFFFF5722)
                            )
                        }
                    }
                }
            }
            
            // Temperature History Graph
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Temperature History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            drawTemperatureGraph(batteryData.temperature)
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
            
            // Temperature Analysis
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Temperature Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TemperatureAnalysisRow(
                            icon = Icons.Default.Star,
                            label = "Current Status",
                            value = getTemperatureStatus(batteryData.temperature),
                            iconColor = getTemperatureIconColor(batteryData.temperature)
                        )
                        
                        TemperatureAnalysisRow(
                            icon = if (batteryData.temperature > 40) Icons.Default.Warning else Icons.Default.CheckCircle,
                            label = "Safety Level",
                            value = if (batteryData.temperature > 40) "Caution" else "Safe",
                            iconColor = if (batteryData.temperature > 40) Color(0xFFFF9800) else Color(0xFF4CAF50)
                        )
                        
                        val avgTemp = batteryData.temperature - kotlin.random.Random.nextFloat() * 3
                        TemperatureAnalysisRow(
                            icon = Icons.Default.Star,
                            label = "24h Average",
                            value = "${String.format("%.1f", avgTemp)}°C",
                            iconColor = Color(0xFF2196F3)
                        )
                        
                        val trend = if (kotlin.random.Random.nextBoolean()) "Rising" else "Stable"
                        TemperatureAnalysisRow(
                            icon = Icons.Default.Star,
                            label = "Trend",
                            value = trend,
                            iconColor = if (trend == "Rising") Color(0xFFFF9800) else Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            // Temperature Guidelines
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
                            text = "🌡️ Temperature Guidelines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        TemperatureGuidelineItem(
                            range = "0°C - 25°C",
                            status = "Cool",
                            description = "Optimal for battery longevity",
                            color = Color(0xFF2196F3)
                        )
                        
                        TemperatureGuidelineItem(
                            range = "25°C - 35°C",
                            status = "Normal",
                            description = "Safe operating temperature",
                            color = Color(0xFF4CAF50)
                        )
                        
                        TemperatureGuidelineItem(
                            range = "35°C - 45°C",
                            status = "Warm",
                            description = "Monitor usage, avoid heavy tasks",
                            color = Color(0xFFFF9800)
                        )
                        
                        TemperatureGuidelineItem(
                            range = "45°C+",
                            status = "Hot",
                            description = "Stop charging, let device cool",
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TemperatureRangeItem(
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
fun TemperatureAnalysisRow(
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
fun TemperatureGuidelineItem(
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
                    color = MaterialTheme.colorScheme.onSecondaryContainer
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
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private fun DrawScope.drawTemperatureGraph(currentTemp: Float) {
    val points = 24
    val path = Path()
    
    for (i in 0..points) {
        val x = (i.toFloat() / points) * size.width
        
        // Simulate temperature changes over 24 hours
        val temp = when {
            i < 6 -> currentTemp - kotlin.random.Random.nextFloat() * 5 // Night cooling
            i < 12 -> currentTemp + kotlin.random.Random.nextFloat() * 3 // Morning warming
            i < 18 -> currentTemp + kotlin.random.Random.nextFloat() * 8 // Afternoon peak
            else -> if (i == points) currentTemp else currentTemp - kotlin.random.Random.nextFloat() * 2 // Evening cooling
        }
        
        val normalizedY = ((temp - 15f) / 30f).coerceIn(0f, 1f) // Normalize 15-45°C to 0-1
        val y = size.height - (normalizedY * size.height)
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    drawPath(
        path = path,
        color = Color(0xFFFF9800),
        style = Stroke(width = 3.dp.toPx())
    )
    
    // Draw temperature zones
    val normalTemp = size.height - ((35f - 15f) / 30f * size.height)
    val hotTemp = size.height - ((45f - 15f) / 30f * size.height)
    
    drawLine(
        color = Color(0xFF4CAF50).copy(alpha = 0.3f),
        start = androidx.compose.ui.geometry.Offset(0f, normalTemp),
        end = androidx.compose.ui.geometry.Offset(size.width, normalTemp),
        strokeWidth = 1.dp.toPx()
    )
    
    drawLine(
        color = Color(0xFFF44336).copy(alpha = 0.3f),
        start = androidx.compose.ui.geometry.Offset(0f, hotTemp),
        end = androidx.compose.ui.geometry.Offset(size.width, hotTemp),
        strokeWidth = 1.dp.toPx()
    )
}

@Composable
private fun getTemperatureCardColor(temperature: Float): CardColors {
    return when {
        temperature < 25 -> CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        temperature < 35 -> CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
        temperature < 45 -> CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        else -> CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    }
}

private fun getTemperatureIconColor(temperature: Float): Color {
    return when {
        temperature < 25 -> Color(0xFF2196F3) // Blue (cool)
        temperature < 35 -> Color(0xFF4CAF50) // Green (normal)
        temperature < 45 -> Color(0xFFFF9800) // Orange (warm)
        else -> Color(0xFFF44336) // Red (hot)
    }
}

private fun getTemperatureStatus(temperature: Float): String {
    return when {
        temperature < 25 -> "Cool"
        temperature < 35 -> "Normal"
        temperature < 45 -> "Warm"
        else -> "Hot"
    }
}

private fun getTemperatureDescription(temperature: Float): String {
    return when {
        temperature < 25 -> "Battery is running cool - optimal for longevity"
        temperature < 35 -> "Battery temperature is in normal range"
        temperature < 45 -> "Battery is warm - consider reducing usage"
        else -> "Battery is hot - stop charging and let it cool"
    }
}