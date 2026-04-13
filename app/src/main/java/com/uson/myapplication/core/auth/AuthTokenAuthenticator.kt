package com.uson.myapplication.core.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthTokenAuthenticator(
    private val sessionManager: AuthSessionManager,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MaxRetryCount) return null

        val currentSession = sessionManager.currentSession() ?: return null
        if (currentSession.refreshToken.isBlank()) return null

        val bootstrapResult = runBlocking {
            sessionManager.bootstrap()
        }

        val refreshedSession = sessionManager.currentSession() ?: return null
        if (bootstrapResult !is AuthBootstrapResult.Authenticated) return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshedSession.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var currentResponse: Response? = response
        var count = 1

        while (currentResponse?.priorResponse != null) {
            count += 1
            currentResponse = currentResponse.priorResponse
        }

        return count
    }

    private companion object {
        const val MaxRetryCount = 2
    }
}
