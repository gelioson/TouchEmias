package ru.touchemiasapp.ui.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.auth.AuthRepository
import javax.inject.Inject

sealed class LoginResult {
    data object Idle : LoginResult()
    data object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}

data class LoginUiState(
    val loginUrl: String = AuthRepository.LOGIN_URL,
    val isLoading: Boolean = false,
    val result: LoginResult = LoginResult.Idle
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun resetError() {
        _state.update { it.copy(result = LoginResult.Idle) }
    }

    fun onEmiasRedirectStarted() {
        _state.update { it.copy(isLoading = true) }
    }

    private var eiTokenHandled = false

    fun onEiTokenCaptured(token: String) {
        if (token.isBlank() || eiTokenHandled) return
        eiTokenHandled = true
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.completeLogin(token)
                .onSuccess { _state.update { it.copy(isLoading = false, result = LoginResult.Success) } }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, result = LoginResult.Error(e.message ?: "Ошибка авторизации")) }
                }
        }
    }

    fun onRedirect(url: String) {
        val code = Uri.parse(url).getQueryParameter("code") ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.exchangeCode(code)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, result = LoginResult.Success) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, result = LoginResult.Error(e.message ?: "Ошибка авторизации")) }
                }
        }
    }
}
