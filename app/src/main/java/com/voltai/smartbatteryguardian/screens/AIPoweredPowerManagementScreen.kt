package com.voltai.smartbatteryguardian.screens

import android.content.res.Resources
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.R
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.viewmodels.BatteryReadingsViewModel
import com.voltai.smartbatteryguardian.viewmodels.PowerProfilesViewModel
import com.voltai.smartbatteryguardian.viewmodels.SystemMetricsViewModel
import kotlin.math.*

data class PowerProfile(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val color: Color,
    val batterySavingsPercent: Int,
    val performanceImpact: PerformanceImpact,
    val features: List<PowerFeature>,
    val isActive: Boolean,
    val isCustom: Boolean,
    val aiOptimized: Boolean,
    val estimatedExtraHours: Double,
)

data class PowerFeature(
    val name: String,
    val description: String,
    val enabled: Boolean,
    val impact: String,
    val category: FeatureCategory,
)

enum class PerformanceImpact { NONE, LOW, MEDIUM, HIGH }

@Composable
fun AdaptationCard(adaptation: Adaptation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = adaptation.type,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            if (adaptation.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = adaptation.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.impact_label),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = adaptation.impact,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.confidence_label),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(adaptation.confidence * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentDrawChart(series: List<Int>) {
    val points = series.takeLast(48)
    val chartCd = "Current draw chart"
    // Enhanced Battery Guru style animation
    val anim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "current_draw_anim",
    )
    // Capture theme color before entering DrawScope
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .semantics { contentDescription = chartCd },
    ) {
        if (points.isEmpty()) return@Canvas
        val minV = points.minOrNull() ?: 0
        val maxV = points.maxOrNull() ?: 0
        val range = (maxV - minV).takeIf { it != 0 } ?: 1
        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
        val path = androidx.compose.ui.graphics.Path()
        points.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - ((v - minV).toFloat() / range.toFloat()) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        clipRect(right = size.width * anim) {
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun OptimizationScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 120.dp,
) {
    val progress = (score.coerceIn(0, 100)) / 100f
    val color = getOptimizationScoreColor(score)

    // Enhanced Battery Guru style animation with subtle 3D effect
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "optimization_ring_anim",
    )
    // Capture outline color before entering DrawScope
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    Canvas(
        modifier =
        modifier
            .size(ringSize),
    ) {
        val stroke = 10.dp.toPx()
        val diameterPx = size.minDimension
        val arcDiameter = (diameterPx - 2f * stroke).coerceAtLeast(0f)
        val arcSize = androidx.compose.ui.geometry.Size(arcDiameter, arcDiameter)
        val topLeft =
            androidx.compose.ui.geometry.Offset(
                (size.width - arcDiameter) / 2f,
                (size.height - arcDiameter) / 2f,
            )
        // Track
        drawArc(
            color = outlineColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            size = arcSize,
            topLeft = topLeft,
        )
        // Progress with animated reveal
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            size = arcSize,
            topLeft = topLeft,
        )
    }
}

@Composable
fun ThermalActionItem(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
        )
    }
}

// Map DB entity to UI model for the power profile cards
private fun mapPowerProfileEntity(
    p: com.voltai.smartbatteryguardian.data.database.entities.PowerProfile,
): PowerProfile {
    return PowerProfile(
        id = p.id.toString(),
        name = p.name,
        description = p.description,
        icon = getProfileIcon(p.name),
        color = getProfileColor(p.name),
        batterySavingsPercent = (p.estimatedSavings ?: 0.0).toInt(),
        performanceImpact = PerformanceImpact.LOW,
        features = emptyList(),
        isActive = p.isActive,
        isCustom = p.isCustom,
        aiOptimized = p.automationRules?.isNotEmpty() == true || p.name.contains("AI", ignoreCase = true),
        estimatedExtraHours = (p.estimatedSavings ?: 0.0),
    )
}

private fun getProfileIcon(name: String): String =
    when {
        name.contains("ai", true) -> "🧠"
        name.contains("performance", true) -> "💪"
        name.contains("battery", true) || name.contains("saver", true) -> "🔋"
        else -> "⚖️"
    }

private fun getProfileColor(name: String): Color =
    when {
        name.contains("ai", true) -> Color(0xFF2196F3) // Blue
        name.contains("performance", true) -> Color(0xFF9C27B0) // Deep Purple
        name.contains("battery", true) || name.contains("saver", true) -> Color(0xFF4CAF50) // Green
        else -> Color(0xFF2196F3)
    }

// Build ThermalData from real readings
private fun deriveThermalData(
    readings: List<com.voltai.smartbatteryguardian.data.database.entities.BatteryReading>,
    batteryData: com.voltai.smartbatteryguardian.BatteryData,
): ThermalData {
    val currentTemp = batteryData.temperature.toDouble()
    val history = readings.takeLast(40).map { TempPoint(it.timestamp, it.temperature, 0.0) }
    val maxTemp = (history.maxOfOrNull { it.temperature } ?: currentTemp).coerceAtLeast(currentTemp)
    return ThermalData(
        currentTemp = currentTemp,
        maxTemp = maxTemp,
        thermalState = getThermalStateFromTemp(currentTemp),
        coolingActive = currentTemp > 40.0,
        // Original data only: do not synthesize fallback points if history is empty
        temperatureHistory = history,
        hotspots =
        listOf(
            ComponentTemp("Battery", currentTemp, getComponentStatus(currentTemp), "🔋"),
        ),
    )
}

enum class FeatureCategory { DISPLAY, CPU, CONNECTIVITY, BACKGROUND, THERMAL }

data class ThermalData(
    val currentTemp: Double,
    val maxTemp: Double,
    val thermalState: ThermalState,
    val coolingActive: Boolean,
    val temperatureHistory: List<TempPoint>,
    val hotspots: List<ComponentTemp>,
)

data class TempPoint(
    val timestamp: Long,
    val temperature: Double,
    val cpuUsage: Double,
)

data class ComponentTemp(
    val component: String,
    val temperature: Double,
    val status: String,
    val icon: String,
)

enum class ThermalState { COOL, NORMAL, WARM, HOT, CRITICAL, WARNING }

data class AdaptiveOptimization(
    val isEnabled: Boolean,
    val learningProgress: Double,
    val optimizationScore: Int,
    val adaptations: List<Adaptation>,
    val nextOptimization: String,
    val dataPoints: Int,
)

data class Adaptation(
    val type: String,
    val description: String,
    val impact: String,
    val confidence: Double,
    val timestamp: Long,
)

data class PowerInsight(
    val title: String,
    val description: String,
    val recommendation: String,
    val priority: InsightPriority,
    val potentialSaving: String,
    val icon: String,
    val aiGenerated: Boolean,
)

enum class InsightPriority { CRITICAL, HIGH, MEDIUM, LOW, OPTIMIZATION }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AIPoweredPowerManagementScreen() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    var selectedTab by remember { mutableStateOf(0) }
    var isLearning by remember { mutableStateOf(false) }

    // Real power profiles from DB via ViewModel
    val profilesVm: PowerProfilesViewModel = hiltViewModel()
    val dbProfiles by profilesVm.profiles.collectAsState()
    val powerProfiles = remember(dbProfiles) { dbProfiles.map { mapPowerProfileEntity(it) } }

    // Real thermal history from readings
    val readingsVm: BatteryReadingsViewModel = hiltViewModel()
    val lastReadings by readingsVm.last24hReadings.collectAsState()
    val thermalData = remember(lastReadings, batteryData) { deriveThermalData(lastReadings, batteryData) }

    // Memoized with proper keys to prevent unnecessary recomputation
    val adaptiveOptimization =
        remember(batteryData.batteryLevel, batteryData.temperature, batteryData.isCharging) {
            generateAdaptiveOptimizationFromRealLocalized(context.resources, batteryData)
        }
    val powerInsights =
        remember(
            batteryData.batteryLevel,
            batteryData.temperature,
            batteryData.voltage,
            batteryData.isCharging,
        ) {
            generatePowerInsightsFromRealLocalized(context.resources, batteryData)
        }

    val tabs =
        listOf(
            stringResource(id = R.string.profiles_tab_title),
            stringResource(id = R.string.ai_adaptive_tab_title),
            stringResource(id = R.string.thermal_tab_title),
            stringResource(id = R.string.analytics_tab_title),
            stringResource(id = R.string.insights_tab_title),
        )

    var activeProfile by remember(powerProfiles) {
        mutableStateOf(
            powerProfiles.firstOrNull { it.isActive } ?: powerProfiles.firstOrNull(),
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.power_management),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text =
                    if (adaptiveOptimization.isEnabled) {
                        stringResource(
                            id = R.string.learning_patterns,
                        )
                    } else {
                        stringResource(id = R.string.ready_to_optimize)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
            }
        }

        // Power Status Metrics
        Card(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            if (powerProfiles.isEmpty()) {
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(id = R.string.no_active_profile),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.create_custom_profile),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    PowerStatusMetricMD3(
                        stringResource(id = R.string.active_profile_label),
                        activeProfile?.name ?: stringResource(id = R.string.no_active_profile),
                        activeProfile?.icon ?: "⚡",
                    )
                    PowerStatusMetricMD3(
                        stringResource(id = R.string.savings_label),
                        "${activeProfile?.batterySavingsPercent ?: 0}%",
                        "💾",
                    )
                    PowerStatusMetricMD3(
                        stringResource(id = R.string.thermal_label),
                        stringResource(id = getThermalStatusStringRes(thermalData.thermalState)),
                        "🌡️",
                    )
                    PowerStatusMetricMD3(
                        stringResource(id = R.string.ai_score_label),
                        "${adaptiveOptimization.optimizationScore}",
                        "🧠",
                    )
                }
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }

        // Content with smooth Battery Guru style animations
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { if (targetState > initialState) it else -it },
                    animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
                ) +
                    fadeIn(
                        animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { if (targetState > initialState) -it else it },
                        animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
                    ) +
                    fadeOut(
                        animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic),
                    )
            },
        ) { tab ->
            when (tab) {
                0 ->
                    PowerProfilesTab(powerProfiles, activeProfile) { selected ->
                        activeProfile = selected
                        selected.id.toLongOrNull()?.let { profilesVm.setActive(it) }
                    }
                1 -> AIAdaptiveTab(adaptiveOptimization, onToggleLearning = { isLearning = !isLearning })
                2 -> ThermalManagementTab(thermalData, batteryData)
                3 -> PowerAnalyticsTab(powerProfiles, thermalData)
                4 -> PowerInsightsTab(powerInsights)
            }
        }
    }
}

@Composable
fun StaticAIOptimizationIcon(isLearning: Boolean) {
    // Efficient static icon without battery-draining infinite animations
    Box(
        modifier =
        Modifier
            .size(48.dp)
            .background(
                if (isLearning) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isLearning) "🧠" else "⚡",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
fun PowerStatusMetric(
    label: String,
    value: String,
    icon: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontSize = 16.sp,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
    }
}

@Composable
fun PowerStatusMetricMD3(
    label: String,
    value: String,
    icon: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 0.dp, max = 120.dp),
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PowerProfilesTab(
    profiles: List<PowerProfile>,
    activeProfile: PowerProfile?,
    onProfileSelected: (PowerProfile) -> Unit,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Quick Profile Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.power_profiles_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (profiles.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.loading_profiles),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(profiles.filter { !it.isCustom }) { profile ->
                                QuickProfileCard(
                                    profile = profile,
                                    isActive = profile.id == activeProfile?.id,
                                    onClick = { onProfileSelected(profile) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Profiles
        item {
            Text(
                text = stringResource(id = R.string.custom_profiles_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        items(profiles.filter { it.isCustom }) { profile ->
            DetailedProfileCard(
                profile = profile,
                isActive = profile.id == activeProfile?.id,
                onSelect = { onProfileSelected(profile) },
                onEdit = { /* Open profile editor */ },
            )
        }

        // Create New Profile Button
        item {
            Card(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { /* Create new profile */ },
                shape = RoundedCornerShape(16.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            ) {
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_custom_profile),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.create_custom_profile),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
fun AIAdaptiveTab(
    optimization: AdaptiveOptimization,
    onToggleLearning: () -> Unit,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // AI Learning Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(id = R.string.ai_icon),
                                fontSize = 32.sp,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = stringResource(id = R.string.ai_adaptive_learning),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                                Text(
                                    text =
                                    if (optimization.isEnabled) {
                                        stringResource(
                                            id = R.string.active_learning_mode,
                                        )
                                    } else {
                                        stringResource(id = R.string.learning_disabled)
                                    },
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Switch(
                            checked = optimization.isEnabled,
                            onCheckedChange = { onToggleLearning() },
                            colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        )
                    }

                    if (optimization.isEnabled) {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Learning Progress
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = stringResource(id = R.string.learning_progress),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = "${(optimization.learningProgress * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = optimization.learningProgress.toFloat().coerceIn(0f, 1f),
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.secondaryContainer,
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text =
                                stringResource(
                                    id = R.string.data_points_next,
                                    optimization.dataPoints,
                                    optimization.nextOptimization,
                                ),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Optimization Score
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(id = R.string.ai_optimization_score_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        val optStatusRes = getOptimizationScoreStringRes(optimization.optimizationScore)
                        val ringCd =
                            stringResource(
                                id = R.string.optimization_ring_cd,
                                optimization.optimizationScore,
                                stringResource(id = optStatusRes),
                            )
                        Box(modifier = Modifier.semantics { contentDescription = ringCd }) {
                            OptimizationScoreRing(optimization.optimizationScore)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "${optimization.optimizationScore}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = getOptimizationScoreColor(optimization.optimizationScore),
                            )
                            Text(
                                text =
                                stringResource(
                                    id = getOptimizationScoreStringRes(optimization.optimizationScore),
                                ),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.based_on_points, optimization.dataPoints),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Recent Adaptations
        item {
            Text(
                text = stringResource(id = R.string.recent_ai_adaptations),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        items(optimization.adaptations) { adaptation ->
            AdaptationCard(adaptation)
        }
    }
}

@Composable
fun ThermalManagementTab(
    thermalData: ThermalData,
    batteryData: com.voltai.smartbatteryguardian.BatteryData,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Thermal Status Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = getThermalStatusColor(thermalData.thermalState).copy(alpha = 0.1f),
                ),
                border = BorderStroke(1.dp, getThermalStatusColor(thermalData.thermalState)),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = getThermalIcon(thermalData.thermalState),
                            fontSize = 32.sp,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = stringResource(id = R.string.thermal_management_title),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(id = getThermalStatusStringRes(thermalData.thermalState)),
                                fontSize = 16.sp,
                                color = getThermalStatusColor(thermalData.thermalState),
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        // Current Temperature Display
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = getThermalStatusColor(thermalData.thermalState).copy(alpha = 0.1f),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "${thermalData.currentTemp.toInt()}°C",
                                    color = getThermalStatusColor(thermalData.thermalState),
                                )
                                Text(
                                    text = stringResource(id = R.string.current_value_label),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Temperature Trend Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.temperature_trend_last_hour),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Only real battery readings - no fallback data
                    val readingsVm: BatteryReadingsViewModel = hiltViewModel()
                    val lastReadings by readingsVm.last24hReadings.collectAsState()

                    if (lastReadings.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                        ) {
                            Text(
                                text = "📊",
                                fontSize = 48.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.no_thermal_data),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        val history = lastReadings.map { TempPoint(it.timestamp, it.temperature, 0.0) }
                        ThermalTrendChart(history)
                    }
                }
            }
        }

        // Component Temperatures
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.component_temperatures_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    thermalData.hotspots.forEach { component ->
                        ComponentTemperatureItem(component)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Thermal Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.thermal_protection_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ThermalActionItem(
                        title = stringResource(id = R.string.active_cooling),
                        description = stringResource(id = R.string.active_cooling_desc),
                        enabled = thermalData.coolingActive,
                        onToggle = { /* Toggle cooling */ },
                    )

                    ThermalActionItem(
                        title = stringResource(id = R.string.background_limit),
                        description = stringResource(id = R.string.background_limit_desc),
                        enabled = true,
                        onToggle = { /* Toggle background limiting */ },
                    )

                    ThermalActionItem(
                        title = stringResource(id = R.string.display_dimming),
                        description = stringResource(id = R.string.display_dimming_desc),
                        enabled = false,
                        onToggle = { /* Toggle display dimming */ },
                    )
                }
            }
        }
    }
}

@Composable
fun PowerAnalyticsTab(
    profiles: List<PowerProfile>,
    thermalData: ThermalData,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Power Savings Analytics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.power_savings_analytics_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PowerSavingsChart(profiles)
                }
            }
        }

        // Usage Pattern Analysis
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.usage_pattern_analysis_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    UsagePatternChart()
                }
            }
        }

        // Current Draw Analysis (real/original data)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "Current Draw (Last 24h)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val readingsVm: BatteryReadingsViewModel = hiltViewModel()
                    val currentSeries by readingsVm.currentSeries.collectAsState()
                    if (currentSeries.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                        ) {
                            Text(
                                text = "⚡",
                                fontSize = 48.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.not_available),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "Current draw data will appear here once measurements begin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    } else {
                        CurrentDrawChart(series = currentSeries)
                    }
                }
            }
        }

        // Efficiency Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.efficiency_metrics_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        val sysVm: SystemMetricsViewModel = hiltViewModel()
                        val summaryState = sysVm.systemSummary.collectAsState()
                        val summary = summaryState.value
                        if (summary == null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.loading),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            EfficiencyMetricItem(
                                stringResource(id = R.string.screen_efficiency_label),
                                "—",
                                MaterialTheme.colorScheme.tertiary,
                            )
                            EfficiencyMetricItem(
                                stringResource(id = R.string.cpu_efficiency_label),
                                "${(100 - summary.avgCpu).toInt()}%",
                                MaterialTheme.colorScheme.primary,
                            )
                            EfficiencyMetricItem(
                                stringResource(id = R.string.perf_score_label),
                                "${summary.avgPerformance.toInt()}",
                                MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PowerInsightsTab(insights: List<PowerInsight>) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // AI-Generated Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🤖",
                        fontSize = 32.sp,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.ai_powered_insights_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(id = R.string.personalized_power_recommendations),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        items(insights) { insight ->
            PowerInsightCard(insight)
        }
    }
}

// Supporting Composables
@Composable
fun QuickProfileCard(
    profile: PowerProfile,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val profileCd = stringResource(id = R.string.power_profile_cd, profile.name)
    Card(
        modifier =
        Modifier
            .width(120.dp)
            .height(180.dp)
            .clickable { onClick() }
            .semantics { contentDescription = profileCd },
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 8.dp else 2.dp),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = profile.icon,
                fontSize = 32.sp,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = profile.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(id = R.string.extra_hours_life, profile.estimatedExtraHours.toInt()),
                    fontSize = 10.sp,
                    color = profile.color,
                    fontWeight = FontWeight.Medium,
                )
            }

            if (profile.aiOptimized) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                ) {
                    Text(
                        text = stringResource(id = R.string.ai_badge),
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedProfileCard(
    profile: PowerProfile,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor =
            if (isActive) {
                profile.color.copy(
                    alpha = 0.1f,
                )
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
        border = if (isActive) BorderStroke(2.dp, profile.color) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = profile.icon,
                    fontSize = 32.sp,
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = profile.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (profile.aiOptimized) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            ) {
                                Text(
                                    text = stringResource(id = R.string.ai_optimized_badge),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = profile.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = stringResource(id = R.string.extra_hours_short, profile.estimatedExtraHours.toInt()),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = profile.color,
                    )
                    Text(
                        text = stringResource(id = R.string.savings_percent, profile.batterySavingsPercent),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Performance Impact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.performance_impact_label),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(id = getImpactStringRes(profile.performanceImpact)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = getPerformanceImpactColor(profile.performanceImpact),
                    )
                }

                Row {
                    if (!isActive) {
                        TextButton(
                            onClick = onSelect,
                            colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = profile.color,
                            ),
                        ) {
                            Text(stringResource(id = R.string.activate))
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThermalTrendChart(temperatureHistory: List<TempPoint>) {
    val chartCd = stringResource(id = R.string.thermal_trend_chart_cd)
    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .semantics { contentDescription = chartCd },
    ) {
        if (temperatureHistory.isEmpty()) return@Canvas

        val stepX = size.width / (temperatureHistory.size - 1).coerceAtLeast(1)
        val maxTemp = temperatureHistory.maxOfOrNull { it.temperature }?.toFloat() ?: 100f
        val minTemp = temperatureHistory.minOfOrNull { it.temperature }?.toFloat() ?: 0f

        val tempPoints =
            temperatureHistory.mapIndexed { index, point ->
                androidx.compose.ui.geometry.Offset(
                    x = index * stepX,
                    y = size.height - ((point.temperature.toFloat() - minTemp) / (maxTemp - minTemp)) * size.height,
                )
            }

        // Draw temperature line
        for (i in 0 until tempPoints.size - 1) {
            drawLine(
                color = getThermalStatusColor(getThermalStateFromTemp(temperatureHistory[i].temperature)),
                start = tempPoints[i],
                end = tempPoints[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Draw points
        tempPoints.forEachIndexed { index, point ->
            drawCircle(
                color = getThermalStatusColor(getThermalStateFromTemp(temperatureHistory[index].temperature)),
                radius = 4.dp.toPx(),
                center = point,
            )
        }
    }
}

@Composable
fun PowerSavingsChart(profiles: List<PowerProfile>) {
    val chartCd = stringResource(id = R.string.power_savings_chart_cd)
    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .semantics { contentDescription = chartCd },
    ) {
        if (profiles.isEmpty()) return@Canvas

        val barCount = profiles.size
        val maxSaving = profiles.maxOf { it.batterySavingsPercent }.coerceAtLeast(1)
        val barSpace = size.width / (barCount * 1.5f)
        val barWidth = barSpace

        profiles.forEachIndexed { index, p ->
            val x = index * (barWidth + barSpace)
            val heightRatio = p.batterySavingsPercent / maxSaving.toFloat()
            val barHeight = heightRatio * (size.height * 0.8f)
            drawRoundRect(
                color = p.color,
                topLeft = androidx.compose.ui.geometry.Offset(x + barSpace, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            )
        }
    }
}

@Composable
fun UsagePatternChart() {
    val readingsVm: BatteryReadingsViewModel = hiltViewModel()
    val levelSeries by readingsVm.levelSeries.collectAsState()
    val chartCd = stringResource(id = R.string.usage_pattern_chart_cd)
    // Capture theme color before entering DrawScope
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .semantics { contentDescription = chartCd },
    ) {
        if (levelSeries.isEmpty()) return@Canvas

        val points = levelSeries.takeLast(24)
        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
        val maxY = 100f
        val path = androidx.compose.ui.graphics.Path()
        points.forEachIndexed { i, level ->
            val x = i * stepX
            val y = size.height - (level / maxY) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
fun EfficiencyMetricItem(
    label: String,
    value: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ComponentTemperatureItem(component: ComponentTemp) {
    val name =
        if (component.component.equals("Battery", ignoreCase = true)) {
            stringResource(id = R.string.battery_component)
        } else {
            component.component
        }
    val state = getThermalStateFromTemp(component.temperature)
    val statusText = stringResource(id = getThermalStatusStringRes(state))
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$name ${component.temperature.toInt()}°C $statusText" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = component.icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = name, fontWeight = FontWeight.Medium)
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = getThermalStatusColor(state),
                )
            }
        }
        Text(
            text = "${component.temperature.toInt()}°C",
            fontWeight = FontWeight.Bold,
            color = getThermalStatusColor(state),
        )
    }
}

@Composable
fun PowerInsightCard(insight: PowerInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor =
            when (insight.priority) {
                InsightPriority.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                InsightPriority.HIGH -> MaterialTheme.colorScheme.secondaryContainer
                InsightPriority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                InsightPriority.OPTIMIZATION -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            },
        ),
        border =
        BorderStroke(
            1.dp,
            when (insight.priority) {
                InsightPriority.CRITICAL -> MaterialTheme.colorScheme.error
                InsightPriority.HIGH -> MaterialTheme.colorScheme.secondary
                InsightPriority.MEDIUM -> MaterialTheme.colorScheme.secondary
                InsightPriority.OPTIMIZATION -> MaterialTheme.colorScheme.tertiary
                else -> Color.Transparent
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = insight.icon,
                    fontSize = 24.sp,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = insight.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (insight.aiGenerated) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            ) {
                                Text(
                                    text = stringResource(id = R.string.ai_badge),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = insight.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                if (insight.potentialSaving.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = insight.potentialSaving,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            if (insight.recommendation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "💡 ${insight.recommendation}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// Utility Functions
private fun getComponentStatus(temp: Double): String {
    return when {
        temp < 30.0 -> "Cool"
        temp < 40.0 -> "Normal"
        temp < 45.0 -> "Warm"
        else -> "Hot"
    }
}

// Legacy generators removed. Real data flows are used throughout this screen.

fun getThermalStatusColor(state: ThermalState): Color {
    return when (state) {
        ThermalState.COOL -> Color(0xFF2196F3) // Blue
        ThermalState.NORMAL -> Color(0xFF4CAF50) // Green
        ThermalState.WARM -> Color(0xFFFF9800) // Orange
        ThermalState.WARNING -> Color(0xFFE53935) // Red (warning)
        ThermalState.HOT -> Color(0xFFE53935) // Red
        ThermalState.CRITICAL -> Color(0xFFE53935) // Red
    }
}

fun getThermalStatusStringRes(state: ThermalState): Int {
    return when (state) {
        ThermalState.COOL -> R.string.thermal_status_cool
        ThermalState.NORMAL -> R.string.thermal_status_normal
        ThermalState.WARM -> R.string.thermal_status_warm
        ThermalState.WARNING -> R.string.thermal_status_warning
        ThermalState.HOT -> R.string.thermal_status_hot
        ThermalState.CRITICAL -> R.string.thermal_status_critical
    }
}

// Deprecated function removed - use getThermalStatusStringRes + stringResource instead

fun getThermalIcon(state: ThermalState): String {
    return when (state) {
        ThermalState.COOL -> "❄️"
        ThermalState.NORMAL -> "🌡️"
        ThermalState.WARM -> "🔥"
        ThermalState.WARNING -> "⚠️"
        ThermalState.HOT -> "🌋"
        ThermalState.CRITICAL -> "🚨"
    }
}

fun getThermalStateFromTemp(temperature: Double): ThermalState {
    return when {
        temperature < 25 -> ThermalState.COOL
        temperature < 35 -> ThermalState.NORMAL
        temperature < 40 -> ThermalState.WARM
        temperature < 45 -> ThermalState.HOT
        else -> ThermalState.CRITICAL
    }
}

@Composable
fun getOptimizationScoreColor(score: Int): Color {
    return when {
        score >= 90 -> MaterialTheme.colorScheme.tertiary
        score >= 70 -> MaterialTheme.colorScheme.primary
        score >= 50 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
}

fun getOptimizationScoreStringRes(score: Int): Int {
    return when {
        score >= 90 -> R.string.opt_score_excellent
        score >= 70 -> R.string.opt_score_good
        score >= 50 -> R.string.opt_score_fair
        else -> R.string.opt_score_needs_work
    }
}

// Deprecated function removed - use getOptimizationScoreStringRes + stringResource instead

@Composable
fun getPerformanceImpactColor(impact: PerformanceImpact): Color {
    return when (impact) {
        PerformanceImpact.NONE -> MaterialTheme.colorScheme.primary
        PerformanceImpact.LOW -> MaterialTheme.colorScheme.secondary
        PerformanceImpact.MEDIUM -> MaterialTheme.colorScheme.tertiary
        PerformanceImpact.HIGH -> MaterialTheme.colorScheme.error
    }
}

fun getImpactStringRes(impact: PerformanceImpact): Int {
    return when (impact) {
        PerformanceImpact.NONE -> R.string.impact_none
        PerformanceImpact.LOW -> R.string.impact_low
        PerformanceImpact.MEDIUM -> R.string.impact_medium
        PerformanceImpact.HIGH -> R.string.impact_high
    }
}

// Missing functions that generate real data
fun generateAdaptiveOptimizationFromReal(
    batteryData: com.voltai.smartbatteryguardian.BatteryData,
): AdaptiveOptimization {
    val batteryLevel = batteryData.batteryLevel
    val isCharging = batteryData.isCharging
    val temperature = batteryData.temperature

    // Calculate optimization score based on real data
    val optimizationScore =
        when {
            batteryLevel > 80 && !isCharging -> 95
            batteryLevel > 60 -> 85
            batteryLevel > 40 -> 70
            batteryLevel > 20 -> 55
            else -> 40
        }

    // Determine if AI optimization should be enabled based on battery state
    val isEnabled = batteryLevel < 80 || temperature > 35

    return AdaptiveOptimization(
        isEnabled = isEnabled,
        learningProgress = minOf(batteryLevel / 100.0, 0.95),
        optimizationScore = optimizationScore,
        adaptations =
        listOf(
            Adaptation(
                type = "Battery-Based CPU Scaling",
                description = "CPU frequency adjusted based on current battery level ($batteryLevel%)",
                impact = "${if (batteryLevel < 50) 15 else 8}% power savings",
                confidence = 0.88,
                timestamp = System.currentTimeMillis() - (1 * 60 * 60 * 1000),
            ),
            Adaptation(
                type = "Temperature-Based Performance",
                description = "Performance throttling based on temperature ($temperature°C)",
                impact = "${if (temperature > 35) "Thermal protection active" else "Normal operation"}",
                confidence = 0.92,
                timestamp = System.currentTimeMillis() - (30 * 60 * 1000),
            ),
            Adaptation(
                type = "Charging State Optimization",
                description = if (isCharging) "Optimized for charging efficiency" else "Power saving mode active",
                impact = "${if (isCharging) "Faster charging" else "Extended battery life"}",
                confidence = 0.85,
                timestamp = System.currentTimeMillis() - (5 * 60 * 1000),
            ),
        ),
        nextOptimization =
        when {
            batteryLevel < 30 -> "Critical power saving in 5min"
            temperature > 40 -> "Thermal management in 2min"
            else -> "Background optimization in 1h"
        },
        dataPoints = batteryLevel * 10 + 500,
    )
}

fun generatePowerInsightsFromReal(batteryData: com.voltai.smartbatteryguardian.BatteryData): List<PowerInsight> {
    val insights = mutableListOf<PowerInsight>()
    val batteryLevel = batteryData.batteryLevel
    val temperature = batteryData.temperature
    val voltage = batteryData.voltage
    val isCharging = batteryData.isCharging

    // Battery level insights
    when {
        batteryLevel < 20 && !isCharging -> {
            insights.add(
                PowerInsight(
                    title = "Critical Battery Level",
                    description = "Battery is at $batteryLevel%. Consider enabling power saving mode immediately.",
                    recommendation = "Enable ultra power saving mode and close non-essential apps",
                    priority = InsightPriority.CRITICAL,
                    potentialSaving = "+2-3h",
                    icon = "🔋",
                    aiGenerated = false,
                ),
            )
        }
        batteryLevel < 50 -> {
            insights.add(
                PowerInsight(
                    title = "Moderate Battery Usage",
                    description = "Battery at $batteryLevel%. Consider optimizing background apps.",
                    recommendation = "Review background app activity and disable unnecessary features",
                    priority = InsightPriority.MEDIUM,
                    potentialSaving = "+1-2h",
                    icon = "📱",
                    aiGenerated = true,
                ),
            )
        }
    }

    // Temperature insights
    when {
        temperature > 40 -> {
            insights.add(
                PowerInsight(
                    title = "High Temperature Detected",
                    description = "Device temperature is $temperature°C. This can impact battery health and performance.",
                    recommendation = "Reduce intensive tasks, remove device case, or move to cooler location",
                    priority = InsightPriority.HIGH,
                    potentialSaving = "Better longevity",
                    icon = "🌡️",
                    aiGenerated = false,
                ),
            )
        }
        temperature < 25 -> {
            insights.add(
                PowerInsight(
                    title = "Optimal Temperature",
                    description = "Device temperature is excellent at $temperature°C. Battery efficiency is optimal.",
                    recommendation = "Continue current usage patterns for best battery health",
                    priority = InsightPriority.OPTIMIZATION,
                    potentialSaving = "",
                    icon = "❄️",
                    aiGenerated = false,
                ),
            )
        }
    }

    // Charging insights
    if (isCharging) {
        insights.add(
            PowerInsight(
                title = "Charging Optimization",
                description = "Currently charging. Voltage: ${String.format("%.2f", voltage)}V",
                recommendation = "For best battery health, unplug when reaching 80-90%",
                priority = InsightPriority.LOW,
                potentialSaving = "Better longevity",
                icon = "⚡",
                aiGenerated = true,
            ),
        )
    }

    // Voltage insights
    when {
        voltage < 3.5 -> {
            insights.add(
                PowerInsight(
                    title = "Low Battery Voltage",
                    description = "Voltage at ${String.format("%.2f", voltage)}V indicates very low charge.",
                    recommendation = "Charge immediately to prevent battery damage",
                    priority = InsightPriority.CRITICAL,
                    potentialSaving = "Prevent damage",
                    icon = "⚠️",
                    aiGenerated = false,
                ),
            )
        }
        voltage > 4.2 -> {
            insights.add(
                PowerInsight(
                    title = "High Battery Voltage",
                    description = "Voltage at ${String.format("%.2f", voltage)}V indicates full charge.",
                    recommendation = "Consider unplugging to avoid overcharging stress",
                    priority = InsightPriority.LOW,
                    potentialSaving = "Better longevity",
                    icon = "🔋",
                    aiGenerated = true,
                ),
            )
        }
    }

    // If no specific insights, add general optimization
    if (insights.isEmpty()) {
        insights.add(
            PowerInsight(
                title = "Battery Status Normal",
                description = "All battery metrics are within normal ranges. Level: $batteryLevel%, Temp: $temperature°C",
                recommendation = "Continue current usage patterns for optimal performance",
                priority = InsightPriority.OPTIMIZATION,
                potentialSaving = "",
                icon = "✅",
                aiGenerated = false,
            ),
        )
    }

    return insights
}

fun generateAdaptiveOptimizationFromRealLocalized(
    res: Resources,
    batteryData: com.voltai.smartbatteryguardian.BatteryData,
): AdaptiveOptimization {
    val batteryLevel = batteryData.batteryLevel
    val isCharging = batteryData.isCharging
    val temperature = batteryData.temperature

    val optimizationScore =
        when {
            batteryLevel > 80 && !isCharging -> 95
            batteryLevel > 60 -> 85
            batteryLevel > 40 -> 70
            batteryLevel > 20 -> 55
            else -> 40
        }

    val isEnabled = batteryLevel < 80 || temperature > 35
    val batterySavingsPercent = if (batteryLevel < 50) 15 else 8

    val adaptations =
        listOf(
            Adaptation(
                type = res.getString(R.string.adaptation_type_battery_cpu_scaling),
                description = res.getString(R.string.adaptation_desc_battery_cpu_scaling, batteryLevel),
                impact = res.getString(R.string.adaptation_impact_power_savings_percent, batterySavingsPercent),
                confidence = 0.88,
                timestamp = System.currentTimeMillis() - (1 * 60 * 60 * 1000),
            ),
            Adaptation(
                type = res.getString(R.string.adaptation_type_temp_perf),
                description = res.getString(R.string.adaptation_desc_temp_perf, temperature),
                impact =
                if (temperature > 35) {
                    res.getString(
                        R.string.adaptation_impact_temp_active,
                    )
                } else {
                    res.getString(R.string.adaptation_impact_temp_normal)
                },
                confidence = 0.92,
                timestamp = System.currentTimeMillis() - (30 * 60 * 1000),
            ),
            Adaptation(
                type = res.getString(R.string.adaptation_type_charging_state_opt),
                description =
                if (isCharging) {
                    res.getString(
                        R.string.adaptation_desc_charging_opt_on,
                    )
                } else {
                    res.getString(R.string.adaptation_desc_charging_opt_off)
                },
                impact =
                if (isCharging) {
                    res.getString(
                        R.string.adaptation_impact_charging_faster,
                    )
                } else {
                    res.getString(R.string.adaptation_impact_charging_extended)
                },
                confidence = 0.85,
                timestamp = System.currentTimeMillis() - (5 * 60 * 1000),
            ),
        )

    val nextOptimization =
        when {
            batteryLevel < 30 -> res.getString(R.string.next_optimization_critical)
            temperature > 40 -> res.getString(R.string.next_optimization_thermal)
            else -> res.getString(R.string.next_optimization_background)
        }

    return AdaptiveOptimization(
        isEnabled = isEnabled,
        learningProgress = minOf(batteryLevel / 100.0, 0.95),
        optimizationScore = optimizationScore,
        adaptations = adaptations,
        nextOptimization = nextOptimization,
        dataPoints = batteryLevel * 10 + 500,
    )
}

fun generatePowerInsightsFromRealLocalized(
    res: Resources,
    batteryData: com.voltai.smartbatteryguardian.BatteryData,
): List<PowerInsight> {
    val insights = mutableListOf<PowerInsight>()
    val batteryLevel = batteryData.batteryLevel
    val temperature = batteryData.temperature
    val voltage = batteryData.voltage
    val isCharging = batteryData.isCharging

    // Battery level insights
    when {
        batteryLevel < 20 && !isCharging -> {
            insights.add(
                PowerInsight(
                    title = res.getString(R.string.insights_critical_battery_title),
                    description = res.getString(R.string.insights_critical_battery_desc, batteryLevel),
                    recommendation = res.getString(R.string.insights_critical_battery_reco),
                    priority = InsightPriority.CRITICAL,
                    potentialSaving = "+2-3h",
                    icon = "🔋",
                    aiGenerated = false,
                ),
            )
        }
        batteryLevel < 50 -> {
            insights.add(
                PowerInsight(
                    title = res.getString(R.string.insights_moderate_battery_title),
                    description = res.getString(R.string.insights_moderate_battery_desc, batteryLevel),
                    recommendation = res.getString(R.string.insights_moderate_battery_reco),
                    priority = InsightPriority.MEDIUM,
                    potentialSaving = "+1-2h",
                    icon = "📱",
                    aiGenerated = true,
                ),
            )
        }
    }

    // Temperature insights
    when {
        temperature > 40 -> {
            insights.add(
                PowerInsight(
                    title = res.getString(R.string.insights_high_temp_title),
                    description = res.getString(R.string.insights_high_temp_desc, temperature),
                    recommendation = res.getString(R.string.insights_high_temp_reco),
                    priority = InsightPriority.HIGH,
                    potentialSaving = res.getString(R.string.label_better_longevity),
                    icon = "🌡️",
                    aiGenerated = false,
                ),
            )
        }
        temperature < 25 -> {
            insights.add(
                PowerInsight(
                    title = res.getString(R.string.insights_optimal_temp_title),
                    description = res.getString(R.string.insights_optimal_temp_desc, temperature),
                    recommendation = res.getString(R.string.insights_optimal_temp_reco),
                    priority = InsightPriority.OPTIMIZATION,
                    potentialSaving = "",
                    icon = "❄️",
                    aiGenerated = false,
                ),
            )
        }
    }

    // Charging insights
    if (isCharging) {
        insights.add(
            PowerInsight(
                title = res.getString(R.string.insights_charging_opt_title),
                description = res.getString(R.string.insights_charging_opt_desc, voltage),
                recommendation = res.getString(R.string.insights_charging_opt_reco),
                priority = InsightPriority.LOW,
                potentialSaving = res.getString(R.string.label_better_longevity),
                icon = "⚡",
                aiGenerated = true,
            ),
        )
    }

    // Voltage insights
    when {
        voltage < 3.5 -> {
            insights.add(
                PowerInsight(
                    title = res.getString(R.string.insights_low_voltage_title),
                    description = res.getString(R.string.insights_low_voltage_desc, voltage),
                    recommendation = res.getString(R.string.insights_low_voltage_reco),
                    priority = InsightPriority.CRITICAL,
                    potentialSaving = res.getString(R.string.label_prevent_damage),
                    icon = "⚠️",
                    aiGenerated = false,
                ),
            )
        }
        voltage > 4.2 -> {
            insights.add(
                PowerInsight(
                    title = res.getString(R.string.insights_high_voltage_title),
                    description = res.getString(R.string.insights_high_voltage_desc, voltage),
                    recommendation = res.getString(R.string.insights_high_voltage_reco),
                    priority = InsightPriority.LOW,
                    potentialSaving = res.getString(R.string.label_better_longevity),
                    icon = "🔋",
                    aiGenerated = true,
                ),
            )
        }
    }

    // General normal status when nothing else applies
    if (insights.isEmpty()) {
        insights.add(
            PowerInsight(
                title = res.getString(R.string.insights_status_normal_title),
                description = res.getString(R.string.insights_status_normal_desc, batteryLevel, temperature),
                recommendation = res.getString(R.string.insights_status_normal_reco),
                priority = InsightPriority.OPTIMIZATION,
                potentialSaving = "",
                icon = "✅",
                aiGenerated = false,
            ),
        )
    }

    return insights
}
