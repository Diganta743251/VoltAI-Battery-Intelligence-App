package com.voltai.smartbatteryguardian.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Premium Theme System with dynamic colors and accessibility support
 * Future-proofed for Android 13+ dynamic colors and theme customization
 */

// Premium Color Palettes
private val PremiumLightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B3A),
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = Color(0xFFEC4899),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF831843),
    error = Color(0xFFDC2626),
    errorContainer = Color(0xFFFEE2E2),
    onError = Color.White,
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFFDFDFD),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFFA5B4FC),
    scrim = Color.Black.copy(alpha = 0.32f)
)

private val PremiumDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B3A),
    primaryContainer = Color(0xFF4338CA),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF065F46),
    secondaryContainer = Color(0xFF047857),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF831843),
    tertiaryContainer = Color(0xFFBE185D),
    onTertiaryContainer = Color(0xFFFCE7F3),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFFDC2626),
    onError = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFE5E5E5),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE5E5E5),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF4B5563),
    outlineVariant = Color(0xFF374151),
    inverseSurface = Color(0xFFE5E5E5),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = Color(0xFF6366F1),
    scrim = Color.Black.copy(alpha = 0.5f)
)

// OLED Black Theme for premium users
private val OLEDBlackColorScheme = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF34D399),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF047857),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFBE185D),
    onTertiaryContainer = Color(0xFFFCE7F3),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFFDC2626),
    onError = Color.Black,
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color.Black,
    onBackground = Color(0xFFE5E5E5),
    surface = Color.Black,
    onSurface = Color(0xFFE5E5E5),
    surfaceVariant = Color(0xFF0D0D0D),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF4B5563),
    outlineVariant = Color(0xFF262626),
    inverseSurface = Color(0xFFE5E5E5),
    inverseOnSurface = Color.Black,
    inversePrimary = Color(0xFF6366F1),
    scrim = Color.Black.copy(alpha = 0.7f)
)

// Premium Gradient Colors
object PremiumGradients {
    val primaryGradient = listOf(
        Color(0xFF6366F1),
        Color(0xFF8B5CF6)
    )
    
    val secondaryGradient = listOf(
        Color(0xFF10B981),
        Color(0xFF059669)
    )
    
    val batteryHealthGradient = listOf(
        Color(0xFF4ADE80),
        Color(0xFF22C55E)
    )
    
    val chargingGradient = listOf(
        Color(0xFFF59E0B),
        Color(0xFFEAB308)
    )
    
    val premiumGradient = listOf(
        Color(0xFFFFD700),
        Color(0xFFB8860B)
    )
}

enum class ThemeMode {
    LIGHT, DARK, OLED_BLACK, DYNAMIC, AUTO
}

data class ThemeConfig(
    val mode: ThemeMode = ThemeMode.AUTO,
    val isDynamicColorEnabled: Boolean = true,
    val isHighContrastEnabled: Boolean = false,
    val fontScale: Float = 1.0f,
    val animationScale: Float = 1.0f
)

@Composable
fun PremiumVoltAITheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeConfig.isDynamicColorEnabled -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeConfig.mode == ThemeMode.OLED_BLACK -> OLEDBlackColorScheme
        themeConfig.mode == ThemeMode.LIGHT -> PremiumLightColorScheme
        themeConfig.mode == ThemeMode.DARK -> PremiumDarkColorScheme
        darkTheme -> PremiumDarkColorScheme
        else -> PremiumLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PremiumTypography,
        shapes = PremiumShapes,
        content = content
    )
}

// Extended color system for premium features
@Stable
class PremiumColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,
    val premium: Color,
    val onPremium: Color,
    val batteryGreen: Color,
    val batteryYellow: Color,
    val batteryRed: Color
) {
    companion object {
        fun light() = PremiumColors(
            success = Color(0xFF10B981),
            onSuccess = Color.White,
            warning = Color(0xFFF59E0B),
            onWarning = Color.White,
            info = Color(0xFF3B82F6),
            onInfo = Color.White,
            premium = Color(0xFFFFD700),
            onPremium = Color.Black,
            batteryGreen = Color(0xFF22C55E),
            batteryYellow = Color(0xFFFBBF24),
            batteryRed = Color(0xFFEF4444)
        )
        
        fun dark() = PremiumColors(
            success = Color(0xFF34D399),
            onSuccess = Color(0xFF065F46),
            warning = Color(0xFFFBBF24),
            onWarning = Color(0xFF92400E),
            info = Color(0xFF60A5FA),
            onInfo = Color(0xFF1E3A8A),
            premium = Color(0xFFFFD700),
            onPremium = Color.Black,
            batteryGreen = Color(0xFF4ADE80),
            batteryYellow = Color(0xFFFDE047),
            batteryRed = Color(0xFFF87171)
        )
    }
}

val LocalPremiumColors = staticCompositionLocalOf { PremiumColors.light() }

@Composable
fun ProvidePremiumColors(
    colors: PremiumColors,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPremiumColors provides colors) {
        content()
    }
}

// Accessibility helpers
@Composable
fun getAdaptiveColor(
    lightColor: Color,
    darkColor: Color,
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme()
): Color {
    return if (isSystemInDarkTheme) darkColor else lightColor
}

@Composable
fun getBatteryLevelColor(level: Int): Color {
    val premiumColors = LocalPremiumColors.current
    return when {
        level > 60 -> premiumColors.batteryGreen
        level > 20 -> premiumColors.batteryYellow
        else -> premiumColors.batteryRed
    }
}

@Composable
fun getHealthPercentageColor(percentage: Int): Color {
    val premiumColors = LocalPremiumColors.current
    return when {
        percentage >= 90 -> premiumColors.batteryGreen
        percentage >= 70 -> premiumColors.batteryYellow
        else -> premiumColors.batteryRed
    }
}
