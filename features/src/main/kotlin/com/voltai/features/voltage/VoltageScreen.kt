package com.voltai.features.voltage

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.ui.components.*
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltageScreen(
    onNavigateBack: () -> Unit = {}
) {
    var currentVoltage by remember { mutableStateOf(3.82f) }
    var maxVoltage by remember { mutableStateOf(4.15f) }
    var minVoltage by remember { mutableStateOf(3.45f) }
    var avgVoltage by remember { mutableStateOf(3.78f) }
    
    // Simulate voltage readings for graph
    val voltageReadings = remember {
        mutableStateListOf<Float>().apply {
            repeat(60) { i ->
                add(3.7f + 0.3f * sin(i * 0.1f).toFloat() + (Math.random() * 0.1f).toFloat())
            }
        }
    }
    
    // Animation for voltage
    val animatedVoltage by animateFloatAsState(
        targetValue = currentVoltage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "voltage_animation"
    )
    
    // Voltage color based on value
    val voltageColor = when {
        animatedVoltage < 3.5f -> Color(0xFFF44336) // Red - Low
        animatedVoltage < 3.7f -> Color(0xFFFF9800) // Orange - Medium
        animatedVoltage < 4.0f -> Color(0xFF4CAF50) // Green - Good
        else -> Color(0xFF2196F3) // Blue - High
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
                    text = "Battery Voltage",
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
                // Main Voltage Card
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
                        // Voltage Icon (Lightning bolt)
                        FlashIcon(
                            modifier = Modifier.size(48.dp),
                            tint = voltageColor
                        )
                        
                        Text(
                            text = "Current Voltage",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${String.format("%.2f", animatedVoltage)}V",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = voltageColor
                        )
                        
                        Text(
                            text = when {
                                animatedVoltage < 3.5f -> "Critical Low"
                                animatedVoltage < 3.7f -> "Low"
                                animatedVoltage < 4.0f -> "Normal"
                                else -> "High"
                            },
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Voltage range indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3.0V",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            LinearProgressIndicator(
                                progress = ((animatedVoltage - 3.0f) / 1.5f).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                                    .height(8.dp),
                                color = voltageColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            
                            Text(
                                text = "4.5V",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            item {
                // Voltage Graph Card
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
                            text = "Voltage History (Last 60 readings)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Simple voltage graph
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val width = size.width
                            val height = size.height
                            val strokeWidth = 2.dp.toPx()
                            
                            if (voltageReadings.isNotEmpty()) {
                                val minV = 3.0f
                                val maxV = 4.5f
                                val stepX = width / (voltageReadings.size - 1)
                                
                                val path = Path()
                                voltageReadings.forEachIndexed { index, voltage ->
                                    val x = index * stepX
                                    val y = height - ((voltage - minV) / (maxV - minV)) * height
                                    
                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }
                                
                                // Draw gradient fill
                                val gradientPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }
                                
                                drawPath(
                                    path = gradientPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            voltageColor.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                
                                // Draw line
                                drawPath(
                                    path = path,
                                    color = voltageColor,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // Voltage Statistics
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
                            text = "Voltage Statistics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            VoltageStatItem(
                                label = "Max",
                                value = "${String.format("%.2f", maxVoltage)}V",
                                color = Color(0xFF2196F3)
                            )
                            VoltageStatItem(
                                label = "Avg",
                                value = "${String.format("%.2f", avgVoltage)}V",
                                color = MaterialTheme.colorScheme.primary
                            )
                            VoltageStatItem(
                                label = "Min",
                                value = "${String.format("%.2f", minVoltage)}V",
                                color = Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
            
            item {
                // Voltage Information Card
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
                            // Info icon
                            val tertiary = MaterialTheme.colorScheme.tertiary
                            Canvas(modifier = Modifier.size(24.dp)) {
                                val strokeWidth = 2.dp.toPx()
                                drawCircle(
                                    color = tertiary,
                                    radius = size.width / 2 - strokeWidth,
                                    style = Stroke(width = strokeWidth)
                                )
                                drawLine(
                                    color = tertiary,
                                    start = Offset(size.width * 0.5f, size.height * 0.3f),
                                    end = Offset(size.width * 0.5f, size.height * 0.7f),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = tertiary,
                                    radius = 1.5.dp.toPx(),
                                    center = Offset(size.width * 0.5f, size.height * 0.2f)
                                )
                            }
                            Text(
                                text = "Voltage Information",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        val voltageInfo = listOf(
                            "Normal range: 3.5V - 4.2V",
                            "Below 3.5V: Battery critically low",
                            "Above 4.2V: Fully charged or overcharged",
                            "Voltage drops as battery discharges",
                            "Sudden voltage drops may indicate battery issues"
                        )
                        
                        voltageInfo.forEach { info ->
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
                                    text = info,
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
private fun VoltageStatItem(
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