package com.voltai.data.repository

import com.voltai.core.utils.BatteryMonitor
import com.voltai.data.local.dao.CurrentSampleDao
import com.voltai.data.local.entity.CurrentSample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentRepository @Inject constructor(
    private val batteryMonitor: BatteryMonitor,
    private val currentSampleDao: CurrentSampleDao
) : com.voltai.domain.repository.ICurrentRepository {
    private val repoScope = CoroutineScope(Dispatchers.IO)

    // Adapt to domain interfaces
    override suspend fun save(log: com.voltai.domain.model.BatteryLog) {
        currentSampleDao.insert(
            CurrentSample(
                timestamp = log.timestamp,
                currentMa = log.current,
                isCharging = log.current < 0f
            )
        )
    }

    override fun getRecentLogs(limit: Int): Flow<List<com.voltai.domain.model.BatteryLog>> {
        // Since DAO is time-based, approximate by taking last N from the stream
        val since = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return currentSampleDao.getSamplesSince(since).map { list ->
            list.takeLast(limit).map {
                com.voltai.domain.model.BatteryLog(
                    id = it.id,
                    timestamp = it.timestamp,
                    batteryLevel = 0,
                    voltage = 0f,
                    temperature = 0f,
                    chargingStatus = if (it.isCharging) "Charging" else "Discharging",
                    chargingType = "",
                    current = it.currentMa
                )
            }
        }
    }
}