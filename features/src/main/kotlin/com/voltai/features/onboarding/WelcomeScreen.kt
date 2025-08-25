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
import com.voltai.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onNavigateToPermissions: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    
    // Battery animation state
    var batteryLevel by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
        delay(500)
        
        // Animate battery filling up
        for (i in 0..100 step 5) {
            batteryLevel = i.toFloat()
            delay(50)
        }
        
        delay(500)
        showButton = true
    }
    
    Column(
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Logo/Icon
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = 0.3f,
                animationSpec = tween(800, easing = EaseOutBack)
            ) + fadeIn(animationSpec = tween(800))
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedBatteryRing(
                    batteryLevel = batteryLevel,
                    isCharging = true,
                    size = 80,
                    strokeWidth = 8f,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Welcome Text
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 400, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Welcome to VoltAI",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Smart Battery Guardian",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Your intelligent companion for battery health monitoring, optimization, and longevity insights.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        
        // Features Preview
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 600, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureItem(
                    icon = { HealthIcon(tint = Color(0xFF4CAF50)) },
                    title = "Real-time Health Monitoring",
                    description = "Track battery condition and degradation"
                )
                FeatureItem(
                    icon = { ThermometerIcon(tint = Color(0xFFF44336)) },
                    title = "Thermal Management",
                    description = "Monitor temperature and prevent overheating"
                )
                FeatureItem(
                    icon = { GraphIcon(tint = Color(0xFF2196F3)) },
                    title = "Usage Analytics",
                    description = "Detailed insights and optimization tips"
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Get Started Button
        AnimatedVisibility(
            visible = showButton,
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(600, easing = EaseOutBack)
            ) + fadeIn(animationSpec = tween(600))
        ) {
            Button(
                onClick = onNavigateToPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                FlashIcon(
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(24.dp)) {
                icon()
            }
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}