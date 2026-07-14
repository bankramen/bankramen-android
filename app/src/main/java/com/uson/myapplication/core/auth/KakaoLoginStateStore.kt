package com.uson.myapplication.core.auth

import android.content.Context
import android.content.SharedPreferences

class KakaoLoginStateStore private constructor(
    context: Context,
) {
    private val preferences: SharedPreferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )

    fun beginLogin(state: String) {
        preferences.edit()
            .putString(KeyPendingState, state)
            .remove(KeyResultType)
            .remove(KeyAuthorizationCode)
            .remove(KeyResultState)
            .remove(KeyErrorMessage)
            .apply()
    }

    fun pendingState(): String? = preferences.getString(KeyPendingState, null)

    fun completeSuccess(
        authorizationCode: String,
        state: String,
        redirectUri: String,
    ) {
        preferences.edit()
            .putString(KeyResultType, ResultTypeSuccess)
            .putString(KeyAuthorizationCode, authorizationCode)
            .putString(KeyResultState, state)
            .putString(KeyRedirectUri, redirectUri)
            .remove(KeyErrorMessage)
            .remove(KeyPendingState)
            .apply()
    }

    fun completeTokenSuccess(accessToken: String, refreshToken: String?) {
        preferences.edit()
            .putString(KeyResultType, ResultTypeDirectToken)
            .putString(KeyAccessToken, accessToken)
            .apply { refreshToken?.let { putString(KeyRefreshToken, it) } ?: remove(KeyRefreshToken) }
            .remove(KeyAuthorizationCode)
            .remove(KeyResultState)
            .remove(KeyRedirectUri)
            .remove(KeyErrorMessage)
            .remove(KeyPendingState)
            .apply()
    }

    fun completeFailure(message: String) {
        preferences.edit()
            .putString(KeyResultType, ResultTypeFailure)
            .putString(KeyErrorMessage, message)
            .remove(KeyAuthorizationCode)
            .remove(KeyResultState)
            .remove(KeyPendingState)
            .apply()
    }

    fun consumeResult(): KakaoLoginResult? {
        val resultType = preferences.getString(KeyResultType, null) ?: return null
        val result = when (resultType) {
            ResultTypeDirectToken -> {
                val accessToken = preferences.getString(KeyAccessToken, null)
                if (accessToken.isNullOrBlank()) {
                    KakaoLoginResult.Failure(message = "accessToken이 없어요")
                } else {
                    KakaoLoginResult.DirectToken(
                        accessToken = accessToken,
                        refreshToken = preferences.getString(KeyRefreshToken, null),
                    )
                }
            }

            ResultTypeSuccess -> {
                val authorizationCode = preferences.getString(KeyAuthorizationCode, null)
                val redirectUri = preferences.getString(KeyRedirectUri, null)
                val state = preferences.getString(KeyResultState, null)

                if (authorizationCode.isNullOrBlank() || redirectUri.isNullOrBlank() || state.isNullOrBlank()) {
                    KakaoLoginResult.Failure(message = "카카오 인가 코드 응답이 올바르지 않아요")
                } else {
                    KakaoLoginResult.Success(
                        authorizationCode = authorizationCode,
                        redirectUri = redirectUri,
                        state = state,
                    )
                }
            }

            ResultTypeFailure -> KakaoLoginResult.Failure(
                message = preferences.getString(KeyErrorMessage, null)
                    ?: "카카오 로그인 콜백 처리에 실패했어요",
            )

            else -> null
        }

        preferences.edit()
            .remove(KeyResultType)
            .remove(KeyAuthorizationCode)
            .remove(KeyResultState)
            .remove(KeyRedirectUri)
            .remove(KeyErrorMessage)
            .apply()

        return result
    }

    companion object {
        private const val PreferencesName = "bankramen_kakao_login"
        private const val KeyPendingState = "pending_state"
        private const val KeyResultType = "result_type"
        private const val KeyAuthorizationCode = "authorization_code"
        private const val KeyResultState = "result_state"
        private const val KeyRedirectUri = "redirect_uri"
        private const val KeyErrorMessage = "error_message"

        private const val ResultTypeSuccess = "success"
        private const val ResultTypeDirectToken = "direct_token"
        private const val ResultTypeFailure = "failure"
        private const val KeyAccessToken = "access_token"
        private const val KeyRefreshToken = "refresh_token"

        @Volatile
        private var instance: KakaoLoginStateStore? = null

        fun get(context: Context): KakaoLoginStateStore = instance ?: synchronized(this) {
            instance ?: KakaoLoginStateStore(context.applicationContext).also { instance = it }
        }
    }
}
