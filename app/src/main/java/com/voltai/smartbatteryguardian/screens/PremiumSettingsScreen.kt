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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.ui.theme.extendedColors

data class SettingsSection(
    val title: String,
    val icon: String,
    val items: List<SettingsItem>,
)

data class SettingsItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val type: SettingsType,
    val value: Any? = null,
    val isPremium: Boolean = false,
    val action: () -> Unit = {},
)

enum class SettingsType { SWITCH, NAVIGATION, SLIDER, DROPDOWN, ACTION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSettingsScreen() {
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("⚙️ General", "🔋 Battery", "🎨 Interface", "🔒 Privacy", "💎 Premium")

    val settingsSections = remember { generateSettingsSections() }

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors =
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                ),
            ),
    ) {
        // Premium Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.tertiary,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                        Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "⚙️", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "VoltAI Settings",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface,
                        )
                        Text(
                            text = "Professional battery management",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = "💎 Pro",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(8.dp),
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
                0 ->
                    items(
                        settingsSections.filter { it.title in listOf("General", "Notifications", "Data & Storage") },
                    ) { section ->
                        SettingsSectionCard(section)
                    }
                1 ->
                    items(
                        settingsSections.filter { it.title in listOf("Battery Monitoring", "Power Optimization", "Charging") },
                    ) { section ->
                        SettingsSectionCard(section)
                    }
                2 ->
                    items(
                        settingsSections.filter { it.title in listOf("Appearance", "Home Screen", "Charts & Graphs") },
                    ) { section ->
                        SettingsSectionCard(section)
                    }
                3 ->
                    items(
                        settingsSections.filter { it.title in listOf("Privacy & Security", "Data Collection", "Permissions") },
                    ) { section ->
                        SettingsSectionCard(section)
                    }
                4 -> item { PremiumFeaturesCard() }
            }

            // About Section
            item {
                AboutCard()
            }
        }
    }
}

@Composable
fun SettingsSectionCard(section: SettingsSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = section.icon,
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = section.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            section.items.forEach { item ->
                SettingsItemRow(item)
                if (item != section.items.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(item: SettingsItem) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .clickable { if (item.type != SettingsType.SWITCH) item.action() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (item.isPremium) MaterialTheme.extendedColors.premiumGold else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                if (item.isPremium) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.extendedColors.premiumGoldContainer,
                    ) {
                        Text(
                            text = "💎",
                            fontSize = 8.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            if (item.subtitle.isNotEmpty()) {
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when (item.type) {
            SettingsType.SWITCH -> {
                var isChecked by remember { mutableStateOf(item.value as? Boolean ?: false) }
                Switch(
                    checked = isChecked,
                    onCheckedChange = {
                        isChecked = it
                        item.action()
                    },
                    colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                        checkedTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                    ),
                )
            }
            SettingsType.NAVIGATION -> {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            SettingsType.ACTION -> {
                if (item.value != null) {
                    Text(
                        text = item.value.toString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            else -> { /* Handle other types */ }
        }
    }
}

@Composable
fun PremiumFeaturesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.extendedColors.premiumGold.copy(alpha = 0.1f),
        ),
        border = BorderStroke(2.dp, MaterialTheme.extendedColors.premiumGold),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "💎", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Premium Features",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(
                        text = "Unlock advanced battery management",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            listOf(
                "🧠 AI-powered battery optimization",
                "📊 Advanced analytics and insights",
                "🔋 Custom power profiles",
                "📈 Detailed health predictions",
                "🌡️ Thermal management controls",
                "📤 Data export (CSV/PDF)",
                "🎨 Premium themes and customization",
                "⚡ Background monitoring",
            ).forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "✨",
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        fontSize = 14.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { /* Open premium purchase */ },
                modifier = Modifier.fillMaxWidth(),
                colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.extendedColors.premiumGold,
                    contentColor = MaterialTheme.extendedColors.onPremiumGold,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Upgrade to Premium",
                    color = MaterialTheme.extendedColors.onPremiumGold,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "🔋",
                fontSize = 48.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "VoltAI Battery Guardian",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Version 2.1.0 Professional",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Advanced AI-powered battery management for Android",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

fun generateSettingsSections(): List<SettingsSection> {
    return listOf(
        SettingsSection(
            title = "General",
            icon = "⚙️",
            items =
            listOf(
                SettingsItem("Language", "English", Icons.Default.Settings, SettingsType.NAVIGATION),
                SettingsItem("Units", "Metric", Icons.Default.Settings, SettingsType.NAVIGATION),
                SettingsItem("Auto-start", "Launch on boot", Icons.Default.PlayArrow, SettingsType.SWITCH, true),
                SettingsItem(
                    "Background monitoring",
                    "Continuous battery tracking",
                    Icons.Default.Settings,
                    SettingsType.SWITCH,
                    true,
                    true,
                ),
            ),
        ),
        SettingsSection(
            title = "Notifications",
            icon = "🔔",
            items =
            listOf(
                SettingsItem(
                    "Battery alerts",
                    "Low battery warnings",
                    Icons.Default.Notifications,
                    SettingsType.SWITCH,
                    true,
                ),
                SettingsItem(
                    "Health notifications",
                    "Battery health updates",
                    Icons.Default.Favorite,
                    SettingsType.SWITCH,
                    true,
                ),
                SettingsItem(
                    "Charging complete",
                    "Full charge alerts",
                    Icons.Default.Build,
                    SettingsType.SWITCH,
                    false,
                ),
                SettingsItem(
                    "AI recommendations",
                    "Smart optimization tips",
                    Icons.Default.Star,
                    SettingsType.SWITCH,
                    true,
                    true,
                ),
            ),
        ),
        SettingsSection(
            title = "Data & Storage",
            icon = "💾",
            items =
            listOf(
                SettingsItem(
                    "Export data",
                    "CSV/PDF reports",
                    Icons.Default.Share,
                    SettingsType.ACTION,
                    "Export",
                    true,
                ),
                SettingsItem(
                    "Import settings",
                    "Restore configuration",
                    Icons.Default.Settings,
                    SettingsType.ACTION,
                    "Import",
                    true,
                ),
                SettingsItem("Clear cache", "Free up storage", Icons.Default.Delete, SettingsType.ACTION, "Clear"),
                SettingsItem(
                    "Data retention",
                    "30 days",
                    Icons.Default.Settings,
                    SettingsType.NAVIGATION,
                    isPremium = true,
                ),
            ),
        ),
        SettingsSection(
            title = "Battery Monitoring",
            icon = "🔋",
            items =
            listOf(
                SettingsItem(
                    "Real-time tracking",
                    "Continuous monitoring",
                    Icons.Default.Build,
                    SettingsType.SWITCH,
                    true,
                ),
                SettingsItem(
                    "Health analysis",
                    "Deep battery diagnostics",
                    Icons.Default.Info,
                    SettingsType.SWITCH,
                    true,
                    true,
                ),
                SettingsItem(
                    "Temperature alerts",
                    "Thermal warnings",
                    Icons.Default.Warning,
                    SettingsType.SWITCH,
                    true,
                ),
                SettingsItem(
                    "Calibration reminders",
                    "Monthly calibration",
                    Icons.Default.Settings,
                    SettingsType.SWITCH,
                    false,
                    true,
                ),
            ),
        ),
        SettingsSection(
            title = "Privacy & Security",
            icon = "🔒",
            items =
            listOf(
                SettingsItem(
                    "Data encryption",
                    "Secure local storage",
                    Icons.Default.Lock,
                    SettingsType.SWITCH,
                    true,
                    true,
                ),
                SettingsItem(
                    "Analytics sharing",
                    "Help improve VoltAI",
                    Icons.Default.Share,
                    SettingsType.SWITCH,
                    false,
                ),
                SettingsItem(
                    "Crash reports",
                    "Automatic bug reporting",
                    Icons.Default.Warning,
                    SettingsType.SWITCH,
                    true,
                ),
                SettingsItem(
                    "Reset all data",
                    "Factory reset",
                    Icons.Default.Refresh,
                    SettingsType.ACTION,
                    "Reset",
                ),
            ),
        ),
    )
}
