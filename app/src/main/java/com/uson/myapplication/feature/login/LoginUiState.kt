package com.uson.myapplication.feature.login

data class LoginUiState(
    val authStatusLabel: String = "로그인 필요",
    val authStatusDetail: String = "저장된 세션이 없어요",
)
