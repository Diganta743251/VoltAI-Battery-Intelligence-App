package com.voltai.smartbatteryguardian.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voltai.smartbatteryguardian.analytics.rememberBatteryAnalytics
import com.voltai.smartbatteryguardian.ui.components.EnhancedPremiumGate
import com.voltai.smartbatteryguardian.ui.components.PremiumFeatureBanner
import kotlin.math.*
import java.text.SimpleDateFormat
import java.util.*

data class HealthMetric(
    val name: String,
    val value: String,
    val percentage: Float,
    val status: HealthStatus,
    val icon: String,
    val description: String
)

enum class HealthStatus {
    EXCELLENT, GOOD, FAIR, POOR, CRITICAL
}

data class DegradationData(
    val period: String,
    val degradation: Float,
    val cycleCount: Int,
    val temperature: Float,
    val healthScore: Float
)

data class HealthPrediction(
    val timeframe: String,
    val predictedHealth: Float,
    val confidence: Float,
    val maintenanceDate: Long?,
    val factors: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryHealthAnalyticsScreen(
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val analyticsEngine = rememberBatteryAnalytics(context)
    
    // Premium state management
    var isPremium by remember { mutableStateOf(false) } // TODO: Connect to actual premium state
    var showPremiumGate by remember { mutableStateOf(false) }
    var showPremiumBanner by remember { mutableStateOf(!isPremium) }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🔋 Health", "📊 Analytics", "📈 Trends", "🔮 Predictions", "💡 Tips")
    
    // Generate sample data
    val healthMetrics = remember { generateHealthMetrics() }
    val degradationData = remember { generateDegradationData() }
    val healthPredictions = remember { generateHealthPredictions() }
    val healthTips = remember { generateHealthTips() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFE8F5E8)
                        )
                    )
                )
        ) {
            // Premium Banner
            PremiumFeatureBanner(
                isVisible = showPremiumBanner && !isPremium,
                onUpgradeClick = {
                    showPremiumGate = true
                },
                onDismiss = {
                    showPremiumBanner = false
                },
                title = "🚀 Unlock AI Health Analytics",
                description = "Get advanced battery insights and predictions"
            )
        // Premium Header with Health Score
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF4CAF50),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔋", fontSize = 32.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Health Analytics",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Advanced health monitoring & diagnostics",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    
                    // Overall Health Score Circle
                    OverallHealthScoreIndicator(healthScore = 89f)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Quick Health Status Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickHealthCard("89%", "Health Score", "🎯", Color(0xFF4CAF50))
                    QuickHealthCard("847", "Charge Cycles", "🔄", Color(0xFF2196F3))
                    QuickHealthCard("2.3%", "Degradation", "📉", Color(0xFFFF9800))
                    QuickHealthCard("8 months", "Est. Lifespan", "⏳", Color(0xFF9C27B0))
                }
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF4CAF50),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isPremiumTab = index > 1 // Analytics, Trends, Predictions, Tips are premium
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        if (!isPremium && isPremiumTab) {
                            showPremiumGate = true
                        } else {
                            selectedTab = index
                        }
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isPremium && isPremiumTab) Color.Gray else LocalContentColor.current
                        )
                        if (!isPremium && isPremiumTab) {
                            Text(
                                text = "💎",
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item { HealthOverviewCard(healthMetrics) }
                    item { HealthDetailedMetricsCard() }
                    item { BatteryVitalSignsCard() }
                }
                1 -> {
                    item { HealthScoreAnalyticsCard() }
                    item { ComponentHealthCard() }
                    item { HistoricalHealthChart() }
                }
                2 -> {
                    item { DegradationTrendsCard(degradationData) }
                    item { HealthTrendComparisonCard() }
                    item { CycleCountAnalysisCard() }
                }
                3 -> {
                    item { HealthPredictionsCard(healthPredictions) }
                    item { MaintenanceScheduleCard() }
                    item { LifespanPredictionCard() }
                }
                4 -> {
                    items(healthTips) { tip ->
                        HealthTipCard(tip)
                    }
                }
            }
        }
        } // Close main Column
        
        // Enhanced Premium Gate
        EnhancedPremiumGate(
            isVisible = showPremiumGate && !isPremium,
            onDismiss = {
                showPremiumGate = false
            },
            onUpgradeClick = {
                showPremiumGate = false
                onNavigateToPaywall()
            },
            onLearnMoreClick = {
                showPremiumGate = false
                onNavigateToOnboarding()
            },
            featureName = "Battery Health Analytics",
            featureDescription = "Get deep insights into your battery's health with AI-powered analytics, predictive maintenance alerts, and professional diagnostic tools.",
            featureIcon = "🧠"
        )
    }
}

@Composable
fun OverallHealthScoreIndicator(healthScore: Float) {
    val animatedScore by animateFloatAsState(
        targetValue = healthScore / 100f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic)
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        CircularProgressIndicator(
            progress = animatedScore,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 6.dp,
            strokeCap = StrokeCap.Round,
            color = Color.White.copy(alpha = 0.9f),
            trackColor = Color.White.copy(alpha = 0.3f)
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${healthScore.toInt()}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Health",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun QuickHealthCard(value: String, label: String, icon: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HealthOverviewCard(healthMetrics: List<HealthMetric>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🔋 Health Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            healthMetrics.forEach { metric ->
                HealthMetricRow(metric)
                if (metric != healthMetrics.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun HealthMetricRow(metric: HealthMetric) {
    val statusColor = when (metric.status) {
        HealthStatus.EXCELLENT -> Color(0xFF4CAF50)
        HealthStatus.GOOD -> Color(0xFF8BC34A)
        HealthStatus.FAIR -> Color(0xFFFF9800)
        HealthStatus.POOR -> Color(0xFFFF5722)
        HealthStatus.CRITICAL -> Color(0xFFF44336)
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = metric.icon, fontSize = 20.sp)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = metric.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = metric.description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = metric.value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = metric.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress bar
        LinearProgressIndicator(
            progress = metric.percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = statusColor,
            trackColor = statusColor.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun HealthDetailedMetricsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "📊 Detailed Metrics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DetailedMetricItem("Voltage", "4.12V", "🔌", Color(0xFF2196F3))
                DetailedMetricItem("Temperature", "32.5°C", "🌡️", Color(0xFFFF5722))
                DetailedMetricItem("Current", "-850mA", "⚡", Color(0xFF9C27B0))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DetailedMetricItem("Capacity", "89.3%", "🔋", Color(0xFF4CAF50))
                DetailedMetricItem("Efficiency", "94.2%", "📈", Color(0xFF00BCD4))
                DetailedMetricItem("Age", "18 mo", "📅", Color(0xFF795548))
            }
        }
    }
}

@Composable
fun DetailedMetricItem(label: String, value: String, icon: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 20.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun DegradationTrendsCard(degradationData: List<DegradationData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "📉 Degradation Trends",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            degradationData.forEach { data ->
                DegradationDataRow(data)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DegradationDataRow(data: DegradationData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFF5F5F5),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.period,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${data.cycleCount} cycles",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${data.degradation}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (data.degradation < 2f) Color(0xFF4CAF50) else Color(0xFFFF5722)
            )
            Text(
                text = "Health: ${data.healthScore}%",
                fontSize = 11.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun HealthPredictionsCard(predictions: List<HealthPrediction>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🔮 Health Predictions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            predictions.forEach { prediction ->
                HealthPredictionRow(prediction)
                if (prediction != predictions.last()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun HealthPredictionRow(prediction: HealthPrediction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFF8F9FA),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prediction.timeframe,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2196F3).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${(prediction.confidence * 100).toInt()}% confidence",
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Predicted Health: ${prediction.predictedHealth.toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (prediction.predictedHealth > 70) Color(0xFF4CAF50) else Color(0xFFFF5722)
        )
        
        if (prediction.maintenanceDate != null) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            Text(
                text = "Maintenance suggested: ${dateFormat.format(Date(prediction.maintenanceDate))}",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
        
        prediction.factors.take(2).forEach { factor ->
            Text(
                text = "• $factor",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun HealthTipCard(tip: HealthTip) {
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
                Text(text = tip.icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = tip.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (tip.priority) {
                        "High" -> Color(0xFFE57373).copy(alpha = 0.2f)
                        "Medium" -> Color(0xFFFFB74D).copy(alpha = 0.2f)
                        else -> Color(0xFF81C784).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = tip.priority,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (tip.priority) {
                            "High" -> Color(0xFFD32F2F)
                            "Medium" -> Color(0xFFF57C00)
                            else -> Color(0xFF388E3C)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = tip.description,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )
            
            if (tip.impact.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Impact: ${tip.impact}",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Data generation functions
private fun generateHealthMetrics(): List<HealthMetric> {
    return listOf(
        HealthMetric(
            name = "Overall Health",
            value = "89%",
            percentage = 89f,
            status = HealthStatus.GOOD,
            icon = "🎯",
            description = "Battery is performing well"
        ),
        HealthMetric(
            name = "Capacity Retention",
            value = "91%",
            percentage = 91f,
            status = HealthStatus.GOOD,
            icon = "🔋",
            description = "Slight capacity loss detected"
        ),
        HealthMetric(
            name = "Charging Efficiency",
            value = "94%",
            percentage = 94f,
            status = HealthStatus.EXCELLENT,
            icon = "⚡",
            description = "Charging optimally"
        ),
        HealthMetric(
            name = "Temperature Management",
            value = "85%",
            percentage = 85f,
            status = HealthStatus.GOOD,
            icon = "🌡️",
            description = "Temperature within safe range"
        ),
        HealthMetric(
            name = "Cycle Life Remaining",
            value = "78%",
            percentage = 78f,
            status = HealthStatus.FAIR,
            icon = "🔄",
            description = "847 of 1000 cycles used"
        )
    )
}

private fun generateDegradationData(): List<DegradationData> {
    return listOf(
        DegradationData("Last 30 days", 0.8f, 45, 31.2f, 89.1f),
        DegradationData("Last 90 days", 2.1f, 132, 32.8f, 87.9f),
        DegradationData("Last 6 months", 4.3f, 267, 33.1f, 85.7f),
        DegradationData("Last year", 8.7f, 543, 33.5f, 81.3f)
    )
}

private fun generateHealthPredictions(): List<HealthPrediction> {
    return listOf(
        HealthPrediction(
            timeframe = "Next 3 months",
            predictedHealth = 86.2f,
            confidence = 0.92f,
            maintenanceDate = null,
            factors = listOf("Current usage pattern", "Temperature exposure", "Charging frequency")
        ),
        HealthPrediction(
            timeframe = "Next 6 months",
            predictedHealth = 82.1f,
            confidence = 0.87f,
            maintenanceDate = null,
            factors = listOf("Cycle count increase", "Seasonal temperature changes")
        ),
        HealthPrediction(
            timeframe = "Next year",
            predictedHealth = 76.8f,
            confidence = 0.78f,
            maintenanceDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
            factors = listOf("Expected degradation rate", "Usage intensity", "Environmental factors")
        )
    )
}

data class HealthTip(
    val title: String,
    val description: String,
    val priority: String,
    val icon: String,
    val impact: String
)

private fun generateHealthTips(): List<HealthTip> {
    return listOf(
        HealthTip(
            title = "Optimize Charging Range",
            description = "Keep battery between 20-80% for optimal longevity. Your current pattern shows frequent 100% charges.",
            priority = "High",
            icon = "🔋",
            impact = "Can extend battery life by 12-18 months"
        ),
        HealthTip(
            title = "Temperature Management",
            description = "Your device has exceeded 40°C on 3 occasions this week. Remove case during charging and avoid direct sunlight.",
            priority = "Medium",
            icon = "🌡️",
            impact = "Reduces degradation rate by up to 25%"
        ),
        HealthTip(
            title = "Charging Speed Balance",
            description = "Mix fast and slow charging. Currently using fast charging 78% of the time - try overnight slow charging.",
            priority = "Medium",
            icon = "⚡",
            impact = "Reduces heat stress and improves longevity"
        ),
        HealthTip(
            title = "Storage Calibration",
            description = "Battery hasn't been fully discharged in 2 months. Consider monthly calibration cycle (5% to 100%).",
            priority = "Low",
            icon = "📊",
            impact = "Improves battery level accuracy"
        ),
        HealthTip(
            title = "Background App Management",
            description = "12 apps are consuming battery while unused. Review background app refresh settings.",
            priority = "High",
            icon = "📱",
            impact = "Reduces daily charge cycles by 15-20%"
        )
    )
}

// Placeholder composables for additional sections
@Composable fun BatteryVitalSignsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "💓 Battery Vital Signs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Real-time health monitoring coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable fun HealthScoreAnalyticsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📊 Health Score Analytics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Detailed health score breakdown coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable fun ComponentHealthCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "🔧 Component Health",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Individual component analysis coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable fun HistoricalHealthChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📈 Historical Health Chart",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Historical health trends coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable fun HealthTrendComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📊 Health Trend Comparison",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Comparative health analysis coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable fun CycleCountAnalysisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "🔄 Cycle Count Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Detailed cycle analysis coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable fun MaintenanceScheduleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "🔧 Maintenance Schedule",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Smart maintenance scheduling coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable fun LifespanPredictionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "⏳ Lifespan Prediction",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("AI-powered lifespan prediction coming soon...", color = Color(0xFF666666))
        }
    }
}
