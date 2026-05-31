package com.tamersarioglu.openapitest.di

import com.tamersarioglu.openapitest.data.repository.OpenRouterRepositoryImpl
import com.tamersarioglu.openapitest.domain.repository.OpenRouterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindOpenRouterRepository(
        impl: OpenRouterRepositoryImpl,
    ): OpenRouterRepository
}
