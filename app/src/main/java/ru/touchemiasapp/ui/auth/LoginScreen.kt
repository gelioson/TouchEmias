package ru.touchemiasapp.ui.auth

import android.util.Log
import android.webkit.CookieManager
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
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.result) {
        when (state.result) {
            is LoginResult.Success -> onSuccess()
            else -> Unit
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Вход через mos.ru") }) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.result is LoginResult.Error) {
                Text(
                    text = (state.result as LoginResult.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // WebView stays alive the entire time — we need it to keep running
                // even after we detect the redirect so onPageFinished / postDelayed can fire
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            CookieManager.getInstance().removeAllCookies(null)

                            // JavascriptInterface: receives EI-Token from the whoAmI XHR body
                            addJavascriptInterface(object : Any() {
                                @android.webkit.JavascriptInterface
                                fun onEiToken(token: String) {
                                    Log.d("TouchEmias", "EI-Token captured: ${token.take(20)}…")
                                    viewModel.onEiTokenCaptured(token)
                                }
                            }, "AndroidBridge")

                            webViewClient = object : WebViewClient() {
                                private var emiasRedirectHandled = false
                                private var interceptorSet = false

                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest
                                ): Boolean {
                                    val url = request.url.toString()
                                    Log.d("TouchEmias", "shouldOverrideUrlLoading: $url")
                                    if (!emiasRedirectHandled && url.startsWith(AuthRepository.REDIRECT_URL)) {
                                        emiasRedirectHandled = true
                                        viewModel.onEmiasRedirectStarted()
                                    }
                                    return false
                                }

                                override fun onPageFinished(view: WebView, url: String) {
                                    Log.d("TouchEmias", "onPageFinished: $url emiasHandled=$emiasRedirectHandled")
                                    if (!emiasRedirectHandled || interceptorSet) return
                                    if (url.startsWith("https://emias.info") && !interceptorSet) {
                                        interceptorSet = true
                                        // Inject XHR interceptor — when whoAmI fires, forward EI-Token to Android
                                        view.evaluateJavascript("""
                                            (function() {
                                                var ox = XMLHttpRequest.prototype.open;
                                                var os = XMLHttpRequest.prototype.send;
                                                XMLHttpRequest.prototype.open = function(m,u) { this._u=u; return ox.apply(this,arguments); };
                                                XMLHttpRequest.prototype.send = function(b) {
                                                    if(this._u && this._u.includes('whoAmI') && b) {
                                                        try {
                                                            var bd=JSON.parse(''+b);
                                                            if(bd.accessToken && window.AndroidBridge) {
                                                                window.AndroidBridge.onEiToken(bd.accessToken);
                                                            }
                                                        } catch(e) {}
                                                    }
                                                    return os.apply(this,arguments);
                                                };
                                            })();
                                        """.trimIndent(), null)
                                    }
                                }

                                override fun onReceivedError(
                                    view: WebView,
                                    request: WebResourceRequest,
                                    error: WebResourceError
                                ) {
                                    Log.e("TouchEmias", "WebView error: ${error.errorCode} ${error.description} ${request.url}")
                                }
                            }
                            loadUrl(state.loginUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                // Loading overlay on top of WebView — keeps WebView alive
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
