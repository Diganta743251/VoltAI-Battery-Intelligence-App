package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.ui.home.*

/**
 * Material Design 3 compliant cards grid implementation.
 * 
 * Removes excessive animations while maintaining essential interaction feedback.
 * Uses standard Material components and follows accessibility guidelines.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MaterialCardsGrid(
    cards: List<HomeCard>,
    batteryData: BatteryData,
    isEditMode: Boolean,
    selectedCard: HomeCard?,
    onCardClick: (HomeCard) -> Unit,
    onCardLongPress: (HomeCard) -> Unit,
    onCardRemove: (String) -> Unit,
    onAddCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            bottom = if (isEditMode) 80.dp else 16.dp
        )
    ) {
        items(
            items = cards.filter { it.isVisible },
            key = { it.id }
        ) { card ->
            MaterialCard(
                card = card,
                batteryData = batteryData,
                isEditMode = isEditMode,
                isSelected = selectedCard?.id == card.id,
                onCardClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCardClick(card) 
                },
                onCardLongPress = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCardLongPress(card) 
                },
                onCardRemove = { onCardRemove(card.id) },
                modifier = Modifier.animateItemPlacement()
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialCard(
    card: HomeCard,
    batteryData: BatteryData,
    isEditMode: Boolean,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onCardLongPress: () -> Unit,
    onCardRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple scale animation for press feedback
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "card_scale"
    )

    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .height(getCardHeight(card.cardType, card.size))
            .scale(scale)
            .zIndex(if (isSelected) 1f else 0f),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && isEditMode) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEditMode && isSelected) 8.dp else 4.dp
        ),
        border = if (isEditMode && isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onCardClick,
                    onLongClick = onCardLongPress
                )
                .indication(null, null) // Remove ripple since Card already handles it
        ) {
            // Card Content
            MaterialCardContent(
                cardType = card.cardType,
                batteryData = batteryData,
                isEditMode = isEditMode,
                modifier = Modifier.fillMaxSize()
            )

            // Edit Mode Controls
            if (isEditMode && isSelected) {
                EditModeOverlay(
                    onRemove = onCardRemove,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun EditModeOverlay(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Remove Button
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(28.dp)
                .clickable { onRemove() },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            shadowElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Card",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Selection overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    MaterialTheme.shapes.large
                )
        )
    }
}

@Composable
private fun AddCardPlaceholder(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
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
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Card",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = "Add Card",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun getCardHeight(cardType: CardType, size: CardSize): androidx.compose.ui.unit.Dp {
    // Simplified height calculation using Material Design spacing
    return when (size) {
        CardSize.Small -> 120.dp
        CardSize.Medium -> 160.dp
        CardSize.Large -> 200.dp
        CardSize.Wide -> 180.dp
    }
}
