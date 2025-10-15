package com.voltai.core.utils

import com.voltai.domain.model.BatteryLog
import com.voltai.domain.repository.IBatteryMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Adapter to expose core BatteryMonitor as domain IBatteryMonitor by mapping to BatteryLog
 */
class IBatteryMonitorAdapter(private val batteryMonitor: BatteryMonitor) : IBatteryMonitor {
    override fun observeBatteryLogs(): Flow<BatteryLog> {
        return batteryMonitor.getBatteryStatusFlow().map { s ->
            BatteryLog(
                id = 0,
                timestamp = System.currentTimeMillis(),
                batteryLevel = s.level,
                voltage = s.voltage.toFloat() / 1000f,
                temperature = s.temperature,
                chargingStatus = if (s.isCharging) "Charging" else "Discharging",
                chargingType = s.chargingType.name,
                current = -(s.currentNow / 1000f)
            )
        }
    }
}