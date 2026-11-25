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

    companion object {
        @Volatile
        private var INSTANCE: VoltAIDatabase? = null

        fun getInstance(context: android.content.Context): VoltAIDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    VoltAIDatabase::class.java,
                    "voltai.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}