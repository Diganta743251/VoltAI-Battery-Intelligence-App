package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.viewmodels.ThemeViewModel

@Composable
fun SettingsAppearanceScreen(onBack: () -> Unit) {
    val themeVm: ThemeViewModel = hiltViewModel()
    val selectedTheme by themeVm.selectedTheme.collectAsState()
    val reduceMotion by themeVm.reduceMotion.collectAsState()

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(24.dp).clickable { onBack() },
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Appearance", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "🎨 Appearance", description = "Customize the app's look and feel") {
            SettingsClickableItem(
                title = "Theme Style",
                description = "Choose your preferred theme",
                icon = Icons.Filled.Settings,
                value = selectedTheme,
                onClick = { },
            )
            Text(
                text = "Premium themes (like E‑Paper) are available under Premium settings.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
            SettingsToggleItem(
                title = "Reduce Animations",
                description = "Minimize motion effects for comfort and battery savings",
                icon = Icons.Filled.Settings,
                checked = reduceMotion,
                onCheckedChange = { themeVm.setReduceMotion(it) },
            )
        }
    }
}
