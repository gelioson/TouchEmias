package ru.touchemiasapp.data.api.model.response

import com.google.gson.annotations.SerializedName
import ru.touchemiasapp.domain.model.Speciality

// TODO: verify field names against real API traffic
data class SpecialityDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("codeForPerson") val code: String? = null
) {
    fun toDomain() = Speciality(id = id, name = name, code = code ?: "")
}
