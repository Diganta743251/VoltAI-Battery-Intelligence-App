package com.voltai.smartbatteryguardian.di

import android.content.Context
import com.voltai.smartbatteryguardian.billing.GooglePlayBillingManager
import com.voltai.smartbatteryguardian.premium.PremiumManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for premium features and billing
 */
@Module
@InstallIn(SingletonComponent::class)
object PremiumModule {
    @Provides
    @Singleton
    fun provideGooglePlayBillingManager(
        @ApplicationContext context: Context,
    ): GooglePlayBillingManager {
        return GooglePlayBillingManager(context)
    }

    @Provides
    @Singleton
    fun providePremiumManager(
        @ApplicationContext context: Context,
    ): PremiumManager {
        return PremiumManager(context)
    }
}
