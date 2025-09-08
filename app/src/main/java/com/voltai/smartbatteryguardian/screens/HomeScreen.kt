package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.ui.home.*
import com.voltai.smartbatteryguardian.ui.animation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CustomizableHomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCard: (String) -> Unit = {},
    onNavigateToPremiumOnboarding: () -> Unit = {}
) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle side effects
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeScreenEffect.PerformHapticFeedback -> {
                    when (effect.type) {
                        HapticType.Click -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        HapticType.LongPress -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        HapticType.Success -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        HapticType.Error -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        HapticType.Warning -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                is HomeScreenEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                else -> { /* Handle other effects */ }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE3F2FD),
                        Color(0xFFF0F4FF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Modern Customizable Top Bar
            CustomizableTopBar(
                isEditMode = uiState.isEditMode,
                onEditModeToggle = {
                    if (uiState.isEditMode) {
                        viewModel.onEvent(HomeScreenEvent.ExitEditMode)
                    } else {
                        viewModel.onEvent(HomeScreenEvent.EnterEditMode)
                    }
                },
                onAddCardClick = {
                    viewModel.onEvent(HomeScreenEvent.ShowAddCardSheet)
                },
                onResetClick = {
                    viewModel.onEvent(HomeScreenEvent.ResetToDefaults())
                },
                onSettingsClick = onNavigateToSettings
            )
            
            // Customizable Cards Grid with Staggered Layout
            CustomizableCardsGrid(
                cards = uiState.config.cards,
                batteryData = batteryData,
                isEditMode = uiState.isEditMode,
                isDragActive = uiState.isDragActive,
                selectedCard = uiState.selectedCard,
                onCardClick = { card ->
                    if (!uiState.isEditMode) {
                        // Check if it's a premium card and user is not premium
                        val isPremiumCard = card.cardType.isPremium
                        val userHasPremium = false // TODO: Connect to actual premium state
                        
                        if (isPremiumCard && !userHasPremium) {
                            // Show premium onboarding for premium cards
                            onNavigateToPremiumOnboarding()
                        } else {
                            // Navigate to card detail based on card type
                            when (card.cardType) {
                                CardType.HISTORY -> onNavigateToCard("history")
                                CardType.OVERLAYS -> onNavigateToCard("overlays")
                                CardType.TEMPERATURE -> onNavigateToCard("temperature_detail")
                                CardType.CURRENT_VOLTAGE -> onNavigateToCard("current_detail")
                                CardType.HEALTH -> onNavigateToCard("health_detail")
                                CardType.CHARGING_ANALYTICS -> onNavigateToCard("charging_analytics")
                                CardType.CHEMISTRY_LAB -> onNavigateToCard("chemistry_lab")
                                CardType.SYSTEM_OPTIMIZATION -> onNavigateToCard("system_optimization")
                                CardType.PERFORMANCE_BENCHMARKS -> onNavigateToCard("performance_benchmarks")
                                CardType.DATA_EXPORT_IMPORT -> onNavigateToCard("data_export_import")
                                // New Premium Analytics Cards
                                CardType.BATTERY_HEALTH_ANALYTICS -> onNavigateToCard("battery_health_analytics")
                                CardType.SMART_CHARGING -> onNavigateToCard("smart_charging")
                                CardType.BATTERY_DIAGNOSTICS -> onNavigateToCard("battery_diagnostics")
                                CardType.REAL_TIME_MONITORING -> onNavigateToCard("real_time_monitoring")
                                CardType.BATTERY_OPTIMIZATION -> onNavigateToCard("battery_optimization")
                                else -> { /* No navigation for this card type */ }
                            }
                        }
                    } else {
                        viewModel.onEvent(HomeScreenEvent.SelectCard(card))
                    }
                },
                onCardLongPress = { card ->
                    if (!uiState.isEditMode) {
                        viewModel.onEvent(HomeScreenEvent.EnterEditMode)
                        viewModel.onEvent(HomeScreenEvent.SelectCard(card))
                    }
                },
                onCardRemove = { cardId ->
                    viewModel.onEvent(HomeScreenEvent.RemoveCard(cardId))
                },
                onCardMove = { cardId, dragOffset ->
                    viewModel.moveCardWithDrag(cardId, dragOffset)
                },
                onCardResize = { cardId, newSize ->
                    viewModel.onEvent(HomeScreenEvent.ResizeCard(cardId, newSize))
                },
                onDragStart = { cardId ->
                    viewModel.onDragStart(cardId)
                },
                onDragEnd = {
                    viewModel.onDragEnd()
                },
                onAddCardClick = {
                    viewModel.onEvent(HomeScreenEvent.ShowAddCardSheet)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )
        }
        
        // Floating Action Button for Edit Mode
        AnimatedVisibility(
            visible = !uiState.isEditMode,
            enter = AnimationSpecs.scaleInAnimation,
            exit = AnimationSpecs.scaleOutAnimation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(HomeScreenEvent.EnterEditMode)
                },
                containerColor = Color(0xFF2196F3),
                modifier = Modifier
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Customize Layout",
                    tint = Color.White
                )
            }
        }
        
        // Edit Mode Controls
        AnimatedVisibility(
            visible = uiState.isEditMode,
            enter = AnimationSpecs.slideInFromBottom,
            exit = AnimationSpecs.slideOutToBottom,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            EditModeControls(
                onDone = {
                    viewModel.onEvent(HomeScreenEvent.ExitEditMode)
                },
                onAddCard = {
                    viewModel.onEvent(HomeScreenEvent.ShowAddCardSheet)
                },
                onReset = {
                    viewModel.onEvent(HomeScreenEvent.ResetToDefaults())
                }
            )
        }
    }
    
    // Add Card Bottom Sheet
    if (uiState.showAddCardSheet) {
        AddCardBottomSheet(
            availableCards = CardType.values().toList(),
            onCardSelect = { cardType ->
                val position = findNextAvailablePosition(
                    uiState.config.cards,
                    cardType.defaultSize,
                    uiState.config.gridColumns
                ) ?: Pair(0, 0)
                viewModel.onEvent(HomeScreenEvent.AddCard(cardType, position))
            },
            onDismiss = {
                viewModel.onEvent(HomeScreenEvent.HideAddCardSheet)
            }
        )
    }
    
    // Snackbar Host for error messages
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    ) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = Color(0xFFFF5252),
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }
    
    // Loading State
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
fun ModernTopBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VoltAI Guardian",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Smart Battery Management",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
            }
            
            IconButton(
                onClick = { /* Settings */ },
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Color(0xFF2196F3),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun ModernBatteryCard(
    batteryLevel: Int,
    isCharging: Boolean,
    chargingStatus: String,
    remainingTime: String,
    current: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Battery Level",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "$batteryLevel%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )
                }
                
                Icon(
                    imageVector = if (isCharging) Icons.Filled.Phone else Icons.Filled.Star,
                    contentDescription = "Battery",
                    modifier = Modifier.size(32.dp),
                    tint = if (batteryLevel > 50) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Status",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = chargingStatus,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Time Remaining",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = remainingTime,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ModernInfoCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666)
            )
            
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF999999),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Removed complex undefined components that cause build errors

// Removed all undefined components to ensure clean build
