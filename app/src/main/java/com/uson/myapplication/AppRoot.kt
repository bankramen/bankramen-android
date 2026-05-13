package com.uson.myapplication

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.core.auth.AuthRepository
import com.uson.myapplication.feature.home.HomeScreen
import com.uson.myapplication.feature.login.LoginRoute
import com.uson.myapplication.feature.login.PostLoginOnboardingScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RootUiState(
    val isAuthenticated: Boolean = false,
    val userId: String? = null,
    val showPostLoginOnboarding: Boolean = false,
)

class RootViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = AuthGraph.sessionManager(application.applicationContext)
    private val authRepository: AuthRepository = AuthGraph.authRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(RootUiState())
    val uiState: StateFlow<RootUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val session = sessionManager.currentSession()
        _uiState.update {
            it.copy(
                isAuthenticated = session != null,
                userId = session?.userId,
            )
        }
    }

    fun completeLogin() {
        val session = sessionManager.currentSession()
        _uiState.update {
            it.copy(
                isAuthenticated = session != null,
                userId = session?.userId,
                showPostLoginOnboarding = session != null,
            )
        }
    }

    fun finishPostLoginOnboarding() {
        _uiState.update {
            it.copy(showPostLoginOnboarding = false)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                RootUiState()
            }
        }
    }
}

@Composable
fun AppRoot(
    viewModel: RootViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.value.isAuthenticated && uiState.value.showPostLoginOnboarding) {
        PostLoginOnboardingScreen(
            onFinished = viewModel::finishPostLoginOnboarding,
            onSkip = viewModel::finishPostLoginOnboarding,
        )
    } else if (uiState.value.isAuthenticated) {
        HomeScreen(
            userId = uiState.value.userId,
            onLogoutClick = viewModel::logout,
        )
    } else {
        LoginRoute(
            onLoginCompleted = viewModel::completeLogin,
        )
    }
}
