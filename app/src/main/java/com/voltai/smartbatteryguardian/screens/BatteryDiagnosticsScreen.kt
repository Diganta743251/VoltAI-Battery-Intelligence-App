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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.rememberBatteryData
import java.text.DateFormat
import java.util.*
import kotlin.math.*

// Data Models
data class DiagnosticResult(
    val category: DiagnosticCategory,
    val title: String,
    val value: String,
    val status: DiagnosticStatus,
    val description: String,
    val recommendation: String?,
    val severity: DiagnosticSeverity,
    val icon: String,
    val normalRange: String,
    val lastUpdated: Long,
)

enum class DiagnosticCategory {
    VOLTAGE,
    TEMPERATURE,
    CYCLE_COUNT,
    CAPACITY,
    CHARGING,
    PERFORMANCE,
    SAFETY,
}

enum class DiagnosticStatus {
    OPTIMAL,
    GOOD,
    WARNING,
    CRITICAL,
    UNKNOWN,
}

enum class DiagnosticSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryDiagnosticsScreen() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🔬 Diagnostics", "📊 Voltage", "🌡️ Temperature", "🔄 Cycles", "🧪 Tests")

    var isRunningDiagnostics by remember { mutableStateOf(false) }

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors =
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ),
            ),
    ) {
        // Premium Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Diagnostic Scanner Animation
                    DiagnosticScannerIcon(isScanning = isRunningDiagnostics)

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Diagnostics",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Advanced battery analysis & health monitoring",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        )
                    }

                    // Diagnostic Status
                    DiagnosticStatusCard()
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Diagnostic Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    val voltageText =
                        if (batteryData.voltage > 0.0) {
                            String.format(
                                "%.2fV",
                                batteryData.voltage,
                            )
                        } else {
                            "—"
                        }
                    val tempText = if (batteryData.temperature > 0f) "${batteryData.temperature.toInt()}°C" else "—"
                    val cyclesText = if (batteryData.cycleCount > 0) batteryData.cycleCount.toString() else "—"
                    val healthText = if (batteryData.healthPercentage in 0..100) "${batteryData.healthPercentage}%" else "—"

                    QuickStatCard(voltageText, "Voltage", "⚡", MaterialTheme.colorScheme.tertiary)
                    QuickStatCard(tempText, "Temperature", "🌡️", MaterialTheme.colorScheme.secondary)
                    QuickStatCard(cyclesText, "Cycles", "🔄", MaterialTheme.colorScheme.primary)
                    QuickStatCard(healthText, "Health", "❤️", MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(vertical = 12.dp),
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }

        // Content
        LazyColumn(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        DiagnosticsOverviewCard(
                            isRunningDiagnostics = isRunningDiagnostics,
                            onStartDiagnostics = { isRunningDiagnostics = true },
                        )
                    }
                    item { GeneralDiagnosticResultsCard() }
                }
                1 -> {
                    item { VoltageAnalysisCard() }
                    item { VoltageHistoryChart() }
                }
                2 -> {
                    item { TemperatureAnalysisCard() }
                    item { TemperatureHistoryChart() }
                }
                3 -> {
                    item { CycleAnalysisCard() }
                    item { CycleHistoryCard() }
                }
                4 -> {
                    item { DiagnosticTestsCard() }
                }
            }
        }
    }
}

@Composable
fun DiagnosticScannerIcon(isScanning: Boolean) {
    // Static, battery-efficient diagnostic scanner indicator
    Box(
        modifier =
        Modifier
            .size(64.dp)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                CircleShape,
            )
            .border(
                2.dp,
                if (isScanning) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                },
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "🔬", fontSize = 32.sp)

        if (isScanning) {
            // Static scanning rings indicating active state
            repeat(3) { index ->
                Box(
                    modifier =
                    Modifier
                        .size((40 + index * 8).dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f - index * 0.1f),
                            CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
fun DiagnosticStatusCard() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val updatedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(batteryData.lastUpdated))
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                    Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Monitoring",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }

            Text(
                text = "Updated: $updatedTime",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
fun QuickStatCard(
    value: String,
    label: String,
    icon: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun DiagnosticsOverviewCard(
    isRunningDiagnostics: Boolean,
    onStartDiagnostics: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🔬 Diagnostic Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onStartDiagnostics,
                    enabled = !isRunningDiagnostics,
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    if (isRunningDiagnostics) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scanning...")
                    } else {
                        Text("Run Diagnostics")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Status Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatusSummaryItem("Critical", 0, MaterialTheme.colorScheme.error)
                StatusSummaryItem("Warning", 1, MaterialTheme.colorScheme.secondary)
                StatusSummaryItem("Optimal", 4, MaterialTheme.colorScheme.tertiary)
            }

            if (isRunningDiagnostics) {
                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Running comprehensive battery analysis...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun StatusSummaryItem(
    label: String,
    count: Int,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun GeneralDiagnosticResultsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "🔍 Diagnostic Results",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current
            val batteryData by rememberBatteryData(context)
            val voltageText = if (batteryData.voltage > 0.0) String.format("%.2fV", batteryData.voltage) else "—"
            val tempText = if (batteryData.temperature > 0f) "${batteryData.temperature.toInt()}°C" else "—"
            val cyclesText = if (batteryData.cycleCount > 0) batteryData.cycleCount.toString() else "—"

            DiagnosticItem(
                icon = "⚡",
                title = "Battery Voltage",
                value = voltageText,
                status = "—",
                statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
                description = "",
            )

            Spacer(modifier = Modifier.height(12.dp))

            DiagnosticItem(
                icon = "🌡️",
                title = "Temperature",
                value = tempText,
                status = "—",
                statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
                description = "",
            )

            Spacer(modifier = Modifier.height(12.dp))

            DiagnosticItem(
                icon = "🔄",
                title = "Charge Cycles",
                value = cyclesText,
                status = "—",
                statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
                description = "",
            )
        }
    }
}

@Composable
fun DiagnosticItem(
    icon: String,
    title: String,
    value: String,
    status: String,
    statusColor: Color,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, fontSize = 20.sp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.1f),
            ) {
                Text(
                    text = status,
                    fontSize = 10.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
fun VoltageAnalysisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "⚡ Voltage Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(20.dp))

            val context = LocalContext.current
            val batteryData by rememberBatteryData(context)
            val voltageText = if (batteryData.voltage > 0.0) String.format("%.2fV", batteryData.voltage) else "—"

            // Voltage gauge placeholder
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = voltageText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(
                        text = "Current Voltage",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Voltage metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                MetricItem("Power Source", batteryData.powerSource, "🔌")
                MetricItem("Status", batteryData.chargingStatus, "⚡")
                MetricItem("Current", "${batteryData.current} mA", "🔋")
            }
        }
    }
}

@Composable
fun TemperatureAnalysisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "🌡️ Temperature Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Temperature gauge placeholder
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val context = LocalContext.current
                    val batteryData by rememberBatteryData(context)
                    val tempText = if (batteryData.temperature > 0f) "${batteryData.temperature.toInt()}°C" else "—"
                    Text(
                        text = tempText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = "Current Temperature",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Temperature metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                val context = LocalContext.current
                val batteryData by rememberBatteryData(context)
                val tempTextRow = if (batteryData.temperature > 0f) "${batteryData.temperature.toInt()}°C" else "—"
                val stateTextRow =
                    when {
                        batteryData.temperature > 45f -> "Hot"
                        batteryData.temperature > 35f -> "Warm"
                        else -> "Normal"
                    }
                val healthTextRow = if (batteryData.healthPercentage in 0..100) "${batteryData.healthPercentage}%" else "—"

                MetricItem("Current", tempTextRow, "🌡️")
                MetricItem("State", stateTextRow, "📊")
                MetricItem("Health", healthTextRow, "❤️")
            }
        }
    }
}

@Composable
fun CycleAnalysisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "🔄 Cycle Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Cycle progress indicator
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val context = LocalContext.current
                val batteryData by rememberBatteryData(context)
                val progressNormalized = ((batteryData.cycleCount.coerceAtLeast(0)) / 1000f).coerceIn(0f, 1f)
                CircularProgressIndicator(
                    progress = progressNormalized,
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val cyclesText = if (batteryData.cycleCount > 0) batteryData.cycleCount.toString() else "—"
                    Text(
                        text = cyclesText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Cycles",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cycle metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                val context = LocalContext.current
                val batteryData by rememberBatteryData(context)
                val cyclesTextRow = if (batteryData.cycleCount > 0) batteryData.cycleCount.toString() else "—"
                val healthTextRow = if (batteryData.healthPercentage in 0..100) "${batteryData.healthPercentage}%" else "—"
                MetricItem("Cycles", cyclesTextRow, "🔄")
                MetricItem("Health", healthTextRow, "❤️")
                MetricItem("Status", batteryData.chargingStatus, "⚡")
            }
        }
    }
}

@Composable
fun DiagnosticTestsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "🧪 Diagnostic Tests",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sample diagnostic tests
            TestItem("Voltage Stability Test", "—", MaterialTheme.colorScheme.onSurfaceVariant, "⚡")
            Spacer(modifier = Modifier.height(12.dp))
            TestItem("Temperature Stress Test", "—", MaterialTheme.colorScheme.onSurfaceVariant, "🌡️")
            Spacer(modifier = Modifier.height(12.dp))
            TestItem("Capacity Calibration", "—", MaterialTheme.colorScheme.onSurfaceVariant, "🔋")
            Spacer(modifier = Modifier.height(12.dp))
            TestItem("Cell Balance Check", "—", MaterialTheme.colorScheme.onSurfaceVariant, "⚖️")
        }
    }
}

@Composable
fun TestItem(
    name: String,
    status: String,
    statusColor: Color,
    icon: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = statusColor.copy(alpha = 0.1f),
        ) {
            Text(
                text = status,
                fontSize = 10.sp,
                color = statusColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    icon: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Placeholder composables for additional sections
@Composable
fun VoltageHistoryChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📈 Voltage History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Voltage trend chart coming soon...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TemperatureHistoryChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📈 Temperature History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Temperature trend chart coming soon...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CycleHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📋 Cycle History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent cycle history coming soon...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
