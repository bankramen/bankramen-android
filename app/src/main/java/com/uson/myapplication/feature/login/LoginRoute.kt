package com.uson.myapplication.feature.login

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uson.myapplication.core.notification.NotificationAccessManager

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = viewModel(),
    onLoginCompleted: () -> Unit = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshOverview()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LoginScreen(
        uiState = uiState.value,
        onPrimaryClick = {
            if (!uiState.value.notificationAccessGranted) {
                context.startActivity(NotificationAccessManager.createSettingsIntent())
            } else if (uiState.value.isAuthenticated) {
                viewModel.onAuthenticatedEntry()
                onLoginCompleted()
                Toast.makeText(context, "자동 로그인 세션이 준비되어 있어요.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.loginWithKakaoSdk(context = context, onSuccess = onLoginCompleted)
                Toast.makeText(context, "카카오 로그인 흐름을 시작합니다.", Toast.LENGTH_SHORT).show()
            }
        },
        onApiHealthRefresh = viewModel::refreshApiHealth,
    )
}
