package ru.touchemiasapp.domain.repository

import ru.touchemiasapp.domain.model.Doctor
import ru.touchemiasapp.domain.model.Speciality
import ru.touchemiasapp.domain.model.TimeSlot

interface EmiasRepository {
    suspend fun checkOms(omsNumber: String, birthDate: String): Result<Unit>
    suspend fun getSpecialities(omsNumber: String, birthDate: String): Result<List<Speciality>>
    suspend fun getDoctors(omsNumber: String, birthDate: String, specialityId: Long): Result<List<Doctor>>
    suspend fun getAvailableSlots(omsNumber: String, birthDate: String, availableResourceId: Long): Result<List<TimeSlot>>
    suspend fun createAppointment(
        omsNumber: String,
        birthDate: String,
        slot: TimeSlot
    ): Result<Unit>
}
