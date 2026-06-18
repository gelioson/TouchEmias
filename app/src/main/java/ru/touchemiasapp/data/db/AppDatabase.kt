package ru.touchemiasapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.touchemiasapp.data.db.dao.LogEntryDao
import ru.touchemiasapp.data.db.dao.WatchJobDao
import ru.touchemiasapp.data.db.entity.LogEntryEntity
import ru.touchemiasapp.data.db.entity.WatchJobConverters
import ru.touchemiasapp.data.db.entity.WatchJobEntity

@Database(
    entities = [WatchJobEntity::class, LogEntryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(WatchJobConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchJobDao(): WatchJobDao
    abstract fun logEntryDao(): LogEntryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE watch_jobs ADD COLUMN complexResourceIds TEXT NOT NULL DEFAULT '[]'")
            }
        }
    }
}
