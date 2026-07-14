package com.uson.myapplication.core.auth

import android.content.Intent
import android.net.Uri
import com.uson.myapplication.BuildConfig

class KakaoLoginCallbackHandler(
    private val stateStore: KakaoLoginStateStore,
) {
    fun handleIntent(intent: Intent?): Boolean {
        val redirectData = intent?.data ?: return false
        if (!redirectData.isExpectedRedirect()) return false
        authLogDebug("handleIntent redirect=$redirectData")

        val error = redirectData.getQueryParameter("error")
        val errorDescription = redirectData.getQueryParameter("error_description")

        if (!error.isNullOrBlank()) {
            stateStore.completeFailure(
                message = listOfNotNull(error, errorDescription).joinToString(separator = ": "),
            )
            return true
        }

        val accessToken = redirectData.getQueryParameter("accessToken")
            ?: redirectData.getQueryParameter("access_token")
        val refreshToken = redirectData.getQueryParameter("refreshToken")
            ?: redirectData.getQueryParameter("refresh_token")

        if (!accessToken.isNullOrBlank()) {
            stateStore.completeTokenSuccess(accessToken = accessToken, refreshToken = refreshToken)
            return true
        }

        val authorizationCode = redirectData.getQueryParameter("code")
        val returnedState = redirectData.getQueryParameter("state")
        val expectedState = stateStore.pendingState()
        authLogDebug("handleIntent codePresent=${!authorizationCode.isNullOrBlank()} returnedState=$returnedState expectedState=$expectedState accessTokenPresent=${!accessToken.isNullOrBlank()}")

        when {
            authorizationCode.isNullOrBlank() -> stateStore.completeFailure("카카오 인가 코드가 응답에 없어요")
            expectedState.isNullOrBlank() -> stateStore.completeFailure("진행 중인 카카오 로그인 요청을 찾지 못했어요")
            returnedState.isNullOrBlank() -> stateStore.completeFailure("카카오 로그인 state 값이 누락됐어요")
            returnedState != expectedState -> stateStore.completeFailure("카카오 로그인 state 검증에 실패했어요")
            else -> stateStore.completeSuccess(
                authorizationCode = authorizationCode,
                state = returnedState,
                redirectUri = BuildConfig.KAKAO_REDIRECT_URI,
            )
        }

        return true
    }

    private fun Uri.isExpectedRedirect(): Boolean {
        val expectedUri = runCatching { Uri.parse(BuildConfig.KAKAO_REDIRECT_URI) }.getOrNull() ?: return false

        return scheme == expectedUri.scheme &&
            host == expectedUri.host &&
            normalizedPath() == expectedUri.normalizedPath()
    }

    private fun Uri.normalizedPath(): String = path?.trimEnd('/') ?: ""
}
