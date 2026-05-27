package com.uson.myapplication

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.core.auth.AuthRepository
import com.uson.myapplication.core.auth.DeviceTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
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
    private val appContext = application.applicationContext
    private val sessionManager = AuthGraph.sessionManager(appContext)
    private val authRepository: AuthRepository = AuthGraph.authRepository(appContext)
    private val deviceTokenRepository = DeviceTokenRepository(appContext)

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
        registerCurrentDeviceToken()
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

    private fun registerCurrentDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("DeviceToken", "Failed to fetch current token after login", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result.orEmpty()
            viewModelScope.launch {
                deviceTokenRepository.registerDeviceToken(token)
                    .onFailure { Log.w("DeviceToken", "Failed to register device token after login", it) }
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
