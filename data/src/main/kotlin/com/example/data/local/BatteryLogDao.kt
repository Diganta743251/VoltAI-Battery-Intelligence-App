package com.voltai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryLog(batteryLog: BatteryLogEntity)

    @Query("SELECT * FROM battery_logs ORDER BY timestamp DESC")
    fun getAllBatteryLogs(): Flow<List<BatteryLogEntity>>
}
