package com.voltai.domain.usecase

import com.voltai.domain.repository.BatteryRepository
class SaveOptimalChargingSettingsUseCase(
    private val batteryRepository: BatteryRepository
) {
    suspend operator fun invoke(enabled: Boolean, percentage: Int) {
        batteryRepository.saveOptimalChargingSettings(enabled, percentage)
    }
}
