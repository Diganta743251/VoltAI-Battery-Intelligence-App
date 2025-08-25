package com.voltai.features.temperature

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
fun TemperatureScreen(
    onNavigateBack: () -> Unit = {}
) {
    var currentTemp by remember { mutableStateOf(32.5f) }
    var maxTemp by remember { mutableStateOf(45.2f) }
    var minTemp by remember { mutableStateOf(18.1f) }
    var avgTemp by remember { mutableStateOf(28.7f) }
    
    // Animation for temperature
    val animatedTemp by animateFloatAsState(
        targetValue = currentTemp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "temp_animation"
    )
    
    // Temperature color based on value
    val tempColor = when {
        animatedTemp < 20 -> Color(0xFF2196F3) // Blue - Cold
        animatedTemp < 30 -> Color(0xFF4CAF50) // Green - Normal
        animatedTemp < 40 -> Color(0xFFFF9800) // Orange - Warm
        else -> Color(0xFFF44336) // Red - Hot
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Battery Temperature",
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
                // Main Temperature Card
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
                        // Temperature Icon
                        ThermometerIcon(
                            modifier = Modifier.size(48.dp),
                            tint = tempColor
                        )
                        
                        Text(
                            text = "Current Temperature",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${String.format("%.1f", animatedTemp)}°C",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = tempColor
                        )
                        
                        Text(
                            text = when {
                                animatedTemp < 20 -> "Cold"
                                animatedTemp < 30 -> "Normal"
                                animatedTemp < 40 -> "Warm"
                                animatedTemp < 45 -> "Hot"
                                else -> "Overheating"
                            },
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Temperature gauge
                        LinearProgressIndicator(
                            progress = (animatedTemp / 60f).coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = tempColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
            
            item {
                // Temperature Statistics
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
                        Text(
                            text = "Temperature Statistics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TemperatureStatItem(
                                label = "Max",
                                value = "${String.format("%.1f", maxTemp)}°C",
                                color = Color(0xFFF44336)
                            )
                            TemperatureStatItem(
                                label = "Avg",
                                value = "${String.format("%.1f", avgTemp)}°C",
                                color = MaterialTheme.colorScheme.primary
                            )
                            TemperatureStatItem(
                                label = "Min",
                                value = "${String.format("%.1f", minTemp)}°C",
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }
            
            item {
                // Temperature Impact Card
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
                            // Warning icon
                            val tertiary = MaterialTheme.colorScheme.tertiary
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.size(24.dp)
                            ) {
                                val strokeWidth = 2.dp.toPx()
                                // Triangle
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(size.width * 0.5f, size.height * 0.1f)
                                    lineTo(size.width * 0.1f, size.height * 0.9f)
                                    lineTo(size.width * 0.9f, size.height * 0.9f)
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = tertiary,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                )
                                // Exclamation mark
                                drawLine(
                                    color = tertiary,
                                    start = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.3f),
                                    end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.6f),
                                    strokeWidth = strokeWidth,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                drawCircle(
                                    color = tertiary,
                                    radius = 1.5.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.75f)
                                )
                            }
                            Text(
                                text = "Temperature Impact",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        val impactText = when {
                            currentTemp < 0 -> "Extremely cold temperatures can cause permanent battery damage and reduced capacity."
                            currentTemp < 10 -> "Cold temperatures reduce battery performance and may cause unexpected shutdowns."
                            currentTemp < 35 -> "Optimal temperature range. Battery performance is at its best."
                            currentTemp < 45 -> "Elevated temperature may reduce battery lifespan over time."
                            else -> "High temperature can cause permanent damage and significantly reduce battery life."
                        }
                        
                        Text(
                            text = impactText,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            item {
                // Temperature Tips Card
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
                                text = "Temperature Tips",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        val tips = listOf(
                            "Keep device in shade when outdoors",
                            "Remove case while charging to improve heat dissipation",
                            "Avoid leaving device in hot cars",
                            "Close resource-intensive apps to reduce heat generation",
                            "Use airplane mode in extremely hot environments"
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

@Composable
private fun TemperatureStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}