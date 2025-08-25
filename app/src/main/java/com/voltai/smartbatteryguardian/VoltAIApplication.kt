package com.voltai.smartbatteryguardian

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.getSystemService
import com.voltai.ui.components.HapticFeedback
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoltAIApplication : Application() {
    
    companion object {
        const val BATTERY_CHANNEL_ID = "battery_monitoring"
        const val ALERTS_CHANNEL_ID = "battery_alerts"
        const val OPTIMIZATION_CHANNEL_ID = "battery_optimization"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize haptic feedback
        HapticFeedback.init(this)
        
        // Create notification channels
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService<NotificationManager>()
            
            // Battery monitoring channel
            val batteryChannel = NotificationChannel(
                BATTERY_CHANNEL_ID,
                "Battery Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing battery monitoring notifications"
                setShowBadge(false)
            }
            
            // Battery alerts channel
            val alertsChannel = NotificationChannel(
                ALERTS_CHANNEL_ID,
                "Battery Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Important battery status alerts"
            }
            
            // Optimization tips channel
            val optimizationChannel = NotificationChannel(
                OPTIMIZATION_CHANNEL_ID,
                "Battery Optimization",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Battery optimization tips and suggestions"
                setShowBadge(false)
            }
            
            notificationManager?.createNotificationChannels(
                listOf(batteryChannel, alertsChannel, optimizationChannel)
            )
        }
    }
}
