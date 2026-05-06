package ru.touchemiasapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.touchemiasapp.domain.model.LogEntry

@Entity(tableName = "log_entries")
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val timestamp: Long,
    @ColumnInfo val doctorId: Long,
    @ColumnInfo val doctorName: String,
    @ColumnInfo val slotsFound: Int,
    @ColumnInfo val bookedSlot: String?,
    @ColumnInfo val errorMessage: String?,
    @ColumnInfo val rawResponse: String
) {
    fun toDomain() = LogEntry(
        id = id,
        timestamp = timestamp,
        doctorId = doctorId,
        doctorName = doctorName,
        slotsFound = slotsFound,
        bookedSlot = bookedSlot,
        errorMessage = errorMessage,
        rawResponse = rawResponse
    )

    companion object {
        fun from(entry: LogEntry) = LogEntryEntity(
            id = entry.id,
            timestamp = entry.timestamp,
            doctorId = entry.doctorId,
            doctorName = entry.doctorName,
            slotsFound = entry.slotsFound,
            bookedSlot = entry.bookedSlot,
            errorMessage = entry.errorMessage,
            rawResponse = entry.rawResponse
        )
    }
}
