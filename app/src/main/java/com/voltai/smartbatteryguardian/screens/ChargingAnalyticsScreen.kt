package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Custom data models for Charging Analytics
data class AnalyticsChargingSession(
    val startTime: String,
    val endTime: String,
    val duration: String,
    val startLevel: Int,
    val endLevel: Int,
    val chargingSpeed: String,
    val chargerType: String,
    val temperature: Float,
    val efficiency: Float
)

data class AnalyticsChargingProfile(
    val name: String,
    val icon: String,
    val sessions: Int,
    val avgTime: String,
    val avgSpeed: String,
    val efficiency: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingAnalyticsScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("⚡ Overview", "📊 Analytics", "🔌 Sessions", "🧠 Insights")

    val chargingProfiles = remember { generateChargingProfiles() }
    val recentSessions = remember { generateChargingSessions() }

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
            color = Color(0xFF2196F3),
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
                            .size(56.dp)
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
                        Text(text = "⚡", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Charging Analytics",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Advanced charging insights & optimization",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    ChargingStatusIndicator()
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickStatCard("92%", "Efficiency", "📈")
                    QuickStatCard("45min", "Avg Time", "⏱️")
                    QuickStatCard("18W", "Avg Power", "🔋")
                }
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF2196F3),
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
                    item { ChargingOverviewCard() }
                    item { ChargingPatternsCard() }
                    item { ChargerTypesCard(chargingProfiles) }
                }
                1 -> {
                    item { ChargingEfficiencyChart() }
                    item { PowerDeliveryAnalysis() }
                    item { TemperatureAnalysis() }
                }
                2 -> {
                    items(recentSessions) { session ->
                        ChargingSessionCard(session)
                    }
                }
                3 -> {
                    item { AIInsightsCard() }
                    item { OptimizationRecommendations() }
                    item { PredictiveAnalysis() }
                }
            }
        }
    }
}

@Composable
fun ChargingStatusIndicator() {
    val isCharging = remember { mutableStateOf(false) }
    val animationRotation by animateFloatAsState(
        targetValue = if (isCharging.value) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .rotate(animationRotation),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔌", fontSize = 20.sp)
            }
            Text(
                text = if (isCharging.value) "Charging" else "Not Charging",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun QuickStatCard(value: String, label: String, icon: String) {
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ChargingOverviewCard() {
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
                text = "⚡ Today's Charging Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        progress = 0.75f,
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "75%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Efficiency",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                Column {
                    InfoRow("🔋 Sessions", "3")
                    InfoRow("⏱️ Total Time", "2h 15m")
                    InfoRow("📊 Energy", "15.2 Wh")
                    InfoRow("🌡️ Max Temp", "32.5°C")
                }
            }
        }
    }
}

@Composable
fun ChargingSessionCard(session: AnalyticsChargingSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${session.startTime} - ${session.endTime}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = session.chargerType,
                        fontSize = 14.sp,
                        color = Color(0xFF2196F3)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        session.efficiency >= 90 -> Color(0xFF4CAF50)
                        session.efficiency >= 75 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${session.efficiency.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            session.efficiency >= 90 -> Color(0xFF4CAF50)
                            session.efficiency >= 75 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn("Battery", "${session.startLevel}% → ${session.endLevel}%")
                InfoColumn("Speed", session.chargingSpeed)
                InfoColumn("Duration", session.duration)
                InfoColumn("Temp", "${session.temperature}°C")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

fun generateChargingProfiles(): List<AnalyticsChargingProfile> {
    return listOf(
        AnalyticsChargingProfile("USB-C PD", "🔌", 45, "1h 20m", "25W", 92.5f),
        AnalyticsChargingProfile("Wireless", "📱", 28, "2h 45m", "15W", 78.3f),
        AnalyticsChargingProfile("Quick Charge", "⚡", 15, "55m", "35W", 89.1f),
        AnalyticsChargingProfile("Standard USB", "🔋", 8, "3h 15m", "10W", 85.7f)
    )
}

fun generateChargingSessions(): List<AnalyticsChargingSession> {
    return listOf(
        AnalyticsChargingSession("09:15", "10:45", "1h 30m", 25, 80, "25W", "USB-C PD", 31.2f, 91.5f),
        AnalyticsChargingSession("14:30", "15:15", "45m", 60, 85, "15W", "Wireless", 29.8f, 78.2f),
        AnalyticsChargingSession("22:00", "23:20", "1h 20m", 20, 75, "30W", "Quick Charge", 33.1f, 88.9f),
        AnalyticsChargingSession("07:45", "08:30", "45m", 45, 70, "20W", "USB-C", 28.5f, 86.3f)
    )
}

// Placeholder composables for other sections
@Composable fun ChargingPatternsCard() { /* Implementation */ }

@Composable fun ChargerTypesCard(profiles: List<AnalyticsChargingProfile>) { /* Implementation */ }

@Composable fun ChargingEfficiencyChart() { /* Implementation */ }

@Composable fun PowerDeliveryAnalysis() { /* Implementation */ }

@Composable fun TemperatureAnalysis() { /* Implementation */ }

@Composable fun AIInsightsCard() { /* Implementation */ }

@Composable fun OptimizationRecommendations() { /* Implementation */ }

@Composable fun PredictiveAnalysis() { /* Implementation */ }
