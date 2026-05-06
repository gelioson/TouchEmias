package ru.touchemiasapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.api.auth.OmsPolicy
import ru.touchemiasapp.data.auth.AuthRepository
import javax.inject.Inject

@HiltViewModel
class PolicySelectionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val policies: List<OmsPolicy> get() = authRepository.pendingPolicies

    private val _done = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done

    fun selectPolicy(policy: OmsPolicy) {
        viewModelScope.launch {
            authRepository.selectPolicy(policy)
            _done.value = true
        }
    }
}
