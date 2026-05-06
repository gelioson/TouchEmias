package ru.touchemiasapp.data.api.model.request

import com.google.gson.annotations.SerializedName

class GetSpecialitiesRequest(
    omsNumber: String,
    birthDate: String,
    @SerializedName("isChatBotEnabled") val isChatBotEnabled: Boolean = false
) : OmsData(omsNumber, birthDate)
