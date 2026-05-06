package ru.touchemiasapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.preferences.UserPreferencesDataStore
import ru.touchemiasapp.domain.repository.EmiasRepository
import javax.inject.Inject

data class OnboardingUiState(
    val omsNumber: String = "",
    val birthDate: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferencesDataStore,
    private val repository: EmiasRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state

    val isRegistered: StateFlow<Boolean> = prefs.userPreferences
        .map { it.isComplete }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onOmsChanged(value: String) = _state.update { it.copy(omsNumber = value, error = null) }
    fun onBirthDateChanged(value: String) = _state.update { it.copy(birthDate = value, error = null) }

    fun checkAndSave(onSuccess: () -> Unit) {
        val s = _state.value
        val birthDateIso = parseBirthDate(s.birthDate) ?: run {
            _state.update { it.copy(error = "Неверный формат даты. Используйте ДД.ММ.ГГГГ") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.checkOms(s.omsNumber.trim(), birthDateIso)
                .onSuccess {
                    prefs.save(s.omsNumber.trim(), birthDateIso)
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    private fun parseBirthDate(input: String): String? {
        val parts = input.split(".")
        if (parts.size != 3) return null
        val (d, m, y) = parts.map { it.trim() }
        if (d.length != 2 || m.length != 2 || y.length != 4) return null
        return "$y-$m-$d"
    }
}
