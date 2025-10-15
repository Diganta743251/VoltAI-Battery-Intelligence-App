package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.data.database.dao.DailyHealthTrend
import com.voltai.smartbatteryguardian.rememberBatteryData
import com.voltai.smartbatteryguardian.viewmodels.BatteryHealthViewModel
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun FocusedBatteryHealthScreen(onNavigateToHealthTips: () -> Unit) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    val healthVm: BatteryHealthViewModel = hiltViewModel()
    val dailyTrends by healthVm.dailyTrends.collectAsState()
    val lastFullCharge by healthVm.lastFullChargeSession.collectAsState()
    val chargedSummary by healthVm.chargedSummaryLast7d.collectAsState()
    val sessionsLast7d by healthVm.chargingSessionsLast7d.collectAsState()
    val latestCompleted by healthVm.latestCompletedSession.collectAsState()
    val latestHealth by healthVm.latestHealthRecord.collectAsState()
    val chargeDeltas by healthVm.chargeDeltasLast14d.collectAsState()

    val designCapacity =
        remember(batteryData) {
            // Try to parse numeric mAh from capacity string, fallback to 4400
            Regex("(\\d+)").find(batteryData.capacity)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 4400
        }
    val measuredCapacity =
        run {
            val retained = latestHealth?.capacityRetained
            if (retained != null) {
                (designCapacity * (retained / 100.0)).roundToInt()
            } else {
                (designCapacity * batteryData.healthPercentage / 100.0).roundToInt()
            }
        }
    var fullChargeReminder by remember { mutableStateOf(true) }
    val now = System.currentTimeMillis()
    val recentWindowMs = 14L * 24 * 60 * 60 * 1000 // 14 days
    val validForEstimation = lastFullCharge?.endTime?.let { end -> (now - end) <= recentWindowMs } ?: false

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top status header
        val healthPercent = (latestHealth?.healthPercentage ?: batteryData.healthPercentage.toDouble()).roundToInt()
        val statusText =
            when {
                healthPercent >= 85 -> stringResource(id = com.voltai.smartbatteryguardian.R.string.excellent)
                healthPercent >= 70 -> stringResource(id = com.voltai.smartbatteryguardian.R.string.good)
                healthPercent >= 50 -> stringResource(id = com.voltai.smartbatteryguardian.R.string.fair)
                else -> stringResource(id = com.voltai.smartbatteryguardian.R.string.poor)
            }
        HealthStatusHeader(
            statusText = statusText,
            percent = healthPercent,
            measuredCapacityMah = measuredCapacity,
            designCapacityMah = designCapacity,
        )

        Divider(
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            thickness = 1.dp,
        )

        // Health history
        HealthHistoryCard(dailyTrends = dailyTrends)

        // Selected session summary (latest completed session)
        SessionSummaryCard(
            healthPercent = healthPercent,
            estimatedMah = measuredCapacity,
            session = latestCompleted,
        )

        // Quick info grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SmallInfoCard(
                title = stringResource(id = com.voltai.smartbatteryguardian.R.string.last_full_charge),
                subtitle =
                formatLastFullCharge(
                    lastFullCharge,
                    stringResource(id = com.voltai.smartbatteryguardian.R.string.not_available),
                ),
                leading = { Dot(color = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.weight(1f),
            )
            SmallInfoCard(
                title = stringResource(id = com.voltai.smartbatteryguardian.R.string.valid_for_estimation),
                subtitle =
                if (validForEstimation) {
                    stringResource(
                        id = com.voltai.smartbatteryguardian.R.string.yes,
                    )
                } else {
                    stringResource(id = com.voltai.smartbatteryguardian.R.string.no)
                },
                leading = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SmallInfoCard(
                title = stringResource(id = com.voltai.smartbatteryguardian.R.string.sessions),
                subtitle = "${sessionsLast7d.size}",
                leading = { Dot(color = MaterialTheme.colorScheme.tertiary) },
                modifier = Modifier.weight(1f),
            )
            SmallInfoCard(
                title = stringResource(id = com.voltai.smartbatteryguardian.R.string.charged),
                subtitle = formatChargedSummary(chargedSummary.totalPercent, measuredCapacity),
                leading = { Dot(color = MaterialTheme.colorScheme.secondary) },
                modifier = Modifier.weight(1f),
            )
        }

        // Charging cycles based on real 14-day charge deltas
        ChargingCyclesCard(chargeDeltas = chargeDeltas)

        // Reminder toggle
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        ) {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.full_charging_reminder),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text =
                        stringResource(
                            id = com.voltai.smartbatteryguardian.R.string.full_charging_reminder_desc,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = fullChargeReminder, onCheckedChange = { fullChargeReminder = it })
            }
        }

        // Health care navigable row
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        ) {
            val healthCareCd = stringResource(id = com.voltai.smartbatteryguardian.R.string.battery_health_care_cd)
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .clickable(role = Role.Button) { onNavigateToHealthTips() }
                    .semantics { contentDescription = healthCareCd },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.battery_health_care),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.discover_health_tips),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HealthStatusHeader(
    statusText: String,
    percent: Int,
    measuredCapacityMah: Int,
    designCapacityMah: Int,
) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .semantics { heading() },
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "$percent% • $measuredCapacityMah of $designCapacityMah mAh",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HealthHistoryCard(dailyTrends: List<DailyHealthTrend>) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        val chartCd = stringResource(id = com.voltai.smartbatteryguardian.R.string.health_history_chart_cd)
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics { contentDescription = chartCd },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.health_history),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.last_30_days),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "history settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                if (dailyTrends.isEmpty()) {
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.not_available),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    HealthLineChart(dailyTrends = dailyTrends)
                }
            }
            // Legend
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    LegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = stringResource(id = com.voltai.smartbatteryguardian.R.string.legend_charging),
                    )
                }
                item {
                    LegendItem(
                        color = MaterialTheme.colorScheme.tertiary,
                        label = stringResource(id = com.voltai.smartbatteryguardian.R.string.legend_health_check),
                    )
                }
                item {
                    LegendItem(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        label = stringResource(id = com.voltai.smartbatteryguardian.R.string.legend_battery_health),
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Dot(color = color)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Dot(color: Color) {
    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
}

@Composable
private fun HealthLineChart(dailyTrends: List<DailyHealthTrend>) {
    val points = dailyTrends.map { it.avgHealth.toFloat() }
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700),
        label = "health_line_anim",
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (points.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        // Grid lines
        val gridColor = Color.Gray.copy(alpha = 0.2f)
        val rows = 5
        repeat(rows + 1) { i ->
            val y = i * (h / rows)
            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(w, y))
        }
        // Line path
        val step = if (points.size > 1) w / (points.size - 1) else w
        val path = Path()
        points.forEachIndexed { i, v ->
            val x = i * step
            val y = h - (v.coerceIn(0f, 100f) / 100f * h)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        clipRect(right = w * progress) {
            drawPath(path = path, color = lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun SessionSummaryCard(
    healthPercent: Int,
    estimatedMah: Int,
    session: com.voltai.smartbatteryguardian.data.database.entities.ChargingSession?,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(id = com.voltai.smartbatteryguardian.R.string.selected_session),
                style = MaterialTheme.typography.titleSmall,
            )
            val batteryLine =
                if (session != null && session.endLevel != null) {
                    stringResource(
                        id = com.voltai.smartbatteryguardian.R.string.battery_level_line,
                        session.startLevel,
                        session.endLevel!!,
                    )
                } else {
                    stringResource(
                        id = com.voltai.smartbatteryguardian.R.string.battery_level_line,
                        0,
                        0,
                    ).replace(Regex("\\d+% to \\d+%"), "—")
                }
            Text(
                text = batteryLine + "\n" + stringResource(id = com.voltai.smartbatteryguardian.R.string.battery_health_line, healthPercent, estimatedMah),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val whenText =
                session?.endTime?.let { end ->
                    val fmt = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT, java.util.Locale.getDefault())
                    fmt.format(java.util.Date(end))
                } ?: stringResource(id = com.voltai.smartbatteryguardian.R.string.not_available)
            Text(
                text = stringResource(id = com.voltai.smartbatteryguardian.R.string.date_label, whenText),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SmallInfoCard(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.heightIn(min = 72.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) { leading() }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ChargingCyclesCard(chargeDeltas: List<Int>) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.charging_cycles),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.based_on_app_data),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = stringResource(id = com.voltai.smartbatteryguardian.R.string.li_poly),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                CyclesBarChart(chargeDeltas)
            }
            val totalPercent = chargeDeltas.sum().coerceAtLeast(0)
            val cycles = (totalPercent / 100.0).let { kotlin.math.floor(it * 10) / 10.0 } // one decimal
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.cycles),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$cycles",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Column {
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.battery_type),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(id = com.voltai.smartbatteryguardian.R.string.li_poly),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CyclesBarChart(chargeDeltas: List<Int>) {
    val data = if (chargeDeltas.isNotEmpty()) chargeDeltas.map { it.coerceIn(0, 100).toFloat() } else List(14) { 0f }
    val barColor = MaterialTheme.colorScheme.primary
    val cyclesCd = stringResource(id = com.voltai.smartbatteryguardian.R.string.charging_cycles_chart_cd)
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600),
        label = "cycles_anim",
    )
    Canvas(
        modifier =
        Modifier
            .fillMaxSize()
            .semantics { contentDescription = cyclesCd },
    ) {
        val w = size.width
        val h = size.height
        val barCount = data.size
        val barWidth = w / (barCount * 1.8f)
        val gap = barWidth * 0.8f
        var x = 0f
        data.forEach { v ->
            val barH = (v.coerceIn(0f, 100f) / 100f) * h * progress
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, h - barH),
                size = androidx.compose.ui.geometry.Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            )
            x += barWidth + gap
        }
    }
}

private fun formatLastFullCharge(
    session: com.voltai.smartbatteryguardian.data.database.entities.ChargingSession?,
    fallback: String,
): String {
    if (session == null || session.endTime == null) return fallback
    val fmt =
        java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.MEDIUM,
            java.text.DateFormat.SHORT,
            java.util.Locale.getDefault(),
        )
    return fmt.format(java.util.Date(session.endTime))
}

private fun formatChargedSummary(
    totalPercent: Int,
    designCapacityMah: Int,
): String {
    val mah = (totalPercent * designCapacityMah / 100.0).toInt()
    return "$totalPercent% ($mah mAh)"
}

private fun statusForHealth(health: Int): String =
    when {
        health >= 85 -> "Excellent"
        health >= 70 -> "OK"
        health >= 50 -> "Fair"
        else -> "Poor"
    }
