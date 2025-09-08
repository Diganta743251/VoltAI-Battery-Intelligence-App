package com.voltai.smartbatteryguardian.work

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.voltai.smartbatteryguardian.database.BatteryLogDao
import com.voltai.smartbatteryguardian.database.BatteryLogEntity
import com.voltai.smartbatteryguardian.database.ChargingSessionDao
import com.voltai.smartbatteryguardian.database.ChargingSessionEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for collecting battery data in background
 */
@HiltWorker
class BatteryDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val batteryLogDao: BatteryLogDao,
    private val chargingSessionDao: ChargingSessionDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "battery_data_collection"
        private const val TAG = "BatteryDataWorker"

        fun enqueueWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false) // We want to monitor even when battery is low
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<BatteryDataWorker>(
                repeatInterval = 15, // Every 15 minutes
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = 5, // 5 minute flex window
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            collectBatteryData()
            manageChargingSessions()
            cleanupOldData()
            Result.success()
        } catch (exception: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun collectBatteryData() {
        val batteryIntent = applicationContext.registerReceiver(
            null, 
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return

        val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // Extract battery information
        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPercentage = if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else return

        val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
        val voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0

        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            } catch (e: Exception) {
                0
            }
        } else 0

        val health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val healthPercentage = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 100
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 85
            BatteryManager.BATTERY_HEALTH_DEAD -> 0
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 75
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 60
            BatteryManager.BATTERY_HEALTH_COLD -> 90
            else -> 80
        }

        val plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val powerSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery"
        }

        val chargingSpeed = determineChargingSpeed(current)

        // Create and insert battery log
        val batteryLog = BatteryLogEntity(
            id = 0, // Auto-generate
            timestamp = System.currentTimeMillis(),
            batteryLevel = batteryPercentage,
            isCharging = isCharging,
            temperature = temperature,
            voltage = voltage,
            current = current,
            healthPercentage = healthPercentage,
            chargingSpeed = chargingSpeed,
            powerSource = powerSource
        )

        batteryLogDao.insertLog(batteryLog)
    }

    private suspend fun manageChargingSessions() {
        val batteryIntent = applicationContext.registerReceiver(
            null, 
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPercentage = if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else return

        val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
        val plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val powerSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery"
        }

        val currentSession = chargingSessionDao.getCurrentSession()

        if (isCharging && currentSession == null) {
            // Start new charging session
            val newSession = ChargingSessionEntity(
                id = 0, // Auto-generate
                startTime = System.currentTimeMillis(),
                endTime = null,
                startLevel = batteryPercentage,
                endLevel = null,
                chargingSpeed = determineChargingSpeed(0), // Will be updated
                powerSource = powerSource,
                averageTemperature = temperature,
                isComplete = false
            )
            chargingSessionDao.insertSession(newSession)
        } else if (!isCharging && currentSession != null) {
            // End current charging session
            val updatedSession = currentSession.copy(
                endTime = System.currentTimeMillis(),
                endLevel = batteryPercentage,
                isComplete = true
            )
            chargingSessionDao.updateSession(updatedSession)
        } else if (isCharging && currentSession != null) {
            // Update ongoing session with latest temperature
            val duration = System.currentTimeMillis() - currentSession.startTime
            val sessionHours = duration / (1000 * 60 * 60f)
            val avgTemp = if (sessionHours > 0) {
                (currentSession.averageTemperature + temperature) / 2
            } else temperature

            val updatedSession = currentSession.copy(
                averageTemperature = avgTemp
            )
            chargingSessionDao.updateSession(updatedSession)
        }
    }

    private suspend fun cleanupOldData() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        
        // Clean up old battery logs (keep 30 days)
        batteryLogDao.deleteOldLogs(thirtyDaysAgo)
        
        // Clean up old charging sessions (keep 90 days)
        val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
        chargingSessionDao.deleteOldSessions(ninetyDaysAgo)
    }

    private fun determineChargingSpeed(current: Int): String {
        val currentMa = kotlin.math.abs(current)
        return when {
            currentMa > 3000 -> "Fast Charging"
            currentMa > 1500 -> "Quick Charging"
            currentMa > 500 -> "Normal"
            currentMa > 0 -> "Slow"
            else -> "Not Charging"
        }
    }
}

/**
 * Worker for app usage data collection (requires usage access permission)
 */
@HiltWorker
class AppUsageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "app_usage_collection"

        fun enqueueWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AppUsageWorker>(
                repeatInterval = 1, // Every hour
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            // Implementation would require UsageStatsManager
            // For now, just return success
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }
}
