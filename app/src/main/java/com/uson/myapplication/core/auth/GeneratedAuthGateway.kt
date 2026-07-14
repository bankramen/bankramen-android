package com.uson.myapplication.core.auth

import okhttp3.ResponseBody
import com.uson.myapplication.generated.model.TokenRequest
import retrofit2.Response

class GeneratedAuthGateway(
    private val authApi: AuthRawApi,
) : AuthReissueGateway, AuthLoginGateway, AuthLogoutGateway {
    override suspend fun reissue(refreshToken: String): Result<AuthSession> = runCatching {
        authLogDebug("reissue request refreshTokenPresent=${refreshToken.isNotBlank()}")
        authApi.reissue(tokenRequest = TokenRequest(refreshToken = refreshToken))
            .requireSuccess("Token reissue failed")
            .toAuthSession()
    }

    override suspend fun loginWithKakao(request: KakaoAuthorizationCodeRequest): Result<AuthSession> = runCatching {
        authLogDebug("loginWithKakao callback request codePresent=${request.authorizationCode.isNotBlank()} state=${request.state.orEmpty()}")
        authApi.callback(code = request.authorizationCode, state = request.state.orEmpty())
            .requireSuccess("Kakao callback failed")
            .toAuthSession()
    }

    override suspend fun logout(refreshToken: String): Result<Unit> = runCatching {
        authLogDebug("logout request refreshTokenPresent=${refreshToken.isNotBlank()}")
        authApi.logout(tokenRequest = TokenRequest(refreshToken = refreshToken))
            .requireSuccess("Logout failed")
    }
}

private fun Response<ResponseBody>.requireSuccess(message: String): String {
    if (!isSuccessful) {
        val errorBody = errorBody()?.string().orEmpty()
        authLogError("$message errorBody=$errorBody")
        error("$message: HTTP ${code()} ${errorBody.ifBlank { message() }}")
    }
    return body()?.string().orEmpty().also {
        authLogDebug("$message successBody=$it")
    }
}

private fun String.toAuthSession(): AuthSession = parseAuthSessionResponse(this)
