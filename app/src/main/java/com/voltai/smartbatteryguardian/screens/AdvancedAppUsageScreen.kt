package com.voltai.smartbatteryguardian.screens

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.voltai.smartbatteryguardian.models.AppUsageData

data class AppCategory(
    val name: String,
    val apps: List<AppUsageData>,
    val totalUsage: Long,
    val batteryImpact: Double,
    val color: Color
)

data class UsageInsight(
    val title: String,
    val description: String,
    val impact: String,
    val severity: InsightSeverity,
    val recommendation: String,
    val icon: String
)

enum class InsightSeverity { HIGH, MEDIUM, LOW, POSITIVE }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AdvancedAppUsageScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableStateOf(0) }
    var selectedPeriod by remember { mutableStateOf(0) }
    var appsData by remember { mutableStateOf<List<AppUsageData>>(emptyList()) }
    var categories by remember { mutableStateOf<List<AppCategory>>(emptyList()) }
    var insights by remember { mutableStateOf<List<UsageInsight>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val tabs = listOf("📊 Overview", "📱 Apps", "📂 Categories", "💡 Insights", "📈 Analytics")
    val periods = listOf("Today", "Yesterday", "Week", "Month")
    
    // Load usage data
    LaunchedEffect(selectedPeriod) {
        scope.launch {
            isLoading = true
            val data = loadAppUsageData(context, selectedPeriod)
            appsData = data
            categories = categorizeApps(data)
            insights = generateUsageInsights(data)
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE3F2FD)
                    )
                )
            )
    ) {
        // Premium Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1976D2),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 24.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "App Usage Analytics",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Advanced battery consumption insights",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Period Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periods.forEachIndexed { index, period ->
                        FilterChip(
                            selected = selectedPeriod == index,
                            onClick = { selectedPeriod = index },
                            label = {
                                Text(
                                    text = period,
                                    color = if (selectedPeriod == index) Color(0xFF1976D2) else Color.White
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF1976D2),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        
        // Content with Loading State
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing app usage patterns...",
                        color = Color(0xFF666666)
                    )
                }
            }
        } else {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { if (targetState > initialState) it else -it },
                        animationSpec = tween(300)
                    ) + fadeIn() togetherWith slideOutHorizontally(
                        targetOffsetX = { if (targetState > initialState) -it else it },
                        animationSpec = tween(300)
                    ) + fadeOut()
                }
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
fun OverviewTab(appsData: List<AppUsageData>, categories: List<AppCategory>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewStatsCard(
                    title = "Total Screen Time",
                    value = formatDuration(appsData.sumOf { it.totalTimeInForeground }),
                    icon = "📱",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                OverviewStatsCard(
                    title = "Battery Impact",
                    value = "${appsData.sumOf { it.batteryUsage }.toInt()}%",
                    icon = "🔋",
                    color = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewStatsCard(
                    title = "Apps Opened",
                    value = "${appsData.sumOf { it.openCount }}",
                    icon = "🚀",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                OverviewStatsCard(
                    title = "Avg Session",
                    value = formatDuration(appsData.map { it.avgSessionTime }.average().toLong()),
                    icon = "⏱️",
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Usage Distribution Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📊 Usage Distribution",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Donut Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 Top Apps",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "⏰ Hourly Usage Pattern",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HourlyUsageChart()
                }
            }
        }
    }
}

@Composable
fun AppsTab(appsData: List<AppUsageData>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(appsData) { app ->
            DetailedAppUsageCard(app)
        }
    }
}

@Composable
fun CategoriesTab(categories: List<AppCategory>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            CategoryCard(category)
        }
    }
}

@Composable
fun InsightsTab(insights: List<UsageInsight>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(insights) { insight ->
            InsightCard(insight)
        }
    }
}

@Composable
fun AnalyticsTab(appsData: List<AppUsageData>, categories: List<AppCategory>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trend Analysis
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📈 Usage Trends",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    WeeklyTrendChart()
                }
            }
        }
        
        // Battery Impact Analysis
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "🔋 Battery Impact Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = icon,
                    fontSize = 16.sp
                )
            }
            
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun UsageDonutChart(categories: List<AppCategory>) {
    Canvas(
        modifier = Modifier
            .size(150.dp)
            .padding(16.dp)
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
                topLeft = androidx.compose.ui.geometry.Offset(
                    centerX - radius,
                    centerY - radius
                )
            )
            
            startAngle += sweepAngle
        }
    }
}

@Composable
fun TopAppItem(app: AppUsageData, rank: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = getRankColor(rank)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = app.appName,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatDuration(app.totalTimeInForeground),
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${app.batteryUsage.toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFFF5722)
            )
            Text(
                text = "battery",
                fontSize = 10.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
fun DetailedAppUsageCard(app: AppUsageData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = app.categoryColor.copy(alpha = 0.1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getAppIcon(app.packageName),
                            fontSize = 24.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = app.appName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Opened ${app.openCount} times",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatDuration(app.totalTimeInForeground),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = app.categoryColor
                    )
                    Text(
                        text = "${app.batteryUsage.toInt()}% battery",
                        fontSize = 12.sp,
                        color = Color(0xFFFF5722)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Usage breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UsageMetricItem("Today", formatDuration(app.usageToday))
                UsageMetricItem("Yesterday", formatDuration(app.usageYesterday))
                UsageMetricItem("Avg Session", formatDuration(app.avgSessionTime))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Usage bar
            LinearProgressIndicator(
                progress = (app.totalTimeInForeground.toFloat() / (8 * 60 * 60 * 1000L)).coerceAtMost(1f), // Max 8 hours
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = app.categoryColor,
                trackColor = app.categoryColor.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun CategoryCard(category: AppCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(category.color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCategoryIcon(category.name),
                        fontSize = 24.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${category.apps.size} apps • ${formatDuration(category.totalUsage)}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                Text(
                    text = "${category.batteryImpact.toInt()}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = category.color
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Top apps in category
            category.apps.take(3).forEach { app ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.appName,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatDuration(app.totalTimeInForeground),
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
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
        colors = CardDefaults.cardColors(
            containerColor = when (insight.severity) {
                InsightSeverity.HIGH -> Color(0xFFFFEBEE)
                InsightSeverity.MEDIUM -> Color(0xFFFFF8E1)
                InsightSeverity.LOW -> Color(0xFFF3E5F5)
                InsightSeverity.POSITIVE -> Color(0xFFE8F5E8)
            }
        ),
        border = BorderStroke(
            1.dp, when (insight.severity) {
                InsightSeverity.HIGH -> Color(0xFFFF5722)
                InsightSeverity.MEDIUM -> Color(0xFFFF9800)
                InsightSeverity.LOW -> Color(0xFF9C27B0)
                InsightSeverity.POSITIVE -> Color(0xFF4CAF50)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = insight.icon,
                    fontSize = 24.sp
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = insight.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = insight.impact,
                        fontSize = 12.sp,
                        color = when (insight.severity) {
                            InsightSeverity.HIGH -> Color(0xFFFF5722)
                            InsightSeverity.MEDIUM -> Color(0xFFFF9800)
                            InsightSeverity.LOW -> Color(0xFF9C27B0)
                            InsightSeverity.POSITIVE -> Color(0xFF4CAF50)
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = insight.description,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF333333)
            )
            
            if (insight.recommendation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 ${insight.recommendation}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
fun HourlyUsageChart() {
    // Simulated hourly data
    val hourlyData = remember {
        (0..23).map { hour ->
            val baseUsage = when (hour) {
                in 0..6 -> Random.nextInt(5, 15) // Night
                in 7..9 -> Random.nextInt(30, 60) // Morning
                in 10..12 -> Random.nextInt(20, 40) // Mid-morning
                in 13..15 -> Random.nextInt(25, 45) // Afternoon
                in 16..18 -> Random.nextInt(40, 70) // Evening
                in 19..22 -> Random.nextInt(50, 80) // Night
                else -> Random.nextInt(10, 25) // Late night
            }
            baseUsage
        }
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val barWidth = size.width / 24
        val maxUsage = hourlyData.maxOrNull() ?: 1
        
        hourlyData.forEachIndexed { index, usage ->
            val barHeight = (usage.toFloat() / maxUsage) * size.height * 0.8f
            val x = index * barWidth + barWidth * 0.2f
            val y = size.height - barHeight
            
            drawRoundRect(
                color = Color(0xFF2196F3).copy(alpha = 0.8f),
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.6f, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )
        }
    }
    
    // Hour labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("12AM", "6AM", "12PM", "6PM", "12AM").forEach { label ->
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun WeeklyTrendChart() {
    val weeklyData = remember {
        (0..6).map { Random.nextInt(2, 8) } // Hours per day
    }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val stepX = size.width / (weeklyData.size - 1)
        val maxHours = 8f
        
        val points = weeklyData.mapIndexed { index, hours ->
            androidx.compose.ui.geometry.Offset(
                x = index * stepX,
                y = size.height - (hours.toFloat() / maxHours) * size.height
            )
        }
        
        // Draw line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFF4CAF50),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        // Draw points
        points.forEach { point ->
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            Text(
                text = day,
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun BatteryImpactItem(app: AppUsageData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = getAppIcon(app.packageName),
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = app.appName,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        LinearProgressIndicator(
            progress = (app.batteryUsage / 100).toFloat().coerceIn(0f, 1f),
            modifier = Modifier
                .width(80.dp)
                .height(4.dp),
            color = when {
                app.batteryUsage > 20 -> Color(0xFFFF5722)
                app.batteryUsage > 10 -> Color(0xFFFF9800)
                else -> Color(0xFF4CAF50)
            },
            trackColor = Color(0xFFE0E0E0)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "${app.batteryUsage.toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun UsageMetricItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
    }
}

// Utility Functions
suspend fun loadAppUsageData(context: Context, period: Int): List<AppUsageData> {
    return withContext(Dispatchers.IO) {
        // Mock data - In real implementation, use UsageStatsManager
        generateMockAppUsageData()
    }
}

fun generateMockAppUsageData(): List<AppUsageData> {
    val mockApps = listOf(
        "Instagram" to "\uD83D\uDCF7",
        "WhatsApp" to "\uD83D\uDCAC",
        "YouTube" to "\uD83C\uDFAC",
        "Chrome" to "\uD83C\uDF10",
        "TikTok" to "\uD83C\uDFB5",
        "Facebook" to "\uD83D\uDC65",
        "Spotify" to "\uD83C\uDFB5",
        "Gmail" to "\uD83D\uDCE7",
        "Twitter" to "\uD83D\uDC26",
        "Netflix" to "\uD83C\uDFAC",
        "Maps" to "\uD83D\uDDFA",
        "Camera" to "\uD83D\uDCF7",
        "Settings" to "⚙️",
        "Gallery" to "\uD83D\uDDBC",
        "Clock" to "⏰"
    )
    
    return mockApps.mapIndexed { index, (name, icon) ->
        val baseUsage = Random.nextLong(30 * 60 * 1000L, 4 * 60 * 60 * 1000L) // 30min to 4hours
        AppUsageData(
            packageName = "com.${name.lowercase()}.app",
            appName = name,
            icon = null,
            totalTimeInForeground = baseUsage,
            batteryUsage = Random.nextDouble(1.0, 25.0),
            lastTimeUsed = System.currentTimeMillis() - Random.nextLong(60 * 60 * 1000L),
            usageToday = baseUsage,
            usageYesterday = Random.nextLong(20 * 60 * 1000L, 3 * 60 * 60 * 1000L),
            usageThisWeek = baseUsage * Random.nextLong(3, 8),
            openCount = Random.nextInt(5, 50),
            avgSessionTime = Random.nextLong(2 * 60 * 1000L, 30 * 60 * 1000L),
            peakUsageHour = Random.nextInt(0, 24),
            categoryColor = getAppCategoryColor(name)
        )
    }.sortedByDescending { it.totalTimeInForeground }
}

fun categorizeApps(apps: List<AppUsageData>): List<AppCategory> {
    val categories = apps.groupBy { getAppCategory(it.appName) }
    
    return categories.map { (categoryName, categoryApps) ->
        AppCategory(
            name = categoryName,
            apps = categoryApps,
            totalUsage = categoryApps.sumOf { it.totalTimeInForeground },
            batteryImpact = categoryApps.sumOf { it.batteryUsage },
            color = getCategoryColorForName(categoryName)
        )
    }.sortedByDescending { it.totalUsage }
}

fun generateUsageInsights(apps: List<AppUsageData>): List<UsageInsight> {
    val insights = mutableListOf<UsageInsight>()
    
    // High usage app insight
    val topApp = apps.firstOrNull()
    if (topApp != null && topApp.totalTimeInForeground > 3 * 60 * 60 * 1000L) { // More than 3 hours
        insights.add(
            UsageInsight(
                title = "High Usage Detected",
                description = "${topApp.appName} consumed ${formatDuration(topApp.totalTimeInForeground)} of screen time today.",
                impact = "High Battery Impact",
                severity = InsightSeverity.HIGH,
                recommendation = "Consider setting app limits or taking regular breaks.",
                icon = "⚠️"
            )
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
                icon = "🔋"
            )
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
                icon = "✅"
            )
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
        "Social" -> Color(0xFFE91E63)
        "Entertainment" -> Color(0xFF9C27B0)
        "Communication" -> Color(0xFF4CAF50)
        "Productivity" -> Color(0xFF2196F3)
        "Photo & Video" -> Color(0xFFFF9800)
        else -> Color(0xFF607D8B)
    }
}

fun getCategoryColorForName(categoryName: String): Color {
    return when (categoryName) {
        "Social" -> Color(0xFFE91E63)
        "Entertainment" -> Color(0xFF9C27B0)
        "Communication" -> Color(0xFF4CAF50)
        "Productivity" -> Color(0xFF2196F3)
        "Photo & Video" -> Color(0xFFFF9800)
        else -> Color(0xFF607D8B)
    }
}

fun getCategoryIcon(categoryName: String): String {
    return when (categoryName) {
        "Social" -> "👥"
        "Entertainment" -> "🎬"
        "Communication" -> "💬"
        "Productivity" -> "💼"
        "Photo & Video" -> "📷"
        else -> "📱"
    }
}

fun getRankColor(rank: Int): Color {
    return when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color(0xFF666666)
    }
}
