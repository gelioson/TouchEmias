package ru.touchemiasapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.touchemiasapp.data.db.entity.WatchJobEntity

@Dao
interface WatchJobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: WatchJobEntity): Long

    @Update
    suspend fun update(job: WatchJobEntity)

    @Query("SELECT * FROM watch_jobs ORDER BY createdAt DESC LIMIT 1")
    fun observeLatest(): Flow<WatchJobEntity?>

    @Query("SELECT * FROM watch_jobs ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatest(): WatchJobEntity?

    @Query("SELECT * FROM watch_jobs WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): WatchJobEntity?

    @Query("UPDATE watch_jobs SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("UPDATE watch_jobs SET isActive = 0")
    suspend fun deactivateAll()

    @Query("DELETE FROM watch_jobs")
    suspend fun deleteAll()
}
