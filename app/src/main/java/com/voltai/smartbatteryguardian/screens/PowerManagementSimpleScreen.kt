package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.R
import com.voltai.smartbatteryguardian.preferences.PreferencesViewModel
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.viewmodels.BatteryReadingsViewModel
import com.voltai.smartbatteryguardian.viewmodels.PowerProfilesViewModel
import com.voltai.smartbatteryguardian.viewmodels.SystemMetricsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerManagementSimpleScreen() {
    val prefs: PreferencesViewModel = hiltViewModel()
    val reduceMotion by prefs.reduceMotion.collectAsState()

    val context = LocalContext.current
    val battery by rememberBatteryData(context)

    val sysVm: SystemMetricsViewModel = hiltViewModel()
    val summary by sysVm.systemSummary.collectAsState()

    val readingsVm: BatteryReadingsViewModel = hiltViewModel()
    val last24h by readingsVm.last24hReadings.collectAsState()

    val profilesVm: PowerProfilesViewModel = hiltViewModel()
    val profiles by profilesVm.profiles.collectAsState()
    val active = remember(profiles) { profiles.firstOrNull { it.isActive } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.power_management), fontWeight = FontWeight.SemiBold) },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val s1 by animateFloatAsState(if (reduceMotion) 1f else 0.995f, tween(120), label = "s1")
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().scale(s1),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(id = R.string.live_battery_status),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    val isBatteryLoading = remember(battery, last24h) { battery.batteryLevel == 0 && last24h.isEmpty() }
                    if (isBatteryLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "${battery.batteryLevel}%",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text =
                                    if (battery.isCharging) {
                                        stringResource(
                                            id = R.string.charging,
                                        )
                                    } else {
                                        battery.chargingStatus
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val currentMa = if (battery.current == Int.MIN_VALUE) 0 else battery.current
                                Text(
                                    "${stringResource(id = R.string.temp_label)} ${formatTempC(battery.temperature)}",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Text(
                                    "${stringResource(id = R.string.voltage_label)} ${formatVoltage(battery.voltage)}",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Text(
                                    "${stringResource(
                                        id = R.string.current_label,
                                    )} ${formatCurrentMaSigned(currentMa)}",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                }
            }

            val s2 by animateFloatAsState(if (reduceMotion) 1f else 0.995f, tween(120), label = "s2")
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().scale(s2),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(id = R.string.thermal_trend_24h), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    val history = remember(last24h) { last24h.map { TempPoint(it.timestamp, it.temperature, 0.0) } }
                    if (history.isEmpty()) {
                        Text(
                            stringResource(id = R.string.no_thermal_data),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        ThermalTrendChart(history)
                    }
                }
            }

            val s3 by animateFloatAsState(if (reduceMotion) 1f else 0.995f, tween(120), label = "s3")
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().scale(s3),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(id = R.string.system_performance_24h),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    if (summary == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        val s = summary!!
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val cpuEff = (100 - s.avgCpu).toInt().coerceIn(0, 100)
                            Metric(stringResource(id = R.string.cpu_label), "$cpuEff%")
                            Metric(stringResource(id = R.string.memory_label), "${s.avgMemory.toInt()}%")
                            Metric(stringResource(id = R.string.perf_label), s.avgPerformance.toInt().toString())
                        }
                    }
                }
            }

            val s4 by animateFloatAsState(if (reduceMotion) 1f else 0.995f, tween(120), label = "s4")
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().scale(s4),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(id = R.string.active_power_profile),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(6.dp))
                    if (profiles.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.loading_profiles),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(profiles.filter { !it.isCustom }) { p ->
                                val profileCd = stringResource(id = R.string.power_profile_cd, p.name)
                                FilterChip(
                                    modifier = Modifier.semantics { contentDescription = profileCd },
                                    selected = p.id == active?.id,
                                    onClick = { profilesVm.setActive(p.id) },
                                    label = { Text(p.name) },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (active != null) {
                        Text(active.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (active.description.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                active.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Text(
                            stringResource(id = R.string.no_active_profile),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Formatting helpers for consistent units
private fun formatTempC(tempC: Float): String {
    return "${tempC.toInt()}°C"
}

private fun formatVoltage(volts: Double): String {
    return String.format("%.2f V", volts)
}

private fun formatCurrentMaSigned(currentMa: Int): String {
    // Assume current in mA; sign reflects direction (charging positive)
    return "$currentMa mA"
}
