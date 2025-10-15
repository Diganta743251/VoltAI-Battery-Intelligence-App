package com.voltai.features.appusage

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.domain.model.AppUsage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AppUsageViewModel = hiltViewModel()
) {
    val appUsage by viewModel.appUsage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val totalScreenTime by viewModel.totalScreenTime.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("App Usage Details") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    // Minimal back icon with RTL support
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
        when {
            isLoading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            !isLoading && !hasPermission -> {
                UsageAccessPermissionBanner()
            }
            !isLoading && hasPermission && appUsage.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No app usage data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Error", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AppUsageHeader(totalScreenTime = totalScreenTime, appsCount = appUsage.size)
                    }
                    items(appUsage) { app ->
                        DetailedAppUsageItem(
                            app = app,
                            // Background time not available yet; show 0 for consistency
                            backgroundTime = 0L,
                            // Battery percentage approximation using estimatedBatteryDrain if available
                            batteryUsage = app.estimatedBatteryDrain.toFloat().coerceAtLeast(0f),
                            percentageOfTotal = percentageOfTotal(app.totalTimeInForeground, totalScreenTime)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUsageHeader(totalScreenTime: Long, appsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Summary", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Total screen time: ${formatDuration(totalScreenTime)} • Apps: $appsCount",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailedAppUsageItem(
    app: AppUsage,
    backgroundTime: Long,
    batteryUsage: Float,
    percentageOfTotal: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(packageName = app.packageName, modifier = Modifier.size(48.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatDuration(app.totalTimeInForeground)} foreground",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatDuration(backgroundTime)} background",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val batteryColor = when {
                    batteryUsage > 50f -> MaterialTheme.colorScheme.error
                    batteryUsage > 20f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                Text(
                    text = "${batteryUsage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = batteryColor
                )
                LinearProgressIndicator(
                    progress = (percentageOfTotal / 100f).coerceIn(0f, 1f),
                    modifier = Modifier.width(60.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${percentageOfTotal.toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val iconDrawable = remember(packageName) {
        runCatching {
            val pm = context.packageManager
            pm.getApplicationIcon(packageName)
        }.getOrNull()
    }
    if (iconDrawable is Drawable) {
        val bmp = remember(iconDrawable) { iconDrawable.toBitmapSafely(96, 96) }
        if (bmp != null) {
            Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = modifier.clip(RoundedCornerShape(12.dp)))
            return
        }
    }
    // Fallback placeholder
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {}
}

private fun percentageOfTotal(value: Long, total: Long): Float =
    if (total > 0) (value.toFloat() / total.toFloat() * 100f) else 0f

private fun formatDuration(ms: Long): String {
    val hours = ms / 3_600_000
    val minutes = (ms % 3_600_000) / 60_000
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Composable
private fun UsageAccessPermissionBanner() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Usage access permission needed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "To show real app usage analytics, allow VoltAI access to Usage Data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = {
                    runCatching {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                }) {
                    Text("Open Settings")
                }
            }
        }
    }
}

private fun Drawable.toBitmapSafely(width: Int, height: Int): Bitmap? = try {
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    bmp
} catch (_: Throwable) { null }