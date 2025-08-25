package com.voltai.data.repository

import com.voltai.data.local.BatteryLogDao
import com.voltai.data.local.BatteryLogEntity
import com.voltai.data.preferences.DataStoreManager
import com.voltai.domain.repository.BatteryRepository
import com.voltai.domain.model.BatteryLog
import com.voltai.domain.model.BatteryStatus
import com.voltai.domain.model.ChargingCycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class BatteryRepositoryImpl @Inject constructor(
    private val batteryLogDao: BatteryLogDao,
    private val dataStoreManager: DataStoreManager
) : BatteryRepository {
    override suspend fun insertBatteryLog(batteryLog: BatteryLog) {
        batteryLogDao.insertBatteryLog(batteryLog.toEntity())
    }

    override fun getAllBatteryLogs(): Flow<List<BatteryLog>> {
        return batteryLogDao.getAllBatteryLogs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCurrentBatteryStatus(): Flow<BatteryStatus?> {
        return batteryLogDao.getAllBatteryLogs().map { entities ->
            entities.firstOrNull()?.let { entity ->
                BatteryStatus(
                    percentage = entity.batteryPercentage,
                    batteryLevel = entity.batteryPercentage,
                    status = if (entity.status == 1) "Charging" else "Not Charging",
                    chargingStatus = if (entity.status == 1) "Charging" else "Not Charging",
                    voltage = entity.voltage.toFloat(),
                    temperature = entity.temperature.toFloat(),
                    current = entity.currentNow / 1000f,
                    chargingType = when (entity.chargePlug) {
                        1 -> "AC"
                        2 -> "USB"
                        else -> "Unknown"
                    }
                )
            }
        }
    }
    
    private fun BatteryLog.toEntity(): BatteryLogEntity {
        return BatteryLogEntity(
            id = id.toInt(),
            timestamp = timestamp,
            batteryPercentage = batteryLevel,
            voltage = voltage.toInt(),
            temperature = temperature.toInt(),
            status = if (chargingStatus == "Charging") 1 else 0,
            chargePlug = if (chargingType == "AC") 1 else if (chargingType == "USB") 2 else 0,
            currentNow = (current * 1000).toInt(), // Convert to microamperes
            health = "Good"
        )
    }
    
    private fun BatteryLogEntity.toDomain(): BatteryLog {
        return BatteryLog(
            id = id.toLong(),
            timestamp = timestamp,
            batteryLevel = batteryPercentage,
            voltage = voltage.toFloat(),
            temperature = temperature.toFloat(),
            chargingStatus = if (status == 1) "Charging" else "Not Charging",
            chargingType = when (chargePlug) {
                1 -> "AC"
                2 -> "USB"
                else -> "Unknown"
            },
            current = currentNow / 1000f // Convert from microamperes
        )
    }

    override fun getChargingCycles(): Flow<List<ChargingCycle>> {
        return batteryLogDao.getAllBatteryLogs().map {
            val chargingCycles = mutableListOf<ChargingCycle>()
            var currentChargingCycle: ChargingCycle? = null
            var cycleLogs = mutableListOf<BatteryLogEntity>()

            it.forEach {
                if (it.isChargingStart) {
                    if (currentChargingCycle != null) {
                        // End previous cycle if a new one starts without an explicit end
                        chargingCycles.add(createChargingCycle(cycleLogs))
                    }
                    currentChargingCycle = ChargingCycle(
                        cycleId = it.cycleId,
                        startTime = it.timestamp,
                        endTime = 0L,
                        startPercentage = it.batteryPercentage,
                        endPercentage = 0,
                        maxTemperature = it.temperature,
                        maxCurrent = it.currentNow
                    )
                    cycleLogs = mutableListOf(it)
                } else if (it.isChargingEnd && currentChargingCycle != null) {
                    cycleLogs.add(it)
                    chargingCycles.add(createChargingCycle(cycleLogs))
                    currentChargingCycle = null
                } else if (currentChargingCycle != null) {
                    cycleLogs.add(it)
                }
            }
            // Add any remaining open cycle
            if (currentChargingCycle != null && cycleLogs.isNotEmpty()) {
                chargingCycles.add(createChargingCycle(cycleLogs))
            }
            chargingCycles
        }
    }

    override fun getOptimalChargingSettings(): Flow<Pair<Boolean, Int>> {
        return dataStoreManager.optimalChargingSettings
    }

    override suspend fun saveOptimalChargingSettings(enabled: Boolean, percentage: Int) {
        dataStoreManager.saveOptimalChargingSettings(enabled, percentage)
    }

    private fun createChargingCycle(logs: List<BatteryLogEntity>): ChargingCycle {
        if (logs.isEmpty()) return ChargingCycle(0L, 0L, 0L, 0, 0, 0, 0)

        val sortedLogs = logs.sortedBy { it.timestamp }
        val firstLog = sortedLogs.first()
        val lastLog = sortedLogs.last()

        return ChargingCycle(
            cycleId = firstLog.cycleId,
            startTime = firstLog.timestamp,
            endTime = lastLog.timestamp,
            startPercentage = firstLog.batteryPercentage,
            endPercentage = lastLog.batteryPercentage,
            maxTemperature = logs.maxOf { it.temperature },
            maxCurrent = logs.maxOf { it.currentNow }
        )
    }
}
