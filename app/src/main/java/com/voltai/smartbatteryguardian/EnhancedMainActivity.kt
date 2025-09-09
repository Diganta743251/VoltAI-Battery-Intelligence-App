package com.voltai.smartbatteryguardian

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

class EnhancedMainActivity : ComponentActivity() {
    private var batteryReceiver: BroadcastReceiver? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            com.voltai.smartbatteryguardian.ui.theme.VoltAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    MaterialBatteryScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MaterialBatteryScreen() {
        var batteryLevel by remember { mutableStateOf(0) }
        var isCharging by remember { mutableStateOf(false) }
        var voltage by remember { mutableStateOf(0f) }
        var temperature by remember { mutableStateOf(0f) }
        var health by remember { mutableStateOf(0) }
        var status by remember { mutableStateOf("Unknown") }

        // Auto-refresh battery info
        LaunchedEffect(Unit) {
            while (true) {
                val batteryInfo = getBatteryInfoEnhanced(this@EnhancedMainActivity)
                batteryLevel = batteryInfo.level
                isCharging = batteryInfo.isCharging
                voltage = batteryInfo.voltage
                temperature = batteryInfo.temperature.toFloat()
                health = batteryInfo.health
                status = batteryInfo.status
                delay(5000) // Update every 5 seconds
            }
        }

        DisposableEffect(Unit) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.let {
                        batteryLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                        val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                        batteryLevel = (batteryLevel * 100) / scale

                        val batteryStatus = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                            batteryStatus == BatteryManager.BATTERY_STATUS_FULL

                        voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
                        temperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f

                        val healthValue = it.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                        health = when (healthValue) {
                            BatteryManager.BATTERY_HEALTH_GOOD -> 100
                            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 75
                            BatteryManager.BATTERY_HEALTH_DEAD -> 0
                            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 60
                            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 50
                            BatteryManager.BATTERY_HEALTH_COLD -> 80
                            else -> 95
                        }

                        status = when (batteryStatus) {
                            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                            BatteryManager.BATTERY_STATUS_FULL -> "Full"
                            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                            else -> "Unknown"
                        }
                    }
                }
            }

            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(receiver, filter)
            batteryReceiver = receiver

            onDispose {
                batteryReceiver?.let { unregisterReceiver(it) }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⚡ VoltAI",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Smart Battery Guardian",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Simple Battery Ring
                SimpleBatteryRing(
                    batteryLevel = batteryLevel,
                    isCharging = isCharging
                )

                // Battery Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "$batteryLevel%",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = getBatteryColorMaterial(batteryLevel)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isCharging) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Charging",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Charging",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "On Battery",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "On Battery",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Text(
                            text = status,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Enhanced Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MaterialStatCard(
                        title = "Health",
                        value = "$health%",
                        icon = "❤️",
                        modifier = Modifier.weight(1f)
                    )
                    MaterialStatCard(
                        title = "Temp",
                        value = "${String.format(Locale.US, "%.1f", temperature)}°C",
                        icon = "🌡️",
                        modifier = Modifier.weight(1f)
                    )
                    MaterialStatCard(
                        title = "Voltage",
                        value = "${String.format(Locale.US, "%.2f", voltage)}V",
                        icon = "⚡",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Button
                Button(
                    onClick = {
                        // Force refresh - the LaunchedEffect will handle updates
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Refresh Battery Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text = "VoltAI v1.0.0 - Smart Battery Management",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    fun SimpleBatteryRing(
        batteryLevel: Int,
        isCharging: Boolean
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = batteryLevel / 100f,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = getBatteryColorMaterial(batteryLevel),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Center content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = getBatteryColorMaterial(batteryLevel)
                )
                if (isCharging) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Charging",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun MaterialStatCard(
        title: String,
        value: String,
        icon: String,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    @Composable
    private fun getBatteryColorMaterial(level: Int): Color {
        return when {
            level > 80 -> MaterialTheme.colorScheme.primary
            level > 50 -> MaterialTheme.colorScheme.secondary  
            level > 20 -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.error
        }
    }

    data class BatteryInfoEnhanced(
        val level: Int,
        val isCharging: Boolean,
        val health: Int,
        val temperature: Int,
        val voltage: Float,
        val status: String,
    )

    private fun getBatteryInfoEnhanced(context: Context): BatteryInfoEnhanced {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = (level * 100 / scale.coerceAtLeast(1))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: 0
        val healthPct = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 100
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 75
            BatteryManager.BATTERY_HEALTH_DEAD -> 0
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 60
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 50
            BatteryManager.BATTERY_HEALTH_COLD -> 80
            else -> 95
        }

        val temperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = temperature / 10

        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val voltageFloat = voltage / 1000f

        val statusText = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }

        return BatteryInfoEnhanced(
            level = batteryPct,
            isCharging = isCharging,
            health = healthPct,
            temperature = tempCelsius,
            voltage = voltageFloat,
            status = statusText,
        )
    }
}
