package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoOptimizationEnabled by remember { mutableStateOf(true) }
    var batteryAlertsEnabled by remember { mutableStateOf(true) }
    var temperatureAlertsEnabled by remember { mutableStateOf(true) }
    var selectedTheme by remember { mutableStateOf("Auto") }
    var selectedLanguage by remember { mutableStateOf("English") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "VoltAI Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize your battery guardian",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // AI & Optimization Settings
        SettingsSection(
            title = "🤖 AI & Optimization",
            description = "Configure intelligent battery management"
        ) {
            SettingsToggleItem(
                title = "AI Auto-Optimization",
                description = "Let AI automatically optimize your battery usage",
                icon = Icons.Default.Build,
                checked = autoOptimizationEnabled,
                onCheckedChange = { autoOptimizationEnabled = it }
            )
            
            SettingsClickableItem(
                title = "Optimization Schedule",
                description = "Set when to run automatic optimizations",
                icon = Icons.Default.Settings,
                value = "Every 2 hours",
                onClick = { /* Show schedule picker */ }
            )
            
            SettingsClickableItem(
                title = "AI Learning Level",
                description = "How aggressively AI learns your patterns",
                icon = Icons.Default.Build,
                value = "Balanced",
                onClick = { /* Show learning options */ }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Alerts & Notifications
        SettingsSection(
            title = "🔔 Alerts & Notifications",
            description = "Stay informed about your battery status"
        ) {
            SettingsToggleItem(
                title = "Smart Notifications",
                description = "Receive intelligent battery alerts",
                icon = Icons.Default.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            
            SettingsToggleItem(
                title = "Battery Level Alerts",
                description = "Alert when battery reaches critical levels",
                icon = Icons.Default.Warning,
                checked = batteryAlertsEnabled,
                onCheckedChange = { batteryAlertsEnabled = it }
            )
            
            SettingsToggleItem(
                title = "Temperature Warnings",
                description = "Alert when battery gets too hot",
                icon = Icons.Default.Settings,
                checked = temperatureAlertsEnabled,
                onCheckedChange = { temperatureAlertsEnabled = it }
            )
            
            SettingsClickableItem(
                title = "Notification Sound",
                description = "Choose alert sound",
                icon = Icons.Default.Settings,
                value = "Default",
                onClick = { /* Show sound picker */ }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Appearance Settings
        SettingsSection(
            title = "🎨 Appearance",
            description = "Customize the app's look and feel"
        ) {
            SettingsToggleItem(
                title = "Dark Mode",
                description = "Use dark theme for better battery life",
                icon = Icons.Default.Settings,
                checked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )
            
            SettingsClickableItem(
                title = "Theme Style",
                description = "Choose your preferred theme",
                icon = Icons.Default.Settings,
                value = selectedTheme,
                onClick = { /* Show theme picker */ }
            )
            
            SettingsClickableItem(
                title = "Language",
                description = "Select app language",
                icon = Icons.Default.Settings,
                value = selectedLanguage,
                onClick = { /* Show language picker */ }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Advanced Settings
        SettingsSection(
            title = "⚙️ Advanced",
            description = "Expert battery management options"
        ) {
            SettingsClickableItem(
                title = "Battery Calibration",
                description = "Improve battery reading accuracy",
                icon = Icons.Default.Settings,
                value = "Last: 5 days ago",
                onClick = { /* Start calibration */ }
            )
            
            SettingsClickableItem(
                title = "Data Export",
                description = "Export battery data for analysis",
                icon = Icons.Default.Settings,
                value = "CSV, JSON",
                onClick = { /* Export data */ }
            )
            
            SettingsClickableItem(
                title = "Reset AI Learning",
                description = "Clear all learned patterns",
                icon = Icons.Default.Refresh,
                value = "Caution",
                onClick = { /* Reset AI */ }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // About Section
        SettingsSection(
            title = "📱 About VoltAI",
            description = "App information and support"
        ) {
            SettingsClickableItem(
                title = "Version",
                description = "Current app version",
                icon = Icons.Default.Info,
                value = "1.0.0 Pro",
                onClick = { /* Show version info */ }
            )
            
            SettingsClickableItem(
                title = "Privacy Policy",
                description = "How we protect your data",
                icon = Icons.Default.Settings,
                value = "",
                onClick = { /* Open privacy policy */ }
            )
            
            SettingsClickableItem(
                title = "Rate VoltAI",
                description = "Help us improve with your feedback",
                icon = Icons.Default.Star,
                value = "⭐⭐⭐⭐⭐",
                onClick = { /* Open play store */ }
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            content()
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    description: String,
    icon: ImageVector,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
        
        if (value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
