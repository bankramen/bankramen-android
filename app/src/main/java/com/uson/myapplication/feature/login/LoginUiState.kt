package com.uson.myapplication.feature.login

data class LoginUiState(
    val notificationAccessGranted: Boolean = false,
    val notificationDebugPath: String = "",
    val parsedNotificationCount: Int = 0,
    val latestMerchant: String? = null,
    val latestAmountLabel: String = "",
    val apiHealthLabel: String = "연결 전",
    val apiServerTime: String? = null,
    val isCheckingApi: Boolean = false,
    val authStatusLabel: String = "로그인 필요",
    val authStatusDetail: String = "저장된 세션이 없어요",
    val isAuthenticated: Boolean = false,
    val authContractLabel: String = "REST 계약 준비 전",
)
