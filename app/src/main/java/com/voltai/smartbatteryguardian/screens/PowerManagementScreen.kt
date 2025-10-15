package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.ai.OptimizationImpact
import com.voltai.smartbatteryguardian.ai.OptimizationRecommendation
import com.voltai.smartbatteryguardian.ai.rememberAIBatteryOptimizer
import com.voltai.smartbatteryguardian.rememberBatteryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerManagementScreen() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val aiOptimizer = rememberAIBatteryOptimizer(context)
    val recommendations by remember { mutableStateOf(aiOptimizer.analyzeBatteryUsage(batteryData)) }
    val prediction = aiOptimizer.getPredictedBatteryLife(batteryData)
    val optimizationScore = aiOptimizer.getOptimizationScore()

    var selectedPowerMode by remember { mutableStateOf("Balanced") }
    var autoOptimizationEnabled by remember { mutableStateOf(false) }

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Power Management",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Intelligent battery optimization",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Optimization Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "🤖 AI Optimization Score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp),
                ) {
                    CircularProgressIndicator(
                        progress = optimizationScore / 100f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 12.dp,
                        color = getScoreColor(optimizationScore),
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${optimizationScore.toInt()}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = getScoreColor(optimizationScore),
                        )
                        Text(
                            text = getScoreLabel(optimizationScore),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your device is ${getOptimizationDescription(optimizationScore)}",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Battery Prediction Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
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
                        text = "🔮 AI Battery Prediction",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = "${(prediction.confidenceLevel * 100).toInt()}% Confidence",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    PredictionMetric(
                        icon = "⏰",
                        value = "${prediction.remainingHours.toInt()}h ${((prediction.remainingHours % 1) * 60).toInt()}m",
                        label = "Remaining Time",
                    )

                    PredictionMetric(
                        icon = "🧠",
                        value = "${(prediction.learningProgress * 100).toInt()}%",
                        label = "AI Learning",
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = prediction.predictionBasis,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Power Modes
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "⚡ Intelligent Power Modes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                val powerModes =
                    listOf(
                        PowerMode("🚀 Performance", "Maximum performance, faster battery drain", "Performance"),
                        PowerMode("⚖️ Balanced", "Optimal balance of performance and efficiency", "Balanced"),
                        PowerMode("🔋 Power Saver", "Extended battery life with reduced performance", "PowerSaver"),
                        PowerMode("🧠 AI Adaptive", "AI learns and adapts to your usage patterns", "AI"),
                    )

                powerModes.forEach { mode ->
                    PowerModeItem(
                        mode = mode,
                        isSelected = selectedPowerMode == mode.id,
                        onSelect = { selectedPowerMode = mode.id },
                    )
                    if (mode != powerModes.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Auto Optimization Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors =
            CardDefaults.cardColors(
                containerColor =
                if (autoOptimizationEnabled) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
        ) {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🤖 AI Auto-Optimization",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Let AI automatically optimize your battery based on usage patterns",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                    )
                }

                Switch(
                    checked = autoOptimizationEnabled,
                    onCheckedChange = {
                        autoOptimizationEnabled = it
                        if (it) aiOptimizer.enableAutoOptimization()
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Recommendations
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "💡 AI Optimization Recommendations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                recommendations.take(3).forEachIndexed { index, recommendation ->
                    var isApplied by remember { mutableStateOf(false) }
                    RecommendationItem(
                        recommendation = recommendation.copy(isApplied = isApplied),
                        onApply = {
                            isApplied = true
                            // Simulate optimization action
                            recommendation.action()
                        },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (recommendations.size > 3) {
                    TextButton(
                        onClick = { /* Show all recommendations */ },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("View ${recommendations.size - 3} More Recommendations")
                    }
                }
            }
        }
    }
}

data class PowerMode(
    val name: String,
    val description: String,
    val id: String,
)

@Composable
fun PredictionMetric(
    icon: String,
    value: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
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
fun PowerModeItem(
    mode: PowerMode,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors =
        CardDefaults.cardColors(
            containerColor =
            if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = mode.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
            )
        }
    }
}

@Composable
fun RecommendationItem(
    recommendation: OptimizationRecommendation,
    onApply: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = getImpactColor(recommendation.impact).copy(alpha = 0.1f),
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
                        text = recommendation.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getImpactColor(recommendation.impact),
                ) {
                    Text(
                        text = getImpactLabel(recommendation.impact),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick =
                if (!recommendation.isApplied) {
                    onApply
                } else {
                    {}
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                    if (recommendation.isApplied) {
                        Color(
                            0xFF4CAF50,
                        )
                    } else {
                        getImpactColor(recommendation.impact)
                    },
                ),
                enabled = !recommendation.isApplied,
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (recommendation.isApplied) {
                        Text("✓ ", fontSize = 14.sp)
                    }
                    Text(
                        if (recommendation.isApplied) "Optimization Applied" else "Apply Optimization",
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun getScoreColor(score: Float): Color {
    return when {
        score >= 80 -> MaterialTheme.colorScheme.tertiary
        score >= 60 -> MaterialTheme.colorScheme.tertiary
        score >= 40 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
}

private fun getScoreLabel(score: Float): String {
    return when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Good"
        score >= 40 -> "Fair"
        else -> "Needs Work"
    }
}

private fun getOptimizationDescription(score: Float): String {
    return when {
        score >= 80 -> "excellently optimized for maximum battery life"
        score >= 60 -> "well optimized with room for minor improvements"
        score >= 40 -> "moderately optimized but could benefit from AI recommendations"
        else -> "poorly optimized and needs immediate attention"
    }
}

@Composable
private fun getImpactColor(impact: OptimizationImpact): Color {
    return when (impact) {
        OptimizationImpact.LOW -> MaterialTheme.colorScheme.primary
        OptimizationImpact.MEDIUM -> MaterialTheme.colorScheme.tertiary
        OptimizationImpact.HIGH -> MaterialTheme.colorScheme.secondary
        OptimizationImpact.CRITICAL -> MaterialTheme.colorScheme.error
    }
}

private fun getImpactLabel(impact: OptimizationImpact): String {
    return when (impact) {
        OptimizationImpact.LOW -> "1-5%"
        OptimizationImpact.MEDIUM -> "5-15%"
        OptimizationImpact.HIGH -> "15-25%"
        OptimizationImpact.CRITICAL -> "25%+"
    }
}
