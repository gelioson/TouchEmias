package ru.touchemiasapp.ui.auth

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.viewinterop.AndroidView
import ru.touchemiasapp.data.auth.AuthRepository

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSuccess: () -> Unit,
    onMultiplePolicies: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.result) {
        when (state.result) {
            is LoginResult.SinglePolicy -> onSuccess()
            is LoginResult.MultiplePolicy -> onMultiplePolicies()
            else -> Unit
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Вход через mos.ru") }) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (state.result is LoginResult.Error) {
                Text(
                    text = (state.result as LoginResult.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest
                                ): Boolean {
                                    val url = request.url.toString()
                                    if (url.startsWith(AuthRepository.REDIRECT_URL)) {
                                        viewModel.onRedirect(url)
                                        return true
                                    }
                                    return false
                                }
                            }
                            loadUrl(state.loginUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
