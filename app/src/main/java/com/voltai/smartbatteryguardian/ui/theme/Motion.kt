package com.voltai.smartbatteryguardian.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable

/**
 * Material Design 3 compliant motion system.
 *
 * Provides purposeful, efficient animations that respect accessibility
 * settings and system preferences for reduced motion.
 */
object VoltAIMotion {
    // Standard Material Design 3 durations
    object Duration {
        const val Short1 = 50
        const val Short2 = 100
        const val Short3 = 150
        const val Short4 = 200
        const val Medium1 = 250
        const val Medium2 = 300
        const val Medium3 = 350
        const val Medium4 = 400
        const val Long1 = 450
        const val Long2 = 500
    }

    // Standard Material Design 3 easing curves
    object Easing {
        val Standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val Decelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        val Accelerate = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
        val Emphasize = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    }

    // Standard animation specs
    val standardTween =
        tween<Float>(
            durationMillis = Duration.Short3,
            easing = Easing.Standard,
        )

    val quickTween =
        tween<Float>(
            durationMillis = Duration.Short2,
            easing = Easing.Standard,
        )

    val emphasizedTween =
        tween<Float>(
            durationMillis = Duration.Medium2,
            easing = Easing.Emphasize,
        )

    // Button press feedback - quick and subtle
    val buttonPress =
        tween<Float>(
            durationMillis = Duration.Short2,
            easing = Easing.Decelerate,
        )

    // Card interactions - smooth but not distracting
    val cardInteraction =
        tween<Float>(
            durationMillis = Duration.Short3,
            easing = Easing.Standard,
        )

    // Content appearance - respectful of user's time
    val contentAppearance =
        tween<Float>(
            durationMillis = Duration.Short4,
            easing = Easing.Decelerate,
        )
}

/**
 * Provides motion specs that respect accessibility settings.
 * Returns instant animations if reduced motion is enabled.
 */
@Composable
fun rememberAccessibleMotionSpec(defaultSpec: AnimationSpec<Float> = VoltAIMotion.standardTween): AnimationSpec<Float> {
    val isReducedMotionEnabled = isReducedMotionPreferred()

    return if (isReducedMotionEnabled) {
        // Instant animations for accessibility
        snap()
    } else {
        defaultSpec
    }
}

/**
 * Accessibility-aware animation spec factory.
 */
object AccessibleMotion {
    @Composable
    fun button(): AnimationSpec<Float> = rememberAccessibleMotionSpec(VoltAIMotion.buttonPress)

    @Composable
    fun card(): AnimationSpec<Float> = rememberAccessibleMotionSpec(VoltAIMotion.cardInteraction)

    @Composable
    fun content(): AnimationSpec<Float> = rememberAccessibleMotionSpec(VoltAIMotion.contentAppearance)

    @Composable
    fun emphasized(): AnimationSpec<Float> = rememberAccessibleMotionSpec(VoltAIMotion.emphasizedTween)
}

/**
 * Standard enter/exit animations for Material Design 3
 */
object MaterialTransitions {
    @Composable
    fun standardFadeIn() =
        fadeIn(
            animationSpec =
            tween(
                durationMillis = VoltAIMotion.Duration.Short3,
                easing = VoltAIMotion.Easing.Standard,
            ),
        )

    @Composable
    fun standardFadeOut() =
        fadeOut(
            animationSpec =
            tween(
                durationMillis = VoltAIMotion.Duration.Short2,
                easing = VoltAIMotion.Easing.Standard,
            ),
        )

    @Composable
    fun standardSlideInFromBottom() =
        slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec =
            tween(
                durationMillis = VoltAIMotion.Duration.Medium2,
                easing = VoltAIMotion.Easing.Decelerate,
            ),
        )

    @Composable
    fun standardSlideOutToBottom() =
        slideOutVertically(
            targetOffsetY = { it / 4 },
            animationSpec =
            tween(
                durationMillis = VoltAIMotion.Duration.Short4,
                easing = VoltAIMotion.Easing.Accelerate,
            ),
        )
}

/**
 * Standard spring specs - use sparingly and only for appropriate interactions
 */
object MaterialSprings {
    // Light spring for subtle feedback
    val gentle =
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        )

    // Standard spring for normal interactions
    val standard =
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )
}
