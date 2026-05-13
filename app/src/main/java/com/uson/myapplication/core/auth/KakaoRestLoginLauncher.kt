package com.uson.myapplication.core.auth

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.uson.myapplication.BuildConfig
import com.uson.myapplication.generated.api.APIApi

class KakaoRestLoginLauncher(
    private val stateStore: KakaoLoginStateStore,
    private val apiApi: APIApi,
) : KakaoLoginLauncher {
    override suspend fun launch(context: Context): Result<Unit> = runCatching {
        val appRedirectUri = BuildConfig.KAKAO_REDIRECT_URI
        require(appRedirectUri.isNotBlank()) { "kakao.redirectUri 설정이 필요해요" }

        val response = apiApi.login()
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string().orEmpty()
            Log.e(
                "KakaoRestLoginLauncher",
                "Failed to issue Kakao login URL: code=${response.code()} message=${response.message()} body=$errorBody",
            )
            error("카카오 로그인 URL 발급 실패: HTTP ${response.code()}")
        }
        val loginUrl = response.body().loginUrl()
        val authorizeUri = Uri.parse(loginUrl)
        val state = authorizeUri.getQueryParameter("state").orEmpty()
        require(state.isNotBlank()) { "서버 로그인 URL에 state 값이 없어요" }

        stateStore.beginLogin(state)
        context.startActivity(
            Intent(Intent.ACTION_VIEW, authorizeUri.withAppRedirect(appRedirectUri)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }.recoverCatching { exception ->
        if (exception is ActivityNotFoundException) {
            throw IllegalStateException("카카오 로그인에 사용할 브라우저를 찾지 못했어요", exception)
        }
        throw exception
    }
}

private fun Any?.loginUrl(): String {
    val fields = this.asObjectMap()
    return fields.stringValue("loginUrl", "login_url", "url")
        .ifBlank { error("서버 로그인 URL 응답이 올바르지 않아요") }
}

private fun Any?.asObjectMap(): Map<String, Any?> = when (this) {
    is Map<*, *> -> entries.associate { (key, value) -> key.toString() to value }
    else -> emptyMap()
}

private fun Map<String, Any?>.stringValue(vararg keys: String): String =
    keys.firstNotNullOfOrNull { key -> this[key]?.toString()?.takeIf(String::isNotBlank) }.orEmpty()

private fun Uri.withAppRedirect(appRedirectUri: String): Uri = buildUpon()
    .clearQuery()
    .appendQueryParameter("response_type", getQueryParameter("response_type").orEmpty())
    .appendQueryParameter("client_id", getQueryParameter("client_id").orEmpty())
    .appendQueryParameter("redirect_uri", appRedirectUri)
    .appendQueryParameter("state", getQueryParameter("state").orEmpty())
    .build()
