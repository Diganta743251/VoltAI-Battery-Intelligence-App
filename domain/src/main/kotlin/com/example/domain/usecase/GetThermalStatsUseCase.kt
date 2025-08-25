package com.voltai.domain.usecase

import com.voltai.domain.model.ThermalStatus
import com.voltai.domain.repository.ThermalRepository
import kotlinx.coroutines.flow.Flow
class GetThermalStatsUseCase(
    private val thermalRepository: ThermalRepository
) {
    operator fun invoke(): Flow<ThermalStatus> {
        return thermalRepository.getThermalStatus()
    }
}
