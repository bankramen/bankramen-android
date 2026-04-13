package com.uson.myapplication.core.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(
    private val sessionStore: AuthSessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = sessionStore.getSession()?.accessToken

        if (accessToken.isNullOrBlank()) {
            return chain.proceed(chain.request())
        }

        val authenticatedRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
