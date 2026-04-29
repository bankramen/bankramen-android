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
@Composable
fun LoginRoute(
    viewModel: LoginViewModel = viewModel(),
    onLoginCompleted: () -> Unit = {},
    onOpenShowcase: () -> Unit = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshOverview()
                viewModel.consumePendingKakaoLogin(onSuccess = onLoginCompleted)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AuthLandingScreen(
        uiState = uiState.value,
        onOpenShowcase = onOpenShowcase,
        onPrimaryClick = {
            if (uiState.value.isAuthenticated) {
                viewModel.onAuthenticatedEntry()
                onLoginCompleted()
                Toast.makeText(context, "자동 로그인 세션이 준비되어 있어요.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.startKakaoLogin(context = context)
                Toast.makeText(context, "카카오 인증 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            }
        },
    )
}
