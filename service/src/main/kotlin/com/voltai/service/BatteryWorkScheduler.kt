package com.voltai.service

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * WorkManager scheduler utility in the :service module to periodically trigger
 * battery level and temperature monitoring via ChargingAlertWorker.
 */
object BatteryWorkScheduler {
    private const val PERIODIC_WORK_NAME = "volt_ai_battery_monitoring_work"

    fun schedulePeriodicMonitoring(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<ChargingAlertWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    fun triggerImmediateCheck(context: Context) {
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<ChargingAlertWorker>()
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
    }
}
