package com.voltai.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class BatteryBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val batteryStatus: Intent? = intent
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct: Float = level / scale.toFloat() * 100

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                  status == BatteryManager.BATTERY_STATUS_FULL

        val voltage: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val temperature: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1

        // Create an intent to send the battery data to the local broadcast
        val batteryDataIntent = Intent("BatteryData")
        batteryDataIntent.putExtra("batteryPct", batteryPct)
        batteryDataIntent.putExtra("isCharging", isCharging)
        batteryDataIntent.putExtra("voltage", voltage)
        batteryDataIntent.putExtra("temperature", temperature)

        // Send the local broadcast
        LocalBroadcastManager.getInstance(context).sendBroadcast(batteryDataIntent)
    }

    companion object {
        fun registerReceiver(context: Context) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(BatteryBroadcastReceiver(), filter)
        }
    }
}
