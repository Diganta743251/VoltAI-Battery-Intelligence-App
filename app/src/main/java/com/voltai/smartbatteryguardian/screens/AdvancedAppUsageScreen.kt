package com.voltai.smartbatteryguardian.screens
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.models.AppUsageData
import com.voltai.smartbatteryguardian.models.InsightSeverity
import com.voltai.smartbatteryguardian.ui.components.FilterChipsRow
import com.voltai.smartbatteryguardian.ui.theme.extendedColors
import com.voltai.smartbatteryguardian.viewmodels.AppUsageUiViewModel

data class AppCategory(
    val name: String,
    val apps: List<AppUsageData>,
    val totalUsage: Long,
    val batteryImpact: Double,
    val color: Color,
)

data class UsageInsight(
    val title: String,
    val description: String,
    val impact: String,
    val severity: InsightSeverity,
    val recommendation: String,
    val icon: String,
)

// InsightSeverity moved to PremiumDataModels

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AdvancedAppUsageScreen() {
    val context = LocalContext.current
    val batteryData = com.voltai.smartbatteryguardian.rememberBatteryData(context)
    val usageVm: AppUsageUiViewModel = hiltViewModel()
    val appsData by usageVm.apps.collectAsState()
    val isLoading by usageVm.loading.collectAsState()
    val hasPermission by usageVm.hasPermission.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedPeriod by remember { mutableStateOf(0) }
    val categories by remember(appsData) { mutableStateOf(categorizeApps(appsData)) }
    val insights by remember(appsData) { mutableStateOf(generateUsageInsights(appsData)) }

    @Composable
    fun EmptyUsageState(onRefresh: () -> Unit) {
        Card(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "No usage data for the selected period",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Try changing the period or use the Refresh button.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onRefresh) { Text("Refresh") }
                }
            }
        }
    }

    val tabs = listOf("📊 Overview", "📱 Apps", "📂 Categories", "💡 Insights", "📈 Analytics")
    val periods = listOf("Today", "Yesterday", "Week", "Month")

    // Reload on period change (repository currently serves 24h totals)
    LaunchedEffect(selectedPeriod) {
        usageVm.load()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Header with title
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "App Usage Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "Advanced battery consumption insights",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
            }
        }

        // Period Selector
        FilterChipsRow(
            options = periods.indices.toList(),
            selectedOption = selectedPeriod,
            onSelectedChange = { selectedPeriod = it },
            labelOf = { idx -> periods[idx] },
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        // Permission banner only when permission is NOT granted.
        // When granted but data is empty, show a friendly empty state instead of re-asking.
        if (!isLoading && !hasPermission) {
            UsageAccessPermissionBanner()
        } else if (!isLoading && hasPermission && appsData.isEmpty()) {
            EmptyUsageState(onRefresh = { usageVm.load() })
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

        // Content with Loading State
        if (isLoading) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing app usage patterns...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { if (targetState > initialState) it else -it },
                    ) + fadeIn() togetherWith slideOutHorizontally(
                        targetOffsetX = { if (targetState > initialState) -it else it },
                    ) + fadeOut()
                },
            ) { tab ->
                when (tab) {
                    0 -> OverviewTab(appsData, categories)
                    1 -> AppsTab(appsData)
                    2 -> CategoriesTab(categories)
                    3 -> InsightsTab(insights)
                    4 -> AnalyticsTab(appsData, categories)
                }
            }
        }
    }
}

@Composable
fun UsageAccessPermissionBanner() {
    val context = LocalContext.current
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Usage access permission needed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "To show real app usage analytics, allow VoltAI access to Usage Data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = {
                    runCatching {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                }) {
                    Text("Open Settings")
                }
            }
        }
    }
}

@Composable
fun OverviewTab(
    appsData: List<AppUsageData>,
    categories: List<AppCategory>,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Quick Stats
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OverviewStatsCard(
                    title = "Total Screen Time",
                    value = formatDuration(appsData.sumOf { it.totalTimeInForeground }),
                    icon = "📱",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                OverviewStatsCard(
                    title = "Battery Impact",
                    value = "${appsData.sumOf { it.batteryUsage }.toInt()}%",
                    icon = "🔋",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OverviewStatsCard(
                    title = "Apps Opened",
                    value = "${appsData.sumOf { it.openCount }}",
                    icon = "🚀",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
                OverviewStatsCard(
                    title = "Avg Session",
                    value = formatDuration(appsData.map { it.avgSessionTime }.average().toLong()),
                    icon = "⏱️",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Usage Distribution Chart
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
                        text = "📊 Usage Distribution",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Donut Chart
                    Box(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        UsageDonutChart(categories.take(5))
                    }
                }
            }
        }

        // Top Apps Quick View
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "🏆 Top Apps",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        TextButton(onClick = { }) {
                            Text("View All")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    appsData.take(5).forEachIndexed { index, app ->
                        TopAppItem(app, index + 1)
                        if (index < 4) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // Hourly Usage Pattern
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
                        text = "⏰ Usage by Top Apps",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TopAppsUsageBarChart(appsData.take(10))
                }
            }
        }
    }
}

@Composable
fun AppsTab(appsData: List<AppUsageData>) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(appsData) { app ->
            DetailedAppUsageCard(app)
        }
    }
}

@Composable
fun CategoriesTab(categories: List<AppCategory>) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(categories) { category ->
            CategoryCard(category)
        }
    }
}

@Composable
fun InsightsTab(insights: List<UsageInsight>) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(insights) { insight ->
            InsightCard(insight)
        }
    }
}

@Composable
fun AnalyticsTab(
    appsData: List<AppUsageData>,
    categories: List<AppCategory>,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 24h Total Usage (real)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "📈 Total Usage (last 24h)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatDuration(appsData.sumOf { it.totalTimeInForeground }),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Battery Impact Analysis
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "🔋 Battery Impact Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    appsData.take(10).forEach { app ->
                        BatteryImpactItem(app)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// Supporting Composables
@Composable
fun OverviewStatsCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = icon,
                    fontSize = 16.sp,
                )
            }

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
fun UsageDonutChart(categories: List<AppCategory>) {
    Canvas(
        modifier =
        Modifier
            .size(150.dp)
            .padding(16.dp),
    ) {
        val total = categories.sumOf { it.totalUsage }.toFloat()
        val strokeWidth = 30.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val centerX = size.width / 2
        val centerY = size.height / 2

        var startAngle = -90f

        categories.forEach { category ->
            val sweepAngle = (category.totalUsage / total) * 360f

            drawArc(
                color = category.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                topLeft =
                androidx.compose.ui.geometry.Offset(
                    centerX - radius,
                    centerY - radius,
                ),
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun TopAppItem(
    app: AppUsageData,
    rank: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = getRankColor(rank),
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = rank.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = app.appName,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatDuration(app.totalTimeInForeground),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = "${app.batteryUsage.toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = "battery",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
fun DetailedAppUsageCard(app: AppUsageData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            val categoryTint = getAppCategoryColor(app.appName)
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = categoryTint.copy(alpha = 0.1f),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = getAppIcon(app.packageName),
                            fontSize = 24.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = app.appName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = "Opened ${app.openCount} times",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = formatDuration(app.totalTimeInForeground),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = categoryTint,
                    )
                    Text(
                        text = "${app.batteryUsage.toInt()}% battery",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Usage breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                UsageMetricItem("Today", formatDuration(app.usageToday))
                UsageMetricItem("Yesterday", formatDuration(app.usageYesterday))
                UsageMetricItem("Avg Session", formatDuration(app.avgSessionTime))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Usage bar
            LinearProgressIndicator(
                progress =
                (app.totalTimeInForeground.toFloat() / (8 * 60 * 60 * 1000L)).coerceAtMost(
                    1f,
                ),
                // Max 8 hours
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = categoryTint,
                trackColor = categoryTint.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
fun CategoryCard(category: AppCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                    Modifier
                        .size(48.dp)
                        .background(category.color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = getCategoryIcon(category.name),
                        fontSize = 24.sp,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Text(
                        text = "${category.apps.size} apps • ${formatDuration(category.totalUsage)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = "${category.batteryImpact.toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = category.color,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top apps in category
            category.apps.take(3).forEach { app ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = app.appName,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatDuration(app.totalTimeInForeground),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun InsightCard(insight: UsageInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor =
            when (insight.severity) {
                InsightSeverity.HIGH -> MaterialTheme.colorScheme.errorContainer
                InsightSeverity.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                InsightSeverity.LOW -> MaterialTheme.colorScheme.tertiaryContainer
                InsightSeverity.POSITIVE -> MaterialTheme.colorScheme.primaryContainer
                InsightSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                InsightSeverity.INFO -> MaterialTheme.colorScheme.surfaceVariant
                InsightSeverity.WARNING -> MaterialTheme.colorScheme.secondaryContainer
            },
        ),
        border =
        BorderStroke(
            1.dp,
            when (insight.severity) {
                InsightSeverity.HIGH -> MaterialTheme.colorScheme.error
                InsightSeverity.MEDIUM -> MaterialTheme.colorScheme.secondary
                InsightSeverity.LOW -> MaterialTheme.colorScheme.tertiary
                InsightSeverity.POSITIVE -> MaterialTheme.colorScheme.primary
                InsightSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                InsightSeverity.INFO -> MaterialTheme.colorScheme.primary
                InsightSeverity.WARNING -> MaterialTheme.colorScheme.secondary
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
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
                    Text(
                        text = insight.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = insight.impact,
                        fontSize = 12.sp,
                        color =
                        when (insight.severity) {
                            InsightSeverity.HIGH -> MaterialTheme.colorScheme.error
                            InsightSeverity.MEDIUM -> MaterialTheme.colorScheme.secondary
                            InsightSeverity.LOW -> MaterialTheme.colorScheme.tertiary
                            InsightSeverity.POSITIVE -> MaterialTheme.colorScheme.primary
                            InsightSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                            InsightSeverity.INFO -> MaterialTheme.colorScheme.primary
                            InsightSeverity.WARNING -> MaterialTheme.colorScheme.secondary
                        },
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = insight.description,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (insight.recommendation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
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

@Composable
fun HourlyUsageChart() {
    Text(
        text = "Hourly usage chart not available for this period",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun WeeklyTrendChart() {
    Text(
        text = "Weekly trend not available",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun TopAppsUsageBarChart(apps: List<AppUsageData>) {
    val topApps = apps.take(10)
    if (topApps.isEmpty()) {
        Text(
            text = "No usage data available",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
        return
    }

    val maxTime = topApps.maxOf { it.totalTimeInForeground }.coerceAtLeast(1L)

    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        val n = topApps.size
        val barWidth = size.width / (n + (n + 1) * 0.3f)
        val spacing = barWidth * 0.3f
        var x = spacing

        topApps.forEach { app ->
            val h = (app.totalTimeInForeground.toFloat() / maxTime) * (size.height * 0.8f)
            val y = size.height - h
            val color = getAppCategoryColor(app.appName)
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
            )
            x += barWidth + spacing
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        topApps.forEach { app ->
            Text(
                text = app.appName.take(3).uppercase(),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun BatteryImpactItem(app: AppUsageData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = getAppIcon(app.packageName),
            fontSize = 16.sp,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = app.appName,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        LinearProgressIndicator(
            progress = (app.batteryUsage / 100).toFloat().coerceIn(0f, 1f),
            modifier =
            Modifier
                .width(80.dp)
                .height(4.dp),
            color =
            when {
                app.batteryUsage > 20 -> MaterialTheme.colorScheme.error
                app.batteryUsage > 10 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.tertiary
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${app.batteryUsage.toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun UsageMetricItem(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Utility Functions
// removed obsolete loadAppUsageData(context, period)
// removed obsolete loadAppUsageDataFromReal(context, period, batteryData)
// removed obsolete generateMockAppUsageData()

fun categorizeApps(apps: List<AppUsageData>): List<AppCategory> {
    val categories = apps.groupBy { getAppCategory(it.appName) }

    return categories.map { (categoryName, categoryApps) ->
        AppCategory(
            name = categoryName,
            apps = categoryApps,
            totalUsage = categoryApps.sumOf { it.totalTimeInForeground },
            batteryImpact = categoryApps.sumOf { it.batteryUsage },
            color = getCategoryColorForName(categoryName),
        )
    }.sortedByDescending { it.totalUsage }
}

fun generateUsageInsightsFromReal(
    apps: List<AppUsageData>,
    batteryData: com.voltai.smartbatteryguardian.BatteryData,
): List<UsageInsight> {
    val insights = mutableListOf<UsageInsight>()

    // Battery health based insights
    val batteryHealth = batteryData.healthPercentage.toDouble() / 100.0
    if (batteryHealth < 0.8) {
        insights.add(
            UsageInsight(
                title = "Battery Health Impact",
                description = "Your battery health is at ${(batteryHealth * 100).toInt()}%, which may increase app power consumption.",
                impact = "High Battery Impact",
                severity = InsightSeverity.HIGH,
                recommendation = "Consider reducing usage of high-power apps and enabling battery optimization.",
                icon = "🔋",
            ),
        )
    }

    // Temperature based insights
    val temperature = batteryData.temperature.toDouble()
    if (temperature > 35.0) {
        insights.add(
            UsageInsight(
                title = "Device Temperature Warning",
                description = "Your device is running at ${temperature.toInt()}°C, which may affect battery efficiency.",
                impact = "Medium Battery Impact",
                severity = InsightSeverity.WARNING,
                recommendation = "Close resource-intensive apps and let your device cool down.",
                icon = "🌡️",
            ),
        )
    }

    // Battery level based insights
    val batteryLevel = batteryData.batteryLevel
    if (batteryLevel < 20 && !batteryData.isCharging) {
        val highPowerApps = apps.filter { it.batteryUsage > 10.0 }
        if (highPowerApps.isNotEmpty()) {
            insights.add(
                UsageInsight(
                    title = "Low Battery Alert",
                    description = "Battery at $batteryLevel%. ${highPowerApps.size} high-power apps are still running.",
                    impact = "Critical Battery Impact",
                    severity = InsightSeverity.CRITICAL,
                    recommendation = "Close ${highPowerApps.joinToString(
                        ", ",
                    ) { it.appName }} and enable power saving mode.",
                    icon = "⚠️",
                ),
            )
        }
    }

    // High usage app insight based on real data
    val topApp = apps.firstOrNull()
    if (topApp != null && topApp.totalTimeInForeground > 3 * 60 * 60 * 1000L) {
        insights.add(
            UsageInsight(
                title = "High Usage Detected",
                description = "${topApp.appName} consumed ${formatDuration(
                    topApp.totalTimeInForeground,
                )} of screen time, contributing to ${topApp.batteryUsage.toInt()}% battery drain.",
                impact = "High Battery Impact",
                severity = InsightSeverity.HIGH,
                recommendation = "Consider setting app limits or taking regular breaks to preserve battery life.",
                icon = "📱",
            ),
        )
    }

    // Charging pattern insights
    if (batteryData.isCharging && batteryLevel > 90) {
        insights.add(
            UsageInsight(
                title = "Optimal Charging Range",
                description = "Battery is at $batteryLevel% while charging. Overcharging may affect long-term battery health.",
                impact = "Low Battery Impact",
                severity = InsightSeverity.INFO,
                recommendation = "Consider unplugging once battery reaches 100% to maintain optimal battery health.",
                icon = "🔌",
            ),
        )
    }

    // Positive insight for good usage patterns
    val totalUsage = apps.sumOf { it.totalTimeInForeground }
    val totalBatteryUsage = apps.sumOf { it.batteryUsage }
    if (totalUsage < 4 * 60 * 60 * 1000L && batteryLevel > 40) {
        insights.add(
            UsageInsight(
                title = "Great Usage Balance!",
                description = "Your screen time is well-balanced with ${formatDuration(
                    totalUsage,
                )} total usage and ${totalBatteryUsage.toInt()}% battery consumption.",
                impact = "Positive Trend",
                severity = InsightSeverity.POSITIVE,
                recommendation = "Keep maintaining this healthy usage pattern for optimal battery life!",
                icon = "✅",
            ),
        )
    }

    return insights
}

fun generateUsageInsights(apps: List<AppUsageData>): List<UsageInsight> {
    val insights = mutableListOf<UsageInsight>()

    // High usage app insight
    val topApp = apps.firstOrNull()
    if (topApp != null && topApp.totalTimeInForeground > 3 * 60 * 60 * 1000L) { // More than 3 hours
        insights.add(
            UsageInsight(
                title = "High Usage Detected",
                description = "${topApp.appName} consumed ${formatDuration(
                    topApp.totalTimeInForeground,
                )} of screen time today.",
                impact = "High Battery Impact",
                severity = InsightSeverity.HIGH,
                recommendation = "Consider setting app limits or taking regular breaks.",
                icon = "⚠️",
            ),
        )
    }

    // Battery drain insight
    val highBatteryApps = apps.filter { it.batteryUsage > 15.0 }
    if (highBatteryApps.isNotEmpty()) {
        insights.add(
            UsageInsight(
                title = "Battery Draining Apps",
                description = "${highBatteryApps.size} apps are consuming significant battery power.",
                impact = "Medium Battery Impact",
                severity = InsightSeverity.MEDIUM,
                recommendation = "Close background apps or enable power saving mode.",
                icon = "🔋",
            ),
        )
    }

    // Positive insight
    val totalUsage = apps.sumOf { it.totalTimeInForeground }
    if (totalUsage < 4 * 60 * 60 * 1000L) { // Less than 4 hours
        insights.add(
            UsageInsight(
                title = "Great Usage Balance!",
                description = "Your screen time is well-balanced today with ${formatDuration(totalUsage)} total usage.",
                impact = "Positive Trend",
                severity = InsightSeverity.POSITIVE,
                recommendation = "Keep maintaining this healthy usage pattern!",
                icon = "✅",
            ),
        )
    }

    return insights
}

fun formatDuration(millis: Long): String {
    val hours = millis / (60 * 60 * 1000)
    val minutes = (millis % (60 * 60 * 1000)) / (60 * 1000)

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

fun getAppIcon(packageName: String): String {
    return when {
        packageName.contains("instagram") -> "📷"
        packageName.contains("whatsapp") -> "💬"
        packageName.contains("youtube") -> "🎬"
        packageName.contains("chrome") -> "🌐"
        packageName.contains("tiktok") -> "🎵"
        packageName.contains("facebook") -> "👥"
        packageName.contains("spotify") -> "🎵"
        packageName.contains("gmail") -> "📧"
        packageName.contains("twitter") -> "🐦"
        packageName.contains("netflix") -> "🎬"
        packageName.contains("maps") -> "🗺️"
        packageName.contains("camera") -> "📷"
        packageName.contains("settings") -> "⚙️"
        packageName.contains("gallery") -> "🖼️"
        packageName.contains("clock") -> "⏰"
        else -> "📱"
    }
}

fun getAppCategory(appName: String): String {
    return when (appName.lowercase()) {
        "instagram", "facebook", "twitter", "tiktok" -> "Social"
        "youtube", "netflix", "spotify" -> "Entertainment"
        "whatsapp", "gmail" -> "Communication"
        "chrome", "maps" -> "Productivity"
        "camera", "gallery" -> "Photo & Video"
        else -> "Other"
    }
}

fun getAppCategoryColor(appName: String): Color {
    return when (getAppCategory(appName)) {
        "Social" -> Color(0xFFE53935)
        "Entertainment" -> Color(0xFF9C27B0)
        "Communication" -> Color(0xFF4CAF50)
        "Productivity" -> Color(0xFF2196F3)
        "Photo & Video" -> Color(0xFF9C27B0)
        else -> Color(0xFF9E9E9E)
    }
}

fun getCategoryColorForName(categoryName: String): Color {
    return when (categoryName) {
        "Social" -> Color(0xFFE53935)
        "Entertainment" -> Color(0xFF9C27B0)
        "Communication" -> Color(0xFF4CAF50)
        "Productivity" -> Color(0xFF2196F3)
        "Photo & Video" -> Color(0xFF9C27B0)
        else -> Color(0xFF9E9E9E)
    }
}

fun getCategoryIcon(categoryName: String): String {
    return when (categoryName) {
        "Social" -> "👥"
        "Communication" -> "💬"
        "Productivity" -> "💼"
        "Photo & Video" -> "📷"
        else -> "📱"
    }
}

@Composable
fun getRankColor(rank: Int): Color {
    return when (rank) {
        1 -> MaterialTheme.extendedColors.premiumGold // Gold
        2 -> MaterialTheme.colorScheme.surfaceVariant // Silver
        3 -> MaterialTheme.colorScheme.secondary // Bronze
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
