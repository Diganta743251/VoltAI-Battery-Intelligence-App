package com.voltai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.Service
import com.voltai.data.local.BatteryLogDao
import com.voltai.data.local.BatteryLogEntity
import com.voltai.domain.model.BatteryStatus
import com.voltai.core.utils.BatteryUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class BatteryForegroundService : Service() {

    @Inject
    lateinit var batteryLogDao: BatteryLogDao
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val batteryStatus = BatteryUtils.getBatteryStatus(context)
            logBatteryData(batteryStatus)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun logBatteryData(batteryStatus: BatteryStatus) {
        serviceScope.launch {
            val batteryLogEntity = BatteryLogEntity(
                timestamp = System.currentTimeMillis(),
                batteryPercentage = batteryStatus.percentage,
                voltage = (batteryStatus.voltage * 1000).toInt(), // Convert to millivolts
                temperature = (batteryStatus.temperature * 10).toInt(), // Convert to decidegrees
                status = when (batteryStatus.status) {
                    "Charging" -> android.os.BatteryManager.BATTERY_STATUS_CHARGING
                    "Discharging" -> android.os.BatteryManager.BATTERY_STATUS_DISCHARGING
                    "Full" -> android.os.BatteryManager.BATTERY_STATUS_FULL
                    "Not Charging" -> android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING
                    else -> android.os.BatteryManager.BATTERY_STATUS_UNKNOWN
                },
                chargePlug = when (batteryStatus.chargingType) {
                    "AC" -> android.os.BatteryManager.BATTERY_PLUGGED_AC
                    "USB" -> android.os.BatteryManager.BATTERY_PLUGGED_USB
                    "Wireless" -> android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS
                    else -> 0
                },
                currentNow = (batteryStatus.current * 1000).toInt(), // Convert mA to µA
                health = batteryStatus.status
            )
            batteryLogDao.insertBatteryLog(batteryLogEntity)
            Log.d(TAG, "Battery data logged: $batteryLogEntity")
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VoltAI: Smart Battery Guardian")
            .setContentText("Monitoring battery status...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Battery Monitoring", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "BatteryForegroundService"
        private const val CHANNEL_ID = "BatteryServiceChannel"
        private const val NOTIFICATION_ID = 1
    }
}
