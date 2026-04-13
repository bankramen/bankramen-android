package com.uson.myapplication.core.auth

import android.content.Context
import com.uson.myapplication.core.api.BankramenApiFactory

object AuthGraph {
    fun sessionStore(context: Context): AuthSessionStore = AuthSessionStore.get(context)

    fun kakaoLoginLauncher(): KakaoLoginLauncher = KakaoSdkLoginLauncher()

    fun sessionManager(context: Context): AuthSessionManager = AuthSessionManager(
        sessionStore = sessionStore(context),
        reissueGateway = GeneratedAuthGateway(BankramenApiFactory.createAuthApi()),
    )

    fun authRepository(context: Context): AuthRepository = AuthRepository(
        loginGateway = GeneratedAuthGateway(BankramenApiFactory.createAuthApi()),
        sessionManager = sessionManager(context),
        kakaoLoginLauncher = kakaoLoginLauncher(),
    )
}
