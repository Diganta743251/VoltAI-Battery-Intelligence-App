package com.voltai.data.repository

import com.voltai.data.local.dao.CurrentSampleDao
import com.voltai.data.local.entity.CurrentSample
import com.voltai.domain.model.BatteryLog
import com.voltai.domain.repository.ICurrentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentRepositoryImpl @Inject constructor(
    private val currentSampleDao: CurrentSampleDao
) : ICurrentRepository {

    override suspend fun save(log: BatteryLog) {
        val entity = CurrentSample(
            timestamp = log.timestamp,
            currentMa = log.current,
            isCharging = log.chargingStatus.equals("Charging", ignoreCase = true)
        )
        currentSampleDao.insert(entity)
    }

    override fun getRecentLogs(limit: Int): Flow<List<BatteryLog>> {
        // Reconstruct recent items from the last window by timestamp
        val now = System.currentTimeMillis()
        val windowStart = now - (60 * 60 * 1000) // 1 hour window to be safe
        return currentSampleDao.getSamplesSince(windowStart).map { samples ->
            samples.takeLast(limit).map { s ->
                BatteryLog(
                    id = 0,
                    timestamp = s.timestamp,
                    batteryLevel = 0,
                    voltage = 0f,
                    temperature = 0f,
                    chargingStatus = if (s.isCharging) "Charging" else "Discharging",
                    chargingType = "",
                    current = s.currentMa
                )
            }
        }
    }
}