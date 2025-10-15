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

@Composable
fun SettingsAdvancedScreen(onBack: () -> Unit) {
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
            Text(text = "Advanced", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "⚙️ Advanced", description = "Expert battery management options") {
            SettingsClickableItem(
                title = "Battery Calibration",
                description = "Improve battery reading accuracy",
                icon = Icons.Filled.Settings,
                value = "Last: 5 days ago",
                onClick = { },
            )
            SettingsClickableItem(
                title = "Data Export",
                description = "Export battery data for analysis",
                icon = Icons.Filled.Settings,
                value = "CSV, JSON",
                onClick = { },
            )
        }
    }
}
