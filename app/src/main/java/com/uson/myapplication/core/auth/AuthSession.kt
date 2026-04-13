package com.uson.myapplication.core.auth

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAtMillis: Long,
    val userId: String? = null,
) {
    fun isAccessTokenExpired(nowMillis: Long): Boolean = accessTokenExpiresAtMillis <= nowMillis
}
