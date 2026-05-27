package com.uson.myapplication.core.auth

import android.content.Context
import android.util.Log
import com.uson.myapplication.core.api.BankramenApiFactory
import com.uson.myapplication.generated.api.APIApi
import com.uson.myapplication.generated.model.DeviceTokenRequest
import java.util.UUID

class DeviceTokenRepository(
    context: Context,
    private val api: APIApi = BankramenApiFactory.createApiApi(),
    private val sessionManager: AuthSessionManager = AuthGraph.sessionManager(context),
) {
    suspend fun registerDeviceToken(token: String): Result<Unit> = runCatching {
        if (token.isBlank()) return@runCatching
        val userId = sessionManager.currentSession()?.userId
            ?.takeIf(String::isNotBlank)
            ?.let(UUID::fromString)
            ?: run {
                Log.d(AuthDebugTag, "skip device token registration: missing authenticated userId")
                return@runCatching
            }

        val response = api.saveDeviceToken(
            memberId = userId,
            deviceTokenRequest = DeviceTokenRequest(token = token),
        )
        if (!response.isSuccessful) {
            error("Device token registration failed: HTTP ${response.code()}")
        }
        Log.d(AuthDebugTag, "device token registered")
    }
}
