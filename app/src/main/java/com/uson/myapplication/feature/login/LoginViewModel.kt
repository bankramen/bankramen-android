package com.uson.myapplication.feature.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uson.myapplication.core.api.BankramenHealthRepository
import com.uson.myapplication.core.auth.AuthBootstrapResult
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.core.auth.AuthRepository
import com.uson.myapplication.core.notification.NotificationAccessManager
import com.uson.myapplication.core.notification.NotificationDebugRepository
import com.uson.myapplication.core.notification.NotificationStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val appContext = application.applicationContext
    private val notificationStore = NotificationStore.get(appContext)
    private val healthRepository = BankramenHealthRepository()
    private val sessionManager = AuthGraph.sessionManager(appContext)
    private val authRepository: AuthRepository = AuthGraph.authRepository(appContext)

    init {
        observeNotificationSummary()
        refreshOverview()
        bootstrapSession()
        refreshApiHealth()
    }

    fun refreshOverview() {
        _uiState.update {
            it.copy(
                notificationAccessGranted = NotificationAccessManager.hasAccess(appContext),
                notificationDebugPath = NotificationDebugRepository.debugFilePath(appContext),
                authContractLabel = "인가 코드 계약 준비 완료 (/api/v1/auth/kakao/login, /api/v1/auth/token/reissue)",
            )
        }
    }

    fun refreshApiHealth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingApi = true) }

            healthRepository.checkHealth()
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            apiHealthLabel = if (result.isHealthy) "정상 (${result.statusLabel})" else "확인 필요 (${result.statusLabel})",
                            apiServerTime = result.serverTime,
                            isCheckingApi = false,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            apiHealthLabel = "연결 실패",
                            apiServerTime = null,
                            isCheckingApi = false,
                        )
                    }
            }
        }
    }

    fun onAuthenticatedEntry() {
        _uiState.update {
            it.copy(
                authStatusLabel = "자동 로그인 준비됨",
                authStatusDetail = "실제 홈 화면 연결 전 단계예요",
                isAuthenticated = true,
            )
        }
    }

    fun startKakaoLogin(context: Context) {
        _uiState.update {
            it.copy(
                authStatusLabel = "카카오 로그인 진행 중",
                authStatusDetail = "브라우저에서 카카오 인증을 완료하면 앱으로 돌아와요",
                isAuthenticated = false,
            )
        }

        authRepository.startKakaoLogin(context)
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        authStatusLabel = "카카오 로그인 시작 실패",
                        authStatusDetail = throwable.message ?: "카카오 로그인 브라우저를 열지 못했어요",
                        isAuthenticated = false,
                    )
                }
            }
    }

    fun consumePendingKakaoLogin(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = authRepository.completePendingKakaoLogin()) {
                null -> Unit
                else -> {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "카카오 로그인 확인 중",
                            authStatusDetail = "인가 코드를 서버 로그인 요청으로 교환하고 있어요",
                            isAuthenticated = false,
                        )
                    }

                    result
                        .onSuccess {
                            _uiState.update {
                                it.copy(
                                    authStatusLabel = "카카오 로그인 완료",
                                    authStatusDetail = "인가 코드 기반 서버 세션 발급이 완료됐어요",
                                    isAuthenticated = true,
                                )
                            }
                            onSuccess()
                        }
                        .onFailure { throwable ->
                            _uiState.update {
                                it.copy(
                                    authStatusLabel = "카카오 로그인 실패",
                                    authStatusDetail = throwable.message ?: "카카오 로그인 중 오류가 발생했어요",
                                    isAuthenticated = false,
                                )
                            }
                        }
                }
            }
        }
    }

    private fun observeNotificationSummary() {
        viewModelScope.launch {
            notificationStore.summary.collect { summary ->
                _uiState.update {
                    it.copy(
                        parsedNotificationCount = summary.count,
                        latestMerchant = summary.latestMerchant,
                        latestAmountLabel = summary.latestAmount?.let(::formatAmount).orEmpty(),
                    )
                }
            }
        }
    }

    private fun bootstrapSession() {
        viewModelScope.launch {
            when (val result = sessionManager.bootstrap()) {
                is AuthBootstrapResult.Authenticated -> {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "자동 로그인 가능",
                            authStatusDetail = "${result.source} 기반 세션을 찾았어요",
                            isAuthenticated = true,
                        )
                    }
                }

                is AuthBootstrapResult.LoggedOut -> {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "로그인 필요",
                            authStatusDetail = "저장된 세션이 없어요",
                            isAuthenticated = false,
                        )
                    }
                }

                is AuthBootstrapResult.RefreshFailed -> {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "세션 만료",
                            authStatusDetail = "리이슈 대기 상태예요: ${result.reason}",
                            isAuthenticated = false,
                        )
                    }
                }
            }
        }
    }

    private fun formatAmount(amount: Long): String = "${amount}원"
}
