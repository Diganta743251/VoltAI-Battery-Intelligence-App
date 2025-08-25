package com.voltai.domain.usecase

import com.voltai.domain.model.BatteryLog
import com.voltai.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetBatteryForecastUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository
) {
    operator fun invoke(): Flow<String> {
        return batteryRepository.getAllBatteryLogs().map {
            if (it.size < 2) {
                "Not enough data for forecast"
            } else {
                // Simple linear regression for demonstration
                val sortedLogs = it.sortedBy { log -> log.timestamp }
                val x = sortedLogs.map { log -> log.timestamp.toDouble() }
                val y = sortedLogs.map { log -> log.batteryLevel.toDouble() }

                val n = x.size
                var sumX = 0.0
                var sumY = 0.0
                var sumXY = 0.0
                var sumX2 = 0.0

                for (i in 0 until n) {
                    sumX += x[i]
                    sumY += y[i]
                    sumXY += x[i] * y[i]
                    sumX2 += x[i] * x[i]
                }

                val denominator = (n * sumX2 - sumX * sumX)
                if (denominator == 0.0) {
                    return@map "Cannot calculate forecast (no variance in data)"
                }

                val b = (n * sumXY - sumX * sumY) / denominator
                val a = (sumY - b * sumX) / n

                // Estimate time to 0% battery
                // batteryPercentage = a + b * timestamp
                // 0 = a + b * timestamp
                // timestamp = -a / b
                if (b >= 0) {
                    "Battery is charging or stable, no discharge forecast"
                } else {
                    val forecastTimestamp = -a / b
                    val currentTime = System.currentTimeMillis().toDouble()
                    val timeLeftMillis = forecastTimestamp - currentTime

                    if (timeLeftMillis > 0) {
                        val hours = (timeLeftMillis / (1000 * 60 * 60)).toLong()
                        val minutes = ((timeLeftMillis % (1000 * 60 * 60)) / (1000 * 60)).toLong()
                        "Estimated ${hours}h ${minutes}m remaining"
                    } else {
                        "Battery is already low or discharged"
                    }
                }
            }
        }
    }
}
