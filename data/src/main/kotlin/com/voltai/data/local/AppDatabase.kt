package com.voltai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.voltai.data.local.dao.CurrentSampleDao
import com.voltai.data.local.entity.CurrentSample

@Database(
    entities = [CurrentSample::class],
    version = 1,
    exportSchema = true
)
abstract class VoltAIDatabase : RoomDatabase() {
    abstract fun currentSampleDao(): CurrentSampleDao
}