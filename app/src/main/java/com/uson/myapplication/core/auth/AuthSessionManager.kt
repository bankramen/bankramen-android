package com.uson.myapplication.core.auth

class AuthSessionManager(
    private val sessionStore: AuthSessionStore,
    private val reissueGateway: AuthReissueGateway,
) {
    suspend fun bootstrap(nowMillis: Long = System.currentTimeMillis()): AuthBootstrapResult {
        val session = sessionStore.getSession() ?: return AuthBootstrapResult.LoggedOut

        if (!session.isAccessTokenExpired(nowMillis)) {
            return AuthBootstrapResult.Authenticated(source = "stored_access_token")
        }

        if (session.refreshToken.isBlank()) {
            sessionStore.clearSession()
            return AuthBootstrapResult.RefreshFailed(reason = "missing_refresh_token")
        }

        return reissueGateway.reissue(session.refreshToken)
            .fold(
                onSuccess = {
                    sessionStore.saveSession(it)
                    AuthBootstrapResult.Authenticated(source = "refresh_token")
                },
                onFailure = {
                sessionStore.clearSession()
                AuthBootstrapResult.RefreshFailed(reason = it.message ?: "unknown_refresh_failure")
                },
            )
    }

    fun currentSession(): AuthSession? = sessionStore.getSession()

    fun saveSession(session: AuthSession) {
        sessionStore.saveSession(session)
    }

    fun clearSession() {
        sessionStore.clearSession()
    }
}
