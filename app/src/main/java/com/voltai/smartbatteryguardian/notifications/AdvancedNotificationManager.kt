package com.voltai.smartbatteryguardian.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.voltai.smartbatteryguardian.MainActivity
import com.voltai.smartbatteryguardian.R
import com.voltai.smartbatteryguardian.ml.BatteryHealthPrediction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced notification system with intelligent alerts, priority management,
 * and actionable notifications for optimal user experience
 */
@Singleton
class AdvancedNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    
    companion object {
        // Notification Channels
        const val CHANNEL_BATTERY_MONITORING = "battery_monitoring"
        const val CHANNEL_BATTERY_ALERTS = "battery_alerts"
        const val CHANNEL_AI_INSIGHTS = "ai_insights"
        const val CHANNEL_OPTIMIZATION = "optimization"
        const val CHANNEL_CHARGING = "charging"
        const val CHANNEL_CRITICAL_ALERTS = "critical_alerts"
        const val CHANNEL_PREMIUM = "premium"
        
        // Notification IDs
        const val ID_BATTERY_MONITORING = 1001
        const val ID_LOW_BATTERY = 1002
        const val ID_HIGH_TEMPERATURE = 1003
        const val ID_CHARGING_COMPLETE = 1004
        const val ID_AI_HEALTH_ALERT = 1005
        const val ID_OPTIMIZATION_SUGGESTION = 1006
        const val ID_BATTERY_SAVER = 1007
        const val ID_CHARGING_SLOW = 1008
        const val ID_PREMIUM_FEATURE = 1009
        const val ID_WEEKLY_REPORT = 1010
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create all notification channels with appropriate priorities and behaviors
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_BATTERY_MONITORING,
                    "Battery Monitoring",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Ongoing battery monitoring service"
                    setShowBadge(false)
                    enableLights(false)
                    enableVibration(false)
                },
                
                NotificationChannel(
                    CHANNEL_BATTERY_ALERTS,
                    "Battery Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Important battery status alerts"
                    setShowBadge(true)
                    enableLights(true)
                    lightColor = android.graphics.Color.YELLOW
                },
                
                NotificationChannel(
                    CHANNEL_AI_INSIGHTS,
                    "AI Insights",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "AI-powered battery insights and recommendations"
                    setShowBadge(true)
                    enableLights(true)
                    lightColor = android.graphics.Color.BLUE
                },
                
                NotificationChannel(
                    CHANNEL_OPTIMIZATION,
                    "Optimization",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Battery optimization suggestions"
                    setShowBadge(true)
                },
                
                NotificationChannel(
                    CHANNEL_CHARGING,
                    "Charging Status",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Charging status and completion alerts"
                    setShowBadge(false)
                    enableLights(true)
                    lightColor = android.graphics.Color.GREEN
                },
                
                NotificationChannel(
                    CHANNEL_CRITICAL_ALERTS,
                    "Critical Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Critical battery and system alerts"
                    setShowBadge(true)
                    enableLights(true)
                    enableVibration(true)
                    lightColor = android.graphics.Color.RED
                },
                
                NotificationChannel(
                    CHANNEL_PREMIUM,
                    "Premium Features",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Premium feature notifications and reports"
                    setShowBadge(false)
                }
            )
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { channel ->
                systemNotificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    /**
     * Show persistent battery monitoring notification
     */
    fun showBatteryMonitoringNotification(
        batteryLevel: Int,
        isCharging: Boolean,
        aiRecommendation: String? = null
    ): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val status = if (isCharging) "Charging" else "On Battery"
        val contentText = if (aiRecommendation != null) {
            "$batteryLevel% - $status\n💡 $aiRecommendation"
        } else {
            "$batteryLevel% - $status"
        }
        
        val builder = NotificationCompat.Builder(context, CHANNEL_BATTERY_MONITORING)
            .setContentTitle("VoltAI Battery Guardian")
            .setContentText("$batteryLevel% - $status")
            .setSmallIcon(getBatteryIcon(batteryLevel, isCharging))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .setColor(getBatteryColor(batteryLevel))
        
        if (aiRecommendation != null) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
        }
        
        // Add quick actions
        addBatteryQuickActions(builder, batteryLevel, isCharging)
        
        val notification = builder.build()
        notificationManager.notify(ID_BATTERY_MONITORING, notification)
        return notification
    }
    
    /**
     * Update battery monitoring notification with current status
     */
    fun updateBatteryStatus(
        batteryLevel: Int,
        isCharging: Boolean,
        temperature: Float,
        aiRecommendation: String? = null
    ) {
        showBatteryMonitoringNotification(batteryLevel, isCharging, aiRecommendation)
    }
    
    /**
     * Show low battery alert with optimization suggestions
     */
    fun showLowBatteryAlert(batteryLevel: Int, estimatedTimeRemaining: String? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", "battery_saver")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ID_LOW_BATTERY, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val title = "Low Battery Warning"
        val text = if (estimatedTimeRemaining != null) {
            "Battery at $batteryLevel%. Approximately $estimatedTimeRemaining remaining."
        } else {
            "Battery at $batteryLevel%. Consider enabling battery saver mode."
        }
        
        val builder = NotificationCompat.Builder(context, CHANNEL_BATTERY_ALERTS)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#FF5722"))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Enable Battery Saver",
                createBatterySaverIntent()
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "View Details",
                pendingIntent
            )
        
        notificationManager.notify(ID_LOW_BATTERY, builder.build())
    }
    
    /**
     * Show high temperature warning - overload for Float parameter
     */
    fun showHighTemperatureAlert(temperature: Float) {
        showHighTemperatureAlert(temperature.toDouble())
    }
    
    /**
     * Show high temperature warning
     */
    fun showHighTemperatureAlert(temperature: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", "temperature_detail")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ID_HIGH_TEMPERATURE, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_CRITICAL_ALERTS)
            .setContentTitle("High Battery Temperature")
            .setContentText("Battery temperature: ${temperature.toInt()}°C. Consider cooling down your device.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#FF5722"))
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Battery temperature is ${temperature.toInt()}°C. High temperatures can damage battery health. " +
                "Remove device case, avoid direct sunlight, and stop intensive tasks."
            ))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Cool Down Tips",
                pendingIntent
            )
        
        notificationManager.notify(ID_HIGH_TEMPERATURE, builder.build())
    }
    
    /**
     * Show charging complete notification - overload without parameters
     */
    fun showChargingCompleteNotification() {
        showChargingCompleteNotification(100, 0L)
    }
    
    /**
     * Show charging complete notification
     */
    fun showChargingCompleteNotification(finalLevel: Int, chargingDuration: Long) {
        val hours = chargingDuration / (1000 * 60 * 60)
        val minutes = (chargingDuration % (1000 * 60 * 60)) / (1000 * 60)
        val durationText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        
        val builder = NotificationCompat.Builder(context, CHANNEL_CHARGING)
            .setContentTitle("Charging Complete")
            .setContentText("Battery charged to $finalLevel% in $durationText")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#4CAF50"))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "View Session",
                createChargingHistoryIntent()
            )
        
        notificationManager.notify(ID_CHARGING_COMPLETE, builder.build())
    }
    
    /**
     * Show battery health alert - overload with Float parameters
     */
    fun showBatteryHealthAlert(healthScore: Float, confidence: Float) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", "battery_health")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ID_AI_HEALTH_ALERT, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val healthScoreInt = healthScore.toInt()
        val title = when {
            healthScoreInt < 60 -> "Critical Battery Health"
            healthScoreInt < 80 -> "Battery Health Alert"
            else -> "Battery Health Update"
        }
        
        val priority = when {
            healthScoreInt < 60 -> NotificationCompat.PRIORITY_HIGH
            healthScoreInt < 80 -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }
        
        val channel = if (healthScoreInt < 60) CHANNEL_CRITICAL_ALERTS else CHANNEL_AI_INSIGHTS
        
        val builder = NotificationCompat.Builder(context, channel)
            .setContentTitle(title)
            .setContentText("Health: $healthScoreInt% | Confidence: ${(confidence * 100).toInt()}%")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .setAutoCancel(true)
            .setColor(getBatteryHealthColor(healthScoreInt))
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Battery Health: $healthScoreInt%\n" +
                "AI Confidence: ${(confidence * 100).toInt()}%\n" +
                "Recommendation: Monitor battery usage patterns."
            ))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "View Details",
                pendingIntent
            )
        
        notificationManager.notify(ID_AI_HEALTH_ALERT, builder.build())
    }
    
    /**
     * Show AI health alert with predictions
     */
    fun showAIHealthAlert(prediction: BatteryHealthPrediction) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", "battery_health")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ID_AI_HEALTH_ALERT, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val healthScore = prediction.healthScore.toInt()
        val title = when {
            healthScore < 60 -> "Critical Battery Health"
            healthScore < 80 -> "Battery Health Alert"
            else -> "Battery Health Update"
        }
        
        val priority = when {
            healthScore < 60 -> NotificationCompat.PRIORITY_HIGH
            healthScore < 80 -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }
        
        val channel = if (healthScore < 60) CHANNEL_CRITICAL_ALERTS else CHANNEL_AI_INSIGHTS
        
        val builder = NotificationCompat.Builder(context, channel)
            .setContentTitle(title)
            .setContentText("Health: $healthScore% | Confidence: ${(prediction.confidence * 100).toInt()}%")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .setAutoCancel(true)
            .setColor(getBatteryHealthColor(healthScore))
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Battery Health: $healthScore%\n" +
                "Degradation Rate: ${String.format("%.1f", prediction.degradationRate)}%/month\n" +
                "Top Recommendation: ${prediction.recommendations.firstOrNull() ?: "Monitor regularly"}"
            ))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "View Details",
                pendingIntent
            )
        
        if (prediction.recommendations.isNotEmpty()) {
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                "Apply Fix",
                createOptimizationIntent()
            )
        }
        
        notificationManager.notify(ID_AI_HEALTH_ALERT, builder.build())
    }
    
    /**
     * Show optimization suggestion notification
     */
    fun showOptimizationSuggestion(
        optimizationType: String,
        description: String,
        potentialSavings: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", "system_optimization")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ID_OPTIMIZATION_SUGGESTION, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_OPTIMIZATION)
            .setContentTitle("🚀 Battery Optimization Available")
            .setContentText("$optimizationType optimization can save $potentialSavings")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#2196F3"))
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$optimizationType Optimization Available\n\n$description\n\nPotential battery savings: $potentialSavings"
            ))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Apply Now",
                createOptimizationIntent()
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Learn More",
                pendingIntent
            )
        
        notificationManager.notify(ID_OPTIMIZATION_SUGGESTION, builder.build())
    }
    
    /**
     * Show slow charging alert
     */
    fun showSlowChargingAlert(chargingSpeed: Double, expectedSpeed: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", "charging_analytics")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ID_CHARGING_SLOW, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val speedDiff = ((expectedSpeed - chargingSpeed) / expectedSpeed * 100).toInt()
        
        val builder = NotificationCompat.Builder(context, CHANNEL_CHARGING)
            .setContentTitle("Slow Charging Detected")
            .setContentText("Charging ${speedDiff}% slower than expected")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#FF9800"))
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Slow Charging Detected\n\n" +
                "Current: ${String.format("%.1f", chargingSpeed)}W\n" +
                "Expected: ${String.format("%.1f", expectedSpeed)}W\n\n" +
                "Check cable, adapter, and charging port for issues."
            ))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Charging Tips",
                pendingIntent
            )
        
        notificationManager.notify(ID_CHARGING_SLOW, builder.build())
    }
    
    /**
     * Show weekly battery report
     */
    fun showWeeklyBatteryReport(
        avgBatteryLife: String,
        healthChange: Double,
        topDrainingApp: String,
        chargingSessions: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", "analytics_dashboard")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ID_WEEKLY_REPORT, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val healthChangeText = if (healthChange >= 0) {
            "+${String.format("%.1f", healthChange)}%"
        } else {
            "${String.format("%.1f", healthChange)}%"
        }
        
        val builder = NotificationCompat.Builder(context, CHANNEL_AI_INSIGHTS)
            .setContentTitle("📊 Weekly Battery Report")
            .setContentText("Avg life: $avgBatteryLife | Health: $healthChangeText")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#9C27B0"))
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Weekly Battery Summary\n\n" +
                "📱 Average battery life: $avgBatteryLife\n" +
                "💚 Health change: $healthChangeText\n" +
                "⚡ Charging sessions: $chargingSessions\n" +
                "📊 Top battery app: $topDrainingApp\n\n" +
                "Tap for detailed insights and trends."
            ))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "View Report",
                pendingIntent
            )
        
        notificationManager.notify(ID_WEEKLY_REPORT, builder.build())
    }
    
    /**
     * Cancel specific notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Cancel all notifications except persistent monitoring
     */
    fun cancelAllExceptMonitoring() {
        listOf(
            ID_LOW_BATTERY, ID_HIGH_TEMPERATURE, ID_CHARGING_COMPLETE,
            ID_AI_HEALTH_ALERT, ID_OPTIMIZATION_SUGGESTION, ID_CHARGING_SLOW,
            ID_PREMIUM_FEATURE, ID_WEEKLY_REPORT
        ).forEach { notificationId ->
            notificationManager.cancel(notificationId)
        }
    }
    
    // Helper methods
    private fun addBatteryQuickActions(
        builder: NotificationCompat.Builder,
        batteryLevel: Int,
        isCharging: Boolean
    ) {
        if (!isCharging && batteryLevel <= 20) {
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                "Battery Saver",
                createBatterySaverIntent()
            )
        }
        
        builder.addAction(
            R.drawable.ic_launcher_foreground,
            "Details",
            createMainAppIntent()
        )
    }
    
    private fun getBatteryIcon(batteryLevel: Int, isCharging: Boolean): Int {
        return R.drawable.ic_launcher_foreground // Simplified - would use actual battery icons
    }
    
    private fun getBatteryColor(batteryLevel: Int): Int {
        return when {
            batteryLevel > 80 -> android.graphics.Color.parseColor("#4CAF50")
            batteryLevel > 50 -> android.graphics.Color.parseColor("#FF9800")
            batteryLevel > 20 -> android.graphics.Color.parseColor("#FF5722")
            else -> android.graphics.Color.parseColor("#D32F2F")
        }
    }
    
    private fun getBatteryHealthColor(healthScore: Int): Int {
        return when {
            healthScore >= 90 -> android.graphics.Color.parseColor("#4CAF50")
            healthScore >= 70 -> android.graphics.Color.parseColor("#FF9800")
            healthScore >= 50 -> android.graphics.Color.parseColor("#FF5722")
            else -> android.graphics.Color.parseColor("#D32F2F")
        }
    }
    
    private fun createMainAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    
    private fun createBatterySaverIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "enable_battery_saver")
        }
        return PendingIntent.getActivity(
            context, 100, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    
    private fun createOptimizationIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", "system_optimization")
        }
        return PendingIntent.getActivity(
            context, 200, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    
    private fun createChargingHistoryIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_screen", "charging_analytics")
        }
        return PendingIntent.getActivity(
            context, 300, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}

/**
 * Notification priority levels for intelligent notification management
 */
enum class NotificationPriority {
    SILENT,
    LOW,
    DEFAULT,
    HIGH,
    CRITICAL
}

/**
 * Data class for notification configuration
 */
data class NotificationConfig(
    val title: String,
    val content: String,
    val priority: NotificationPriority,
    val channel: String,
    val autoCancel: Boolean = true,
    val ongoing: Boolean = false,
    val actions: List<NotificationAction> = emptyList()
)

/**
 * Data class for notification actions
 */
data class NotificationAction(
    val icon: Int,
    val title: String,
    val intent: PendingIntent
)
