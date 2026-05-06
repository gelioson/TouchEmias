package ru.touchemiasapp.data.api.model.request

import com.google.gson.annotations.SerializedName

// TODO: verify actual field names against real API traffic
class CreateAppointmentRequest(
    omsNumber: String,
    birthDate: String,
    @SerializedName("availableResourceId") val availableResourceId: Long,
    @SerializedName("complexResourceId") val complexResourceId: Long,
    @SerializedName("receptionTypeId") val receptionTypeId: String,
    @SerializedName("startTime") val startTime: String,   // ISO datetime
    @SerializedName("endTime") val endTime: String
) : OmsData(omsNumber, birthDate)
