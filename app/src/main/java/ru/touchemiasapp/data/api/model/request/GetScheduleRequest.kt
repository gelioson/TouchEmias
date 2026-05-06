package ru.touchemiasapp.data.api.model.request

import com.google.gson.annotations.SerializedName

// TODO: verify actual field names against real API traffic
class GetScheduleRequest(
    omsNumber: String,
    birthDate: String,
    @SerializedName("availableResourceId") val availableResourceId: Long
) : OmsData(omsNumber, birthDate)
