package com.uson.myapplication.core.auth

import android.content.Context

fun interface KakaoLoginLauncher {
    suspend fun launch(context: Context): Result<Unit>
}
