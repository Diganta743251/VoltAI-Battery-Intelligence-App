package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.viewmodels.ThemeViewModel

@Composable
fun SettingsPremiumScreen(onBack: () -> Unit) {
    val themeVm: ThemeViewModel = hiltViewModel()
    val selectedTheme by themeVm.selectedTheme.collectAsState()

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
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Premium Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Consolidated premium content
        PremiumFeaturesCard()
        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            title = "🎨 Premium Themes",
            description = "Unlock and choose premium themes like E‑Paper",
        ) {
            val themes =
                listOf(
                    "electric_blue" to "⚡ Electric Blue",
                    "emerald_green" to "🌿 Emerald Green",
                    "sunset_orange" to "🌅 Sunset Orange",
                    "deep_purple" to "💜 Deep Purple",
                    "e_paper" to "📄 E-Paper (Battery Saver)",
                )

            themes.forEach { (themeId, themeName) ->
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { themeVm.setSelectedTheme(themeId) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedTheme == themeId,
                        onClick = { themeVm.setSelectedTheme(themeId) },
                        colors =
                        RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = themeName,
                        fontSize = 16.sp,
                        color =
                        if (selectedTheme == themeId) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (selectedTheme == themeId) FontWeight.Medium else FontWeight.Normal,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
