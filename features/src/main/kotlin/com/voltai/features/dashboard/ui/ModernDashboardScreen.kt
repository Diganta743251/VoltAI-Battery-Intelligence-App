package com.voltai.features.dashboard.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.animateFloat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.voltai.features.dashboard.DashboardViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
    toolItems: List<ToolItem> = emptyList(),
    onNavigateToAppUsage: (() -> Unit)? = null,
) {
    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val currentData by viewModel.currentData.collectAsState()
    val forecast by viewModel.batteryForecast.collectAsState()
    val thermal by viewModel.thermalData.collectAsState()
    val health by viewModel.batteryHealth.collectAsState()
    val appUsage by viewModel.appUsage.collectAsState()
    val usageError by viewModel.usageError.collectAsState()

    val batteryLevel = batteryStatus.level
    val isCharging = batteryStatus.isCharging

    // Chevron icon used by CTA
    @Composable
    fun ChevronIcon(
        tint: Color = MaterialTheme.colorScheme.primary,
        modifier: Modifier = Modifier
    ) {
        Canvas(modifier = modifier.size(20.dp)) {
            val stroke = 2.dp.toPx()
            val w = size.width
            val h = size.height
            // Draw a right-pointing chevron >
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.25f),
                end = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.5f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.75f),
                end = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.5f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }

    // Premium CTA component for navigation to detailed app usage
    @Composable
    fun DetailedAppUsageCTA(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .clip(MaterialTheme.shapes.large)
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.voltai.ui.components.GraphIcon(tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Detailed App Usage",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ChevronIcon(tint = MaterialTheme.colorScheme.primary)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header with gradient background and ring
        ParallaxHeader(
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            temperature = thermal,
            healthPercentage = health.healthPercentage
        )

        // Permission state and effects
        val hasUsagePermission by viewModel.hasUsageStatsPermission.collectAsState()
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current

        // Handle one-shot permission request events
        LaunchedEffect(Unit) {
            viewModel.permissionRequests.collect {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
        }
        // Refresh permission when screen is visible again
        LaunchedEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refreshUsageStatsPermission()
            }
        }

        // Inline permission banner
        if (!hasUsagePermission) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Usage Access Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Grant permission to see detailed app usage statistics and screen time analytics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.requestUsageStatsPermission() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                            contentColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text("Open Settings")
                    }
                }
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            // Full-width header-like items should span across both columns
            // so the CTA visually aligns with premium design sections.
            val fullSpan = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine
            item {
                HeroBatteryCard(
                    batteryLevel = batteryLevel,
                    isCharging = isCharging,
                    health = health.healthPercentage,
                    temperature = thermal
                )
            }
            item {
                LiveCurrentGraphCard(
                    data = currentData,
                    isCharging = isCharging
                )
            }
            item {
                val screenTime by viewModel.screenTimeMinutes.collectAsState()
                val usageMah by viewModel.usageTodayMah.collectAsState()
                QuickStatsRow(
                    screenTimeMinutes = screenTime,
                    usageTodayMah = usageMah,
                    chargeCycles = health.cycleCount
                )
            }
            // Screen time detail card with empty/error states
            item {
                ScreenTimeDetailCard(
                    screenTimeMinutes = viewModel.screenTimeMinutes.collectAsState().value,
                    error = usageError
                )
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                DetailedAppUsageCTA(
                    onClick = { onNavigateToAppUsage?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
            // App usage breakdown list (top drainers)
            item {
                val visible = appUsage.isNotEmpty() || usageError != null
                AnimatedVisibility(
                    visible = visible,
                    enter = androidx.compose.animation.slideInVertically() + androidx.compose.animation.fadeIn()
                ) {
                    AppUsageBreakdownCard(
                        usage = appUsage,
                        isEmpty = appUsage.isEmpty(),
                        error = usageError,
                        onNavigateToAppUsage = { onNavigateToAppUsage?.invoke() }
                    )
                }
            }
            items(toolItems) { tool ->
                DashboardToolCard(tool = tool)
            }
            item {
                val history by viewModel.healthHistory.collectAsState()
                HealthTrendCard(
                    healthHistory = history,
                    currentHealth = health.healthPercentage
                )
            }
            item {
                val (timeRemaining, chargingTime) = derivePredictionTexts(isCharging, forecast)
                UsagePredictionCard(
                    timeRemaining = timeRemaining,
                    chargingTime = chargingTime
                )
            }
        }
    }
}

@Composable
private fun derivePredictionTexts(isCharging: Boolean, forecast: String): Pair<String, String> {
    // Pure helper: no composable calls here; safe to mark @Composable for previews if needed
    return if (isCharging) {
        val charge = if (forecast.isBlank()) "Charging..." else forecast
        "--" to charge
    } else {
        val remain = if (forecast.isBlank()) "Estimating usage..." else forecast
        remain to "--"
    }
}

@Composable
private fun ParallaxHeader(
    batteryLevel: Int,
    isCharging: Boolean,
    temperature: Float,
    healthPercentage: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "VoltAI",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isCharging) "Charging" else "Discharging",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Temp: ${"%.1f".format(temperature)}°C · Health: $healthPercentage%",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            AnimatedBatteryRing(
                batteryLevel = batteryLevel,
                isCharging = isCharging,
                size = 100.dp
            )
        }
    }
}

@Composable
fun HeroBatteryCard(
    batteryLevel: Int,
    isCharging: Boolean,
    health: Int,
    temperature: Float,
    modifier: Modifier = Modifier
) {
    val batteryColor = when {
        batteryLevel > 80 -> Color(0xFF4CAF50)
        batteryLevel > 20 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("Battery", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = batteryColor
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isCharging) "Charging" else "Discharging",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Health $health% · ${"%.1f".format(temperature)}°C",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedBatteryRing(batteryLevel = batteryLevel, isCharging = isCharging, size = 80.dp)
        }
    }
}

@Composable
fun LiveCurrentGraphCard(
    data: List<Float>,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Live Current", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val strokeColor = if (isCharging) Color(0xFF4CAF50) else Color(0xFFF44336)
            val bg = MaterialTheme.colorScheme.surfaceVariant
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(bg)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (data.isEmpty()) return@Canvas
                    val maxAbs = (data.maxOf { kotlin.math.abs(it) }.takeIf { it > 0 } ?: 1f)
                    val stepX = size.width / (data.size - 1).coerceAtLeast(1)
                    var lastX = 0f
                    var lastY = size.height / 2f - (data.first() / maxAbs) * (size.height / 2f)
                    for (i in 1 until data.size) {
                        val x = i * stepX
                        val y = size.height / 2f - (data[i] / maxAbs) * (size.height / 2f)
                        drawLine(
                            color = strokeColor,
                            start = androidx.compose.ui.geometry.Offset(lastX, lastY),
                            end = androidx.compose.ui.geometry.Offset(x, y),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        lastX = x
                        lastY = y
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatsRow(
    screenTimeMinutes: Int,
    usageTodayMah: Int,
    chargeCycles: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatChip(title = "Screen Time", value = "${screenTimeMinutes}m")
            StatChip(title = "Usage", value = "${usageTodayMah} mAh")
            StatChip(title = "Cycles", value = "$chargeCycles")
        }
    }
}

@Composable
private fun StatChip(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HealthTrendCard(
    healthHistory: List<Int>,
    currentHealth: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Health Trend", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            // Simple sparkline using Canvas (placeholder)
            val primaryColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (healthHistory.size < 2) return@Canvas
                    val min = healthHistory.min()
                    val max = healthHistory.max()
                    val range = (max - min).coerceAtLeast(1)
                    val stepX = size.width / (healthHistory.size - 1)
                    var lastX = 0f
                    var lastY = size.height - (healthHistory.first() - min) / range.toFloat() * size.height
                    for (i in 1 until healthHistory.size) {
                        val x = i * stepX
                        val y = size.height - (healthHistory[i] - min) / range.toFloat() * size.height
                        drawLine(
                            color = primaryColor,
                            start = androidx.compose.ui.geometry.Offset(lastX, lastY),
                            end = androidx.compose.ui.geometry.Offset(x, y),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        lastX = x
                        lastY = y
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Current: $currentHealth%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun UsagePredictionCard(
    timeRemaining: String,
    chargingTime: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Prediction", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EtaChip(label = "Time remaining", value = timeRemaining)
                EtaChip(label = "Charging time", value = chargingTime)
            }
        }
    }
}

@Composable
private fun EtaChip(label: String, value: String) {
    AssistChip(
        onClick = {},
        label = { Text(text = "$label: $value") },
        leadingIcon = null
    )
}

@Composable
fun ScreenTimeDetailCard(
    screenTimeMinutes: Int,
    error: String?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Screen Time", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            when {
                error != null -> Text("$error", color = MaterialTheme.colorScheme.error)
                screenTimeMinutes <= 0 -> Text("No screen time data in the last 24h", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> Text("${screenTimeMinutes} minutes today", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AppUsageBreakdownCard(
    usage: List<com.voltai.domain.model.AppUsage>,
    isEmpty: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    onNavigateToAppUsage: () -> Unit = {}
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Top Battery Consumers", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onNavigateToAppUsage) {
                    Text("See all")
                }
            }
            Spacer(Modifier.height(8.dp))
            when {
                error != null -> Text("$error", color = MaterialTheme.colorScheme.error)
                isEmpty -> Text("No app usage data", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> {
                    val total = usage.sumOf { it.totalTimeInForeground }
                    usage.take(5).forEach { item ->
                        AppUsageItem(
                            appName = item.appName,
                            packageName = item.packageName,
                            usageTime = item.totalTimeInForeground,
                            percentage = if (total > 0) (item.totalTimeInForeground.toFloat() / total * 100f) else 0f
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUsageItem(
    appName: String,
    packageName: String,
    usageTime: Long,
    percentage: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconDrawable = remember(packageName) {
        runCatching {
            val pm = context.packageManager
            pm.getApplicationIcon(packageName)
        }.getOrNull()
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconDrawable is Drawable) {
            val bmp = remember(iconDrawable) { iconDrawable.toBitmapSafely(96, 96) }
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
            }
        } else {
            // Fallback simple circle when icon not available
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {}
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(appName, style = MaterialTheme.typography.bodyLarge)
            Text(
                formatDuration(usageTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = (percentage / 100f).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("${percentage.toInt()}%", style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatDuration(ms: Long): String {
    val hours = ms / 3_600_000
    val minutes = (ms % 3_600_000) / 60_000
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

// Safely convert Drawable to Bitmap for Compose Image
private fun Drawable.toBitmapSafely(width: Int, height: Int): Bitmap? = try {
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    bmp
} catch (_: Throwable) { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardToolCard(
    tool: ToolItem,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    Card(
        onClick = {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            tool.navigationAction()
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(tool.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(tool.subtitle ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AnimatedBatteryRing(
    batteryLevel: Int,
    isCharging: Boolean,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = (batteryLevel.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing), label = "batteryProgress"
    )
    val glowAnim = rememberInfiniteTransition(label = "glow")
    val glow by glowAnim.animateFloat(
        initialValue = if (isCharging) 0.6f else 0f,
        targetValue = if (isCharging) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "glowAnim"
    )

    val ringColor = when {
        batteryLevel > 80 -> Color(0xFF4CAF50)
        batteryLevel > 20 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Battery ring $batteryLevel%" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background track
            drawArc(
                color = Color.Black.copy(alpha = 0.08f),
                startAngle = -210f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress
            drawArc(
                color = ringColor.copy(alpha = 1f),
                startAngle = -210f,
                sweepAngle = 240f * progress,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
            if (isCharging && glow > 0f) {
                drawArc(
                    color = ringColor.copy(alpha = 0.2f * glow),
                    startAngle = -210f,
                    sweepAngle = 240f * progress,
                    useCenter = false,
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Text(text = "$batteryLevel%", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

// Simple tool item model for dashboard shortcuts
data class ToolItem(
    val title: String,
    val subtitle: String? = null,
    val navigationAction: () -> Unit = {}
)