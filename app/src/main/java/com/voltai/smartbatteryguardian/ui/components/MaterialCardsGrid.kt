package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.ui.home.*
import com.voltai.smartbatteryguardian.ui.theme.VoltAIMotion

/**
 * Material Design 3 compliant cards grid implementation.
 *
 * Removes excessive animations while maintaining essential interaction feedback.
 * Uses standard Material components and follows accessibility guidelines.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    columns: Int = 2,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val cols = columns.coerceAtLeast(1)

        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding =
            PaddingValues(
                bottom = if (isEditMode) 96.dp else 12.dp, // room for compact edit bar + bottom nav
            ),
        ) {
            val visibleCards =
                cards
                    .filter { it.isVisible }
                    .sortedWith(compareBy<HomeCard> { it.row }.thenBy { it.column })
            items(
                count = visibleCards.size,
                key = { visibleCards[it].id },
                span = { index -> GridItemSpan(visibleCards[index].size.columns.coerceAtMost(cols)) },
            ) { index ->
                val card = visibleCards[index]
                MaterialCard(
                    card = card,
                    batteryData = batteryData,
                    isEditMode = isEditMode,
                    isSelected = selectedCard?.id == card.id,
                    reduceMotion = reduceMotion,
                    onCardClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCardClick(card)
                    },
                    onCardLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCardLongPress(card)
                    },
                    onCardRemove = { onCardRemove(card.id) },
                    modifier = Modifier.animateItemPlacement(),
                )
            }

            // Add Card Placeholder in Edit Mode
            if (isEditMode && cards.filter { it.isVisible }.size < 12) {
                item {
                    AddCardPlaceholder(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAddCardClick()
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MaterialCard(
    card: HomeCard,
    batteryData: BatteryData,
    isEditMode: Boolean,
    isSelected: Boolean,
    reduceMotion: Boolean,
    onCardClick: () -> Unit,
    onCardLongPress: () -> Unit,
    onCardRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Material Design 3 compliant press feedback
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = VoltAIMotion.buttonPress,
        label = "card_scale",
    )

    Card(
        onClick = onCardClick,
        modifier =
        modifier
            .fillMaxWidth()
            .height(getCardHeight(card.cardType, card.size))
            .scale(scale)
            .zIndex(if (isSelected) 1f else 0f)
            .semantics(mergeDescendants = true) {
                contentDescription = "${card.cardType.title} card"
                role = Role.Button
            },
        shape = RoundedCornerShape(20.dp),
        colors =
        CardDefaults.cardColors(
            containerColor =
            if (isSelected && isEditMode) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation =
        CardDefaults.cardElevation(
            defaultElevation =
            if (isEditMode && isSelected) {
                2.dp
            } else if (isEditMode) {
                1.dp
            } else {
                3.dp
            },
        ),
        border =
        if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onCardClick,
                    onLongClick = onCardLongPress,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ),
        ) {
            // Card Content
            MaterialCardContent(
                cardType = card.cardType,
                batteryData = batteryData,
                size = card.size,
                isEditMode = isEditMode,
                modifier = Modifier.fillMaxSize(),
                reduceMotion = reduceMotion,
            )

            // Edit Mode Controls - Always show cross button in edit mode
            if (isEditMode) {
                EditModeOverlay(
                    isSelected = isSelected,
                    onRemove = onCardRemove,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun EditModeOverlay(
    isSelected: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // Remove Button - Always visible in edit mode, more prominent when selected
        Surface(
            modifier =
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp) // Adjusted positioning for better visibility
                .size(48.dp) // 48dp minimum touch target
                .clickable { onRemove() }
                .semantics {
                    contentDescription = "Remove card"
                    role = Role.Button
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            shadowElevation = if (isSelected) 6.dp else 3.dp,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Card",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Selection overlay - only shown when selected
        if (isSelected) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.shapes.large,
                    ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCardPlaceholder(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier =
        modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border =
        BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier =
                    Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Add card"
                            role = Role.Button
                        },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Card",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Text(
                    text = "Add Card",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun getCardHeight(
    cardType: CardType,
    size: CardSize,
): androidx.compose.ui.unit.Dp {
    val rowBase = 90.dp // Base row height: 2 rows = 180dp
    val spacing = 8.dp // Grid spacing
    return when (size) {
        CardSize.Small -> rowBase * 2 + spacing * 1 // 2 rows = 188dp
        CardSize.Medium -> rowBase * 2 + spacing * 1 // 2 rows = 188dp (spans 2 cols)
        CardSize.MediumTall -> rowBase * 3 + spacing * 2 // 3 rows = 286dp
        CardSize.Large -> rowBase * 4 + spacing * 3 // 4 rows = 384dp
        CardSize.FullWidth -> rowBase * 4 + spacing * 3 // 4 rows = 384dp
        CardSize.FullWidthShort -> rowBase * 3 + spacing * 2 // 3 rows = 286dp
        CardSize.Wide -> rowBase * 2 + spacing * 1 // 2 rows = 188dp
        CardSize.XLarge -> rowBase * 6 + spacing * 5 // 6 rows = 580dp
        CardSize.XXLarge -> rowBase * 8 + spacing * 7 // 8 rows = 776dp
    }
}
