package ru.touchemiasapp.data.repository

import ru.touchemiasapp.data.api.EmiasApi
import ru.touchemiasapp.data.api.model.request.CreateAppointmentRequest
import ru.touchemiasapp.data.api.model.request.GetDoctorsRequest
import ru.touchemiasapp.data.api.model.request.GetScheduleRequest
import ru.touchemiasapp.data.api.model.request.GetSpecialitiesRequest
import ru.touchemiasapp.domain.model.Doctor
import ru.touchemiasapp.domain.model.Speciality
import ru.touchemiasapp.domain.model.TimeSlot
import ru.touchemiasapp.domain.repository.EmiasRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmiasRepositoryImpl @Inject constructor(
    private val api: EmiasApi
) : EmiasRepository {

    override suspend fun getSpecialities(omsNumber: String, birthDate: String): Result<List<Speciality>> =
        runCatching {
            val resp = api.getSpecialities(GetSpecialitiesRequest(omsNumber, birthDate))
            if (!resp.isSuccess) error(resp.errorMessage ?: "Failed to load specialities")
            resp.result?.map { it.toDomain() } ?: emptyList()
        }

    override suspend fun getDoctors(omsNumber: String, birthDate: String, specialityId: Long): Result<List<Doctor>> =
        runCatching {
            val resp = api.getDoctors(GetDoctorsRequest(omsNumber, birthDate, setOf(specialityId)))
            if (!resp.isSuccess) error(resp.errorMessage ?: "Failed to load doctors")
            resp.result?.map { it.toDomain() } ?: emptyList()
        }

    override suspend fun getAvailableSlots(
        omsNumber: String,
        birthDate: String,
        availableResourceId: Long,
        complexResourceId: Long
    ): Result<List<TimeSlot>> =
        runCatching {
            val resp = api.getSchedule(
                GetScheduleRequest(omsNumber, birthDate, availableResourceId, complexResourceId)
            )
            if (!resp.isSuccess) error(resp.errorMessage ?: "Failed to load schedule")
            resp.result?.flatMap { day ->
                day.slots?.map { slot -> slot.toDomain(day.date, availableResourceId) } ?: emptyList()
            } ?: emptyList()
        }

    override suspend fun createAppointment(omsNumber: String, birthDate: String, slot: TimeSlot): Result<Unit> =
        runCatching {
            val datePrefix = slot.date + "T"
            val resp = api.createAppointment(
                CreateAppointmentRequest(
                    omsNumber = omsNumber,
                    birthDate = birthDate,
                    availableResourceId = slot.availableResourceId,
                    complexResourceId = slot.complexResourceId,
                    receptionTypeId = slot.receptionTypeId,
                    startTime = datePrefix + slot.startTime + ":00",
                    endTime = datePrefix + slot.endTime + ":00"
                )
            )
            if (!resp.isSuccess) error(resp.errorMessage ?: "Failed to create appointment")
        }
}
