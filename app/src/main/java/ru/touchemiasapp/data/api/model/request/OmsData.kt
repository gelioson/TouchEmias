package ru.touchemiasapp.data.api.model.request

import com.google.gson.annotations.SerializedName

// Base params included in every EMIAS request
open class OmsData(
    omsNumber: String,
    @SerializedName("birthDate") val birthDate: String   // yyyy-MM-dd
) {
    @SerializedName("omsNumber")
    val omsNumber: String = omsNumber.replace(" ", "")
}
