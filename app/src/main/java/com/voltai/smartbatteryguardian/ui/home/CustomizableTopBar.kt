package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CustomizableTopBar(
    isEditMode: Boolean,
    onEditModeToggle: () -> Unit,
    onAddCardClick: () -> Unit,
    onResetClick: () -> Unit,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    // Animated elevation and colors for edit mode
    val elevation by animateDpAsState(
        targetValue = if (isEditMode) 6.dp else 4.dp,
        animationSpec = tween(300),
    )

    val backgroundColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
    )

    Surface(
        modifier =
        modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = backgroundColor,
        shadowElevation = elevation,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Logo and Title Section
                Column {
                    Text(
                        text = "VoltAI Guardian",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp,
                    )

                    AnimatedContent(
                        targetState = isEditMode,
                        transitionSpec = {
                            slideInVertically(
                                animationSpec = tween(200),
                                initialOffsetY = { it / 2 },
                            ) togetherWith
                                slideOutVertically(
                                    animationSpec = tween(200),
                                    targetOffsetY = { -it / 2 },
                                )
                        },
                    ) { editMode ->
                        Text(
                            text = if (editMode) "Customize your dashboard" else "Smart Battery Management",
                            fontSize = 12.sp,
                            color = if (editMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Undo (visible only in edit mode when available)
                    AnimatedVisibility(
                        visible = isEditMode && canUndo,
                        enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                        exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
                    ) {
                        TopBarButton(
                            icon = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onUndo()
                            },
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Redo (visible only in edit mode when available)
                    AnimatedVisibility(
                        visible = isEditMode && canRedo,
                        enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                        exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
                    ) {
                        TopBarButton(
                            icon = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onRedo()
                            },
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Add Card (visible only in edit mode)
                    AnimatedVisibility(
                        visible = isEditMode,
                        enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                        exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
                    ) {
                        TopBarButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Add card",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAddCardClick()
                            },
                            backgroundColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        )
                    }

                    // Reset Layout (visible only in edit mode)
                    AnimatedVisibility(
                        visible = isEditMode,
                        enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                        exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
                    ) {
                        TopBarButton(
                            icon = Icons.Default.Refresh,
                            contentDescription = "Reset layout",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onResetClick()
                            },
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Edit Mode Toggle Button
                    TopBarButton(
                        icon = if (isEditMode) Icons.Default.Done else Icons.Default.Edit,
                        contentDescription = if (isEditMode) "Done editing" else "Edit dashboard",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEditModeToggle()
                        },
                        backgroundColor = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        contentColor = if (isEditMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                        isSelected = isEditMode,
                    )
                }
            }

            // Edit controls are provided via top bar when in edit mode
        }
    }
}

@Composable
private fun TopBarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    contentDescription: String? = null,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue =
        when {
            isPressed -> 0.9f
            isSelected -> 1.1f
            else -> 1f
        },
        animationSpec =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 2.dp,
        animationSpec = tween(300),
    )

    val resolvedBackground = backgroundColor ?: MaterialTheme.colorScheme.surfaceVariant
    val resolvedContent = contentColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier =
        modifier
            .size(48.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                isPressed = true
                onClick()
            },
        shape = CircleShape,
        color = resolvedBackground,
        shadowElevation = elevation,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = resolvedContent,
                modifier = Modifier.size(20.dp),
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

@Composable
private fun EditModeActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
    )

    val backgroundColor =
        if (isDestructive) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }

    val contentColor =
        if (isDestructive) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

    Surface(
        modifier =
        modifier
            .height(48.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
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
