package com.voltai.domain.usecase

import com.voltai.domain.model.PowerProfile
import com.voltai.domain.repository.PowerProfileRepository
import kotlinx.coroutines.flow.Flow
class GetPowerProfileUseCase(
    private val powerProfileRepository: PowerProfileRepository
) {
    operator fun invoke(): Flow<PowerProfile> {
        return powerProfileRepository.getPowerProfile()
    }
}
