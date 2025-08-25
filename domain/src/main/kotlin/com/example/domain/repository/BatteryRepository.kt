package com.voltai.domain.repository

import com.voltai.domain.model.BatteryLog
import com.voltai.domain.model.BatteryStatus
import com.voltai.domain.model.ChargingCycle
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    suspend fun insertBatteryLog(batteryLog: BatteryLog)
    fun getAllBatteryLogs(): Flow<List<BatteryLog>>
    fun getCurrentBatteryStatus(): Flow<BatteryStatus?>
    fun getChargingCycles(): Flow<List<ChargingCycle>>
    fun getOptimalChargingSettings(): Flow<Pair<Boolean, Int>>
    suspend fun saveOptimalChargingSettings(enabled: Boolean, percentage: Int)
}
