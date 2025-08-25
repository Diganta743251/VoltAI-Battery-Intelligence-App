package com.voltai.domain.usecase

import com.voltai.domain.model.BatteryStatus
import com.voltai.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetBatteryStatsUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository
) {
    operator fun invoke(): Flow<BatteryStatus?> {
        return batteryRepository.getCurrentBatteryStatus()
    }
}
