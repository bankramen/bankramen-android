package com.uson.myapplication.core.auth

import java.util.UUID

internal object TemporaryAuthSessionFactory {
    private const val TemporaryUserId = "debug-kakao-user"
    private const val TemporarySessionLifetimeMillis = 30L * 24L * 60L * 60L * 1000L

    fun create(nowMillis: Long = System.currentTimeMillis()): AuthSession = AuthSession(
        accessToken = "debug-access-${UUID.randomUUID()}",
        refreshToken = "debug-refresh-${UUID.randomUUID()}",
        accessTokenExpiresAtMillis = nowMillis + TemporarySessionLifetimeMillis,
        userId = TemporaryUserId,
    )
}
