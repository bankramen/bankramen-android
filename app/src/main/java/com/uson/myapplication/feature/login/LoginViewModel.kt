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
                authContractLabel = "REST 계약 준비 완료 (/api/v1/auth/kakao/login, /api/v1/auth/token/reissue)",
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

    fun loginWithPreparedContract(
        kakaoAccessToken: String = "placeholder-kakao-access-token",
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            authRepository.loginWithKakao(kakaoAccessToken)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "로그인 성공 형식 확인",
                            authStatusDetail = "백엔드가 준비되면 같은 계약으로 실제 로그인돼요",
                            isAuthenticated = true,
                        )
                    }
                    onSuccess()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "연결 대기",
                            authStatusDetail = "REST 계약은 준비됐고 백엔드 연결만 남았어요",
                            isAuthenticated = false,
                        )
                    }
                }
        }
    }

    fun loginWithKakaoSdk(
        context: Context,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            authRepository.loginWithKakaoSdk(context)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "카카오 로그인 준비 완료",
                            authStatusDetail = "SDK와 백엔드가 연결되면 바로 홈으로 이동해요",
                            isAuthenticated = true,
                        )
                    }
                    onSuccess()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "카카오 SDK 연결 대기",
                            authStatusDetail = "SDK 키와 실제 Kakao 로그인 연결만 남았어요",
                            isAuthenticated = false,
                        )
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
