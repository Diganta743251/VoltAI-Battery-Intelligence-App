package com.voltai.data.di

import android.content.Context
import androidx.work.WorkManager
import com.voltai.data.repository.BatteryRepositoryImpl
import com.voltai.data.repository.PowerProfileRepositoryImpl
import com.voltai.data.repository.AppUsageRepositoryImpl
import com.voltai.data.repository.ThermalRepositoryImpl
import com.voltai.domain.repository.BatteryRepository
import com.voltai.domain.repository.PowerProfileRepository
import com.voltai.domain.repository.AppUsageRepository
import com.voltai.domain.repository.ThermalRepository
import com.voltai.domain.usecase.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindBatteryRepository(
        batteryRepositoryImpl: BatteryRepositoryImpl
    ): BatteryRepository

    @Binds
    @Singleton
    abstract fun bindPowerProfileRepository(
        powerProfileRepositoryImpl: PowerProfileRepositoryImpl
    ): PowerProfileRepository

    @Binds
    @Singleton
    abstract fun bindAppUsageRepository(
        appUsageRepositoryImpl: AppUsageRepositoryImpl
    ): AppUsageRepository

    @Binds
    @Singleton
    abstract fun bindThermalRepository(
        thermalRepositoryImpl: ThermalRepositoryImpl
    ): ThermalRepository

    companion object {
        @Provides
        @Singleton
        fun provideGetBatteryStatsUseCase(
            batteryRepository: BatteryRepository
        ): GetBatteryStatsUseCase {
            return GetBatteryStatsUseCase(batteryRepository)
        }

        @Provides
        @Singleton
        fun provideGetBatteryTipsUseCase(
            batteryRepository: BatteryRepository
        ): GetBatteryTipsUseCase {
            return GetBatteryTipsUseCase(batteryRepository)
        }

        @Provides
        @Singleton
        fun provideGetBatteryForecastUseCase(
            batteryRepository: BatteryRepository
        ): GetBatteryForecastUseCase {
            return GetBatteryForecastUseCase(batteryRepository)
        }

        @Provides
        @Singleton
        fun provideGetAppUsageUseCase(
            appUsageRepository: AppUsageRepository
        ): GetAppUsageUseCase {
            return GetAppUsageUseCase(appUsageRepository)
        }

        @Provides
        @Singleton
        fun provideGetThermalStatsUseCase(
            thermalRepository: ThermalRepository
        ): GetThermalStatsUseCase {
            return GetThermalStatsUseCase(thermalRepository)
        }

        @Provides
        @Singleton
        fun provideGetPowerProfileUseCase(
            powerProfileRepository: PowerProfileRepository
        ): GetPowerProfileUseCase {
            return GetPowerProfileUseCase(powerProfileRepository)
        }

        @Provides
        @Singleton
        fun provideSavePowerProfileUseCase(
            powerProfileRepository: PowerProfileRepository
        ): SavePowerProfileUseCase {
            return SavePowerProfileUseCase(powerProfileRepository)
        }

        @Provides
        @Singleton
        fun provideGetOptimalChargingSettingsUseCase(
            batteryRepository: BatteryRepository
        ): GetOptimalChargingSettingsUseCase {
            return GetOptimalChargingSettingsUseCase(batteryRepository)
        }

        @Provides
        @Singleton
        fun provideSaveOptimalChargingSettingsUseCase(
            batteryRepository: BatteryRepository
        ): SaveOptimalChargingSettingsUseCase {
            return SaveOptimalChargingSettingsUseCase(batteryRepository)
        }

        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
            return WorkManager.getInstance(context)
        }
    }
}
