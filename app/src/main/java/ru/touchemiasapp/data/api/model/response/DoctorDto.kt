package ru.touchemiasapp.data.api.model.response

import com.google.gson.annotations.SerializedName
import ru.touchemiasapp.domain.model.Doctor

// payload → DoctorsPayloadDto
data class DoctorsPayloadDto(
    @SerializedName("doctorsInfo") val doctorsInfo: List<LpuGroupDto>? = null
)

data class LpuGroupDto(
    @SerializedName("lpuId") val lpuId: Long? = null,
    @SerializedName("lpuShortName") val lpuShortName: String? = null,
    @SerializedName("availableResources") val availableResources: List<AvailableResourceDto>? = null
)

data class AvailableResourceDto(
    @SerializedName("id") val id: Long,
    @SerializedName("lpuId") val lpuId: Long? = null,
    @SerializedName("arSpecialityName") val specialityName: String? = null,
    @SerializedName("mainDoctor") val mainDoctor: MainDoctorDto? = null,
    @SerializedName("complexResource") val complexResource: List<ComplexResourceDto>? = null
) {
    fun toDomain(lpu: LpuGroupDto) = Doctor(
        availableResourceId = id,
        complexResourceId = complexResource?.firstOrNull()?.id ?: id,
        name = mainDoctor?.fullName ?: "",
        specialityName = specialityName ?: "",
        clinicId = lpu.lpuId ?: 0L,
        clinicName = lpu.lpuShortName ?: ""
    )
}

data class MainDoctorDto(
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("secondName") val secondName: String? = null
) {
    val fullName: String get() = listOfNotNull(lastName, firstName, secondName)
        .filter { it.isNotBlank() }.joinToString(" ")
}

data class ComplexResourceDto(
    @SerializedName("id") val id: Long
)
