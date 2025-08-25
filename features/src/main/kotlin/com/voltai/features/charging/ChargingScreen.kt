package com.voltai.features.charging

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.features.dashboard.DashboardViewModel
import com.voltai.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingScreen(
    onNavigateBack: () -> Unit = {},
    onDismiss: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val batteryForecast by viewModel.batteryForecast.collectAsState()
    
    val batteryLevel = batteryStatus?.level?.toFloat() ?: 75f
    val voltage = batteryStatus?.voltage?.toDouble() ?: 4.2
    val current = batteryStatus?.currentNow ?: 1500
    val temperature = batteryStatus?.temperature?.toInt() ?: 32
    val timeToFull = batteryForecast
    var isVisible by remember { mutableStateOf(false) }
    
    // Ambient pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val ambientAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_alpha"
    )
    
    // Floating particles animation
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_offset"
    )
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
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
                    text = "Charging Monitor",
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
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Ambient background with pulsing gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4CAF50).copy(alpha = ambientAlpha),
                                Color.Transparent,
                                Color(0xFF2196F3).copy(alpha = ambientAlpha * 0.5f)
                            ),
                            radius = 800f
                        )
                    )
            )
        
            // Floating energy particles
            repeat(8) { index ->
                val delay = index * 250
                val xOffset = (index % 4 - 1.5f) * 100f
                
                Box(
                    modifier = Modifier
                        .offset(x = xOffset.dp, y = particleOffset.dp)
                        .size(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Color(0xFF4CAF50).copy(
                                alpha = (1f - (particleOffset + 200f) / 200f).coerceIn(0f, 1f)
                            )
                        )
                        .graphicsLayer {
                            translationY = (particleOffset + delay) % 400f - 200f
                        }
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
            // Main battery ring with enhanced glow
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow ring
                    AnimatedBatteryRing(
                        batteryLevel = batteryLevel,
                        isCharging = true,
                        size = 200,
                        modifier = Modifier.graphicsLayer {
                            scaleX = 1.2f
                            scaleY = 1.2f
                            alpha = 0.3f
                        }
                    )
                    
                    // Main battery ring
                    AnimatedBatteryRing(
                        batteryLevel = batteryLevel,
                        isCharging = true,
                        size = 200
                    )
                }
            }
            
            // Charging status
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, delayMillis = 400)
                ) + fadeIn()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Charging",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    
                    Text(
                        text = "$timeToFull until full",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Live stats grid
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, delayMillis = 600)
                ) + fadeIn()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ChargingStatCard(
                        label = "Voltage",
                        value = "${String.format("%.2f", voltage)}V",
                        icon = { FlashIcon(tint = Color(0xFFFFEB3B)) }
                    )
                    
                    ChargingStatCard(
                        label = "Current",
                        value = "${current}mA",
                        icon = { GraphIcon(tint = Color(0xFF2196F3)) }
                    )
                    
                    ChargingStatCard(
                        label = "Temperature",
                        value = "${temperature}°C",
                        icon = { ThermometerIcon(tint = getTemperatureColor(temperature)) }
                    )
                }
            }
            
            // Dismiss hint
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 1000))
            ) {
                Text(
                    text = "Tap anywhere to dismiss",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
}

@Composable
fun ChargingStatCard(
    label: String,
    value: String,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
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