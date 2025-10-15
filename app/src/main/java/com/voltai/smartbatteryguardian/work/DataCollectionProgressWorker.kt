package com.voltai.smartbatteryguardian.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.voltai.smartbatteryguardian.data.FirstTimeUserManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker to periodically update data collection progress and manage first-time user experience
 */
@HiltWorker
class DataCollectionProgressWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firstTimeUserManager: FirstTimeUserManager,
) : CoroutineWorker(context, workerParams) {
    companion object {
        const val WORK_NAME = "data_collection_progress"

        /**
         * Schedule periodic progress updates
         */
        fun schedulePeriodicUpdates(context: Context) {
            val constraints =
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false) // We want to run even on low battery
                    .build()

            val progressRequest =
                PeriodicWorkRequestBuilder<DataCollectionProgressWorker>(
                    15,
                    TimeUnit.MINUTES, // Check every 15 minutes
                )
                    .setConstraints(constraints)
                    .setInitialDelay(5, TimeUnit.MINUTES) // Start after 5 minutes
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                progressRequest,
            )
        }

        /**
         * Schedule one-time immediate update
         */
        fun scheduleImmediateUpdate(context: Context) {
            val immediateRequest =
                OneTimeWorkRequestBuilder<DataCollectionProgressWorker>()
                    .setInitialDelay(30, TimeUnit.SECONDS) // Small delay to ensure app is initialized
                    .build()

            WorkManager.getInstance(context).enqueue(immediateRequest)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            // Update data collection progress
            firstTimeUserManager.updateDataCollectionProgress()

            val state = firstTimeUserManager.firstTimeUserState.value

            android.util.Log.d(
                "VoltAI",
                "Data collection progress: ${(state.dataCollectionProgress * 100).toInt()}%. " +
                    "Duration: ${state.dataCollectionDuration / (60 * 1000)} minutes. " +
                    "Status: ${firstTimeUserManager.getDataCollectionStatusMessage()}",
            )

            // Check if we should mark sufficient data as available
            if (!state.sufficientDataAvailable && shouldMarkSufficientData(state)) {
                firstTimeUserManager.markSufficientDataAvailable()
                android.util.Log.d("VoltAI", "Marked sufficient data as available")
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("VoltAI", "Failed to update data collection progress", e)
            Result.failure()
        }
    }

    private fun shouldMarkSufficientData(state: com.voltai.smartbatteryguardian.data.FirstTimeUserState): Boolean {
        // Mark as sufficient if:
        // 1. We've been collecting for at least 30 minutes
        // 2. OR we have some good quality data indicators
        val minimumTimeReached = state.dataCollectionDuration >= 30 * 60 * 1000L // 30 minutes

        // In the future, we could add more sophisticated checks here:
        // - Check if we have enough battery level changes
        // - Check if we have charging/discharging cycles
        // - Check data quality metrics

        return minimumTimeReached
    }
}

/**
 * Helper singleton to manage data collection progress scheduling
 */
object DataCollectionScheduler {
    /**
     * Initialize data collection progress tracking
     * Call this from the Application class
     */
    fun initialize(context: Context) {
        // Schedule periodic updates
        DataCollectionProgressWorker.schedulePeriodicUpdates(context)

        // Schedule an immediate update to get things started
        DataCollectionProgressWorker.scheduleImmediateUpdate(context)
    }

    /**
     * Cancel all scheduled work (useful for testing or cleanup)
     */
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(
            DataCollectionProgressWorker.WORK_NAME,
        )
    }
}
