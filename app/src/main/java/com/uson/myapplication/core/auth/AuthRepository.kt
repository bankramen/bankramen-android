package com.uson.myapplication.core.auth

import android.content.Context

class AuthRepository(
    private val loginGateway: AuthLoginGateway,
    private val sessionManager: AuthSessionManager,
    private val kakaoLoginLauncher: KakaoLoginLauncher,
    private val kakaoLoginStateStore: KakaoLoginStateStore,
) {
    suspend fun loginWithKakao(request: KakaoAuthorizationCodeRequest): Result<AuthSession> =
        loginGateway.loginWithKakao(request)
            .onSuccess(sessionManager::saveSession)

    suspend fun startKakaoLogin(context: Context): Result<Unit> = kakaoLoginLauncher.launch(context)

    suspend fun logout(): Result<Unit> = sessionManager.logout()

    suspend fun completePendingKakaoLogin(): Result<AuthSession>? = when (val result = kakaoLoginStateStore.consumeResult()) {
        null -> null
        is KakaoLoginResult.Failure -> Result.failure(IllegalStateException(result.message))
        is KakaoLoginResult.DirectToken -> {
            val session = AuthSession(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken ?: "",
                accessTokenExpiresAtMillis = System.currentTimeMillis() + 3_600_000L,
                userId = null,
            )
            sessionManager.saveSession(session)
            Result.success(session)
        }
        is KakaoLoginResult.Success -> loginWithKakao(
            KakaoAuthorizationCodeRequest(
                authorizationCode = result.authorizationCode,
                redirectUri = result.redirectUri,
                state = result.state,
            ),
        )
    }
}
