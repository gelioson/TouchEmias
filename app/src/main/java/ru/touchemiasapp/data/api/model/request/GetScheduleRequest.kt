package ru.touchemiasapp.data.api.model.request

import com.google.gson.annotations.SerializedName

class GetScheduleRequest(
    omsNumber: String,
    birthDate: String,
    @SerializedName("availableResourceId") val availableResourceId: Long,
    // Required by new API v4
    @SerializedName("complexResourceId") val complexResourceId: Long
) : OmsData(omsNumber, birthDate)
