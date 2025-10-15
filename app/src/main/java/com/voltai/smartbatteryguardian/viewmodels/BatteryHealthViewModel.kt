package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.smartbatteryguardian.data.database.dao.BatteryDao
import com.voltai.smartbatteryguardian.data.database.dao.DailyHealthTrend
import com.voltai.smartbatteryguardian.data.database.entities.BatteryHealthRecord
import com.voltai.smartbatteryguardian.data.database.entities.ChargingSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class BatteryHealthViewModel
@Inject
constructor(
    private val batteryDao: BatteryDao,
) : ViewModel() {
    private val last30dStart: Long
        get() = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L
    private val last7dStart: Long
        get() = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000L
    private val last14dStart: Long
        get() = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000L
    private val last90dStart: Long
        get() = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000L

    val dailyTrends: StateFlow<List<DailyHealthTrend>> =
        flow {
            emit(batteryDao.getDailyHealthTrends(last30dStart))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Recent charging sessions (last 50)
    val recentChargingSessions: StateFlow<List<ChargingSession>> =
        batteryDao.getRecentChargingSessions(limit = 50)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Charging sessions in the last 7 days
    val chargingSessionsLast7d: StateFlow<List<ChargingSession>> =
        batteryDao.getChargingSessionsSince(startTime = last7dStart)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Last completed full charge session (endLevel >= 95 and has endTime)
    val lastFullChargeSession: StateFlow<ChargingSession?> =
        recentChargingSessions
            .map { sessions ->
                sessions.firstOrNull { it.endTime != null && (it.endLevel ?: 0) >= 95 }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Summary of charged percentage across last 7 days
    data class ChargedSummary(val totalPercent: Int)

    val chargedSummaryLast7d: StateFlow<ChargedSummary> =
        chargingSessionsLast7d
            .map { sessions ->
                val percent =
                    sessions.filter { it.endTime != null && it.endLevel != null }
                        .sumOf { (it.endLevel!! - it.startLevel).coerceAtLeast(0) }
                ChargedSummary(totalPercent = percent)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChargedSummary(0))

    // Latest completed session (not necessarily full), by most recent endTime
    val latestCompletedSession: StateFlow<ChargingSession?> =
        recentChargingSessions
            .map { sessions ->
                sessions
                    .filter { it.endTime != null }
                    .maxByOrNull { it.endTime ?: Long.MIN_VALUE }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Latest battery health record for accurate capacity retained
    val latestHealthRecord: StateFlow<BatteryHealthRecord?> =
        flow {
            emit(batteryDao.getLatestHealthRecord())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // 14-day daily charged deltas (sum of positive (end-start) per day)
    val chargeDeltasLast14d: StateFlow<List<Int>> =
        batteryDao.getChargingSessionsSince(startTime = last14dStart)
            .map { sessions ->
                val dateFmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val grouped =
                    sessions
                        .filter { it.endTime != null && it.endLevel != null }
                        .groupBy { dateFmt.format(java.util.Date(it.endTime!!)) }
                        .mapValues { (_, list) ->
                            list.sumOf { (it.endLevel!! - it.startLevel).coerceAtLeast(0) }
                        }

                // Build series for the last 14 days in chronological order
                val now = System.currentTimeMillis()
                val oneDay = 24L * 60 * 60 * 1000L
                val result = ArrayList<Int>(14)
                for (i in 13 downTo 0) {
                    val dayMs = now - i * oneDay
                    val key = dateFmt.format(java.util.Date(dayMs))
                    result.add(grouped[key] ?: 0)
                }
                result
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), List(14) { 0 })

    // Today start timestamp (local)
    private val todayStart: Long
        get() {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    data class DayChargingOverview(
        val sessionsCount: Int,
        val totalTimeMs: Long,
        val energyWh: Double?,
        val maxTemp: Double?,
        val efficiencyAvg: Double?,
    )

    // Charging sessions for today
    val chargingSessionsToday: StateFlow<List<ChargingSession>> =
        batteryDao.getChargingSessionsSince(startTime = todayStart)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Computed overview for today
    val chargingOverviewToday: StateFlow<DayChargingOverview> =
        chargingSessionsToday
            .map { sessions ->
                val completed = sessions.filter { it.endTime != null && it.duration > 0 }
                val count = completed.size
                val totalTime = completed.sumOf { it.duration }
                val energy = completed.map { it.energyConsumed }.takeIf { it.isNotEmpty() }?.sum()
                val maxT = completed.maxByOrNull { it.maxTemperature }?.maxTemperature
                val eff = completed.map { it.efficiency }.takeIf { it.isNotEmpty() }?.average()
                DayChargingOverview(
                    sessionsCount = count,
                    totalTimeMs = totalTime,
                    energyWh = energy,
                    maxTemp = maxT,
                    efficiencyAvg = eff,
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                DayChargingOverview(0, 0L, null, null, null),
            )

    // Sessions in the last 30 days (for accurate EFC calculation)
    val chargingSessionsLast30d: StateFlow<List<ChargingSession>> =
        batteryDao.getChargingSessionsSince(startTime = last30dStart)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Sessions in the last 90 days (for time range analytics)
    val chargingSessionsLast90d: StateFlow<List<ChargingSession>> =
        batteryDao.getChargingSessionsSince(startTime = last90dStart)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Equivalent Full Cycles over last 30 days (sum of charge percentage gains / 100)
    val equivalentFullCycles30d: StateFlow<Double> =
        chargingSessionsLast30d
            .map { sessions ->
                val totalPercent =
                    sessions
                        .filter { it.endTime != null && it.endLevel != null }
                        .sumOf { (it.endLevel!! - it.startLevel).coerceAtLeast(0) }
                totalPercent / 100.0
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // Lifetime EFC across all recorded sessions
    val efcLifetime: StateFlow<Double> =
        batteryDao.getChargingSessionsSince(0L)
            .map { sessions ->
                val totalPercent =
                    sessions
                        .filter { it.endTime != null && it.endLevel != null }
                        .sumOf { (it.endLevel!! - it.startLevel).coerceAtLeast(0) }
                totalPercent / 100.0
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // Raw 30-day health delta using raw health records (last - first)
    val degradation30dRaw: StateFlow<Double?> =
        flow {
            emit(batteryDao.getHealthRecordsSince(last30dStart))
        }
            .map { records ->
                if (records.size >= 2) {
                    val first = records.first()
                    val last = records.last()
                    last.healthPercentage - first.healthPercentage
                } else {
                    null
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Thermal exposure index over last 30 days (proxy) weighted by duration
    // 0% means cool (<35°C), 100% means hot (~45°C or above)
    val thermalExposureIndex30d: StateFlow<Double?> =
        chargingSessionsLast30d
            .map { sessions ->
                val completed = sessions.filter { it.endTime != null && it.duration > 0 }
                if (completed.isEmpty()) return@map null
                val totalMinutes = completed.sumOf { it.duration } / 60_000.0
                if (totalMinutes <= 0) return@map null
                val weighted =
                    completed.sumOf { s ->
                        val norm = ((s.avgTemperature - 35.0) / 10.0).coerceIn(0.0, 1.0)
                        norm * (s.duration / 60_000.0)
                    }
                (weighted / totalMinutes) * 100.0
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Total completed charging sessions as a proxy for cycles
    val totalCycles: StateFlow<Int> =
        flow {
            emit(batteryDao.getTotalChargingSessions())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // 30-day health delta (latest avgHealth - earliest avgHealth). Negative => degradation
    val degradation30d: StateFlow<Double?> =
        dailyTrends
            .map { trends ->
                if (trends.size >= 2) {
                    val sorted = trends.sortedBy { it.date }
                    sorted.last().avgHealth - sorted.first().avgHealth
                } else {
                    null
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
