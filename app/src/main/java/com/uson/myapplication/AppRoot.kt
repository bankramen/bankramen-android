package com.uson.myapplication

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.feature.home.HomeScreen
import com.uson.myapplication.feature.login.LoginRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RootUiState(
    val isAuthenticated: Boolean = false,
    val userId: String? = null,
)

class RootViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = AuthGraph.sessionManager(application.applicationContext)

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

    fun logout() {
        sessionManager.clearSession()
        refresh()
    }
}

@Composable
fun AppRoot(
    viewModel: RootViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.value.isAuthenticated) {
        HomeScreen(
            userId = uiState.value.userId,
            onLogoutClick = viewModel::logout,
        )
    } else {
        LoginRoute(onLoginCompleted = viewModel::refresh)
    }
}
