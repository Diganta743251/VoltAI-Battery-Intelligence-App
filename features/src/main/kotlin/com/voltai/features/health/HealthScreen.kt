package com.voltai.features.health

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    onNavigateBack: () -> Unit = {}
) {
    var batteryHealth by remember { mutableStateOf(95) }
    var cycleCount by remember { mutableStateOf(127) }
    var designCapacity by remember { mutableStateOf(4000) }
    var currentCapacity by remember { mutableStateOf(3800) }
    
    // Animation for health percentage
    val animatedHealth by animateIntAsState(
        targetValue = batteryHealth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "health_animation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Battery Health",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    BackArrowIcon(
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Main Health Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Health Icon
                        HealthIcon(
                            modifier = Modifier.size(48.dp),
                            tint = when {
                                animatedHealth >= 90 -> Color(0xFF4CAF50)
                                animatedHealth >= 70 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                        
                        Text(
                            text = "Overall Health",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${animatedHealth}%",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                animatedHealth >= 90 -> Color(0xFF4CAF50)
                                animatedHealth >= 70 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                        
                        Text(
                            text = when {
                                animatedHealth >= 90 -> "Excellent"
                                animatedHealth >= 80 -> "Good"
                                animatedHealth >= 70 -> "Fair"
                                animatedHealth >= 60 -> "Poor"
                                else -> "Replace Soon"
                            },
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                // Capacity Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BatteryIcon(
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Battery Capacity",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Current",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${currentCapacity} mAh",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Design",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${designCapacity} mAh",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Capacity Progress Bar
                        LinearProgressIndicator(
                            progress = currentCapacity.toFloat() / designCapacity.toFloat(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
            
            item {
                // Cycle Count Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Using a simple cycle icon
                            val primary = MaterialTheme.colorScheme.primary
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.size(24.dp)
                            ) {
                                val strokeWidth = 2.dp.toPx()
                                drawArc(
                                    color = primary,
                                    startAngle = 0f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = strokeWidth,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                                // Arrow
                                drawLine(
                                    color = primary,
                                    start = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f),
                                    end = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.1f),
                                    strokeWidth = strokeWidth,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                drawLine(
                                    color = primary,
                                    start = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f),
                                    end = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.3f),
                                    strokeWidth = strokeWidth,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                            Text(
                                text = "Charge Cycles",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Text(
                            text = "$cycleCount cycles",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = when {
                                cycleCount < 100 -> "Like new"
                                cycleCount < 300 -> "Excellent condition"
                                cycleCount < 500 -> "Good condition"
                                cycleCount < 800 -> "Fair condition"
                                else -> "Consider replacement"
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                // Health Tips Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LightbulbIcon(
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Health Tips",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        val tips = listOf(
                            "Avoid extreme temperatures (below 0°C or above 35°C)",
                            "Don't let battery drain completely (keep above 20%)",
                            "Unplug when fully charged to prevent overcharging",
                            "Use original or certified chargers",
                            "Enable battery optimization for unused apps"
                        )
                        
                        tips.forEach { tip ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "•",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = tip,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}