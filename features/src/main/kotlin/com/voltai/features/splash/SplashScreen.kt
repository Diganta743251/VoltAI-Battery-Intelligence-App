package com.voltai.features.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.ui.components.AnimatedBatteryRing
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var batteryLevel by remember { mutableStateOf(0f) }
    
    // Animate battery level from 0 to 100
    LaunchedEffect(Unit) {
        isVisible = true
        // Animate battery level
        for (i in 0..100) {
            batteryLevel = i.toFloat()
            delay(20) // 2 second total animation
        }
        delay(1000) // Hold at 100% for 1 second
        onNavigateToMain()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0066CC), // VoltAI Electric Blue
                        Color(0xFF6200EE)  // VoltAI Deep Purple
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // App Logo/Icon with animated battery ring
            AnimatedBatteryRing(
                batteryLevel = batteryLevel,
                isCharging = true,
                size = 120
            )
            
            // App Name
            Text(
                text = "VoltAI",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // Tagline
            Text(
                text = "Smart Battery Guardian",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            // Loading indicator
            if (batteryLevel < 100f) {
                LinearProgressIndicator(
                    progress = batteryLevel / 100f,
                    modifier = Modifier
                        .width(200.dp)
                        .padding(top = 16.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
        
        // Version info at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Version 1.0.0",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = "Built with ❤️ for Android",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}