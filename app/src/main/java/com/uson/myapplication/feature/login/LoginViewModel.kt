package com.uson.myapplication.feature.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uson.myapplication.core.auth.AuthBootstrapResult
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.core.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val appContext = application.applicationContext
    private val sessionManager = AuthGraph.sessionManager(appContext)
    private val authRepository: AuthRepository = AuthGraph.authRepository(appContext)

    init {
        bootstrapSession()
    }

    fun startKakaoLogin(context: Context) {
        _uiState.update {
            it.copy(
                authStatusLabel = "카카오 로그인 진행 중",
                authStatusDetail = "브라우저에서 카카오 인증을 완료하면 앱으로 돌아와요",
            )
        }

        viewModelScope.launch {
            authRepository.startKakaoLogin(context)
                .onFailure { throwable ->
                    Log.e("LoginViewModel", "startKakaoLogin failed", throwable)
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "카카오 로그인 시작 실패",
                            authStatusDetail = throwable.message ?: "카카오 로그인 브라우저를 열지 못했어요",
                        )
                    }
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
                        )
                    }

                    result
                        .onSuccess {
                            _uiState.update {
                                it.copy(
                                    authStatusLabel = "카카오 로그인 완료",
                                    authStatusDetail = "인가 코드 기반 서버 세션 발급이 완료됐어요",
                                )
                            }
                            onSuccess()
                        }
                        .onFailure { throwable ->
                            Log.e("LoginViewModel", "consumePendingKakaoLogin failed", throwable)
                            _uiState.update {
                                it.copy(
                                    authStatusLabel = "카카오 로그인 실패",
                                    authStatusDetail = throwable.message ?: "카카오 로그인 중 오류가 발생했어요",
                                )
                            }
                        }
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
                        )
                    }
                }

                is AuthBootstrapResult.LoggedOut -> {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "로그인 필요",
                            authStatusDetail = "저장된 세션이 없어요",
                        )
                    }
                }

                is AuthBootstrapResult.RefreshFailed -> {
                    _uiState.update {
                        it.copy(
                            authStatusLabel = "세션 만료",
                            authStatusDetail = "리이슈 대기 상태예요: ${result.reason}",
                        )
                    }
                }
            }
        }
    }
}
