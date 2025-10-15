package com.voltai.smartbatteryguardian.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.BatteryManager
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.voltai.smartbatteryguardian.MainActivity
import com.voltai.smartbatteryguardian.R
import kotlin.math.roundToInt

/**
 * Advanced Battery Widget with multiple sizes and interactive features
 * Provides real-time battery status, quick actions, and optimization shortcuts
 */
class BatteryWidget : AppWidgetProvider() {
    companion object {
        private const val ACTION_BATTERY_SAVER_TOGGLE = "com.voltai.smartbatteryguardian.BATTERY_SAVER_TOGGLE"
        private const val ACTION_OPTIMIZATION_TOGGLE = "com.voltai.smartbatteryguardian.OPTIMIZATION_TOGGLE"
        private const val ACTION_REFRESH_WIDGET = "com.voltai.smartbatteryguardian.REFRESH_WIDGET"
        private const val ACTION_OPEN_APP = "com.voltai.smartbatteryguardian.OPEN_APP"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Update all widget instances
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_BATTERY_SAVER_TOGGLE -> {
                handleBatterySaverToggle(context)
                refreshAllWidgets(context)
            }
            ACTION_OPTIMIZATION_TOGGLE -> {
                handleOptimizationToggle(context)
                refreshAllWidgets(context)
            }
            ACTION_REFRESH_WIDGET -> {
                refreshAllWidgets(context)
            }
            ACTION_OPEN_APP -> {
                openMainApp(context)
            }
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_BOOT_COMPLETED,
            -> {
                refreshAllWidgets(context)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        val batteryInfo = getBatteryInfo(context)
        val widgetSize = getWidgetSize(context, appWidgetManager, appWidgetId)

        val views =
            when (widgetSize) {
                WidgetSize.SMALL -> createSmallWidget(context, batteryInfo)
                WidgetSize.MEDIUM -> createMediumWidget(context, batteryInfo)
                WidgetSize.LARGE -> createLargeWidget(context, batteryInfo)
            }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createSmallWidget(
        context: Context,
        batteryInfo: BatteryInfo,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_battery_small)

        // Update battery level
        views.setTextViewText(R.id.battery_percentage, "${batteryInfo.level}%")
        views.setTextViewText(R.id.battery_status, getBatteryStatusText(batteryInfo))

        // Set battery color based on level
        val color = getBatteryColor(context, batteryInfo.level)
        views.setTextColor(R.id.battery_percentage, color)
        // Ensure status text has proper contrast on all backgrounds
        views.setTextColor(
            R.id.battery_status,
            ContextCompat.getColor(context, R.color.widget_text_secondary),
        )

        // Create circular battery indicator
        val batteryBitmap = createBatteryCircle(context, batteryInfo.level, batteryInfo.isCharging)
        views.setImageViewBitmap(R.id.battery_circle, batteryBitmap)

        // Set click to open app
        val openAppIntent = createPendingIntent(context, ACTION_OPEN_APP)
        views.setOnClickPendingIntent(R.id.widget_container, openAppIntent)

        return views
    }

    private fun createMediumWidget(
        context: Context,
        batteryInfo: BatteryInfo,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_battery_medium)

        // Update battery information
        views.setTextViewText(R.id.battery_percentage, "${batteryInfo.level}%")
        views.setTextViewText(R.id.battery_status, getBatteryStatusText(batteryInfo))
        views.setTextViewText(R.id.time_remaining, formatTimeRemaining(batteryInfo.timeRemaining))
        views.setTextViewText(R.id.temperature, "${batteryInfo.temperature}°C")

        // Set colors
        val batteryColor = getBatteryColor(context, batteryInfo.level)
        val tempColor = getTemperatureColor(context, batteryInfo.temperature)
        views.setTextColor(R.id.battery_percentage, batteryColor)
        views.setTextColor(R.id.temperature, tempColor)
        // Secondary texts for readability
        views.setTextColor(
            R.id.battery_status,
            ContextCompat.getColor(context, R.color.widget_text_secondary),
        )
        views.setTextColor(
            R.id.time_remaining,
            ContextCompat.getColor(context, R.color.widget_text_secondary),
        )

        // Create battery progress bar
        val progressBitmap = createBatteryProgressBar(context, batteryInfo.level, batteryInfo.isCharging)
        views.setImageViewBitmap(R.id.battery_progress, progressBitmap)

        // Setup quick action buttons
        setupQuickActions(context, views)

        return views
    }

    private fun createLargeWidget(
        context: Context,
        batteryInfo: BatteryInfo,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_battery_large)

        // Update comprehensive battery information
        views.setTextViewText(R.id.battery_percentage, "${batteryInfo.level}%")
        views.setTextViewText(R.id.battery_status, getBatteryStatusText(batteryInfo))
        views.setTextViewText(R.id.time_remaining, formatTimeRemaining(batteryInfo.timeRemaining))
        views.setTextViewText(R.id.temperature, "${batteryInfo.temperature}°C")
        views.setTextViewText(R.id.voltage, "${batteryInfo.voltage}V")
        views.setTextViewText(R.id.health, getBatteryHealthText(batteryInfo.health))

        // Set colors
        views.setTextColor(R.id.battery_percentage, getBatteryColor(context, batteryInfo.level))
        views.setTextColor(R.id.temperature, getTemperatureColor(context, batteryInfo.temperature))
        views.setTextColor(R.id.health, getHealthColor(context, batteryInfo.health))
        // Secondary texts for readability
        views.setTextColor(
            R.id.battery_status,
            ContextCompat.getColor(context, R.color.widget_text_secondary),
        )
        views.setTextColor(
            R.id.time_remaining,
            ContextCompat.getColor(context, R.color.widget_text_secondary),
        )

        // Create advanced battery visualization
        val batteryViz = createAdvancedBatteryVisualization(context, batteryInfo)
        views.setImageViewBitmap(R.id.battery_visualization, batteryViz)

        // Setup advanced quick actions
        setupAdvancedQuickActions(context, views)

        // Add optimization status
        val optimizationStatus = getOptimizationStatus(context)
        views.setTextViewText(R.id.optimization_status, optimizationStatus)

        return views
    }

    private fun setupQuickActions(
        context: Context,
        views: RemoteViews,
    ) {
        // Battery saver toggle
        val batterySaverIntent = createPendingIntent(context, ACTION_BATTERY_SAVER_TOGGLE)
        views.setOnClickPendingIntent(R.id.battery_saver_button, batterySaverIntent)

        // Optimization toggle
        val optimizationIntent = createPendingIntent(context, ACTION_OPTIMIZATION_TOGGLE)
        views.setOnClickPendingIntent(R.id.optimization_button, optimizationIntent)

        // Refresh button
        val refreshIntent = createPendingIntent(context, ACTION_REFRESH_WIDGET)
        views.setOnClickPendingIntent(R.id.refresh_button, refreshIntent)
    }

    private fun setupAdvancedQuickActions(
        context: Context,
        views: RemoteViews,
    ) {
        setupQuickActions(context, views)

        // Open app button
        val openAppIntent = createPendingIntent(context, ACTION_OPEN_APP)
        views.setOnClickPendingIntent(R.id.open_app_button, openAppIntent)

        // Settings shortcut (if available)
        try {
            val settingsIntent =
                Intent(context, MainActivity::class.java).apply {
                    putExtra("navigate_to", "settings")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            val settingsPendingIntent =
                PendingIntent.getActivity(
                    context,
                    4,
                    settingsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            views.setOnClickPendingIntent(R.id.settings_button, settingsPendingIntent)
        } catch (e: Exception) {
            // Fallback to main app
            views.setOnClickPendingIntent(R.id.settings_button, openAppIntent)
        }
    }

    private fun createBatteryCircle(
        context: Context,
        level: Int,
        isCharging: Boolean,
    ): Bitmap {
        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 8f
            }

        val rect = RectF(16f, 16f, size - 16f, size - 16f)

        // Background circle
        paint.color = ContextCompat.getColor(context, R.color.widget_background)
        canvas.drawOval(rect, paint)

        // Battery level arc
        paint.color = getBatteryColor(context, level)
        val sweepAngle = (level / 100f) * 360f
        canvas.drawArc(rect, -90f, sweepAngle, false, paint)

        // Charging indicator
        if (isCharging) {
            paint.style = Paint.Style.FILL
            paint.color = ContextCompat.getColor(context, R.color.charging_indicator)
            canvas.drawCircle(size / 2f, size / 2f, 8f, paint)
        }

        return bitmap
    }

    private fun createBatteryProgressBar(
        context: Context,
        level: Int,
        isCharging: Boolean,
    ): Bitmap {
        val width = 200
        val height = 20
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint =
            Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }

        // Background
        paint.color = ContextCompat.getColor(context, R.color.widget_background)
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 10f, 10f, paint)

        // Progress
        paint.color = getBatteryColor(context, level)
        val progressWidth = (level / 100f) * width
        canvas.drawRoundRect(RectF(0f, 0f, progressWidth, height.toFloat()), 10f, 10f, paint)

        // Charging animation effect
        if (isCharging) {
            paint.color = ContextCompat.getColor(context, R.color.charging_overlay)
            paint.alpha = 128
            canvas.drawRoundRect(RectF(0f, 0f, progressWidth, height.toFloat()), 10f, 10f, paint)
        }

        return bitmap
    }

    private fun createAdvancedBatteryVisualization(
        context: Context,
        batteryInfo: BatteryInfo,
    ): Bitmap {
        val width = 300
        val height = 150
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint =
            Paint().apply {
                isAntiAlias = true
                textSize = 24f
                textAlign = Paint.Align.CENTER
            }

        // Draw battery outline
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = ContextCompat.getColor(context, R.color.widget_outline)

        val batteryRect = RectF(20f, 40f, width - 40f, height - 40f)
        canvas.drawRoundRect(batteryRect, 8f, 8f, paint)

        // Battery terminal
        val terminalRect = RectF(width - 35f, height / 2f - 15f, width - 20f, height / 2f + 15f)
        canvas.drawRoundRect(terminalRect, 4f, 4f, paint)

        // Battery fill
        paint.style = Paint.Style.FILL
        paint.color = getBatteryColor(context, batteryInfo.level)

        val fillWidth = (batteryInfo.level / 100f) * (batteryRect.width() - 8f)
        val fillRect =
            RectF(
                batteryRect.left + 4f,
                batteryRect.top + 4f,
                batteryRect.left + 4f + fillWidth,
                batteryRect.bottom - 4f,
            )
        canvas.drawRoundRect(fillRect, 4f, 4f, paint)

        // Status indicators
        paint.color = ContextCompat.getColor(context, R.color.widget_text)
        paint.textSize = 18f

        if (batteryInfo.isCharging) {
            canvas.drawText("⚡", width / 2f, 30f, paint)
        }

        // Health indicator
        val healthColor = getHealthColor(context, batteryInfo.health)
        paint.color = healthColor
        paint.textSize = 14f
        canvas.drawText(getBatteryHealthText(batteryInfo.health), width / 2f, height - 10f, paint)

        return bitmap
    }

    private fun getBatteryInfo(context: Context): BatteryInfo {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = (level / scale.toFloat() * 100).roundToInt()

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: 0
        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: 0
        val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN

        // Calculate estimated time remaining (simplified calculation)
        val timeRemaining = calculateTimeRemaining(batteryPct, isCharging)

        return BatteryInfo(
            level = batteryPct,
            isCharging = isCharging,
            temperature = temperature / 10, // Convert from tenths of degrees
            voltage = voltage / 1000f, // Convert from mV to V
            health = health,
            timeRemaining = timeRemaining,
        )
    }

    private fun calculateTimeRemaining(
        level: Int,
        isCharging: Boolean,
    ): Long {
        // Improved calculation based on typical charging/discharging rates
        return if (isCharging) {
            val remainingCharge = 100 - level
            // More realistic: ~2 minutes per percent for modern fast charging
            // Slower when battery is fuller (80%+ takes longer)
            val minutesPerPercent =
                when {
                    level >= 90 -> 3.5f // Trickle charge phase
                    level >= 80 -> 2.5f // Slower charging
                    else -> 1.8f // Fast charging
                }
            (remainingCharge * minutesPerPercent * 60 * 1000L).toLong()
        } else {
            // Discharging: typical phones last 8-12 hours
            // Conservative estimate: 8 minutes per percent
            val minutesPerPercent = 8f
            (level * minutesPerPercent * 60 * 1000L).toLong()
        }
    }

    private fun getBatteryColor(
        context: Context,
        level: Int,
    ): Int {
        return when {
            level >= 80 -> ContextCompat.getColor(context, R.color.battery_excellent)
            level >= 60 -> ContextCompat.getColor(context, R.color.battery_good)
            level >= 40 -> ContextCompat.getColor(context, R.color.battery_fair)
            level >= 20 -> ContextCompat.getColor(context, R.color.battery_low)
            else -> ContextCompat.getColor(context, R.color.battery_critical)
        }
    }

    private fun getTemperatureColor(
        context: Context,
        temperature: Int,
    ): Int {
        return when {
            temperature >= 45 -> ContextCompat.getColor(context, R.color.temperature_critical)
            temperature >= 40 -> ContextCompat.getColor(context, R.color.temperature_high)
            temperature >= 35 -> ContextCompat.getColor(context, R.color.temperature_warm)
            else -> ContextCompat.getColor(context, R.color.temperature_normal)
        }
    }

    private fun getHealthColor(
        context: Context,
        health: Int,
    ): Int {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> ContextCompat.getColor(context, R.color.health_good)
            BatteryManager.BATTERY_HEALTH_OVERHEAT,
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE,
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE,
            ->
                ContextCompat.getColor(
                    context,
                    R.color.health_critical,
                )
            BatteryManager.BATTERY_HEALTH_DEAD -> ContextCompat.getColor(context, R.color.health_dead)
            BatteryManager.BATTERY_HEALTH_COLD -> ContextCompat.getColor(context, R.color.health_cold)
            else -> ContextCompat.getColor(context, R.color.health_unknown)
        }
    }

    private fun getBatteryStatusText(batteryInfo: BatteryInfo): String {
        return when {
            batteryInfo.isCharging && batteryInfo.level == 100 -> "Fully Charged"
            batteryInfo.isCharging -> "Charging"
            batteryInfo.level <= 15 -> "Critical"
            batteryInfo.level <= 30 -> "Low"
            else -> "Discharging"
        }
    }

    private fun getBatteryHealthText(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failed"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }

    private fun formatTimeRemaining(timeMs: Long): String {
        val hours = timeMs / (1000 * 60 * 60)
        val minutes = (timeMs % (1000 * 60 * 60)) / (1000 * 60)

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    private fun getWidgetSize(
        _context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ): WidgetSize {
        // Determine widget size using options provided by the launcher
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        // Heuristics: adjust thresholds as needed based on design
        return when {
            minWidth >= 260 || minHeight >= 160 -> WidgetSize.LARGE
            minWidth >= 180 || minHeight >= 110 -> WidgetSize.MEDIUM
            else -> WidgetSize.SMALL
        }
    }

    private fun getOptimizationStatus(_context: Context): String {
        // In a real implementation, check actual optimization status
        return "Status unavailable"
    }

    private fun handleBatterySaverToggle(_context: Context) {
        // Implement battery saver toggle logic
        // This would interact with your app's battery optimization features
    }

    private fun handleOptimizationToggle(_context: Context) {
        // Implement optimization toggle logic
        // This would trigger background optimization processes
    }

    private fun openMainApp(context: Context) {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        context.startActivity(intent)
    }

    private fun createPendingIntent(
        context: Context,
        action: String,
    ): PendingIntent {
        val intent =
            Intent(context, BatteryWidget::class.java).apply {
                this.action = action
            }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun refreshAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, BatteryWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private enum class WidgetSize {
        SMALL,
        MEDIUM,
        LARGE,
    }

    private data class BatteryInfo(
        val level: Int,
        val isCharging: Boolean,
        val temperature: Int,
        val voltage: Float,
        val health: Int,
        val timeRemaining: Long,
    )
}
