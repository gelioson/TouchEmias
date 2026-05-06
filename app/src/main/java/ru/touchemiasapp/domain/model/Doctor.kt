package ru.touchemiasapp.domain.model

data class Doctor(
    val availableResourceId: Long,
    val name: String,
    val specialityName: String,
    val clinicId: Long,
    val clinicName: String,
    val ariaNumber: String = "",
    val nearestDate: String? = null
)
