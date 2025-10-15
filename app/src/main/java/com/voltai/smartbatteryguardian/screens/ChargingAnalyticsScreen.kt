package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.data.database.entities.ChargingSession
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.viewmodels.BatteryHealthViewModel
import com.voltai.smartbatteryguardian.viewmodels.SmartChargingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom data models for Charging Analytics
data class AnalyticsChargingSession(
    val startTime: String,
    val endTime: String,
    val duration: String,
    val startLevel: Int,
    val endLevel: Int,
    val chargingSpeed: String,
    val chargerType: String,
    val temperature: Float,
    val efficiency: Float,
)

data class AnalyticsChargingProfile(
    val name: String,
    val icon: String,
    val sessions: Int,
    val avgTime: String,
    val avgSpeed: String,
    val efficiency: Float,
)

data class TimeSeriesPoint(val speedW: Double, val efficiencyPct: Double)

@Composable
fun GroupedBarsChart(points: List<TimeSeriesPoint>) {
    val maxSpeed = remember(points) { points.maxOfOrNull { it.speedW }?.coerceAtLeast(1.0) ?: 1.0 }
    val area = 80.dp
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(area)
            .semantics { contentDescription = "Grouped bars: speed and efficiency per point" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        points.forEach { p ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                    Box(
                        modifier =
                        Modifier.width(
                            8.dp,
                        ).height(
                            ((p.speedW / maxSpeed) * 76).coerceIn(2.0, 76.0).dp,
                        ).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)),
                    )
                    Box(
                        modifier =
                        Modifier.width(
                            8.dp,
                        ).height(
                            ((p.efficiencyPct / 100.0) * 76).coerceIn(2.0, 76.0).dp,
                        ).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)),
                    )
                }
            }
        }
    }
}

@Composable
fun MiniLinesChart(points: List<TimeSeriesPoint>) {
    // Render two thin sparklines using variable height bars
    val maxSpeed = remember(points) { points.maxOfOrNull { it.speedW }?.coerceAtLeast(1.0) ?: 1.0 }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .semantics { contentDescription = "Speed sparkline" },
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            points.forEach { p ->
                Box(
                    modifier =
                    Modifier
                        .width(3.dp)
                        .height(((p.speedW / maxSpeed) * 28).coerceIn(1.0, 28.0).dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                )
            }
        }
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .semantics { contentDescription = "Efficiency sparkline" },
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            points.forEach { p ->
                Box(
                    modifier =
                    Modifier
                        .width(3.dp)
                        .height(((p.efficiencyPct / 100.0) * 28).coerceIn(1.0, 28.0).dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingAnalyticsScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    @Composable
    fun PerChargerTimeSeriesCard(
        sessions7d: List<ChargingSession>,
        sessions30d: List<ChargingSession>,
        sessions90d: List<ChargingSession>,
    ) {
        Card(
            modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 220.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            var grouped by remember { mutableStateOf(true) }
            var rangeDays by remember { mutableStateOf(30) }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "🔌 Per-Charger Time Series", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = grouped,
                        onClick = { grouped = true },
                        label = { Text("Grouped Bars", fontSize = 12.sp) },
                    )
                    FilterChip(
                        selected = !grouped,
                        onClick = { grouped = false },
                        label = { Text("Mini Lines", fontSize = 12.sp) },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = rangeDays == 7,
                        onClick = { rangeDays = 7 },
                        label = { Text("7d", fontSize = 12.sp) },
                    )
                    FilterChip(
                        selected = rangeDays == 30,
                        onClick = { rangeDays = 30 },
                        label = { Text("30d", fontSize = 12.sp) },
                    )
                    FilterChip(
                        selected = rangeDays == 90,
                        onClick = { rangeDays = 90 },
                        label = { Text("90d", fontSize = 12.sp) },
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            val selectedSessions =
                remember(rangeDays, sessions7d, sessions30d, sessions90d) {
                    when (rangeDays) {
                        7 -> sessions7d
                        90 -> sessions90d
                        else -> sessions30d
                    }
                }

            val groups =
                remember(selectedSessions) {
                    selectedSessions.filter { it.endTime != null && it.duration > 0 }
                        .groupBy { it.chargerType.ifBlank { "Unknown" } }
                        .toList().sortedByDescending { it.second.size }
                }
            if (groups.isEmpty()) {
                EmptyState("No charger data available")
            } else {
                groups.forEach { (type, list) ->
                    val points =
                        remember(list) {
                            list.sortedBy { it.endTime ?: Long.MIN_VALUE }
                                .takeLast(8).map { s ->
                                    val hours = s.duration / 3_600_000.0
                                    val energyWh =
                                        if (s.energyConsumed > 0) {
                                            s.energyConsumed
                                        } else if (s.avgPower > 0 && hours > 0) {
                                            s.avgPower * hours
                                        } else {
                                            0.0
                                        }
                                    val speed =
                                        if (s.avgPower > 0) {
                                            s.avgPower.toDouble()
                                        } else if (hours > 0 && energyWh > 0) {
                                            energyWh / hours
                                        } else {
                                            0.0
                                        }
                                    TimeSeriesPoint(speedW = speed, efficiencyPct = s.efficiency.toDouble())
                                }
                        }
                    Column(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .semantics { contentDescription = "$type series with ${points.size} points" },
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text =
                                when {
                                    type.contains("wireless", true) -> "📱"
                                    type.contains("pd", true) -> "🔌"
                                    type.contains("quick", true) -> "⚡"
                                    else -> "🔋"
                                },
                                fontSize = 18.sp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = type, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${list.size} sessions",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (points.isEmpty()) {
                            Text(
                                text = "No points",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            if (grouped) {
                                GroupedBarsChart(points)
                            } else {
                                MiniLinesChart(points)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    val tabs = listOf("⚡ Overview", "📊 Analytics", "🔌 Sessions", "🧠 Insights")

    val scVm: SmartChargingViewModel = hiltViewModel()
    val summary by scVm.summary.collectAsState()
    val healthVm: BatteryHealthViewModel = hiltViewModel()
    val recentSessions by healthVm.recentChargingSessions.collectAsState()
    val overview by healthVm.chargingOverviewToday.collectAsState()
    val deltas14d by healthVm.chargeDeltasLast14d.collectAsState()
    val thermalIdx by healthVm.thermalExposureIndex30d.collectAsState()
    val sessions7d by healthVm.chargingSessionsLast7d.collectAsState()
    val sessions30d by healthVm.chargingSessionsLast30d.collectAsState()
    val sessions90d by healthVm.chargingSessionsLast90d.collectAsState()

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors =
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                ),
            ),
    ) {
        // Premium Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                        Modifier
                            .size(56.dp)
                            .background(
                                Brush.radialGradient(
                                    colors =
                                    listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                    ),
                                ),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "⚡", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Charging Analytics",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface,
                        )
                        Text(
                            text = "Advanced charging insights & optimization",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        )
                    }

                    ChargingStatusIndicator()
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    val effText by remember(
                        summary,
                    ) { derivedStateOf { summary.avgEfficiency?.let { "${it.toInt()}%" } ?: "—" } }
                    val timeText by remember(summary) { derivedStateOf { scVm.formatDuration(summary.avgDurationMs) } }
                    val powerText by remember(summary) {
                        derivedStateOf { summary.avgPowerW?.let { String.format(Locale.getDefault(), "%.1fW", it) } ?: "—" }
                    }
                    QuickStatCard(effText, "Efficiency", "📈")
                    QuickStatCard(timeText, "Avg Time", "⏱️")
                    QuickStatCard(powerText, "Avg Power", "🔋")
                }
                if (summary.sessionsCount < 5) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfidenceBadge(text = "Low sample (last 7d: ${summary.sessionsCount})")
                }
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(vertical = 12.dp),
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }

        // Content
        LazyColumn(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (selectedTab) {
                0 -> {
                    item { ChargingOverviewCard(overview) }
                    item { ChargingPatternsCard() }
                    item {
                        val profiles =
                            remember(recentSessions) {
                                val groups = recentSessions.groupBy { it.chargerType }
                                groups.map { (type, list) ->
                                    val completed = list.filter { it.endTime != null && it.duration > 0 }
                                    val count = completed.size
                                    val energyUsedWh: (ChargingSession) -> Double = { s ->
                                        val e = s.energyConsumed
                                        if (e > 0) {
                                            e
                                        } else if (s.avgPower > 0 && s.duration > 0) {
                                            s.avgPower * (s.duration / 3_600_000.0)
                                        } else {
                                            0.0
                                        }
                                    }
                                    val totalEnergyWh = completed.sumOf { energyUsedWh(it) }
                                    val totalTimeHours = completed.sumOf { it.duration } / 3_600_000.0
                                    val avgDurationMs = completed.map { it.duration }.takeIf { it.isNotEmpty() }?.average()?.toLong() ?: 0L
                                    val avgSpeedW = if (totalTimeHours > 0) totalEnergyWh / totalTimeHours else null
                                    val eff =
                                        if (totalEnergyWh > 0) {
                                            completed.sumOf { it.efficiency * energyUsedWh(it) } / totalEnergyWh
                                        } else {
                                            completed.map { it.efficiency }.takeIf { it.isNotEmpty() }?.average()
                                        }
                                    AnalyticsChargingProfile(
                                        name = type,
                                        icon =
                                        when {
                                            type.contains("wireless", ignoreCase = true) -> "📱"
                                            type.contains("pd", ignoreCase = true) -> "🔌"
                                            type.contains("quick", ignoreCase = true) -> "⚡"
                                            else -> "🔋"
                                        },
                                        sessions = count,
                                        avgTime = if (avgDurationMs > 0) scVm.formatDuration(avgDurationMs) else "—",
                                        avgSpeed = avgSpeedW?.let { String.format(Locale.getDefault(), "%.1fW", it) } ?: "—",
                                        efficiency = eff?.toFloat() ?: 0f,
                                    )
                                }.sortedByDescending { it.sessions }
                            }
                        ChargerTypesCard(profiles)
                    }
                }
                1 -> {
                    item { ChargingHistogram14d(deltas14d) }
                    item { ChargingEfficiencyChart() }
                    item { PowerDeliveryAnalysis() }
                    item { TemperatureStressCard(thermalIdx) }
                    item { PerChargerTimeSeriesCard(sessions7d, sessions30d, sessions90d) }
                    item { TemperatureAnalysis() }
                }
                2 -> {
                    if (recentSessions.isEmpty()) {
                        item { EmptyState("No charging sessions yet") }
                    } else {
                        items(recentSessions) { session ->
                            ChargingSessionCard(session)
                        }
                    }
                }
                3 -> {
                    item { AIInsightsCard() }
                    item { OptimizationRecommendations() }
                    item { PredictiveAnalysis() }
                }
            }
        }
    }
}

@Composable
fun ChargingStatusIndicator() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
    ) {
        Column(
            modifier =
            Modifier
                .padding(12.dp)
                .semantics { contentDescription = "Charging status: ${batteryData.chargingStatus}" },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                Modifier
                    .size(32.dp)
                    .background(
                        if (batteryData.isCharging) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            Color.Transparent
                        },
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (batteryData.isCharging) "⚡" else "🔌",
                    fontSize = 20.sp,
                )
            }
            Text(
                text = batteryData.chargingStatus,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.surface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun QuickStatCard(
    value: String,
    label: String,
    icon: String,
) {
    Surface(
        modifier = Modifier.defaultMinSize(minHeight = 72.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
    ) {
        Column(
            modifier =
            Modifier
                .padding(12.dp)
                .semantics { contentDescription = "$label: $value" },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = icon, fontSize = 20.sp)
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface,
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
fun ChargingOverviewCard(
    overview: com.voltai.smartbatteryguardian.viewmodels.BatteryHealthViewModel.DayChargingOverview,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "⚡ Today's Charging Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = ((overview.efficiencyAvg ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 8.dp,
                            strokeCap = StrokeCap.Round,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        // Subtle static lighting highlight
                        Box(
                            modifier =
                            Modifier
                                .align(Alignment.TopStart)
                                .size(28.dp)
                                .background(
                                    brush =
                                    Brush.radialGradient(
                                        colors =
                                        listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                                            Color.Transparent,
                                        ),
                                    ),
                                    shape = CircleShape,
                                ),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = overview.efficiencyAvg?.let { "${it.toInt()}%" } ?: "—",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Efficiency",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column {
                    val totalTimeText by remember(overview.totalTimeMs) {
                        derivedStateOf {
                            val ms = overview.totalTimeMs
                            val totalMinutes = ms / 60000
                            val h = totalMinutes / 60
                            val m = totalMinutes % 60
                            if (ms > 0) {
                                if (h > 0) {
                                    "${h}h ${m}m"
                                } else {
                                    "${m}m"
                                }
                            } else {
                                "—"
                            }
                        }
                    }
                    val energyText by remember(overview.energyWh) {
                        derivedStateOf { overview.energyWh?.let { String.format(Locale.getDefault(), "%.1f Wh", it) } ?: "—" }
                    }
                    val maxTempText by remember(overview.maxTemp) {
                        derivedStateOf { overview.maxTemp?.let { String.format(Locale.getDefault(), "%.1f°C", it) } ?: "—" }
                    }
                    InfoRow("🔋 Sessions", overview.sessionsCount.toString())
                    InfoRow("⏱️ Total Time", totalTimeText)
                    InfoRow("📊 Energy", energyText)
                    InfoRow("🌡️ Max Temp", maxTempText)
                }
            }
        }
    }
}

@Composable
fun ChargingSessionCard(session: ChargingSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text =
                        run {
                            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val start = fmt.format(Date(session.startTime))
                            val end = session.endTime?.let { fmt.format(Date(it)) } ?: "—"
                            "$start - $end"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = session.chargerType,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color =
                    when {
                        session.efficiency >= 90 -> MaterialTheme.colorScheme.primary
                        session.efficiency >= 75 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = "${session.efficiency.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                        when {
                            session.efficiency >= 90 -> MaterialTheme.colorScheme.primary
                            session.efficiency >= 75 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val endLevelText = session.endLevel?.toString() ?: "—"
                val durationText =
                    run {
                        val ms = session.duration
                        val totalMinutes = ms / 60000
                        val h = totalMinutes / 60
                        val m = totalMinutes % 60
                        if (ms > 0) {
                            if (h > 0) {
                                "${h}h ${m}m"
                            } else {
                                "${m}m"
                            }
                        } else {
                            "—"
                        }
                    }
                val speedText =
                    if (session.avgPower > 0) {
                        String.format(
                            Locale.getDefault(),
                            "%.1fW",
                            session.avgPower,
                        )
                    } else {
                        "—"
                    }
                val tempText =
                    if (session.avgTemperature > 0) {
                        String.format(
                            Locale.getDefault(),
                            "%.1f°C",
                            session.avgTemperature,
                        )
                    } else {
                        "—"
                    }
                InfoColumn("Battery", "${session.startLevel}% → $endLevelText%")
                InfoColumn("Speed", speedText)
                InfoColumn("Duration", durationText)
                InfoColumn("Temp", tempText)
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp),
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun InfoColumn(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

fun generateChargingProfiles(): List<AnalyticsChargingProfile> {
    return listOf(
        AnalyticsChargingProfile("USB-C PD", "🔌", 45, "1h 20m", "25W", 92.5f),
        AnalyticsChargingProfile("Wireless", "📱", 28, "2h 45m", "15W", 78.3f),
        AnalyticsChargingProfile("Quick Charge", "⚡", 15, "55m", "35W", 89.1f),
        AnalyticsChargingProfile("Standard USB", "🔋", 8, "3h 15m", "10W", 85.7f),
    )
}

fun generateChargingSessions(): List<AnalyticsChargingSession> {
    return listOf(
        AnalyticsChargingSession("09:15", "10:45", "1h 30m", 25, 80, "25W", "USB-C PD", 31.2f, 91.5f),
        AnalyticsChargingSession("14:30", "15:15", "45m", 60, 85, "15W", "Wireless", 29.8f, 78.2f),
        AnalyticsChargingSession("22:00", "23:20", "1h 20m", 20, 75, "30W", "Quick Charge", 33.1f, 88.9f),
        AnalyticsChargingSession("07:45", "08:30", "45m", 45, 70, "20W", "USB-C", 28.5f, 86.3f),
    )
}

// Placeholder composables for other sections
@Composable fun ChargingPatternsCard() { /* Implementation */ }

@Composable
fun ChargerTypesCard(profiles: List<AnalyticsChargingProfile>) {
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "🔌 Charger Types",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (profiles.isEmpty()) {
                Text(
                    text = "No charger data yet.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                profiles.forEach { p ->
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .semantics {
                                contentDescription = "${if (p.name.isNotBlank()) p.name else "Unknown"} charger: ${p.sessions} sessions, efficiency ${p.efficiency.toInt()}%"
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = p.icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (p.name.isNotBlank()) p.name else "Unknown",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "Sessions: ${p.sessions}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${p.efficiency.toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "${p.avgTime} • ${p.avgSpeed}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (p.sessions < 3) {
                                Spacer(modifier = Modifier.height(4.dp))
                                ConfidenceBadge(text = "Low sample")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable fun ChargingEfficiencyChart() { /* Implementation */ }

@Composable fun PowerDeliveryAnalysis() { /* Implementation */ }

@Composable fun TemperatureAnalysis() { /* Implementation */ }

@Composable fun AIInsightsCard() { /* Implementation */ }

@Composable fun OptimizationRecommendations() { /* Implementation */ }

@Composable fun PredictiveAnalysis() { /* Implementation */ }

@Composable
fun ConfidenceBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
fun EmptyState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "ℹ️", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ChargingHistogram14d(deltas: List<Int>) {
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 240.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📅 Charge Gain (14d)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (deltas.isEmpty() || deltas.all { it == 0 }) {
                Text(
                    text = "Insufficient data to display. Start charging to see trends.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val maxVal = remember(deltas) { (deltas.maxOrNull() ?: 0).coerceAtLeast(1) }
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .semantics { contentDescription = "14-day charge gain histogram" },
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    deltas.forEach { v ->
                        val h = ((v.toFloat() / maxVal) * 100f).coerceAtLeast(4f)
                        Box(
                            modifier =
                            Modifier
                                .weight(1f)
                                .height(h.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                    RoundedCornerShape(4.dp),
                                ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "−14d", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Today", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun TemperatureStressCard(thermalIndex: Double?) {
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "🌡️ Thermal Exposure (30d)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            val display = thermalIndex?.let { "${it.toInt()}%" } ?: "—"
            Text(
                text = display,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = "Higher values indicate more time spent near hot temperatures during charging.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
