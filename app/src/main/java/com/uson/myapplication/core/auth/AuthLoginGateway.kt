package com.uson.myapplication.core.auth

interface AuthLoginGateway {
    suspend fun loginWithKakao(request: KakaoAuthorizationCodeRequest): Result<AuthSession>
}
