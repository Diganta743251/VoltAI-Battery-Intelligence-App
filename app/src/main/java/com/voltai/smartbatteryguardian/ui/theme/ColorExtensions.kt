package com.voltai.smartbatteryguardian.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Material Design 3 semantic color extensions for VoltAI
 * Maps common UI patterns to appropriate Material color roles
 */

// Semantic color mapping for battery states - accessibility aware
@Composable
fun ColorScheme.batteryLevelColor(level: Int): Color =
    when {
        level > 80 -> primary // Normal/healthy level - use primary for positive indication
        level > 50 -> tertiary // Medium level - amber/yellow tone
        level > 20 -> error // Low level - warning
        else -> error // Critical level - error
    }

@Composable
fun ColorScheme.temperatureColor(temperature: Float): Color =
    when {
        temperature > 45f -> error // Hot - danger
        temperature > 35f -> tertiary // Warm - warning
        else -> onSurfaceVariant // Normal - neutral
    }

@Composable
fun ColorScheme.healthStatusColor(percentage: Int): Color =
    when {
        percentage > 80 -> primary // Excellent health - positive indication
        percentage > 60 -> tertiary // Good health
        percentage > 40 -> error // Fair health - warning
        else -> error // Poor health - critical
    }

// Semantic colors for common UI elements
val ColorScheme.mutedText: Color
    get() = onSurfaceVariant

val ColorScheme.secondaryText: Color
    get() = onSurfaceVariant.copy(alpha = 0.7f)

val ColorScheme.placeholderBackground: Color
    get() = surfaceVariant

val ColorScheme.cardBackground: Color
    get() = surface

val ColorScheme.editModeOverlay: Color
    get() = scrim.copy(alpha = 0.05f)

// Progress and indicator colors
val ColorScheme.progressBackground: Color
    get() = surfaceVariant

val ColorScheme.positiveIndicator: Color
    get() = onSurfaceVariant

val ColorScheme.chargingIndicator: Color
    get() = primary

// Legacy color mappings (for gradual migration)
object LegacyColorMapping {
    val darkGray = "onSurfaceVariant"
    val lightGray = "surfaceVariant"
    val mediumGray = "onSurfaceVariant.copy(alpha = 0.7f)"
    val green = "onSurfaceVariant (or primary for active states)"
    val red = "error"
    val orange = "tertiary"
    val blue = "secondary"
    val purple = "tertiary"
    val backgroundLight = "surface"
    val backgroundDark = "surface"
}
