package com.voltai.domain.repository

import com.voltai.domain.model.BatteryLog
import kotlinx.coroutines.flow.Flow

interface IBatteryMonitor {
    fun observeBatteryLogs(): Flow<BatteryLog>
}