package ru.touchemiasapp.domain.model

data class TimeSlot(
    val date: String,           // yyyy-MM-dd
    val startTime: String,      // HH:mm
    val endTime: String,        // HH:mm
    val complexResourceId: Long,
    val receptionTypeId: String,
    val availableResourceId: Long
)
