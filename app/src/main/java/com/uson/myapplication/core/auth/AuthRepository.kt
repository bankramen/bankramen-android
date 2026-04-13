package com.uson.myapplication.core.auth

import android.content.Context

class AuthRepository(
    private val loginGateway: AuthLoginGateway,
    private val sessionManager: AuthSessionManager,
    private val kakaoLoginLauncher: KakaoLoginLauncher,
) {
    suspend fun loginWithKakao(kakaoAccessToken: String): Result<AuthSession> =
        loginGateway.loginWithKakao(kakaoAccessToken)
            .onSuccess(sessionManager::saveSession)

    suspend fun loginWithKakaoSdk(context: Context): Result<AuthSession> {
        val kakaoTokenResult = kakaoLoginLauncher.login(context)
        val kakaoAccessToken = kakaoTokenResult.getOrElse { return Result.failure(it) }
        return loginWithKakao(kakaoAccessToken)
    }
}
