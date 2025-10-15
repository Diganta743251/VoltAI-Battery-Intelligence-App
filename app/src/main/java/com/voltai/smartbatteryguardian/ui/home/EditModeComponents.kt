@file:OptIn(ExperimentalMaterial3Api::class)

package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.ui.animation.AnimationSpecs

@Deprecated("Replaced by EditModeBottomBar; this composable is intentionally no-op")
@Composable
fun EditModeControls(
    gridColumns: Int,
    onUpdateGridColumns: (Int) -> Unit,
    onDone: () -> Unit,
    onAddCard: () -> Unit,
    onReset: () -> Unit,
    onResetKeepPremium: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    // No-op: kept for backward compatibility
    return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Drag Handle
            Box(
                modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 32.dp, height = 4.dp)
                    .background(
                        Color(0xFFE0E0E0),
                        RoundedCornerShape(2.dp),
                    ),
            )

            // Title
            Text(
                text = "Edit Dashboard",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            // Grid Columns Control
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Grid Columns",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..4).forEach { col ->
                        val isSelected = col == gridColumns
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = if (isSelected) 2.dp else 0.dp,
                        ) {
                            Text(
                                text = col.toString(),
                                modifier =
                                Modifier
                                    .height(40.dp)
                                    .width(48.dp)
                                    .clickable { onUpdateGridColumns(col) }
                                    .semantics { contentDescription = "Set grid columns to $col" }
                                    .wrapContentHeight(Alignment.CenterVertically),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Add Card Button
                EditControlButton(
                    icon = Icons.Default.Add,
                    text = "Add Card",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAddCard()
                    },
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f),
                )

                // Reset Button
                EditControlButton(
                    icon = Icons.Default.Refresh,
                    text = "Reset",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReset()
                    },
                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
            }

            // Reset while keeping premium cards
            EditControlButton(
                icon = Icons.Default.Refresh,
                text = "Reset (Keep PRO)",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onResetKeepPremium()
                },
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            )

            // Done Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDone()
                },
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Done",
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Done Editing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun EditModeBottomBar(
    gridColumns: Int,
    onUpdateGridColumns: (Int) -> Unit,
    onDone: () -> Unit,
    onAddCard: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val scroll = rememberScrollState()

    Surface(
        modifier =
        modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier =
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f, fill = true))
                AssistChip(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAddCard()
                    },
                    label = { Text("Add") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors =
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
                AssistChip(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReset()
                    },
                    label = { Text("Reset") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors =
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDone()
                },
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Done Editing")
            }
        }
    }
}

@Composable
private fun EditControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = AnimationSpecs.snappy,
    )

    Surface(
        modifier =
        modifier
            .height(48.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardBottomSheet(
    availableCards: List<CardType>,
    onCardSelect: (CardType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        modifier = modifier,
        containerColor = Color(0xFFFAFBFC),
        dragHandle = {
            Surface(
                modifier =
                Modifier
                    .padding(top = 16.dp)
                    .size(width = 32.dp, height = 4.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFE0E0E0),
            ) {}
        },
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Add Card",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose a card to add to your dashboard",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card Categories
            val cardsByCategory = availableCards.groupBy { it.category }

            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                cardsByCategory.forEach { (category, cards) ->
                    item {
                        CardCategorySection(
                            category = category,
                            cards = cards,
                            onCardSelect = { cardType ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCardSelect(cardType)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardCategorySection(
    category: CardCategory,
    cards: List<CardType>,
    onCardSelect: (CardType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Category Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = category.color.copy(alpha = 0.2f),
                modifier = Modifier.size(8.dp),
            ) {}

            Text(
                text = category.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = category.color,
            )
        }

        // Cards Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            cards.forEach { cardType ->
                AddCardItem(
                    cardType = cardType,
                    onClick = { onCardSelect(cardType) },
                )
            }
        }
    }
}

@Composable
private fun AddCardItem(
    cardType: CardType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = AnimationSpecs.snappy,
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 4.dp,
        animationSpec = tween(300),
    )

    Surface(
        modifier =
        modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = elevation,
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = cardType.category.color.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = cardType.icon,
                        contentDescription = cardType.title,
                        tint = cardType.category.color,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // Card Info
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = cardType.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                    )

                    if (cardType.isPremium) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF9C27B0).copy(alpha = 0.1f),
                            modifier = Modifier.padding(0.dp),
                        ) {
                            Text(
                                text = "PRO",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9C27B0),
                            )
                        }
                    }
                }

                Text(
                    text = cardType.description,
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Size badges
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    cardType.allowedSizes.forEach { size ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF0F0F0),
                            modifier = Modifier.padding(0.dp),
                        ) {
                            Text(
                                text = size.displayName,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                color = Color(0xFF666666),
                            )
                        }
                    }
                }
            }

            // Add Button
            Surface(
                shape = CircleShape,
                color = Color(0xFF2196F3),
                modifier = Modifier.size(32.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add ${cardType.title}",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}
