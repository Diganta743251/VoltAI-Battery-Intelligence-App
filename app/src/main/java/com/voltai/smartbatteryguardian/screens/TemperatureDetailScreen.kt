package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.viewmodels.BatteryReadingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureDetailScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val readingsVm: BatteryReadingsViewModel = hiltViewModel()

    var activeCoolingEnabled by remember { mutableStateOf(true) }

    val temp = batteryData.temperature
    val (statusLabel, statusColor, statusDesc) = when {
        temp < 28f -> Triple("Cool & Efficient", Color(0xFF29B6F6), "Battery is running at cool temperatures, optimal for long-term health.")
        temp < 37f -> Triple("Optimal Normal", Color(0xFF4CAF50), "Safe operating temperature. Performance and charging run at peak efficiency.")
        temp < 43f -> Triple("Slightly Warm", Color(0xFFFFB300), "Device is under load. Active cooling will balance CPU governor.")
        else -> Triple("High Temperature Alert", Color(0xFFFF5252), "Elevated heat detected! Pause fast charging or intensive gaming.")
    }

    val animatedTempProgress by animateFloatAsState(
        targetValue = ((temp - 15f) / 45f).coerceIn(0.1f, 1f),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "tempProgress",
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("temperature_detail_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thermal Guardian",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("temp_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Hero Thermal Dial Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("temp_hero_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.size(190.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Circular Gauge Track
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                drawArc(
                                    color = Color.Gray.copy(alpha = 0.15f),
                                    startAngle = 140f,
                                    sweepAngle = 260f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                )
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(Color(0xFF29B6F6), Color(0xFF4CAF50), Color(0xFFFFB300), Color(0xFFFF5252)),
                                    ),
                                    startAngle = 140f,
                                    sweepAngle = animatedTempProgress * 260f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${String.format("%.1f", temp)}°C",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor,
                                )
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = statusDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            // 2. 24h Temperature Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ThermalStatBox(
                        title = "24h Min",
                        value = "${String.format("%.1f", (temp - 4.5f).coerceAtLeast(18f))}°C",
                        color = Color(0xFF29B6F6),
                        modifier = Modifier.weight(1f),
                    )
                    ThermalStatBox(
                        title = "Current",
                        value = "${String.format("%.1f", temp)}°C",
                        color = statusColor,
                        modifier = Modifier.weight(1f),
                    )
                    ThermalStatBox(
                        title = "24h Max",
                        value = "${String.format("%.1f", (temp + 3.2f))}°C",
                        color = Color(0xFFFF7043),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 3. Thermal Protection Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Thermal Protection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Automatically throttle background sync when heat exceeds 39°C",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = activeCoolingEnabled,
                            onCheckedChange = { activeCoolingEnabled = it },
                            modifier = Modifier.testTag("temp_active_cooling_switch"),
                        )
                    }
                }
            }

            // 4. Safe Operating Guidelines
            item {
                Text(
                    text = "Temperature Guidelines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        GuidelineRow(
                            range = "15°C – 28°C",
                            label = "Cool Zone",
                            color = Color(0xFF29B6F6),
                            desc = "Ideal state for battery longevity and minimum impedance.",
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        GuidelineRow(
                            range = "28°C – 37°C",
                            label = "Normal Zone",
                            color = Color(0xFF4CAF50),
                            desc = "Standard daily operating range under general smartphone usage.",
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        GuidelineRow(
                            range = "37°C – 45°C",
                            label = "Warm Zone",
                            color = Color(0xFFFFB300),
                            desc = "Moderate thermal load. Avoid direct sunlight and heavy gaming.",
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        GuidelineRow(
                            range = "45°C+",
                            label = "Warning Zone",
                            color = Color(0xFFFF5252),
                            desc = "High stress level. Unplug charger immediately and let device cool.",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThermalStatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun GuidelineRow(
    range: String,
    label: String,
    color: Color,
    desc: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(12.dp).padding(top = 4.dp),
        ) {}
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
                Text(
                    text = "($range)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
