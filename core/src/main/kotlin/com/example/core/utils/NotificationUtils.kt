package com.voltai.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationUtils {

    const val BATTERY_STATUS_CHANNEL_ID = "battery_status_channel"
    const val BATTERY_ALERT_CHANNEL_ID = "battery_alert_channel"

    fun createChannel(context: Context, channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager = 
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val batteryStatusChannel = NotificationChannel(
                BATTERY_STATUS_CHANNEL_ID,
                "Battery Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for ongoing battery status."
            }

            val batteryAlertChannel = NotificationChannel(
                BATTERY_ALERT_CHANNEL_ID,
                "Battery Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for important battery alerts like overheating or low battery."
            }

            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(batteryStatusChannel)
            notificationManager.createNotificationChannel(batteryAlertChannel)
        }
    }

    fun sendBatteryStatusNotification(context: Context, title: String, content: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, BATTERY_STATUS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun sendBatteryAlertNotification(context: Context, title: String, content: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(context, BATTERY_ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}
