package ru.touchemiasapp.ui.specialities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.preferences.UserPreferencesDataStore
import ru.touchemiasapp.domain.model.Speciality
import ru.touchemiasapp.domain.repository.EmiasRepository
import javax.inject.Inject

sealed class SpecialitiesUiState {
    data object Loading : SpecialitiesUiState()
    data class Success(val items: List<Speciality>) : SpecialitiesUiState()
    data class Error(val message: String) : SpecialitiesUiState()
}

@HiltViewModel
class SpecialitiesViewModel @Inject constructor(
    private val repository: EmiasRepository,
    private val prefs: UserPreferencesDataStore
) : ViewModel() {

    private val _state = MutableStateFlow<SpecialitiesUiState>(SpecialitiesUiState.Loading)
    val state: StateFlow<SpecialitiesUiState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { SpecialitiesUiState.Loading }
            val userPrefs = prefs.userPreferences.first()
            repository.getSpecialities(userPrefs.omsNumber, userPrefs.birthDate)
                .onSuccess { list -> _state.update { SpecialitiesUiState.Success(list) } }
                .onFailure { e -> _state.update { SpecialitiesUiState.Error(e.message ?: "Ошибка") } }
        }
    }
}
