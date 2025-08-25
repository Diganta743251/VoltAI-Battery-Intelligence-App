package com.voltai.data.repository

import com.voltai.data.preferences.DataStoreManager
import com.voltai.domain.model.PowerProfile
import com.voltai.domain.repository.PowerProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PowerProfileRepositoryImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : PowerProfileRepository {
    override fun getPowerProfile(): Flow<PowerProfile> {
        return dataStoreManager.powerProfile
    }

    override suspend fun savePowerProfile(profile: PowerProfile) {
        dataStoreManager.savePowerProfile(profile)
    }
}
