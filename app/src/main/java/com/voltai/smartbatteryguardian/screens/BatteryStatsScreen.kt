package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.analytics.BatteryInsight
import com.voltai.smartbatteryguardian.analytics.DailyBatteryStats
import com.voltai.smartbatteryguardian.analytics.InsightSeverity
import com.voltai.smartbatteryguardian.analytics.OptimizationTip
import com.voltai.smartbatteryguardian.analytics.PowerConsumption
import com.voltai.smartbatteryguardian.analytics.rememberBatteryAnalytics

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun BatteryStatsScreen() {
    val context = LocalContext.current
    val analyticsEngine = rememberBatteryAnalytics(context)

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Analytics", "Insights", "Consumption")

    val dailyStats = remember { analyticsEngine.generateDailyStats(7) }
    val weeklyAnalysis = remember { analyticsEngine.generateWeeklyAnalysis() }
    val insights = remember { analyticsEngine.generateBatteryInsights() }
    val powerConsumption = remember { analyticsEngine.getPowerConsumptionBreakdown() }
    val chargingAnalysis = remember { analyticsEngine.getChargingPatternAnalysis() }
    val optimizationTips = remember { analyticsEngine.getOptimizationRecommendations() }

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Battery Analytics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Advanced battery insights & statistics",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content with Battery Guru style Animation
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { if (targetState > initialState) it else -it },
                    animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
                ) +
                    fadeIn(
                        animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
                    ) with slideOutHorizontally(
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
                    OverviewTab(
                        dailyStats,
                        weeklyAnalysis.firstOrNull() ?: defaultWeeklyAnalysis(),
                        chargingAnalysis,
                    )
                1 -> AnalyticsTab(dailyStats, weeklyAnalysis)
                2 -> InsightsTab(insights)
                3 -> ConsumptionTab(powerConsumption, optimizationTips)
            }
        }
    }
}

@Composable
fun OverviewTab(
    dailyStats: List<DailyBatteryStats>,
    weeklyAnalysis: com.voltai.smartbatteryguardian.analytics.WeeklyAnalysis,
    chargingAnalysis: com.voltai.smartbatteryguardian.analytics.ChargingAnalysis,
) {
    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Quick Stats Cards with Staggered Animation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = true,
                enter =
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
                ) + fadeIn(animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)),
            ) {
                QuickStatCard(
                    title = "Avg Screen Time",
                    value = "${weeklyAnalysis.averageScreenTime / 60}h ${weeklyAnalysis.averageScreenTime % 60}m",
                    icon = "📱",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }

            AnimatedVisibility(
                visible = true,
                enter =
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400),
                ) + fadeIn(animationSpec = tween(300, delayMillis = 100)),
            ) {
                QuickStatCard(
                    title = "Charging Cycles",
                    value = String.format("%.1f", weeklyAnalysis.averageChargingCycles),
                    icon = "🔋",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = true,
                enter =
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400),
                ) + fadeIn(animationSpec = tween(300, delayMillis = 200)),
            ) {
                QuickStatCard(
                    title = "Efficiency Trend",
                    value = "${if (weeklyAnalysis.powerEfficiencyTrend > 0) "+" else ""}${String.format(
                        "%.1f",
                        weeklyAnalysis.powerEfficiencyTrend * 100,
                    )}%",
                    icon = if (weeklyAnalysis.powerEfficiencyTrend > 0) "📈" else "📉",
                    color = if (weeklyAnalysis.powerEfficiencyTrend > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
            }

            AnimatedVisibility(
                visible = true,
                enter =
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400),
                ) + fadeIn(animationSpec = tween(300, delayMillis = 300)),
            ) {
                QuickStatCard(
                    title = "Avg Temperature",
                    value = "${String.format("%.1f", dailyStats.map { it.averageTemperature }.average())}°C",
                    icon = "🌡️",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Usage Chart Placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "📊 Daily Battery Usage Trend",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Simplified chart representation
                dailyStats.forEach { stat ->
                    DailyUsageItem(stat)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Charging Analysis
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "⚡ Charging Pattern Analysis",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ChargingMetric(
                        label = "Avg Speed",
                        value = "${chargingAnalysis.averageChargingSpeed}W",
                        icon = "⚡",
                    )
                    ChargingMetric(
                        label = "Efficiency",
                        value = "${chargingAnalysis.chargingEfficiency.toInt()}%",
                        icon = "✅",
                    )
                    ChargingMetric(
                        label = "Heat Risk",
                        value = getRiskLabel(chargingAnalysis.heatGenerationRisk),
                        icon = "🌡️",
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Optimal charging windows: ${chargingAnalysis.optimalChargingWindows.joinToString(", ")}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun AnalyticsTab(
    dailyStats: List<DailyBatteryStats>,
    weeklyAnalysis: List<com.voltai.smartbatteryguardian.analytics.WeeklyAnalysis>,
) {
    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Weekly Comparison
        weeklyAnalysis.forEach { week ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "📅 ${week.week} Analysis",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Top Power Consumers:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    week.topPowerConsumers.take(3).forEach { (component, percentage) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = component,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${percentage.toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        LinearProgressIndicator(
                            progress = (percentage / 50f).coerceIn(0f, 1f), // Normalized & clamped
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun InsightsTab(insights: List<BatteryInsight>) {
    LazyColumn {
        itemsIndexed(insights) { index, insight ->
            InsightCard(insight)
            if (index < insights.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ConsumptionTab(
    powerConsumption: List<PowerConsumption>,
    optimizationTips: List<OptimizationTip>,
) {
    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Power Consumption Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "🔋 Power Consumption Breakdown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                powerConsumption.forEach { consumption ->
                    PowerConsumptionItem(consumption)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Optimization Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "💡 Optimization Recommendations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                optimizationTips.forEach { tip ->
                    OptimizationTipItem(tip)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
        ),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun DailyUsageItem(stat: DailyBatteryStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stat.date,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${stat.screenOnTime / 60}h ${stat.screenOnTime % 60}m screen",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${stat.totalUsage.toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = getUsageColor(stat.totalUsage),
            )
            Text(
                text = "${stat.efficiencyScore.toInt()}% efficient",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    LinearProgressIndicator(
        progress = (stat.totalUsage / 100f).coerceIn(0f, 1f),
        modifier =
        Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        color = getUsageColor(stat.totalUsage),
        trackColor = getUsageColor(stat.totalUsage).copy(alpha = 0.2f),
    )
}

@Composable
fun ChargingMetric(
    label: String,
    value: String,
    icon: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun InsightCard(insight: BatteryInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = getSeverityColor(insight.severity).copy(alpha = 0.1f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = insight.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = insight.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getSeverityColor(insight.severity),
                ) {
                    Text(
                        text = "${(insight.confidence * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            insight.improvement?.let { improvement ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    ),
                ) {
                    Text(
                        text = "💡 $improvement",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun PowerConsumptionItem(consumption: PowerConsumption) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = consumption.component,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = consumption.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = "${consumption.percentage.toInt()}%",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }

    LinearProgressIndicator(
        progress = (consumption.percentage / 50f).coerceIn(0f, 1f), // Normalized & clamped
        modifier =
        Modifier
            .fillMaxWidth()
            .height(4.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    )
}

@Composable
fun OptimizationTipItem(tip: OptimizationTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = getPriorityColor(tip.priority).copy(alpha = 0.1f),
        ),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = tip.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = getPriorityColor(tip.priority),
                ) {
                    Text(
                        text = tip.estimatedSavings,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Text(
                    text = "${tip.priority} Priority",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun getUsageColor(usage: Float): Color {
    return when {
        usage <= 30 -> MaterialTheme.colorScheme.tertiary
        usage <= 60 -> MaterialTheme.colorScheme.tertiary
        usage <= 80 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun getSeverityColor(severity: InsightSeverity): Color {
    return when (severity) {
        InsightSeverity.INFO -> MaterialTheme.colorScheme.primary
        InsightSeverity.WARNING -> MaterialTheme.colorScheme.secondary
        InsightSeverity.CRITICAL -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun getPriorityColor(priority: String): Color {
    return when (priority.lowercase()) {
        "high" -> MaterialTheme.colorScheme.error
        "medium" -> MaterialTheme.colorScheme.secondary
        "low" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun getRiskLabel(risk: Float): String {
    return when {
        risk <= 0.3 -> "Low"
        risk <= 0.6 -> "Medium"
        else -> "High"
    }
}

private fun defaultWeeklyAnalysis(): com.voltai.smartbatteryguardian.analytics.WeeklyAnalysis {
    return com.voltai.smartbatteryguardian.analytics.WeeklyAnalysis(
        week = "This Week",
        averageScreenTime = 0,
        averageChargingCycles = 0f,
        powerEfficiencyTrend = 0f,
        healthTrend = 0f,
        topPowerConsumers = emptyList(),
    )
}
