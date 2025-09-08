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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.voltai.smartbatteryguardian.rememberBatteryData
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

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
    val estimatedExtraHours: Double
)

data class PowerFeature(
    val name: String,
    val description: String,
    val enabled: Boolean,
    val impact: String,
    val category: FeatureCategory
)

enum class PerformanceImpact { NONE, LOW, MEDIUM, HIGH }
enum class FeatureCategory { DISPLAY, CPU, CONNECTIVITY, BACKGROUND, THERMAL }

data class ThermalData(
    val currentTemp: Double,
    val maxTemp: Double,
    val thermalState: ThermalState,
    val coolingActive: Boolean,
    val temperatureHistory: List<TempPoint>,
    val hotspots: List<ComponentTemp>
)

data class TempPoint(
    val timestamp: Long,
    val temperature: Double,
    val cpuUsage: Double
)

data class ComponentTemp(
    val component: String,
    val temperature: Double,
    val status: String,
    val icon: String
)

enum class ThermalState { COOL, NORMAL, WARM, HOT, CRITICAL }

data class AdaptiveOptimization(
    val isEnabled: Boolean,
    val learningProgress: Double,
    val optimizationScore: Int,
    val adaptations: List<Adaptation>,
    val nextOptimization: String,
    val dataPoints: Int
)

data class Adaptation(
    val type: String,
    val description: String,
    val impact: String,
    val confidence: Double,
    val timestamp: Long
)

data class PowerInsight(
    val title: String,
    val description: String,
    val recommendation: String,
    val priority: InsightPriority,
    val potentialSaving: String,
    val icon: String,
    val aiGenerated: Boolean
)

enum class InsightPriority { CRITICAL, HIGH, MEDIUM, LOW, OPTIMIZATION }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AIPoweredPowerManagementScreen() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    var selectedTab by remember { mutableStateOf(0) }
    var isLearning by remember { mutableStateOf(false) }
    
    val powerProfiles = remember { generatePowerProfiles() }
    val thermalData = remember { generateThermalData() }
    val adaptiveOptimization = remember { generateAdaptiveOptimization() }
    val powerInsights = remember { generatePowerInsights() }
    
    val tabs = listOf("⚡ Profiles", "🧠 AI Adaptive", "🌡️ Thermal", "📊 Analytics", "💡 Insights")
    
    var activeProfile by remember { mutableStateOf(powerProfiles.first { it.isActive }) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE1F5FE)
                    )
                )
            )
    ) {
        // AI-Powered Header with Dynamic Status
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = activeProfile.color,
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
                            .size(64.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AIOptimizationIcon(adaptiveOptimization.isEnabled)
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "AI Power Management",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (adaptiveOptimization.isEnabled) "Learning your patterns..." else "Ready to optimize",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Power Savings Display
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "+${activeProfile.estimatedExtraHours.toInt()}h",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Extra Life",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current Profile Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PowerStatusMetric("Active Profile", activeProfile.name, activeProfile.icon)
                    PowerStatusMetric("Savings", "${activeProfile.batterySavingsPercent}%", "💾")
                    PowerStatusMetric("Thermal", getThermalStatusText(thermalData.thermalState), "🌡️")
                    PowerStatusMetric("AI Score", "${adaptiveOptimization.optimizationScore}", "🧠")
                }
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = activeProfile.color,
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
        
        // Content with smooth animations
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { if (targetState > initialState) it else -it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn() togetherWith slideOutHorizontally(
                    targetOffsetX = { if (targetState > initialState) -it else it },
                    animationSpec = tween(300, easing = FastOutLinearInEasing)
                ) + fadeOut()
            }
        ) { tab ->
            when (tab) {
                0 -> PowerProfilesTab(powerProfiles, activeProfile) { activeProfile = it }
                1 -> AIAdaptiveTab(adaptiveOptimization, onToggleLearning = { isLearning = !isLearning })
                2 -> ThermalManagementTab(thermalData, batteryData)
                3 -> PowerAnalyticsTab(powerProfiles, thermalData)
                4 -> PowerInsightsTab(powerInsights)
            }
        }
    }
}

@Composable
fun AIOptimizationIcon(isLearning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                if (isLearning) {
                    rotationZ = rotationAngle
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isLearning) "🧠" else "⚡",
            fontSize = 28.sp
        )
    }
}

@Composable
fun PowerStatusMetric(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 16.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun PowerProfilesTab(
    profiles: List<PowerProfile>,
    activeProfile: PowerProfile,
    onProfileSelected: (PowerProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Profile Selection
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
                        text = "⚡ Power Profiles",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(profiles.filter { !it.isCustom }) { profile ->
                            QuickProfileCard(
                                profile = profile,
                                isActive = profile.id == activeProfile.id,
                                onClick = { onProfileSelected(profile) }
                            )
                        }
                    }
                }
            }
        }
        
        // Custom Profiles
        item {
            Text(
                text = "🎛️ Custom Profiles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        
        items(profiles.filter { it.isCustom }) { profile ->
            DetailedProfileCard(
                profile = profile,
                isActive = profile.id == activeProfile.id,
                onSelect = { onProfileSelected(profile) },
                onEdit = { /* Open profile editor */ }
            )
        }
        
        // Create New Profile Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Create new profile */ },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                ),
                border = BorderStroke(2.dp, Color(0xFF2196F3).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Custom Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

@Composable
fun AIAdaptiveTab(
    optimization: AdaptiveOptimization,
    onToggleLearning: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Learning Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                ),
                border = BorderStroke(2.dp, Color(0xFF6A1B9A).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🧠",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "AI Adaptive Learning",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A)
                                )
                                Text(
                                    text = if (optimization.isEnabled) "Active Learning Mode" else "Learning Disabled",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                        
                        Switch(
                            checked = optimization.isEnabled,
                            onCheckedChange = { onToggleLearning() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF6A1B9A),
                                checkedTrackColor = Color(0xFF6A1B9A).copy(alpha = 0.3f)
                            )
                        )
                    }
                    
                    if (optimization.isEnabled) {
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Learning Progress
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Learning Progress",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(optimization.learningProgress * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LinearProgressIndicator(
                                progress = optimization.learningProgress.toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF6A1B9A),
                                trackColor = Color(0xFF6A1B9A).copy(alpha = 0.2f)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Data Points: ${optimization.dataPoints} • Next: ${optimization.nextOptimization}",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎯 AI Optimization Score",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OptimizationScoreRing(optimization.optimizationScore)
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${optimization.optimizationScore}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = getOptimizationScoreColor(optimization.optimizationScore)
                            )
                            Text(
                                text = getOptimizationScoreText(optimization.optimizationScore),
                                fontSize = 10.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Based on ${optimization.dataPoints} data points collected over time",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Recent Adaptations
        item {
            Text(
                text = "🔄 Recent AI Adaptations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        
        items(optimization.adaptations) { adaptation ->
            AdaptationCard(adaptation)
        }
    }
}

@Composable
fun ThermalManagementTab(thermalData: ThermalData, batteryData: com.voltai.smartbatteryguardian.BatteryData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Thermal Status Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = getThermalStatusColor(thermalData.thermalState).copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, getThermalStatusColor(thermalData.thermalState))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getThermalIcon(thermalData.thermalState),
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Thermal Management",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = getThermalStatusText(thermalData.thermalState),
                                fontSize = 16.sp,
                                color = getThermalStatusColor(thermalData.thermalState),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Current Temperature Display
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = getThermalStatusColor(thermalData.thermalState).copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${thermalData.currentTemp.toInt()}°C",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getThermalStatusColor(thermalData.thermalState)
                                )
                                Text(
                                    text = "Current",
                                    fontSize = 10.sp,
                                    color = Color(0xFF666666)
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📈 Temperature Trend (Last Hour)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ThermalTrendChart(thermalData.temperatureHistory)
                }
            }
        }
        
        // Component Temperatures
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
                        text = "🔧 Component Temperatures",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "🛡️ Thermal Protection",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ThermalActionItem(
                        title = "Active Cooling",
                        description = "Reduce CPU frequency when temperature exceeds threshold",
                        enabled = thermalData.coolingActive,
                        onToggle = { /* Toggle cooling */ }
                    )
                    
                    ThermalActionItem(
                        title = "Background Limit",
                        description = "Limit background app activity during thermal events",
                        enabled = true,
                        onToggle = { /* Toggle background limiting */ }
                    )
                    
                    ThermalActionItem(
                        title = "Display Dimming",
                        description = "Auto-dim display brightness when device gets warm",
                        enabled = false,
                        onToggle = { /* Toggle display dimming */ }
                    )
                }
            }
        }
    }
}

@Composable
fun PowerAnalyticsTab(profiles: List<PowerProfile>, thermalData: ThermalData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Power Savings Analytics
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
                        text = "💾 Power Savings Analytics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📊 Usage Pattern Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    UsagePatternChart()
                }
            }
        }
        
        // Efficiency Metrics
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
                        text = "⚡ Efficiency Metrics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EfficiencyMetricItem("Screen Efficiency", "87%", Color(0xFF4CAF50))
                        EfficiencyMetricItem("CPU Efficiency", "92%", Color(0xFF2196F3))
                        EfficiencyMetricItem("Radio Efficiency", "74%", Color(0xFFFF9800))
                    }
                }
            }
        }
    }
}

@Composable
fun PowerInsightsTab(insights: List<PowerInsight>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AI-Generated Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🤖",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "AI-Powered Insights",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                        Text(
                            text = "Personalized power optimization recommendations",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) profile.color.copy(alpha = 0.1f) else Color.White
        ),
        border = if (isActive) BorderStroke(2.dp, profile.color) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = profile.icon,
                fontSize = 32.sp
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = profile.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "+${profile.estimatedExtraHours.toInt()}h life",
                    fontSize = 10.sp,
                    color = profile.color,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (profile.aiOptimized) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "🧠 AI",
                        fontSize = 8.sp,
                        color = Color(0xFF6A1B9A),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) profile.color.copy(alpha = 0.1f) else Color.White
        ),
        border = if (isActive) BorderStroke(2.dp, profile.color) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profile.icon,
                    fontSize = 32.sp
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profile.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (profile.aiOptimized) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "🧠 AI Optimized",
                                    fontSize = 10.sp,
                                    color = Color(0xFF6A1B9A),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = profile.description,
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "+${profile.estimatedExtraHours.toInt()}h",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = profile.color
                    )
                    Text(
                        text = "${profile.batterySavingsPercent}% savings",
                        fontSize = 10.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Performance Impact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Performance Impact: ",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = profile.performanceImpact.name.lowercase().capitalize(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = getPerformanceImpactColor(profile.performanceImpact)
                    )
                }
                
                Row {
                    if (!isActive) {
                        TextButton(
                            onClick = onSelect,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = profile.color
                            )
                        ) {
                            Text("Activate")
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizationScoreRing(score: Int) {
    Canvas(
        modifier = Modifier.size(120.dp)
    ) {
        val strokeWidth = 12.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        
        // Background ring
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            )
        )
        
        // Progress ring
        val sweepAngle = (score / 100f) * 360f
        drawArc(
            color = getOptimizationScoreColor(score),
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            )
        )
    }
}

@Composable
fun AdaptationCard(adaptation: Adaptation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = adaptation.type,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = adaptation.description,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${(adaptation.confidence * 100).toInt()}% confident",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Impact: ${adaptation.impact}",
                fontSize = 11.sp,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ThermalTrendChart(temperatureHistory: List<TempPoint>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        if (temperatureHistory.isEmpty()) return@Canvas
        
        val stepX = size.width / (temperatureHistory.size - 1).coerceAtLeast(1)
        val maxTemp = temperatureHistory.maxOfOrNull { it.temperature }?.toFloat() ?: 100f
        val minTemp = temperatureHistory.minOfOrNull { it.temperature }?.toFloat() ?: 0f
        
        val tempPoints = temperatureHistory.mapIndexed { index, point ->
            androidx.compose.ui.geometry.Offset(
                x = index * stepX,
                y = size.height - ((point.temperature.toFloat() - minTemp) / (maxTemp - minTemp)) * size.height
            )
        }
        
        // Draw temperature line
        for (i in 0 until tempPoints.size - 1) {
            drawLine(
                color = getThermalStatusColor(getThermalStateFromTemp(temperatureHistory[i].temperature)),
                start = tempPoints[i],
                end = tempPoints[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        // Draw points
        tempPoints.forEachIndexed { index, point ->
            drawCircle(
                color = getThermalStatusColor(getThermalStateFromTemp(temperatureHistory[index].temperature)),
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun ComponentTemperatureItem(component: ComponentTemp) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = component.icon,
            fontSize = 20.sp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = component.component,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = component.status,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
        
        Text(
            text = "${component.temperature.toInt()}°C",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = getThermalStatusColor(getThermalStateFromTemp(component.temperature))
        )
    }
}

@Composable
fun ThermalActionItem(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
        
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF4CAF50),
                checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun PowerSavingsChart(profiles: List<PowerProfile>) {
    // Implementation for power savings comparison chart
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Power Savings Comparison Chart\n(Profile efficiency visualization)",
            textAlign = TextAlign.Center,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun UsagePatternChart() {
    // Implementation for usage pattern analysis
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Usage Pattern Analysis\n(AI learning visualization)",
            textAlign = TextAlign.Center,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun EfficiencyMetricItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PowerInsightCard(insight: PowerInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (insight.priority) {
                InsightPriority.CRITICAL -> Color(0xFFFFEBEE)
                InsightPriority.HIGH -> Color(0xFFFFF8E1)
                InsightPriority.MEDIUM -> Color(0xFFF3E5F5)
                InsightPriority.OPTIMIZATION -> Color(0xFFE8F5E8)
                else -> Color.White
            }
        ),
        border = BorderStroke(
            1.dp, when (insight.priority) {
                InsightPriority.CRITICAL -> Color(0xFFFF5722)
                InsightPriority.HIGH -> Color(0xFFFF9800)
                InsightPriority.MEDIUM -> Color(0xFF9C27B0)
                InsightPriority.OPTIMIZATION -> Color(0xFF4CAF50)
                else -> Color.Transparent
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = insight.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (insight.aiGenerated) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "🧠 AI",
                                    fontSize = 10.sp,
                                    color = Color(0xFF6A1B9A),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = insight.description,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                if (insight.potentialSaving.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = insight.potentialSaving,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

// Utility Functions
fun generatePowerProfiles(): List<PowerProfile> {
    return listOf(
        PowerProfile(
            id = "balanced",
            name = "Balanced",
            description = "Optimal balance between performance and battery life",
            icon = "⚖️",
            color = Color(0xFF2196F3),
            batterySavingsPercent = 25,
            performanceImpact = PerformanceImpact.LOW,
            features = emptyList(),
            isActive = true,
            isCustom = false,
            aiOptimized = false,
            estimatedExtraHours = 3.5
        ),
        PowerProfile(
            id = "performance",
            name = "Performance",
            description = "Maximum performance with moderate power usage",
            icon = "🚀",
            color = Color(0xFFFF5722),
            batterySavingsPercent = 10,
            performanceImpact = PerformanceImpact.NONE,
            features = emptyList(),
            isActive = false,
            isCustom = false,
            aiOptimized = false,
            estimatedExtraHours = 1.2
        ),
        PowerProfile(
            id = "battery_saver",
            name = "Battery Saver",
            description = "Maximum battery life with performance limitations",
            icon = "🔋",
            color = Color(0xFF4CAF50),
            batterySavingsPercent = 60,
            performanceImpact = PerformanceImpact.MEDIUM,
            features = emptyList(),
            isActive = false,
            isCustom = false,
            aiOptimized = false,
            estimatedExtraHours = 8.5
        ),
        PowerProfile(
            id = "ai_optimized",
            name = "AI Optimized",
            description = "Machine learning optimized profile based on your usage",
            icon = "🧠",
            color = Color(0xFF6A1B9A),
            batterySavingsPercent = 45,
            performanceImpact = PerformanceImpact.LOW,
            features = emptyList(),
            isActive = false,
            isCustom = true,
            aiOptimized = true,
            estimatedExtraHours = 6.2
        )
    )
}

fun generateThermalData(): ThermalData {
    val temperatureHistory = (0..20).map {
        TempPoint(
            timestamp = System.currentTimeMillis() - (it * 3 * 60 * 1000), // Every 3 minutes
            temperature = Random.nextDouble(25.0, 42.0),
            cpuUsage = Random.nextDouble(10.0, 90.0)
        )
    }.reversed()
    
    return ThermalData(
        currentTemp = 32.5,
        maxTemp = 45.0,
        thermalState = ThermalState.NORMAL,
        coolingActive = false,
        temperatureHistory = temperatureHistory,
        hotspots = listOf(
            ComponentTemp("CPU", 34.2, "Normal", "🧠"),
            ComponentTemp("Battery", 31.8, "Normal", "🔋"),
            ComponentTemp("GPU", 29.5, "Cool", "🎮"),
            ComponentTemp("Charging IC", 33.1, "Normal", "⚡"),
            ComponentTemp("Display", 28.9, "Cool", "📱")
        )
    )
}

fun generateAdaptiveOptimization(): AdaptiveOptimization {
    return AdaptiveOptimization(
        isEnabled = true,
        learningProgress = 0.73,
        optimizationScore = 87,
        adaptations = listOf(
            Adaptation(
                type = "Background App Limiting",
                description = "Reduced background activity for social media apps during work hours",
                impact = "12% battery savings",
                confidence = 0.91,
                timestamp = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
            ),
            Adaptation(
                type = "Display Brightness Optimization",
                description = "Auto-adjusted brightness based on ambient light and usage patterns",
                impact = "8% battery savings",
                confidence = 0.85,
                timestamp = System.currentTimeMillis() - (1 * 60 * 60 * 1000)
            ),
            Adaptation(
                type = "CPU Frequency Scaling",
                description = "Optimized CPU performance for your typical app usage",
                impact = "15% performance improvement",
                confidence = 0.78,
                timestamp = System.currentTimeMillis() - (30 * 60 * 1000)
            )
        ),
        nextOptimization = "Connectivity optimization in 2h",
        dataPoints = 1247
    )
}

fun generatePowerInsights(): List<PowerInsight> {
    return listOf(
        PowerInsight(
            title = "High Screen Time Detected",
            description = "Your screen time is 40% higher than optimal. Consider reducing brightness or enabling dark mode.",
            recommendation = "Enable adaptive brightness and dark mode for 15% battery savings",
            priority = InsightPriority.HIGH,
            potentialSaving = "+2.1h",
            icon = "📱",
            aiGenerated = true
        ),
        PowerInsight(
            title = "Background App Optimization",
            description = "5 apps are using significant background battery. AI can optimize these automatically.",
            recommendation = "Enable AI background optimization for these apps",
            priority = InsightPriority.MEDIUM,
            potentialSaving = "+1.3h",
            icon = "🔄",
            aiGenerated = true
        ),
        PowerInsight(
            title = "Excellent Thermal Management",
            description = "Device temperature has been optimal for the past 24 hours. Good usage patterns!",
            recommendation = "Continue current usage patterns for optimal battery health",
            priority = InsightPriority.OPTIMIZATION,
            potentialSaving = "",
            icon = "🌡️",
            aiGenerated = false
        ),
        PowerInsight(
            title = "WiFi vs Mobile Data Usage",
            description = "You're using mobile data 60% more than necessary. WiFi is available in your frequent locations.",
            recommendation = "Enable smart WiFi connection for better efficiency",
            priority = InsightPriority.MEDIUM,
            potentialSaving = "+0.8h",
            icon = "📶",
            aiGenerated = true
        )
    )
}

fun getThermalStatusColor(state: ThermalState): Color {
    return when (state) {
        ThermalState.COOL -> Color(0xFF2196F3)
        ThermalState.NORMAL -> Color(0xFF4CAF50)
        ThermalState.WARM -> Color(0xFFFF9800)
        ThermalState.HOT -> Color(0xFFFF5722)
        ThermalState.CRITICAL -> Color(0xFFD32F2F)
    }
}

fun getThermalStatusText(state: ThermalState): String {
    return when (state) {
        ThermalState.COOL -> "Cool & Efficient"
        ThermalState.NORMAL -> "Normal Temperature"
        ThermalState.WARM -> "Slightly Warm"
        ThermalState.HOT -> "Getting Hot"
        ThermalState.CRITICAL -> "Critical Temperature"
    }
}

fun getThermalIcon(state: ThermalState): String {
    return when (state) {
        ThermalState.COOL -> "❄️"
        ThermalState.NORMAL -> "🌡️"
        ThermalState.WARM -> "🔥"
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

fun getOptimizationScoreColor(score: Int): Color {
    return when {
        score >= 90 -> Color(0xFF4CAF50)
        score >= 70 -> Color(0xFF2196F3)
        score >= 50 -> Color(0xFFFF9800)
        else -> Color(0xFFFF5722)
    }
}

fun getOptimizationScoreText(score: Int): String {
    return when {
        score >= 90 -> "Excellent"
        score >= 70 -> "Good"
        score >= 50 -> "Fair"
        else -> "Needs Work"
    }
}

fun getPerformanceImpactColor(impact: PerformanceImpact): Color {
    return when (impact) {
        PerformanceImpact.NONE -> Color(0xFF4CAF50)
        PerformanceImpact.LOW -> Color(0xFF2196F3)
        PerformanceImpact.MEDIUM -> Color(0xFFFF9800)
        PerformanceImpact.HIGH -> Color(0xFFFF5722)
    }
}
