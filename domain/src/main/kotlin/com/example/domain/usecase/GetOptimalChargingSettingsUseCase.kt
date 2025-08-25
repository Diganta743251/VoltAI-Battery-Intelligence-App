package com.voltai.domain.usecase

import com.voltai.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
class GetOptimalChargingSettingsUseCase(
    private val batteryRepository: BatteryRepository
) {
    operator fun invoke(): Flow<Pair<Boolean, Int>> {
        return batteryRepository.getOptimalChargingSettings()
    }
}
