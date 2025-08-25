package com.voltai.domain.repository

import com.voltai.domain.model.AppUsage
import kotlinx.coroutines.flow.Flow

interface AppUsageRepository {
    fun getAppUsageStats(): Flow<List<AppUsage>>
}
