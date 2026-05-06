package ru.touchemiasapp.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Onboarding is replaced by LoginScreen (OAuth). Kept as stub to avoid orphaned references.
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, onSuccess: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator()
    }
}
