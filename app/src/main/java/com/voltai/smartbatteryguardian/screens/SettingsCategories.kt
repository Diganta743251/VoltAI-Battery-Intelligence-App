package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsCategoriesScreen(onNavigateTo: (String) -> Unit) {
    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(text = "Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Choose a category",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        SettingsCategoryItem(
            title = "AI & Optimization",
            description = "Configure intelligent battery management",
            icon = Icons.Filled.Build,
            onClick = { onNavigateTo("settings/ai") },
        )
        SettingsCategoryItem(
            title = "Alerts & Notifications",
            description = "Stay informed about your battery status",
            icon = Icons.Filled.Notifications,
            onClick = { onNavigateTo("settings/notifications") },
        )
        SettingsCategoryItem(
            title = "Appearance",
            description = "Customize the app's look and feel",
            icon = Icons.Filled.Settings,
            onClick = { onNavigateTo("settings/appearance") },
        )
        SettingsCategoryItem(
            title = "Advanced",
            description = "Expert battery management options",
            icon = Icons.Filled.Settings,
            onClick = { onNavigateTo("settings/advanced") },
        )
        SettingsCategoryItem(
            title = "Premium",
            description = "All premium features in one place",
            icon = Icons.Filled.Star,
            onClick = { onNavigateTo("settings/premium") },
        )
        SettingsCategoryItem(
            title = "About VoltAI",
            description = "App information and support",
            icon = Icons.Filled.Info,
            onClick = { onNavigateTo("settings/about") },
        )
    }
}

@Composable
private fun SettingsCategoryItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
