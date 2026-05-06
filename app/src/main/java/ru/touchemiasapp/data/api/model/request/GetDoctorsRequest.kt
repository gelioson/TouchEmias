package ru.touchemiasapp.data.api.model.request

import com.google.gson.annotations.SerializedName

// TODO: verify actual field names against real API traffic
class GetDoctorsRequest(
    omsNumber: String,
    birthDate: String,
    @SerializedName("specialityId") val specialityId: Long
) : OmsData(omsNumber, birthDate)
