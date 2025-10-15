package com.voltai.ui.designsystem

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object VoltAIDesignSystem {
    
    // Color System
    object Colors {
        val ElectricBlue = Color(0xFF0066CC)
        val DeepPurple = Color(0xFF6200EE)
        val ChargingGreen = Color(0xFF4CAF50)
        val WarningOrange = Color(0xFFFF9800)
        val CriticalRed = Color(0xFFF44336)
        val NeutralGray = Color(0xFF9E9E9E)
        
        val LightBackground = Color(0xFFF8F9FA)
        val DarkBackground = Color(0xFF121212)
        val LightSurface = Color(0xFFFFFFFF)
        val DarkSurface = Color(0xFF1E1E1E)
        
        val BatteryLevels = listOf(
            CriticalRed,     // 0-20%
            WarningOrange,   // 21-50%
            ChargingGreen,   // 51-80%
            ElectricBlue     // 81-100%
        )
    }
    
    // Typography System
    object Typography {
        val displayLarge = TextStyle(
            fontSize = 57.sp,
            lineHeight = 64.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = (-0.25).sp
        )
        
        val displayMedium = TextStyle(
            fontSize = 45.sp,
            lineHeight = 52.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.sp
        )
        
        val displaySmall = TextStyle(
            fontSize = 36.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.sp
        )
        
        val headlineLarge = TextStyle(
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.sp
        )
        
        val headlineMedium = TextStyle(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.sp
        )
        
        val headlineSmall = TextStyle(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.sp
        )
        
        val titleLarge = TextStyle(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.sp
        )
        
        val titleMedium = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.15.sp
        )
        
        val titleSmall = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.1.sp
        )
        
        val bodyLarge = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.15.sp
        )
        
        val bodyMedium = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.25.sp
        )
        
        val bodySmall = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.W400,
            letterSpacing = 0.4.sp
        )
        
        val labelLarge = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.1.sp
        )
        
        val labelMedium = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.5.sp
        )
        
        val labelSmall = TextStyle(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.5.sp
        )
    }
    
    // Shape System
    object Shapes {
        val extraSmall = RoundedCornerShape(4.dp)
        val small = RoundedCornerShape(8.dp)
        val medium = RoundedCornerShape(12.dp)
        val large = RoundedCornerShape(16.dp)
        val extraLarge = RoundedCornerShape(20.dp)
        
        // VoltAI specific shapes
        val batteryCard = RoundedCornerShape(20.dp)
        val metricCard = RoundedCornerShape(16.dp)
        val button = RoundedCornerShape(12.dp)
        val chip = RoundedCornerShape(20.dp)
    }
    
    // Spacing System
    object Spacing {
        val extraSmall = 4.dp
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
        val extraLarge = 32.dp
        val huge = 48.dp
        
        // Content spacing
        val cardPadding = 16.dp
        val screenPadding = 16.dp
        val itemSpacing = 12.dp
        val sectionSpacing = 24.dp
    }
    
    // Elevation System
    object Elevation {
        val level0 = 0.dp
        val level1 = 1.dp
        val level2 = 3.dp
        val level3 = 6.dp
        val level4 = 8.dp
        val level5 = 12.dp
        
        // Component elevations
        val card = level2
        val sheet = level3
        val dialog = level5
        val fab = level3
    }
    
    // Animation System
    object Animations {
        val fastDuration = 150
        val mediumDuration = 300
        val slowDuration = 500
        
        val standardEasing = FastOutSlowInEasing
        val emphasizedEasing = EaseInOutCubic
        val deceleratedEasing = FastOutLinearInEasing
        val acceleratedEasing = LinearOutSlowInEasing
        
        // Common animations
        val fadeInOut = fadeIn(animationSpec = tween(mediumDuration)) to fadeOut(animationSpec = tween(mediumDuration))
        val slideUpDown = slideInVertically(
            animationSpec = tween(mediumDuration),
            initialOffsetY = { it }
        ) to slideOutVertically(
            animationSpec = tween(mediumDuration),
            targetOffsetY = { it }
        )
        val scaleInOut = scaleIn(animationSpec = tween(mediumDuration)) to scaleOut(animationSpec = tween(mediumDuration))
        
        // Battery specific animations
        val chargingPulse = infiniteRepeatable<Float>(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
        
        val levelChange = tween<Float>(
            durationMillis = slowDuration,
            easing = emphasizedEasing
        )
    }
}

// Theme Configuration
@Composable
fun VoltAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = VoltAIDesignSystem.Colors.ElectricBlue,
            secondary = VoltAIDesignSystem.Colors.DeepPurple,
            tertiary = VoltAIDesignSystem.Colors.ChargingGreen,
            background = VoltAIDesignSystem.Colors.DarkBackground,
            surface = VoltAIDesignSystem.Colors.DarkSurface,
            error = VoltAIDesignSystem.Colors.CriticalRed
        )
        else -> lightColorScheme(
            primary = VoltAIDesignSystem.Colors.ElectricBlue,
            secondary = VoltAIDesignSystem.Colors.DeepPurple,
            tertiary = VoltAIDesignSystem.Colors.ChargingGreen,
            background = VoltAIDesignSystem.Colors.LightBackground,
            surface = VoltAIDesignSystem.Colors.LightSurface,
            error = VoltAIDesignSystem.Colors.CriticalRed
        )
    }
    
    val typography = androidx.compose.material3.Typography(
        displayLarge = VoltAIDesignSystem.Typography.displayLarge,
        displayMedium = VoltAIDesignSystem.Typography.displayMedium,
        displaySmall = VoltAIDesignSystem.Typography.displaySmall,
        headlineLarge = VoltAIDesignSystem.Typography.headlineLarge,
        headlineMedium = VoltAIDesignSystem.Typography.headlineMedium,
        headlineSmall = VoltAIDesignSystem.Typography.headlineSmall,
        titleLarge = VoltAIDesignSystem.Typography.titleLarge,
        titleMedium = VoltAIDesignSystem.Typography.titleMedium,
        titleSmall = VoltAIDesignSystem.Typography.titleSmall,
        bodyLarge = VoltAIDesignSystem.Typography.bodyLarge,
        bodyMedium = VoltAIDesignSystem.Typography.bodyMedium,
        bodySmall = VoltAIDesignSystem.Typography.bodySmall,
        labelLarge = VoltAIDesignSystem.Typography.labelLarge,
        labelMedium = VoltAIDesignSystem.Typography.labelMedium,
        labelSmall = VoltAIDesignSystem.Typography.labelSmall
    )
    
    val shapes = Shapes(
        extraSmall = VoltAIDesignSystem.Shapes.extraSmall,
        small = VoltAIDesignSystem.Shapes.small,
        medium = VoltAIDesignSystem.Shapes.medium,
        large = VoltAIDesignSystem.Shapes.large,
        extraLarge = VoltAIDesignSystem.Shapes.extraLarge
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

// Utility functions for battery-specific colors
@Composable
fun getBatteryLevelColor(level: Int): Color {
    return when {
        level <= 20 -> VoltAIDesignSystem.Colors.CriticalRed
        level <= 50 -> VoltAIDesignSystem.Colors.WarningOrange
        level <= 80 -> VoltAIDesignSystem.Colors.ChargingGreen
        else -> VoltAIDesignSystem.Colors.ElectricBlue
    }
}

@Composable
fun getTemperatureColor(temperature: Float): Color {
    return when {
        temperature >= 45f -> VoltAIDesignSystem.Colors.CriticalRed
        temperature >= 35f -> VoltAIDesignSystem.Colors.WarningOrange
        else -> VoltAIDesignSystem.Colors.ChargingGreen
    }
}

// Common padding modifiers
fun Modifier.voltAICardPadding() = this.padding(VoltAIDesignSystem.Spacing.cardPadding)
fun Modifier.voltAIScreenPadding() = this.padding(VoltAIDesignSystem.Spacing.screenPadding)
fun Modifier.voltAIItemSpacing() = this.padding(VoltAIDesignSystem.Spacing.itemSpacing)

// Common animations
@Composable
fun rememberChargingAnimation(): State<Float> {
    return animateFloatAsState(
        targetValue = 1f,
        animationSpec = VoltAIDesignSystem.Animations.chargingPulse,
        label = "charging_animation"
    )
}

@Composable
fun rememberBatteryLevelAnimation(targetLevel: Float): State<Float> {
    return animateFloatAsState(
        targetValue = targetLevel,
        animationSpec = VoltAIDesignSystem.Animations.levelChange,
        label = "battery_level_animation"
    )
}
