package ru.touchemiasapp.data.api.model.response

import com.google.gson.annotations.SerializedName

data class EmiasResponse<T>(
    @SerializedName("payload") val result: T? = null,
    @SerializedName("error") val error: EmiasError? = null
) {
    val isSuccess: Boolean get() = error == null
    val errorMessage: String? get() = error?.description
}

data class EmiasError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("origin") val origin: String? = null
)
