package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.ui.animation.AnimationSpecs
import com.voltai.smartbatteryguardian.ui.animation.AnimatedCardEntrance
import com.voltai.smartbatteryguardian.ui.CardContent
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomizableCardsGrid(
    cards: List<HomeCard>,
    batteryData: BatteryData,
    isEditMode: Boolean,
    isDragActive: Boolean,
    selectedCard: HomeCard?,
    onCardClick: (HomeCard) -> Unit,
    onCardLongPress: (HomeCard) -> Unit,
    onCardRemove: (String) -> Unit,
    onCardMove: (String, Offset) -> Unit,
    onCardResize: (String, CardSize) -> Unit,
    onDragStart: (String) -> Unit,
    onDragEnd: () -> Unit,
    onAddCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Performance optimization: Reduce animation complexity with many cards
    val shouldUseReducedAnimations = cards.size > 8
    val haptic = LocalHapticFeedback.current
    
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            bottom = if (isEditMode) 100.dp else 16.dp
        )
    ) {
        items(
            items = cards.filter { it.isVisible },
            key = { it.id }
        ) { card ->
            AnimatedCardEntrance(
                visible = true,
                delay = if (shouldUseReducedAnimations) 0 else cards.indexOf(card) * 30
            ) {
                DraggableCard(
                    card = card,
                    batteryData = batteryData,
                    isEditMode = isEditMode,
                    isSelected = selectedCard?.id == card.id,
                    isDragActive = isDragActive,
                    onCardClick = { onCardClick(card) },
                    onCardLongPress = { onCardLongPress(card) },
                    onCardRemove = { onCardRemove(card.id) },
                    onCardResize = { newSize -> onCardResize(card.id, newSize) },
                    onCardMove = { dragOffset -> onCardMove(card.id, dragOffset) },
                    onDragStart = { onDragStart(card.id) },
                    onDragEnd = onDragEnd,
                    modifier = Modifier
                        .animateItemPlacement(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                )
            }
        }
        
        // Add Card Placeholder in Edit Mode
        if (isEditMode && cards.filter { it.isVisible }.size < 12) {
            item {
                AddCardPlaceholder(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAddCardClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun DraggableCard(
    card: HomeCard,
    batteryData: BatteryData,
    isEditMode: Boolean,
    isSelected: Boolean,
    isDragActive: Boolean,
    onCardClick: () -> Unit,
    onCardLongPress: () -> Unit,
    onCardRemove: () -> Unit,
    onCardResize: (CardSize) -> Unit,
    onCardMove: (Offset) -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val haptic = LocalHapticFeedback.current
    
    // Animations
    val scale by animateFloatAsState(
        targetValue = when {
            isDragging -> 1.05f
            isPressed -> 0.98f
            isSelected -> 1.02f
            else -> 1f
        },
        animationSpec = AnimationSpecs.snappy
    )
    
    val elevation by animateDpAsState(
        targetValue = when {
            isDragging -> 16.dp
            isSelected && isEditMode -> 8.dp
            isEditMode -> 6.dp
            else -> 4.dp
        },
        animationSpec = tween(300)
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected && isEditMode -> Color(0xFF2196F3)
            isEditMode -> Color(0xFF2196F3).copy(alpha = 0.3f)
            else -> Color.Transparent
        },
        animationSpec = tween(200)
    )
    
    val animatedOffset by animateOffsetAsState(
        targetValue = if (isDragging) dragOffset else Offset.Zero,
        animationSpec = if (isDragging) {
            tween(0) // Immediate during drag
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        }
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .offset {
                IntOffset(
                    animatedOffset.x.roundToInt(),
                    animatedOffset.y.roundToInt()
                )
            }
            .zIndex(if (isDragging) 1f else 0f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = getCardHeight(card.cardType, card.size))
                .shadow(elevation, RoundedCornerShape(20.dp))
                .border(
                    width = if (isEditMode) 2.dp else 0.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .pointerInput(isEditMode, card.id) {
                    if (isEditMode) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                onDragStart()
                                // Enhanced haptic for drag start
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragEnd = {
                                isDragging = false
                                // Calculate new position based on drag offset
                                val dragDistance = sqrt(dragOffset.x * dragOffset.x + dragOffset.y * dragOffset.y)
                                if (dragDistance > 50f) { // Minimum drag distance to trigger reorder
                                    onCardMove(dragOffset)
                                    // Success haptic for successful reorder
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } else {
                                    // Light haptic for cancelled drag
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                dragOffset = Offset.Zero
                                onDragEnd()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount
                            }
                        )
                    }
                }
                .clickable(enabled = !isDragging) {
                    if (isEditMode) {
                        onCardLongPress()
                    } else {
                        onCardClick()
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Card Content
                CardContent(
                    cardType = card.cardType,
                    batteryData = batteryData,
                    isEditMode = isEditMode
                )
                
                // Edit Mode Overlay
                if (isEditMode) {
                    EditModeOverlay(
                        card = card,
                        isSelected = isSelected,
                        onRemove = onCardRemove,
                        onResize = onCardResize,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun EditModeOverlay(
    card: HomeCard,
    isSelected: Boolean,
    onRemove: () -> Unit,
    onResize: (CardSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Remove Button
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onRemove() },
                shape = CircleShape,
                color = Color(0xFFFF5252),
                shadowElevation = 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Card",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // Resize Handle (bottom-right)
        if (card.cardType.allowedSizes.size > 1) {
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            // Cycle through available sizes
                            val currentIndex = card.cardType.allowedSizes.indexOf(card.size)
                            val nextIndex = (currentIndex + 1) % card.cardType.allowedSizes.size
                            val newSize = card.cardType.allowedSizes.elementAt(nextIndex)
                            onResize(newSize)
                        },
                    shape = CircleShape,
                    color = Color(0xFF2196F3),
                    shadowElevation = 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                        imageVector = Icons.Default.MoreVert,
                            contentDescription = "Resize Card",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
        
        // Edit Mode Overlay Background
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.1f),
                        RoundedCornerShape(20.dp)
                    )
            )
        }
    }
}

@Composable
private fun AddCardPlaceholder(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = Color(0xFF2196F3).copy(alpha = 0.3f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2196F3).copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Card",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Text(
                    text = "Add Card",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

private fun getCardHeight(cardType: CardType, size: CardSize): Dp {
    // Dynamic height based on card size
    val baseHeight = when (size) {
        CardSize.Small -> 110.dp
        CardSize.Medium -> 150.dp
        CardSize.Large -> 200.dp
        CardSize.Wide -> 180.dp
    }
    
    // Adjust height based on card content complexity
    val contentMultiplier = when (cardType) {
        CardType.BATTERY_LEVEL -> 1.0f
        CardType.CHARGING_STATUS -> 1.1f
        CardType.TEMPERATURE -> 1.0f
        CardType.HEALTH -> 1.2f
        CardType.CURRENT_VOLTAGE -> 1.0f
        CardType.SCREEN_TIME -> 1.2f
        CardType.APP_USAGE -> 1.4f
        CardType.CHARGING_HISTORY -> 1.2f
        CardType.AI_INSIGHTS -> 1.5f
        CardType.POWER_MODES -> 1.1f
        CardType.QUICK_ACTIONS -> 1.3f
        CardType.PERFORMANCE_CHART -> 1.4f
        CardType.THERMAL_STATUS -> 1.0f
        CardType.BATTERY_TIPS -> 1.3f
        CardType.HISTORY -> 1.2f
        CardType.OVERLAYS -> 1.1f
        // Premium Cards
        CardType.CHARGING_ANALYTICS -> 1.5f
        CardType.CHEMISTRY_LAB -> 1.4f
        CardType.SYSTEM_OPTIMIZATION -> 1.5f
        CardType.PERFORMANCE_BENCHMARKS -> 1.6f
        CardType.DATA_EXPORT_IMPORT -> 1.3f
        
        // New Premium Analytics Cards
        CardType.BATTERY_HEALTH_ANALYTICS -> 1.6f
        CardType.SMART_CHARGING -> 1.5f
        CardType.BATTERY_DIAGNOSTICS -> 1.4f
        CardType.REAL_TIME_MONITORING -> 1.5f
        CardType.BATTERY_OPTIMIZATION -> 1.4f
    }
    
    return baseHeight * contentMultiplier
}

@Composable
private fun animateOffsetAsState(
    targetValue: Offset,
    animationSpec: AnimationSpec<Offset>
): State<Offset> {
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = Offset.VectorConverter,
        animationSpec = animationSpec
    )
}
