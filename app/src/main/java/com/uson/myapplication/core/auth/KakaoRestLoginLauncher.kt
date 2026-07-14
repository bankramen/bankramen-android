package com.uson.myapplication.core.auth

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import okhttp3.ResponseBody
import retrofit2.Response

class KakaoRestLoginLauncher(
    private val stateStore: KakaoLoginStateStore,
    private val authApi: AuthRawApi,
) : KakaoLoginLauncher {
    override suspend fun launch(context: Context): Result<Unit> = runCatching {
        authLogDebug("launch login start")
        val response = authApi.login()
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string().orEmpty()
            authLogError("Failed to issue Kakao login URL: code=${response.code()} message=${response.message()} body=$errorBody")
            error("카카오 로그인 URL 발급 실패: HTTP ${response.code()}")
        }
        val loginUrl = response.requireRawBody().loginUrl()
        authLogDebug("launch login parsedUrl=$loginUrl")
        val authorizeUri = Uri.parse(loginUrl)
        val state = authorizeUri.getQueryParameter("state").orEmpty()
        require(state.isNotBlank()) { "서버 로그인 URL에 state 값이 없어요" }

        stateStore.beginLogin(state)
        context.startActivity(KakaoWebViewActivity.createIntent(context, loginUrl))
    }.recoverCatching { exception ->
        authLogError("launch login failed", exception)
        if (exception is ActivityNotFoundException) {
            throw IllegalStateException("카카오 로그인에 사용할 브라우저를 찾지 못했어요", exception)
        }
        throw exception
    }
}

private fun Response<ResponseBody>.requireRawBody(): String =
    body()?.string().orEmpty()
        .also { authLogDebug("login rawBody=$it") }
        .ifBlank { error("서버 로그인 URL 응답이 비어 있어요") }

private fun String.loginUrl(): String = parseLoginUrlResponse(this)
