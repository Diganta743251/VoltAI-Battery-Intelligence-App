package com.voltai.data.di

import android.content.Context
import androidx.room.Room
import com.voltai.data.local.VoltAIDatabase
import com.voltai.data.local.dao.CurrentSampleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VoltAIDatabase =
        Room.databaseBuilder(context, VoltAIDatabase::class.java, "voltai.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCurrentSampleDao(db: VoltAIDatabase): CurrentSampleDao = db.currentSampleDao()
}