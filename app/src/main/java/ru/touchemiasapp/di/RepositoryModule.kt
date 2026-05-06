package ru.touchemiasapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.touchemiasapp.data.repository.EmiasRepositoryImpl
import ru.touchemiasapp.domain.repository.EmiasRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEmiasRepository(impl: EmiasRepositoryImpl): EmiasRepository
}
