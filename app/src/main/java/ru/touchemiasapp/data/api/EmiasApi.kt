package ru.touchemiasapp.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import ru.touchemiasapp.data.api.model.request.CreateAppointmentRequest
import ru.touchemiasapp.data.api.model.request.GetDoctorsRequest
import ru.touchemiasapp.data.api.model.request.GetScheduleRequest
import ru.touchemiasapp.data.api.model.request.OmsData
import ru.touchemiasapp.data.api.model.response.DoctorDto
import ru.touchemiasapp.data.api.model.response.EmiasResponse
import ru.touchemiasapp.data.api.model.response.ScheduleDayDto
import ru.touchemiasapp.data.api.model.response.SpecialityDto

// All EMIAS API calls are POST with JSON body to /api/new/eip2/?<methodName>
// TODO: If API v8 uses different paths, update these URLs after traffic analysis
interface EmiasApi {

    @POST("api/new/eip2/?getReferralsInfo")
    suspend fun checkOms(@Body body: OmsData): EmiasResponse<Any>

    @POST("api/new/eip2/?getSpecialitiesInfo")
    suspend fun getSpecialities(@Body body: OmsData): EmiasResponse<List<SpecialityDto>>

    @POST("api/new/eip2/?getDoctorsInfo")
    suspend fun getDoctors(@Body body: GetDoctorsRequest): EmiasResponse<List<DoctorDto>>

    @POST("api/new/eip2/?getAvailableResourceScheduleInfo")
    suspend fun getSchedule(@Body body: GetScheduleRequest): EmiasResponse<List<ScheduleDayDto>>

    @POST("api/new/eip2/?createAppointment")
    suspend fun createAppointment(@Body body: CreateAppointmentRequest): EmiasResponse<Any>

    @POST("api/new/eip2/?cancelAppointment")
    suspend fun cancelAppointment(@Body body: OmsData): EmiasResponse<Any>
}
