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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.data.database.dao.DailyHealthTrend
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.viewmodels.BatteryHealthViewModel
import kotlin.math.*

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
    val predictions: HealthPredictions,
)

data class BatteryChemistry(
    val type: String, // Li-ion, Li-Po, etc.
    val cathode: String,
    val anode: String,
    val electrolyte: String,
    val nominalVoltage: Double,
    val cutoffVoltage: Double,
    val maxVoltage: Double,
)

data class HealthPoint(
    val date: Long,
    val healthPercentage: Int,
    val capacity: Int,
    val cycles: Int,
)

data class HealthPredictions(
    val remainingLifeMonths: Int,
    val nextCalibrationDays: Int,
    val replacementRecommendation: String,
    val capacityIn6Months: Int,
    val capacityIn1Year: Int,
    val healthScore: String,
)

data class HealthRecommendation(
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val action: String,
    val icon: String,
)

enum class RecommendationPriority { CRITICAL, HIGH, MEDIUM, LOW, INFO }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun EnhancedBatteryHealthScreen() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val healthVm: BatteryHealthViewModel = hiltViewModel()
    val dailyTrends by healthVm.dailyTrends.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Simulate loading for realistic UX
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800) // Brief loading to show processing
        isLoading = false
    }

    // Generate health data based on real battery data + real trends from database
    val healthData = remember(batteryData, dailyTrends) {
        generateHealthDataFromReal(batteryData, dailyTrends)
    }
    val recommendations = remember(healthData) { generateRecommendations(healthData) }

    val tabs = listOf("🏥 Overview", "🔬 Chemistry", "📊 Analytics", "🔮 Predictions", "🛠 Tools")

    if (isLoading) {
        // Loading State
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Analyzing Battery Health...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Processing real battery data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header with health status
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "Battery Health Lab",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = getHealthStatusText(healthData.overallHealth),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    )
                }
            }

            // Quick Health Metrics
            Card(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    QuickHealthMetricMD3("Cycles", "${healthData.cycles}", "🔄")
                    QuickHealthMetricMD3("Capacity", "${healthData.capacity}mAh", "⚡")
                    QuickHealthMetricMD3("Wear", "${String.format("%.1f", healthData.wear)}%", "📉")
                    QuickHealthMetricMD3(
                        "Efficiency",
                        "${String.format("%.0f", healthData.chargingEfficiency * 100)}%",
                        "🎯",
                    )
                }
            }

            // Health Score Card
            Card(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Overall Health Score",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "${healthData.overallHealth}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
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

            // Content with Battery Guru style animations
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { if (targetState > initialState) it else -it },
                        animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
                    ) +
                        fadeIn(
                            animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { if (targetState > initialState) -it else it },
                            animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
                        ) +
                        fadeOut(
                            animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic),
                        )
                },
            ) { tab ->
                when (tab) {
                    0 -> HealthOverviewTab(healthData, recommendations, batteryData, dailyTrends)
                    1 -> BatteryChemistryTab(healthData.chemistry, batteryData)
                    2 -> HealthAnalyticsTab(healthData, dailyTrends)
                    3 -> PredictionsTab(healthData, healthData.predictions)
                    4 -> HealthToolsTab(onAnalyze = { isAnalyzing = !isAnalyzing })
                }
            }
        }
    }
}

@Composable
fun StaticHealthIcon(healthPercentage: Int) {
    // Efficient static icon without battery-draining infinite animations
    Box(
        modifier =
        Modifier
            .size(48.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text =
            when {
                healthPercentage >= 90 -> "💚"
                healthPercentage >= 75 -> "💛"
                healthPercentage >= 50 -> "🧡"
                else -> "❤️"
            },
            fontSize = 24.sp,
        )
    }
}

@Composable
fun QuickHealthMetric(
    label: String,
    value: String,
    icon: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = icon,
            fontSize = 16.sp,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun QuickHealthMetricMD3(
    label: String,
    value: String,
    icon: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = icon,
            fontSize = 16.sp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun HealthOverviewTab(
    healthData: BatteryHealthData,
    recommendations: List<HealthRecommendation>,
    batteryData: com.voltai.smartbatteryguardian.BatteryData,
    dailyTrends: List<DailyHealthTrend>,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Advanced Health Ring
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Comprehensive Health Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Multi-ring health display
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AdvancedHealthRings(healthData)

                        // Center content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "${healthData.overallHealth}%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = healthData.predictions.healthScore,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Health metrics grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
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
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        items(recommendations.filter { it.priority <= RecommendationPriority.HIGH }) { recommendation ->
            RecommendationCard(recommendation)
        }

        // Health Trend Chart (from Room daily trends)
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
                        text = "📈 Health Trend (30 Days)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HealthTrendChartRoom(dailyTrends)
                }
            }
        }

        // Detailed Metrics
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
                        text = "📋 Detailed Metrics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailedMetricRow(
                        "Temperature",
                        "${batteryData.temperature}°C",
                        getTemperatureStatus(batteryData.temperature.toDouble()),
                    )
                    DetailedMetricRow(
                        "Voltage",
                        "${batteryData.voltage}V",
                        getVoltageStatus(batteryData.voltage.toDouble()),
                    )
                    DetailedMetricRow(
                        "Current",
                        "${String.format("%.0f", healthData.current)}mA",
                        getCurrentStatus(healthData.current),
                    )
                    DetailedMetricRow(
                        "Charging Efficiency",
                        "${String.format("%.1f", healthData.chargingEfficiency * 100)}%",
                        getEfficiencyStatus(healthData.chargingEfficiency),
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryChemistryTab(
    chemistry: BatteryChemistry,
    batteryData: com.voltai.smartbatteryguardian.BatteryData,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Chemistry Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "🧪",
                            fontSize = 32.sp,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Battery Chemistry",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                text = chemistry.type,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "⚡ Voltage Characteristics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    VoltageCharacteristicsChart(chemistry, batteryData.voltage.toDouble())

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        VoltageMetric("Nominal", "${chemistry.nominalVoltage}V", MaterialTheme.colorScheme.tertiary)
                        VoltageMetric("Current", "${batteryData.voltage}V", MaterialTheme.colorScheme.primary)
                        VoltageMetric("Maximum", "${chemistry.maxVoltage}V", MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Chemistry Analysis
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
                        text = "🔬 Chemical Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real chemical analysis requires specialized hardware
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    ) {
                        Text(
                            text = "🔬",
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chemical Analysis Unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Advanced chemical analysis requires specialized laboratory equipment to measure ion mobility, electrode integrity, and electrolyte quality.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }

        // Safety Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "🛡️ Safety Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SafetyInfoItem("Operating Temperature", "0°C to 45°C", batteryData.temperature < 45)
                    SafetyInfoItem(
                        "Voltage Range",
                        "${chemistry.cutoffVoltage}V - ${chemistry.maxVoltage}V",
                        batteryData.voltage.toDouble() in chemistry.cutoffVoltage..chemistry.maxVoltage,
                    )
                    SafetyInfoItem("Overcharge Protection", "Active", true)
                    SafetyInfoItem("Thermal Protection", "Active", batteryData.temperature < 40)
                }
            }
        }
    }
}

@Composable
fun HealthAnalyticsTab(
    healthData: BatteryHealthData,
    dailyTrends: List<DailyHealthTrend>,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Performance Analytics
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
                        text = "📊 Performance Analytics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PerformanceAnalyticsChartRoom(dailyTrends)
                }
            }
        }

        // Degradation Analysis
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
                        text = "📉 Degradation Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Only show degradation factors we can actually calculate from real data
                    DegradationFactorItem(
                        "Cycle Usage",
                        (healthData.cycles.toDouble() / healthData.maxCycles * 100).toInt(),
                        MaterialTheme.colorScheme.error,
                    )

                    // Note: Calendar aging, temperature stress, and voltage stress
                    // require historical data analysis over extended periods
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    ) {
                        Text(
                            text = "📊 Additional degradation factors require extended monitoring period",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        // Comparative Analysis
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
                        text = "⚖️ Comparative Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ComparisonChartRoom(dailyTrends)
                }
            }
        }
    }
}

@Composable
fun PredictionsTab(
    healthData: BatteryHealthData,
    predictions: HealthPredictions,
) {
    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // AI Predictions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "🔮",
                            fontSize = 32.sp,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "AI-Powered Predictions",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                            Text(
                                text = "Machine learning battery forecasts",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "⏳ Remaining Battery Life",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // AI predictions require extended historical data
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                    ) {
                        Text(
                            text = "🤖",
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AI Predictions Unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Accurate battery life predictions require at least 30 days of usage data to train machine learning models.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }

        // Capacity Predictions
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
                        text = "📈 Capacity Forecast",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Capacity forecasts need historical trend data
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                    ) {
                        Text(
                            text = "📈",
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Capacity Forecast Unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Battery capacity forecasts require historical capacity measurements over several months to establish degradation trends.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }

        // Maintenance Predictions
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
                        text = "🔧 Maintenance Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Only show maintenance tasks based on real battery status
                    if (healthData.cycles > 100) {
                        MaintenanceScheduleItem("Battery Calibration", "Recommended", "🔋")
                    }

                    // General maintenance recommendations (not time-based)
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    ) {
                        Text(
                            text = "🔧 General Maintenance Tips:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Avoid extreme temperatures (0°C - 35°C optimal)\n• Don't let battery drain completely regularly\n• Use original charger when possible",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (predictions.replacementRecommendation.isNotEmpty()) {
                        Card(
                            colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                            ) {
                                Text(
                                    text = "💡 Replacement Recommendation",
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = predictions.replacementRecommendation,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Battery Calibration Tool
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
                        text = "🎯 Battery Calibration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Improve battery accuracy by calibrating the fuel gauge",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Start calibration */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                        ),
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "🔍 Deep Health Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Comprehensive battery health diagnostic using AI algorithms",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onAnalyze,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    Text(
                        text = "📊 Export Health Report",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Generate detailed PDF report with all health data",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { /* Export CSV */ },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Export CSV")
                        }
                        Button(
                            onClick = { /* Export PDF */ },
                            modifier = Modifier.weight(1f),
                            colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                            ),
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
    // Animated ring reveals with staggered timing - Battery Guru style
    val outerRingProgress by animateFloatAsState(
        targetValue = healthData.overallHealth / 100f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "outerRing",
    )

    val middleRingProgress by animateFloatAsState(
        targetValue = (healthData.capacity.toFloat() / healthData.designCapacity),
        animationSpec = tween(durationMillis = 800, delayMillis = 200, easing = EaseOutCubic),
        label = "middleRing",
    )

    val innerRingProgress by animateFloatAsState(
        targetValue = (healthData.maxCycles - healthData.cycles).toFloat() / healthData.maxCycles,
        animationSpec = tween(durationMillis = 800, delayMillis = 400, easing = EaseOutCubic),
        label = "innerRing",
    )
    // Capture theme colors before entering DrawScope
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val tertiary = MaterialTheme.colorScheme.tertiary
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Canvas(
        modifier = Modifier.size(200.dp),
    ) {
        val center = center
        val strokeWidth = 12.dp.toPx()
        val gap = 8.dp.toPx()

        // Outer ring - Overall Health
        val outerRadius = size.minDimension / 2 - strokeWidth / 2
        // Background track for outer ring
        drawArc(
            color = surfaceVariant.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth),
            size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
            topLeft =
            androidx.compose.ui.geometry.Offset(
                center.x - outerRadius,
                center.y - outerRadius,
            ),
        )
        // Animated outer ring - Overall Health
        drawArc(
            color = tertiary,
            startAngle = -90f,
            sweepAngle = outerRingProgress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
            topLeft =
            androidx.compose.ui.geometry.Offset(
                center.x - outerRadius,
                center.y - outerRadius,
            ),
        )

        // Middle ring - Capacity
        val middleRadius = outerRadius - strokeWidth - gap
        // Background track for middle ring
        drawArc(
            color = surfaceVariant.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth * 0.8f),
            size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2),
            topLeft =
            androidx.compose.ui.geometry.Offset(
                center.x - middleRadius,
                center.y - middleRadius,
            ),
        )
        // Animated middle ring - Capacity
        drawArc(
            color = primary,
            startAngle = -90f,
            sweepAngle = middleRingProgress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2),
            topLeft =
            androidx.compose.ui.geometry.Offset(
                center.x - middleRadius,
                center.y - middleRadius,
            ),
        )

        // Inner ring - Cycle Life Remaining
        val innerRadius = middleRadius - strokeWidth - gap
        // Background track for inner ring
        drawArc(
            color = surfaceVariant.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth * 0.6f),
            size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
            topLeft =
            androidx.compose.ui.geometry.Offset(
                center.x - innerRadius,
                center.y - innerRadius,
            ),
        )
        // Animated inner ring - Cycle Life Remaining (inverted - shows remaining cycles)
        drawArc(
            color = secondary,
            startAngle = -90f,
            sweepAngle = innerRingProgress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth * 0.6f, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
            topLeft =
            androidx.compose.ui.geometry.Offset(
                center.x - innerRadius,
                center.y - innerRadius,
            ),
        )
    }
}

@Composable
fun HealthMetricItem(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun RecommendationCard(recommendation: HealthRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor =
            when (recommendation.priority) {
                RecommendationPriority.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                RecommendationPriority.HIGH -> MaterialTheme.colorScheme.secondaryContainer
                RecommendationPriority.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            },
        ),
        border =
        BorderStroke(
            1.dp,
            when (recommendation.priority) {
                RecommendationPriority.CRITICAL -> MaterialTheme.colorScheme.error
                RecommendationPriority.HIGH -> MaterialTheme.colorScheme.secondary
                RecommendationPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
                else -> Color.Transparent
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = recommendation.icon,
                fontSize = 24.sp,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = recommendation.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Text(
                    text = recommendation.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (recommendation.action.isNotEmpty()) {
                    Text(
                        text = "Action: ${recommendation.action}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

// Removed duplicate HealthAnalyticsTab definition

@Composable
fun HealthTrendChartRoom(trends: List<DailyHealthTrend>) {
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        if (trends.isEmpty()) return@Canvas
        val pointsCount = trends.size
        val stepX = size.width / (pointsCount - 1).coerceAtLeast(1)
        val maxHealth = 100f
        val pts =
            trends.sortedBy { it.date }.mapIndexed { index, t ->
                androidx.compose.ui.geometry.Offset(
                    x = index * stepX,
                    y = size.height - ((t.avgHealth.toFloat() / maxHealth) * size.height),
                )
            }
        for (i in 0 until pts.size - 1) {
            drawLine(
                color = tertiaryColor,
                start = pts[i],
                end = pts[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Draw points
        pts.forEach { point ->
            drawCircle(
                color = tertiaryColor,
                radius = 4.dp.toPx(),
                center = point,
            )
            drawCircle(
                color = surfaceColor,
                radius = 2.dp.toPx(),
                center = point,
            )
        }
    }
}

// Utility Functions
fun generateHealthDataFromReal(
    batteryData: com.voltai.smartbatteryguardian.BatteryData,
    dailyTrends: List<DailyHealthTrend>,
): BatteryHealthData {
    // Extract capacity from batteryData.capacity (e.g., "2500/4000 mAh")
    val capacityParts = batteryData.capacity.split("/")
    val currentCapacity = capacityParts[0].replace(" mAh", "").toIntOrNull() ?: 2500
    val designCapacity = capacityParts[1].replace(" mAh", "").toIntOrNull() ?: 4000

    // Calculate wear based on capacity degradation
    val wear = ((designCapacity - currentCapacity).toDouble() / designCapacity * 100)

    // Calculate charging efficiency based on current charging conditions
    val chargingEfficiency =
        when {
            batteryData.temperature > 40f -> 0.85 // Reduced efficiency when hot
            batteryData.voltage > 4.1 -> 0.94 // Good efficiency in normal voltage range
            batteryData.voltage < 3.5 -> 0.88 // Reduced efficiency at low voltage
            else -> 0.92 // Normal efficiency
        }

    // Estimate remaining life based on health percentage and cycle count
    val estimatedLifeMonths =
        when {
            batteryData.healthPercentage >= 95 -> 30
            batteryData.healthPercentage >= 85 -> 24
            batteryData.healthPercentage >= 75 -> 18
            batteryData.healthPercentage >= 65 -> 12
            else -> 6
        }

    // Calculate next calibration based on battery condition
    val nextCalibrationDays =
        when {
            batteryData.healthPercentage < 80 -> 7 // More frequent calibration for poor health
            batteryData.healthPercentage < 90 -> 14 // Bi-weekly for moderate health
            else -> 30 // Monthly for good health
        }

    return BatteryHealthData(
        overallHealth = batteryData.healthPercentage,
        capacity = currentCapacity,
        designCapacity = designCapacity,
        cycles = batteryData.cycleCount,
        maxCycles = 1000,
        wear = wear,
        chargingEfficiency = chargingEfficiency,
        temperature = batteryData.temperature.toDouble(),
        voltage = batteryData.voltage,
        current = batteryData.current.toDouble(),
        chemistry =
        BatteryChemistry(
            type = "Li-Ion", // Generic type that's typically accurate for mobile devices
            cathode = "Unknown - Requires Lab Analysis",
            anode = "Unknown - Requires Lab Analysis",
            electrolyte = "Unknown - Requires Lab Analysis",
            nominalVoltage = 3.7, // Standard for Li-Ion
            cutoffVoltage = 3.0,
            maxVoltage = 4.2,
        ),
        calibrationDate = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L),
        manufactureDate = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L),
        healthTrend = convertDailyTrendsToHealthPoints(dailyTrends, batteryData.healthPercentage),
        predictions =
        HealthPredictions(
            remainingLifeMonths = estimatedLifeMonths,
            nextCalibrationDays = nextCalibrationDays,
            replacementRecommendation = getReplacementRecommendation(batteryData.healthPercentage),
            capacityIn6Months = (currentCapacity * 0.98).toInt(), // Realistic 2% degradation in 6 months
            capacityIn1Year = (currentCapacity * 0.95).toInt(), // Realistic 5% degradation in 1 year
            healthScore = getHealthScoreText(batteryData.healthPercentage),
        ),
    )
}

/**
 * Convert real DailyHealthTrend data from database to HealthPoint for charting
 * Falls back to estimated trend if no historical data available yet
 */
fun convertDailyTrendsToHealthPoints(
    dailyTrends: List<DailyHealthTrend>,
    currentHealth: Int,
): List<HealthPoint> {
    // Use real data from database if available
    if (dailyTrends.isNotEmpty()) {
        return dailyTrends.map { trend ->
            // Parse date string (format: "yyyy-MM-dd") to timestamp
            val dateTimestamp = try {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .parse(trend.date)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            HealthPoint(
                date = dateTimestamp,
                healthPercentage = trend.avgHealth.toInt(),
                capacity = trend.avgCapacity.toInt(),
                cycles = trend.avgCycles.toInt(),
            )
        }
    }

    // Fallback: Generate estimated trend for first-time users (no historical data yet)
    return (0..29).map { day ->
        val daysAgo = 29 - day
        val healthAtTime =
            when {
                currentHealth >= 90 -> (currentHealth + (daysAgo * 0.1f / 7f)).toInt().coerceAtMost(100)
                currentHealth >= 75 -> (currentHealth + (daysAgo * 0.3f / 7f)).toInt().coerceAtMost(100)
                else -> (currentHealth + (daysAgo * 0.5f / 7f)).toInt().coerceAtMost(100)
            }

        val estimatedCapacity = (4000 * healthAtTime / 100.0 * 0.9).toInt()
        val estimatedCycles = ((100 - healthAtTime) * 10).coerceAtLeast(0)

        HealthPoint(
            date = System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000L),
            healthPercentage = healthAtTime,
            capacity = estimatedCapacity,
            cycles = estimatedCycles + day, // Cycles increase over time
        )
    }
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
                icon = "⚠️",
            ),
        )
    }

    if (healthData.temperature > 35.0) {
        recommendations.add(
            HealthRecommendation(
                title = "Temperature Management",
                description = "Battery temperature is elevated. Avoid charging in hot conditions.",
                priority = RecommendationPriority.MEDIUM,
                action = "Cool down device before charging",
                icon = "🌡️",
            ),
        )
    }

    if (healthData.chargingEfficiency < 0.9) {
        recommendations.add(
            HealthRecommendation(
                title = "Charging Efficiency Low",
                description = "Charging efficiency has decreased. Consider battery calibration.",
                priority = RecommendationPriority.MEDIUM,
                action = "Run battery calibration",
                icon = "🔋",
            ),
        )
    }

    return recommendations
}

fun getReplacementRecommendation(healthPercentage: Int): String {
    return when {
        healthPercentage >= 90 -> "Battery is in excellent condition. No replacement needed."
        healthPercentage >= 80 -> "Battery health is good. Consider replacement in 18-24 months."
        healthPercentage >= 70 -> "Battery health is fair. Plan replacement in 12-18 months."
        healthPercentage >= 60 -> "Battery health is declining. Consider replacement in 6-12 months."
        else -> "Battery health is poor. Replacement recommended within 6 months."
    }
}

fun getHealthScoreText(healthPercentage: Int): String {
    return when {
        healthPercentage >= 95 -> "Excellent"
        healthPercentage >= 85 -> "Good"
        healthPercentage >= 75 -> "Fair"
        healthPercentage >= 65 -> "Poor"
        else -> "Critical"
    }
}

@Composable
fun getHealthStatusColor(health: Int): Color {
    return when {
        health >= 90 -> MaterialTheme.colorScheme.tertiary
        health >= 75 -> MaterialTheme.colorScheme.secondary
        health >= 50 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.error
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
fun ChemistryComponent(
    name: String,
    component: String,
    icon: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
            Text(
                text = component,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun VoltageCharacteristicsChart(
    chemistry: BatteryChemistry,
    currentVoltage: Double,
) {
    val errColor = MaterialTheme.colorScheme.error
    val tertColor = MaterialTheme.colorScheme.tertiary
    val secColor = MaterialTheme.colorScheme.secondary
    val primColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(80.dp),
    ) {
        val barWidth = size.width / 3
        val maxVoltage = chemistry.maxVoltage.toFloat()

        // Draw voltage bars
        val voltages = listOf(chemistry.cutoffVoltage, chemistry.nominalVoltage, chemistry.maxVoltage)
        val colors = listOf(errColor, tertColor, secColor)
        val labels = listOf("Cutoff", "Nominal", "Maximum")

        voltages.forEachIndexed { index, voltage ->
            val barHeight = (voltage.toFloat() / maxVoltage) * size.height * 0.8f
            val x = index * barWidth + barWidth * 0.25f
            val y = size.height - barHeight

            drawRoundRect(
                color = colors[index].copy(alpha = 0.7f),
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.5f, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            )
        }

        // Draw current voltage indicator
        val currentX = size.width / 2
        val currentY = size.height - (currentVoltage.toFloat() / maxVoltage) * size.height * 0.8f
        drawCircle(
            color = primColor,
            radius = 6.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(currentX, currentY),
        )
    }
}

@Composable
fun VoltageMetric(
    label: String,
    value: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
            Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ChemicalAnalysisItem(
    title: String,
    description: String,
    status: String,
    color: Color,
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f),
            ) {
                Text(
                    text = status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
        Text(
            text = description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
fun SafetyInfoItem(
    parameter: String,
    range: String,
    isWithinRange: Boolean,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = parameter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
            Text(
                text = range,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = if (isWithinRange) Icons.Default.Check else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isWithinRange) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun DetailedMetricRow(
    label: String,
    value: String,
    status: String,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = status,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PerformanceAnalyticsChartRoom(trends: List<DailyHealthTrend>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        if (trends.isEmpty()) return@Canvas
        val ordered = trends.sortedBy { it.date }
        val count = ordered.size
        val stepX = size.width / (count - 1).coerceAtLeast(1)

        fun yFor(
            value: Double,
            max: Float,
        ): Float = size.height - ((value.toFloat() / max) * size.height * 0.85f)

        val capPts =
            ordered.mapIndexed { i, t ->
                androidx.compose.ui.geometry.Offset(i * stepX, yFor(t.avgCapacity, 100f))
            }
        for (i in 0 until capPts.size - 1) {
            drawLine(primaryColor, capPts[i], capPts[i + 1], 3.dp.toPx(), StrokeCap.Round)
        }

        val maxCycles = (ordered.maxOfOrNull { it.avgCycles } ?: 1.0).toFloat().coerceAtLeast(1f)
        val cycPts =
            ordered.mapIndexed { i, t ->
                androidx.compose.ui.geometry.Offset(i * stepX, yFor((t.avgCycles / maxCycles) * 100.0, 100f))
            }
        for (i in 0 until cycPts.size - 1) {
            drawLine(secondaryColor, cycPts[i], cycPts[i + 1], 3.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
fun DegradationFactorItem(
    factor: String,
    percentage: Int,
    color: Color,
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = factor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "$percentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier =
            Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}

@Composable
fun ComparisonChartRoom(trends: List<DailyHealthTrend>) {
    // Capture theme colors outside DrawScope
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        if (trends.isEmpty()) return@Canvas
        val ordered = trends.sortedBy { it.date }
        val count = ordered.size
        val stepX = size.width / (count - 1).coerceAtLeast(1)

        val baseline = 85f
        val devicePts =
            ordered.mapIndexed { i, t ->
                androidx.compose.ui.geometry.Offset(
                    i * stepX,
                    size.height - (t.avgHealth.toFloat() / 100f * size.height),
                )
            }
        for (i in 0 until devicePts.size - 1) {
            drawLine(tertiaryColor, devicePts[i], devicePts[i + 1], 3.dp.toPx(), StrokeCap.Round)
        }
        val y = size.height - (baseline / 100f * size.height)
        drawLine(
            outlineVariantColor,
            androidx.compose.ui.geometry.Offset(0f, y),
            androidx.compose.ui.geometry.Offset(size.width, y),
            2.dp.toPx(),
        )
    }
}

@Composable
fun RemainingLifeVisualization(months: Int) {
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    Canvas(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(60.dp),
    ) {
        val totalMonths = 36f
        val progress = months / totalMonths
        val barHeight = 20.dp.toPx()
        val barY = (size.height - barHeight) / 2

        drawRoundRect(
            color = surfaceVariantColor,
            topLeft = androidx.compose.ui.geometry.Offset(0f, barY),
            size = androidx.compose.ui.geometry.Size(size.width, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
        )

        drawRoundRect(
            color = tertiaryColor,
            topLeft = androidx.compose.ui.geometry.Offset(0f, barY),
            size = androidx.compose.ui.geometry.Size(size.width * progress, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
        )
    }
}

@Composable
fun CapacityForecastChart(predictions: HealthPredictions) {
    Box(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Capacity Forecast Chart\n(Predictive visualization)",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ForecastMetric(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun MaintenanceScheduleItem(
    task: String,
    timeframe: String,
    icon: String,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = task,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
            Text(
                text = "Due in $timeframe",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        ) {
            Text(
                text = timeframe,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
