package com.voltai.smartbatteryguardian.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.voltai.smartbatteryguardian.BatteryDataProvider
import com.voltai.smartbatteryguardian.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class BatteryMonitoringService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Inject
    lateinit var batteryDataProvider: BatteryDataProvider

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startMonitoring()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        val notification =
            NotificationCompat.Builder(this, "battery_channel")
                .setContentTitle("VoltAI Battery Guardian")
                .setContentText("Monitoring battery health")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

        startForeground(1, notification)
    }

    private fun startMonitoring() {
        serviceScope.launch {
            batteryDataProvider.batteryData.collect { batteryData ->
                // Check for critical conditions
                if (batteryData.temperature > 45f) {
                    sendCriticalTemperatureNotification(batteryData.temperature)
                }

                if (batteryData.batteryLevel <= 15 && !batteryData.isCharging) {
                    sendLowBatteryNotification(batteryData.batteryLevel)
                }

                if (batteryData.healthPercentage < 60) {
                    sendHealthWarningNotification(batteryData.healthPercentage)
                }
            }
        }
    }

    private fun sendCriticalTemperatureNotification(temperature: Float) {
        val notification =
            NotificationCompat.Builder(this, "battery_alerts")
                .setContentTitle("🌡️ Critical Battery Temperature")
                .setContentText("Temperature: $temperature°C - Cool down device immediately!")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

        try {
            NotificationManagerCompat.from(this).notify(2, notification)
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }

    private fun sendLowBatteryNotification(level: Int) {
        val notification =
            NotificationCompat.Builder(this, "battery_alerts")
                .setContentTitle("🔋 Low Battery Warning")
                .setContentText("Battery: $level% - Enable power saving mode")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

        try {
            NotificationManagerCompat.from(this).notify(3, notification)
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }

    private fun sendHealthWarningNotification(health: Int) {
        val notification =
            NotificationCompat.Builder(this, "battery_alerts")
                .setContentTitle("❤️ Battery Health Alert")
                .setContentText("Battery health: $health% - Consider maintenance")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()

        try {
            NotificationManagerCompat.from(this).notify(4, notification)
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
