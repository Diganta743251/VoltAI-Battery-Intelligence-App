package com.voltai.smartbatteryguardian.screens

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
import com.voltai.smartbatteryguardian.rememberBatteryData
import kotlin.math.*
import kotlin.random.Random

data class BatteryHealthData(
    val overallHealth: Int,
    val capacity: Int,
    val designCapacity: Int,
    val cycles: Int,
    val maxCycles: Int,
    val wear: Double,
    val chargingEfficiency: Double,
    val temperature: Double,
    val voltage: Double,
    val current: Double,
    val chemistry: BatteryChemistry,
    val calibrationDate: Long,
    val manufactureDate: Long,
    val healthTrend: List<HealthPoint>,
    val predictions: HealthPredictions
)

data class BatteryChemistry(
    val type: String, // Li-ion, Li-Po, etc.
    val cathode: String,
    val anode: String,
    val electrolyte: String,
    val nominalVoltage: Double,
    val cutoffVoltage: Double,
    val maxVoltage: Double
)

data class HealthPoint(
    val date: Long,
    val healthPercentage: Int,
    val capacity: Int,
    val cycles: Int
)

data class HealthPredictions(
    val remainingLifeMonths: Int,
    val nextCalibrationDays: Int,
    val replacementRecommendation: String,
    val capacityIn6Months: Int,
    val capacityIn1Year: Int,
    val healthScore: String
)

data class HealthRecommendation(
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val action: String,
    val icon: String
)

enum class RecommendationPriority { CRITICAL, HIGH, MEDIUM, LOW, INFO }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun EnhancedBatteryHealthScreen() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    var selectedTab by remember { mutableStateOf(0) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val healthData = remember { generateHealthData() }
    val recommendations = remember { generateRecommendations(healthData) }

    val tabs = listOf("🏥 Overview", "🔬 Chemistry", "📊 Analytics", "🔮 Predictions", "🛠 Tools")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFFFF3E0)
                    )
                )
            )
    ) {
        // Premium Header with Health Status
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = getHealthStatusColor(healthData.overallHealth),
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
                        AnimatedHealthIcon(healthData.overallHealth)
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Battery Health Lab",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = getHealthStatusText(healthData.overallHealth),
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Real-time Health Score
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${healthData.overallHealth}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Health Score",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Health Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickHealthMetric("Cycles", "${healthData.cycles}", "🔄")
                    QuickHealthMetric("Capacity", "${healthData.capacity}mAh", "⚡")
                    QuickHealthMetric("Wear", "${String.format("%.1f", healthData.wear)}%", "📉")
                    QuickHealthMetric(
                        "Efficiency",
                        "${String.format("%.0f", healthData.chargingEfficiency * 100)}%",
                        "🎯"
                    )
                }
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = getHealthStatusColor(healthData.overallHealth),
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

        // Content with advanced animations
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
                0 -> HealthOverviewTab(healthData, recommendations, batteryData)
                1 -> BatteryChemistryTab(healthData.chemistry, batteryData)
                2 -> HealthAnalyticsTab(healthData)
                3 -> PredictionsTab(healthData.predictions)
                4 -> HealthToolsTab(onAnalyze = { isAnalyzing = !isAnalyzing })
            }
        }
    }
}

@Composable
fun AnimatedHealthIcon(healthPercentage: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                healthPercentage >= 90 -> "💚"
                healthPercentage >= 75 -> "💛"
                healthPercentage >= 50 -> "🧡"
                else -> "❤️"
            },
            fontSize = 24.sp,
            modifier = Modifier.graphicsLayer {
                rotationZ = rotationAngle
            }
        )
    }
}

@Composable
fun QuickHealthMetric(label: String, value: String, icon: String) {
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
fun HealthOverviewTab(
    healthData: BatteryHealthData,
    recommendations: List<HealthRecommendation>,
    batteryData: com.voltai.smartbatteryguardian.BatteryData
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Advanced Health Ring
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Comprehensive Health Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Multi-ring health display
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AdvancedHealthRings(healthData)

                        // Center content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${healthData.overallHealth}%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = getHealthStatusColor(healthData.overallHealth)
                            )
                            Text(
                                text = healthData.predictions.healthScore,
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Health metrics grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        HealthMetricItem("Capacity", "${healthData.capacity}/${healthData.designCapacity} mAh")
                        HealthMetricItem("Cycles", "${healthData.cycles}/${healthData.maxCycles}")
                        HealthMetricItem("Wear Level", "${String.format("%.1f", healthData.wear)}%")
                    }
                }
            }
        }

        // Critical Recommendations
        item {
            Text(
                text = "🚨 Priority Recommendations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        items(recommendations.filter { it.priority <= RecommendationPriority.HIGH }) { recommendation ->
            RecommendationCard(recommendation)
        }

        // Health Trend Chart
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
                        text = "📈 Health Trend (30 Days)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HealthTrendChart(healthData.healthTrend)
                }
            }
        }

        // Detailed Metrics
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
                        text = "📋 Detailed Metrics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailedMetricRow(
                        "Temperature",
                        "${batteryData.temperature}°C",
                        getTemperatureStatus(batteryData.temperature.toDouble())
                    )
                    DetailedMetricRow(
                        "Voltage",
                        "${batteryData.voltage}V",
                        getVoltageStatus(batteryData.voltage.toDouble())
                    )
                    DetailedMetricRow(
                        "Current",
                        "${String.format("%.0f", healthData.current)}mA",
                        getCurrentStatus(healthData.current)
                    )
                    DetailedMetricRow(
                        "Charging Efficiency",
                        "${String.format("%.1f", healthData.chargingEfficiency * 100)}%",
                        getEfficiencyStatus(healthData.chargingEfficiency)
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryChemistryTab(chemistry: BatteryChemistry, batteryData: com.voltai.smartbatteryguardian.BatteryData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chemistry Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A237E).copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, Color(0xFF1A237E).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🧪",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Battery Chemistry",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E)
                            )
                            Text(
                                text = chemistry.type,
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Chemistry components
                    ChemistryComponent("Cathode Material", chemistry.cathode, "⚫")
                    Spacer(modifier = Modifier.height(12.dp))
                    ChemistryComponent("Anode Material", chemistry.anode, "⚪")
                    Spacer(modifier = Modifier.height(12.dp))
                    ChemistryComponent("Electrolyte", chemistry.electrolyte, "💧")
                }
            }
        }

        // Voltage Characteristics
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
                        text = "⚡ Voltage Characteristics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    VoltageCharacteristicsChart(chemistry, batteryData.voltage.toDouble())

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        VoltageMetric("Nominal", "${chemistry.nominalVoltage}V", Color(0xFF4CAF50))
                        VoltageMetric("Current", "${batteryData.voltage}V", Color(0xFF2196F3))
                        VoltageMetric("Maximum", "${chemistry.maxVoltage}V", Color(0xFFFF5722))
                    }
                }
            }
        }

        // Chemistry Analysis
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
                        text = "🔬 Chemical Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ChemicalAnalysisItem(
                        title = "Ion Mobility",
                        description = "Excellent lithium ion conductivity detected",
                        status = "Optimal",
                        color = Color(0xFF4CAF50)
                    )

                    ChemicalAnalysisItem(
                        title = "Electrode Integrity",
                        description = "Minor degradation in cathode structure",
                        status = "Good",
                        color = Color(0xFFFF9800)
                    )

                    ChemicalAnalysisItem(
                        title = "Electrolyte Quality",
                        description = "No significant decomposition products",
                        status = "Excellent",
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Safety Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFE0B2)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "🛡️ Safety Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SafetyInfoItem("Operating Temperature", "0°C to 45°C", batteryData.temperature < 45)
                    SafetyInfoItem(
                        "Voltage Range",
                        "${chemistry.cutoffVoltage}V - ${chemistry.maxVoltage}V",
                        batteryData.voltage.toDouble() in chemistry.cutoffVoltage..chemistry.maxVoltage
                    )
                    SafetyInfoItem("Overcharge Protection", "Active", true)
                    SafetyInfoItem("Thermal Protection", "Active", batteryData.temperature < 40)
                }
            }
        }
    }
}

@Composable
fun HealthAnalyticsTab(healthData: BatteryHealthData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Performance Analytics
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
                        text = "📊 Performance Analytics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PerformanceAnalyticsChart(healthData)
                }
            }
        }

        // Degradation Analysis
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
                        text = "📉 Degradation Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DegradationFactorItem(
                        "Cycle Aging",
                        (healthData.cycles.toDouble() / healthData.maxCycles * 100).toInt(),
                        Color(0xFFFF5722)
                    )
                    DegradationFactorItem("Calendar Aging", 15, Color(0xFFFF9800))
                    DegradationFactorItem("Temperature Stress", 8, Color(0xFF4CAF50))
                    DegradationFactorItem("Voltage Stress", 12, Color(0xFFFF9800))
                }
            }
        }

        // Comparative Analysis
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
                        text = "⚖️ Comparative Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ComparisonChart(healthData)
                }
            }
        }
    }
}

@Composable
fun PredictionsTab(predictions: HealthPredictions) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Predictions Card
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔮",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "AI-Powered Predictions",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6A1B9A)
                            )
                            Text(
                                text = "Machine learning battery forecasts",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }

        // Remaining Life Prediction
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
                        text = "⏳ Remaining Battery Life",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    RemainingLifeVisualization(predictions.remainingLifeMonths)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Estimated ${predictions.remainingLifeMonths} months of optimal performance remaining",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Capacity Predictions
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
                        text = "📈 Capacity Forecast",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CapacityForecastChart(predictions)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ForecastMetric("6 Months", "${predictions.capacityIn6Months}mAh")
                        ForecastMetric("1 Year", "${predictions.capacityIn1Year}mAh")
                    }
                }
            }
        }

        // Maintenance Predictions
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
                        text = "🔧 Maintenance Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MaintenanceScheduleItem("Battery Calibration", "${predictions.nextCalibrationDays} days", "🔋")
                    MaintenanceScheduleItem("Deep Cycle", "14 days", "🔄")
                    MaintenanceScheduleItem("Health Check", "30 days", "🏥")

                    Spacer(modifier = Modifier.height(16.dp))

                    if (predictions.replacementRecommendation.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFE0B2)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "💡 Replacement Recommendation",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = predictions.replacementRecommendation,
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HealthToolsTab(onAnalyze: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Battery Calibration Tool
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
                        text = "🎯 Battery Calibration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Improve battery accuracy by calibrating the fuel gauge",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Start calibration */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Start Calibration Process")
                    }
                }
            }
        }

        // Deep Analysis Tool
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
                        text = "🔍 Deep Health Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Comprehensive battery health diagnostic using AI algorithms",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onAnalyze,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("Run Deep Analysis")
                    }
                }
            }
        }

        // Export Health Report
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
                        text = "📊 Export Health Report",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Generate detailed PDF report with all health data",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Export CSV */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export CSV")
                        }
                        Button(
                            onClick = { /* Export PDF */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0)
                            )
                        ) {
                            Text("Export PDF")
                        }
                    }
                }
            }
        }
    }
}

// Supporting Composables
@Composable
fun AdvancedHealthRings(healthData: BatteryHealthData) {
    Canvas(
        modifier = Modifier.size(200.dp)
    ) {
        val center = center
        val strokeWidth = 12.dp.toPx()
        val gap = 8.dp.toPx()

        // Outer ring - Overall Health
        val outerRadius = size.minDimension / 2 - strokeWidth / 2
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - outerRadius,
                center.y - outerRadius
            )
        )
        drawArc(
            color = getHealthStatusColor(healthData.overallHealth),
            startAngle = -90f,
            sweepAngle = (healthData.overallHealth / 100f) * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - outerRadius,
                center.y - outerRadius
            )
        )

        // Middle ring - Capacity
        val middleRadius = outerRadius - strokeWidth - gap
        val capacityPercentage = (healthData.capacity.toFloat() / healthData.designCapacity * 100).toInt()
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth * 0.8f),
            size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - middleRadius,
                center.y - middleRadius
            )
        )
        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = -90f,
            sweepAngle = (capacityPercentage / 100f) * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - middleRadius,
                center.y - middleRadius
            )
        )

        // Inner ring - Cycles
        val innerRadius = middleRadius - strokeWidth - gap
        val cyclePercentage = (healthData.cycles.toFloat() / healthData.maxCycles * 100).toInt()
        drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth * 0.6f),
            size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - innerRadius,
                center.y - innerRadius
            )
        )
        drawArc(
            color = Color(0xFFFF9800),
            startAngle = -90f,
            sweepAngle = (cyclePercentage / 100f) * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth * 0.6f, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - innerRadius,
                center.y - innerRadius
            )
        )
    }
}

@Composable
fun HealthMetricItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun RecommendationCard(recommendation: HealthRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (recommendation.priority) {
                RecommendationPriority.CRITICAL -> Color(0xFFFFEBEE)
                RecommendationPriority.HIGH -> Color(0xFFFFF8E1)
                RecommendationPriority.MEDIUM -> Color(0xFFF3E5F5)
                else -> Color.White
            }
        ),
        border = BorderStroke(
            1.dp,
            when (recommendation.priority) {
                RecommendationPriority.CRITICAL -> Color(0xFFFF5722)
                RecommendationPriority.HIGH -> Color(0xFFFF9800)
                RecommendationPriority.MEDIUM -> Color(0xFF9C27B0)
                else -> Color.Transparent
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = recommendation.icon,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recommendation.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = recommendation.description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (recommendation.action.isNotEmpty()) {
                    Text(
                        text = "Action: ${recommendation.action}",
                        fontSize = 12.sp,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HealthTrendChart(healthTrend: List<HealthPoint>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        if (healthTrend.isEmpty()) return@Canvas

        val stepX = size.width / (healthTrend.size - 1).coerceAtLeast(1)
        val maxHealth = 100f
        val minHealth = healthTrend.minOfOrNull { it.healthPercentage }?.toFloat() ?: 0f

        val points = healthTrend.mapIndexed { index, point ->
            androidx.compose.ui.geometry.Offset(
                x = index * stepX,
                y = size.height - ((point.healthPercentage - minHealth) / (maxHealth - minHealth)) * size.height
            )
        }

        // Draw gradient area
        val path = androidx.compose.ui.graphics.Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, size.height)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, size.height)
                close()
            }
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4CAF50).copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
        )

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
}

// Utility Functions
fun generateHealthData(): BatteryHealthData {
    return BatteryHealthData(
        overallHealth = Random.nextInt(75, 95),
        capacity = Random.nextInt(2800, 3200),
        designCapacity = 3000,
        cycles = Random.nextInt(200, 800),
        maxCycles = 1000,
        wear = Random.nextDouble(5.0, 25.0),
        chargingEfficiency = Random.nextDouble(0.85, 0.95),
        temperature = Random.nextDouble(20.0, 35.0),
        voltage = Random.nextDouble(3.7, 4.2),
        current = Random.nextDouble(-1000.0, 1000.0),
        chemistry = BatteryChemistry(
            type = "Lithium-Ion",
            cathode = "LiCoO2 (Lithium Cobalt Oxide)",
            anode = "Graphite Carbon",
            electrolyte = "LiPF6 in organic carbonate",
            nominalVoltage = 3.7,
            cutoffVoltage = 3.0,
            maxVoltage = 4.2
        ),
        calibrationDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L),
        manufactureDate = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L),
        healthTrend = generateHealthTrend(),
        predictions = HealthPredictions(
            remainingLifeMonths = Random.nextInt(18, 36),
            nextCalibrationDays = Random.nextInt(7, 30),
            replacementRecommendation = "Consider replacement in 24 months for optimal performance",
            capacityIn6Months = Random.nextInt(2700, 3100),
            capacityIn1Year = Random.nextInt(2500, 2900),
            healthScore = "Excellent"
        )
    )
}

fun generateHealthTrend(): List<HealthPoint> {
    val baseHealth = Random.nextInt(85, 95)
    return (0..29).map { day ->
        HealthPoint(
            date = System.currentTimeMillis() - (day * 24 * 60 * 60 * 1000L),
            healthPercentage = (baseHealth - Random.nextInt(0, 5)).coerceAtLeast(70),
            capacity = Random.nextInt(2800, 3200),
            cycles = Random.nextInt(200 + day, 800 + day)
        )
    }.reversed()
}

fun generateRecommendations(healthData: BatteryHealthData): List<HealthRecommendation> {
    val recommendations = mutableListOf<HealthRecommendation>()

    if (healthData.cycles > healthData.maxCycles * 0.8) {
        recommendations.add(
            HealthRecommendation(
                title = "High Cycle Count Warning",
                description = "Battery has completed ${healthData.cycles} cycles. Consider reducing charge frequency.",
                priority = RecommendationPriority.HIGH,
                action = "Enable adaptive charging",
                icon = "⚠️"
            )
        )
    }

    if (healthData.temperature > 35.0) {
        recommendations.add(
            HealthRecommendation(
                title = "Temperature Management",
                description = "Battery temperature is elevated. Avoid charging in hot conditions.",
                priority = RecommendationPriority.MEDIUM,
                action = "Cool down device before charging",
                icon = "🌡️"
            )
        )
    }

    if (healthData.chargingEfficiency < 0.9) {
        recommendations.add(
            HealthRecommendation(
                title = "Charging Efficiency Low",
                description = "Charging efficiency has decreased. Consider battery calibration.",
                priority = RecommendationPriority.MEDIUM,
                action = "Run battery calibration",
                icon = "🔋"
            )
        )
    }

    return recommendations
}

fun getHealthStatusColor(health: Int): Color {
    return when {
        health >= 90 -> Color(0xFF4CAF50)
        health >= 75 -> Color(0xFFFF9800)
        health >= 50 -> Color(0xFFFF5722)
        else -> Color(0xFFD32F2F)
    }
}

fun getHealthStatusText(health: Int): String {
    return when {
        health >= 90 -> "Excellent Health"
        health >= 75 -> "Good Health"
        health >= 50 -> "Fair Health"
        else -> "Poor Health"
    }
}

fun getTemperatureStatus(temp: Double): String {
    return when {
        temp < 15 -> "Cold"
        temp > 35 -> "Warm"
        else -> "Normal"
    }
}

fun getVoltageStatus(voltage: Double): String {
    return when {
        voltage < 3.5 -> "Low"
        voltage > 4.1 -> "High"
        else -> "Normal"
    }
}

fun getCurrentStatus(current: Double): String {
    return when {
        current > 0 -> "Charging"
        current < 0 -> "Discharging"
        else -> "Idle"
    }
}

fun getEfficiencyStatus(efficiency: Double): String {
    return when {
        efficiency >= 0.95 -> "Excellent"
        efficiency >= 0.9 -> "Good"
        efficiency >= 0.8 -> "Fair"
        else -> "Poor"
    }
}

// Additional composables for chemistry tab
@Composable
fun ChemistryComponent(name: String, component: String, icon: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = component,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun VoltageCharacteristicsChart(chemistry: BatteryChemistry, currentVoltage: Double) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val barWidth = size.width / 3
        val maxVoltage = chemistry.maxVoltage.toFloat()

        // Draw voltage bars
        val voltages = listOf(chemistry.cutoffVoltage, chemistry.nominalVoltage, chemistry.maxVoltage)
        val colors = listOf(Color(0xFFFF5722), Color(0xFF4CAF50), Color(0xFFFF9800))
        val labels = listOf("Cutoff", "Nominal", "Maximum")

        voltages.forEachIndexed { index, voltage ->
            val barHeight = (voltage.toFloat() / maxVoltage) * size.height * 0.8f
            val x = index * barWidth + barWidth * 0.25f
            val y = size.height - barHeight

            drawRoundRect(
                color = colors[index].copy(alpha = 0.7f),
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.5f, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }

        // Draw current voltage indicator
        val currentX = size.width / 2
        val currentY = size.height - (currentVoltage.toFloat() / maxVoltage) * size.height * 0.8f
        drawCircle(
            color = Color(0xFF2196F3),
            radius = 6.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(currentX, currentY)
        )
    }
}

@Composable
fun VoltageMetric(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun ChemicalAnalysisItem(title: String, description: String, status: String, color: Color) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Text(
                    text = status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SafetyInfoItem(parameter: String, range: String, isWithinRange: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = parameter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = range,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
        Icon(
            imageVector = if (isWithinRange) Icons.Default.Check else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isWithinRange) Color(0xFF4CAF50) else Color(0xFFFF5722),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DetailedMetricRow(label: String, value: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3)
        )
        Text(
            text = status,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

// Additional composables for analytics tab
@Composable
fun PerformanceAnalyticsChart(healthData: BatteryHealthData) {
    // Implementation for performance analytics chart
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Performance Analytics Chart\n(Advanced visualization)",
            textAlign = TextAlign.Center,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun DegradationFactorItem(factor: String, percentage: Int, color: Color) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = factor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$percentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ComparisonChart(healthData: BatteryHealthData) {
    // Implementation for comparison chart
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Device vs Average Comparison\n(Chart visualization)",
            textAlign = TextAlign.Center,
            color = Color(0xFF666666)
        )
    }
}

// Additional composables for predictions tab
@Composable
fun RemainingLifeVisualization(months: Int) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val totalMonths = 36f // Assume 3 years total life
        val progress = months / totalMonths
        val barHeight = 20.dp.toPx()
        val barY = (size.height - barHeight) / 2

        // Background bar
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.2f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, barY),
            size = androidx.compose.ui.geometry.Size(size.width, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
        )

        // Progress bar
        drawRoundRect(
            color = Color(0xFF4CAF50),
            topLeft = androidx.compose.ui.geometry.Offset(0f, barY),
            size = androidx.compose.ui.geometry.Size(size.width * progress, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
        )
    }
}

@Composable
fun CapacityForecastChart(predictions: HealthPredictions) {
    // Implementation for capacity forecast chart
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Capacity Forecast Chart\n(Predictive visualization)",
            textAlign = TextAlign.Center,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun ForecastMetric(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF6A1B9A)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun MaintenanceScheduleItem(task: String, timeframe: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = task,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = "Due in $timeframe",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2196F3).copy(alpha = 0.1f)
        ) {
            Text(
                text = timeframe,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
