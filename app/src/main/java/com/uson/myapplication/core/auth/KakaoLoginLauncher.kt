package com.uson.myapplication.core.auth

import android.content.Context

fun interface KakaoLoginLauncher {
    suspend fun login(context: Context): Result<String>
}
