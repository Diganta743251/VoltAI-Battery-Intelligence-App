package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsAiOptimizationScreen(onBack: () -> Unit) {
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
            Text(text = "AI & Optimization", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        var autoOptimizationEnabled by remember { mutableStateOf(true) }
        SettingsSection(title = "🤖 AI & Optimization", description = "Configure intelligent battery management") {
            SettingsToggleItem(
                title = "AI Auto-Optimization",
                description = "Let AI automatically optimize your battery usage",
                icon = Icons.Filled.Build,
                checked = autoOptimizationEnabled,
                onCheckedChange = { autoOptimizationEnabled = it },
            )
            SettingsClickableItem(
                title = "Optimization Schedule",
                description = "Set when to run automatic optimizations",
                icon = Icons.Filled.Settings,
                value = "Every 2 hours",
                onClick = { },
            )
        }
    }
}
