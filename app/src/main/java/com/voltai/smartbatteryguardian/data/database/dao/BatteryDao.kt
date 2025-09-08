package com.voltai.smartbatteryguardian.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.voltai.smartbatteryguardian.data.database.entities.*

@Dao
interface BatteryDao {
    
    // Battery Readings Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryReading(reading: BatteryReading): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryReadings(readings: List<BatteryReading>)
    
    @Query("SELECT * FROM battery_readings WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getBatteryReadingsSince(startTime: Long): Flow<List<BatteryReading>>
    
    @Query("SELECT * FROM battery_readings ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestBatteryReadings(limit: Int = 100): Flow<List<BatteryReading>>
    
    @Query("SELECT * FROM battery_readings WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getBatteryReadingsBetween(start: Long, end: Long): List<BatteryReading>
    
    @Query("SELECT AVG(batteryLevel) FROM battery_readings WHERE timestamp >= :startTime")
    suspend fun getAverageBatteryLevel(startTime: Long): Double
    
    @Query("SELECT AVG(temperature) FROM battery_readings WHERE timestamp >= :startTime")
    suspend fun getAverageTemperature(startTime: Long): Double
    
    @Query("DELETE FROM battery_readings WHERE timestamp < :cutoffTime")
    suspend fun deleteBatteryReadingsOlderThan(cutoffTime: Long)
    
    // Advanced battery analytics
    @Query("""
        SELECT 
            DATE(timestamp / 1000, 'unixepoch') as date,
            MIN(batteryLevel) as minLevel,
            MAX(batteryLevel) as maxLevel,
            AVG(batteryLevel) as avgLevel,
            COUNT(*) as readingCount
        FROM battery_readings 
        WHERE timestamp >= :startTime
        GROUP BY DATE(timestamp / 1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getDailyBatteryStatistics(startTime: Long): List<DailyBatteryStats>
    
    // Charging Sessions Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChargingSession(session: ChargingSession): Long
    
    @Update
    suspend fun updateChargingSession(session: ChargingSession)
    
    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentChargingSessions(limit: Int = 50): Flow<List<ChargingSession>>
    
    @Query("SELECT * FROM charging_sessions WHERE startTime >= :startTime ORDER BY startTime DESC")
    fun getChargingSessionsSince(startTime: Long): Flow<List<ChargingSession>>
    
    @Query("SELECT * FROM charging_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveChargingSession(): ChargingSession?
    
    @Query("SELECT AVG(efficiency) FROM charging_sessions WHERE startTime >= :startTime")
    suspend fun getAverageChargingEfficiency(startTime: Long): Double
    
    @Query("SELECT AVG(duration) FROM charging_sessions WHERE endTime IS NOT NULL AND startTime >= :startTime")
    suspend fun getAverageChargingDuration(startTime: Long): Long
    
    // App Usage Stats Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsageStats(stats: AppUsageStats): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsageStatsList(statsList: List<AppUsageStats>)
    
    @Query("SELECT * FROM app_usage_stats WHERE timestamp >= :startTime ORDER BY batteryUsage DESC")
    fun getAppUsageStatsSince(startTime: Long): Flow<List<AppUsageStats>>
    
    @Query("""
        SELECT packageName, appName, category,
               SUM(batteryUsage) as totalBatteryUsage,
               SUM(foregroundTime) as totalForegroundTime,
               SUM(backgroundTime) as totalBackgroundTime,
               AVG(cpuUsage) as avgCpuUsage,
               SUM(networkUsage) as totalNetworkUsage
        FROM app_usage_stats 
        WHERE timestamp >= :startTime
        GROUP BY packageName
        ORDER BY totalBatteryUsage DESC
        LIMIT :limit
    """)
    suspend fun getTopBatteryConsumingApps(startTime: Long, limit: Int = 20): List<AppBatterySummary>
    
    @Query("""
        SELECT category,
               SUM(batteryUsage) as totalBatteryUsage,
               COUNT(DISTINCT packageName) as appCount,
               AVG(batteryUsage) as avgBatteryUsage
        FROM app_usage_stats 
        WHERE timestamp >= :startTime
        GROUP BY category
        ORDER BY totalBatteryUsage DESC
    """)
    suspend fun getBatteryUsageByCategory(startTime: Long): List<CategoryBatterySummary>
    
    // Battery Health Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryHealthRecord(record: BatteryHealthRecord): Long
    
    @Query("SELECT * FROM battery_health_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHealthRecords(limit: Int = 100): Flow<List<BatteryHealthRecord>>
    
    @Query("SELECT * FROM battery_health_history WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    suspend fun getHealthRecordsSince(startTime: Long): List<BatteryHealthRecord>
    
    @Query("SELECT * FROM battery_health_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestHealthRecord(): BatteryHealthRecord?
    
    @Query("SELECT AVG(healthPercentage) FROM battery_health_history WHERE timestamp >= :startTime")
    suspend fun getAverageHealthPercentage(startTime: Long): Double
    
    @Query("""
        SELECT 
            DATE(timestamp / 1000, 'unixepoch') as date,
            AVG(healthPercentage) as avgHealth,
            AVG(capacityRetained) as avgCapacity,
            AVG(cycleCount) as avgCycles,
            AVG(degradationRate) as avgDegradation
        FROM battery_health_history 
        WHERE timestamp >= :startTime
        GROUP BY DATE(timestamp / 1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getDailyHealthTrends(startTime: Long): List<DailyHealthTrend>
    
    // Power Profiles Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPowerProfile(profile: PowerProfile): Long
    
    @Update
    suspend fun updatePowerProfile(profile: PowerProfile)
    
    @Delete
    suspend fun deletePowerProfile(profile: PowerProfile)
    
    @Query("SELECT * FROM power_profiles ORDER BY isActive DESC, name ASC")
    fun getAllPowerProfiles(): Flow<List<PowerProfile>>
    
    @Query("SELECT * FROM power_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePowerProfile(): PowerProfile?
    
    @Query("UPDATE power_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()
    
    // System Metrics Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemMetrics(metrics: SystemMetrics): Long
    
    @Query("SELECT * FROM system_metrics WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getSystemMetricsSince(startTime: Long): Flow<List<SystemMetrics>>
    
    @Query("SELECT * FROM system_metrics ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSystemMetrics(): SystemMetrics?
    
    @Query("""
        SELECT AVG(cpuUsage) as avgCpu, 
               AVG(memoryUsage) as avgMemory, 
               AVG(performanceScore) as avgPerformance
        FROM system_metrics 
        WHERE timestamp >= :startTime
    """)
    suspend fun getSystemPerformanceSummary(startTime: Long): SystemPerformanceSummary
    
    // Optimization Logs Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptimizationLog(log: OptimizationLog): Long
    
    @Query("SELECT * FROM optimization_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentOptimizationLogs(limit: Int = 100): Flow<List<OptimizationLog>>
    
    @Query("SELECT COUNT(*) FROM optimization_logs WHERE success = 1 AND timestamp >= :startTime")
    suspend fun getSuccessfulOptimizationsCount(startTime: Long): Int
    
    @Query("SELECT AVG(impactScore) FROM optimization_logs WHERE success = 1 AND timestamp >= :startTime")
    suspend fun getAverageOptimizationImpact(startTime: Long): Double
    
    // Benchmark Results Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenchmarkResult(result: BenchmarkResult): Long
    
    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBenchmarkResults(limit: Int = 50): Flow<List<BenchmarkResult>>
    
    @Query("SELECT * FROM benchmark_results WHERE benchmarkType = :type ORDER BY timestamp DESC LIMIT :limit")
    fun getBenchmarkResultsByType(type: String, limit: Int = 20): Flow<List<BenchmarkResult>>
    
    @Query("SELECT AVG(score) FROM benchmark_results WHERE benchmarkType = :type AND timestamp >= :startTime")
    suspend fun getAverageBenchmarkScore(type: String, startTime: Long): Double
    
    // AI Predictions Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIPrediction(prediction: AIPrediction): Long
    
    @Query("SELECT * FROM ai_predictions WHERE isActive = 1 ORDER BY timestamp DESC")
    fun getActivePredictions(): Flow<List<AIPrediction>>
    
    @Query("SELECT * FROM ai_predictions WHERE predictionType = :type AND isActive = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPrediction(type: String): AIPrediction?
    
    @Query("UPDATE ai_predictions SET actualValue = :actualValue, errorRate = ABS(predictedValue - :actualValue) WHERE id = :id")
    suspend fun updatePredictionActual(id: Long, actualValue: Double)
    
    @Query("SELECT * FROM ai_predictions WHERE predictionType = :predictionType ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentPredictions(predictionType: String, limit: Int): List<AIPrediction>
    
    // User Preferences Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferences)
    
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferences(): UserPreferences?
    
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferencesFlow(): Flow<UserPreferences?>
    
    // Cleanup Operations
    @Query("DELETE FROM battery_readings WHERE timestamp < :cutoffTime")
    suspend fun cleanupBatteryReadings(cutoffTime: Long)
    
    @Query("DELETE FROM app_usage_stats WHERE timestamp < :cutoffTime")
    suspend fun cleanupAppUsageStats(cutoffTime: Long)
    
    @Query("DELETE FROM system_metrics WHERE timestamp < :cutoffTime")
    suspend fun cleanupSystemMetrics(cutoffTime: Long)
    
    @Query("DELETE FROM optimization_logs WHERE timestamp < :cutoffTime")
    suspend fun cleanupOptimizationLogs(cutoffTime: Long)
    
    @Transaction
    suspend fun cleanupOldData(retentionDays: Int) {
        val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        cleanupBatteryReadings(cutoffTime)
        cleanupAppUsageStats(cutoffTime)
        cleanupSystemMetrics(cutoffTime)
        cleanupOptimizationLogs(cutoffTime)
    }
}

// Data classes for complex query results
data class DailyBatteryStats(
    val date: String,
    val minLevel: Int,
    val maxLevel: Int,
    val avgLevel: Double,
    val readingCount: Int
)

data class AppBatterySummary(
    val packageName: String,
    val appName: String,
    val category: String,
    val totalBatteryUsage: Double,
    val totalForegroundTime: Long,
    val totalBackgroundTime: Long,
    val avgCpuUsage: Double,
    val totalNetworkUsage: Long
)

data class CategoryBatterySummary(
    val category: String,
    val totalBatteryUsage: Double,
    val appCount: Int,
    val avgBatteryUsage: Double
)

data class DailyHealthTrend(
    val date: String,
    val avgHealth: Double,
    val avgCapacity: Double,
    val avgCycles: Double,
    val avgDegradation: Double
)

data class SystemPerformanceSummary(
    val avgCpu: Double,
    val avgMemory: Double,
    val avgPerformance: Double
)
