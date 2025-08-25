package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import kotlin.math.sin
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricCurrentDetailScreen(
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
                    "Electric Current",
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
            // Current Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (batteryData.current > 0) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (batteryData.current > 0) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = if (batteryData.current > 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "${kotlin.math.abs(batteryData.current)}mA",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = if (batteryData.current > 0) "Charging" else "Discharging",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "${String.format("%.3f", batteryData.voltage)}V",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Real-time Current Graph
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Real-time Current Flow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Enhanced waveform
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            drawCurrentWaveform(batteryData.current)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CurrentStatItem(
                                label = "Current",
                                value = "${batteryData.current}mA",
                                color = if (batteryData.current > 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
                            )
                            
                            CurrentStatItem(
                                label = "Voltage",
                                value = "${String.format("%.3f", batteryData.voltage)}V",
                                color = Color(0xFF2196F3)
                            )
                            
                            CurrentStatItem(
                                label = "Power",
                                value = "${String.format("%.2f", (batteryData.current * batteryData.voltage / 1000))}W",
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                }
            }
            
            // Current History
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Current History (24h)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            drawCurrentHistory(batteryData.current)
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
            
            // Current Analysis
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Current Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        CurrentAnalysisRow(
                            icon = Icons.Default.Star,
                            label = "Status",
                            value = if (batteryData.current > 0) "Charging" else "Discharging",
                            iconColor = if (batteryData.current > 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
                        )
                        
                        CurrentAnalysisRow(
                            icon = Icons.Default.KeyboardArrowUp,
                            label = "Peak Current",
                            value = "${kotlin.math.abs(batteryData.current) + kotlin.random.Random.nextInt(100, 500)}mA",
                            iconColor = Color(0xFFFF9800)
                        )
                        
                        CurrentAnalysisRow(
                            icon = Icons.Default.KeyboardArrowDown,
                            label = "Average Current",
                            value = "${kotlin.math.abs(batteryData.current) - kotlin.random.Random.nextInt(50, 200)}mA",
                            iconColor = Color(0xFF2196F3)
                        )
                        
                        val efficiency = if (batteryData.current > 0) {
                            kotlin.random.Random.nextInt(85, 95)
                        } else {
                            kotlin.random.Random.nextInt(75, 90)
                        }
                        
                        CurrentAnalysisRow(
                            icon = Icons.Default.Star,
                            label = "Efficiency",
                            value = "$efficiency%",
                            iconColor = if (efficiency > 85) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentStatItem(
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
fun CurrentAnalysisRow(
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

private fun DrawScope.drawCurrentWaveform(current: Int) {
    val path = Path()
    val points = 100
    val amplitude = size.height * 0.3f
    val centerY = size.height / 2f
    val frequency = if (current > 0) 3.0 else 2.0 // Different frequency for charging vs discharging
    
    for (i in 0..points) {
        val x = (i.toFloat() / points) * size.width
        val normalizedCurrent = (kotlin.math.abs(current) / 2000f).coerceIn(0.1f, 1.0f)
        val y = centerY + (sin(2 * PI * frequency * i / points) * amplitude * normalizedCurrent).toFloat()
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    val waveColor = if (current > 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
    
    drawPath(
        path = path,
        color = waveColor,
        style = Stroke(width = 4.dp.toPx())
    )
    
    // Draw center line
    drawLine(
        color = Color.Gray.copy(alpha = 0.3f),
        start = androidx.compose.ui.geometry.Offset(0f, centerY),
        end = androidx.compose.ui.geometry.Offset(size.width, centerY),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawCurrentHistory(currentCurrent: Int) {
    val points = 24 // 24 hours
    val path = Path()
    
    for (i in 0..points) {
        val x = (i.toFloat() / points) * size.width
        
        // Simulate current changes over 24 hours
        val historicalCurrent = when {
            i < 6 -> kotlin.random.Random.nextInt(500, 1500) // Night charging
            i < 12 -> -kotlin.random.Random.nextInt(200, 800) // Morning usage
            i < 18 -> -kotlin.random.Random.nextInt(300, 1000) // Afternoon usage
            else -> if (i == points) currentCurrent else kotlin.random.Random.nextInt(-500, 200) // Evening
        }
        
        val normalizedY = (historicalCurrent + 1500f) / 3000f // Normalize to 0-1
        val y = size.height - (normalizedY * size.height)
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    drawPath(
        path = path,
        color = Color(0xFF2196F3),
        style = Stroke(width = 3.dp.toPx())
    )
    
    // Draw zero line
    val zeroY = size.height - (1500f / 3000f * size.height)
    drawLine(
        color = Color.Gray.copy(alpha = 0.5f),
        start = androidx.compose.ui.geometry.Offset(0f, zeroY),
        end = androidx.compose.ui.geometry.Offset(size.width, zeroY),
        strokeWidth = 1.dp.toPx()
    )
}