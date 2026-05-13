package com.uson.myapplication.core.auth

class AuthSessionManager(
    private val sessionStore: AuthSessionStore,
    private val reissueGateway: AuthReissueGateway,
    private val logoutGateway: AuthLogoutGateway,
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

    suspend fun logout(): Result<Unit> {
        val refreshToken = sessionStore.getSession()?.refreshToken.orEmpty()

        val result = if (refreshToken.isBlank()) {
            Result.success(Unit)
        } else {
            logoutGateway.logout(refreshToken)
        }

        sessionStore.clearSession()
        return result
    }

    fun clearSession() {
        sessionStore.clearSession()
    }
}
