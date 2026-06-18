package ru.touchemiasapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.preferences.UserPreferencesDataStore
import javax.inject.Inject

data class OmsEntryState(
    val omsNumber: String = "",
    val birthDate: String = "",
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class OmsEntryViewModel @Inject constructor(
    private val prefs: UserPreferencesDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(OmsEntryState())
    val state: StateFlow<OmsEntryState> = _state

    fun onOmsChanged(value: String) = _state.update { it.copy(omsNumber = value, error = null) }
    fun onBirthDateChanged(value: String) = _state.update { it.copy(birthDate = value, error = null) }

    fun save() {
        val oms = _state.value.omsNumber.trim()
        val bd = _state.value.birthDate.trim()
        if (oms.isBlank()) { _state.update { it.copy(error = "Введите номер полиса ОМС") }; return }
        if (!bd.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            _state.update { it.copy(error = "Дата рождения в формате ГГГГ-ММ-ДД") }; return
        }
        viewModelScope.launch {
            prefs.save(oms, bd)
            _state.update { it.copy(saved = true) }
        }
    }
}
