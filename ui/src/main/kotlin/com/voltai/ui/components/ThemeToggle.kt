package com.voltai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ThemeToggle(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFE0E0E0),
        animationSpec = tween(300),
        label = "toggleBackground"
    )
    
    val thumbSize by animateDpAsState(
        targetValue = if (isDarkTheme) 28.dp else 24.dp,
        animationSpec = tween(300),
        label = "thumbSize"
    )
    
    val thumbScale by animateFloatAsState(
        targetValue = if (isDarkTheme) 1.1f else 1f,
        animationSpec = tween(300),
        label = "thumbScale"
    )

    Box(
        modifier = modifier
            .width(60.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onToggle)
            .padding(4.dp),
        contentAlignment = if (isDarkTheme) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .scale(thumbScale)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isDarkTheme) {
                MoonIcon(
                    tint = Color(0xFF1E1E1E),
                    modifier = Modifier.size(14.dp)
                )
            } else {
                SunIcon(
                    tint = Color(0xFFE0E0E0),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}