package com.voltai.smartbatteryguardian

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.voltai.smartbatteryguardian.database.AppUsageDao
import com.voltai.smartbatteryguardian.database.AppUsageEntity
import com.voltai.smartbatteryguardian.database.BatteryAlertDao
import com.voltai.smartbatteryguardian.database.BatteryAlertEntity
import com.voltai.smartbatteryguardian.database.BatteryDatabase
import com.voltai.smartbatteryguardian.database.BatteryLogDao
import com.voltai.smartbatteryguardian.database.BatteryLogEntity
import com.voltai.smartbatteryguardian.database.ChargingSessionDao
import com.voltai.smartbatteryguardian.database.ChargingSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for Room database operations
 */
@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {
    private lateinit var database: BatteryDatabase
    private lateinit var batteryLogDao: BatteryLogDao
    private lateinit var chargingSessionDao: ChargingSessionDao
    private lateinit var appUsageDao: AppUsageDao
    private lateinit var alertDao: BatteryAlertDao

    @Before
    fun setup() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                BatteryDatabase::class.java,
            ).allowMainThreadQueries().build()

        batteryLogDao = database.batteryLogDao()
        chargingSessionDao = database.chargingSessionDao()
        appUsageDao = database.appUsageDao()
        alertDao = database.batteryAlertDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveBatteryLog() =
        runTest {
            // Given
            val batteryLog =
                BatteryLogEntity(
                    id = 1,
                    timestamp = System.currentTimeMillis(),
                    batteryLevel = 75,
                    isCharging = true,
                    temperature = 32.5f,
                    voltage = 3.8,
                    current = 1200,
                    healthPercentage = 95,
                    chargingSpeed = "Fast Charging",
                    powerSource = "AC Adapter",
                )

            // When
            batteryLogDao.insertLog(batteryLog)
            val retrievedLogs = batteryLogDao.getRecentLogs(10).first()

            // Then
            assertThat(retrievedLogs).hasSize(1)
            assertThat(retrievedLogs[0].batteryLevel).isEqualTo(75)
            assertThat(retrievedLogs[0].isCharging).isTrue()
            assertThat(retrievedLogs[0].temperature).isWithin(0.1f).of(32.5f)
            assertThat(retrievedLogs[0].chargingSpeed).isEqualTo("Fast Charging")
        }

    @Test
    fun insertMultipleLogsAndQueryByTimeRange() =
        runTest {
            // Given
            val now = System.currentTimeMillis()
            val oneHourAgo = now - (60 * 60 * 1000)
            val twoHoursAgo = now - (2 * 60 * 60 * 1000)

            val logs =
                listOf(
                    BatteryLogEntity(1, twoHoursAgo, 80, false, 30f, 3.7, -500, 95, "Normal", "Battery"),
                    BatteryLogEntity(2, oneHourAgo, 70, false, 32f, 3.6, -600, 95, "Normal", "Battery"),
                    BatteryLogEntity(3, now, 65, true, 35f, 3.8, 1000, 95, "Quick Charging", "USB"),
                )

            // When
            batteryLogDao.insertLogs(logs)
            val recentLogs = batteryLogDao.getLogsBetween(oneHourAgo - 1000, now + 1000)

            // Then
            assertThat(recentLogs).hasSize(2)
            assertThat(recentLogs.map { it.batteryLevel }).containsExactly(70, 65).inOrder()
        }

    @Test
    fun calculateAverageBatteryLevel() =
        runTest {
            // Given
            val now = System.currentTimeMillis()
            val logs =
                listOf(
                    BatteryLogEntity(1, now - 1000, 80, false, 30f, 3.7, -500, 95, "Normal", "Battery"),
                    BatteryLogEntity(2, now - 500, 70, false, 32f, 3.6, -600, 95, "Normal", "Battery"),
                    BatteryLogEntity(3, now, 60, false, 34f, 3.5, -700, 95, "Normal", "Battery"),
                )

            // When
            batteryLogDao.insertLogs(logs)
            val average = batteryLogDao.getAverageBatteryLevel(now - 2000)

            // Then
            assertThat(average).isWithin(0.1).of(70.0) // (80 + 70 + 60) / 3 = 70
        }

    @Test
    fun chargingSessionLifecycle() =
        runTest {
            // Given
            val startTime = System.currentTimeMillis() - (2 * 60 * 60 * 1000) // 2 hours ago
            val endTime = System.currentTimeMillis() - (1 * 60 * 60 * 1000) // 1 hour ago

            val chargingSession =
                ChargingSessionEntity(
                    id = 1,
                    startTime = startTime,
                    endTime = null,
                    startLevel = 20,
                    endLevel = null,
                    chargingSpeed = "Fast Charging",
                    powerSource = "AC Adapter",
                    averageTemperature = 35f,
                    isComplete = false,
                )

            // When - Start charging session
            chargingSessionDao.insertSession(chargingSession)
            val currentSession = chargingSessionDao.getCurrentSession()

            // Then - Session should be active
            assertThat(currentSession).isNotNull()
            assertThat(currentSession?.isComplete).isFalse()
            assertThat(currentSession?.startLevel).isEqualTo(20)

            // When - Complete charging session
            val completedSession =
                currentSession!!.copy(
                    endTime = endTime,
                    endLevel = 85,
                    isComplete = true,
                )
            chargingSessionDao.updateSession(completedSession)

            val noCurrentSession = chargingSessionDao.getCurrentSession()
            val recentSessions = chargingSessionDao.getRecentSessions(5).first()

            // Then - Session should be completed
            assertThat(noCurrentSession).isNull()
            assertThat(recentSessions).hasSize(1)
            assertThat(recentSessions[0].isComplete).isTrue()
            assertThat(recentSessions[0].endLevel).isEqualTo(85)
        }

    @Test
    fun appUsageTracking() =
        runTest {
            // Given
            val today = "2024-01-15"
            val usageData =
                listOf(
                    AppUsageEntity(1, "com.example.app1", "App 1", 3600000, 15.5, today, System.currentTimeMillis()),
                    AppUsageEntity(2, "com.example.app2", "App 2", 1800000, 8.2, today, System.currentTimeMillis()),
                    AppUsageEntity(3, "com.example.app3", "App 3", 900000, 4.1, today, System.currentTimeMillis()),
                )

            // When
            appUsageDao.insertUsages(usageData)
            val todayUsage = appUsageDao.getUsageForDate(today)
            val topApps = appUsageDao.getTopApps(2)
            val totalBatteryUsage = appUsageDao.getTotalBatteryUsageForDate(today)

            // Then
            assertThat(todayUsage).hasSize(3)
            assertThat(topApps).hasSize(2)
            assertThat(topApps[0].appName).isEqualTo("App 1") // Highest usage time
            assertThat(totalBatteryUsage).isWithin(0.1).of(27.8) // 15.5 + 8.2 + 4.1
        }

    @Test
    fun batteryAlertsManagement() =
        runTest {
            // Given
            val alerts =
                listOf(
                    BatteryAlertEntity(
                        1,
                        "LOW_BATTERY",
                        "Battery level is critically low",
                        5,
                        System.currentTimeMillis(),
                        false,
                        null,
                    ),
                    BatteryAlertEntity(
                        2,
                        "OVERHEAT",
                        "Device is overheating",
                        4,
                        System.currentTimeMillis() - 1000,
                        false,
                        null,
                    ),
                    BatteryAlertEntity(
                        3,
                        "HEALTH_DECLINE",
                        "Battery health declining",
                        3,
                        System.currentTimeMillis() - 2000,
                        true,
                        "User acknowledged",
                    ),
                )

            // When
            alerts.forEach { alertDao.insertAlert(it) }

            val allAlerts = alertDao.getAllAlerts().first()
            val unreadAlerts = alertDao.getUnreadAlerts().first()
            val highPriorityAlerts = alertDao.getHighPriorityAlerts(4)

            // Then
            assertThat(allAlerts).hasSize(3)
            assertThat(unreadAlerts).hasSize(2)
            assertThat(highPriorityAlerts).hasSize(2) // Severity >= 4
            assertThat(highPriorityAlerts.map { it.alertType }).containsExactly("LOW_BATTERY", "OVERHEAT")

            // When - Mark alert as read
            alertDao.markAlertAsRead(1)
            val updatedUnreadAlerts = alertDao.getUnreadAlerts().first()

            // Then
            assertThat(updatedUnreadAlerts).hasSize(1)
        }

    @Test
    fun dataCleanupOperations() =
        runTest {
            // Given - Old data
            val now = System.currentTimeMillis()
            val oldTimestamp = now - (40L * 24 * 60 * 60 * 1000) // 40 days ago
            val recentTimestamp = now - (10L * 24 * 60 * 60 * 1000) // 10 days ago

            val oldLog = BatteryLogEntity(1, oldTimestamp, 50, false, 30f, 3.7, -500, 95, "Normal", "Battery")
            val recentLog = BatteryLogEntity(2, recentTimestamp, 60, false, 32f, 3.8, -400, 95, "Normal", "Battery")

            val oldSession =
                ChargingSessionEntity(
                    1,
                    oldTimestamp,
                    oldTimestamp + 3600000,
                    20,
                    80,
                    "Normal",
                    "AC",
                    30f,
                    true,
                )
            val recentSession =
                ChargingSessionEntity(
                    2,
                    recentTimestamp,
                    recentTimestamp + 3600000,
                    30,
                    90,
                    "Fast",
                    "AC",
                    32f,
                    true,
                )

            // When - Insert data
            batteryLogDao.insertLogs(listOf(oldLog, recentLog))
            chargingSessionDao.insertSession(oldSession)
            chargingSessionDao.insertSession(recentSession)

            val cutoffTime = now - (30L * 24 * 60 * 60 * 1000) // 30 days ago

            // Clean old data
            batteryLogDao.deleteOldLogs(cutoffTime)
            chargingSessionDao.deleteOldSessions(cutoffTime)

            val remainingLogs = batteryLogDao.getRecentLogs(10).first()
            val remainingSessions = chargingSessionDao.getRecentSessions(10).first()

            // Then - Only recent data should remain
            assertThat(remainingLogs).hasSize(1)
            assertThat(remainingLogs[0].id).isEqualTo(2)
            assertThat(remainingSessions).hasSize(1)
            assertThat(remainingSessions[0].id).isEqualTo(2)
        }

    @Test
    fun complexQueryWithJoins() =
        runTest {
            // Given - Battery logs and charging sessions for same time period
            val now = System.currentTimeMillis()
            val sessionStart = now - 3600000 // 1 hour ago
            val sessionEnd = now

            // Insert battery logs during charging
            val chargingLogs =
                listOf(
                    BatteryLogEntity(1, sessionStart, 20, true, 30f, 3.7, 1000, 95, "Fast", "AC"),
                    BatteryLogEntity(2, sessionStart + 1800000, 60, true, 35f, 3.8, 1200, 95, "Fast", "AC"),
                    BatteryLogEntity(3, sessionEnd, 90, true, 40f, 3.9, 800, 95, "Fast", "AC"),
                )

            val session = ChargingSessionEntity(1, sessionStart, sessionEnd, 20, 90, "Fast", "AC", 35f, true)

            // When
            batteryLogDao.insertLogs(chargingLogs)
            chargingSessionDao.insertSession(session)

            // Query data for analysis
            val sessionLogs = batteryLogDao.getLogsBetween(sessionStart, sessionEnd)
            val completedSessions = chargingSessionDao.getCompletedSessions(5)

            // Then - Verify data integrity
            assertThat(sessionLogs).hasSize(3)
            assertThat(completedSessions).hasSize(1)

            val sessionData = completedSessions[0]
            assertThat(sessionData.endLevel?.minus(sessionData.startLevel)).isEqualTo(70) // 90 - 20
            assertThat(sessionLogs.all { it.isCharging }).isTrue()
        }
}
