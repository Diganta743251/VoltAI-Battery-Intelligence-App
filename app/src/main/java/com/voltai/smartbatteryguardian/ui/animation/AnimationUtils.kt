package com.voltai.smartbatteryguardian.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Animation specifications for different types of animations
 */
object AnimationSpecs {
    
    // Spring animations for natural feel
    val bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val smooth = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    // Tween animations for precise control
    val quick = tween<Float>(
        durationMillis = 200,
        easing = FastOutSlowInEasing
    )
    
    val medium = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
    
    val slow = tween<Float>(
        durationMillis = 500,
        easing = FastOutSlowInEasing
    )
    
    // Entrance animations
    val slideInFromBottom = slideInVertically(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        initialOffsetY = { it }
    )
    
    val scaleInAnimation = scaleIn(
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        initialScale = 0.8f
    ) + fadeIn(animationSpec = tween(200))
    
    // Exit animations
    val slideOutToBottom = slideOutVertically(
        animationSpec = tween(250, easing = FastOutLinearInEasing),
        targetOffsetY = { it }
    )
    
    val scaleOutAnimation = scaleOut(
        animationSpec = tween(150, easing = FastOutLinearInEasing),
        targetScale = 0.8f
    ) + fadeOut(animationSpec = tween(150))
    
    // Advanced animations for premium experience
    val premium = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = 400f
    )
    
    val elastic = spring<Float>(
        dampingRatio = 0.6f,
        stiffness = 600f
    )
    
    val superSmooth = spring<Float>(
        dampingRatio = 1.0f,
        stiffness = 350f
    )
    
    // Card movement animations
    val cardReorder = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 500f
    )
    
    // Screen transitions
    val screenTransition = tween<Float>(
        durationMillis = 350,
        easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
    )
}

/**
 * Modifier for drag and drop functionality with smooth animations
 */
fun Modifier.draggableCard(
    enabled: Boolean = true,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDrag: (Offset) -> Unit = {}
) = composed {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    val animatedOffset by animateOffsetAsState(
        targetValue = if (isDragging) offset else Offset.Zero,
        animationSpec = if (isDragging) {
            tween(16, easing = LinearEasing) // Smooth during drag
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        }
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = AnimationSpecs.snappy
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 4.dp,
        animationSpec = tween(200)
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
                    }
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
    onLongPress: () -> Unit = {}
) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = AnimationSpecs.snappy
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
                    onDrag = { _, _ -> }
                )
            }
    } else {
        this
    }
}

/**
 * Shimmer effect for loading states
 */
fun Modifier.shimmerEffect(
    enabled: Boolean = true,
    duration: Int = 1200
) = composed {
    if (enabled) {
        val transition = rememberInfiniteTransition()
        val shimmerOffset by transition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        
        this.graphicsLayer {
            translationX = shimmerOffset * size.width
            alpha = 0.7f + 0.3f * (1f + shimmerOffset) / 2f
        }
    } else {
        this
    }
}

/**
 * Parallax effect for background elements
 */
fun Modifier.parallaxEffect(
    scrollOffset: Float,
    parallaxRatio: Float = 0.5f
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
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delay,
                easing = OvershootInterpolator(1.2f)::getInterpolation
            ),
            initialScale = 0f
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = delay
            )
        ),
        exit = scaleOut(
            animationSpec = tween(200),
            targetScale = 0f
        ) + fadeOut(animationSpec = tween(200))
    ) {
        content()
    }
}

/**
 * Custom interpolator for overshoot animation
 */
class OvershootInterpolator(private val tension: Float = 2f) {
    fun getInterpolation(input: Float): Float {
        return (input - 1) * (input - 1) * ((tension + 1) * (input - 1) + tension) + 1
    }
}

/**
 * Haptic feedback helper functions
 */
object HapticHelper {
    fun HapticFeedback.performLightImpact() {
        performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun HapticFeedback.performMediumImpact() {
        performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun HapticFeedback.performHeavyImpact() {
        performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun HapticFeedback.performSuccess() {
        // Simulate success pattern with multiple quick feedbacks
        performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun HapticFeedback.performError() {
        // Simulate error pattern
        performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

/**
 * Animation for morphing between different card sizes
 */
@Composable
fun animateCardSize(
    targetSize: androidx.compose.ui.unit.DpSize,
    animationSpec: AnimationSpec<androidx.compose.ui.unit.Dp> = tween(300)
): androidx.compose.ui.unit.DpSize {
    val animatedWidth by animateDpAsState(
        targetValue = targetSize.width,
        animationSpec = animationSpec
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetSize.height,
        animationSpec = animationSpec
    )
    return androidx.compose.ui.unit.DpSize(animatedWidth, animatedHeight)
}

/**
 * Custom animation for smooth offset changes
 */
@Composable
fun animateOffsetAsState(
    targetValue: Offset,
    animationSpec: AnimationSpec<Offset> = spring()
): State<Offset> {
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = Offset.VectorConverter,
        animationSpec = animationSpec
    )
}

/**
 * Staggered animation for multiple cards
 */
@Composable
fun StaggeredAnimation(
    itemCount: Int,
    staggerDelay: Int = 50,
    content: @Composable (index: Int, animationDelay: Int) -> Unit
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
    content: @Composable (Boolean) -> Unit
) {
    val transition = updateTransition(targetState = targetState)
    
    val cornerRadius by transition.animateDp(
        transitionSpec = { tween(300) }
    ) { state ->
        if (state) 24.dp else 12.dp
    }
    
    val elevation by transition.animateDp(
        transitionSpec = { tween(300) }
    ) { state ->
        if (state) 8.dp else 2.dp
    }
    
    content(targetState)
}
