package com.voltai.domain.repository

import com.voltai.domain.model.PowerProfile
import kotlinx.coroutines.flow.Flow

interface PowerProfileRepository {
    fun getPowerProfile(): Flow<PowerProfile>
    suspend fun savePowerProfile(profile: PowerProfile)
}
