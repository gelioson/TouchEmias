package ru.touchemiasapp.domain.model

data class LogEntry(
    val id: Long = 0,
    val timestamp: Long,
    val doctorId: Long,
    val doctorName: String,
    val slotsFound: Int,
    val bookedSlot: String?,
    val errorMessage: String?,
    val rawResponse: String
)
