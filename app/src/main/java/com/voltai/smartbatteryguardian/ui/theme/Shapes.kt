package com.voltai.smartbatteryguardian.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 shape tokens for VoltAI app.
 *
 * Following Material Design 3 guidelines, these shape tokens provide
 * a consistent, predictable system for shape throughout the app.
 */
val AppShapes =
    Shapes(
        // MD3 shape scale
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )
