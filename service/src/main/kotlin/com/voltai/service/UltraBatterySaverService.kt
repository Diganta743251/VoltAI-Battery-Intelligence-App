package com.voltai.service

import android.accessibilityservice.AccessibilityService
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.ServiceInfo
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.lang.reflect.Method
import javax.inject.Inject

class UltraBatterySaverService : Service() {
    
    companion object {
        private const val TAG = "UltraBatterySaver"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "ultra_battery_saver"
        private const val CHANNEL_NAME = "Ultra Battery Saver"
        private const val PREF_NAME = "ultra_battery_saver_prefs"
        private const val KEY_WHITELIST = "app_whitelist"
        private const val KEY_ORIGINAL_BRIGHTNESS = "original_brightness"
        private const val KEY_ORIGINAL_TIMEOUT = "original_timeout"
        private const val KEY_IS_ACTIVE = "is_active"
        
        const val ACTION_TOGGLE = "com.voltai.ACTION_TOGGLE_ULTRA_SAVER"
        const val ACTION_STOP = "com.voltai.ACTION_STOP_ULTRA_SAVER"
        const val ACTION_WHITELIST_APP = "com.voltai.ACTION_WHITELIST_APP"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }
    
    private lateinit var powerManager: PowerManager
    private lateinit var wifiManager: WifiManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var locationManager: LocationManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var sharedPreferences: SharedPreferences
    
    private var originalBrightness: Int = 128
    private var originalTimeout: Int = 60000
    private var isActive = false
    private val whitelistedApps = mutableSetOf<String>()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        initializeManagers()
        loadSettings()
        createNotificationChannel()
    }
    
    private fun initializeManagers() {
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth not available", e)
        }
    }
    
    private fun loadSettings() {
        isActive = sharedPreferences.getBoolean(KEY_IS_ACTIVE, false)
        originalBrightness = sharedPreferences.getInt(KEY_ORIGINAL_BRIGHTNESS, 128)
        originalTimeout = sharedPreferences.getInt(KEY_ORIGINAL_TIMEOUT, 60000)
        whitelistedApps.addAll(
            sharedPreferences.getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()
        )
    }
    
    private fun saveSettings() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_ACTIVE, isActive)
            putInt(KEY_ORIGINAL_BRIGHTNESS, originalBrightness)
            putInt(KEY_ORIGINAL_TIMEOUT, originalTimeout)
            putStringSet(KEY_WHITELIST, whitelistedApps)
            apply()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> toggleUltraBatterySaver()
            ACTION_STOP -> stopUltraBatterySaver()
            ACTION_WHITELIST_APP -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                if (packageName != null) {
                    whitelistedApps.add(packageName)
                    saveSettings()
                }
            }
            else -> {
                if (!isActive) {
                    startUltraBatterySaver()
                }
            }
        }
        
        return START_STICKY
    }
    
    private fun startUltraBatterySaver() {
        Log.d(TAG, "Starting Ultra Battery Saver")
        isActive = true
        saveSettings()
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Save original settings
        saveOriginalSettings()
        
        // Apply extreme power-saving policies
        serviceScope.launch {
            applyPowerSavingPolicies()
        }
        
        // Monitor battery and adjust policies
        startBatteryMonitoring()
    }
    
    private fun toggleUltraBatterySaver() {
        if (isActive) {
            stopUltraBatterySaver()
        } else {
            startUltraBatterySaver()
        }
    }
    
    private fun stopUltraBatterySaver() {
        Log.d(TAG, "Stopping Ultra Battery Saver")
        isActive = false
        saveSettings()
        
        // Restore original settings
        restoreOriginalSettings()
        
        // Cancel all jobs
        serviceScope.cancel()
        
        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun saveOriginalSettings() {
        try {
            // Save screen brightness
            originalBrightness = Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            )
            
            // Save screen timeout
            originalTimeout = Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                60000
            )
            
            saveSettings()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save original settings", e)
        }
    }
    
    private suspend fun applyPowerSavingPolicies() = withContext(Dispatchers.IO) {
        try {
            // 1. Reduce screen brightness to minimum
            setScreenBrightness(10) // Very dim
            
            // 2. Set aggressive screen timeout
            setScreenTimeout(15000) // 15 seconds
            
            // 3. Disable radios
            disableWifi()
            disableBluetooth()
            disableLocation()
            disableMobileData()
            
            // 4. Enable power saving mode
            enablePowerSavingMode()
            
            // 5. Force dark mode
            forceDarkMode()
            
            // 6. Disable vibration
            disableVibration()
            
            // 7. Limit background processes
            limitBackgroundProcesses()
            
            // 8. Disable animations
            disableAnimations()
            
            // 9. Schedule periodic sync off
            disableAutoSync()
            
            // 10. Lower CPU governor (requires root)
            attemptLowerCPUGovernor()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply power saving policies", e)
        }
    }
    
    private fun setScreenBrightness(brightness: Int) {
        try {
            if (Settings.System.canWrite(this)) {
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness.coerceIn(0, 255)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set screen brightness", e)
        }
    }
    
    private fun setScreenTimeout(timeout: Int) {
        try {
            if (Settings.System.canWrite(this)) {
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    timeout
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set screen timeout", e)
        }
    }
    
    private fun disableWifi() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = false
            } else {
                // For Android Q and above, prompt user to disable
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
                panelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(panelIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable WiFi", e)
        }
    }
    
    private fun disableBluetooth() {
        try {
            if (::bluetoothAdapter.isInitialized) {
                bluetoothAdapter.disable()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable Bluetooth", e)
        }
    }
    
    private fun disableLocation() {
        // Prompt user to disable location
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
    
    private fun disableMobileData() {
        try {
            // This requires MODIFY_PHONE_STATE permission which is system-only
            // We can only prompt the user
            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable mobile data", e)
        }
    }
    
    private fun enablePowerSavingMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable power saving mode", e)
        }
    }
    
    private fun forceDarkMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Force dark mode for all apps
                Settings.Secure.putString(
                    contentResolver,
                    "ui_night_mode",
                    "2" // Always dark
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to force dark mode", e)
        }
    }
    
    private fun disableVibration() {
        try {
            if (Settings.System.canWrite(this)) {
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.HAPTIC_FEEDBACK_ENABLED,
                    0
                )
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.VIBRATE_ON,
                    0
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable vibration", e)
        }
    }
    
    private fun limitBackgroundProcesses() {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            
            // Kill background processes (except whitelisted)
            val runningApps = activityManager.runningAppProcesses
            runningApps?.forEach { processInfo ->
                if (processInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE &&
                    !whitelistedApps.contains(processInfo.processName)) {
                    activityManager.killBackgroundProcesses(processInfo.processName)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to limit background processes", e)
        }
    }
    
    private fun disableAnimations() {
        try {
            if (Settings.System.canWrite(this)) {
                Settings.Global.putFloat(contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 0f)
                Settings.Global.putFloat(contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 0f)
                Settings.Global.putFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable animations", e)
        }
    }
    
    private fun disableAutoSync() {
        try {
            ContentResolver.setMasterSyncAutomatically(false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable auto sync", e)
        }
    }
    
    private fun attemptLowerCPUGovernor() {
        // This requires root access
        // We'll just log it for now
        Log.d(TAG, "CPU governor adjustment requires root access")
    }
    
    private fun restoreOriginalSettings() {
        try {
            if (Settings.System.canWrite(this)) {
                // Restore brightness
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    originalBrightness
                )
                
                // Restore timeout
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    originalTimeout
                )
                
                // Re-enable animations
                Settings.Global.putFloat(contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
                Settings.Global.putFloat(contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
                Settings.Global.putFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
                
                // Re-enable vibration
                Settings.System.putInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1)
                Settings.System.putInt(contentResolver, Settings.System.VIBRATE_ON, 1)
                
                // Re-enable auto sync
                ContentResolver.setMasterSyncAutomatically(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore original settings", e)
        }
    }
    
    private fun startBatteryMonitoring() {
        serviceScope.launch {
            while (isActive) {
                delay(60000) // Check every minute
                
                val batteryLevel = getBatteryLevel()
                
                // Adjust policies based on battery level
                when {
                    batteryLevel < 5 -> {
                        // Critical level - maximum power saving
                        setScreenBrightness(1)
                        setScreenTimeout(5000)
                    }
                    batteryLevel < 15 -> {
                        // Very low - aggressive power saving
                        setScreenBrightness(5)
                        setScreenTimeout(10000)
                    }
                    batteryLevel < 30 -> {
                        // Low - moderate power saving
                        setScreenBrightness(10)
                        setScreenTimeout(15000)
                    }
                }
                
                // Kill background processes periodically
                if (batteryLevel < 20) {
                    limitBackgroundProcesses()
                }
            }
        }
    }
    
    private fun getBatteryLevel(): Int {
        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            registerReceiver(null, filter)
        }
        
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        
        return (level * 100 / scale)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ultra Battery Saver Service"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val stopIntent = Intent(this, UltraBatterySaverService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ultra Battery Saver Active")
            .setContentText("Extreme power saving mode is enabled")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }
        
        return builder.build()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (isActive) {
            restoreOriginalSettings()
        }
    }
}
