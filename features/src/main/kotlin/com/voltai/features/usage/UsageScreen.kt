package com.voltai.features.usage

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreen(
    onNavigateBack: () -> Unit = {}
) {
    var todayUsage by remember { mutableStateOf(2140) } // mAh
    var screenOnUsage by remember { mutableStateOf(1650) } // mAh
    var standbyUsage by remember { mutableStateOf(490) } // mAh
    var dailyAverage by remember { mutableStateOf(1980) } // mAh
    var batteryCapacity by remember { mutableStateOf(4000) } // mAh
    
    // Weekly usage data
    val weeklyUsage = remember {
        listOf(1850, 2200, 1950, 2340, 1780, 2140, 1990) // Last 7 days
    }
    
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    // Animation for usage values
    val animatedTodayUsage by animateIntAsState(
        targetValue = todayUsage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "usage_animation"
    )
    
    val animatedScreenUsage by animateIntAsState(
        targetValue = screenOnUsage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "screen_usage_animation"
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
                    text = "Power Usage",
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
                // Main Usage Card
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
                        // Usage Icon (Battery with usage indicator)
                        val primary = MaterialTheme.colorScheme.primary
                        Canvas(modifier = Modifier.size(48.dp)) {
                            val strokeWidth = 3.dp.toPx()
                            val batteryWidth = size.width * 0.7f
                            val batteryHeight = size.height * 0.5f
                            val batteryX = (size.width - batteryWidth) / 2
                            val batteryY = (size.height - batteryHeight) / 2
                            
                            // Battery outline
                            drawRoundRect(
                                color = primary,
                                topLeft = Offset(batteryX, batteryY),
                                size = Size(batteryWidth, batteryHeight),
                                style = Stroke(width = strokeWidth)
                            )
                            
                            // Battery tip
                            drawRoundRect(
                                color = primary,
                                topLeft = Offset(batteryX + batteryWidth, batteryY + batteryHeight * 0.3f),
                                size = Size(size.width * 0.1f, batteryHeight * 0.4f),
                                style = Stroke(width = strokeWidth)
                            )
                            
                            // Usage fill (partial)
                            val usagePercent = animatedTodayUsage.toFloat() / batteryCapacity.toFloat()
                            val fillWidth = batteryWidth * 0.8f * usagePercent
                            drawRoundRect(
                                color = primary.copy(alpha = 0.3f),
                                topLeft = Offset(batteryX + batteryWidth * 0.1f, batteryY + batteryHeight * 0.2f),
                                size = Size(fillWidth, batteryHeight * 0.6f)
                            )
                        }
                        
                        Text(
                            text = "Today's Usage",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "$animatedTodayUsage mAh",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "${String.format("%.1f", (animatedTodayUsage.toFloat() / batteryCapacity * 100))}% of battery capacity",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Usage progress bar
                        LinearProgressIndicator(
                            progress = (animatedTodayUsage.toFloat() / batteryCapacity).coerceIn(0f, 1f),
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
                // Usage Breakdown Card
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
                            text = "Usage Breakdown",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Screen On Usage
                        UsageBreakdownItem(
                            label = "Screen On",
                            value = animatedScreenUsage,
                            total = animatedTodayUsage,
                            color = MaterialTheme.colorScheme.primary,
                            icon = @Composable {
                                val primary = MaterialTheme.colorScheme.primary
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val strokeWidth = 2.dp.toPx()
                                    // Screen rectangle
                                    drawRoundRect(
                                        color = primary,
                                        topLeft = Offset(size.width * 0.2f, size.height * 0.1f),
                                        size = Size(size.width * 0.6f, size.height * 0.8f),
                                        style = Stroke(width = strokeWidth)
                                    )
                                    // Screen content
                                    drawRoundRect(
                                        color = primary.copy(alpha = 0.3f),
                                        topLeft = Offset(size.width * 0.25f, size.height * 0.2f),
                                        size = Size(size.width * 0.5f, size.height * 0.6f)
                                    )
                                }
                            }
                        )
                        
                        // Standby Usage
                        UsageBreakdownItem(
                            label = "Standby",
                            value = standbyUsage,
                            total = animatedTodayUsage,
                            color = Color(0xFF4CAF50),
                            icon = @Composable {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val strokeWidth = 2.dp.toPx()
                                    // Moon icon for standby
                                    drawArc(
                                        color = Color(0xFF4CAF50),
                                        startAngle = 45f,
                                        sweepAngle = 270f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                            }
                        )
                    }
                }
            }
            
            item {
                // Weekly Usage Chart
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
                            text = "Weekly Usage Trend",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Bar chart
                        val primary = MaterialTheme.colorScheme.primary
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val barWidth = size.width / (weeklyUsage.size * 1.5f)
                            val maxUsage = weeklyUsage.maxOrNull() ?: 1
                            val spacing = barWidth * 0.5f
                            
                            weeklyUsage.forEachIndexed { index, usage ->
                                val barHeight = (usage.toFloat() / maxUsage) * size.height * 0.8f
                                val x = index * (barWidth + spacing) + spacing
                                val y = size.height - barHeight
                                
                                // Bar
                                drawRoundRect(
                                    color = if (index == weeklyUsage.size - 1) 
                                        primary
                                    else 
                                        primary.copy(alpha = 0.6f),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                )
                            }
                        }
                        
                        // Day labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            daysOfWeek.forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // Usage Statistics
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
                            text = "Usage Statistics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            UsageStatItem(
                                label = "Daily Avg",
                                value = "$dailyAverage mAh",
                                color = MaterialTheme.colorScheme.primary
                            )
                            UsageStatItem(
                                label = "Weekly Max",
                                value = "${weeklyUsage.maxOrNull() ?: 0} mAh",
                                color = Color(0xFFF44336)
                            )
                            UsageStatItem(
                                label = "Weekly Min",
                                value = "${weeklyUsage.minOrNull() ?: 0} mAh",
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
            
            item {
                // Usage Tips Card
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
                                text = "Usage Optimization Tips",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        val tips = listOf(
                            "Monitor apps with highest battery drain",
                            "Reduce screen brightness and timeout",
                            "Use dark mode to save OLED display power",
                            "Disable unnecessary background app refresh",
                            "Turn off location services for unused apps"
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
private fun UsageBreakdownItem(
    label: String,
    value: Int,
    total: Int,
    color: Color,
    icon: @Composable () -> Unit
) {
    val percentage = if (total > 0) (value.toFloat() / total * 100) else 0f
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        icon()
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$value mAh",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Text(
                text = "${String.format("%.1f", percentage)}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UsageStatItem(
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}