package ru.touchemiasapp.ui.doctors

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.preferences.UserPreferencesDataStore
import ru.touchemiasapp.data.repository.WatchJobRepository
import ru.touchemiasapp.domain.model.Doctor
import ru.touchemiasapp.domain.model.MonitorMode
import ru.touchemiasapp.domain.model.TimeSlot
import ru.touchemiasapp.domain.model.WatchConfig
import ru.touchemiasapp.domain.repository.EmiasRepository
import javax.inject.Inject

data class DoctorUiItem(
    val doctor: Doctor,
    val isSelected: Boolean = false,
    val slots: List<TimeSlot> = emptyList(),
    val isSlotsExpanded: Boolean = false,
    val isSlotsLoading: Boolean = false
)

data class DoctorsUiState(
    val specialityId: Long = 0,
    val specialityName: String = "",
    val doctors: List<DoctorUiItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val selectedCount get() = doctors.count { it.isSelected }
}

@HiltViewModel
class DoctorsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EmiasRepository,
    private val prefs: UserPreferencesDataStore,
    private val watchJobRepository: WatchJobRepository
) : ViewModel() {

    private val specialityId: Long = savedStateHandle["specialityId"] ?: 0L
    private val specialityName: String = savedStateHandle["specialityName"] ?: ""

    private val _state = MutableStateFlow(DoctorsUiState(specialityId, specialityName))
    val state: StateFlow<DoctorsUiState> = _state

    init { loadDoctors() }

    private fun loadDoctors() {
        viewModelScope.launch {
            val userPrefs = prefs.userPreferences.first()
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getDoctors(userPrefs.omsNumber, userPrefs.birthDate, specialityId)
                .onSuccess { list ->
                    _state.update { it.copy(isLoading = false, doctors = list.map { d -> DoctorUiItem(d) }) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun toggleSelection(doctorId: Long) {
        _state.update { state ->
            state.copy(doctors = state.doctors.map { item ->
                if (item.doctor.availableResourceId == doctorId) item.copy(isSelected = !item.isSelected)
                else item
            })
        }
    }

    fun toggleSlots(doctorId: Long) {
        val item = _state.value.doctors.find { it.doctor.availableResourceId == doctorId } ?: return
        if (item.isSlotsExpanded) {
            _state.update { state ->
                state.copy(doctors = state.doctors.map {
                    if (it.doctor.availableResourceId == doctorId) it.copy(isSlotsExpanded = false) else it
                })
            }
            return
        }
        if (item.slots.isEmpty()) {
            loadSlots(doctorId)
        } else {
            _state.update { state ->
                state.copy(doctors = state.doctors.map {
                    if (it.doctor.availableResourceId == doctorId) it.copy(isSlotsExpanded = true) else it
                })
            }
        }
    }

    private fun loadSlots(doctorId: Long) {
        viewModelScope.launch {
            _state.update { state ->
                state.copy(doctors = state.doctors.map {
                    if (it.doctor.availableResourceId == doctorId) it.copy(isSlotsLoading = true) else it
                })
            }
            val userPrefs = prefs.userPreferences.first()
            val doctor = _state.value.doctors.find { it.doctor.availableResourceId == doctorId }?.doctor
            repository.getAvailableSlots(
                userPrefs.omsNumber, userPrefs.birthDate,
                doctorId, doctor?.complexResourceId ?: doctorId
            )
                .onSuccess { slots ->
                    _state.update { state ->
                        state.copy(doctors = state.doctors.map {
                            if (it.doctor.availableResourceId == doctorId)
                                it.copy(slots = slots, isSlotsLoading = false, isSlotsExpanded = true)
                            else it
                        })
                    }
                }
                .onFailure {
                    _state.update { state ->
                        state.copy(doctors = state.doctors.map {
                            if (it.doctor.availableResourceId == doctorId)
                                it.copy(isSlotsLoading = false, isSlotsExpanded = true)
                            else it
                        })
                    }
                }
        }
    }

    fun getSelectedDoctors(): List<Doctor> =
        _state.value.doctors.filter { it.isSelected }.map { it.doctor }

    // Called before navigating to ScheduleScreen — persists selection as a draft WatchJob
    fun saveSelectionDraft(onDone: () -> Unit) {
        val selected = getSelectedDoctors()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            watchJobRepository.save(
                WatchConfig(
                    specialityId = specialityId,
                    specialityName = specialityName,
                    doctors = selected,
                    selectedDates = emptyList(),
                    timeFrom = "09:00",
                    timeTo = "17:00",
                    mode = MonitorMode.NOTIFY_ONLY,
                    intervalSeconds = 60,
                    isActive = false
                )
            )
            onDone()
        }
    }
}
