package com.voltai.smartbatteryguardian

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoltAIApplication : Application() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "voltai_monitoring"
        const val VERSION_NAME = "1.0.0"
        const val BUILD_TYPE = "Professional"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channels
        createNotificationChannels()
        
        // Initialize AI services (placeholder)
        initializeAIServices()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun initializeAIServices() {
        // Initialize AI battery optimization engine
        // This would normally connect to ML models and start background services
        // For now, this is a placeholder for future implementation
    }
    
    fun getVersionInfo(): String {
        return "$VERSION_NAME ($BUILD_TYPE)"
    }
}
