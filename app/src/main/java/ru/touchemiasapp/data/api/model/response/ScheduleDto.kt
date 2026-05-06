package ru.touchemiasapp.data.api.model.response

import com.google.gson.annotations.SerializedName
import ru.touchemiasapp.domain.model.TimeSlot

// TODO: verify field names + date/time format against real API traffic
data class ScheduleDayDto(
    @SerializedName("date") val date: String,           // ISO datetime or yyyy-MM-dd
    @SerializedName("scheduleBySlot") val slots: List<SlotDto>? = null
)

data class SlotDto(
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("complexResourceId") val complexResourceId: Long,
    @SerializedName("receptionTypeId") val receptionTypeId: String
) {
    fun toDomain(date: String, availableResourceId: Long) = TimeSlot(
        date = date.take(10),                   // keep only yyyy-MM-dd part
        startTime = startTime.substring(11, 16).ifEmpty { startTime.take(5) },
        endTime = endTime.substring(11, 16).ifEmpty { endTime.take(5) },
        complexResourceId = complexResourceId,
        receptionTypeId = receptionTypeId,
        availableResourceId = availableResourceId
    )
}
