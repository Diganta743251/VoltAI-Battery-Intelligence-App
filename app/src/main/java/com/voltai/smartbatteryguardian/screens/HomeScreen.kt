package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.debug.BatteryDataDebugScreen
import com.voltai.smartbatteryguardian.preferences.PreferencesViewModel
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.ui.home.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CustomizableHomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCard: (String) -> Unit = {},
    onNavigateToPremiumOnboarding: () -> Unit = {},
    onShowWelcome: () -> Unit = {},
) {
    var showDebugScreen by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val uiState by viewModel.uiState.collectAsState()
    // Reduce Motion preference
    val preferencesViewModel: PreferencesViewModel = hiltViewModel()
    val reduceMotion by preferencesViewModel.reduceMotion.collectAsState()
    val userHasPremium by viewModel.isPremium.collectAsState()
    // Use configured column count (default 4) to emulate Android launcher grid
    val gridCols = uiState.config.gridColumns
    // Note: FirstTimeUserManager should be injected via Hilt but we'll use a simple approach for now
    // val firstTimeUserManager: FirstTimeUserManager = hiltViewModel()
    // val firstTimeState by firstTimeUserManager.firstTimeUserState.collectAsState()
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
                        duration = SnackbarDuration.Short,
                    )
                }
                else -> { /* Handle other effects */ }
            }
        }
    }

    // Material Design 3 compliant scaffold
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            com.voltai.smartbatteryguardian.ui.home.CustomizableTopBar(
                isEditMode = uiState.isEditMode,
                onEditModeToggle = {
                    if (uiState.isEditMode) {
                        viewModel.onEvent(HomeScreenEvent.ExitEditMode)
                    } else {
                        viewModel.onEvent(HomeScreenEvent.EnterEditMode)
                    }
                },
                onAddCardClick = { showAddDialog = true },
                onResetClick = {
                    viewModel.onEvent(
                        HomeScreenEvent.ResetToDefaults(keepCustomizations = false, keepPremiumCards = false),
                    )
                },
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onUndo = { viewModel.onEvent(HomeScreenEvent.Undo) },
                onRedo = { viewModel.onEvent(HomeScreenEvent.Redo) },
                onSettingsClick = onNavigateToSettings,
            )
        },
        floatingActionButton = {},
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = MaterialTheme.shapes.medium,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Surface(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Edit mode banner
                if (uiState.isEditMode) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Mode - Tap cards to select, drag to reposition",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                if (!uiState.isEditMode) {
                    // Viewing mode: use the same absolute-position grid to mirror coordinates
                    com.voltai.smartbatteryguardian.ui.home.CustomizableCardsGrid(
                        cards = uiState.config.cards,
                        batteryData = batteryData,
                        isEditMode = false,
                        isDragActive = false,
                        selectedCard = uiState.selectedCard,
                        columns = gridCols,
                        reduceMotion = reduceMotion,
                        onCardClick = { card ->
                            // Only navigate when NOT in edit mode
                            val isPremiumCard = card.cardType.isPremium
                            if (isPremiumCard && !userHasPremium) {
                                onNavigateToPremiumOnboarding()
                            } else {
                                when (card.cardType) {
                                    CardType.BATTERY_LEVEL -> onNavigateToCard("battery_info")
                                    CardType.CHARGING_STATUS -> onNavigateToCard("current_detail")
                                    CardType.CURRENT_VOLTAGE -> onNavigateToCard("current_detail")
                                    CardType.TEMPERATURE -> onNavigateToCard("temperature_detail")
                                    CardType.HEALTH -> onNavigateToCard("health_detail")
                                    CardType.SCREEN_TIME -> onNavigateToCard("ongoing_detail")
                                    CardType.APP_USAGE -> onNavigateToCard("stats")
                                    CardType.HISTORY -> onNavigateToCard("history")
                                    CardType.OVERLAYS -> onNavigateToCard("overlays")
                                    CardType.POWER_MODES -> onNavigateToCard("power")
                                    CardType.QUICK_ACTIONS -> onNavigateToCard("settings")
                                    // Premium and advanced cards
                                    CardType.CHARGING_ANALYTICS -> onNavigateToCard("charging_analytics")
                                    CardType.CHEMISTRY_LAB -> onNavigateToCard("chemistry_lab")
                                    CardType.SYSTEM_OPTIMIZATION -> onNavigateToCard("system_optimization")
                                    CardType.PERFORMANCE_BENCHMARKS -> onNavigateToCard("performance_benchmarks")
                                    CardType.DATA_EXPORT_IMPORT -> onNavigateToCard("data_export_import")
                                    CardType.BATTERY_HEALTH_ANALYTICS -> onNavigateToCard("battery_health_analytics")
                                    CardType.SMART_CHARGING -> onNavigateToCard("smart_charging")
                                    CardType.BATTERY_DIAGNOSTICS -> onNavigateToCard("battery_diagnostics")
                                    CardType.REAL_TIME_MONITORING -> onNavigateToCard("real_time_monitoring")
                                    CardType.BATTERY_OPTIMIZATION -> onNavigateToCard("battery_optimization")
                                    else -> { /* No navigation for this card type */ }
                                }
                            }
                        },
                        onCardLongPress = { card ->
                            // Enter edit mode after long-press and select the pressed card
                            viewModel.onEvent(HomeScreenEvent.EnterEditMode)
                            viewModel.onEvent(HomeScreenEvent.SelectCard(card))
                        },
                        onCardRemove = { /* no-op in view mode */ },
                        onToggleVisibility = { /* no-op in view mode */ },
                        onCardMoveTo = { _, _ -> },
                        onCardResize = { _, _ -> },
                        onDragStart = { },
                        onDragEnd = { },
                        onAddCardClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    // Edit mode: full customization with drag & resize
                    Column(modifier = Modifier.fillMaxSize()) {
                        com.voltai.smartbatteryguardian.ui.home.CustomizableCardsGrid(
                            cards = uiState.config.cards,
                            batteryData = batteryData,
                            isEditMode = true,
                            isDragActive = uiState.isDragActive,
                            selectedCard = uiState.selectedCard,
                            columns = gridCols,
                            reduceMotion = reduceMotion,
                            onCardClick = { card ->
                                // In edit mode, only select cards - NO navigation
                                viewModel.onEvent(HomeScreenEvent.SelectCard(card))
                            },
                        onCardLongPress = { /* already in edit mode */ },
                        onCardRemove = { cardId -> viewModel.onEvent(HomeScreenEvent.RemoveCard(cardId)) },
                        onToggleVisibility = { cardId -> viewModel.onEvent(HomeScreenEvent.ToggleVisibility(cardId)) },
                        onCardMoveTo = { cardId, newPos ->
                            viewModel.onEvent(
                                HomeScreenEvent.MoveCard(cardId, newPos),
                            )
                        },
                        onCardResize = { cardId, newSize ->
                            viewModel.onEvent(
                                HomeScreenEvent.ResizeCard(cardId, newSize),
                            )
                        },
                            onDragStart = { cardId -> viewModel.onDragStart(cardId) },
                            onDragEnd = { viewModel.onDragEnd() },
                            onAddCardClick = { showAddDialog = true },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }

    // Full-screen Add overlay (lists widgets not yet added)
    if (showAddDialog) {
        val addedCardTypes = uiState.config.cards.map { it.cardType }.toSet()
        val availableCardsToAdd = CardType.values().filter { it !in addedCardTypes }

        AddCardsOverlay(
            availableCards = availableCardsToAdd,
            onCardSelect = { cardType ->
                val position =
                    findNextAvailablePosition(
                        uiState.config.cards,
                        cardType.defaultSize,
                        gridCols,
                    ) ?: Pair(0, 0)
                viewModel.onEvent(HomeScreenEvent.AddCard(cardType, position))
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }

    // Loading State
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    // Debug Screen Overlay
    if (showDebugScreen) {
        BatteryDataDebugScreen(
            onBackClick = { showDebugScreen = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCardsOverlay(
    availableCards: List<CardType>,
    onCardSelect: (CardType) -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Add widgets") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
            )

            if (availableCards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "All available cards have been added",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(availableCards) { type ->
                        ListItem(
                            headlineContent = { Text(type.title) },
                            supportingContent = { Text(type.description) },
                            leadingContent = { Icon(type.icon, contentDescription = null) },
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onCardSelect(type) },
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun ModernTopBar() {
    Surface(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "VoltAI Guardian",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = "Smart Battery Management",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }

            IconButton(
                onClick = { /* Settings */ },
                modifier =
                Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp),
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
    current: String,
) {
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = "Battery Level",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$batteryLevel%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Icon(
                    imageVector = if (isCharging) Icons.Filled.Phone else Icons.Filled.Star,
                    contentDescription = "Battery",
                    modifier = Modifier.size(32.dp),
                    tint = if (batteryLevel > 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Status",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = chargingStatus,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Time Remaining",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = remainingTime,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
        modifier
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// Removed complex undefined components that cause build errors

// Removed all undefined components to ensure clean build
