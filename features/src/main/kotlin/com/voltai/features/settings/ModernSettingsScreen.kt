package com.voltai.features.settings

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
// Using custom Composable icons instead of ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.ui.components.*
import kotlinx.coroutines.delay

data class ModernSettingsSection(
    val title: String,
    val items: List<ModernSettingsItem>
)

data class ModernSettingsItem(
    val title: String,
    val description: String,
    val icon: @Composable (Color) -> Unit, // Custom icon composable
    val type: ModernSettingsItemType,
    val action: () -> Unit = {}
)

sealed class ModernSettingsItemType {
    object Navigation : ModernSettingsItemType()
    data class Toggle(val isEnabled: Boolean, val onToggle: (Boolean) -> Unit) : ModernSettingsItemType()
    data class Selection(val options: List<String>, val selected: String, val onSelect: (String) -> Unit) : ModernSettingsItemType()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(false) }
    var isHighContrast by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedTheme by remember { mutableStateOf("Auto") }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    val settingsSections = listOf(
        ModernSettingsSection(
            title = "Appearance",
            items = listOf(
                ModernSettingsItem(
                    title = "Theme",
                    description = "Choose your preferred theme",
                    icon = { color -> SunIcon(tint = color) },
                    type = ModernSettingsItemType.Selection(
                        options = listOf("Light", "Dark", "Auto"),
                        selected = selectedTheme,
                        onSelect = { selectedTheme = it }
                    )
                ),
                ModernSettingsItem(
                    title = "High Contrast",
                    description = "Enhance visibility with high contrast colors",
                    icon = { color -> EyeIcon(tint = color) },
                    type = ModernSettingsItemType.Toggle(
                        isEnabled = isHighContrast,
                        onToggle = { isHighContrast = it }
                    )
                ),
                ModernSettingsItem(
                    title = "Dark Battery Icon",
                    description = "Use dark icon in status bar",
                    icon = { color -> BatteryIcon(tint = color) },
                    type = ModernSettingsItemType.Toggle(
                        isEnabled = false,
                        onToggle = { }
                    )
                )
            )
        ),
        ModernSettingsSection(
            title = "Notifications",
            items = listOf(
                ModernSettingsItem(
                    title = "Battery Alerts",
                    description = "Get notified about battery status",
                    icon = { color -> BatteryIcon(tint = color) },
                    type = ModernSettingsItemType.Toggle(
                        isEnabled = notificationsEnabled,
                        onToggle = { notificationsEnabled = it }
                    )
                ),
                ModernSettingsItem(
                    title = "Low Battery Warning",
                    description = "Alert when battery is low",
                    icon = { color -> ThermometerIcon(tint = color) },
                    type = ModernSettingsItemType.Toggle(
                        isEnabled = true,
                        onToggle = { }
                    )
                ),
                ModernSettingsItem(
                    title = "Charging Complete",
                    description = "Notify when charging is complete",
                    icon = { color -> FlashIcon(tint = color) },
                    type = ModernSettingsItemType.Toggle(
                        isEnabled = true,
                        onToggle = { }
                    )
                )
            )
        ),
        ModernSettingsSection(
            title = "Data & Privacy",
            items = listOf(
                ModernSettingsItem(
                    title = "Export Battery Data",
                    description = "Export your battery logs as CSV",
                    icon = { color -> GraphIcon(tint = color) },
                    type = ModernSettingsItemType.Navigation,
                    action = { /* Export action */ }
                ),
                ModernSettingsItem(
                    title = "Reset Statistics",
                    description = "Clear all battery usage data",
                    icon = { color -> RefreshIcon(tint = color) },
                    type = ModernSettingsItemType.Navigation,
                    action = { /* Reset action */ }
                ),
                ModernSettingsItem(
                    title = "Usage Data Collection",
                    description = "Allow anonymous usage analytics",
                    icon = { color -> GraphIcon(tint = color) },
                    type = ModernSettingsItemType.Toggle(
                        isEnabled = false,
                        onToggle = { }
                    )
                )
            )
        ),
        ModernSettingsSection(
            title = "Accessibility",
            items = listOf(
                ModernSettingsItem(
                    title = "Screen Reader Support",
                    description = "Enhanced accessibility features",
                    icon = { color -> EyeIcon(tint = color) },
                    type = ModernSettingsItemType.Toggle(
                        isEnabled = true,
                        onToggle = { }
                    )
                ),
                ModernSettingsItem(
                    title = "Haptic Feedback",
                    description = "Vibration feedback for interactions",
                    icon = { color -> BatteryIcon(tint = color) },
                    type = ModernSettingsItemType.Toggle(
                        isEnabled = true,
                        onToggle = { }
                    )
                )
            )
        ),
        ModernSettingsSection(
            title = "About",
            items = listOf(
                ModernSettingsItem(
                    title = "Help & Support",
                    description = "Get help and view documentation",
                    icon = { color -> SupportIcon(tint = color) },
                    type = ModernSettingsItemType.Navigation,
                    action = onNavigateToHelp
                ),
                ModernSettingsItem(
                    title = "Send Feedback",
                    description = "Share your thoughts and suggestions",
                    icon = { color -> SupportIcon(tint = color) },
                    type = ModernSettingsItemType.Navigation,
                    action = onNavigateToFeedback
                ),
                ModernSettingsItem(
                    title = "Rate App",
                    description = "Rate VoltAI on Google Play",
                    icon = { color -> SupportIcon(tint = color) },
                    type = ModernSettingsItemType.Navigation,
                    action = { /* Rate action */ }
                )
            )
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Header
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ) + fadeIn()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            BackArrowIcon(
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Customize your VoltAI experience",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            items(settingsSections) { section ->
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(600, delayMillis = 200)
                    ) + fadeIn()
                ) {
                    SettingsSectionCard(section = section)
                }
            }
            
            item {
                // App version
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 800))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VoltAI v1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    section: ModernSettingsSection
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            section.items.forEachIndexed { index, item ->
                SettingsItemRow(item = item)
                if (index < section.items.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: ModernSettingsItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.action() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            item.icon(MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        when (val type = item.type) {
            is ModernSettingsItemType.Navigation -> {
                // Right arrow for navigation items
                val onSurface50 = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                Canvas(modifier = Modifier.size(20.dp)) {
                    val strokeWidth = 1.5.dp.toPx()
                    drawLine(
                        color = onSurface50,
                        start = Offset(size.width * 0.3f, size.height * 0.3f),
                        end = Offset(size.width * 0.7f, size.height * 0.5f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = onSurface50,
                        start = Offset(size.width * 0.7f, size.height * 0.5f),
                        end = Offset(size.width * 0.3f, size.height * 0.7f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
            is ModernSettingsItemType.Toggle -> {
                Switch(
                    checked = type.isEnabled,
                    onCheckedChange = type.onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
            is ModernSettingsItemType.Selection -> {
                Text(
                    text = type.selected,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}