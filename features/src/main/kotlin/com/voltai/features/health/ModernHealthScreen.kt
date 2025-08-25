package com.voltai.features.health

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
// Removed default Material icons - using custom AppIcons.kt instead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.features.dashboard.DashboardViewModel
import com.voltai.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernHealthScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var isVisible by remember { mutableStateOf(false) }
    
    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val batteryHealth by viewModel.batteryHealth.collectAsState()
    
    // Real data from ViewModel
    val currentCapacity = batteryHealth?.currentCapacity?.toFloat() ?: 3820f
    val designCapacity = batteryHealth?.designCapacity?.toFloat() ?: 4000f
    val healthPercentage = batteryHealth?.healthPercentage ?: 95
    val cycleCount = batteryHealth?.cycleCount ?: 247
    val averageTemperature = batteryStatus?.temperature?.toInt() ?: 32
    val lastCalibration = "2 weeks ago"
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ) + fadeIn()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            BackArrowIcon(
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Battery Health",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Monitor your battery's condition",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            item {
                // Main Health Card
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(600, delayMillis = 200)
                    ) + fadeIn()
                ) {
                    HealthOverviewCard(
                        healthPercentage = healthPercentage,
                        currentCapacity = currentCapacity,
                        designCapacity = designCapacity
                    )
                }
            }
            
            item {
                // Health Metrics Grid
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(600, delayMillis = 400)
                    ) + fadeIn()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactInfoCard(
                            title = "Cycle Count",
                            value = cycleCount.toString(),
                            subtitle = "Total cycles",
                            icon = { BatteryIcon(tint = Color(0xFF2196F3)) },
                            color = Color(0xFF2196F3),
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                        
                        CompactInfoCard(
                            title = "Avg. Temp",
                            value = "${averageTemperature}°C",
                            subtitle = "Last 30 days",
                            icon = { ThermometerIcon(tint = getTemperatureColor(averageTemperature)) },
                            color = getTemperatureColor(averageTemperature),
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            item {
                // Health Trend Graph
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(600, delayMillis = 600)
                    ) + fadeIn()
                ) {
                    HealthTrendCard()
                }
            }
            
            item {
                // Battery Tips Card
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(600, delayMillis = 800)
                    ) + fadeIn()
                ) {
                    BatteryTipsCard()
                }
            }
            
            item {
                // Calibration Card
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(600, delayMillis = 1000)
                    ) + fadeIn()
                ) {
                    CalibrationCard(lastCalibration = lastCalibration)
                }
            }
        }
    }
}

@Composable
private fun HealthOverviewCard(
    healthPercentage: Int,
    currentCapacity: Float,
    designCapacity: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            getHealthColor(healthPercentage).copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Health Ring
                AnimatedBatteryRing(
                    batteryLevel = healthPercentage.toFloat(),
                    isCharging = false,
                    size = 140
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Health Status
                Text(
                    text = getHealthStatus(healthPercentage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = getHealthColor(healthPercentage)
                )
                
                Text(
                    text = "Battery Health",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Capacity Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${currentCapacity.toInt()} mAh",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${designCapacity.toInt()} mAh",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Design",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthTrendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Health Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "Last 30 days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sample trend data
            val trendData = listOf(96f, 95f, 95f, 94f, 94f, 93f, 93f)
            LiveCurrentGraph(
                data = trendData,
                isCharging = false
            )
        }
    }
}

@Composable
private fun BatteryTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    HealthIcon(tint = MaterialTheme.colorScheme.primary)
                }
                
                Column {
                    Text(
                        text = "Battery Care Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Keep your battery healthy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val tips = listOf(
                "Avoid extreme temperatures (below 0°C or above 35°C)",
                "Don't let battery drain completely regularly",
                "Use original charger when possible"
            )
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CalibrationCard(lastCalibration: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Battery Calibration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Last calibrated: $lastCalibration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Button(
                    onClick = { /* Calibration action */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Calibrate")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Calibration helps improve battery percentage accuracy by resetting the battery statistics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun getHealthColor(healthPercentage: Int): Color {
    return when {
        healthPercentage >= 85 -> Color(0xFF4CAF50) // Green
        healthPercentage >= 70 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

@Composable
private fun getHealthStatus(healthPercentage: Int): String {
    return when {
        healthPercentage >= 85 -> "Excellent"
        healthPercentage >= 70 -> "Good"
        healthPercentage >= 50 -> "Fair"
        else -> "Poor"
    }
}

@Composable
private fun getTemperatureColor(temperature: Int): Color {
    return when {
        temperature > 40 -> Color(0xFFF44336) // Red for hot
        temperature > 35 -> Color(0xFFFF9800) // Orange for warm
        else -> Color(0xFF4CAF50) // Green for normal
    }
}