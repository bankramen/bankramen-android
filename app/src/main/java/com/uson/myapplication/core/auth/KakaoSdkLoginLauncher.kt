package com.uson.myapplication.core.auth

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.uson.myapplication.BuildConfig
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class KakaoSdkLoginLauncher : KakaoLoginLauncher {
    override suspend fun login(context: Context): Result<String> {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank()) {
            return Result.failure(IllegalStateException("Kakao native app key is missing"))
        }

        return suspendCoroutine { continuation ->
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                when {
                    error != null -> continuation.resume(Result.failure(error))
                    token != null -> continuation.resume(Result.success(token.accessToken))
                    else -> continuation.resume(Result.failure(IllegalStateException("Kakao login token is empty")))
                }
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resume(Result.failure(error))
                    } else if (error != null) {
                        UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                    } else {
                        callback(token, null)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            }
        }
    }
}
