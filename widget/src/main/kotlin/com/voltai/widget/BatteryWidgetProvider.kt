package com.voltai.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BatteryWidgetProvider : AppWidgetProvider() {
    
    @Inject
    lateinit var widgetConfigManager: WidgetConfigManager
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
        appWidgetIds.forEach { widgetId ->
            scope.launch {
                updateWidget(context, appWidgetManager, widgetId)
            }
        }
    }
    
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { widgetId ->
            widgetConfigManager.removeWidgetConfig(widgetId)
        }
    }
    
    private suspend fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val config = widgetConfigManager.getWidgetConfig(widgetId)
        val batteryData = getBatteryData(context)
        
        val views = when (config.layout) {
            WidgetLayout.COMPACT -> createCompactWidget(context, batteryData, config)
            WidgetLayout.DETAILED -> createDetailedWidget(context, batteryData, config)
            WidgetLayout.CIRCULAR -> createCircularWidget(context, batteryData, config)
            WidgetLayout.MINIMAL -> createMinimalWidget(context, batteryData, config)
        }
        
        // Set click intent
        val intent = Intent(context, BatteryWidgetProvider::class.java).apply {
            action = "WIDGET_CLICK"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context, widgetId, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        appWidgetManager.updateAppWidget(widgetId, views)
    }
    
    private fun createCompactWidget(context: Context, data: BatteryData, config: WidgetConfig): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_compact)
        
        views.setTextViewText(R.id.battery_percentage, "${data.batteryLevel}%")
        views.setTextViewText(R.id.battery_status, data.status)
        
        // Set battery icon based on level
        val batteryIcon = when {
            data.batteryLevel >= 90 -> R.drawable.ic_battery_full
            data.batteryLevel >= 60 -> R.drawable.ic_battery_high
            data.batteryLevel >= 30 -> R.drawable.ic_battery_medium
            data.batteryLevel >= 15 -> R.drawable.ic_battery_low
            else -> R.drawable.ic_battery_critical
        }
        
        views.setImageViewResource(R.id.battery_icon, batteryIcon)
        
        // Apply theme
        applyWidgetTheme(views, config.theme)
        
        if (config.showTemperature) {
            views.setViewVisibility(R.id.temperature_text, android.view.View.VISIBLE)
            views.setTextViewText(R.id.temperature_text, "${data.temperature}°C")
        }
        
        return views
    }
    
    private fun createDetailedWidget(context: Context, data: BatteryData, config: WidgetConfig): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_detailed)
        
        views.setTextViewText(R.id.battery_percentage, "${data.batteryLevel}%")
        views.setTextViewText(R.id.battery_status, data.status)
        views.setTextViewText(R.id.voltage_text, "${String.format("%.2f", data.voltage)}V")
        views.setTextViewText(R.id.temperature_text, "${String.format("%.1f", data.temperature)}°C")
        views.setTextViewText(R.id.health_text, "${data.health}%")
        
        if (data.remainingTime.isNotEmpty()) {
            views.setTextViewText(R.id.remaining_time, data.remainingTime)
            views.setViewVisibility(R.id.remaining_time, android.view.View.VISIBLE)
        }
        
        // Create battery progress bar
        views.setProgressBar(R.id.battery_progress, 100, data.batteryLevel, false)
        
        applyWidgetTheme(views, config.theme)
        return views
    }
    
    private fun createCircularWidget(context: Context, data: BatteryData, config: WidgetConfig): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_circular)
        
        // Create circular battery indicator
        val circularBitmap = createCircularBatteryBitmap(context, data.batteryLevel, data.isCharging, config)
        views.setImageViewBitmap(R.id.circular_battery, circularBitmap)
        
        views.setTextViewText(R.id.battery_percentage, "${data.batteryLevel}%")
        
        if (config.showAdditionalInfo) {
            views.setTextViewText(R.id.additional_info, "${data.temperature}°C")
            views.setViewVisibility(R.id.additional_info, android.view.View.VISIBLE)
        }
        
        applyWidgetTheme(views, config.theme)
        return views
    }
    
    private fun createMinimalWidget(context: Context, data: BatteryData, config: WidgetConfig): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_minimal)
        
        views.setTextViewText(R.id.battery_percentage, "${data.batteryLevel}%")
        
        // Simple battery icon
        val color = when {
            data.batteryLevel >= 50 -> android.graphics.Color.GREEN
            data.batteryLevel >= 20 -> android.graphics.Color.YELLOW
            else -> android.graphics.Color.RED
        }
        
        views.setInt(R.id.battery_indicator, "setColorFilter", color)
        
        applyWidgetTheme(views, config.theme)
        return views
    }
    
    private fun createCircularBatteryBitmap(context: Context, batteryLevel: Int, isCharging: Boolean, config: WidgetConfig): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val paint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 12f
            style = Paint.Style.STROKE
        }
        
        val rect = RectF(20f, 20f, size - 20f, size - 20f)
        
        // Background circle
        paint.color = if (config.theme == WidgetTheme.DARK) {
            android.graphics.Color.parseColor("#333333")
        } else {
            android.graphics.Color.parseColor("#EEEEEE")
        }
        canvas.drawOval(rect, paint)
        
        // Battery level arc
        paint.color = when {
            batteryLevel >= 50 -> android.graphics.Color.parseColor("#4CAF50")
            batteryLevel >= 20 -> android.graphics.Color.parseColor("#FF9800")
            else -> android.graphics.Color.parseColor("#F44336")
        }
        
        val sweepAngle = (batteryLevel / 100f) * 360f
        canvas.drawArc(rect, -90f, sweepAngle, false, paint)
        
        // Charging indicator
        if (isCharging) {
            paint.style = Paint.Style.FILL
            paint.color = android.graphics.Color.parseColor("#FFC107")
            val centerX = size / 2f
            val centerY = size / 2f
            canvas.drawCircle(centerX, centerY, 15f, paint)
        }
        
        return bitmap
    }
    
    private fun applyWidgetTheme(views: RemoteViews, theme: WidgetTheme) {
        val backgroundRes = when (theme) {
            WidgetTheme.LIGHT -> R.drawable.widget_background_light
            WidgetTheme.DARK -> R.drawable.widget_background_dark
            WidgetTheme.TRANSPARENT -> R.drawable.widget_background_transparent
            WidgetTheme.CUSTOM -> R.drawable.widget_background_custom
        }
        
        views.setInt(R.id.widget_container, "setBackgroundResource", backgroundRes)
        
        val textColor = when (theme) {
            WidgetTheme.LIGHT -> android.graphics.Color.BLACK
            WidgetTheme.DARK -> android.graphics.Color.WHITE
            WidgetTheme.TRANSPARENT -> android.graphics.Color.WHITE
            WidgetTheme.CUSTOM -> android.graphics.Color.parseColor("#0066CC")
        }
        
        // Apply text colors to all text views
        views.setTextColor(R.id.battery_percentage, textColor)
        views.setTextColor(R.id.battery_status, textColor)
        views.setTextColor(R.id.temperature_text, textColor)
        views.setTextColor(R.id.voltage_text, textColor)
        views.setTextColor(R.id.health_text, textColor)
        views.setTextColor(R.id.remaining_time, textColor)
    }
    
    private fun getBatteryData(context: Context): BatteryData {
        val batteryStatus = context.registerReceiver(null, 
            android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        
        val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryLevel = (level * 100) / scale
        
        val status = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING
        
        val temperature = (batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val voltage = (batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
        val health = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1) ?: -1
        
        val statusString = when (status) {
            android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            android.os.BatteryManager.BATTERY_STATUS_FULL -> "Full"
            android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
        
        val healthPercentage = when (health) {
            android.os.BatteryManager.BATTERY_HEALTH_GOOD -> 100
            android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> 80
            android.os.BatteryManager.BATTERY_HEALTH_DEAD -> 0
            android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 70
            else -> 95
        }
        
        // Calculate remaining time (simplified)
        val remainingTime = if (isCharging) {
            val remainingPercent = 100 - batteryLevel
            val estimatedHours = remainingPercent / 20 // Rough estimate
            if (estimatedHours > 0) "${estimatedHours}h remaining" else ""
        } else {
            val estimatedHours = batteryLevel / 15 // Rough estimate
            if (estimatedHours > 0) "${estimatedHours}h remaining" else ""
        }
        
        return BatteryData(
            batteryLevel = batteryLevel,
            status = statusString,
            temperature = temperature,
            voltage = voltage,
            health = healthPercentage,
            isCharging = isCharging,
            remainingTime = remainingTime
        )
    }
    
    companion object {
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, BatteryWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}

data class BatteryData(
    val batteryLevel: Int,
    val status: String,
    val temperature: Float,
    val voltage: Float,
    val health: Int,
    val isCharging: Boolean,
    val remainingTime: String
)

data class WidgetConfig(
    val widgetId: Int,
    val layout: WidgetLayout = WidgetLayout.COMPACT,
    val theme: WidgetTheme = WidgetTheme.LIGHT,
    val updateInterval: Long = 60000, // 1 minute
    val showTemperature: Boolean = true,
    val showVoltage: Boolean = false,
    val showHealth: Boolean = false,
    val showAdditionalInfo: Boolean = false
)

enum class WidgetLayout {
    COMPACT,    // Single line with battery % and status
    DETAILED,   // Multiple lines with all info
    CIRCULAR,   // Circular battery indicator
    MINIMAL     // Just battery percentage
}

enum class WidgetTheme {
    LIGHT,
    DARK,
    TRANSPARENT,
    CUSTOM
}
