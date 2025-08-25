package com.voltai.features.forecast

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    onNavigateBack: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    var batteryLevel by remember { mutableStateOf(85) }
    var timeRemaining by remember { mutableStateOf("3h 18m") }
    var timeToFull by remember { mutableStateOf("1h 04m") }
    var isCharging by remember { mutableStateOf(false) }
    
    // Simulate real-time updates
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
        
        // Update forecast every 30 seconds
        while (true) {
            delay(30000)
            if (isCharging) {
                if (batteryLevel < 100) {
                    batteryLevel += 1
                    val remainingToCharge = 100 - batteryLevel
                    val hoursToFull = remainingToCharge / 60.0
                    val hours = hoursToFull.toInt()
                    val minutes = ((hoursToFull - hours) * 60).toInt()
                    timeToFull = "${hours}h ${minutes}m"
                }
            } else {
                if (batteryLevel > 0) {
                    batteryLevel -= 1
                    val hoursRemaining = batteryLevel / 25.0 // Rough estimate
                    val hours = hoursRemaining.toInt()
                    val minutes = ((hoursRemaining - hours) * 60).toInt()
                    timeRemaining = "${hours}h ${minutes}m"
                }
            }
        }
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
                    text = "Battery Forecast",
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
        // Header
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(800, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AccessTimeIcon(
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Battery Forecast",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Predict your battery life",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main Forecast Display
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Current Battery Level
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Current Level",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$batteryLevel%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                batteryLevel > 80 -> Color(0xFF4CAF50)
                                batteryLevel > 50 -> Color(0xFF2196F3)
                                batteryLevel > 20 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            },
                            fontSize = 48.sp
                        )
                    }
                    
                    // Time Predictions
                    if (isCharging) {
                        // Time to Full
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FlashIcon(
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Time to Full",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = timeToFull,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50),
                                fontSize = 40.sp
                            )
                        }
                    } else {
                        // Time Remaining
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BatteryIcon(
                                tint = when {
                                    batteryLevel > 50 -> Color(0xFF4CAF50)
                                    batteryLevel > 20 -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Time Remaining",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = timeRemaining,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    batteryLevel > 50 -> Color(0xFF4CAF50)
                                    batteryLevel > 20 -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                },
                                fontSize = 40.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Forecast Model Info
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 400, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Forecast Model",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Adaptive prediction based on your usage patterns",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Accuracy",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "±15 minutes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Last Updated",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Just now",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        // Toggle Charging State (for demo)
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 600, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
        ) {
            Button(
                onClick = { isCharging = !isCharging },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCharging) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            ) {
                if (isCharging) {
                    FlashIcon(
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    BatteryIcon(
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCharging) "Charging" else "On Battery",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
    }
}