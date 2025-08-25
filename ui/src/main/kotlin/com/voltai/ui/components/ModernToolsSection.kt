package com.voltai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay

data class ToolItem(
    val title: String,
    val description: String,
    val icon: @Composable () -> Unit,
    val color: Color,
    val onClick: () -> Unit,
    val delay: Int = 0
)

@Composable
fun ModernToolsSection(
    // Dedicated feature screens
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
    onNavigateToWidgetConfig: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(600)
            ) + fadeIn()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Battery Tools",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Optimize and manage your battery",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // Tools Grid
        val tools = getToolItems(
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
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(240.dp)
        ) {
            items(tools) { tool ->
                AnimatedVisibility(
                    visible = isVisible,
                    enter = scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(400, delayMillis = tool.delay)
                    ) + fadeIn(animationSpec = tween(400, delayMillis = tool.delay))
                ) {
                    ModernToolCard(tool = tool)
                }
            }
        }
    }
}

@Composable
private fun ModernToolCard(
    tool: ToolItem,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tool_scale"
    )
    
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { 
                isPressed = !isPressed
                tool.onClick() 
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tool.color.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 100f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon container with glow effect
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tool.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    tool.icon()
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun getToolItems(
    // Dedicated feature screens
    onNavigateToHealth: () -> Unit,
    onNavigateToTemperature: () -> Unit,
    onNavigateToVoltage: () -> Unit,
    onNavigateToCurrent: () -> Unit,
    onNavigateToUsage: () -> Unit,
    onNavigateToScreenTime: () -> Unit,
    onNavigateToCycleCount: () -> Unit,
    onNavigateToForecast: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    // Legacy navigation (keeping for compatibility)
    onNavigateToSaver: () -> Unit,
    onNavigateToThermal: () -> Unit,
    onNavigateToAppUsage: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToTips: () -> Unit,
    onNavigateToWidgetConfig: () -> Unit
): List<ToolItem> {
    return listOf(
        // Primary Battery Metrics (Row 1)
        ToolItem(
            title = "Health",
            description = "Battery condition",
            icon = { HealthIcon(tint = Color(0xFF4CAF50)) },
            color = Color(0xFF4CAF50),
            onClick = onNavigateToHealth,
            delay = 0
        ),
        ToolItem(
            title = "Temperature",
            description = "Thermal monitoring",
            icon = { ThermometerIcon(tint = Color(0xFFF44336)) },
            color = Color(0xFFF44336),
            onClick = onNavigateToTemperature,
            delay = 50
        ),
        ToolItem(
            title = "Voltage",
            description = "Power stability",
            icon = { FlashIcon(tint = Color(0xFFFF9800)) },
            color = Color(0xFFFF9800),
            onClick = onNavigateToVoltage,
            delay = 100
        ),
        
        // Power Analysis (Row 2)
        ToolItem(
            title = "Current",
            description = "Power draw",
            icon = { FlashIcon(tint = Color(0xFF2196F3)) },
            color = Color(0xFF2196F3),
            onClick = onNavigateToCurrent,
            delay = 150
        ),
        ToolItem(
            title = "Usage",
            description = "mAh consumption",
            icon = { BatteryIcon(tint = Color(0xFF9C27B0)) },
            color = Color(0xFF9C27B0),
            onClick = onNavigateToUsage,
            delay = 200
        ),
        ToolItem(
            title = "Screen Time",
            description = "Display usage",
            icon = { EyeIcon(tint = Color(0xFF00BCD4)) },
            color = Color(0xFF00BCD4),
            onClick = onNavigateToScreenTime,
            delay = 250
        ),
        
        // Advanced Metrics (Row 3)
        ToolItem(
            title = "Cycles",
            description = "Charge count",
            icon = { RefreshIcon(tint = Color(0xFF795548)) },
            color = Color(0xFF795548),
            onClick = onNavigateToCycleCount,
            delay = 300
        ),
        ToolItem(
            title = "Forecast",
            description = "Life prediction",
            icon = { GraphIcon(tint = Color(0xFF607D8B)) },
            color = Color(0xFF607D8B),
            onClick = onNavigateToForecast,
            delay = 350
        ),
        ToolItem(
            title = "Tools",
            description = "Power management",
            icon = { PowerIcon(tint = Color(0xFFE91E63)) },
            color = Color(0xFFE91E63),
            onClick = onNavigateToTools,
            delay = 400
        ),
        
        // Navigation & Settings (Row 4)
        ToolItem(
            title = "History",
            description = "Usage trends",
            icon = { GraphIcon(tint = Color(0xFF3F51B5)) },
            color = Color(0xFF3F51B5),
            onClick = onNavigateToHistory,
            delay = 450
        ),
        ToolItem(
            title = "Settings",
            description = "App config",
            icon = { SettingsIcon(tint = Color(0xFF9E9E9E)) },
            color = Color(0xFF9E9E9E),
            onClick = onNavigateToSettings,
            delay = 500
        )
    )
}

@Composable
fun QuickActionButton(
    title: String,
    icon: @Composable () -> Unit,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "quick_action_scale"
    )
    
    Button(
        onClick = { 
            isPressed = !isPressed
            onClick() 
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}