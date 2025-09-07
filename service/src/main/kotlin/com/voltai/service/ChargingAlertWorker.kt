package com.voltai.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voltai.data.preferences.DataStoreManager
import com.voltai.core.utils.BatteryUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import android.os.PowerManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import java.util.concurrent.TimeUnit

@HiltWorker
class ChargingAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataStoreManager: DataStoreManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "charging_alerts"
        const val NOTIFICATION_CHANNEL_NAME = "Charging Alerts"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_CHARGING = "com.voltai.ACTION_STOP_CHARGING"
        const val ACTION_SNOOZE = "com.voltai.ACTION_SNOOZE"
        const val WORK_NAME = "charging_alert_work"
        
        // Input data keys
        const val KEY_ALERT_TYPE = "alert_type"
        const val KEY_BATTERY_LEVEL = "battery_level"
        const val KEY_CUSTOM_MESSAGE = "custom_message"
    }
    
    private val notificationManager: NotificationManager by lazy {
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override suspend fun doWork(): Result {
        return try {
            // Get battery status
            val batteryStatus = BatteryUtils.getBatteryStatus(applicationContext)
            
            // Get user preferences for charging alerts (temporary implementation)
            val chargingAlertSettings = ChargingAlertSettings()
            
            // Check various alert conditions
            when {
                // High battery alert
                chargingAlertSettings.highBatteryAlertEnabled && 
                batteryStatus.percentage >= chargingAlertSettings.highBatteryThreshold -> {
                    sendNotification(
                        title = "Battery Charged",
                        message = "Battery has reached ${batteryStatus.percentage}%. Consider unplugging to preserve battery health.",
                        priority = NotificationCompat.PRIORITY_HIGH,
                        vibrate = true,
                        sound = true,
                        actions = listOf(NotificationAction.STOP_CHARGING, NotificationAction.SNOOZE)
                    )
                }
                
                // Low battery alert
                chargingAlertSettings.lowBatteryAlertEnabled &&
                batteryStatus.percentage <= chargingAlertSettings.lowBatteryThreshold -> {
                    sendNotification(
                        title = "Low Battery",
                        message = "Battery is at ${batteryStatus.percentage}%. Please charge your device.",
                        priority = NotificationCompat.PRIORITY_HIGH,
                        vibrate = true,
                        sound = true
                    )
                }
                
                // Temperature alert
                chargingAlertSettings.temperatureAlertEnabled &&
                batteryStatus.temperature > chargingAlertSettings.temperatureThreshold -> {
                    sendNotification(
                        title = "Battery Temperature Warning",
                        message = "Battery temperature is ${batteryStatus.temperature}°C. Consider letting it cool down.",
                        priority = NotificationCompat.PRIORITY_MAX,
                        vibrate = true,
                        sound = true,
                        actions = listOf(NotificationAction.STOP_CHARGING)
                    )
                }
                
                // Fast charging notification
                batteryStatus.isCharging && batteryStatus.voltage > 4000 -> {
                    if (chargingAlertSettings.fastChargingNotificationEnabled) {
                        sendNotification(
                            title = "Fast Charging Active",
                            message = "Your device is fast charging. Current level: ${batteryStatus.percentage}%",
                            priority = NotificationCompat.PRIORITY_LOW,
                            vibrate = false,
                            sound = false
                        )
                    }
                }
                
                // Optimal charging reached
                chargingAlertSettings.optimalChargingEnabled &&
                batteryStatus.percentage >= chargingAlertSettings.optimalChargingLimit -> {
                    sendNotification(
                        title = "Optimal Charge Reached",
                        message = "Battery at ${batteryStatus.percentage}%. Unplug now for best battery longevity.",
                        priority = NotificationCompat.PRIORITY_HIGH,
                        vibrate = true,
                        sound = true,
                        actions = listOf(NotificationAction.STOP_CHARGING)
                    )
                }
            }
            
            // Schedule next check if charging
            if (batteryStatus.isCharging) {
                scheduleNextCheck(delayMinutes = 5)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun sendNotification(
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        vibrate: Boolean = false,
        sound: Boolean = false,
        actions: List<NotificationAction> = emptyList()
    ) {
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        
        // Create main intent
        val mainIntent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName)
        val mainPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
        
        // Add vibration
        if (vibrate) {
            notificationBuilder.setVibrate(longArrayOf(0, 500, 250, 500))
        }
        
        // Add sound
        if (sound) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationBuilder.setSound(alarmSound)
        }
        
        // Add action buttons
        actions.forEach { action ->
            when (action) {
                NotificationAction.STOP_CHARGING -> {
                    val stopIntent = Intent(ACTION_STOP_CHARGING).apply {
                        setPackage(applicationContext.packageName)
                    }
                    val stopPendingIntent = PendingIntent.getBroadcast(
                        applicationContext,
                        1,
                        stopIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    notificationBuilder.addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Stop Charging",
                        stopPendingIntent
                    )
                }
                NotificationAction.SNOOZE -> {
                    val snoozeIntent = Intent(ACTION_SNOOZE).apply {
                        setPackage(applicationContext.packageName)
                    }
                    val snoozePendingIntent = PendingIntent.getBroadcast(
                        applicationContext,
                        2,
                        snoozeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    notificationBuilder.addAction(
                        android.R.drawable.ic_menu_recent_history,
                        "Snooze 30 min",
                        snoozePendingIntent
                    )
                }
            }
        }
        
        // Show notification
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        
        // Wake up screen for high priority notifications
        if (priority >= NotificationCompat.PRIORITY_HIGH) {
            wakeUpScreen()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for battery charging alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun wakeUpScreen() {
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "VoltAI:ChargingAlert"
        )
        wakeLock.acquire(3000) // Wake screen for 3 seconds
    }
    
    private fun scheduleNextCheck(delayMinutes: Long) {
        val nextWorkRequest = OneTimeWorkRequestBuilder<ChargingAlertWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                nextWorkRequest
            )
    }
    
    enum class NotificationAction {
        STOP_CHARGING,
        SNOOZE
    }
    
    enum class ChargingSpeed {
        SLOW,
        NORMAL,
        FAST,
        SUPER_FAST
    }
    
    data class ChargingAlertSettings(
        val highBatteryAlertEnabled: Boolean = true,
        val highBatteryThreshold: Int = 80,
        val lowBatteryAlertEnabled: Boolean = true,
        val lowBatteryThreshold: Int = 20,
        val temperatureAlertEnabled: Boolean = true,
        val temperatureThreshold: Float = 40f,
        val fastChargingNotificationEnabled: Boolean = true,
        val optimalChargingEnabled: Boolean = true,
        val optimalChargingLimit: Int = 85
    )
}
