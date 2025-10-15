package com.voltai.smartbatteryguardian.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Accessibility preferences for animation and motion control.
 * Respects system preferences for reduced motion and provides battery-efficient alternatives.
 */

/**
 * Check if reduced motion is preferred by the system or user
 */
@Composable
fun isReducedMotionPreferred(): Boolean {
    val context = LocalContext.current
    return remember {
        // Check system accessibility settings
        val contentResolver = context.contentResolver
        try {
            val animationScale =
                android.provider.Settings.Global.getFloat(
                    contentResolver,
                    android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                    1.0f,
                )
            animationScale == 0f
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Get animation spec based on accessibility preferences
 */
@Composable
fun <T> accessibleAnimationSpec(
    normal: AnimationSpec<T> = SpringSpec(),
    reduced: AnimationSpec<T> = TweenSpec(durationMillis = 0),
): AnimationSpec<T> {
    return if (isReducedMotionPreferred()) reduced else normal
}

/**
 * Battery-friendly animation duration
 * Reduces animation duration to save battery while maintaining visual feedback
 */
object AccessibleAnimations {
    const val QUICK_DURATION = 150
    const val NORMAL_DURATION = 300
    const val SLOW_DURATION = 500

    /**
     * Get animation duration based on accessibility and battery preferences
     */
    @Composable
    fun getDuration(
        normalDuration: Int = NORMAL_DURATION,
        quickDuration: Int = QUICK_DURATION,
    ): Int {
        return if (isReducedMotionPreferred()) 0 else quickDuration
    }
}

/**
 * Accessibility-aware alpha values
 * Provides higher contrast when accessibility is enabled
 */
object AccessibleAlpha {
    const val DISABLED_NORMAL = 0.38f
    const val DISABLED_HIGH_CONTRAST = 0.12f
    const val SUBTLE_NORMAL = 0.12f
    const val SUBTLE_HIGH_CONTRAST = 0.24f

    @Composable
    fun getDisabledAlpha(highContrast: Boolean = false): Float {
        return if (highContrast) DISABLED_HIGH_CONTRAST else DISABLED_NORMAL
    }

    @Composable
    fun getSubtleAlpha(highContrast: Boolean = false): Float {
        return if (highContrast) SUBTLE_HIGH_CONTRAST else SUBTLE_NORMAL
    }
}
