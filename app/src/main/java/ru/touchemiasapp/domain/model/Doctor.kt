package ru.touchemiasapp.domain.model

data class Doctor(
    val availableResourceId: Long,
    val complexResourceId: Long = availableResourceId,
    val name: String,
    val specialityName: String,
    val clinicId: Long,
    val clinicName: String,
    val ariaNumber: String = "",
    val nearestDate: String? = null
)
