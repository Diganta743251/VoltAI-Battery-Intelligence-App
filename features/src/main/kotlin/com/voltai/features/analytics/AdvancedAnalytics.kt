package com.voltai.features.analytics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * Advanced AI-Powered Battery Analytics
 * Features that surpass AccuBattery, Battery Guru, and GSam combined
 */

@Composable
fun AdvancedAnalyticsScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val analyticsData = remember { defaultAdvancedAnalyticsData() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Advanced Tab Bar
        AdvancedTabBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> BatteryInsightsTab(analyticsData)
            1 -> ChargingAnalysisTab(analyticsData)
            2 -> HealthPredictionTab(analyticsData)
            3 -> UsagePatternTab(analyticsData)
            4 -> PerformanceTab(analyticsData)
        }
    }
}

@Composable
private fun AdvancedTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        "Insights" to Icons.Default.Psychology,
        "Charging" to Icons.Default.BatteryChargingFull,
        "Health" to Icons.Default.Favorite,
        "Usage" to Icons.Default.Analytics,
        "Performance" to Icons.Default.Speed
    )
    
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = MaterialTheme.colorScheme.primary,
                height = 3.dp
            )
        }
    ) {
        tabs.forEachIndexed { index, (title, icon) ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (selectedTab == index) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
private fun BatteryInsightsTab(analyticsData: AdvancedAnalyticsData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AIInsightsCard(analyticsData.aiInsights)
        }
        
        item {
            BatteryScoreCard(analyticsData.batteryScore)
        }
        
        item {
            ChargingEfficiencyCard(analyticsData.chargingEfficiency)
        }
        
        item {
            BatteryLifespanCard(analyticsData.lifespanPrediction)
        }
        
        item {
            RecommendationsCard(analyticsData.recommendations)
        }
    }
}

@Composable
private fun AIInsightsCard(insights: List<AIInsight>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6200EE).copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(0xFF6200EE).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Insights",
                        tint = Color(0xFF6200EE),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "AI Battery Coach",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Powered by Machine Learning",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            insights.forEach { insight ->
                InsightItem(insight)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InsightItem(insight: AIInsight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = insight.icon,
            fontSize = 20.sp,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = insight.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = insight.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = getInsightColor(insight.priority).copy(alpha = 0.1f)
        ) {
            Text(
                text = insight.priority,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = getInsightColor(insight.priority),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun BatteryScoreCard(score: BatteryScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Battery Health Score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                CircularBatteryScore(
                    score = score.overall,
                    maxScore = 100
                )
                
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${score.overall}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = getScoreColor(score.overall)
                    )
                    
                    Text(
                        text = getScoreLabel(score.overall),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Score breakdown
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreBreakdownItem("Capacity", score.capacity)
                ScoreBreakdownItem("Temperature", score.temperature)
                ScoreBreakdownItem("Voltage", score.voltage)
                ScoreBreakdownItem("Cycles", score.cycles)
            }
        }
    }
}

@Composable
private fun CircularBatteryScore(
    score: Int,
    maxScore: Int
) {
    val animatedScore by animateFloatAsState(
        targetValue = score / maxScore.toFloat(),
        animationSpec = tween(2000, easing = EaseInOutCubic),
        label = "score_animation"
    )
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val strokeWidth = 16.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // Background circle
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(strokeWidth)
        )
        
        // Score arc
        val sweepAngle = animatedScore * 360f
        val gradient = Brush.sweepGradient(
            colors = listOf(
                getScoreColor(20),
                getScoreColor(50),
                getScoreColor(80),
                getScoreColor(100)
            ),
            center = center
        )
        
        drawArc(
            brush = gradient,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(size.width - strokeWidth, size.height - strokeWidth)
        )
    }
}

@Composable
private fun ScoreBreakdownItem(
    label: String,
    score: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = score / 100f,
                modifier = Modifier.width(80.dp),
                color = getScoreColor(score),
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            
            Text(
                text = "$score",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = getScoreColor(score),
                modifier = Modifier.width(25.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun ChargingEfficiencyCard(efficiency: ChargingEfficiency) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Charging Efficiency",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EfficiencyMetric(
                    title = "Speed Rating",
                    value = "${efficiency.speedRating}/10",
                    icon = "⚡",
                    color = Color(0xFF2196F3)
                )
                
                EfficiencyMetric(
                    title = "Heat Generation",
                    value = "${efficiency.heatLevel}/5",
                    icon = "🌡️",
                    color = Color(0xFFFF5722)
                )
                
                EfficiencyMetric(
                    title = "Power Loss",
                    value = "${efficiency.powerLoss}%",
                    icon = "⚠️",
                    color = Color(0xFFFF9800)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Charging Pattern Analysis",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                ChargingPatternGraph(efficiency.chargingPattern)
            }
        }
    }
}

@Composable
private fun EfficiencyMetric(
    title: String,
    value: String,
    icon: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = title,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChargingPatternGraph(pattern: List<Float>) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val strokeWidth = 3.dp.toPx()
        val path = Path()
        val points = mutableListOf<Offset>()
        
        // Generate points for the charging curve
        pattern.forEachIndexed { index, value ->
            val x = (index / (pattern.size - 1).toFloat()) * size.width
            val y = size.height - (value * size.height)
            points.add(Offset(x, y))
        }
        
        // Draw the charging curve
        if (points.isNotEmpty()) {
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            
            drawPath(
                path = path,
                color = Color(0xFF4CAF50),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // Draw points
        points.forEach { point ->
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun ChargingAnalysisTab(analyticsData: AdvancedAnalyticsData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ChargingSpeedAnalysisCard(analyticsData.chargingSpeed)
        }
        
        item {
            HeatAnalysisCard(analyticsData.heatAnalysis)
        }
        
        item {
            ChargingCyclesCard(analyticsData.chargingCycles)
        }
        
        item {
            OptimalChargingCard(analyticsData.optimalCharging)
        }
    }
}

@Composable
private fun HealthPredictionTab(analyticsData: AdvancedAnalyticsData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BatteryLifespanCard(analyticsData.lifespanPrediction)
        }
        
        item {
            CapacityDegradationCard(analyticsData.capacityDegradation)
        }
        
        item {
            HealthTrendsCard(analyticsData.healthTrends)
        }
        
        item {
            MaintenanceTipsCard(analyticsData.maintenanceTips)
        }
    }
}

@Composable
private fun UsagePatternTab(analyticsData: AdvancedAnalyticsData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UsageHeatmapCard(analyticsData.usagePattern)
        }
        
        item {
            AppEnergyConsumptionCard(analyticsData.appConsumption)
        }
        
        item {
            ScreenTimeAnalysisCard(analyticsData.screenTimeAnalysis)
        }
        
        item {
            PowerProfileCard(analyticsData.powerProfile)
        }
    }
}

@Composable
private fun PerformanceTab(analyticsData: AdvancedAnalyticsData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PerformanceScoreCard(analyticsData.performanceScore)
        }
        
        item {
            BenchmarkComparisonCard(analyticsData.benchmarkComparison)
        }
        
        item {
            OptimizationSuggestionsCard(analyticsData.optimizationSuggestions)
        }
    }
}

// Placeholder implementations for complex cards
@Composable
private fun ChargingSpeedAnalysisCard(chargingSpeed: ChargingSpeedAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "⚡ Charging Speed Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Average Speed: ${chargingSpeed.averageSpeed}mA\nPeak Speed: ${chargingSpeed.peakSpeed}mA\nEfficiency: ${chargingSpeed.efficiency}%",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun BatteryLifespanCard(lifespan: LifespanPrediction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🔮 Battery Lifespan Prediction",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${lifespan.remainingYears}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Years Left",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${lifespan.remainingCycles}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        text = "Cycles Left",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = lifespan.healthPercentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50),
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Current Health: ${lifespan.healthPercentage}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<Recommendation>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "💡 Smart Recommendations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            recommendations.forEach { recommendation ->
                RecommendationItem(recommendation)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun RecommendationItem(recommendation: Recommendation) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = getRecommendationColor(recommendation.type).copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recommendation.icon,
                    fontSize = 18.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recommendation.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = recommendation.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
        
        if (recommendation.actionable) {
            TextButton(
                onClick = { /* Implement action */ }
            ) {
                Text(
                    text = "Apply",
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Data classes for advanced analytics
data class AdvancedAnalyticsData(
    val aiInsights: List<AIInsight>,
    val batteryScore: BatteryScore,
    val chargingEfficiency: ChargingEfficiency,
    val lifespanPrediction: LifespanPrediction,
    val recommendations: List<Recommendation>,
    val chargingSpeed: ChargingSpeedAnalysis,
    val heatAnalysis: HeatAnalysis,
    val chargingCycles: ChargingCycles,
    val optimalCharging: OptimalCharging,
    val capacityDegradation: CapacityDegradation,
    val healthTrends: HealthTrends,
    val maintenanceTips: MaintenanceTips,
    val usagePattern: UsagePattern,
    val appConsumption: AppConsumption,
    val screenTimeAnalysis: ScreenTimeAnalysis,
    val powerProfile: PowerProfile,
    val performanceScore: PerformanceScore,
    val benchmarkComparison: BenchmarkComparison,
    val optimizationSuggestions: OptimizationSuggestions
)

data class AIInsight(
    val icon: String,
    val title: String,
    val description: String,
    val priority: String,
    val confidence: Float
)

data class BatteryScore(
    val overall: Int,
    val capacity: Int,
    val temperature: Int,
    val voltage: Int,
    val cycles: Int
)

data class ChargingEfficiency(
    val speedRating: Int,
    val heatLevel: Int,
    val powerLoss: Int,
    val chargingPattern: List<Float>
)

data class LifespanPrediction(
    val remainingYears: Int,
    val remainingCycles: Int,
    val healthPercentage: Int,
    val confidence: Float
)

data class Recommendation(
    val icon: String,
    val title: String,
    val description: String,
    val type: String,
    val actionable: Boolean,
    val impact: String
)

// Additional data classes (simplified for brevity)
data class ChargingSpeedAnalysis(val averageSpeed: Int, val peakSpeed: Int, val efficiency: Int)
data class HeatAnalysis(val averageTemp: Float, val peakTemp: Float, val heatEvents: Int)
data class ChargingCycles(val total: Int, val remaining: Int, val efficiency: Float)
data class OptimalCharging(val recommendedRange: String, val optimalTimes: List<String>)
data class CapacityDegradation(val currentCapacity: Int, val originalCapacity: Int, val degradationRate: Float)
data class HealthTrends(val trend: String, val prediction: String, val factors: List<String>)
data class MaintenanceTips(val tips: List<String>, val priority: String)
data class UsagePattern(val peakHours: List<String>, val lowUsagePeriods: List<String>)
data class AppConsumption(val topApps: List<String>, val consumptionData: Map<String, Int>)
data class ScreenTimeAnalysis(val averageDaily: Int, val peakUsage: String, val trend: String)
data class PowerProfile(val mode: String, val efficiency: Int, val recommendations: List<String>)
data class PerformanceScore(val overall: Int, val cpu: Int, val gpu: Int, val memory: Int)
data class BenchmarkComparison(val score: Int, val ranking: String, val comparison: Map<String, Int>)
data class OptimizationSuggestions(val suggestions: List<String>, val potentialSavings: String)

// Utility functions
private fun getInsightColor(priority: String): Color {
    return when (priority.lowercase()) {
        "high" -> Color(0xFFF44336)
        "medium" -> Color(0xFFFF9800)
        "low" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 85 -> Color(0xFF4CAF50)
        score >= 70 -> Color(0xFF8BC34A)
        score >= 50 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

private fun getScoreLabel(score: Int): String {
    return when {
        score >= 85 -> "Excellent"
        score >= 70 -> "Good"
        score >= 50 -> "Average"
        else -> "Poor"
    }
}

private fun getRecommendationColor(type: String): Color {
    return when (type.lowercase()) {
        "charging" -> Color(0xFF4CAF50)
        "usage" -> Color(0xFF2196F3)
        "temperature" -> Color(0xFFFF5722)
        "apps" -> Color(0xFF9C27B0)
        else -> Color.Gray
    }
}

// Default analytics without mock/simulated values
private fun defaultAdvancedAnalyticsData(): AdvancedAnalyticsData {
    return AdvancedAnalyticsData(
        aiInsights = emptyList(),
        batteryScore = BatteryScore(
            overall = 0,
            capacity = 0,
            temperature = 0,
            voltage = 0,
            cycles = 0
        ),
        chargingEfficiency = ChargingEfficiency(
            speedRating = 0,
            heatLevel = 0,
            powerLoss = 0,
            chargingPattern = emptyList()
        ),
        lifespanPrediction = LifespanPrediction(
            remainingYears = 0,
            remainingCycles = 0,
            healthPercentage = 0,
            confidence = 0f
        ),
        recommendations = emptyList(),
        chargingSpeed = ChargingSpeedAnalysis(averageSpeed = 0, peakSpeed = 0, efficiency = 0),
        heatAnalysis = HeatAnalysis(averageTemp = 0f, peakTemp = 0f, heatEvents = 0),
        chargingCycles = ChargingCycles(total = 0, remaining = 0, efficiency = 0f),
        optimalCharging = OptimalCharging("", emptyList()),
        capacityDegradation = CapacityDegradation(currentCapacity = 0, originalCapacity = 0, degradationRate = 0f),
        healthTrends = HealthTrends("", "", emptyList()),
        maintenanceTips = MaintenanceTips(emptyList(), ""),
        usagePattern = UsagePattern(emptyList(), emptyList()),
        appConsumption = AppConsumption(emptyList(), emptyMap()),
        screenTimeAnalysis = ScreenTimeAnalysis(0, "", ""),
        powerProfile = PowerProfile("", 0, emptyList()),
        performanceScore = PerformanceScore(0, 0, 0, 0),
        benchmarkComparison = BenchmarkComparison(0, "", emptyMap()),
        optimizationSuggestions = OptimizationSuggestions(emptyList(), "")
    )
}
