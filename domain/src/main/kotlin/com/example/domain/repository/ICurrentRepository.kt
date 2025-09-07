package com.voltai.domain.repository

import com.voltai.domain.model.BatteryLog
import kotlinx.coroutines.flow.Flow

interface ICurrentRepository {
    suspend fun save(log: BatteryLog)
    fun getRecentLogs(limit: Int): Flow<List<BatteryLog>>
}