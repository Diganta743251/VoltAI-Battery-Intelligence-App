package com.voltai.smartbatteryguardian.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

// Room Entities
@Entity(tableName = "battery_logs")
data class BatteryLogEntity(
    @PrimaryKey val id: Long = 0,
    val timestamp: Long,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val temperature: Float,
    val voltage: Double,
    val current: Int,
    val healthPercentage: Int,
    val chargingSpeed: String,
    val powerSource: String,
)

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val id: Long = 0,
    val packageName: String,
    val appName: String,
    val usageTime: Long,
    val batteryUsage: Double,
    val date: String,
    val timestamp: Long,
)

@Entity(tableName = "charging_sessions")
data class ChargingSessionEntity(
    @PrimaryKey val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val startLevel: Int,
    val endLevel: Int?,
    val chargingSpeed: String,
    val powerSource: String,
    val averageTemperature: Float,
    val isComplete: Boolean = false,
)

@Entity(tableName = "battery_alerts")
data class BatteryAlertEntity(
    @PrimaryKey val id: Long = 0,
    val alertType: String, // LOW_BATTERY, OVERHEAT, HEALTH_DECLINE, etc.
    val message: String,
    val severity: Int, // 1-5 scale
    val timestamp: Long,
    val isRead: Boolean = false,
    val actionTaken: String? = null,
)

@Entity(tableName = "optimization_settings")
data class OptimizationSettingsEntity(
    @PrimaryKey val id: Long = 1,
    val batteryOptimizationEnabled: Boolean = true,
    val thermalManagementEnabled: Boolean = true,
    val backgroundAppLimitEnabled: Boolean = false,
    val chargingOptimizationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val autoBackupEnabled: Boolean = false,
    val premiumFeaturesEnabled: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
)

// Room DAOs
@Dao
interface BatteryLogDao {
    @Query("SELECT * FROM battery_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<BatteryLogEntity>>

    @Query("SELECT * FROM battery_logs WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getLogsSince(startTime: Long): Flow<List<BatteryLogEntity>>

    @Query("SELECT * FROM battery_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getLogsBetween(
        startTime: Long,
        endTime: Long,
    ): List<BatteryLogEntity>

    @Query("SELECT AVG(batteryLevel) FROM battery_logs WHERE timestamp >= :startTime")
    suspend fun getAverageBatteryLevel(startTime: Long): Double?

    @Insert
    suspend fun insertLog(log: BatteryLogEntity)

    @Insert
    suspend fun insertLogs(logs: List<BatteryLogEntity>)

    @Query("DELETE FROM battery_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)

    @Query("SELECT COUNT(*) FROM battery_logs")
    suspend fun getLogCount(): Int
}

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY usageTime DESC")
    suspend fun getUsageForDate(date: String): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage ORDER BY usageTime DESC LIMIT :limit")
    suspend fun getTopApps(limit: Int = 10): List<AppUsageEntity>

    @Query("SELECT SUM(batteryUsage) FROM app_usage WHERE date = :date")
    suspend fun getTotalBatteryUsageForDate(date: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: AppUsageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsages(usages: List<AppUsageEntity>)

    @Query("DELETE FROM app_usage WHERE timestamp < :cutoffTime")
    suspend fun deleteOldUsage(cutoffTime: Long)
}

@Dao
interface ChargingSessionDao {
    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 50): Flow<List<ChargingSessionEntity>>

    @Query("SELECT * FROM charging_sessions WHERE isComplete = 1 ORDER BY startTime DESC LIMIT :limit")
    suspend fun getCompletedSessions(limit: Int = 30): List<ChargingSessionEntity>

    @Query("SELECT * FROM charging_sessions WHERE isComplete = 0 LIMIT 1")
    suspend fun getCurrentSession(): ChargingSessionEntity?

    @Query("SELECT AVG(endLevel - startLevel) FROM charging_sessions WHERE isComplete = 1 AND startTime >= :startTime")
    suspend fun getAverageChargeGain(startTime: Long): Double?

    @Insert
    suspend fun insertSession(session: ChargingSessionEntity)

    @Update
    suspend fun updateSession(session: ChargingSessionEntity)

    @Query("DELETE FROM charging_sessions WHERE startTime < :cutoffTime")
    suspend fun deleteOldSessions(cutoffTime: Long)
}

@Dao
interface BatteryAlertDao {
    @Query("SELECT * FROM battery_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<BatteryAlertEntity>>

    @Query("SELECT * FROM battery_alerts WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadAlerts(): Flow<List<BatteryAlertEntity>>

    @Query("SELECT * FROM battery_alerts WHERE severity >= :minSeverity ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getHighPriorityAlerts(
        minSeverity: Int = 3,
        limit: Int = 20,
    ): List<BatteryAlertEntity>

    @Insert
    suspend fun insertAlert(alert: BatteryAlertEntity)

    @Query("UPDATE battery_alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertAsRead(alertId: Long)

    @Query("UPDATE battery_alerts SET isRead = 1")
    suspend fun markAllAlertsAsRead()

    @Query("DELETE FROM battery_alerts WHERE timestamp < :cutoffTime")
    suspend fun deleteOldAlerts(cutoffTime: Long)
}

@Dao
interface OptimizationSettingsDao {
    @Query("SELECT * FROM optimization_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): OptimizationSettingsEntity?

    @Query("SELECT * FROM optimization_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<OptimizationSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: OptimizationSettingsEntity)

    @Query(
        "UPDATE optimization_settings SET batteryOptimizationEnabled = :enabled, lastModified = :timestamp WHERE id = 1",
    )
    suspend fun updateBatteryOptimization(
        enabled: Boolean,
        timestamp: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE optimization_settings SET premiumFeaturesEnabled = :enabled, lastModified = :timestamp WHERE id = 1")
    suspend fun updatePremiumStatus(
        enabled: Boolean,
        timestamp: Long = System.currentTimeMillis(),
    )
}

// Database Migrations
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
            ALTER TABLE battery_logs ADD COLUMN powerSource TEXT NOT NULL DEFAULT 'Battery'
        """,
            )
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
            CREATE TABLE IF NOT EXISTS battery_alerts (
                id INTEGER PRIMARY KEY NOT NULL,
                alertType TEXT NOT NULL,
                message TEXT NOT NULL,
                severity INTEGER NOT NULL,
                timestamp INTEGER NOT NULL,
                isRead INTEGER NOT NULL DEFAULT 0,
                actionTaken TEXT
            )
        """,
            )
        }
    }

// Main Database
@Database(
    entities = [
        BatteryLogEntity::class,
        AppUsageEntity::class,
        ChargingSessionEntity::class,
        BatteryAlertEntity::class,
        OptimizationSettingsEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class BatteryDatabase : RoomDatabase() {
    abstract fun batteryLogDao(): BatteryLogDao

    abstract fun appUsageDao(): AppUsageDao

    abstract fun chargingSessionDao(): ChargingSessionDao

    abstract fun batteryAlertDao(): BatteryAlertDao

    abstract fun optimizationSettingsDao(): OptimizationSettingsDao

    companion object {
        const val DATABASE_NAME = "voltai_battery_database"

        @Volatile
        private var INSTANCE: BatteryDatabase? = null

        fun getInstance(context: android.content.Context): BatteryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    BatteryDatabase::class.java,
                    DATABASE_NAME,
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Type Converters for complex data types
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromFloatList(value: List<Float>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun toFloatList(value: String): List<Float> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { it.toFloat() }
    }
}
