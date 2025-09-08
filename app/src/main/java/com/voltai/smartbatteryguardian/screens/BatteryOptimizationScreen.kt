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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*
import com.voltai.smartbatteryguardian.models.OptimizationCategory
import com.voltai.smartbatteryguardian.models.OptimizationImpact

// Local data models for Battery Optimization
data class OptimizationSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val category: OptimizationCategory,
    val priority: OptimizationPriority,
    val potentialSavings: String,
    val difficulty: OptimizationDifficulty,
    val timeToImplement: String,
    val icon: String,
    val isImplemented: Boolean,
    val instructions: List<String>,
    val impact: OptimizationImpact
)

enum class OptimizationPriority {
    CRITICAL, HIGH, MEDIUM, LOW
}

enum class OptimizationDifficulty {
    EASY, MEDIUM, ADVANCED
}

// Custom AppUsageData for this screen
data class AppUsageData(
    val appName: String,
    val packageName: String,
    val batteryUsage: Float, // percentage
    val screenTime: Long, // in minutes
    val backgroundTime: Long, // in minutes
    val networkUsage: Float, // MB
    val wakeups: Int,
    val isOptimizable: Boolean,
    val icon: String
)

data class SystemOptimization(
    val category: String,
    val currentValue: String,
    val recommendedValue: String,
    val description: String,
    val savingsPotential: String,
    val icon: String,
    val isSystemSetting: Boolean
)

data class OptimizationScore(
    val overallScore: Int, // 0-100
    val categoryScores: Map<OptimizationCategory, Int>,
    val implementedOptimizations: Int,
    val totalOptimizations: Int,
    val estimatedBatteryGain: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryOptimizationScreen() {
    val context = LocalContext.current
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("💡 Suggestions", "📱 Apps", "⚙️ System", "📊 Analysis", "🎯 Score")
    
    // Generate sample data
    val optimizationSuggestions = remember { generateOptimizationSuggestions() }
    val appUsageData = remember { generateAppUsageData() }
    val systemOptimizations = remember { generateSystemOptimizations() }
    val optimizationScore = remember { calculateOptimizationScore(optimizationSuggestions) }
    
    var implementedSuggestions by remember { 
        mutableStateOf(optimizationSuggestions.filter { it.isImplemented }.map { it.id }.toSet())
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFF3E5F5)
                    )
                )
            )
    ) {
        // Header with Optimization Score
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1976D2),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Optimization Brain Animation
                    OptimizationBrainIcon()
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Optimization",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "AI-powered suggestions to extend battery life",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    
                    // Optimization Score Circle
                    OptimizationScoreIndicator(score = optimizationScore.overallScore)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Quick optimization stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OptimizationStatCard(
                        "${optimizationScore.overallScore}%", 
                        "Score", 
                        "🎯", 
                        getScoreColor(optimizationScore.overallScore)
                    )
                    OptimizationStatCard(
                        "${optimizationSuggestions.count { it.priority == OptimizationPriority.HIGH }}", 
                        "High Priority", 
                        "🔥", 
                        Color(0xFFFF5722)
                    )
                    OptimizationStatCard(
                        optimizationScore.estimatedBatteryGain, 
                        "Est. Gain", 
                        "⚡", 
                        Color(0xFF4CAF50)
                    )
                    OptimizationStatCard(
                        "${optimizationScore.implementedOptimizations}/${optimizationScore.totalOptimizations}", 
                        "Applied", 
                        "✅", 
                        Color(0xFF2196F3)
                    )
                }
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF1976D2),
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
                    item { OptimizationOverviewCard(optimizationScore) }
                    items(optimizationSuggestions.sortedByDescending { it.priority.ordinal }) { suggestion ->
                        OptimizationSuggestionCard(
                            suggestion = suggestion,
                            onImplement = { suggestionId ->
                                implementedSuggestions = implementedSuggestions + suggestionId
                            }
                        )
                    }
                }
                1 -> {
                    item { AppOptimizationOverviewCard(appUsageData) }
                    items(appUsageData.sortedByDescending { it.batteryUsage }) { app ->
                        AppOptimizationCard(app)
                    }
                }
                2 -> {
                    items(systemOptimizations) { optimization ->
                        SystemOptimizationCard(optimization)
                    }
                }
                3 -> {
                    item { OptimizationAnalysisCard(optimizationScore) }
                    item { CategoryBreakdownCard(optimizationScore.categoryScores) }
                    item { OptimizationTrendAnalysisCard() }
                }
                4 -> {
                    item { DetailedScoreCard(optimizationScore) }
                    item { ScoreHistoryCard() }
                    item { OptimizationGoalsCard() }
                }
            }
        }
    }
}

@Composable
fun OptimizationBrainIcon() {
    val rotation by animateFloatAsState(
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val pulseScale by animateFloatAsState(
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
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
            )
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
                rotationZ = rotation / 10f
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "💡", fontSize = 32.sp)
        
        // Optimization rings
        repeat(2) { index ->
            val ringScale by animateFloatAsState(
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, delayMillis = index * 1500, easing = EaseOut),
                    repeatMode = RepeatMode.Restart
                )
            )
            
            Box(
                modifier = Modifier
                    .size((50 + index * 10).dp)
                    .graphicsLayer {
                        scaleX = ringScale
                        scaleY = ringScale
                        alpha = 1f - ((ringScale - 1f) / 0.3f) * 0.7f
                    }
                    .border(
                        1.5.dp,
                        Color.White.copy(alpha = 0.3f),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun OptimizationScoreIndicator(score: Int) {
    val animatedScore by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic)
    )
    
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
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
                text = "${score}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Score",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun OptimizationStatCard(value: String, label: String, icon: String, color: Color) {
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
fun OptimizationOverviewCard(score: OptimizationScore) {
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
                text = "💡 Optimization Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Optimization Level",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = getScoreLevel(score.overallScore),
                        fontSize = 14.sp,
                        color = getScoreColor(score.overallScore),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "${score.overallScore}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(score.overallScore)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = score.overallScore / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = getScoreColor(score.overallScore),
                trackColor = getScoreColor(score.overallScore).copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Potential battery life extension: ${score.estimatedBatteryGain}",
                fontSize = 14.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OptimizationSuggestionCard(
    suggestion: OptimizationSuggestion,
    onImplement: (String) -> Unit
) {
    val priorityColor = when (suggestion.priority) {
        OptimizationPriority.CRITICAL -> Color(0xFFF44336)
        OptimizationPriority.HIGH -> Color(0xFFFF5722)
        OptimizationPriority.MEDIUM -> Color(0xFFFF9800)
        OptimizationPriority.LOW -> Color(0xFF4CAF50)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = suggestion.icon, fontSize = 24.sp)
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = suggestion.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = priorityColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = suggestion.priority.name,
                                fontSize = 10.sp,
                                color = priorityColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = suggestion.description,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
                
                if (suggestion.isImplemented) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "✅", fontSize = 16.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Impact details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ImpactItem("Savings", suggestion.potentialSavings, "💚")
                ImpactItem("Time", suggestion.timeToImplement, "⏱️")
                ImpactItem("Difficulty", suggestion.difficulty.name, "🎯")
            }
            
            if (!suggestion.isImplemented) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { /* Show instructions */ },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("How to", fontSize = 12.sp)
                    }
                    
                    Button(
                        onClick = { onImplement(suggestion.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        Text("Apply", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ImpactItem(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 12.sp)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun AppOptimizationOverviewCard(apps: List<AppUsageData>) {
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
                text = "📱 App Usage Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val topConsumers = apps.sortedByDescending { it.batteryUsage }.take(3)
            val optimizableApps = apps.count { it.isOptimizable }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AppStatItem("Apps Scanned", "${apps.size}", "📊")
                AppStatItem("Optimizable", "$optimizableApps", "🔧")
                AppStatItem("Top Consumer", topConsumers.firstOrNull()?.appName ?: "N/A", "🔋")
            }
        }
    }
}

@Composable
fun AppStatItem(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 16.sp)
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
fun AppOptimizationCard(app: AppUsageData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = app.icon, fontSize = 24.sp)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Battery: ${String.format("%.1f", app.batteryUsage)}% • Screen: ${app.screenTime}min",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
                
                if (app.wakeups > 0) {
                    Text(
                        text = "${app.wakeups} wake-ups • Background: ${app.backgroundTime}min",
                        fontSize = 10.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${String.format("%.1f", app.batteryUsage)}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = getBatteryUsageColor(app.batteryUsage)
                )
                
                if (app.isOptimizable) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "OPTIMIZE",
                            fontSize = 9.sp,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SystemOptimizationCard(optimization: SystemOptimization) {
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = optimization.icon, fontSize = 20.sp)
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = optimization.category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = optimization.description,
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                if (optimization.isSystemSetting) {
                    Text(
                        text = "⚙️",
                        fontSize = 16.sp
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
                        text = "Current: ${optimization.currentValue}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "Recommended: ${optimization.recommendedValue}",
                        fontSize = 12.sp,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "💚 ${optimization.savingsPotential}",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun OptimizationAnalysisCard(score: OptimizationScore) {
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
                text = "📊 Optimization Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your device's optimization score is ${score.overallScore}%, which is ${getScoreLevel(score.overallScore).lowercase()}. You have implemented ${score.implementedOptimizations} out of ${score.totalOptimizations} recommended optimizations.",
                fontSize = 14.sp,
                color = Color(0xFF333333),
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Key Insights:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val insights = generateOptimizationInsights(score)
            insights.forEach { insight ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(text = "• ", fontSize = 12.sp, color = Color(0xFF666666))
                    Text(text = insight, fontSize = 12.sp, color = Color(0xFF666666))
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownCard(categoryScores: Map<OptimizationCategory, Int>) {
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
                text = "📈 Category Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            categoryScores.forEach { (category, score) ->
                CategoryScoreItem(category, score)
                if (category != categoryScores.keys.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryScoreItem(category: OptimizationCategory, score: Int) {
    val categoryIcon = when (category) {
        OptimizationCategory.BATTERY -> "🔋"
        OptimizationCategory.CPU -> "⚙️"
        OptimizationCategory.MEMORY -> "💾"
        OptimizationCategory.NETWORK -> "📶"
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = categoryIcon, fontSize = 16.sp)
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            LinearProgressIndicator(
                progress = score / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = getScoreColor(score),
                trackColor = getScoreColor(score).copy(alpha = 0.2f)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "${score}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = getScoreColor(score)
        )
    }
}

// Additional placeholder cards
@Composable
fun DetailedScoreCard(score: OptimizationScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "🎯 Detailed Optimization Score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Comprehensive score breakdown coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable
fun ScoreHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📈 Score History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Optimization score trends coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable
fun OptimizationGoalsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "🏆 Optimization Goals",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Personalized optimization goals coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable
fun OptimizationTrendAnalysisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📊 Trend Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Battery usage trend analysis coming soon...", color = Color(0xFF666666))
        }
    }
}

// Utility functions
private fun generateOptimizationSuggestions(): List<OptimizationSuggestion> {
    return listOf(
        OptimizationSuggestion(
            id = "display_brightness",
            title = "Enable Adaptive Brightness",
            description = "Automatic brightness adjustment can save 10-15% battery by optimizing screen brightness based on ambient light.",
            category = OptimizationCategory.BATTERY,
            priority = OptimizationPriority.HIGH,
            potentialSavings = "10-15%",
            difficulty = OptimizationDifficulty.EASY,
            timeToImplement = "30 seconds",
            icon = "☀️",
            isImplemented = false,
            instructions = listOf("Go to Settings", "Display", "Enable Adaptive brightness"),
            impact = OptimizationImpact.HIGH
        ),
        OptimizationSuggestion(
            id = "background_refresh",
            title = "Optimize Background App Refresh",
            description = "Limiting background refresh for unused apps can significantly reduce battery drain and improve performance.",
            category = OptimizationCategory.CPU,
            priority = OptimizationPriority.HIGH,
            potentialSavings = "8-12%",
            difficulty = OptimizationDifficulty.EASY,
            timeToImplement = "2 minutes",
            icon = "📱",
            isImplemented = false,
            instructions = listOf("Go to Settings", "Apps", "Special access", "Background activity"),
            impact = OptimizationImpact.MEDIUM
        ),
        OptimizationSuggestion(
            id = "wifi_preference",
            title = "Prefer Wi-Fi Over Mobile Data",
            description = "Wi-Fi uses significantly less power than cellular data. Enable automatic Wi-Fi connection when available.",
            category = OptimizationCategory.NETWORK,
            priority = OptimizationPriority.MEDIUM,
            potentialSavings = "20-25%",
            difficulty = OptimizationDifficulty.EASY,
            timeToImplement = "1 minute",
            icon = "📶",
            isImplemented = true,
            instructions = listOf("Settings", "Wi-Fi", "Wi-Fi preferences", "Turn on Wi-Fi automatically"),
            impact = OptimizationImpact.HIGH
        ),
        OptimizationSuggestion(
            id = "location_optimization",
            title = "Optimize Location Services",
            description = "Review app location permissions and switch to battery-optimized location mode for better efficiency.",
            category = OptimizationCategory.MEMORY,
            priority = OptimizationPriority.MEDIUM,
            potentialSavings = "12-18%",
            difficulty = OptimizationDifficulty.MEDIUM,
            timeToImplement = "5 minutes",
            icon = "📍",
            isImplemented = false,
            instructions = listOf("Settings", "Location", "App location permissions", "Review and adjust"),
            impact = OptimizationImpact.MEDIUM
        ),
        OptimizationSuggestion(
            id = "charging_optimization",
            title = "Enable Adaptive Charging",
            description = "Adaptive charging learns your routine and delays charging to 100% until you need it, improving battery longevity.",
            category = OptimizationCategory.BATTERY,
            priority = OptimizationPriority.CRITICAL,
            potentialSavings = "30% longer lifespan",
            difficulty = OptimizationDifficulty.EASY,
            timeToImplement = "30 seconds",
            icon = "🔋",
            isImplemented = false,
            instructions = listOf("Settings", "Battery", "Adaptive preferences", "Adaptive charging"),
            impact = OptimizationImpact.LOW
        )
    )
}

private fun generateAppUsageData(): List<AppUsageData> {
    val apps = listOf(
        "Chrome" to "🌐", "Instagram" to "📸", "YouTube" to "📹", 
        "WhatsApp" to "💬", "Spotify" to "🎵", "Maps" to "🗺️",
        "Camera" to "📷", "Gmail" to "📧", "Photos" to "🖼️", "Games" to "🎮"
    )
    
    return apps.map { (name, icon) ->
        AppUsageData(
            appName = name,
            packageName = "com.example.$name",
            batteryUsage = Random.nextFloat() * 15f + 1f,
            screenTime = Random.nextLong(10, 300),
            backgroundTime = Random.nextLong(0, 100),
            networkUsage = Random.nextFloat() * 500f,
            wakeups = Random.nextInt(0, 50),
            isOptimizable = Random.nextBoolean(),
            icon = icon
        )
    }
}

private fun generateSystemOptimizations(): List<SystemOptimization> {
    return listOf(
        SystemOptimization(
            category = "Screen Timeout",
            currentValue = "2 minutes",
            recommendedValue = "30 seconds",
            description = "Reduce screen timeout to save battery when phone is idle",
            savingsPotential = "5-8%",
            icon = "⏰",
            isSystemSetting = true
        ),
        SystemOptimization(
            category = "Always-on Display",
            currentValue = "Enabled",
            recommendedValue = "Disabled",
            description = "Turn off always-on display to reduce power consumption",
            savingsPotential = "3-5%",
            icon = "📱",
            isSystemSetting = true
        ),
        SystemOptimization(
            category = "Haptic Feedback",
            currentValue = "Strong",
            recommendedValue = "Light",
            description = "Reduce haptic feedback intensity to save battery",
            savingsPotential = "2-3%",
            icon = "📳",
            isSystemSetting = true
        )
    )
}

private fun calculateOptimizationScore(suggestions: List<OptimizationSuggestion>): OptimizationScore {
    val implementedCount = suggestions.count { it.isImplemented }
    val totalCount = suggestions.size
    val overallScore = if (totalCount > 0) (implementedCount * 100) / totalCount else 100
    
    val categoryScores = OptimizationCategory.values().associateWith { category ->
        val categorySuggestions = suggestions.filter { it.category == category }
        val implementedInCategory = categorySuggestions.count { it.isImplemented }
        if (categorySuggestions.isNotEmpty()) 
            (implementedInCategory * 100) / categorySuggestions.size 
        else 100
    }
    
    return OptimizationScore(
        overallScore = overallScore,
        categoryScores = categoryScores,
        implementedOptimizations = implementedCount,
        totalOptimizations = totalCount,
        estimatedBatteryGain = "${Random.nextInt(2, 8)}h"
    )
}

private fun generateOptimizationInsights(score: OptimizationScore): List<String> {
    val insights = mutableListOf<String>()
    
    if (score.overallScore < 50) {
        insights.add("Your optimization score is below average. Focus on high-priority recommendations first.")
    }
    
    val lowestCategory = score.categoryScores.minByOrNull { it.value }
    lowestCategory?.let { (category, categoryScore) ->
        if (categoryScore < 60) {
            insights.add("${category.name.lowercase().replaceFirstChar { it.uppercase() }} optimizations need attention (${categoryScore}%).")
        }
    }
    
    if (score.implementedOptimizations < 3) {
        insights.add("Implementing just 3 more optimizations could improve your battery life significantly.")
    }
    
    insights.add("Your current settings could extend battery life by up to ${score.estimatedBatteryGain}.")
    
    return insights
}

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50)
        score >= 60 -> Color(0xFF8BC34A)
        score >= 40 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

private fun getScoreLevel(score: Int): String {
    return when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Good"
        score >= 40 -> "Fair"
        else -> "Poor"
    }
}

private fun getBatteryUsageColor(usage: Float): Color {
    return when {
        usage >= 10f -> Color(0xFFF44336) // Red for high usage
        usage >= 5f -> Color(0xFFFF9800)  // Orange for medium usage
        else -> Color(0xFF4CAF50)          // Green for low usage
    }
}
