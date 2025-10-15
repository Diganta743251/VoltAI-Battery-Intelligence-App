package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.rememberBatteryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTipsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val tips = deriveHealthTips(batteryData.temperature, batteryData.healthPercentage, batteryData.current)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery health care") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .semantics { contentDescription = "Battery health care tips" },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Improve and preserve your battery health",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
            }
            items(tips) { tip ->
                Surface(shape = MaterialTheme.shapes.medium) {
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

private fun deriveHealthTips(
    tempC: Float,
    healthPercent: Int,
    currentMa: Int,
): List<String> {
    val tips = mutableListOf<String>()

    if (tempC > 35f) {
        tips.add("Reduce device temperature by removing thick cases and closing intensive apps during charging.")
    }
    if (healthPercent < 80) {
        tips.add("Avoid frequent 100% charges — staying around 20–80% helps preserve capacity.")
        tips.add("Use original or certified chargers to prevent voltage irregularities.")
    }
    tips.add("Enable adaptive charging if available to limit overnight stress.")
    tips.add("Avoid extreme temperatures (below 0°C or above 35°C) when possible.")

    if (currentMa < -1500) {
        tips.add("High power draw detected — reduce screen brightness and close background apps.")
    }

    tips.add("Schedule an occasional full calibration cycle when recommended by the app.")
    return tips
}
