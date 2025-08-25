package com.voltai.domain.repository

import com.voltai.domain.model.ThermalStatus
import kotlinx.coroutines.flow.Flow

interface ThermalRepository {
    fun getThermalStatus(): Flow<ThermalStatus>
}
