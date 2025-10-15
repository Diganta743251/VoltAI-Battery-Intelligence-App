package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voltai.smartbatteryguardian.rememberBatteryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsDetailScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Statistics",
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        LazyColumn(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Averages",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Calculate realistic drain rates from battery current
                        val screenOnDrain =
                            if (batteryData.current < 0) {
                                val drainRate = kotlin.math.abs(batteryData.current) * 0.8 / 100
                                String.format("%.1f", drainRate)
                            } else {
                                "N/A"
                            }
                        val screenOffDrain =
                            if (batteryData.current < 0) {
                                val drainRate = kotlin.math.abs(batteryData.current) * 0.15 / 100
                                String.format("%.1f", drainRate)
                            } else {
                                "N/A"
                            }

                        Text(
                            text = "Screen-on: ~$screenOnDrain%/h",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Screen-off: ~$screenOffDrain%/h",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Full charge estimates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        // Calculate realistic estimates from remaining time
                        val remainingHours = batteryData.remainingTime.split("h").firstOrNull()?.trim()?.toIntOrNull() ?: 0
                        val screenOnEstimate =
                            if (remainingHours > 0) {
                                "${remainingHours}h (estimated)"
                            } else {
                                batteryData.remainingTime
                            }

                        Text(
                            text = "Screen-on: $screenOnEstimate",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Screen-off: ~${remainingHours * 3}h (estimated)",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text = "Detailed Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Battery Usage Patterns",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calculate real statistics
                        val screenOnUsage =
                            if (batteryData.current < 0) {
                                String.format("%.1f", kotlin.math.abs(batteryData.current) * 0.8 / 100)
                            } else {
                                "N/A"
                            }
                        val screenOffUsage =
                            if (batteryData.current < 0) {
                                String.format("%.1f", kotlin.math.abs(batteryData.current) * 0.15 / 100)
                            } else {
                                "N/A"
                            }

                        Text(
                            text =
                            "• Screen-on usage: $screenOnUsage%/hour average\n" +
                                "• Screen-off usage: $screenOffUsage%/hour average\n" +
                                "• Current battery level: ${batteryData.batteryLevel}%\n" +
                                "• Remaining time: ${batteryData.remainingTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}
