package com.voltai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_logs")
data class BatteryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val batteryPercentage: Int,
    val voltage: Int,
    val temperature: Int,
    val status: Int,
    val chargePlug: Int,
    val currentNow: Int, // Charging current in microamperes (µA)
    val health: String, // Battery health status
    val isChargingStart: Boolean = false,
    val isChargingEnd: Boolean = false,
    val cycleId: Long = 0L
)
