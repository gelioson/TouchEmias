package ru.touchemiasapp.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import ru.touchemiasapp.data.auth.AuthRepository

class LoginWebViewActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var tokenHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) webView.goBack()
            else { setResult(RESULT_CANCELED); finish() }
        }

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = settings.userAgentString.replace(" wv", "")

            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            addJavascriptInterface(object : Any() {
                @android.webkit.JavascriptInterface
                fun onEiToken(token: String) {
                    if (token.isBlank() || tokenHandled) return
                    tokenHandled = true
                    Log.d("TouchEmias", "EI-Token captured: ${token.take(20)}…")
                    runOnUiThread {
                        setResult(RESULT_OK, Intent().putExtra("ei_token", token))
                        finish()
                    }
                }
            }, "AndroidBridge")

            webViewClient = object : WebViewClient() {
                private var emiasRedirectHandled = false

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()
                    Log.d("TouchEmias", "shouldOverrideUrlLoading: $url")
                    if (!emiasRedirectHandled && url.startsWith(AuthRepository.REDIRECT_URL)) {
                        emiasRedirectHandled = true
                    }
                    return false
                }

                override fun onPageFinished(view: WebView, url: String) {
                    Log.d("TouchEmias", "onPageFinished: $url emiasHandled=$emiasRedirectHandled")
                    if (!emiasRedirectHandled) return
                    if (url.startsWith("https://emias.info")) {
                        view.evaluateJavascript("""
                            (function() {
                                if(window.__eiInterceptorInstalled) return;
                                window.__eiInterceptorInstalled = true;
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

                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    Log.e("TouchEmias", "WebView error: ${error.errorCode} ${error.description} ${request.url}")
                }
            }

            loadUrl(AuthRepository.LOGIN_URL)
        }

        setContentView(webView)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
