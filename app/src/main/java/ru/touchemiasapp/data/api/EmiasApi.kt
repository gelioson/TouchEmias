package ru.touchemiasapp.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import ru.touchemiasapp.data.api.model.request.CancelAppointmentRequest
import ru.touchemiasapp.data.api.model.request.CreateAppointmentRequest
import ru.touchemiasapp.data.api.model.request.GetDoctorsRequest
import ru.touchemiasapp.data.api.model.request.GetScheduleRequest
import ru.touchemiasapp.data.api.model.request.GetSpecialitiesRequest
import ru.touchemiasapp.data.api.model.response.DoctorsPayloadDto
import ru.touchemiasapp.data.api.model.response.EmiasResponse
import ru.touchemiasapp.data.api.model.response.SchedulePayloadDto
import ru.touchemiasapp.data.api.model.response.SpecialityDto

// Base URL: https://emias.info/api-eip/
// All calls require EI-Token header (added by AuthInterceptor)
interface EmiasApi {

    @POST("v7/saOrchestrator/getSpecialitiesInfo")
    suspend fun getSpecialities(@Body body: GetSpecialitiesRequest): EmiasResponse<List<SpecialityDto>>

    @POST("v5/saOrchestrator/getDoctorsInfo")
    suspend fun getDoctors(@Body body: GetDoctorsRequest): EmiasResponse<DoctorsPayloadDto>

    @POST("v4/saOrchestrator/getAvailableResourceScheduleInfo")
    suspend fun getSchedule(@Body body: GetScheduleRequest): EmiasResponse<SchedulePayloadDto>

    @POST("v4/saOrchestrator/createAppointment")
    suspend fun createAppointment(@Body body: CreateAppointmentRequest): EmiasResponse<Any>

    @POST("v5/saOrchestrator/cancelAppointment")
    suspend fun cancelAppointment(@Body body: CancelAppointmentRequest): EmiasResponse<Any>
}
