package com.uson.myapplication.core.auth

interface AuthReissueGateway {
    suspend fun reissue(refreshToken: String): Result<AuthSession>
}
