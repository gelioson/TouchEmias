package ru.touchemiasapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.touchemiasapp.data.auth.SudirAuthDataStore
import javax.inject.Inject

// Kept only for backward compatibility. New auth flow uses LoginScreen + AuthRepository.
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authDataStore: SudirAuthDataStore
) : ViewModel() {

    val isRegistered: StateFlow<Boolean> = authDataStore.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
