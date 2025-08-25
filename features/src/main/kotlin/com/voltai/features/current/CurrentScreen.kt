package com.voltai.features.current

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
import kotlin.math.abs
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentScreen(
    onNavigateBack: () -> Unit = {}
) {
    var currentDraw by remember { mutableStateOf(-850f) } // Negative = discharging
    var maxDraw by remember { mutableStateOf(-1250f) }
    var minDraw by remember { mutableStateOf(-200f) }
    var avgDraw by remember { mutableStateOf(-650f) }
    var isCharging by remember { mutableStateOf(false) }
    
    // Simulate current readings for graph
    val currentReadings = remember {
        mutableStateListOf<Float>().apply {
            repeat(60) { i ->
                add(-800f + 300f * sin(i * 0.1f).toFloat() + (Math.random() * 200f - 100f).toFloat())
            }
        }
    }
    
    // Animation for current
    val animatedCurrent by animateFloatAsState(
        targetValue = currentDraw,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "current_animation"
    )
    
    // Current color based on value and direction
    val currentColor = when {
        animatedCurrent > 0 -> Color(0xFF4CAF50) // Green - Charging
        abs(animatedCurrent) < 300 -> Color(0xFF2196F3) // Blue - Low drain
        abs(animatedCurrent) < 800 -> Color(0xFFFF9800) // Orange - Medium drain
        else -> Color(0xFFF44336) // Red - High drain
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
                    text = "Electric Current",
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
                // Main Current Card
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
                        // Current Flow Icon
                        Canvas(modifier = Modifier.size(48.dp)) {
                            val strokeWidth = 3.dp.toPx()
                            val centerY = size.height / 2
                            
                            // Draw flow lines
                            for (i in 0..2) {
                                val y = centerY + (i - 1) * 8.dp.toPx()
                                drawLine(
                                    color = currentColor,
                                    start = Offset(size.width * 0.1f, y),
                                    end = Offset(size.width * 0.9f, y),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                                
                                // Arrow heads
                                val arrowX = size.width * 0.8f
                                drawLine(
                                    color = currentColor,
                                    start = Offset(arrowX, y),
                                    end = Offset(arrowX - 6.dp.toPx(), y - 4.dp.toPx()),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = currentColor,
                                    start = Offset(arrowX, y),
                                    end = Offset(arrowX - 6.dp.toPx(), y + 4.dp.toPx()),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        
                        Text(
                            text = if (animatedCurrent > 0) "Charging Current" else "Discharge Current",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${String.format("%.0f", abs(animatedCurrent))} mA",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentColor
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Direction indicator
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val strokeWidth = 2.dp.toPx()
                                if (animatedCurrent > 0) {
                                    // Up arrow for charging
                                    drawLine(
                                        color = currentColor,
                                        start = Offset(size.width * 0.5f, size.height * 0.8f),
                                        end = Offset(size.width * 0.5f, size.height * 0.2f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = currentColor,
                                        start = Offset(size.width * 0.5f, size.height * 0.2f),
                                        end = Offset(size.width * 0.3f, size.height * 0.4f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = currentColor,
                                        start = Offset(size.width * 0.5f, size.height * 0.2f),
                                        end = Offset(size.width * 0.7f, size.height * 0.4f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                } else {
                                    // Down arrow for discharging
                                    drawLine(
                                        color = currentColor,
                                        start = Offset(size.width * 0.5f, size.height * 0.2f),
                                        end = Offset(size.width * 0.5f, size.height * 0.8f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = currentColor,
                                        start = Offset(size.width * 0.5f, size.height * 0.8f),
                                        end = Offset(size.width * 0.3f, size.height * 0.6f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = currentColor,
                                        start = Offset(size.width * 0.5f, size.height * 0.8f),
                                        end = Offset(size.width * 0.7f, size.height * 0.6f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                            
                            Text(
                                text = if (animatedCurrent > 0) "Charging" else when {
                                    abs(animatedCurrent) < 300 -> "Low Drain"
                                    abs(animatedCurrent) < 800 -> "Medium Drain"
                                    else -> "High Drain"
                                },
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            item {
                // Current Flow Graph Card
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
                            text = "Current Flow (Last 60 readings)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Current flow graph
                        val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val width = size.width
                            val height = size.height
                            val strokeWidth = 2.dp.toPx()
                            val centerY = height / 2
                            
                            // Draw zero line
                            drawLine(
                                color = outlineColor,
                                start = Offset(0f, centerY),
                                end = Offset(width, centerY),
                                strokeWidth = 1.dp.toPx()
                            )
                            
                            if (currentReadings.isNotEmpty()) {
                                val maxAbsCurrent = 1500f
                                val stepX = width / (currentReadings.size - 1)
                                
                                val path = Path()
                                currentReadings.forEachIndexed { index, current ->
                                    val x = index * stepX
                                    val y = centerY - (current / maxAbsCurrent) * (height / 2)
                                    
                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }
                                
                                // Draw line
                                drawPath(
                                    path = path,
                                    color = currentColor,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }
                        
                        // Graph labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "+1500mA",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "0mA",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "-1500mA",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            item {
                // Current Statistics
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
                            text = "Current Statistics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CurrentStatItem(
                                label = "Peak",
                                value = "${String.format("%.0f", abs(maxDraw))} mA",
                                color = Color(0xFFF44336)
                            )
                            CurrentStatItem(
                                label = "Avg",
                                value = "${String.format("%.0f", abs(avgDraw))} mA",
                                color = MaterialTheme.colorScheme.primary
                            )
                            CurrentStatItem(
                                label = "Min",
                                value = "${String.format("%.0f", abs(minDraw))} mA",
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
            
            item {
                // Power Consumption Tips
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
                                text = "Power Optimization Tips",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        val tips = listOf(
                            "Lower screen brightness to reduce current draw",
                            "Close background apps consuming high current",
                            "Use airplane mode in low signal areas",
                            "Disable location services when not needed",
                            "Turn off Wi-Fi and Bluetooth when unused"
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
private fun CurrentStatItem(
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