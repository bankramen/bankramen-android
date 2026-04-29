package com.uson.myapplication.core.auth

data class KakaoAuthorizationCodeRequest(
    val authorizationCode: String,
    val redirectUri: String,
    val state: String? = null,
)
