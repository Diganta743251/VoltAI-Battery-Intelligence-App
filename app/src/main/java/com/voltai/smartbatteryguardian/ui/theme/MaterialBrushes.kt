package com.voltai.smartbatteryguardian.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 compliant gradients using semantic colors from the theme
 * All gradients adapt to dynamic color schemes and theme changes
 */
@Stable
object MaterialBrushes {
    /**
     * Primary surface gradient using primary and primary container colors
     * Automatically falls back to solid color when reduced motion is enabled
     */
    @Composable
    fun primaryGradient(
        startOffset: Offset = Offset(0f, 0f),
        endOffset: Offset = Offset(1000f, 1000f),
    ): Brush {
        val reduceMotion = isReducedMotionPreferred()
        return if (reduceMotion) {
            SolidColor(MaterialTheme.colorScheme.primary)
        } else {
            Brush.linearGradient(
                colors =
                listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primaryContainer,
                ),
                start = startOffset,
                end = endOffset,
            )
        }
    }

    /**
     * Secondary surface gradient using secondary and secondary container colors
     * Automatically falls back to solid color when reduced motion is enabled
     */
    @Composable
    fun secondaryGradient(
        startOffset: Offset = Offset(0f, 0f),
        endOffset: Offset = Offset(1000f, 1000f),
    ): Brush {
        val reduceMotion = isReducedMotionPreferred()
        return if (reduceMotion) {
            SolidColor(MaterialTheme.colorScheme.secondary)
        } else {
            Brush.linearGradient(
                colors =
                listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.secondaryContainer,
                ),
                start = startOffset,
                end = endOffset,
            )
        }
    }

    /**
     * Tertiary surface gradient using tertiary and tertiary container colors
     * Automatically falls back to solid color when reduced motion is enabled
     */
    @Composable
    fun tertiaryGradient(
        startOffset: Offset = Offset(0f, 0f),
        endOffset: Offset = Offset(1000f, 1000f),
    ): Brush {
        val reduceMotion = isReducedMotionPreferred()
        return if (reduceMotion) {
            androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.tertiary)
        } else {
            Brush.linearGradient(
                colors =
                listOf(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.tertiaryContainer,
                ),
                start = startOffset,
                end = endOffset,
            )
        }
    }

    /**
     * Surface gradient using surface variants for subtle elevation
     * Automatically falls back to solid color when reduced motion is enabled
     */
    @Composable
    fun surfaceGradient(
        startOffset: Offset = Offset(0f, 0f),
        endOffset: Offset = Offset(1000f, 1000f),
    ): Brush {
        val reduceMotion = isReducedMotionPreferred()
        return if (reduceMotion) {
            androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.surface)
        } else {
            Brush.linearGradient(
                colors =
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant,
                ),
                start = startOffset,
                end = endOffset,
            )
        }
    }

    /**
     * Battery health gradient using success colors from PremiumColors
     * Automatically falls back to solid color when reduced motion is enabled
     */
    @Composable
    fun batteryHealthGradient(
        startOffset: Offset = Offset(0f, 0f),
        endOffset: Offset = Offset(1000f, 1000f),
    ): Brush {
        val premiumColors = LocalPremiumColors.current
        val reduceMotion = isReducedMotionPreferred()
        return if (reduceMotion) {
            androidx.compose.ui.graphics.SolidColor(premiumColors.success)
        } else {
            Brush.linearGradient(
                colors =
                listOf(
                    premiumColors.success,
                    premiumColors.batteryGreen,
                ),
                start = startOffset,
                end = endOffset,
            )
        }
    }

    /**
     * Warning/charging gradient using warning colors
     */
    @Composable
    fun chargingGradient(
        startOffset: Offset = Offset(0f, 0f),
        endOffset: Offset = Offset(1000f, 1000f),
    ): Brush {
        val premiumColors = LocalPremiumColors.current
        return Brush.linearGradient(
            colors =
            listOf(
                premiumColors.warning,
                premiumColors.batteryYellow,
            ),
            start = startOffset,
            end = endOffset,
        )
    }

    /**
     * Error/critical gradient using error colors
     */
    @Composable
    fun errorGradient(
        startOffset: Offset = Offset(0f, 0f),
        endOffset: Offset = Offset(1000f, 1000f),
    ): Brush {
        val premiumColors = LocalPremiumColors.current
        return Brush.linearGradient(
            colors =
            listOf(
                MaterialTheme.colorScheme.error,
                premiumColors.batteryRed,
            ),
            start = startOffset,
            end = endOffset,
        )
    }

    /**
     * Subtle background gradient with transparency for cards/surfaces
     */
    @Composable
    fun cardGradient(
        startOffset: Offset = Offset(0f, 0f),
        endOffset: Offset = Offset(1000f, 1000f),
    ): Brush {
        return Brush.linearGradient(
            colors =
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            ),
            start = startOffset,
            end = endOffset,
        )
    }

    /**
     * Radial gradient for premium/accent elements
     */
    @Composable
    fun premiumRadialGradient(
        center: Offset = Offset(0f, 0f),
        radius: Float = 1000f,
    ): Brush {
        val premiumColors = LocalPremiumColors.current
        return Brush.radialGradient(
            colors =
            listOf(
                premiumColors.premium.copy(alpha = 0.8f),
                premiumColors.premium.copy(alpha = 0.4f),
                Color.Transparent,
            ),
            center = center,
            radius = radius,
            tileMode = TileMode.Clamp,
        )
    }
}

/**
 * Convenience modifiers for applying gradients with semantic meaning
 */
@Composable
fun Modifier.primaryGradientBackground(): Modifier {
    return this.background(MaterialBrushes.primaryGradient())
}

@Composable
fun Modifier.secondaryGradientBackground(): Modifier {
    return this.background(MaterialBrushes.secondaryGradient())
}

@Composable
fun Modifier.surfaceGradientBackground(): Modifier {
    return this.background(MaterialBrushes.surfaceGradient())
}

@Composable
fun Modifier.batteryHealthGradientBackground(): Modifier {
    return this.background(MaterialBrushes.batteryHealthGradient())
}

@Composable
fun Modifier.chargingGradientBackground(): Modifier {
    return this.background(MaterialBrushes.chargingGradient())
}

@Composable
fun Modifier.errorGradientBackground(): Modifier {
    return this.background(MaterialBrushes.errorGradient())
}

@Composable
fun Modifier.cardGradientBackground(): Modifier {
    return this.background(MaterialBrushes.cardGradient())
}

@Composable
fun Modifier.primaryGradientBorder(
    width: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(0.dp),
): Modifier {
    return this.border(
        width = width,
        brush = MaterialBrushes.primaryGradient(),
        shape = shape,
    )
}

@Composable
fun Modifier.secondaryGradientBorder(
    width: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(0.dp),
): Modifier {
    return this.border(
        width = width,
        brush = MaterialBrushes.secondaryGradient(),
        shape = shape,
    )
}
