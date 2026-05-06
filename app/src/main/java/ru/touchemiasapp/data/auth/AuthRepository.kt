package ru.touchemiasapp.data.auth

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.touchemiasapp.data.api.auth.AuthApi
import ru.touchemiasapp.data.api.auth.GetTokensRequest
import ru.touchemiasapp.data.api.auth.JsonRpcRequest
import ru.touchemiasapp.data.api.auth.OmsPolicy
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
    @Named("plain") private val okHttpClient: OkHttpClient,
    private val gson: Gson
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

    // Policies returned after code exchange — held in memory for PolicySelectionScreen
    var pendingPolicies: List<OmsPolicy> = emptyList()
        private set

    suspend fun exchangeCode(code: String): Result<List<OmsPolicy>> = runCatching {
        val response = authApi.getTokens(GetTokensRequest(code, REDIRECT_URL))
        val accessToken = response.accessToken ?: error("No access token in response")
        val refreshToken = response.refreshToken ?: error("No refresh token in response")
        authDataStore.saveTokens(accessToken, refreshToken, response.idToken)
        val policies = fetchPoliciesSync(accessToken)
        pendingPolicies = policies
        policies
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

    suspend fun selectPolicy(policy: OmsPolicy) {
        userPrefsDataStore.save(policy.omsNumber, policy.birthDate)
    }

    suspend fun logout() {
        authDataStore.clear()
        userPrefsDataStore.clear()
    }

    private fun fetchPoliciesSync(accessToken: String): List<OmsPolicy> {
        val body = gson.toJson(JsonRpcRequest(method = "oms_list"))
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://emias.info/api/vault/v3/")
            .post(body)
            .header("Authorization", "Bearer $accessToken")
            .build()
        val responseJson = okHttpClient.newCall(request).execute().use {
            it.body?.string() ?: ""
        }
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        val map: Map<String, Any?> = runCatching<Map<String, Any?>> { gson.fromJson(responseJson, type) }.getOrNull()
            ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val resultList = map["result"] as? List<Map<String, Any?>> ?: return emptyList()
        return resultList.mapNotNull { item ->
            val oms = item["omsNumber"] as? String ?: return@mapNotNull null
            val bd = item["birthDate"] as? String ?: return@mapNotNull null
            OmsPolicy(
                omsNumber = oms,
                birthDate = bd,
                policyName = item["policyName"] as? String,
                permissionType = item["permissionType"] as? String
            )
        }
    }
}
