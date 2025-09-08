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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import java.text.SimpleDateFormat
import java.util.*
import com.voltai.smartbatteryguardian.models.RecommendationPriority
import com.voltai.smartbatteryguardian.models.ChargingProfile

// Local Data Models for Smart Charging
data class ChargingRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val category: ChargingCategory,
    val impact: String,
    val actionText: String,
    val icon: String,
    val estimatedBenefit: String,
    val confidence: Float
)

enum class ChargingCategory {
    TIMING, SPEED, LEVEL, TEMPERATURE, EFFICIENCY
}

// Local ChargingProfile for this screen
data class LocalChargingProfile(
    val name: String,
    val icon: String,
    val description: String,
    val targetRange: IntRange,
    val chargingSpeed: ChargingSpeed,
    val timeWindows: List<String>,
    val benefits: List<String>,
    val isRecommended: Boolean
)

enum class ChargingSpeed {
    SLOW, NORMAL, FAST, ULTRA_FAST
}

data class ChargingInsight(
    val title: String,
    val value: String,
    val change: String,
    val changeType: ChangeType,
    val icon: String,
    val description: String
)

enum class ChangeType {
    POSITIVE, NEGATIVE, NEUTRAL
}

data class OptimizationTip(
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val timeToImplement: String,
    val energySavings: String,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartChargingScreen() {
    val context = LocalContext.current
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🧠 Smart Charging", "📊 Insights", "⚙️ Profiles", "🔧 Optimization", "📈 Analytics")
    
    // Generate sample data
    val recommendations = remember { generateChargingRecommendations() }
    val profiles = remember { generateLocalChargingProfiles() }
    val insights = remember { generateChargingInsights() }
    val optimizationTips = remember { generateOptimizationTips() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color(0xFFE8F5E8)
                    )
                )
            )
    ) {
        // Premium Header with AI indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF6A1B9A),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI Brain Animation
                    AIBrainIndicator()
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Charging AI",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Intelligent charging optimization & recommendations",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    
                    // AI Status Indicator
                    AIStatusCard()
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Quick AI Insights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickInsightCard("94%", "Efficiency", "⚡", Color(0xFF4CAF50))
                    QuickInsightCard("3.2h", "Optimal Window", "⏰", Color(0xFF2196F3))
                    QuickInsightCard("15%", "Energy Saved", "💚", Color(0xFF8BC34A))
                    QuickInsightCard("87%", "AI Confidence", "🎯", Color(0xFFFF9800))
                }
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF6A1B9A),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                    )
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
                    item { CurrentChargingStatusCard() }
                    item { SmartRecommendationsCard(recommendations.take(3)) }
                    item { ChargingScheduleCard() }
                }
                1 -> {
                    items(insights) { insight ->
                        ChargingInsightCard(insight)
                    }
                }
                2 -> {
                    items(profiles) { profile ->
                        ChargingProfileCard(profile)
                    }
                }
                3 -> {
                    items(optimizationTips) { tip ->
                        OptimizationTipCard(tip)
                    }
                }
                4 -> {
                    item { ChargingAnalyticsOverview() }
                    item { EfficiencyTrendsCard() }
                    item { PowerConsumptionAnalysis() }
                }
            }
        }
    }
}

@Composable
fun AIBrainIndicator() {
    val rotation by animateFloatAsState(
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
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
        Text(
            text = "🧠",
            fontSize = 32.sp,
            modifier = Modifier.graphicsLayer { rotationZ = rotation / 8f }
        )
        
        // Pulsing ring
        val pulseScale by animateFloatAsState(
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer { 
                    scaleX = pulseScale 
                    scaleY = pulseScale
                    alpha = 1f - ((pulseScale - 1f) / 0.2f) * 0.5f
                }
                .border(
                    2.dp, 
                    Color.White.copy(alpha = 0.4f), 
                    CircleShape
                )
        )
    }
}

@Composable
fun AIStatusCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Active",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "Learning...",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun QuickInsightCard(value: String, label: String, icon: String, color: Color) {
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
fun CurrentChargingStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ Current Charging Status",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A148C)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Charging",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Battery level and charging animation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated battery
                AnimatedBatteryIcon(level = 67, isCharging = true)
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column {
                    Text(
                        text = "67%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "23 minutes to 80%",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = 0.67f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Charging details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ChargingDetailItem("25W", "Power", "⚡")
                ChargingDetailItem("32.1°C", "Temperature", "🌡️")
                ChargingDetailItem("USB-C", "Type", "🔌")
                ChargingDetailItem("94%", "Efficiency", "📊")
            }
        }
    }
}

@Composable
fun ChargingDetailItem(value: String, label: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun AnimatedBatteryIcon(level: Int, isCharging: Boolean) {
    val animatedLevel by animateFloatAsState(
        targetValue = level / 100f,
        animationSpec = tween(1000, easing = EaseOutCubic)
    )
    
    Box(
        modifier = Modifier.size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        // Battery outline
        Canvas(modifier = Modifier.fillMaxSize()) { 
            val strokeWidth = 3.dp.toPx()
            val cornerRadius = 6.dp.toPx()
            
            // Battery body
            drawRoundRect(
                color = Color(0xFF666666),
                size = androidx.compose.ui.geometry.Size(
                    width = size.width * 0.8f,
                    height = size.height * 0.6f
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = size.width * 0.1f,
                    y = size.height * 0.2f
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
            
            // Battery tip
            drawRoundRect(
                color = Color(0xFF666666),
                size = androidx.compose.ui.geometry.Size(
                    width = size.width * 0.1f,
                    height = size.height * 0.2f
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = size.width * 0.9f,
                    y = size.height * 0.4f
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius / 2, cornerRadius / 2)
            )
            
            // Battery level fill
            val fillColor = when {
                level > 80 -> Color(0xFF4CAF50)
                level > 50 -> Color(0xFFFF9800)
                level > 20 -> Color(0xFFFF5722)
                else -> Color(0xFFF44336)
            }
            
            drawRoundRect(
                color = fillColor,
                size = androidx.compose.ui.geometry.Size(
                    width = (size.width * 0.8f - strokeWidth * 2) * animatedLevel,
                    height = size.height * 0.6f - strokeWidth * 2
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = size.width * 0.1f + strokeWidth,
                    y = size.height * 0.2f + strokeWidth
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius - strokeWidth, cornerRadius - strokeWidth)
            )
        }
        
        // Charging bolt animation
        if (isCharging) {
            val boltAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Text(
                text = "⚡",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = boltAlpha),
                modifier = Modifier.graphicsLayer { alpha = boltAlpha }
            )
        }
    }
}

@Composable
fun SmartRecommendationsCard(recommendations: List<ChargingRecommendation>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🧠 AI Recommendations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "SMART",
                        fontSize = 10.sp,
                        color = Color(0xFF6A1B9A),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            recommendations.forEach { recommendation ->
                RecommendationCard(recommendation)
                if (recommendation != recommendations.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendation: ChargingRecommendation) {
    val priorityColor = when (recommendation.priority) {
        RecommendationPriority.URGENT -> Color(0xFFF44336)
        RecommendationPriority.HIGH -> Color(0xFFFF5722)
        RecommendationPriority.MEDIUM -> Color(0xFFFF9800)
        RecommendationPriority.LOW -> Color(0xFF4CAF50)
        RecommendationPriority.INFO -> Color(0xFF2196F3)
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = recommendation.icon, fontSize = 20.sp)
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recommendation.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = recommendation.description,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = priorityColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = recommendation.priority.name,
                        fontSize = 10.sp,
                        color = priorityColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "💡 ${recommendation.impact}",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Confidence: ${(recommendation.confidence * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                OutlinedButton(
                    onClick = { /* Handle action */ },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = recommendation.actionText,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChargingInsightCard(insight: ChargingInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = insight.icon, fontSize = 24.sp)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = insight.description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    lineHeight = 16.sp
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = insight.value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                val changeColor = when (insight.changeType) {
                    ChangeType.POSITIVE -> Color(0xFF4CAF50)
                    ChangeType.NEGATIVE -> Color(0xFFF44336)
                    ChangeType.NEUTRAL -> Color(0xFF666666)
                }
                
                Text(
                    text = insight.change,
                    fontSize = 12.sp,
                    color = changeColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ChargingProfileCard(profile: LocalChargingProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.isRecommended) 
                Color(0xFF6A1B9A).copy(alpha = 0.05f) 
            else Color.White
        ),
        border = if (profile.isRecommended) 
            BorderStroke(2.dp, Color(0xFF6A1B9A).copy(alpha = 0.3f)) 
        else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = profile.icon, fontSize = 24.sp)
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profile.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (profile.isRecommended) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF6A1B9A)
                            ) {
                                Text(
                                    text = "RECOMMENDED",
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = profile.description,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
                
                Switch(
                    checked = profile.isRecommended,
                    onCheckedChange = { /* Handle toggle */ },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF6A1B9A),
                        checkedTrackColor = Color(0xFF6A1B9A).copy(alpha = 0.3f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Profile details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileDetailItem("Range", "${profile.targetRange.first}-${profile.targetRange.last}%")
                ProfileDetailItem("Speed", profile.chargingSpeed.name)
                ProfileDetailItem("Windows", "${profile.timeWindows.size}")
            }
            
            if (profile.benefits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                profile.benefits.take(2).forEach { benefit ->
                    Text(
                        text = "• $benefit",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun OptimizationTipCard(tip: OptimizationTip) {
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
                    color = when (tip.difficulty) {
                        "Easy" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        "Medium" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        else -> Color(0xFFF44336).copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = tip.difficulty,
                        fontSize = 10.sp,
                        color = when (tip.difficulty) {
                            "Easy" -> Color(0xFF4CAF50)
                            "Medium" -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        },
                        fontWeight = FontWeight.Medium,
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏱️ ${tip.timeToImplement}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "💚 ${tip.energySavings} savings",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Additional placeholder cards
@Composable
fun ChargingScheduleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📅 Smart Charging Schedule",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("AI-powered charging schedule coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable
fun ChargingAnalyticsOverview() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📊 Charging Analytics Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Detailed charging analytics coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable
fun EfficiencyTrendsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📈 Efficiency Trends",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Efficiency trend analysis coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable
fun PowerConsumptionAnalysis() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "⚡ Power Consumption Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Power consumption insights coming soon...", color = Color(0xFF666666))
        }
    }
}

// Data generation functions
private fun generateChargingRecommendations(): List<ChargingRecommendation> {
    return listOf(
        ChargingRecommendation(
            id = "rec1",
            title = "Enable Adaptive Charging",
            description = "Based on your routine, enabling adaptive charging can extend battery life by up to 18 months.",
            priority = RecommendationPriority.HIGH,
            category = ChargingCategory.TIMING,
            impact = "Extends battery lifespan",
            actionText = "Enable",
            icon = "🧠",
            estimatedBenefit = "18 months longer life",
            confidence = 0.94f
        ),
        ChargingRecommendation(
            id = "rec2",
            title = "Charge to 85% Tonight",
            description = "Your pattern shows you typically need 75% by morning. Charging to 85% instead of 100% will improve longevity.",
            priority = RecommendationPriority.MEDIUM,
            category = ChargingCategory.LEVEL,
            impact = "Reduces degradation",
            actionText = "Set Limit",
            icon = "🎯",
            estimatedBenefit = "15% less degradation",
            confidence = 0.89f
        ),
        ChargingRecommendation(
            id = "rec3",
            title = "Use Slow Charging",
            description = "Device temperature has been elevated during fast charging. Consider slow charging when time permits.",
            priority = RecommendationPriority.MEDIUM,
            category = ChargingCategory.SPEED,
            impact = "Lower temperature stress",
            actionText = "Switch Mode",
            icon = "🌡️",
            estimatedBenefit = "25% cooler charging",
            confidence = 0.92f
        ),
        ChargingRecommendation(
            id = "rec4",
            title = "Optimal Charging Window",
            description = "Your electricity rates are lowest between 10 PM - 6 AM. Schedule charging during this period.",
            priority = RecommendationPriority.LOW,
            category = ChargingCategory.TIMING,
            impact = "Cost savings",
            actionText = "Schedule",
            icon = "💰",
            estimatedBenefit = "30% cost reduction",
            confidence = 0.87f
        )
    )
}

private fun generateLocalChargingProfiles(): List<LocalChargingProfile> {
    return listOf(
        LocalChargingProfile(
            name = "Gentle Care",
            icon = "🍃",
            description = "Maximizes battery health with slow, controlled charging",
            targetRange = 20..80,
            chargingSpeed = ChargingSpeed.SLOW,
            timeWindows = listOf("22:00-06:00", "12:00-14:00"),
            benefits = listOf("30% longer battery life", "Reduced heat generation", "Optimal for overnight use"),
            isRecommended = true
        ),
        LocalChargingProfile(
            name = "Balanced Performance",
            icon = "⚖️",
            description = "Balanced approach between speed and battery health",
            targetRange = 15..90,
            chargingSpeed = ChargingSpeed.NORMAL,
            timeWindows = listOf("20:00-08:00"),
            benefits = listOf("Good balance of speed and health", "Suitable for daily use"),
            isRecommended = false
        ),
        LocalChargingProfile(
            name = "Quick Boost",
            icon = "⚡",
            description = "Fast charging for when you need power quickly",
            targetRange = 10..100,
            chargingSpeed = ChargingSpeed.FAST,
            timeWindows = listOf("Anytime"),
            benefits = listOf("Fastest charging possible", "Emergency situations"),
            isRecommended = false
        ),
        LocalChargingProfile(
            name = "Travel Mode",
            icon = "✈️",
            description = "Optimized for travel with maximum capacity retention",
            targetRange = 30..95,
            chargingSpeed = ChargingSpeed.NORMAL,
            timeWindows = listOf("Evening", "Hotel stays"),
            benefits = listOf("Maintains charge longer", "Reduced anxiety about power"),
            isRecommended = false
        )
    )
}

private fun generateChargingInsights(): List<ChargingInsight> {
    return listOf(
        ChargingInsight(
            title = "Charging Efficiency",
            value = "94.2%",
            change = "+2.1% this week",
            changeType = ChangeType.POSITIVE,
            icon = "⚡",
            description = "Your charging efficiency has improved due to optimal temperature management"
        ),
        ChargingInsight(
            title = "Average Temperature",
            value = "31.5°C",
            change = "-3.2°C from last week",
            changeType = ChangeType.POSITIVE,
            icon = "🌡️",
            description = "Temperature during charging has decreased, which is excellent for battery health"
        ),
        ChargingInsight(
            title = "Charging Sessions",
            value = "1.2/day",
            change = "-0.3 from last week",
            changeType = ChangeType.POSITIVE,
            icon = "🔄",
            description = "Fewer charging sessions indicate improved battery optimization"
        ),
        ChargingInsight(
            title = "Energy Consumed",
            value = "4.2 kWh",
            change = "-15% this month",
            changeType = ChangeType.POSITIVE,
            icon = "💚",
            description = "Smart charging has reduced your energy consumption significantly"
        )
    )
}

private fun generateOptimizationTips(): List<OptimizationTip> {
    return listOf(
        OptimizationTip(
            title = "Enable Adaptive Brightness",
            description = "Adaptive brightness can reduce battery drain by automatically adjusting screen brightness based on ambient light.",
            category = "Display",
            difficulty = "Easy",
            timeToImplement = "30 seconds",
            energySavings = "10-15%",
            icon = "☀️"
        ),
        OptimizationTip(
            title = "Optimize Background App Refresh",
            description = "Disable background refresh for apps you don't actively use to reduce unnecessary battery drain.",
            category = "Apps",
            difficulty = "Easy",
            timeToImplement = "2 minutes",
            energySavings = "8-12%",
            icon = "📱"
        ),
        OptimizationTip(
            title = "Use Wi-Fi When Available",
            description = "Wi-Fi consumes significantly less power than cellular data for internet connectivity.",
            category = "Connectivity",
            difficulty = "Easy",
            timeToImplement = "Instant",
            energySavings = "20-25%",
            icon = "📶"
        ),
        OptimizationTip(
            title = "Reduce Screen Timeout",
            description = "Set screen timeout to 30 seconds or less to prevent unnecessary battery drain from forgotten screen-on time.",
            category = "Display",
            difficulty = "Easy",
            timeToImplement = "30 seconds",
            energySavings = "5-8%",
            icon = "⏰"
        ),
        OptimizationTip(
            title = "Location Services Optimization",
            description = "Review and disable location access for apps that don't need it, or switch to battery-optimized location mode.",
            category = "Privacy",
            difficulty = "Medium",
            timeToImplement = "5 minutes",
            energySavings = "12-18%",
            icon = "📍"
        )
    )
}
