package com.voltai.core.di

import android.content.Context
import com.voltai.core.utils.BatteryMonitor
import com.voltai.core.utils.IBatteryMonitorAdapter
import com.voltai.domain.repository.IBatteryMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    
    @Provides
    @Singleton
    fun provideBatteryMonitor(
        @ApplicationContext context: Context
    ): BatteryMonitor {
        return BatteryMonitor(context)
    }

    @Provides
    @Singleton
    fun provideIBatteryMonitor(
        batteryMonitor: BatteryMonitor
    ): IBatteryMonitor = IBatteryMonitorAdapter(batteryMonitor)
}