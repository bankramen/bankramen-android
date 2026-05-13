package com.uson.myapplication.core.api

import com.uson.myapplication.BuildConfig
import com.uson.myapplication.generated.infrastructure.Serializer
import okhttp3.Authenticator
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.InetAddress
import java.net.URI

object BankramenRetrofitBuilder {
    fun create(
        interceptors: List<Interceptor> = emptyList(),
        authenticator: Authenticator? = null,
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .dns(BankramenDns)
            .addInterceptor(BankramenApiInterceptor())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        },
                    )
                }
                interceptors.forEach(::addInterceptor)
                if (authenticator != null) {
                    authenticator(authenticator)
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(requireBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(Serializer.gsonBuilder.create()))
            .build()
    }

    private fun requireBaseUrl(): String {
        val baseUrl = BuildConfig.API_BASE_URL.trim()
        require(baseUrl.isNotBlank()) {
            "api.baseUrl must be set in local.properties"
        }
        return if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    }
}

private object BankramenDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val overrideIp = BuildConfig.API_HOST_IP.trim()
        val apiHost = runCatching { URI(BankramenRetrofitBuilderBaseUrl.value).host }.getOrNull()

        if (overrideIp.isNotBlank() && hostname == apiHost) {
            return InetAddress.getAllByName(overrideIp).toList()
        }

        return Dns.SYSTEM.lookup(hostname)
    }
}

private object BankramenRetrofitBuilderBaseUrl {
    val value: String
        get() = BuildConfig.API_BASE_URL.trim()
}
