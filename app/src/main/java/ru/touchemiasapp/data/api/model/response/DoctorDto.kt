package ru.touchemiasapp.data.api.model.response

import com.google.gson.annotations.SerializedName
import ru.touchemiasapp.domain.model.Doctor

data class DoctorDto(
    @SerializedName("availableResourceId") val availableResourceId: Long,
    @SerializedName("complexResourceId") val complexResourceId: Long? = null,
    @SerializedName("name") val name: String,
    @SerializedName("specialityName") val specialityName: String? = null,
    @SerializedName("clinicId") val clinicId: Long? = null,
    @SerializedName("clinicShortName") val clinicShortName: String? = null,
    @SerializedName("ariaNumber") val ariaNumber: String? = null,
    @SerializedName("nearestDate") val nearestDate: String? = null
) {
    fun toDomain() = Doctor(
        availableResourceId = availableResourceId,
        complexResourceId = complexResourceId ?: availableResourceId,
        name = name,
        specialityName = specialityName ?: "",
        clinicId = clinicId ?: 0L,
        clinicName = clinicShortName ?: "",
        ariaNumber = ariaNumber ?: "",
        nearestDate = nearestDate
    )
}
