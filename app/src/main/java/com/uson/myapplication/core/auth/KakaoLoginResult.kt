package com.uson.myapplication.core.auth

sealed interface KakaoLoginResult {
    data class Success(
        val authorizationCode: String,
        val redirectUri: String,
        val state: String,
    ) : KakaoLoginResult

    data class Failure(
        val message: String,
    ) : KakaoLoginResult
}
