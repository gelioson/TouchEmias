package ru.touchemiasapp.ui.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val loginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val token = result.data?.getStringExtra("ei_token") ?: return@rememberLauncherForActivityResult
            viewModel.onEiTokenCaptured(token)
        }
    }

    LaunchedEffect(state.result) {
        if (state.result is LoginResult.Success) onSuccess()
    }

    // Launch the native WebView activity immediately on first composition
    LaunchedEffect(Unit) {
        loginLauncher.launch(Intent(context, LoginWebViewActivity::class.java))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Вход через mos.ru") }) }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val result = state.result) {
                is LoginResult.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(result.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        viewModel.resetError()
                        loginLauncher.launch(Intent(context, LoginWebViewActivity::class.java))
                    }) { Text("Попробовать снова") }
                }
                else -> if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(onClick = {
                        loginLauncher.launch(Intent(context, LoginWebViewActivity::class.java))
                    }) { Text("Войти через mos.ru") }
                }
            }
        }
    }
}
