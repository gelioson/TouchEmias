package ru.touchemiasapp.data.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.touchemiasapp.data.api.auth.AuthApi
import ru.touchemiasapp.data.api.auth.GetTokensRequest
import ru.touchemiasapp.data.api.auth.RefreshTokenRequest
import ru.touchemiasapp.data.preferences.UserPreferencesDataStore
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val authDataStore: SudirAuthDataStore,
    private val userPrefsDataStore: UserPreferencesDataStore,
    @Named("plain") private val httpClient: OkHttpClient
) {
    companion object {
        const val REDIRECT_URL = "https://emias.info/sudir-web"
        const val LOGIN_URL = "https://login.mos.ru/sps/oauth/ae" +
            "?scope=openid+profile" +
            "&client_id=emias.info.web" +
            "&redirect_uri=https%3A%2F%2Femias.info%2Fsudir-web" +
            "&response_type=code" +
            "&access_type=offline" +
            "&prompt=login"
    }

    val isLoggedIn: Flow<Boolean> = authDataStore.isLoggedIn

    suspend fun saveSessionCookies(cookieString: String): Result<Unit> = runCatching {
        authDataStore.saveSessionCookies(cookieString)
    }

    suspend fun completeLogin(eiToken: String): Result<Unit> = runCatching {
        authDataStore.saveEiToken(eiToken)

        withContext(Dispatchers.IO) {
            val body = """{"accessToken":"$eiToken"}""".toRequestBody("application/json".toMediaType())
            val req = Request.Builder()
                .url("https://emias.info/web-api/whoAmI/")
                .post(body)
                .header("EI-Token", eiToken)
                .header("X-App", "portal")
                .header("Accept", "application/json, text/plain, */*")
                .build()

            httpClient.newCall(req).execute().use { resp ->
                Log.d("TouchEmias", "whoAmI code=${resp.code}")
                val bodyStr = resp.body?.string() ?: ""
                Log.d("TouchEmias", "whoAmI body=${bodyStr.take(500)}")
                val setCookies = resp.headers("Set-Cookie")
                Log.d("TouchEmias", "whoAmI Set-Cookie: $setCookies")

                val newCookie = setCookies
                    .find { it.startsWith("session-cookie=") }
                    ?.substringAfter("session-cookie=")?.substringBefore(";")
                if (!newCookie.isNullOrBlank()) {
                    authDataStore.saveSessionCookies("session-cookie=$newCookie")
                    Log.d("TouchEmias", "Saved fresh session-cookie from OkHttp whoAmI")
                } else if (resp.code != 200) {
                    error("whoAmI failed: ${resp.code} $bodyStr")
                }
            }
        }
    }

    suspend fun exchangeCode(code: String): Result<Unit> = runCatching {
        val response = authApi.getTokens(GetTokensRequest(code, REDIRECT_URL))
        val accessToken = response.accessToken ?: error("No access token in response")
        val refreshToken = response.refreshToken ?: error("No refresh token in response")
        authDataStore.saveTokens(accessToken, refreshToken, response.idToken)
    }

    suspend fun refreshToken(): Boolean {
        val refreshToken = authDataStore.getRefreshToken() ?: return false
        return runCatching {
            val response = authApi.refreshTokens(RefreshTokenRequest(refreshToken))
            val newAccess = response.accessToken ?: return false
            if (response.refreshToken != null) {
                authDataStore.saveTokens(newAccess, response.refreshToken, response.idToken)
            } else {
                authDataStore.updateAccessToken(newAccess)
            }
            true
        }.getOrDefault(false)
    }

    suspend fun logout() {
        authDataStore.clear()
        userPrefsDataStore.clear()
    }
}
