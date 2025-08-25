package com.voltai.features.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
// Removed default Material icons - using custom AppIcons.kt instead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.voltai.ui.components.*
import kotlinx.coroutines.delay

sealed class SettingsItemType {
    data class Toggle(
        val isEnabled: Boolean,
        val onToggle: (Boolean) -> Unit
    ) : SettingsItemType()
    
    object Navigation : SettingsItemType()
    
    data class Selection(
        val selected: String
    ) : SettingsItemType()
}

data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

data class SettingsItem(
    val title: String,
    val description: String,
    val icon: @Composable () -> Unit,
    val type: SettingsItemType,
    val action: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var isDarkTheme by remember { mutableStateOf(false) }
    var isHapticsEnabled by remember { mutableStateOf(true) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var isAutoOptimizeEnabled by remember { mutableStateOf(false) }
    
    // Haptic feedback
    val hapticFeedback = LocalHapticFeedback.current
    
    // Animation state
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val settingsSections = listOf(
        SettingsSection(
            title = "Appearance",
            items = listOf(
                SettingsItem(
                    title = "Dark Theme",
                    description = "Switch between light and dark mode",
                    icon = { Brightness6Icon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Toggle(
                        isEnabled = isDarkTheme,
                        onToggle = { isDarkTheme = it }
                    )
                )
            )
        ),
        SettingsSection(
            title = "Experience",
            items = listOf(
                SettingsItem(
                    title = "Haptic Feedback",
                    description = "Feel vibrations when interacting with the app",
                    icon = { PowerIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Toggle(
                        isEnabled = isHapticsEnabled,
                        onToggle = { 
                            isHapticsEnabled = it
                            if (it) hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                ),
                SettingsItem(
                    title = "Notifications",
                    description = "Receive battery alerts and optimization tips",
                    icon = { BatteryIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Toggle(
                        isEnabled = isNotificationsEnabled,
                        onToggle = { isNotificationsEnabled = it }
                    )
                )
            )
        ),
        SettingsSection(
            title = "Battery",
            items = listOf(
                SettingsItem(
                    title = "Auto Optimize",
                    description = "Automatically optimize battery settings",
                    icon = { SettingsIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Toggle(
                        isEnabled = isAutoOptimizeEnabled,
                        onToggle = { isAutoOptimizeEnabled = it }
                    )
                ),
                SettingsItem(
                    title = "Battery Health",
                    description = "View detailed battery health information",
                    icon = { HealthIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Navigation
                ),
                SettingsItem(
                    title = "Usage Statistics",
                    description = "Analyze app battery consumption",
                    icon = { AnalyticsIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Navigation
                )
            )
        ),
        SettingsSection(
            title = "Data",
            items = listOf(
                SettingsItem(
                    title = "Export Data",
                    description = "Export battery logs and statistics",
                    icon = { GetAppIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Navigation
                ),
                SettingsItem(
                    title = "Clear Data",
                    description = "Reset all battery statistics",
                    icon = { ExportIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Navigation
                )
            )
        ),
        SettingsSection(
            title = "About",
            items = listOf(
                SettingsItem(
                    title = "App Version",
                    description = "1.0.0 (Build 1)",
                    icon = { BatteryIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Navigation
                ),
                SettingsItem(
                    title = "Privacy Policy",
                    description = "Read our privacy policy",
                    icon = { HealthIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Navigation
                ),
                SettingsItem(
                    title = "Open Source Licenses",
                    description = "View third-party licenses",
                    icon = { AnalyticsIcon(tint = MaterialTheme.colorScheme.primary) },
                    type = SettingsItemType.Navigation
                )
            )
        )
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Customize your VoltAI experience",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Settings sections
        items(settingsSections) { section ->
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(
                        800, 
                        delayMillis = 200 + (settingsSections.indexOf(section) * 100),
                        easing = EaseOutCubic
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        800, 
                        delayMillis = 200 + (settingsSections.indexOf(section) * 100)
                    )
                )
            ) {
                SettingsSectionCard(section = section)
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    section: SettingsSection,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section title
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Section items
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                section.items.forEachIndexed { index, item ->
                    SettingsItemRow(
                        item = item,
                        showDivider = index < section.items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsItem,
    showDivider: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { 
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    item.action()
                }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                item.icon()
            }
            
            // Title and description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action (toggle, arrow, etc.)
            when (val type = item.type) {
                is SettingsItemType.Toggle -> {
                    Switch(
                        checked = type.isEnabled,
                        onCheckedChange = type.onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                }
                is SettingsItemType.Navigation -> {
                    Text(
                        text = ">",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is SettingsItemType.Selection -> {
                    Text(
                        text = type.selected,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 52.dp, top = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

// SettingsScreen.kt - Fixed to use only custom AppIcons.kt icons