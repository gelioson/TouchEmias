package ru.touchemiasapp.data.api.model.response

import com.google.gson.annotations.SerializedName
import ru.touchemiasapp.domain.model.Speciality

data class SpecialityDto(
    @SerializedName("code") val code: String,
    @SerializedName("title") val title: String,
    @SerializedName("parentCode") val parentCode: String? = null,
    @SerializedName("parentTitle") val parentTitle: String? = null,
    @SerializedName("specialities") val specialities: List<SpecialitySubDto>? = null
) {
    fun toDomain() = Speciality(
        id = specialities?.firstOrNull()?.specialityCode?.toLongOrNull() ?: 0L,
        name = title,
        code = code
    )
}

data class SpecialitySubDto(
    @SerializedName("specialityCode") val specialityCode: String? = null,
    @SerializedName("isMultipleLpuSpeciality") val isMultipleLpuSpeciality: Boolean = false
)
