package com.voltai.smartbatteryguardian

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.voltai.smartbatteryguardian.data.FirstTimeUserManager
import com.voltai.smartbatteryguardian.work.DataCollectionScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VoltAIApplication : Application(), Configuration.Provider {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "voltai_monitoring"
    }

    // Application scope for background operations
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Battery data provider will be injected by Hilt when first accessed
    @Inject
    lateinit var batteryDataProvider: BatteryDataProvider

    // First-time user experience manager
    @Inject
    lateinit var firstTimeUserManager: FirstTimeUserManager

    // Hilt WorkerFactory for WorkManager injected via Hilt
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize notification channels
        createNotificationChannels()

        // Initialize first-time user experience
        initializeFirstTimeUser()

        // Ensure immediate battery data collection starts
        initializeBatteryMonitoring()

        // Initialize AI services (placeholder)
        initializeAIServices()

        // Initialize data collection progress tracking
        DataCollectionScheduler.initialize(this)
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = getString(R.string.notification_channel_description)
                    enableVibration(true)
                    setShowBadge(true)
                }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeFirstTimeUser() {
        applicationScope.launch {
            try {
                // Check if this is a first launch and handle accordingly
                if (firstTimeUserManager.shouldShowOnboarding()) {
                    android.util.Log.d("VoltAI", "First time user detected - onboarding required")
                } else {
                    // Existing user - ensure data collection is ongoing
                    if (!firstTimeUserManager.isInDataCollectionPhase()) {
                        firstTimeUserManager.startDataCollection()
                    }
                    android.util.Log.d("VoltAI", "Existing user - continuing data collection")
                }
            } catch (e: Exception) {
                android.util.Log.e("VoltAI", "Failed to initialize first-time user experience", e)
            }
        }
    }

    private fun initializeBatteryMonitoring() {
        // Launch battery monitoring in background to ensure immediate data collection
        applicationScope.launch {
            try {
                // Access the battery data provider to trigger initialization
                // This ensures monitoring starts immediately when app launches
                val currentData = batteryDataProvider.batteryData.value
                android.util.Log.d(
                    "VoltAI",
                    "Battery monitoring initialized. Current level: ${currentData.batteryLevel}%",
                )

                // Mark that we've started collecting initial data
                if (!firstTimeUserManager.firstTimeUserState.value.initialDataCollected) {
                    firstTimeUserManager.startDataCollection()
                }
            } catch (e: Exception) {
                android.util.Log.e("VoltAI", "Failed to initialize battery monitoring", e)
            }
        }
    }

    private fun initializeAIServices() {
        // Initialize AI battery optimization engine
        // This would normally connect to ML models and start background services
        // For now, this is a placeholder for future implementation

        applicationScope.launch {
            // Pre-warm AI optimization algorithms
            try {
                // Simulate AI service initialization
                android.util.Log.d("VoltAI", "AI services initialized successfully")
            } catch (e: Exception) {
                android.util.Log.e("VoltAI", "Failed to initialize AI services", e)
            }
        }
    }

    fun getVersionInfo(): String {
        // Version info dynamically read from PackageManager
        val versionName =
            try {
                packageManager.getPackageInfo(packageName, 0).versionName
            } catch (e: Exception) {
                "1.0" // Fallback version
            }
        // Build type determined at runtime
        val buildType =
            if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                "Debug"
            } else {
                "Release"
            }
        return "$versionName ($buildType)"
    }
}
