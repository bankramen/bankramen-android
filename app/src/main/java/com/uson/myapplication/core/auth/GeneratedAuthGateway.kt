package com.uson.myapplication.core.auth

import com.uson.myapplication.generated.api.APIApi
import com.uson.myapplication.generated.model.TokenRequest
import java.time.OffsetDateTime

class GeneratedAuthGateway(
    private val apiApi: APIApi,
) : AuthReissueGateway, AuthLoginGateway, AuthLogoutGateway {
    override suspend fun reissue(refreshToken: String): Result<AuthSession> = runCatching {
        val response = apiApi.reissue(tokenRequest = TokenRequest(refreshToken = refreshToken))
        if (!response.isSuccessful) {
            error("Token reissue failed: HTTP ${response.code()}")
        }
        response.body().toAuthSession()
    }

    override suspend fun loginWithKakao(request: KakaoAuthorizationCodeRequest): Result<AuthSession> = runCatching {
        val response = apiApi.callback(code = request.authorizationCode, state = request.state.orEmpty())
        if (!response.isSuccessful) {
            error("Kakao callback failed: HTTP ${response.code()}")
        }
        response.body().toAuthSession()
    }

    override suspend fun logout(refreshToken: String): Result<Unit> = runCatching {
        val response = apiApi.logout(tokenRequest = TokenRequest(refreshToken = refreshToken))
        if (!response.isSuccessful) {
            error("Logout failed: HTTP ${response.code()}")
        }
    }
}

private fun Any?.toAuthSession(): AuthSession {
    val fields = this.asObjectMap()
    val accessToken = fields.stringValue("accessToken", "access_token", "access")
    val refreshToken = fields.stringValue("refreshToken", "refresh_token", "refresh")
    require(accessToken.isNotBlank()) { "Auth response does not include accessToken" }

    return AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        accessTokenExpiresAtMillis = fields.expiryMillis(),
        userId = fields.stringValue("userId", "user_id", "memberId").ifBlank { null },
    )
}

private fun Any?.asObjectMap(): Map<String, Any?> = when (this) {
    is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value }
    else -> emptyMap()
}

private fun Map<String, Any?>.stringValue(vararg keys: String): String =
    keys.firstNotNullOfOrNull { key -> this[key]?.toString()?.takeIf(String::isNotBlank) }.orEmpty()

private fun Map<String, Any?>.expiryMillis(): Long {
    val expiresAt = stringValue("accessTokenExpiresAt", "expiresAt", "expiredAt")
    if (expiresAt.isNotBlank()) {
        runCatching { return OffsetDateTime.parse(expiresAt).toInstant().toEpochMilli() }
    }

    val expiresInSeconds = sequenceOf("expiresIn", "expires_in", "accessTokenExpiresIn")
        .firstNotNullOfOrNull { key -> this[key]?.toString()?.toDoubleOrNull() }

    return System.currentTimeMillis() + ((expiresInSeconds ?: DefaultTokenTtlSeconds) * 1000).toLong()
}

private const val DefaultTokenTtlSeconds = 3600.0
