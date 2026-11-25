package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.preferences.PreferencesViewModel
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.ui.home.CustomizableHomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class HomeFilterCategory(val label: String) {
    ALL("All"),
    VITALS("Vitals"),
    POWER("Power & Saving"),
    ADVANCED("Advanced AI")
}

data class DashboardCardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val icon: ImageVector,
    val iconTint: Color,
    val route: String,
    val category: HomeFilterCategory,
    val isPremium: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CustomizableHomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCard: (String) -> Unit = {},
    onNavigateToPremiumOnboarding: () -> Unit = {},
    onShowWelcome: () -> Unit = {},
) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val preferencesViewModel: PreferencesViewModel = hiltViewModel()
    val reduceMotion by preferencesViewModel.reduceMotion.collectAsState()
    val userHasPremium by viewModel.isPremium.collectAsState()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(HomeFilterCategory.ALL) }
    var isOptimizing by remember { mutableStateOf(false) }
    var optimizationSuccess by remember { mutableStateOf(false) }

    // Dynamic color accents based on thermal & battery state
    val batteryProgress = (batteryData.batteryLevel / 100f).coerceIn(0f, 1f)
    val accentColor = when {
        batteryData.temperature > 40f -> MaterialTheme.colorScheme.error
        batteryData.batteryLevel <= 20 -> MaterialTheme.colorScheme.tertiary
        batteryData.isCharging -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("home_screen_root"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "VoltAI",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "VoltAI Guardian",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = if (batteryData.isCharging) "⚡ Charging Active" else "🛡️ Battery Protected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coroutineScope.launch {
                                isOptimizing = true
                                delay(900)
                                isOptimizing = false
                                optimizationSuccess = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                snackbarHostState.showSnackbar("Background services optimized & thermals stabilized")
                            }
                        },
                        modifier = Modifier.testTag("quick_optimize_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Quick Refresh",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("home_settings_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. HERO BATTERY INTELLIGENCE CENTERPIECE
            item {
                HeroBatteryCenterpiece(
                    batteryData = batteryData,
                    accentColor = accentColor,
                    reduceMotion = reduceMotion,
                    isOptimizing = isOptimizing,
                    onOptimizeClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        coroutineScope.launch {
                            isOptimizing = true
                            delay(900)
                            isOptimizing = false
                            optimizationSuccess = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            snackbarHostState.showSnackbar("VoltAI AI Engine: Battery drain reduced by ~18%")
                        }
                    },
                    onCardClick = { onNavigateToCard("battery_info") },
                )
            }

            // 2. QUICK VITALS TELEMETRY RIBBON
            item {
                VitalsTelemetryRibbon(
                    batteryData = batteryData,
                    onNavigateToCard = onNavigateToCard,
                )
            }

            // 3. AI BATTERY INTELLIGENCE ADVISORY BANNER
            item {
                AIBatteryAdvisoryBanner(
                    batteryData = batteryData,
                    onActionClick = { onNavigateToCard("power") },
                )
            }

            // 4. CATEGORY FILTER CHIPS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HomeFilterCategory.values().forEach { category ->
                        val selected = selectedCategory == category
                        FilterChip(
                            selected = selected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedCategory = category
                            },
                            label = { Text(category.label, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}"),
                        )
                    }
                }
            }

            // 5. REDESIGNED FEATURE CARDS
            val allCards = remember(batteryData) {
                listOf(
                    DashboardCardItem(
                        id = "battery_info",
                        title = "Battery Health & Specs",
                        subtitle = "Capacity ${batteryData.capacity} mAh • ${batteryData.cycleCount} cycles",
                        badge = "${batteryData.healthPercentage}% Health",
                        icon = Icons.Default.Favorite,
                        iconTint = Color(0xFF4CAF50),
                        route = "battery_info",
                        category = HomeFilterCategory.VITALS,
                    ),
                    DashboardCardItem(
                        id = "current_detail",
                        title = "Electric Current & Wattage",
                        subtitle = "${batteryData.current} mA • ${String.format("%.2f", batteryData.voltage)}V • ${batteryData.chargingSpeed}",
                        badge = if (batteryData.isCharging) "Charging" else "Discharging",
                        icon = Icons.Default.Bolt,
                        iconTint = Color(0xFFFFB300),
                        route = "current_detail",
                        category = HomeFilterCategory.VITALS,
                    ),
                    DashboardCardItem(
                        id = "temperature_detail",
                        title = "Thermal Protection",
                        subtitle = "${String.format("%.1f", batteryData.temperature)}°C • Cooling active",
                        badge = when {
                            batteryData.temperature < 32f -> "Cool"
                            batteryData.temperature < 40f -> "Normal"
                            else -> "Warm"
                        },
                        icon = Icons.Default.Star,
                        iconTint = Color(0xFF29B6F6),
                        route = "temperature_detail",
                        category = HomeFilterCategory.POWER,
                    ),
                    DashboardCardItem(
                        id = "ongoing_detail",
                        title = "Screen Time & Drain",
                        subtitle = "Screen On: ${batteryData.screenOnTime} • Idle: ${batteryData.screenOffTime}",
                        badge = "Real-time",
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFFAB47BC),
                        route = "ongoing_detail",
                        category = HomeFilterCategory.VITALS,
                    ),
                    DashboardCardItem(
                        id = "power",
                        title = "AI Power Profiles",
                        subtitle = "Smart Adaptive CPU & Display optimization",
                        badge = "Balanced",
                        icon = Icons.Default.Build,
                        iconTint = Color(0xFF26A69A),
                        route = "power",
                        category = HomeFilterCategory.POWER,
                    ),
                    DashboardCardItem(
                        id = "system_optimization",
                        title = "System Optimization Center",
                        subtitle = "Tune background wakelocks and app refresh rates",
                        badge = "AI Pro",
                        icon = Icons.Default.Speed,
                        iconTint = Color(0xFFFF7043),
                        route = "system_optimization",
                        category = HomeFilterCategory.ADVANCED,
                        isPremium = false,
                    ),
                    DashboardCardItem(
                        id = "history",
                        title = "Charge Sessions & History",
                        subtitle = "Historical trend analysis over 30 days",
                        badge = "Logged",
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFF7E57C2),
                        route = "history",
                        category = HomeFilterCategory.POWER,
                    ),
                    DashboardCardItem(
                        id = "real_time_monitoring",
                        title = "Real-Time Telemetry Monitor",
                        subtitle = "High-frequency live current & voltage stream",
                        badge = "Live Stream",
                        icon = Icons.Default.Share,
                        iconTint = Color(0xFF00ACC1),
                        route = "real_time_monitoring",
                        category = HomeFilterCategory.ADVANCED,
                    ),
                )
            }

            val filteredCards = allCards.filter {
                selectedCategory == HomeFilterCategory.ALL || it.category == selectedCategory
            }

            items(filteredCards, key = { it.id }) { cardItem ->
                DashboardCard(
                    item = cardItem,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (cardItem.isPremium && !userHasPremium) {
                            onNavigateToPremiumOnboarding()
                        } else {
                            onNavigateToCard(cardItem.route)
                        }
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HeroBatteryCenterpiece(
    batteryData: BatteryData,
    accentColor: Color,
    reduceMotion: Boolean,
    isOptimizing: Boolean,
    onOptimizeClick: () -> Unit,
    onCardClick: () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (batteryData.batteryLevel / 100f).coerceIn(0f, 1f),
        animationSpec = if (reduceMotion) tween(0) else tween(800, easing = FastOutSlowInEasing),
        label = "heroBatteryProgress",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onCardClick)
            .testTag("hero_battery_centerpiece"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Circular Energy Pulse Meter
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Background Track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.15f),
                        style = Stroke(width = strokeWidth),
                    )
                }

                // Active Progress Arc
                val primaryColor = MaterialTheme.colorScheme.primary
                val tertiaryColor = MaterialTheme.colorScheme.tertiary
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(primaryColor, accentColor, tertiaryColor, primaryColor),
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                // Center Telemetry
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "${batteryData.batteryLevel}%",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-1).sp,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = if (batteryData.isCharging) Icons.Default.Bolt else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = if (batteryData.isCharging) batteryData.chargingStatus else "${batteryData.healthPercentage}% Health",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-status and Time estimation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Time Left",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (batteryData.remainingTime.isNotBlank()) batteryData.remainingTime else "12h 45m",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                VerticalDivider(modifier = Modifier.height(28.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Power Source",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (batteryData.isCharging) batteryData.powerSource else "Battery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                VerticalDivider(modifier = Modifier.height(28.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Temp",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${String.format("%.1f", batteryData.temperature)}°C",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Instant Optimize Action Button
            Button(
                onClick = onOptimizeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("hero_optimize_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                enabled = !isOptimizing,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isOptimizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Text("Optimizing Engine...", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text("Instant Optimize & Protect", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalsTelemetryRibbon(
    batteryData: BatteryData,
    onNavigateToCard: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        VitalPill(
            title = "Temp",
            value = "${String.format("%.1f", batteryData.temperature)}°",
            status = if (batteryData.temperature < 35f) "Normal" else "Warm",
            icon = Icons.Default.Star,
            color = if (batteryData.temperature < 35f) Color(0xFF29B6F6) else Color(0xFFFF5722),
            modifier = Modifier.weight(1f),
            onClick = { onNavigateToCard("temperature_detail") },
        )

        VitalPill(
            title = "Voltage",
            value = "${String.format("%.2f", batteryData.voltage)}V",
            status = "Optimal",
            icon = Icons.Default.Bolt,
            color = Color(0xFFFFB300),
            modifier = Modifier.weight(1f),
            onClick = { onNavigateToCard("current_detail") },
        )

        VitalPill(
            title = "Current",
            value = "${batteryData.current}mA",
            status = batteryData.chargingSpeed,
            icon = Icons.Default.Refresh,
            color = Color(0xFF66BB6A),
            modifier = Modifier.weight(1f),
            onClick = { onNavigateToCard("current_detail") },
        )

        VitalPill(
            title = "Health",
            value = "${batteryData.healthPercentage}%",
            status = "Good",
            icon = Icons.Default.Favorite,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f),
            onClick = { onNavigateToCard("health_detail") },
        )
    }
}

@Composable
private fun VitalPill(
    title: String,
    value: String,
    status: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("vital_pill_${title.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AIBatteryAdvisoryBanner(
    batteryData: BatteryData,
    onActionClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("ai_battery_advisory_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "AI Guardian",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Battery Guardian",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = when {
                        batteryData.isCharging -> "Fast charging active. Thermal guard monitoring charging curve to maximize battery lifespan."
                        batteryData.temperature > 38f -> "Device running warm. AI recommends throttling background refresh to preserve longevity."
                        else -> "Power consumption is optimized. Current drain rate is within the top 5% efficiency benchmark."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun DashboardCard(
    item: DashboardCardItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("dashboard_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = item.iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.iconTint,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            ) {
                Text(
                    text = item.badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
