package com.voltai.smartbatteryguardian

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class BatteryInfoVm(
    val level: Int = 0,
    val status: String = "Unknown",
    val temperature: String = "0°C",
    val voltage: String = "0V",
    val health: String = "Unknown",
    val isCharging: Boolean = false
)

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _batteryInfo = MutableLiveData(BatteryInfoVm())
    val batteryInfo: LiveData<BatteryInfoVm> = _batteryInfo
    
    private val _screenOnTime = MutableLiveData("0h 0m")
    val screenOnTime: LiveData<String> = _screenOnTime
    
    private val _screenOffTime = MutableLiveData("0h 0m")
    val screenOffTime: LiveData<String> = _screenOffTime
    
    private val _batteryUsed = MutableLiveData("0%")
    val batteryUsed: LiveData<String> = _batteryUsed
    
    private var batteryReceiver: BroadcastReceiver? = null
    
    init {
        registerBatteryReceiver()
        // Simulate some initial data
        updateMockData()
    }
    
    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { handleBatteryChanged(it) }
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        getApplication<Application>().registerReceiver(batteryReceiver, filter)
    }
    
    private fun handleBatteryChanged(intent: Intent) {
        viewModelScope.launch {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPercentage = if (level != -1 && scale != -1) {
                (level * 100) / scale
            } else {
                0
            }
            
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            
            val batteryInfo = BatteryInfoVm(
                level = batteryPercentage,
                status = getStatusString(status),
                temperature = "${temperature / 10}°C",
                voltage = "${voltage / 1000.0}V",
                health = getHealthString(health),
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
            )
            
            _batteryInfo.value = batteryInfo
        }
    }
    
    private fun updateMockData() {
        viewModelScope.launch {
            // Simulate screen time data
            _screenOnTime.value = "4h 32m"
            _screenOffTime.value = "12h 15m"
            _batteryUsed.value = "45%"
        }
    }
    
    private fun getStatusString(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            else -> "Unknown"
        }
    }
    
    private fun getHealthString(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        batteryReceiver?.let { 
            try {
                getApplication<Application>().unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered
            }
        }
    }
}
