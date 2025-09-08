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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import java.text.SimpleDateFormat
import java.util.*

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
    val lastUpdated: Long
)

enum class DiagnosticCategory {
    VOLTAGE, TEMPERATURE, CYCLE_COUNT, CAPACITY, CHARGING, PERFORMANCE, SAFETY
}

enum class DiagnosticStatus {
    OPTIMAL, GOOD, WARNING, CRITICAL, UNKNOWN
}

enum class DiagnosticSeverity {
    INFO, WARNING, ERROR, CRITICAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryDiagnosticsScreen() {
    val context = LocalContext.current
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🔬 Diagnostics", "📊 Voltage", "🌡️ Temperature", "🔄 Cycles", "🧪 Tests")
    
    var isRunningDiagnostics by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color(0xFFE1F5FE)
                    )
                )
            )
    ) {
        // Premium Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1565C0),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Diagnostic Scanner Animation
                    DiagnosticScannerIcon(isScanning = isRunningDiagnostics)
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Diagnostics",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Advanced battery analysis & health monitoring",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    
                    // Diagnostic Status
                    DiagnosticStatusCard()
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Quick Diagnostic Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickStatCard("4.12V", "Voltage", "⚡", Color(0xFF4CAF50))
                    QuickStatCard("32.1°C", "Temperature", "🌡️", Color(0xFFFF9800))
                    QuickStatCard("847", "Cycles", "🔄", Color(0xFF2196F3))
                    QuickStatCard("89%", "Health", "❤️", Color(0xFF9C27B0))
                }
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF1565C0),
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
                    item { 
                        DiagnosticsOverviewCard(
                            isRunningDiagnostics = isRunningDiagnostics,
                            onStartDiagnostics = { isRunningDiagnostics = true }
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
    val rotation by animateFloatAsState(
        targetValue = if (isScanning) 360f else 0f,
        animationSpec = if (isScanning) infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ) else tween(500)
    )
    
    val pulseScale by animateFloatAsState(
        targetValue = if (isScanning) 1.1f else 1.0f,
        animationSpec = if (isScanning) infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ) else tween(300)
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
                rotationZ = rotation
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "🔬", fontSize = 32.sp)
        
        if (isScanning) {
            // Scanning rings
            repeat(3) { index ->
                val delay = index * 500
                val ringScale by animateFloatAsState(
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, delayMillis = delay, easing = EaseOut),
                        repeatMode = RepeatMode.Restart
                    )
                )
                
                Box(
                    modifier = Modifier
                        .size((40 + index * 8).dp)
                        .graphicsLayer {
                            scaleX = ringScale
                            scaleY = ringScale
                            alpha = 1f - ((ringScale - 1f) / 0.5f)
                        }
                        .border(
                            2.dp,
                            Color.White.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun DiagnosticStatusCard() {
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
                    text = "System Healthy",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "Last scan: 2m ago",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun QuickStatCard(value: String, label: String, icon: String, color: Color) {
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
fun DiagnosticsOverviewCard(
    isRunningDiagnostics: Boolean,
    onStartDiagnostics: () -> Unit
) {
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
                    text = "🔬 Diagnostic Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onStartDiagnostics,
                    enabled = !isRunningDiagnostics,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0)
                    )
                ) {
                    if (isRunningDiagnostics) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusSummaryItem("Critical", 0, Color(0xFFF44336))
                StatusSummaryItem("Warning", 1, Color(0xFFFF9800))
                StatusSummaryItem("Optimal", 4, Color(0xFF4CAF50))
            }
            
            if (isRunningDiagnostics) {
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1565C0)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Running comprehensive battery analysis...",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StatusSummaryItem(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 24.sp,
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
fun GeneralDiagnosticResultsCard() {
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
                text = "🔍 Diagnostic Results",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sample diagnostic results
            DiagnosticItem(
                icon = "⚡",
                title = "Battery Voltage",
                value = "4.12V",
                status = "OPTIMAL",
                statusColor = Color(0xFF4CAF50),
                description = "Voltage levels are within optimal range"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DiagnosticItem(
                icon = "🌡️",
                title = "Temperature",
                value = "32.1°C",
                status = "GOOD",
                statusColor = Color(0xFF8BC34A),
                description = "Temperature within safe operating range"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DiagnosticItem(
                icon = "🔄",
                title = "Charge Cycles",
                value = "847",
                status = "WARNING",
                statusColor = Color(0xFFFF9800),
                description = "Approaching recommended cycle limit"
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
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = status,
                    fontSize = 10.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "⚡ Voltage Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Voltage gauge placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "4.12V",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Current Voltage",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Voltage metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricItem("Nominal", "3.7V", "🎯")
                MetricItem("Max", "4.2V", "📊")
                MetricItem("Stability", "94%", "📈")
            }
        }
    }
}

@Composable
fun TemperatureAnalysisCard() {
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
                text = "🌡️ Temperature Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Temperature gauge placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "32°C",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = "Current Temperature",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Temperature metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricItem("Average", "28°C", "📊")
                MetricItem("Max Safe", "45°C", "🔥")
                MetricItem("Heat Events", "3", "⚠️")
            }
        }
    }
}

@Composable
fun CycleAnalysisCard() {
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
                text = "🔄 Cycle Analysis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Cycle progress indicator
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = 0.847f,
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round,
                    color = Color(0xFFFF9800),
                    trackColor = Color(0xFFE0E0E0)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "847",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cycles",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Cycle metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricItem("Full", "224", "🔴")
                MetricItem("Partial", "623", "🟡")
                MetricItem("Health", "89%", "❤️")
            }
        }
    }
}

@Composable
fun DiagnosticTestsCard() {
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
                text = "🧪 Diagnostic Tests",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sample diagnostic tests
            TestItem("Voltage Stability Test", "COMPLETED", Color(0xFF4CAF50), "⚡")
            Spacer(modifier = Modifier.height(12.dp))
            TestItem("Temperature Stress Test", "RUNNING", Color(0xFF2196F3), "🌡️")
            Spacer(modifier = Modifier.height(12.dp))
            TestItem("Capacity Calibration", "NOT STARTED", Color(0xFF666666), "🔋")
            Spacer(modifier = Modifier.height(12.dp))
            TestItem("Cell Balance Check", "FAILED", Color(0xFFF44336), "⚖️")
        }
    }
}

@Composable
fun TestItem(name: String, status: String, statusColor: Color, icon: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = statusColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = status,
                fontSize = 10.sp,
                color = statusColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, icon: String) {
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

// Placeholder composables for additional sections
@Composable
fun VoltageHistoryChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📈 Voltage History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Voltage trend chart coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable
fun TemperatureHistoryChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📈 Temperature History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Temperature trend chart coming soon...", color = Color(0xFF666666))
        }
    }
}

@Composable
fun CycleHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "📋 Cycle History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent cycle history coming soon...", color = Color(0xFF666666))
        }
    }
}
