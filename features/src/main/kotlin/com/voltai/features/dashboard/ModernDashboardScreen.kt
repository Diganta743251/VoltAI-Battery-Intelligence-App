package com.voltai.features.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
// Removed default Material icons - using custom AppIcons.kt instead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToHealth: () -> Unit = {},
    onNavigateToTemperature: () -> Unit = {},
    onNavigateToVoltage: () -> Unit = {},
    onNavigateToCurrent: () -> Unit = {},
    onNavigateToUsage: () -> Unit = {},
    onNavigateToScreenTime: () -> Unit = {},
    onNavigateToCycleCount: () -> Unit = {},
    onNavigateToForecast: () -> Unit = {},
    onNavigateToTools: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    // Legacy navigation (keeping for compatibility)
    onNavigateToSaver: () -> Unit = {},
    onNavigateToThermal: () -> Unit = {},
    onNavigateToAppUsage: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToTips: () -> Unit = {},
    onNavigateToWidgetConfig: () -> Unit = {}
) {
    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val batteryTips by viewModel.batteryTips.collectAsState()
    val batteryForecast by viewModel.batteryForecast.collectAsState()
    val currentData by viewModel.currentData.collectAsState()
    val thermalData by viewModel.thermalData.collectAsState()
    
    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    // Parallax scroll effect
    var scrollOffset by remember { mutableStateOf(0f) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Hero Battery Section with Parallax
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800))
            ) {
                HeroBatteryCard(
                    batteryStatus = batteryStatus,
                    modifier = Modifier.graphicsLayer {
                        translationY = scrollOffset * 0.5f
                    }
                )
            }
        }
        
        // AI Insights Section
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
            ) {
                AIInsightsCard(
                    tips = batteryTips,
                    forecast = batteryForecast
                )
            }
        }
        
        // Live Current Graph
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800, delayMillis = 400, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
            ) {
                LiveCurrentCard(
                    currentData = currentData,
                    isCharging = batteryStatus?.isCharging ?: false
                )
            }
        }
        
        // Quick Stats Grid
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800, delayMillis = 600, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
            ) {
                QuickStatsGrid(
                    batteryStatus = batteryStatus
                )
            }
        }
        
        // Modern Tools Section
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800, delayMillis = 800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 800))
            ) {
                ModernToolsSection(
                    // Dedicated feature screens
                    onNavigateToHealth = onNavigateToHealth,
                    onNavigateToTemperature = onNavigateToTemperature,
                    onNavigateToVoltage = onNavigateToVoltage,
                    onNavigateToCurrent = onNavigateToCurrent,
                    onNavigateToUsage = onNavigateToUsage,
                    onNavigateToScreenTime = onNavigateToScreenTime,
                    onNavigateToCycleCount = onNavigateToCycleCount,
                    onNavigateToForecast = onNavigateToForecast,
                    onNavigateToTools = onNavigateToTools,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToSettings = onNavigateToSettings,
                    // Legacy navigation (keeping for compatibility)
                    onNavigateToSaver = onNavigateToSaver,
                    onNavigateToThermal = onNavigateToThermal,
                    onNavigateToAppUsage = onNavigateToAppUsage,
                    onNavigateToBackup = onNavigateToBackup,
                    onNavigateToTips = onNavigateToTips,
                    onNavigateToWidgetConfig = onNavigateToWidgetConfig
                )
            }
        }
    }
}

@Composable
private fun HeroBatteryCard(
    batteryStatus: com.voltai.domain.model.BatteryStatus?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Battery Ring
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedBatteryRing(
                        batteryLevel = batteryStatus?.percentage?.toFloat() ?: 0f,
                        isCharging = batteryStatus?.status?.contains("Charging", ignoreCase = true) ?: false,
                        size = 160,
                        strokeWidth = 16f
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "${batteryStatus?.percentage ?: 0}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = batteryStatus?.status ?: "Battery",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Battery Details
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BatteryDetailItem(
                        icon = { AnimatedThermometerIcon(tint = MaterialTheme.colorScheme.primary, temperature = batteryStatus?.temperature ?: 25f) },
                        label = "Temperature",
                        value = "${batteryStatus?.temperature?.toInt() ?: 25}°C"
                    )
                    
                    BatteryDetailItem(
                        icon = { FlashOnIcon(tint = MaterialTheme.colorScheme.primary) },
                        label = "Voltage",
                        value = "${batteryStatus?.voltage?.toInt() ?: 3800}mV"
                    )
                    
                    BatteryDetailItem(
                        icon = { AnimatedHealthIcon(tint = MaterialTheme.colorScheme.primary, healthPercentage = 100f) },
                        label = "Current",
                        value = "${batteryStatus?.current?.toInt() ?: 0}mA"
                    )
                    
                    BatteryDetailItem(
                        icon = { AccessTimeIcon(tint = MaterialTheme.colorScheme.primary) },
                        label = "Status",
                        value = batteryStatus?.chargingStatus ?: "Unknown"
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryDetailItem(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AIInsightsCard(
    tips: List<String>,
    forecast: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MemoryIcon(
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "AI Insights",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Battery Forecast
            forecast?.let { forecastText ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Battery Forecast",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = forecastText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // AI Tips
            if (tips.isNotEmpty()) {
                Text(
                    text = "Smart Recommendations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                tips.take(3).forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LightbulbIcon(
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveCurrentCard(
    currentData: List<Float>,
    isCharging: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedGraphIcon(
                    tint = MaterialTheme.colorScheme.primary,
                    isAnimating = true
                )
                Text(
                    text = "Live Current",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "${currentData.lastOrNull()?.toInt() ?: 0}mA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCharging) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LiveCurrentGraph(
                data = currentData,
                isCharging = isCharging,
                modifier = Modifier.height(120.dp)
            )
        }
    }
}

@Composable
private fun QuickStatsGrid(
    batteryStatus: com.voltai.domain.model.BatteryStatus?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Battery Level
        QuickStatCard(
            modifier = Modifier.weight(1f),
            icon = { BatteryIcon(tint = Color(0xFF2196F3)) },
            label = "Battery",
            value = "${batteryStatus?.percentage ?: 0}%",
            color = Color(0xFF2196F3)
        )
        
        // Temperature
        QuickStatCard(
            modifier = Modifier.weight(1f),
            icon = { AnimatedThermometerIcon(tint = Color(0xFFFF9800), temperature = batteryStatus?.temperature ?: 35f) },
            label = "Temp",
            value = "${batteryStatus?.temperature?.toInt() ?: 35}°C",
            color = Color(0xFFFF9800)
        )
        
        // Voltage
        QuickStatCard(
            modifier = Modifier.weight(1f),
            icon = { FlashIcon(tint = Color(0xFF9C27B0)) },
            label = "Voltage",
            value = "${batteryStatus?.voltage?.toInt() ?: 3800}mV",
            color = Color(0xFF9C27B0)
        )
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides color) {
                    icon()
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}