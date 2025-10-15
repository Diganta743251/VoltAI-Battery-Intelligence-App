package com.voltai.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.voltai.core.analytics.BatteryAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartNotificationManager @Inject constructor(
    private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _notificationHistory = MutableStateFlow<List<NotificationEvent>>(emptyList())
    val notificationHistory: Flow<List<NotificationEvent>> = _notificationHistory.asStateFlow()
    
    // Notification settings
    private val notificationSettings = MutableStateFlow(NotificationSettings())
    
    // Notification channels
    companion object {
        const val CHANNEL_BATTERY_ALERTS = "battery_alerts"
        const val CHANNEL_CHARGING_STATUS = "charging_status"
        const val CHANNEL_HEALTH_WARNINGS = "health_warnings"
        const val CHANNEL_MAINTENANCE = "maintenance_reminders"
        const val CHANNEL_POWER_SAVING = "power_saving_tips"
        const val CHANNEL_ANALYTICS = "analytics_insights"
        
        // Notification IDs
        const val NOTIFICATION_LOW_BATTERY = 1001
        const val NOTIFICATION_HIGH_TEMPERATURE = 1002
        const val NOTIFICATION_CHARGING_COMPLETE = 1003
        const val NOTIFICATION_HEALTH_WARNING = 1004
        const val NOTIFICATION_MAINTENANCE_DUE = 1005
        const val NOTIFICATION_POWER_SAVING_TIP = 1006
        const val NOTIFICATION_ANALYTICS_INSIGHT = 1007
        const val NOTIFICATION_OPTIMAL_CHARGING = 1008
        const val NOTIFICATION_CYCLE_COUNT_MILESTONE = 1009
        const val NOTIFICATION_EFFICIENCY_ALERT = 1010
    }
    
    init {
        createNotificationChannels()
        startNotificationEngine()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_BATTERY_ALERTS,
                    "Battery Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Critical battery level alerts"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                },
                NotificationChannel(
                    CHANNEL_CHARGING_STATUS,
                    "Charging Status",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Charging completion and status updates"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 100, 100, 100)
                },
                NotificationChannel(
                    CHANNEL_HEALTH_WARNINGS,
                    "Battery Health",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Battery health warnings and degradation alerts"
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    CHANNEL_MAINTENANCE,
                    "Maintenance Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Battery maintenance and optimization reminders"
                    enableVibration(false)
                    setShowBadge(true)
                },
                NotificationChannel(
                    CHANNEL_POWER_SAVING,
                    "Power Saving Tips",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Smart power saving recommendations"
                    enableVibration(false)
                    setShowBadge(false)
                },
                NotificationChannel(
                    CHANNEL_ANALYTICS,
                    "Analytics Insights",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Battery usage insights and analytics"
                    enableVibration(false)
                    setShowBadge(false)
                }
            )
            
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    private fun startNotificationEngine() {
        scope.launch {
            while (true) {
                // Check for scheduled notifications every minute
                delay(60_000)
                checkScheduledNotifications()
            }
        }
    }
    
    fun processeBatteryUpdate(
        batteryLevel: Int,
        isCharging: Boolean,
        temperature: Float,
        voltage: Float,
        analytics: BatteryAnalytics
    ) {
        scope.launch {
            // Low battery alert
            if (batteryLevel <= 15 && !isCharging && notificationSettings.value.lowBatteryEnabled) {
                showLowBatteryNotification(batteryLevel)
            }
            
            // High temperature warning
            if (temperature > 45f && notificationSettings.value.temperatureWarningEnabled) {
                showHighTemperatureNotification(temperature)
            }
            
            // Charging complete notification
            if (batteryLevel >= 95 && isCharging && notificationSettings.value.chargingCompleteEnabled) {
                showChargingCompleteNotification(batteryLevel)
            }
            
            // Optimal charging notification
            if (batteryLevel >= 80 && isCharging && notificationSettings.value.optimalChargingEnabled) {
                showOptimalChargingNotification(batteryLevel)
            }
            
            // Health warning
            if (analytics.batteryHealthScore < 70f && notificationSettings.value.healthWarningEnabled) {
                showHealthWarningNotification(analytics.batteryHealthScore, analytics.usageRecommendations)
            }
            
            // Maintenance reminder
            if (shouldShowMaintenanceReminder(analytics)) {
                showMaintenanceReminderNotification(analytics)
            }
            
            // Analytics insights
            if (shouldShowAnalyticsInsight(analytics)) {
                showAnalyticsInsightNotification(analytics)
            }
            
            // Cycle count milestone
            if (analytics.totalCycleCount > 0 && analytics.totalCycleCount % 100 == 0) {
                showCycleCountMilestoneNotification(analytics.totalCycleCount)
            }
        }
    }
    
    private fun showLowBatteryNotification(batteryLevel: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_BATTERY_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Low Battery Alert")
            .setContentText("Battery at $batteryLevel%. Please charge your device.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your battery is running low at $batteryLevel%. Consider charging your device soon to avoid unexpected shutdown.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFFFF4444.toInt())
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Enable Power Saving",
                createPowerSavingPendingIntent()
            )
            .addAction(
                android.R.drawable.ic_menu_info_details,
                "View Battery Info",
                createBatteryInfoPendingIntent()
            )
            .build()
            
        notificationManager.notify(NOTIFICATION_LOW_BATTERY, notification)
        addNotificationEvent(NotificationEvent.LowBattery(batteryLevel, System.currentTimeMillis()))
    }
    
    private fun showHighTemperatureNotification(temperature: Float) {
        val notification = NotificationCompat.Builder(context, CHANNEL_HEALTH_WARNINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🌡️ High Temperature Warning")
            .setContentText("Battery temperature is ${String.format("%.1f", temperature)}°C")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your battery is running hot at ${String.format("%.1f", temperature)}°C. Consider letting it cool down by removing the case and avoiding direct sunlight.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFFFF9800.toInt())
            .addAction(
                android.R.drawable.ic_menu_info_details,
                "Cooling Tips",
                createCoolingTipsPendingIntent()
            )
            .build()
            
        notificationManager.notify(NOTIFICATION_HIGH_TEMPERATURE, notification)
        addNotificationEvent(NotificationEvent.HighTemperature(temperature, System.currentTimeMillis()))
    }
    
    private fun showChargingCompleteNotification(batteryLevel: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_CHARGING_STATUS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔋 Charging Complete")
            .setContentText("Battery charged to $batteryLevel%. Ready to unplug!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your device is now charged to $batteryLevel%. You can safely unplug the charger. Keeping it plugged longer won't harm the battery thanks to modern charging protection.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(0xFF4CAF50.toInt())
            .build()
            
        notificationManager.notify(NOTIFICATION_CHARGING_COMPLETE, notification)
        addNotificationEvent(NotificationEvent.ChargingComplete(batteryLevel, System.currentTimeMillis()))
    }
    
    private fun showOptimalChargingNotification(batteryLevel: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_POWER_SAVING)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚡ Optimal Charging Reached")
            .setContentText("Battery at $batteryLevel%. Consider unplugging for optimal health.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your battery has reached $batteryLevel%, which is the optimal charging level for long-term battery health. You can unplug now to maximize battery lifespan.")
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setColor(0xFF2196F3.toInt())
            .build()
            
        notificationManager.notify(NOTIFICATION_OPTIMAL_CHARGING, notification)
        addNotificationEvent(NotificationEvent.OptimalCharging(batteryLevel, System.currentTimeMillis()))
    }
    
    private fun showHealthWarningNotification(healthScore: Float, recommendations: List<String>) {
        val recommendationText = recommendations.take(2).joinToString(". ")
        
        val notification = NotificationCompat.Builder(context, CHANNEL_HEALTH_WARNINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Battery Health Alert")
            .setContentText("Battery health: ${String.format("%.0f", healthScore)}%")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your battery health has dropped to ${String.format("%.0f", healthScore)}%. $recommendationText")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFFFF9800.toInt())
            .addAction(
                android.R.drawable.ic_menu_info_details,
                "View Recommendations",
                createHealthRecommendationsPendingIntent()
            )
            .build()
            
        notificationManager.notify(NOTIFICATION_HEALTH_WARNING, notification)
        addNotificationEvent(NotificationEvent.HealthWarning(healthScore, System.currentTimeMillis()))
    }
    
    private fun showMaintenanceReminderNotification(analytics: BatteryAnalytics) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val maintenanceDate = dateFormat.format(Date(analytics.nextMaintenanceDate))
        
        val notification = NotificationCompat.Builder(context, CHANNEL_MAINTENANCE)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("🔧 Battery Maintenance Due")
            .setContentText("Scheduled maintenance: $maintenanceDate")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your battery is due for maintenance on $maintenanceDate. Regular maintenance helps preserve battery life and performance.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(0xFF9C27B0.toInt())
            .addAction(
                android.R.drawable.ic_menu_agenda,
                "Schedule Service",
                createMaintenanceSchedulePendingIntent()
            )
            .build()
            
        notificationManager.notify(NOTIFICATION_MAINTENANCE_DUE, notification)
        addNotificationEvent(NotificationEvent.MaintenanceReminder(analytics.nextMaintenanceDate, System.currentTimeMillis()))
    }
    
    private fun showAnalyticsInsightNotification(analytics: BatteryAnalytics) {
        val insight = generateInsightText(analytics)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ANALYTICS)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("📊 Battery Insight")
            .setContentText(insight.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(insight.description)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setColor(0xFF607D8B.toInt())
            .addAction(
                android.R.drawable.ic_menu_view,
                "View Analytics",
                createAnalyticsPendingIntent()
            )
            .build()
            
        notificationManager.notify(NOTIFICATION_ANALYTICS_INSIGHT, notification)
        addNotificationEvent(NotificationEvent.AnalyticsInsight(insight.title, System.currentTimeMillis()))
    }
    
    private fun showCycleCountMilestoneNotification(cycleCount: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ANALYTICS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔄 Charging Cycle Milestone")
            .setContentText("Reached $cycleCount charging cycles!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your battery has completed $cycleCount charging cycles. This milestone helps track battery longevity and usage patterns.")
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setColor(0xFF3F51B5.toInt())
            .build()
            
        notificationManager.notify(NOTIFICATION_CYCLE_COUNT_MILESTONE, notification)
        addNotificationEvent(NotificationEvent.CycleMilestone(cycleCount, System.currentTimeMillis()))
    }
    
    private fun shouldShowMaintenanceReminder(analytics: BatteryAnalytics): Boolean {
        val timeDiff = analytics.nextMaintenanceDate - System.currentTimeMillis()
        val daysUntilMaintenance = timeDiff / (24 * 60 * 60 * 1000)
        
        return daysUntilMaintenance <= 30 && daysUntilMaintenance > 0
    }
    
    private fun shouldShowAnalyticsInsight(analytics: BatteryAnalytics): Boolean {
        // Show insights weekly if there's meaningful data
        return analytics.totalCycleCount > 10 && 
               analytics.predictionAccuracy > 70f &&
               System.currentTimeMillis() % (7 * 24 * 60 * 60 * 1000) < 60000 // Weekly
    }
    
    private fun generateInsightText(analytics: BatteryAnalytics): Insight {
        return when {
            analytics.chargingEfficiency < 80f -> Insight(
                title = "Charging efficiency is ${String.format("%.0f", analytics.chargingEfficiency)}%",
                description = "Your charging efficiency could be improved. Try using the original charger and avoid charging in hot environments."
            )
            analytics.temperatureAnalysis.averageTemperature > 35f -> Insight(
                title = "Average temperature is high",
                description = "Your device has been running warm (${String.format("%.1f", analytics.temperatureAnalysis.averageTemperature)}°C avg). Consider using it in cooler environments."
            )
            analytics.totalCycleCount > 300 -> Insight(
                title = "Battery aging analysis",
                description = "With ${analytics.totalCycleCount} cycles completed, your battery is showing normal aging patterns. Health score: ${String.format("%.0f", analytics.batteryHealthScore)}%"
            )
            else -> Insight(
                title = "Battery performance is optimal",
                description = "Your battery is performing well with ${String.format("%.0f", analytics.batteryHealthScore)}% health and ${String.format("%.0f", analytics.chargingEfficiency)}% charging efficiency."
            )
        }
    }
    
    private fun checkScheduledNotifications() {
        // Check for any scheduled notifications that need to be sent
        // This could include reminders, maintenance alerts, etc.
    }
    
    private fun addNotificationEvent(event: NotificationEvent) {
        val currentHistory = _notificationHistory.value.toMutableList()
        currentHistory.add(0, event) // Add to beginning
        
        // Keep only last 100 notifications
        if (currentHistory.size > 100) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        _notificationHistory.value = currentHistory
    }
    
    // Pending Intent creators
    private fun createPowerSavingPendingIntent(): PendingIntent {
        val intent = Intent(context, Class.forName("com.voltai.smartbatteryguardian.MainActivity"))
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createBatteryInfoPendingIntent(): PendingIntent {
        val intent = Intent(context, Class.forName("com.voltai.smartbatteryguardian.MainActivity"))
        return PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createCoolingTipsPendingIntent(): PendingIntent {
        val intent = Intent(context, Class.forName("com.voltai.smartbatteryguardian.MainActivity"))
        return PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createHealthRecommendationsPendingIntent(): PendingIntent {
        val intent = Intent(context, Class.forName("com.voltai.smartbatteryguardian.MainActivity"))
        return PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createMaintenanceSchedulePendingIntent(): PendingIntent {
        val intent = Intent(context, Class.forName("com.voltai.smartbatteryguardian.MainActivity"))
        return PendingIntent.getActivity(
            context,
            4,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createAnalyticsPendingIntent(): PendingIntent {
        val intent = Intent(context, Class.forName("com.voltai.smartbatteryguardian.MainActivity"))
        return PendingIntent.getActivity(
            context,
            5,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    // Settings management
    fun updateNotificationSettings(settings: NotificationSettings) {
        notificationSettings.value = settings
    }
    
    fun getNotificationSettings(): NotificationSettings = notificationSettings.value
    
    // Clear notifications
    fun clearAllNotifications() {
        notificationManager.cancelAll()
    }
    
    fun clearNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}

// Data classes
data class NotificationSettings(
    val lowBatteryEnabled: Boolean = true,
    val lowBatteryThreshold: Int = 15,
    val temperatureWarningEnabled: Boolean = true,
    val temperatureThreshold: Float = 45f,
    val chargingCompleteEnabled: Boolean = true,
    val optimalChargingEnabled: Boolean = true,
    val healthWarningEnabled: Boolean = true,
    val maintenanceRemindersEnabled: Boolean = true,
    val analyticsInsightsEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 22, // 10 PM
    val quietHoursEnd: Int = 8 // 8 AM
)

sealed class NotificationEvent {
    abstract val timestamp: Long
    
    data class LowBattery(val level: Int, override val timestamp: Long) : NotificationEvent()
    data class HighTemperature(val temperature: Float, override val timestamp: Long) : NotificationEvent()
    data class ChargingComplete(val level: Int, override val timestamp: Long) : NotificationEvent()
    data class OptimalCharging(val level: Int, override val timestamp: Long) : NotificationEvent()
    data class HealthWarning(val healthScore: Float, override val timestamp: Long) : NotificationEvent()
    data class MaintenanceReminder(val dueDate: Long, override val timestamp: Long) : NotificationEvent()
    data class AnalyticsInsight(val insight: String, override val timestamp: Long) : NotificationEvent()
    data class CycleMilestone(val cycleCount: Int, override val timestamp: Long) : NotificationEvent()
}

private data class Insight(
    val title: String,
    val description: String
)
