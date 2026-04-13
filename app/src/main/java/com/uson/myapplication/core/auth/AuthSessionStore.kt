package com.uson.myapplication.core.auth

import android.content.Context
import android.content.SharedPreferences

class AuthSessionStore private constructor(
    context: Context,
) {
    private val preferences: SharedPreferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )

    fun getSession(): AuthSession? {
        val accessToken = preferences.getString(KeyAccessToken, null) ?: return null
        val refreshToken = preferences.getString(KeyRefreshToken, null) ?: return null
        val expiresAt = preferences.getLong(KeyAccessTokenExpiresAt, 0L)

        return AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresAtMillis = expiresAt,
            userId = preferences.getString(KeyUserId, null),
        )
    }

    fun saveSession(session: AuthSession) {
        preferences.edit()
            .putString(KeyAccessToken, session.accessToken)
            .putString(KeyRefreshToken, session.refreshToken)
            .putLong(KeyAccessTokenExpiresAt, session.accessTokenExpiresAtMillis)
            .putString(KeyUserId, session.userId)
            .apply()
    }

    fun clearSession() {
        preferences.edit()
            .remove(KeyAccessToken)
            .remove(KeyRefreshToken)
            .remove(KeyAccessTokenExpiresAt)
            .remove(KeyUserId)
            .apply()
    }

    companion object {
        private const val PreferencesName = "bankramen_auth"
        private const val KeyAccessToken = "access_token"
        private const val KeyRefreshToken = "refresh_token"
        private const val KeyAccessTokenExpiresAt = "access_token_expires_at"
        private const val KeyUserId = "user_id"

        @Volatile
        private var instance: AuthSessionStore? = null

        fun get(context: Context): AuthSessionStore = instance ?: synchronized(this) {
            instance ?: AuthSessionStore(context.applicationContext).also { instance = it }
        }
    }
}
