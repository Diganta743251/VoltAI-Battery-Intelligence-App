package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.viewmodels.OverlayCustomizerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun OverlayCustomizerScreen(
    viewModel: OverlayCustomizerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Overlay Customizer",
                    fontWeight = FontWeight.Bold,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )

        // Tabs for overlay type
        val tabs = listOf("BATTERY_SAVER" to "Battery Saver", "AOD" to "AOD")
        val selectedIndex = tabs.indexOfFirst { it.first == state.overlayType }.let { if (it < 0) 0 else it }
        TabRow(selectedTabIndex = selectedIndex) {
            tabs.forEachIndexed { index, pair ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { viewModel.setOverlayType(pair.first) },
                    text = { Text(pair.second) },
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Enable overlay
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Enable overlay", style = MaterialTheme.typography.titleMedium)
                            Text("Display overlay when active", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = state.enabled, onCheckedChange = { viewModel.setEnabled(it) })
                    }
                }
            }

            // Elements
            item { Text("Elements", style = MaterialTheme.typography.titleMedium) }
            items(
                listOf(
                    Triple("Battery ring", state.showRing) { b: Boolean -> viewModel.setShowRing(b) },
                    Triple("Stats", state.showStats) { b: Boolean -> viewModel.setShowStats(b) },
                    Triple("Clock", state.showClock) { b: Boolean -> viewModel.setShowClock(b) },
                    Triple("Tips", state.showTips) { b: Boolean -> viewModel.setShowTips(b) },
                ),
            ) { (title, checked, onToggle) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(title)
                        Switch(checked = checked, onCheckedChange = { onToggle(it) })
                    }
                }
            }

            // Sliders
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Ring size")
                        Slider(value = state.ringSize, onValueChange = { viewModel.setRingSize(it) }, valueRange = 0f..1f)
                        Text("Opacity")
                        Slider(value = state.opacity, onValueChange = { viewModel.setOpacity(it) }, valueRange = 0f..1f)
                        if (state.overlayType == "AOD") {
                            Text("AOD Brightness")
                            Slider(
                                value = state.aodBrightness / 100f,
                                onValueChange = { viewModel.setAodBrightness((it * 100).toInt()) },
                                valueRange = 0f..1f,
                            )
                        }
                    }
                }
            }

            // Colors
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Accent color")
                        ColorRow(
                            colors = listOf(
                                Color(0xFF4CAF50), // Green
                                Color(0xFF2196F3), // Blue
                                Color(0xFFFF9800), // Orange
                                Color(0xFFE91E63), // Pink
                                Color(0xFF9C27B0), // Purple
                            ),
                            onPick = { c -> viewModel.setAccentColor(c.toArgb().toLong()) },
                        )
                        Text("Background color")
                        ColorRow(
                            colors = listOf(
                                Color(0x00000000), // Transparent
                                Color(0x80212121),
                                Color(0xB3000000),
                                Color(0xFFFFFFFF),
                                Color(0xFFE0E0E0),
                            ),
                            onPick = { c -> viewModel.setBackgroundColor(c.toArgb().toLong()) },
                        )
                    }
                }
            }

            // Preview
            item {
                Text("Preview", style = MaterialTheme.typography.titleMedium)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .semantics { contentDescription = "Overlay preview" },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(state.backgroundColor.toInt()).copy(alpha = state.opacity)),
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (state.showRing) {
                                // Simple ring mock using a spacer box with accent color border
                                Box(
                                    modifier = Modifier
                                        .size((100 * state.ringSize.coerceIn(0.2f, 1f)).dp)
                                        .background(Color.Transparent),
                                )
                                Divider(color = Color(state.accentColor.toInt()), thickness = 4.dp)
                            }
                            if (state.showStats) Text("Battery: 82% · Temp: 32°C", color = Color(state.accentColor.toInt()))
                            if (state.showClock) Text("10:24", style = MaterialTheme.typography.titleLarge)
                            if (state.showTips) Text("Tip: Avoid full discharges", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorRow(colors: List<Color>, onPick: (Color) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        colors.forEach { c ->
            AssistChip(onClick = { onPick(c) }, label = { Text("") }, colors = AssistChipDefaults.assistChipColors(
                containerColor = c,
                labelColor = Color.Transparent,
            ))
        }
    }
}
