package com.uson.myapplication.core.auth

sealed interface AuthBootstrapResult {
    data object LoggedOut : AuthBootstrapResult
    data class Authenticated(val source: String) : AuthBootstrapResult
    data class RefreshFailed(val reason: String) : AuthBootstrapResult
}
