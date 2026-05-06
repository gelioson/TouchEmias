package ru.touchemiasapp.data.api.model.response

import com.google.gson.annotations.SerializedName

// Generic wrapper for all EMIAS API responses
// TODO: verify actual wrapper structure against real API traffic
data class EmiasResponse<T>(
    @SerializedName("result") val result: T?,
    @SerializedName("errorCode") val errorCode: Int? = null,
    @SerializedName("errorMessage") val errorMessage: String? = null
) {
    val isSuccess: Boolean get() = errorCode == null || errorCode == 0
}
