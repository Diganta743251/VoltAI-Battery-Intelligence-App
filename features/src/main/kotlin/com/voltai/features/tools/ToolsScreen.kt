package com.voltai.features.tools

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.ui.components.*
import kotlinx.coroutines.delay

data class ToolItem(
    val title: String,
    val description: String,
    val icon: @Composable () -> Unit,
    val color: Color,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigateBack: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val tools = listOf(
        ToolItem(
            title = "Battery Saver",
            description = "Optimize power consumption",
            icon = { PowerIcon(tint = Color.White, modifier = Modifier.size(24.dp)) },
            color = Color(0xFF4CAF50),
            action = { /* Navigate to Battery Saver */ }
        ),
        ToolItem(
            title = "Optimize Apps",
            description = "Manage app battery usage",
            icon = { SettingsIcon(tint = Color.White, modifier = Modifier.size(24.dp)) },
            color = Color(0xFF2196F3),
            action = { /* Navigate to App Optimizer */ }
        ),
        ToolItem(
            title = "Calibrate Battery",
            description = "Reset battery statistics",
            icon = { RefreshIcon(tint = Color.White, modifier = Modifier.size(24.dp)) },
            color = Color(0xFFFF9800),
            action = { /* Start calibration */ }
        ),
        ToolItem(
            title = "Temperature Monitor",
            description = "Track thermal health",
            icon = { ThermometerIcon(tint = Color.White, modifier = Modifier.size(24.dp)) },
            color = Color(0xFFF44336),
            action = { /* Navigate to Temperature */ }
        ),
        ToolItem(
            title = "Usage Analytics",
            description = "Analyze consumption patterns",
            icon = { AnalyticsIcon(tint = Color.White, modifier = Modifier.size(24.dp)) },
            color = Color(0xFF9C27B0),
            action = { /* Navigate to Analytics */ }
        ),
        ToolItem(
            title = "Smart Tips",
            description = "AI-powered recommendations",
            icon = { LightbulbIcon(tint = Color.White, modifier = Modifier.size(24.dp)) },
            color = Color(0xFFFFEB3B),
            action = { /* Show tips */ }
        ),
        ToolItem(
            title = "Export Data",
            description = "Backup battery logs",
            icon = { ExportIcon(tint = Color.White, modifier = Modifier.size(24.dp)) },
            color = Color(0xFF607D8B),
            action = { /* Export data */ }
        ),
        ToolItem(
            title = "Contact Support",
            description = "Get help and support",
            icon = { SupportIcon(tint = Color.White, modifier = Modifier.size(24.dp)) },
            color = Color(0xFF795548),
            action = { /* Contact support */ }
        )
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
                    text = "Battery Tools",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsIcon(
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Tools",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Quick actions and diagnostics",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
            // Tools Grid
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tools) { tool ->
                        ToolCard(
                            tool = tool,
                            onTap = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                tool.action()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: ToolItem,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardElevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onTap()
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = tool.color,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                tool.icon()
            }
            
            // Title and Description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }
        }
    }

    // Reset pressed state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}