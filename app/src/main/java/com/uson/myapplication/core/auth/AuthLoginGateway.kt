package com.uson.myapplication.core.auth

interface AuthLoginGateway {
    suspend fun loginWithKakao(kakaoAccessToken: String): Result<AuthSession>
}
