package ru.touchemiasapp.data.api.model.request

import com.google.gson.annotations.SerializedName

class CreateAppointmentRequest(
    omsNumber: String,
    birthDate: String,
    @SerializedName("availableResourceId") val availableResourceId: Long,
    @SerializedName("complexResourceId") val complexResourceId: Long,
    @SerializedName("receptionTypeId") val receptionTypeId: Long,
    @SerializedName("startTime") val startTime: String,   // ISO datetime with tz
    @SerializedName("endTime") val endTime: String
) : OmsData(omsNumber, birthDate)
