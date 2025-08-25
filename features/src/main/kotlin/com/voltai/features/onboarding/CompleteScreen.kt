package com.voltai.features.onboarding

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.voltai.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteScreen(
    onNavigateToDashboard: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    
    // Confetti animation states
    val confettiColors = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
        Color(0xFF96CEB4), Color(0xFFFECA57), Color(0xFFFF9FF3)
    )
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
        delay(300)
        showConfetti = true
        delay(800)
        showContent = true
        delay(600)
        showButton = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        // Confetti Animation
        if (showConfetti) {
            repeat(20) { index ->
                ConfettiParticle(
                    color = confettiColors[index % confettiColors.size],
                    delay = index * 100,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar
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
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Step 2 of 2",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Success Icon with Animation
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(
                    initialScale = 0.3f,
                    animationSpec = tween(1000, easing = EaseOutBack)
                ) + fadeIn(animationSpec = tween(1000))
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(70.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing ring animation
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_scale"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CheckIcon(
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Success Message
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "You're All Set!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "VoltAI is ready to help you optimize your battery life and monitor your device's health.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Features Summary
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "What's Next?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        FeatureSummaryItem(
                            icon = { HealthIcon(tint = Color(0xFF4CAF50)) },
                            text = "Monitor your battery health in real-time"
                        )
                        FeatureSummaryItem(
                            icon = { ThermometerIcon(tint = Color(0xFFF44336)) },
                            text = "Track temperature and prevent overheating"
                        )
                        FeatureSummaryItem(
                            icon = { GraphIcon(tint = Color(0xFF2196F3)) },
                            text = "Get personalized optimization recommendations"
                        )
                        FeatureSummaryItem(
                            icon = { FlashIcon(tint = Color(0xFFFF9800)) },
                            text = "Analyze charging patterns and efficiency"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Launch Dashboard Button
            AnimatedVisibility(
                visible = showButton,
                enter = scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(600, easing = EaseOutBack)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Button(
                    onClick = onNavigateToDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Open VoltAI Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BatteryIcon(
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureSummaryItem(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(18.dp)) {
                icon()
            }
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ConfettiParticle(
    color: Color,
    delay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(2000, easing = LinearEasing)
        ) + fadeIn(animationSpec = tween(500)) + scaleIn(
            initialScale = 0f,
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(500)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(4.dp)
                )
                .offset(
                    x = ((-200..200).random()).dp,
                    y = 0.dp
                )
        )
    }
}