package com.voltai.smartbatteryguardian.di

import android.content.Context
import androidx.room.Room
import com.voltai.smartbatteryguardian.database.BatteryAlertDao
import com.voltai.smartbatteryguardian.database.BatteryDatabase
import com.voltai.smartbatteryguardian.database.BatteryLogDao
import com.voltai.smartbatteryguardian.database.ChargingSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppRoomModule {
    @Provides
    @Singleton
    fun provideBatteryDatabase(
        @ApplicationContext context: Context,
    ): BatteryDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            BatteryDatabase::class.java,
            BatteryDatabase.DATABASE_NAME,
        ).addMigrations(
            com.voltai.smartbatteryguardian.database.MIGRATION_1_2,
            com.voltai.smartbatteryguardian.database.MIGRATION_2_3,
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBatteryLogDao(db: BatteryDatabase): BatteryLogDao = db.batteryLogDao()

    @Provides
    @Singleton
    fun provideChargingSessionDao(db: BatteryDatabase): ChargingSessionDao = db.chargingSessionDao()

    @Provides
    @Singleton
    fun provideBatteryAlertDao(db: BatteryDatabase): BatteryAlertDao = db.batteryAlertDao()
}
