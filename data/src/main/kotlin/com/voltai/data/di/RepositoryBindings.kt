package com.voltai.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Legacy alias removed to avoid duplicate Hilt bindings for AppUsageRepository.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings