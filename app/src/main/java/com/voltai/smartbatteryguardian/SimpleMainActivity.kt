package com.voltai.smartbatteryguardian

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SimpleMainActivity : ComponentActivity() {
    
    private var batteryReceiver: BroadcastReceiver? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            com.voltai.ui.theme.VoltAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SimpleBatteryScreen()
                }
            }
        }
    }
    
    @Composable
    fun SimpleBatteryScreen() {
        var batteryLevel by remember { mutableStateOf(0) }
        var isCharging by remember { mutableStateOf(false) }
        var voltage by remember { mutableStateOf(0f) }
        var temperature by remember { mutableStateOf(0f) }
        
        DisposableEffect(Unit) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.let {
                        batteryLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                        val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                        batteryLevel = (batteryLevel * 100) / scale
                        
                        val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                   status == BatteryManager.BATTERY_STATUS_FULL
                        
                        voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
                        temperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                    }
                }
            }
            
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(receiver, filter)
            batteryReceiver = receiver
            
            onDispose {
                batteryReceiver?.let { unregisterReceiver(it) }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "VoltAI Battery Guardian",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$batteryLevel%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            batteryLevel > 50 -> MaterialTheme.colorScheme.primary
                            batteryLevel > 20 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    
                    Text(
                        text = if (isCharging) "Charging" else "Not Charging",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Voltage",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.2f", voltage)}V",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Temperature",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.1f", temperature)}°C",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Real-time battery monitoring",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


