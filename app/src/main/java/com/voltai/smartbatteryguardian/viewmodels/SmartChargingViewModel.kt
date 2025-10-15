package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.smartbatteryguardian.data.database.dao.BatteryDao
import com.voltai.smartbatteryguardian.data.database.entities.ChargingSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SmartChargingViewModel
@Inject
constructor(
    private val batteryDao: BatteryDao,
) : ViewModel() {
    private val last7dStart: Long
        get() = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000L

    data class SmartChargingSummary(
        val avgEfficiency: Double?,
        val avgDurationMs: Long?,
        val avgPowerW: Double?,
        val sessionsCount: Int,
        val optimalWindow: String?,
    )

    val summary: StateFlow<SmartChargingSummary> =
        batteryDao.getChargingSessionsSince(last7dStart)
            .map { sessions ->
                computeSummary(sessions)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                SmartChargingSummary(null, null, null, 0, null),
            )

    private fun computeSummary(sessions: List<ChargingSession>): SmartChargingSummary {
        if (sessions.isEmpty()) {
            return SmartChargingSummary(null, null, null, 0, null)
        }
        val completed = sessions.filter { it.endTime != null && it.duration > 0 }

        val count = completed.size
        // Fallback energy if missing: avgPower * duration(h)
        val energyUsedWh: (ChargingSession) -> Double = { s ->
            val e = s.energyConsumed
            if (e > 0) {
                e
            } else if (s.avgPower > 0 && s.duration > 0) {
                s.avgPower * (s.duration / 3_600_000.0)
            } else {
                0.0
            }
        }
        val totalEnergyWh = completed.sumOf { energyUsedWh(it) }
        val totalTimeHours = completed.sumOf { it.duration } / 3_600_000.0
        val avgEff =
            if (totalEnergyWh > 0) {
                (completed.sumOf { it.efficiency * energyUsedWh(it) } / totalEnergyWh).coerceIn(0.0, 100.0)
            } else {
                completed.map { it.efficiency }.takeIf { it.isNotEmpty() }?.average()?.coerceIn(0.0, 100.0)
            }
        val avgDur = completed.map { it.duration }.takeIf { it.isNotEmpty() }?.average()?.toLong()
        val avgPower = if (totalTimeHours > 0) totalEnergyWh / totalTimeHours else null
        val optimal = computeOptimalWindow(completed)

        return SmartChargingSummary(
            avgEfficiency = avgEff,
            avgDurationMs = avgDur,
            avgPowerW = avgPower,
            sessionsCount = count,
            optimalWindow = optimal,
        )
    }

    private fun computeOptimalWindow(sessions: List<ChargingSession>): String? {
        if (sessions.isEmpty()) return null
        val cal = Calendar.getInstance()
        val histogram = DoubleArray(24)
        for (s in sessions) {
            cal.timeInMillis = s.startTime
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minutes = (s.duration / 60_000.0).coerceAtLeast(0.0)
            if (hour in 0..23) histogram[hour] += minutes
        }
        val maxHour = histogram.indices.maxByOrNull { histogram[it] } ?: return null
        return String.format(Locale.getDefault(), "%02d:00–%02d:59", maxHour, maxHour)
    }

    fun formatDuration(ms: Long?): String {
        if (ms == null || ms <= 0) return "—"
        val totalMinutes = ms / 60000
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
