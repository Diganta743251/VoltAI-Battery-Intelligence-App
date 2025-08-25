package com.voltai.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voltai.ui.components.*

@Preview(showBackground = true)
@Composable
fun TestBatteryGuruComponents() {
    MaterialTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BatteryInfoCard(
                    batteryPercentage = 29,
                    status = "Discharging",
                    remainingTime = "3h 30m left"
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElectricCurrentCard(
                        currentValue = 743,
                        voltage = 3.711f,
                        modifier = Modifier.weight(1f)
                    )
                    
                    OngoingCard(
                        screenOnTime = "1h 27m 3s",
                        screenOffTime = "7h 10m 37s",
                        batteryUsed = "70% • 1874 mAh",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BatteryStatusCard(
                        title = "Battery temperat...",
                        value = "34.9°C",
                        description = "↓ 34.0°C • ↑ 36.5°C",
                        modifier = Modifier.weight(1f)
                    )
                    
                    BatteryHealthCard(
                        healthPercentage = 60,
                        capacity = "2645/4400 mAh",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                StatisticsCard(
                    averageScreenOn = "~19.2%/h",
                    averageScreenOff = "~5.7%/h",
                    fullChargeScreenOn = "7h 36m 21s",
                    fullChargeScreenOff = "1d 59m 53s"
                )
            }
            
            item {
                HistoryCard(
                    chargingSessions = 12,
                    chargedAmount = "457% • 12522 mAh",
                    lastDays = 10
                )
            }
            
            item {
                ToolsCard(
                    onBluetoothClick = { /* Navigate to Bluetooth */ },
                    onWakelocksClick = { /* Navigate to Wakelocks */ },
                    onIdleLogClick = { /* Navigate to Idle log */ },
                    onDeviceLogClick = { /* Navigate to Device log */ },
                    onBatterySaverClick = { /* Navigate to Battery saver */ },
                    onOverlaysClick = { /* Navigate to Overlays */ }
                )
            }
        }
    }
}