package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.rememberBatteryData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val scrollState = rememberScrollState()

    // Mock data for demonstration - replace with actual repository data
    val batteryHistory = remember { generateMockBatteryHistory() }

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
        // Carbon Footprint Header Card
        CarbonFootprintCard(
            totalSessions = batteryHistory.size,
            carbonSaved = batteryHistory.sumOf { it.carbonFootprint },
            modifier = Modifier.padding(16.dp)
        )

        // Battery History List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today's sessions
            val today = batteryHistory.filter { isToday(it.timestamp) }
            if (today.isNotEmpty()) {
                item {
                    DateSeparator("Today")
                }
                items(today) { session ->
                    BatteryHistoryCard(session)
                }
            }

            // Yesterday's sessions
            val yesterday = batteryHistory.filter { isYesterday(it.timestamp) }
            if (yesterday.isNotEmpty()) {
                item {
                    DateSeparator("Yesterday")
                }
                items(yesterday) { session ->
                    BatteryHistoryCard(session)
                }
            }

            // Earlier this week
            val thisWeek = batteryHistory.filter {
                !isToday(it.timestamp) && !isYesterday(it.timestamp) && isThisWeek(it.timestamp)
            }
            if (thisWeek.isNotEmpty()) {
                item {
                    DateSeparator("Earlier this week")
                }
                items(thisWeek) { session ->
                    BatteryHistoryCard(session)
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CarbonFootprintCard(
    totalSessions: Int,
    carbonSaved: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Your carbon footprint",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Adopt a greener lifestyle by optimizing device energy use. Charge during periods of less demand, minimize waste, and choose clean energy.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Environmental illustration
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF81C784),
                                Color(0xFF4CAF50)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌱",
                    fontSize = 32.sp
                )
            }
        }
    }
}

@Composable
fun DateSeparator(date: String) {
    Text(
        text = date,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun BatteryHistoryCard(session: BatterySession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with session type and battery change
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (session.isCharging) Icons.Default.Add else Icons.Default.Clear,
                        contentDescription = null,
                        tint = if (session.isCharging) Color(0xFF4CAF50) else Color(0xFFFF5722),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (session.isCharging) "Charged for" else "Used for",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${if (session.isCharging) "+" else "-"}${session.batteryChange}% • ${session.energyUsed}mAh",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (session.isCharging) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duration and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.duration,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatTime(session.timestamp, session.endTime),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Battery level indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start level
                BatteryLevelIndicator(
                    level = session.startLevel,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${session.startLevel}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = session.batteryChange / 100f,
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp),
                    color = if (session.isCharging) Color(0xFF4CAF50) else Color(0xFFFF5722),
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "${session.endLevel}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                // End level
                BatteryLevelIndicator(
                    level = session.endLevel,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun BatteryLevelIndicator(
    level: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = when {
                    level > 80 -> Color(0xFF4CAF50)
                    level > 20 -> Color(0xFFFF9800)
                    else -> Color(0xFFFF5722)
                },
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$level",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// Data classes and utility functions
data class BatterySession(
    val id: String,
    val isCharging: Boolean,
    val duration: String,
    val batteryChange: Int,
    val startLevel: Int,
    val endLevel: Int,
    val energyUsed: Int, // mAh
    val carbonFootprint: Double, // grams of CO2
    val timestamp: Long,
    val endTime: Long
)

fun generateMockBatteryHistory(): List<BatterySession> {
    val sessions = mutableListOf<BatterySession>()
    val now = System.currentTimeMillis()
    val random = kotlin.random.Random

    // Generate sessions for the last 7 days
    for (day in 0..6) {
        val dayStart = now - (day * 24 * 60 * 60 * 1000)

        // 2-4 sessions per day
        for (session in 1..random.nextInt(2, 5)) {
            val isCharging = random.nextBoolean()
            val batteryChange = if (isCharging) random.nextInt(10, 50) else random.nextInt(5, 40)
            val startLevel = if (isCharging) random.nextInt(20, 80) else random.nextInt(30, 90)
            val endLevel = if (isCharging) {
                (startLevel + batteryChange).coerceAtMost(
                    100
                )
            } else {
                (startLevel - batteryChange).coerceAtLeast(0)
            }
            val durationMinutes = random.nextInt(30, 180)
            val energyUsed = random.nextInt(200, 800)
            val carbonFootprint = energyUsed * 0.0005 // rough calculation

            val sessionStart = dayStart + (session * 6 * 60 * 60 * 1000) // spread throughout day
            val sessionEnd = sessionStart + (durationMinutes * 60 * 1000)

            sessions.add(
                BatterySession(
                    id = "session_${day}_$session",
                    isCharging = isCharging,
                    duration = formatDuration(durationMinutes),
                    batteryChange = batteryChange,
                    startLevel = startLevel,
                    endLevel = endLevel,
                    energyUsed = energyUsed,
                    carbonFootprint = carbonFootprint,
                    timestamp = sessionStart,
                    endTime = sessionEnd
                )
            )
        }
    }

    return sessions.sortedByDescending { it.timestamp }
}

fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        "${hours}h ${mins}m"
    } else {
        "${mins}m"
    }
}

fun formatTime(startTime: Long, endTime: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val start = sdf.format(Date(startTime))
    val end = sdf.format(Date(endTime))
    return "$start - $end"
}

fun isToday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.timeInMillis = timestamp
    return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}

fun isYesterday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.timeInMillis = timestamp
    cal1.add(Calendar.DAY_OF_YEAR, -1)
    return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}

fun isThisWeek(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.timeInMillis = timestamp
    return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) &&
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}
