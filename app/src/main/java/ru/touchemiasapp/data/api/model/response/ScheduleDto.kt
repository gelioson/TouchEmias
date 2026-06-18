package ru.touchemiasapp.data.api.model.response

import com.google.gson.annotations.SerializedName
import ru.touchemiasapp.domain.model.TimeSlot

// payload → SchedulePayloadDto
data class SchedulePayloadDto(
    @SerializedName("availableResource") val availableResource: ScheduleResourceDto? = null,
    @SerializedName("scheduleOfDay") val scheduleOfDay: List<ScheduleDayDto>? = null
)

data class ScheduleResourceDto(
    @SerializedName("receptionType") val receptionType: List<ReceptionTypeDto>? = null
)

data class ReceptionTypeDto(
    @SerializedName("code") val code: Long? = null
)

data class ScheduleDayDto(
    @SerializedName("date") val date: String,
    @SerializedName("scheduleBySlot") val scheduleBySlot: List<ScheduleBySlotDto>? = null
)

data class ScheduleBySlotDto(
    @SerializedName("complexResourceId") val complexResourceId: Long,
    @SerializedName("slot") val slots: List<SlotTimeDto>? = null
)

data class SlotTimeDto(
    @SerializedName("startTime") val startTime: String,  // "2026-06-22T08:12:00+03:00"
    @SerializedName("endTime") val endTime: String
) {
    fun toDomain(date: String, complexResourceId: Long, availableResourceId: Long, receptionTypeId: Long) = TimeSlot(
        date = date,
        startTime = startTime.substring(11, 16),
        endTime = endTime.substring(11, 16),
        complexResourceId = complexResourceId,
        receptionTypeId = receptionTypeId,
        availableResourceId = availableResourceId
    )
}
