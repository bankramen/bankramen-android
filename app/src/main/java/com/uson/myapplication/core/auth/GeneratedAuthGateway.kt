package com.uson.myapplication.core.auth

import com.uson.myapplication.generated.api.AuthApi
import com.uson.myapplication.generated.model.AuthSessionResponse
import com.uson.myapplication.generated.model.KakaoLoginRequest
import com.uson.myapplication.generated.model.TokenReissueRequest
import java.time.OffsetDateTime

class GeneratedAuthGateway(
    private val authApi: AuthApi,
) : AuthReissueGateway, AuthLoginGateway {
    override suspend fun reissue(refreshToken: String): Result<AuthSession> = runCatching {
        val response = authApi.reissueToken(
            tokenReissueRequest = TokenReissueRequest(refreshToken = refreshToken),
        )
        val body = response.body() ?: error("Token reissue response body is empty")
        body.toAuthSession()
    }

    override suspend fun loginWithKakao(request: KakaoAuthorizationCodeRequest): Result<AuthSession> = runCatching {
        val response = authApi.loginWithKakao(
            kakaoLoginRequest = KakaoLoginRequest(
                authorizationCode = request.authorizationCode,
                redirectUri = request.redirectUri,
                state = request.state,
            ),
        )
        val body = response.body() ?: error("Kakao login response body is empty")
        body.toAuthSession()
    }
}

private fun AuthSessionResponse.toAuthSession(): AuthSession = AuthSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    accessTokenExpiresAtMillis = accessTokenExpiresAt.toEpochMillis(),
    userId = userId,
)

private fun OffsetDateTime.toEpochMillis(): Long = toInstant().toEpochMilli()
