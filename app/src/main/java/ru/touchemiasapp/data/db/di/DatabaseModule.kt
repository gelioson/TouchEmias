package ru.touchemiasapp.data.db.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.touchemiasapp.data.db.AppDatabase
import ru.touchemiasapp.data.db.dao.LogEntryDao
import ru.touchemiasapp.data.db.dao.WatchJobDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "touchemias.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideWatchJobDao(db: AppDatabase): WatchJobDao = db.watchJobDao()
    @Provides fun provideLogEntryDao(db: AppDatabase): LogEntryDao = db.logEntryDao()
}
