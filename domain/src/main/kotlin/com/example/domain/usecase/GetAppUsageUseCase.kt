package com.voltai.domain.usecase

import com.voltai.domain.model.AppUsage
import com.voltai.domain.repository.AppUsageRepository
import kotlinx.coroutines.flow.Flow
class GetAppUsageUseCase(
    private val appUsageRepository: AppUsageRepository
) {
    operator fun invoke(): Flow<List<AppUsage>> {
        return appUsageRepository.getAppUsageStats()
    }
}
