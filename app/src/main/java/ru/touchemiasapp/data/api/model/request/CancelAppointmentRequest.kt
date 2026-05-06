package ru.touchemiasapp.data.api.model.request

import com.google.gson.annotations.SerializedName

class CancelAppointmentRequest(
    omsNumber: String,
    birthDate: String,
    @SerializedName("appointmentId") val appointmentId: Long
) : OmsData(omsNumber, birthDate)
