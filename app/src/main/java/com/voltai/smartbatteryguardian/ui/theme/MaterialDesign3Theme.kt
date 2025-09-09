package com.voltai.smartbatteryguardian.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 compliant theme system
 * Following official MD3 design tokens and guidelines
 */

// Material Design 3 Shape Tokens (Official)
object MaterialShapes {
    val extraSmall = RoundedCornerShape(4.dp) // Corner radius: 4dp
    val small = RoundedCornerShape(8.dp) // Corner radius: 8dp
    val medium = RoundedCornerShape(12.dp) // Corner radius: 12dp
    val large = RoundedCornerShape(16.dp) // Corner radius: 16dp
    val extraLarge = RoundedCornerShape(28.dp) // Corner radius: 28dp
}

// Material Design 3 Spacing System (8dp grid)
object MaterialSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
}

// Material Design 3 Animation Durations (Official)
object MaterialMotion {
    const val short1 = 50 // Micro-interactions
    const val short2 = 100 // Small utility animations
    const val medium1 = 250 // Standard animations
    const val medium2 = 300 // Longer utility animations
    const val long1 = 400 // Complex animations
    const val long2 = 500 // Large/complex animations
}

/**
 * Material Design 3 compliant color scheme
 * Uses semantic color tokens that adapt to light/dark theme
 */
@Immutable
data class ExtendedColorScheme(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

// Light theme extended colors
val extendedLightColors = ExtendedColorScheme(
    success = Color(0xFF006D3C),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFF89F8B7),
    onSuccessContainer = Color(0xFF00210F),
    warning = Color(0xFF7D5700),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFE16A),
    onWarningContainer = Color(0xFF271900),
    info = Color(0xFF0061A4),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFD1E4FF),
    onInfoContainer = Color(0xFF001D36),
)

// Dark theme extended colors
val extendedDarkColors = ExtendedColorScheme(
    success = Color(0xFF6CDB9B),
    onSuccess = Color(0xFF00391D),
    successContainer = Color(0xFF00522C),
    onSuccessContainer = Color(0xFF89F8B7),
    warning = Color(0xFFD4C442),
    onWarning = Color(0xFF3E2E00),
    warningContainer = Color(0xFF5B4200),
    onWarningContainer = Color(0xFFFFE16A),
    info = Color(0xFF9ECAFF),
    onInfo = Color(0xFF003258),
    infoContainer = Color(0xFF00497D),
    onInfoContainer = Color(0xFFD1E4FF),
)

/**
 * Clean, minimal animation specs for Material Design 3
 * Removes excessive animations and uses purposeful transitions
 */
object MaterialAnimationSpecs {
    // Standard fade animation - subtle and purposeful
    @Composable
    fun standardFadeIn() = fadeIn(
        animationSpec = tween(
            durationMillis = MaterialMotion.medium1,
            easing = FastOutSlowInEasing
        )
    )

    @Composable
    fun standardFadeOut() = fadeOut(
        animationSpec = tween(
            durationMillis = MaterialMotion.short2,
            easing = LinearOutSlowInEasing
        )
    )

    // Standard slide animation - for navigation and panel changes
    @Composable
    fun standardSlideUp() = slideInVertically(
        animationSpec = tween(
            durationMillis = MaterialMotion.medium2,
            easing = FastOutSlowInEasing
        )
    ) { it / 4 } // Subtle slide, not full height

    @Composable
    fun standardSlideDown() = slideOutVertically(
        animationSpec = tween(
            durationMillis = MaterialMotion.medium1,
            easing = LinearOutSlowInEasing
        )
    ) { it / 4 }
}

/**
 * Material Design 3 compliant theme
 * Removes custom gradients and uses proper color tokens
 */
@Composable
fun VoltAIMaterial3Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF6EDBFF),
            onPrimary = Color(0xFF003544),
            primaryContainer = Color(0xFF004D61),
            onPrimaryContainer = Color(0xFFBEEAFF),
            secondary = Color(0xFFB1CAD6),
            onSecondary = Color(0xFF1C3438),
            secondaryContainer = Color(0xFF324B4F),
            onSecondaryContainer = Color(0xFFCDE6F2),
            surface = Color(0xFF0E1415),
            onSurface = Color(0xFFDDE3E0),
            surfaceVariant = Color(0xFF40484C),
            onSurfaceVariant = Color(0xFFC0C8CC),
            background = Color(0xFF0E1415),
            onBackground = Color(0xFFDDE3E0),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF006781),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFBEEAFF),
            onPrimaryContainer = Color(0xFF001E2A),
            secondary = Color(0xFF4A6267),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCDE6F2),
            onSecondaryContainer = Color(0xFF051F23),
            surface = Color(0xFFF5FFFE),
            onSurface = Color(0xFF171D1E),
            surfaceVariant = Color(0xFFDCE4E9),
            onSurfaceVariant = Color(0xFF40484C),
            background = Color(0xFFF5FFFE),
            onBackground = Color(0xFF171D1E),
        )
    }

    val shapes = Shapes(
        extraSmall = MaterialShapes.extraSmall,
        small = MaterialShapes.small,
        medium = MaterialShapes.medium,
        large = MaterialShapes.large,
        extraLarge = MaterialShapes.extraLarge
    )

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = Typography(), // Use default Material typography
        content = content
    )
}
