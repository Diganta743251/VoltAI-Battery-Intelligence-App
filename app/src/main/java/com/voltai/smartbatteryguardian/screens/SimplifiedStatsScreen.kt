package com.voltai.smartbatteryguardian.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.models.AppUsageData
import com.voltai.smartbatteryguardian.preferences.PreferencesViewModel
import com.voltai.smartbatteryguardian.viewmodels.AppUsageUiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifiedStatsScreen() {
    val usageVm: AppUsageUiViewModel = hiltViewModel()
    val apps by usageVm.apps.collectAsState()
    val loading by usageVm.loading.collectAsState()

    val preferencesViewModel: PreferencesViewModel = hiltViewModel()
    val reduceMotion by preferencesViewModel.reduceMotion.collectAsState()

    var sortBy by remember { mutableStateOf(SortBy.TIME) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Initial load
    LaunchedEffect(Unit) {
        usageVm.load()
    }

    val sorted =
        remember(apps, sortBy) {
            when (sortBy) {
                SortBy.TIME -> apps.sortedByDescending { it.totalTimeInForeground }
                SortBy.BATTERY -> apps.sortedByDescending { it.batteryUsage }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App battery usage", fontWeight = FontWeight.SemiBold) },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Sort by time") },
                                onClick = {
                                    sortBy = SortBy.TIME
                                    menuExpanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by battery %") },
                                onClick = {
                                    sortBy = SortBy.BATTERY
                                    menuExpanded = false
                                },
                            )
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        when {
            loading ->
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            sorted.isEmpty() ->
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.TopCenter,
                ) { UsageAccessPermissionBanner() }
            else ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(12.dp),
                ) {
                    items(sorted, key = { it.packageName }) { app ->
                        AppUsageRow(app = app, reduceMotion = reduceMotion)
                    }
                }
        }
    }
}

enum class SortBy { TIME, BATTERY }

@Composable
private fun AppUsageRow(
    app: AppUsageData,
    reduceMotion: Boolean,
) {
    var pressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (!reduceMotion && pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "rowScale",
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier =
        Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                if (!reduceMotion) {
                    pressed = true
                    scope.launch {
                        delay(120)
                        pressed = false
                    }
                }
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val iconPainter = remember(app.icon) { app.icon?.toImageBitmap()?.let { BitmapPainter(it) } }
            if (iconPainter != null) {
                Image(
                    painter = iconPainter,
                    contentDescription = app.appName,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)),
                )
            } else {
                Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(10.dp), tonalElevation = 2.dp) {}
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = formatUsageDuration(app.totalTimeInForeground),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${"%.1f".format(app.batteryUsage)}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "battery",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun Drawable.toImageBitmap(): ImageBitmap {
    return when (this) {
        is BitmapDrawable ->
            this.bitmap?.asImageBitmap() ?: run {
                val bmp = Bitmap.createBitmap(intrinsicWidth.coerceAtLeast(1), intrinsicHeight.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
                val c = Canvas(bmp)
                setBounds(0, 0, c.width, c.height)
                draw(c)
                bmp.asImageBitmap()
            }
        else -> {
            val bmp =
                Bitmap.createBitmap(
                    intrinsicWidth.coerceAtLeast(1),
                    intrinsicHeight.coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888,
                )
            val c = Canvas(bmp)
            setBounds(0, 0, c.width, c.height)
            draw(c)
            bmp.asImageBitmap()
        }
    }
}

private fun formatUsageDuration(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
