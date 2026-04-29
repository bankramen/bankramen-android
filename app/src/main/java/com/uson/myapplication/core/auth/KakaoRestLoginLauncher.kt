package com.uson.myapplication.core.auth

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.uson.myapplication.BuildConfig
import java.security.SecureRandom

class KakaoRestLoginLauncher(
    private val stateStore: KakaoLoginStateStore,
) : KakaoLoginLauncher {
    override fun launch(context: Context): Result<Unit> = runCatching {
        val restApiKey = BuildConfig.KAKAO_REST_API_KEY
        val redirectUri = BuildConfig.KAKAO_REDIRECT_URI

        require(restApiKey.isNotBlank()) { "kakao.restApiKey 설정이 필요해요" }
        require(redirectUri.isNotBlank()) { "kakao.redirectUri 설정이 필요해요" }

        val state = generateState()
        stateStore.beginLogin(state)

        val authorizeUri = Uri.parse(KakaoAuthorizeUrl)
            .buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", restApiKey)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("state", state)
            .build()

        val intent = Intent(Intent.ACTION_VIEW, authorizeUri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            throw IllegalStateException("카카오 로그인에 사용할 브라우저를 찾지 못했어요", exception)
        }
    }

    private fun generateState(): String {
        val randomBytes = ByteArray(StateByteLength).also(SecureRandom()::nextBytes)
        return Base64.encodeToString(randomBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private companion object {
        const val KakaoAuthorizeUrl = "https://kauth.kakao.com/oauth/authorize"
        const val StateByteLength = 32
    }
}
