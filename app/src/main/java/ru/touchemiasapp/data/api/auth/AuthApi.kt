package ru.touchemiasapp.data.api.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("web-api/getTokens/")
    suspend fun getTokens(@Body request: GetTokensRequest): TokenResponse

    @POST("web-api/refreshTokens/")
    suspend fun refreshTokens(@Body request: RefreshTokenRequest): TokenResponse
}
