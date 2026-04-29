package com.uson.myapplication.core.auth

import android.content.Context

fun interface KakaoLoginLauncher {
    fun launch(context: Context): Result<Unit>
}
