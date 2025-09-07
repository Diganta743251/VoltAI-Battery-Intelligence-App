package com.voltai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.voltai.data.local.dao.CurrentSampleDao
import com.voltai.data.local.entity.CurrentSample

@Database(
    entities = [CurrentSample::class, BatteryLogEntity::class],
    version = 2,
    exportSchema = true
)
abstract class VoltAIDatabase : RoomDatabase() {
    abstract fun currentSampleDao(): CurrentSampleDao
    abstract fun batteryLogDao(): BatteryLogDao
}