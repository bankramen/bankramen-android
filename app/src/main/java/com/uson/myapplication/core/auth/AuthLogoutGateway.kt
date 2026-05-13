package com.uson.myapplication.core.auth

interface AuthLogoutGateway {
    suspend fun logout(refreshToken: String): Result<Unit>
}
