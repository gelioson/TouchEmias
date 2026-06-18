package ru.touchemiasapp.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.repository.WatchJobRepository
import ru.touchemiasapp.domain.model.MonitorMode
import ru.touchemiasapp.domain.model.POLL_INTERVALS
import ru.touchemiasapp.domain.model.WatchConfig
import javax.inject.Inject

data class ScheduleUiState(
    val selectedDates: Set<String> = emptySet(),
    val timeFrom: String = "09:00",
    val timeTo: String = "17:00",
    val mode: MonitorMode = MonitorMode.NOTIFY_ONLY,
    val intervalSeconds: Int = 60,
    val isSaving: Boolean = false
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val watchJobRepository: WatchJobRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState())
    val state: StateFlow<ScheduleUiState> = _state

    fun toggleDate(date: String) {
        _state.update { s ->
            val dates = s.selectedDates.toMutableSet()
            if (dates.contains(date)) dates.remove(date) else dates.add(date)
            s.copy(selectedDates = dates)
        }
    }

    fun setTimeFrom(value: String) = _state.update { it.copy(timeFrom = value) }
    fun setTimeTo(value: String) = _state.update { it.copy(timeTo = value) }
    fun setMode(mode: MonitorMode) = _state.update { it.copy(mode = mode) }
    fun setInterval(seconds: Int) = _state.update { it.copy(intervalSeconds = seconds) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.selectedDates.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val draft = watchJobRepository.observeLatest().firstOrNull() ?: return@launch
            watchJobRepository.save(
                draft.copy(
                    selectedDates = s.selectedDates.sorted(),
                    timeFrom = s.timeFrom,
                    timeTo = s.timeTo,
                    mode = s.mode,
                    intervalSeconds = s.intervalSeconds,
                    isActive = false
                )
            )
            _state.update { it.copy(isSaving = false) }
            onDone()
        }
    }

    val availableIntervals = POLL_INTERVALS
}
