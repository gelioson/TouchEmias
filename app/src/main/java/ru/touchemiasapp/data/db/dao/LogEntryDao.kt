package ru.touchemiasapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.touchemiasapp.data.db.entity.LogEntryEntity

@Dao
interface LogEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LogEntryEntity)

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 500): List<LogEntryEntity>

    @Query("DELETE FROM log_entries")
    suspend fun deleteAll()

    @Query("DELETE FROM log_entries WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
