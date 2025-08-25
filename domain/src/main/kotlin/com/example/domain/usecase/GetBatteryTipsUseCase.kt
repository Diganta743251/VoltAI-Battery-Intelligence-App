package com.voltai.domain.usecase

import com.voltai.domain.model.BatteryLog
import com.voltai.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetBatteryTipsUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository
) {
    operator fun invoke(): Flow<List<String>> {
        return batteryRepository.getAllBatteryLogs().map { logs ->
            generateBatteryTips(logs)
        }
    }
    
    private fun generateBatteryTips(logs: List<BatteryLog>): List<String> {
        val tips = mutableListOf<String>()
        
        if (logs.isNotEmpty()) {
            val latestLog = logs.first()
            
            if (latestLog.temperature > 35) {
                tips.add("Your device is running hot. Consider reducing usage or removing the case.")
            }
            
            if (latestLog.batteryLevel < 20) {
                tips.add("Battery level is low. Consider charging your device.")
            }
            
            if (latestLog.batteryLevel > 80 && latestLog.chargingStatus == "Charging") {
                tips.add("Consider unplugging to preserve battery health.")
            }
        }
        
        if (tips.isEmpty()) {
            tips.add("Your battery is performing well!")
        }
        
        return tips
    }
}
