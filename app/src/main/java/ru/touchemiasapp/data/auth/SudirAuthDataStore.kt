package ru.touchemiasapp.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "sudir_auth")

@Singleton
class SudirAuthDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val ID_TOKEN = stringPreferencesKey("id_token")

    val isLoggedIn: Flow<Boolean> = context.authDataStore.data
        .map { !it[ACCESS_TOKEN].isNullOrBlank() }

    // Called from OkHttp interceptor (background thread) — runBlocking is safe here
    fun getAccessTokenSync(): String? = runBlocking {
        context.authDataStore.data.first()[ACCESS_TOKEN]
    }

    fun getRefreshTokenSync(): String? = runBlocking {
        context.authDataStore.data.first()[REFRESH_TOKEN]
    }

    suspend fun getAccessToken(): String? = context.authDataStore.data.first()[ACCESS_TOKEN]
    suspend fun getRefreshToken(): String? = context.authDataStore.data.first()[REFRESH_TOKEN]

    suspend fun saveTokens(accessToken: String, refreshToken: String, idToken: String?) {
        context.authDataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
            idToken?.let { prefs[ID_TOKEN] = it }
        }
    }

    suspend fun updateAccessToken(accessToken: String) {
        context.authDataStore.edit { it[ACCESS_TOKEN] = accessToken }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }
}
