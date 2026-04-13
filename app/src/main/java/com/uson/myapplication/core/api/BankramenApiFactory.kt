package com.uson.myapplication.core.api

import com.uson.myapplication.BuildConfig
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.core.auth.AuthHeaderInterceptor
import com.uson.myapplication.core.auth.AuthTokenAuthenticator
import com.uson.myapplication.generated.api.AuthApi
import com.uson.myapplication.generated.api.SystemApi
import com.uson.myapplication.generated.infrastructure.ApiClient
import okhttp3.OkHttpClient

object BankramenApiFactory {
    @Volatile
    private var appContextProvider: (() -> android.content.Context)? = null

    fun initialize(context: android.content.Context) {
        appContextProvider = { context.applicationContext }
    }

    fun createSystemApi(): SystemApi = createApiClient().createService(SystemApi::class.java)

    fun createAuthApi(): AuthApi = createApiClient().createService(AuthApi::class.java)

    private fun createApiClient(): ApiClient {
        val context = appContextProvider?.invoke()
        val okHttpClientBuilder = if (context != null) {
            OkHttpClient.Builder()
                .addInterceptor(AuthHeaderInterceptor(AuthGraph.sessionStore(context)))
                .authenticator(AuthTokenAuthenticator(AuthGraph.sessionManager(context)))
        } else {
            OkHttpClient.Builder()
        }

        val client = ApiClient(
            baseUrl = BuildConfig.API_BASE_URL,
            okHttpClientBuilder = okHttpClientBuilder,
        )
            .setLogger { }

        if (BuildConfig.API_AUTH_TOKEN.isNotBlank() && context == null) {
            val staticAuthorizationClient = ApiClient(baseUrl = BuildConfig.API_BASE_URL)
                .setLogger { }
            return staticAuthorizationClient.addAuthorization(
                authName = "bearer",
                authorization = okhttp3.Interceptor { chain ->
                    val authenticatedRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer ${BuildConfig.API_AUTH_TOKEN}")
                        .build()
                    chain.proceed(authenticatedRequest)
                },
            )
        }

        return client
    }
}
