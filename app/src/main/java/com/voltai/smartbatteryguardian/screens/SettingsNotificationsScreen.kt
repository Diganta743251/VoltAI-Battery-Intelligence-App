package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsNotificationsScreen(onBack: () -> Unit) {
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
            Text(text = "Alerts & Notifications", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        var notificationsEnabled by remember { mutableStateOf(true) }
        var batteryAlertsEnabled by remember { mutableStateOf(true) }
        var temperatureAlertsEnabled by remember { mutableStateOf(true) }

        SettingsSection(
            title = "🔔 Alerts & Notifications",
            description = "Stay informed about your battery status",
        ) {
            SettingsToggleItem(
                title = "Smart Notifications",
                description = "Receive intelligent battery alerts",
                icon = Icons.Filled.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it },
            )
            SettingsToggleItem(
                title = "Battery Level Alerts",
                description = "Alert when battery reaches critical levels",
                icon = Icons.Filled.Notifications,
                checked = batteryAlertsEnabled,
                onCheckedChange = { batteryAlertsEnabled = it },
            )
            SettingsToggleItem(
                title = "Temperature Warnings",
                description = "Alert when battery gets too hot",
                icon = Icons.Filled.Notifications,
                checked = temperatureAlertsEnabled,
                onCheckedChange = { temperatureAlertsEnabled = it },
            )
        }
    }
}
