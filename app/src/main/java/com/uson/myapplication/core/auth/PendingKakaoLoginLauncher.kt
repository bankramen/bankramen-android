package com.uson.myapplication.core.auth

import android.content.Context

object PendingKakaoLoginLauncher : KakaoLoginLauncher {
    override suspend fun login(context: Context): Result<String> = Result.failure(
        IllegalStateException("Kakao SDK login is not connected yet"),
    )
}
