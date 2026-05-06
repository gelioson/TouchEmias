package ru.touchemiasapp.data.api.model.response

import com.google.gson.annotations.SerializedName
import ru.touchemiasapp.domain.model.Doctor

// TODO: verify field names against real API traffic
data class DoctorDto(
    @SerializedName("availableResourceId") val availableResourceId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("specialityName") val specialityName: String? = null,
    @SerializedName("clinicId") val clinicId: Long,
    @SerializedName("clinicShortName") val clinicShortName: String,
    @SerializedName("ariaNumber") val ariaNumber: String? = null,
    @SerializedName("nearestDate") val nearestDate: String? = null
) {
    fun toDomain() = Doctor(
        availableResourceId = availableResourceId,
        name = name,
        specialityName = specialityName ?: "",
        clinicId = clinicId,
        clinicName = clinicShortName,
        ariaNumber = ariaNumber ?: "",
        nearestDate = nearestDate
    )
}
