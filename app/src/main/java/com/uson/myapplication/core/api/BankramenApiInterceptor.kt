package com.uson.myapplication.core.api

import okhttp3.Interceptor
import okhttp3.Response

class BankramenApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .build()

        return chain.proceed(request)
    }
}
