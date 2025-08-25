package com.voltai.features.cycle_count

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
fun CycleCountScreen(
    onNavigateBack: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    var estimatedCycles by remember { mutableStateOf(127) }
    var animatedCycles by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
        delay(500)
        // Animate cycle count
        for (i in 0..estimatedCycles) {
            animatedCycles = i
            delay(20)
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
                    text = "Charge Cycles",
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
                    RefreshIcon(
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Charge Cycles",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Track your battery's charging cycles",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Main Cycle Count Card
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Estimated Cycles",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "$animatedCycles",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            animatedCycles < 200 -> Color(0xFF4CAF50) // Green
                            animatedCycles < 400 -> Color(0xFF2196F3) // Blue
                            animatedCycles < 600 -> Color(0xFFFF9800) // Orange
                            else -> Color(0xFFF44336) // Red
                        },
                        fontSize = 64.sp
                    )
                    
                    // Progress bar to 500 cycles
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = (animatedCycles / 500f).coerceAtMost(1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = when {
                                animatedCycles < 200 -> Color(0xFF4CAF50)
                                animatedCycles < 400 -> Color(0xFF2196F3)
                                animatedCycles < 600 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            },
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        )
                        Text(
                            text = "Target: 500 cycles (typical battery lifespan)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Information Card
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
                        text = "About Charge Cycles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "A charge cycle is completed when you use 100% of your battery's capacity, but not necessarily from a single charge. For example, using 50% today and 50% tomorrow equals one cycle.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                    
                    Text(
                        text = "Most lithium-ion batteries maintain 80% capacity after 500-800 cycles. Battery wear is normal and occurs with each full cycle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        
        // Reset Button
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 600, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
        ) {
            OutlinedButton(
                onClick = { 
                    // Reset cycle count for calibration
                    estimatedCycles = 0
                    animatedCycles = 0
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                RefreshIcon(
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset for Calibration",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
    }
}