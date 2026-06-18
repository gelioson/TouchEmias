package ru.touchemiasapp.data.api.auth

import com.google.gson.annotations.SerializedName

data class GetTokensRequest(
    val code: String,
    val redirectUrl: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("id_token") val idToken: String?,
    @SerializedName("expires_in") val expiresIn: Int?
)

data class OmsPolicy(
    @SerializedName("omsNumber") val omsNumber: String,
    @SerializedName("birthDate") val birthDate: String,
    @SerializedName("policyName") val policyName: String? = null,
    @SerializedName("permissionType") val permissionType: String? = null
)

data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int = 1,
    val method: String,
    val params: Map<String, Any> = emptyMap()
)

data class JsonRpcError(
    val code: Int?,
    val message: String?
)
