package ru.touchemiasapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.touchemiasapp.data.db.dao.WatchJobDao
import ru.touchemiasapp.data.db.entity.WatchJobEntity
import ru.touchemiasapp.domain.model.Doctor
import ru.touchemiasapp.domain.model.MonitorMode
import ru.touchemiasapp.domain.model.WatchConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchJobRepository @Inject constructor(
    private val dao: WatchJobDao
) {
    fun observeLatest(): Flow<WatchConfig?> = dao.observeLatest().map { it?.toDomain() }

    suspend fun getActive(): WatchConfig? = dao.getActive()?.toDomain()

    suspend fun save(config: WatchConfig): Long {
        val entity = config.toEntity()
        return dao.insert(entity)
    }

    suspend fun setActive(id: Long, active: Boolean) {
        if (active) dao.deactivateAll()
        dao.setActive(id, active)
    }

    suspend fun deactivateAll() = dao.deactivateAll()

    private fun WatchJobEntity.toDomain(): WatchConfig {
        val doctors = doctorIds.indices.map { i ->
            Doctor(
                availableResourceId = doctorIds[i],
                complexResourceId = complexResourceIds.getOrElse(i) { doctorIds[i] },
                name = doctorNames.getOrElse(i) { "" },
                specialityName = specialityName,
                clinicId = 0,
                clinicName = clinicNames.getOrElse(i) { "" }
            )
        }
        return WatchConfig(
            id = id,
            specialityId = specialityId,
            specialityName = specialityName,
            doctors = doctors,
            selectedDates = selectedDates,
            timeFrom = timeFrom,
            timeTo = timeTo,
            mode = if (mode == "AUTO_BOOK") MonitorMode.AUTO_BOOK else MonitorMode.NOTIFY_ONLY,
            intervalSeconds = intervalSeconds,
            isActive = isActive
        )
    }

    private fun WatchConfig.toEntity() = WatchJobEntity(
        id = id,
        specialityId = specialityId,
        specialityName = specialityName,
        doctorIds = doctors.map { it.availableResourceId },
        complexResourceIds = doctors.map { it.complexResourceId },
        doctorNames = doctors.map { it.name },
        clinicNames = doctors.map { it.clinicName },
        selectedDates = selectedDates,
        timeFrom = timeFrom,
        timeTo = timeTo,
        mode = mode.name,
        intervalSeconds = intervalSeconds,
        isActive = isActive
    )
}
