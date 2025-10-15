package com.voltai.smartbatteryguardian.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Battery-efficient animation specifications with accessibility support
 * @deprecated Use VoltAIMotion from Motion.kt instead for better accessibility and battery efficiency
 */
@Deprecated("Use VoltAIMotion from Motion.kt", ReplaceWith("VoltAIMotion"))
object AnimationSpecs {
    // Battery Guru Style: 800ms EaseOutCubic for all major animations
    val batteryGuru =
        tween<Float>(
            durationMillis = 800,
            easing = EaseOutCubic,
        )

    // Int-based tween for slide animations
    val batteryGuruInt =
        tween<Int>(
            durationMillis = 800,
            easing = EaseOutCubic,
        )

    // Quick interactions (200ms for immediate feedback)
    val snappy =
        tween<Float>(
            durationMillis = 200,
            easing = EaseOutCubic,
        )

    // Standard Battery Guru timing for all main animations
    val bouncy = batteryGuru
    val smooth = batteryGuru
    val quick = snappy // Keep quick for immediate feedback
    val medium = batteryGuru
    val slow = batteryGuru

    // Battery Guru Style entrance animations
    val slideInFromBottom =
        slideInVertically(
            initialOffsetY = { it },
        )

    val scaleInAnimation =
        scaleIn(
            animationSpec = batteryGuru,
            initialScale = 0.8f,
        ) + fadeIn(animationSpec = batteryGuru)

    // Battery Guru Style exit animations (slightly faster for responsiveness)
    val slideOutToBottom =
        slideOutVertically(
            targetOffsetY = { it },
        )

    val scaleOutAnimation =
        scaleOut(
            animationSpec = tween(400, easing = EaseInCubic),
            targetScale = 0.8f,
        ) + fadeOut(animationSpec = tween(400, easing = EaseInCubic))

    // Battery Guru Style - all premium animations use 800ms EaseOutCubic
    val premium = batteryGuru
    val elastic = batteryGuru
    val superSmooth = batteryGuru

    // Card movement animations - Battery Guru style
    val cardReorder = batteryGuru

    // Screen transitions - Battery Guru style
    val screenTransition = batteryGuru
}

/**
 * Modifier for drag and drop functionality with smooth animations
 */
fun Modifier.draggableCard(
    enabled: Boolean = true,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
) = composed {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val animatedOffset by animateOffsetAsState(
        targetValue = if (isDragging) offset else Offset.Zero,
        animationSpec =
        if (isDragging) {
            tween(16, easing = LinearEasing) // Smooth during drag
        } else {
            tween(
                durationMillis = 200,
                easing = FastOutSlowInEasing,
            )
        },
    )

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = AnimationSpecs.snappy,
    )

    val elevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 4.dp,
        animationSpec = tween(200),
    )

    if (enabled) {
        this
            .scale(scale)
            .shadow(elevation)
            .offset { IntOffset(animatedOffset.x.roundToInt(), animatedOffset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onDragStart()
                    },
                    onDragEnd = {
                        isDragging = false
                        offset = Offset.Zero
                        onDragEnd()
                    },
                    onDrag = { _, dragAmount ->
                        offset += dragAmount
                        onDrag(dragAmount)
                    },
                )
            }
    } else {
        this
    }
}

/**
 * Modifier for press interactions with haptic feedback
 */
fun Modifier.pressableCard(
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {},
) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = AnimationSpecs.snappy,
    )

    if (enabled) {
        this
            .scale(scale)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    },
                    onDragEnd = {
                        isPressed = false
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    },
                    onDrag = { _, _ -> },
                )
            }
    } else {
        this
    }
}

/**
 * Professional loading state with subtle Battery Guru animation
 * Uses 800ms EaseOutCubic timing for consistency
 */
fun Modifier.loadingEffect(enabled: Boolean = true) =
    composed {
        val alpha by animateFloatAsState(
            targetValue = if (enabled) 0.6f else 1f,
            animationSpec = AnimationSpecs.batteryGuru,
        )

        val scale by animateFloatAsState(
            targetValue = if (enabled) 0.98f else 1f,
            animationSpec = AnimationSpecs.batteryGuru,
        )

        this
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
    }

/**
 * Parallax effect for background elements
 */
fun Modifier.parallaxEffect(
    scrollOffset: Float,
    parallaxRatio: Float = 0.5f,
) = composed {
    this.graphicsLayer {
        translationY = scrollOffset * parallaxRatio
    }
}

/**
 * Bouncy entrance animation for new cards
 */
@Composable
fun AnimatedCardEntrance(
    visible: Boolean,
    delay: Int = 0,
    content: @Composable () -> Unit,
) {
    // Professional entrance animation with sophisticated easing
    val dynamicDuration = (800 + (delay * 0.2f)).toInt().coerceAtMost(1000)

    AnimatedVisibility(
        visible = visible,
        enter =
        scaleIn(
            animationSpec =
            tween(
                durationMillis = dynamicDuration, // Dynamic timing based on delay
                delayMillis = delay,
                easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f), // Custom premium easing
            ),
            initialScale = 0.7f, // Slightly larger initial scale for impact
        ) +
            fadeIn(
                animationSpec =
                tween(
                    durationMillis = dynamicDuration,
                    delayMillis = delay,
                    easing = EaseOutCubic,
                ),
            ) +
            slideInVertically(
                animationSpec =
                tween(
                    durationMillis = dynamicDuration,
                    delayMillis = delay,
                    easing = EaseOutCubic,
                ),
                initialOffsetY = { it / 3 }, // More pronounced slide for elegance
            ),
        exit =
        scaleOut(
            animationSpec = tween(400, easing = EaseInOutCubic),
            targetScale = 0.9f,
        ) +
            fadeOut(
                animationSpec = tween(400, easing = EaseInOutCubic),
            ) +
            slideOutVertically(
                animationSpec = tween(400, easing = EaseInOutCubic),
                targetOffsetY = { -it / 6 }, // Subtle upward exit
            ),
    ) {
        content()
    }
}

/**
 * @deprecated Custom interpolators consume unnecessary CPU. Use standard Material easing functions.
 * Replace with FastOutSlowInEasing, LinearOutSlowInEasing, or other Material Design easing.
 */
@Deprecated("Use Material Design standard easing functions", ReplaceWith("FastOutSlowInEasing"))
class OvershootInterpolator(private val tension: Float = 2f) {
    fun getInterpolation(input: Float): Float {
        // Simplified for better battery performance
        return FastOutSlowInEasing.transform(input)
    }
}

/**
 * Enhanced haptic feedback for Battery Guru professional feel
 */
object BatteryGuruHaptics {
    fun HapticFeedback.cardClick() = performHapticFeedback(HapticFeedbackType.TextHandleMove)

    fun HapticFeedback.cardLongPress() = performHapticFeedback(HapticFeedbackType.LongPress)

    fun HapticFeedback.dragStart() = performHapticFeedback(HapticFeedbackType.LongPress)

    fun HapticFeedback.dragDrop() = performHapticFeedback(HapticFeedbackType.TextHandleMove)

    fun HapticFeedback.batteryAlert() = performHapticFeedback(HapticFeedbackType.LongPress)

    fun HapticFeedback.success() = performHapticFeedback(HapticFeedbackType.TextHandleMove)
}

/**
 * Animation for morphing between different card sizes
 */
@Composable
fun animateCardSize(
    targetSize: androidx.compose.ui.unit.DpSize,
    animationSpec: AnimationSpec<androidx.compose.ui.unit.Dp> = tween(300),
): androidx.compose.ui.unit.DpSize {
    val animatedWidth by animateDpAsState(
        targetValue = targetSize.width,
        animationSpec = animationSpec,
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetSize.height,
        animationSpec = animationSpec,
    )
    return androidx.compose.ui.unit.DpSize(animatedWidth, animatedHeight)
}

/**
 * Custom animation for smooth offset changes
 */
@Composable
fun animateOffsetAsState(
    targetValue: Offset,
    animationSpec: AnimationSpec<Offset> = spring(),
): State<Offset> {
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = Offset.VectorConverter,
        animationSpec = animationSpec,
    )
}

/**
 * Staggered animation for multiple cards
 */
@Composable
fun StaggeredAnimation(
    itemCount: Int,
    staggerDelay: Int = 50,
    content: @Composable (index: Int, animationDelay: Int) -> Unit,
) {
    repeat(itemCount) { index ->
        content(index, index * staggerDelay)
    }
}

/**
 * Morphing container animation between different states
 */
@Composable
fun MorphingContainer(
    targetState: Boolean,
    content: @Composable (Boolean) -> Unit,
) {
    val transition = updateTransition(targetState = targetState)

    val cornerRadius by transition.animateDp(
        transitionSpec = { tween(300) },
    ) { state ->
        if (state) 24.dp else 12.dp
    }

    val elevation by transition.animateDp(
        transitionSpec = { tween(300) },
    ) { state ->
        if (state) 8.dp else 2.dp
    }

    content(targetState)
}
