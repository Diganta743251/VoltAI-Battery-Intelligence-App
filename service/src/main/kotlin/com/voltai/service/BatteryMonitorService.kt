package com.voltai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BatteryMonitorService : Service() {

    private var batteryReceiver: BroadcastReceiver? = null
    private var lastBatteryLevel = -1
    private var lastChargingStatus = -1

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "battery_monitor_channel"
        private const val CHANNEL_NAME = "Battery Monitor"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Starting battery monitoring..."))
        registerBatteryReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterBatteryReceiver()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Battery monitoring service"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VoltAI Battery Monitor")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { handleBatteryChanged(it) }
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    private fun unregisterBatteryReceiver() {
        batteryReceiver?.let { 
            try {
                unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered
            }
        }
    }

    private fun handleBatteryChanged(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPercentage = if (level != -1 && scale != -1) {
            (level * 100) / scale
        } else {
            -1
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)

        // Only update notification on significant changes
        val shouldUpdate = batteryPercentage != lastBatteryLevel || 
                          status != lastChargingStatus

        if (shouldUpdate && batteryPercentage != -1) {
            val chargingStatus = getChargingStatusString(status)
            val tempCelsius = temperature / 10f
            
            val content = "${batteryPercentage}% • ${chargingStatus} • ${tempCelsius.toInt()}°C"
            updateNotification(content)

            lastBatteryLevel = batteryPercentage
            lastChargingStatus = status
        }
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getChargingStatusString(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            else -> "Unknown"
        }
    }
}
