package com.uson.myapplication.core.auth

import android.content.Context

object PendingKakaoLoginLauncher : KakaoLoginLauncher {
    override fun launch(context: Context): Result<Unit> = Result.failure(
        IllegalStateException("Kakao REST login launcher is not connected yet"),
    )
}
