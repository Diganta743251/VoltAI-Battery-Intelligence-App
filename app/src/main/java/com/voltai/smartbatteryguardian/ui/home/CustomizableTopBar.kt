package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.ui.animation.AnimationSpecs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CustomizableTopBar(
    isEditMode: Boolean,
    onEditModeToggle: () -> Unit,
    onAddCardClick: () -> Unit,
    onResetClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // Animated elevation and colors for edit mode
    val elevation by animateDpAsState(
        targetValue = if (isEditMode) 8.dp else 4.dp,
        animationSpec = tween(300)
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isEditMode) Color(0xFFFFF3E0) else Color.White,
        animationSpec = tween(300)
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = backgroundColor,
        shadowElevation = elevation,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo and Title Section
                Column {
                    Text(
                        text = "VoltAI Guardian",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A),
                        letterSpacing = (-0.5).sp
                    )
                    
                    AnimatedContent(
                        targetState = isEditMode,
                        transitionSpec = {
                            slideInVertically(
                                animationSpec = tween(200),
                                initialOffsetY = { it / 2 }
                            ) with slideOutVertically(
                                animationSpec = tween(200),
                                targetOffsetY = { -it / 2 }
                            )
                        }
                    ) { editMode ->
                        Text(
                            text = if (editMode) "Customize your dashboard" else "Smart Battery Management",
                            fontSize = 12.sp,
                            color = if (editMode) Color(0xFFFF6F00) else Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings Button (hidden in edit mode)
                    AnimatedVisibility(
                        visible = !isEditMode,
                        enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                        exit = scaleOut(animationSpec = tween(200)) + fadeOut()
                    ) {
                        TopBarButton(
                            icon = Icons.Default.Settings,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSettingsClick()
                            },
                            backgroundColor = Color(0xFFF0F0F0),
                            contentColor = Color(0xFF666666)
                        )
                    }
                    
                    // Edit Mode Toggle Button
                    TopBarButton(
                        icon = if (isEditMode) Icons.Default.Done else Icons.Default.Edit,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEditModeToggle()
                        },
                        backgroundColor = if (isEditMode) Color(0xFF4CAF50) else Color(0xFF2196F3),
                        contentColor = Color.White,
                        isSelected = isEditMode
                    )
                }
            }
            
            // Edit Mode Controls
            AnimatedVisibility(
                visible = isEditMode,
                enter = expandVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(200, delayMillis = 100)),
                exit = shrinkVertically(
                    animationSpec = tween(200, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Add Card Button
                    EditModeActionButton(
                        text = "Add Card",
                        icon = Icons.Default.Add,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAddCardClick()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Reset Layout Button
                    EditModeActionButton(
                        text = "Reset",
                        icon = Icons.Default.Refresh,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onResetClick()
                        },
                        modifier = Modifier.weight(1f),
                        isDestructive = true
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBarButton(
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0xFFF0F0F0),
    contentColor: Color = Color(0xFF666666),
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.9f
            isSelected -> 1.1f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 2.dp,
        animationSpec = tween(300)
    )
    
    Surface(
        modifier = modifier
            .size(44.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = elevation
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
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
    isDestructive: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = AnimationSpecs.snappy
    )
    
    val backgroundColor = if (isDestructive) {
        Color(0xFFFFF3E0)
    } else {
        Color(0xFFE3F2FD)
    }
    
    val contentColor = if (isDestructive) {
        Color(0xFFFF6F00)
    } else {
        Color(0xFF2196F3)
    }
    
    Surface(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
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
