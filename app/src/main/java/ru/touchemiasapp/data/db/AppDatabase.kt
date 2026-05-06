package ru.touchemiasapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.touchemiasapp.data.db.dao.LogEntryDao
import ru.touchemiasapp.data.db.dao.WatchJobDao
import ru.touchemiasapp.data.db.entity.LogEntryEntity
import ru.touchemiasapp.data.db.entity.WatchJobConverters
import ru.touchemiasapp.data.db.entity.WatchJobEntity

@Database(
    entities = [WatchJobEntity::class, LogEntryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(WatchJobConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchJobDao(): WatchJobDao
    abstract fun logEntryDao(): LogEntryDao
}
