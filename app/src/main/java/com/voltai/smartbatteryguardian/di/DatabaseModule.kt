package com.voltai.smartbatteryguardian.di

import android.content.Context
import androidx.room.Room
import com.voltai.smartbatteryguardian.BatteryDataProvider
import com.voltai.smartbatteryguardian.BatteryDataProviderImpl
import com.voltai.smartbatteryguardian.data.database.BatteryRepository
import com.voltai.smartbatteryguardian.data.database.BatteryRepositoryImpl
import com.voltai.smartbatteryguardian.data.database.VoltAIDatabase
import com.voltai.smartbatteryguardian.data.database.dao.BatteryDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database and repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VoltAIDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            VoltAIDatabase::class.java,
            "voltai_database"
        )
            .addMigrations(VoltAIDatabase.MIGRATION_1_2, VoltAIDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration() // For development only
            .build()
    }

    @Provides
    @Singleton
    fun provideBatteryDao(database: VoltAIDatabase): BatteryDao {
        return database.batteryDao()
    }

    @Provides
    @Singleton
    fun provideBatteryRepository(dao: BatteryDao): BatteryRepository {
        return BatteryRepositoryImpl(dao)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindBatteryDataProvider(
        batteryDataProviderImpl: BatteryDataProviderImpl
    ): BatteryDataProvider
}
