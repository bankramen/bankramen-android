package com.uson.myapplication.core.api

import android.content.Context
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.core.auth.AuthHeaderInterceptor
import com.uson.myapplication.core.auth.AuthTokenAuthenticator
import com.uson.myapplication.generated.api.APIApi
import com.uson.myapplication.generated.api.CategoryApi
import com.uson.myapplication.generated.api.MonthlyReportApi
import com.uson.myapplication.generated.api.TransactionApi
import okhttp3.Authenticator
import okhttp3.Interceptor

object BankramenApiFactory {
    @Volatile
    private var appContextProvider: (() -> Context)? = null

    fun initialize(context: Context) {
        appContextProvider = { context.applicationContext }
    }

    fun createApiApi(includeSessionAuth: Boolean = true): APIApi = createService(
        serviceClass = APIApi::class.java,
        includeSessionAuth = includeSessionAuth,
    )

    fun createTransactionApi(): TransactionApi = createService(
        serviceClass = TransactionApi::class.java,
        includeSessionAuth = true,
    )

    fun createMonthlyReportApi(): MonthlyReportApi = createService(
        serviceClass = MonthlyReportApi::class.java,
        includeSessionAuth = true,
    )

    fun createCategoryApi(): CategoryApi = createService(
        serviceClass = CategoryApi::class.java,
        includeSessionAuth = true,
    )

    private fun <T> createService(
        serviceClass: Class<T>,
        includeSessionAuth: Boolean,
    ): T {
        val context = appContextProvider?.invoke()
        val sessionInterceptors = sessionInterceptors(
            context = context,
            includeSessionAuth = includeSessionAuth,
        )
        val authenticator = sessionAuthenticator(
            context = context,
            includeSessionAuth = includeSessionAuth,
        )

        return BankramenRetrofitBuilder.create(
            interceptors = sessionInterceptors,
            authenticator = authenticator,
        ).create(serviceClass)
    }

    private fun sessionInterceptors(
        context: Context?,
        includeSessionAuth: Boolean,
    ): List<Interceptor> {
        if (!includeSessionAuth || context == null) return emptyList()
        return listOf(AuthHeaderInterceptor(AuthGraph.sessionStore(context)))
    }

    private fun sessionAuthenticator(
        context: Context?,
        includeSessionAuth: Boolean,
    ): Authenticator? {
        if (!includeSessionAuth || context == null) return null
        return AuthTokenAuthenticator(AuthGraph.sessionManager(context))
    }
}
