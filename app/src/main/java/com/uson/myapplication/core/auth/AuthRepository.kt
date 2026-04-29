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

    fun startKakaoLogin(context: Context): Result<Unit> = kakaoLoginLauncher.launch(context)

    suspend fun completePendingKakaoLogin(): Result<AuthSession>? = when (val result = kakaoLoginStateStore.consumeResult()) {
        null -> null
        is KakaoLoginResult.Failure -> Result.failure(IllegalStateException(result.message))
        is KakaoLoginResult.Success -> loginWithKakao(
            KakaoAuthorizationCodeRequest(
                authorizationCode = result.authorizationCode,
                redirectUri = result.redirectUri,
                state = result.state,
            ),
        )
    }
}
