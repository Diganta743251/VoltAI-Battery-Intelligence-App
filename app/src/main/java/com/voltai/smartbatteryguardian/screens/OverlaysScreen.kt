package com.voltai.smartbatteryguardian.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// DataStore for overlay preferences
val android.content.Context.overlayDataStore: DataStore<Preferences> by preferencesDataStore(name = "overlay_settings")

private val BATTERY_OVERLAY_KEY = booleanPreferencesKey("battery_overlay_enabled")
private val CURRENT_OVERLAY_KEY = booleanPreferencesKey("current_overlay_enabled")
private val TEMPERATURE_OVERLAY_KEY = booleanPreferencesKey("temperature_overlay_enabled")
private val OVERLAY_OPACITY_KEY = floatPreferencesKey("overlay_opacity")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaysScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State for overlay settings
    val batteryOverlayEnabled by context.overlayDataStore.data
        .map { it[BATTERY_OVERLAY_KEY] ?: false }
        .collectAsState(initial = false)

    val currentOverlayEnabled by context.overlayDataStore.data
        .map { it[CURRENT_OVERLAY_KEY] ?: false }
        .collectAsState(initial = false)

    val temperatureOverlayEnabled by context.overlayDataStore.data
        .map { it[TEMPERATURE_OVERLAY_KEY] ?: false }
        .collectAsState(initial = false)

    val overlayOpacity by context.overlayDataStore.data
        .map { it[OVERLAY_OPACITY_KEY] ?: 0.8f }
        .collectAsState(initial = 0.8f)

    // Permission dialog state
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Check if overlay permission is granted
    val hasOverlayPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Advanced overlays",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Display real-time battery information on your screen. Overlays will appear over other apps to keep you informed about your battery status.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        if (!hasOverlayPermission) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Permission required for overlays to work",
                                        fontSize = 14.sp,
                                        color = Color(0xFFFF9800),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // All-in-One Overlay Section
            item {
                Text(
                    text = "All in one overlay",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                OverlayOptionCard(
                    title = "All in one overlay",
                    description = "This overlay shows live data of most important battery info, such as electric current, battery temperature, battery voltage, etc.",
                    additionalInfo = "Overlay is collapsed by default, but can be expanded by tapping on it. Overlay is also draggable.",
                    enabled = batteryOverlayEnabled,
                    onToggle = { enabled ->
                        if (enabled && !hasOverlayPermission) {
                            showPermissionDialog = true
                        } else {
                            scope.launch {
                                context.overlayDataStore.edit { preferences ->
                                    preferences[BATTERY_OVERLAY_KEY] = enabled
                                }
                            }
                        }
                    }
                )
            }

            // Simple Overlays Section
            item {
                Text(
                    text = "Simple overlays",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                OverlayOptionCard(
                    title = "Electric current",
                    description = "Show live electric current in overlay",
                    enabled = currentOverlayEnabled,
                    onToggle = { enabled ->
                        if (enabled && !hasOverlayPermission) {
                            showPermissionDialog = true
                        } else {
                            scope.launch {
                                context.overlayDataStore.edit { preferences ->
                                    preferences[CURRENT_OVERLAY_KEY] = enabled
                                }
                            }
                        }
                    }
                )
            }

            item {
                OverlayOptionCard(
                    title = "Battery temperature",
                    description = "Show live battery temperature in overlay",
                    enabled = temperatureOverlayEnabled,
                    onToggle = { enabled ->
                        if (enabled && !hasOverlayPermission) {
                            showPermissionDialog = true
                        } else {
                            scope.launch {
                                context.overlayDataStore.edit { preferences ->
                                    preferences[TEMPERATURE_OVERLAY_KEY] = enabled
                                }
                            }
                        }
                    }
                )
            }

            // Overlay Customization
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Overlay Opacity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Adjust transparency of overlay elements",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "20%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Slider(
                                value = overlayOpacity,
                                onValueChange = { newValue ->
                                    scope.launch {
                                        context.overlayDataStore.edit { preferences ->
                                            preferences[OVERLAY_OPACITY_KEY] = newValue
                                        }
                                    }
                                },
                                valueRange = 0.2f..1.0f,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF2196F3),
                                    activeTrackColor = Color(0xFF2196F3)
                                )
                            )

                            Text(
                                text = "100%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Current: ${(overlayOpacity * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Additional settings card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "💡 Overlay Tips",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OverlayTipItem("Tap overlays to expand and see more details")
                        OverlayTipItem("Long press to move overlays around your screen")
                        OverlayTipItem("Overlays work on top of all other apps")
                        OverlayTipItem("Disable when not needed to save battery")
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Permission Request Dialog
    if (showPermissionDialog) {
        OverlayPermissionDialog(
            onRequestPermission = {
                showPermissionDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            },
            onDismiss = {
                showPermissionDialog = false
            }
        )
    }
}

@Composable
fun OverlayOptionCard(
    title: String,
    description: String,
    additionalInfo: String = "",
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    if (additionalInfo.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = additionalInfo,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF2196F3),
                        checkedTrackColor = Color(0xFF2196F3).copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Composable
fun OverlayTipItem(tip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = tip,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OverlayPermissionDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Overlay Permission Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "To display battery information on top of other apps, VoltAI needs permission to draw overlays. This permission is required for the overlay feature to work.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}
