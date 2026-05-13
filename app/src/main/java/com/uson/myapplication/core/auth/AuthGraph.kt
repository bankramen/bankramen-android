package com.uson.myapplication.core.auth

import android.content.Context
import com.uson.myapplication.core.api.BankramenApiFactory

object AuthGraph {
    fun sessionStore(context: Context): AuthSessionStore = AuthSessionStore.get(context)

    fun kakaoLoginStateStore(context: Context): KakaoLoginStateStore = KakaoLoginStateStore.get(context)

    fun kakaoLoginLauncher(context: Context): KakaoLoginLauncher = KakaoRestLoginLauncher(
        stateStore = kakaoLoginStateStore(context),
        apiApi = BankramenApiFactory.createApiApi(includeSessionAuth = false),
    )

    fun kakaoLoginCallbackHandler(context: Context): KakaoLoginCallbackHandler = KakaoLoginCallbackHandler(
        stateStore = kakaoLoginStateStore(context),
    )

    fun sessionManager(context: Context): AuthSessionManager = AuthSessionManager(
        sessionStore = sessionStore(context),
        reissueGateway = GeneratedAuthGateway(BankramenApiFactory.createApiApi(includeSessionAuth = false)),
        logoutGateway = GeneratedAuthGateway(BankramenApiFactory.createApiApi(includeSessionAuth = false)),
    )

    fun authRepository(context: Context): AuthRepository = AuthRepository(
        loginGateway = GeneratedAuthGateway(BankramenApiFactory.createApiApi(includeSessionAuth = false)),
        sessionManager = sessionManager(context),
        kakaoLoginLauncher = kakaoLoginLauncher(context),
        kakaoLoginStateStore = kakaoLoginStateStore(context),
    )
}
