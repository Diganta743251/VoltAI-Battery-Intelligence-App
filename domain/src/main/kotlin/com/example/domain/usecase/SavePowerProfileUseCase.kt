package com.voltai.domain.usecase

import com.voltai.domain.model.PowerProfile
import com.voltai.domain.repository.PowerProfileRepository
class SavePowerProfileUseCase(
    private val powerProfileRepository: PowerProfileRepository
) {
    suspend operator fun invoke(profile: PowerProfile) {
        powerProfileRepository.savePowerProfile(profile)
    }
}
