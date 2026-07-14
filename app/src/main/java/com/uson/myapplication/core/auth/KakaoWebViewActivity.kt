package com.uson.myapplication.core.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.uson.myapplication.BuildConfig

class KakaoWebViewActivity : ComponentActivity() {

    private val stateStore by lazy { KakaoLoginStateStore.get(this) }
    private var handled = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loginUrl = intent.getStringExtra(EXTRA_LOGIN_URL)
        if (loginUrl.isNullOrBlank()) {
            stateStore.completeFailure("로그인 URL이 없어요")
            finish()
            return
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!handled) stateStore.completeFailure("로그인을 취소했어요")
                finish()
            }
        })

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (handled) return true
                    val url = request.url?.toString().orEmpty()
                    if (url.isCallbackUrl()) {
                        handled = true
                        handleCallbackUrl(request.url)
                        return true
                    }
                    return false
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    if (handled) return
                    val callbackUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/auth/kakao/callback"
                    if (url?.startsWith(callbackUrl) == true) {
                        val uri = Uri.parse(url)
                        if (uri.getQueryParameter("code").isNullOrBlank().not()) {
                            handled = true
                            handleCallbackUrl(uri)
                        } else {
                            view.evaluateJavascript("(function(){return document.body.innerText;})()") { raw: String? ->
                                if (!handled) {
                                    handled = true
                                    parseAndFinish(raw)
                                }
                            }
                        }
                    }
                }
            }
            loadUrl(loginUrl)
        }

        setContentView(webView)
    }

    private fun parseAndFinish(raw: String?) {
        runCatching {
            authLogDebug("webview parse raw=$raw")
            val text = normalizeJavascriptString(raw)
            val session = parseAuthSessionResponse(text)
            stateStore.completeTokenSuccess(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken.takeIf { it.isNotBlank() },
            )
        }.onFailure { e ->
            authLogError("webview parse failed", e)
            stateStore.completeFailure(e.message ?: "토큰 파싱 실패")
        }
        finish()
    }

    private fun handleCallbackUrl(uri: Uri) {
        authLogDebug("webview handleCallbackUrl uri=$uri")

        val error = uri.getQueryParameter("error")
        val errorDescription = uri.getQueryParameter("error_description")
        if (!error.isNullOrBlank()) {
            stateStore.completeFailure(
                listOfNotNull(error, errorDescription).joinToString(": "),
            )
            finish()
            return
        }

        val accessToken = uri.getQueryParameter("accessToken")
            ?: uri.getQueryParameter("access_token")
        val refreshToken = uri.getQueryParameter("refreshToken")
            ?: uri.getQueryParameter("refresh_token")
        if (!accessToken.isNullOrBlank()) {
            stateStore.completeTokenSuccess(accessToken = accessToken, refreshToken = refreshToken)
            finish()
            return
        }

        val authorizationCode = uri.getQueryParameter("code")
        val returnedState = uri.getQueryParameter("state")

        if (!authorizationCode.isNullOrBlank() && !returnedState.isNullOrBlank()) {
            stateStore.completeSuccess(
                authorizationCode = authorizationCode,
                state = returnedState,
                redirectUri = BuildConfig.KAKAO_REDIRECT_URI,
            )
            finish()
            return
        }

        stateStore.completeFailure("카카오 로그인 응답에서 code/state 또는 accessToken을 찾지 못했어요")
        finish()
    }

    private fun normalizeJavascriptString(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) error("서버 응답이 비어있어요")

        return when {
            value == "null" -> error("서버 응답이 비어있어요")
            value.startsWith("\"") -> runCatching {
                // evaluateJavascript wraps string results as a JSON string literal.
                com.google.gson.Gson().fromJson(value, String::class.java)
            }.getOrNull() ?: value.removeSurrounding("\"")
            else -> value
        }
    }

    companion object {
        private const val EXTRA_LOGIN_URL = "extra_login_url"

        fun createIntent(context: Context, loginUrl: String): Intent =
            Intent(context, KakaoWebViewActivity::class.java).apply {
                putExtra(EXTRA_LOGIN_URL, loginUrl)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
    }
}

private fun String.isCallbackUrl(): Boolean =
    startsWith(BuildConfig.API_BASE_URL.trimEnd('/') + "/auth/kakao/callback")
