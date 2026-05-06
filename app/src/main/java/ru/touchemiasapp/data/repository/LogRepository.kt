package ru.touchemiasapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.touchemiasapp.data.db.dao.LogEntryDao
import ru.touchemiasapp.data.db.entity.LogEntryEntity
import ru.touchemiasapp.domain.model.LogEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val dao: LogEntryDao
) {
    fun observeAll(): Flow<List<LogEntry>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun add(entry: LogEntry) = dao.insert(LogEntryEntity.from(entry))

    suspend fun getAll(): List<LogEntry> = dao.getRecent(limit = 2000).map { it.toDomain() }

    suspend fun clear() = dao.deleteAll()
}
