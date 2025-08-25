package com.voltai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BatteryLogEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batteryLogDao(): BatteryLogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE battery_logs ADD COLUMN isChargingStart INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE battery_logs ADD COLUMN isChargingEnd INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE battery_logs ADD COLUMN cycleId INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
