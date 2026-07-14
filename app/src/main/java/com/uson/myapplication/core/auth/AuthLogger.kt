package com.uson.myapplication.core.auth

import android.util.Log

internal fun authLogDebug(message: String) {
    runCatching { Log.d(AuthDebugTag, message) }
}

internal fun authLogWarn(message: String, throwable: Throwable? = null) {
    runCatching { Log.w(AuthDebugTag, message, throwable) }
}

internal fun authLogError(message: String, throwable: Throwable? = null) {
    runCatching { Log.e(AuthDebugTag, message, throwable) }
}
