package com.uson.myapplication.core.auth

object PendingAuthReissueGateway : AuthReissueGateway {
    override suspend fun reissue(refreshToken: String): Result<AuthSession> = Result.failure(
        IllegalStateException("Auth reissue API is not connected yet"),
    )
}
