package ru.touchemiasapp.domain.model

enum class MonitorMode { NOTIFY_ONLY, AUTO_BOOK }

data class WatchConfig(
    val id: Long = 0,
    val specialityId: Long,
    val specialityName: String,
    val doctors: List<Doctor>,
    val selectedDates: List<String>,    // yyyy-MM-dd
    val timeFrom: String,               // HH:mm
    val timeTo: String,                 // HH:mm
    val mode: MonitorMode,
    val intervalSeconds: Int,
    val isActive: Boolean = false
)

val POLL_INTERVALS = listOf(30, 60, 300, 600, 1800, 3600)
