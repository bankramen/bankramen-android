package com.uson.myapplication.core.api

import com.uson.myapplication.generated.model.HealthResponse

data class HealthCheckResult(
    val isHealthy: Boolean,
    val statusLabel: String,
    val serverTime: String?,
)

class BankramenHealthRepository(
    private val systemApi: com.uson.myapplication.generated.api.SystemApi = BankramenApiFactory.createSystemApi(),
) {
    suspend fun checkHealth(): Result<HealthCheckResult> = runCatching {
        val response = systemApi.getHealth()
        val body = response.body() ?: error("Health response body is empty")
        body.toHealthCheckResult()
    }
}

private fun HealthResponse.toHealthCheckResult(): HealthCheckResult = HealthCheckResult(
    isHealthy = status.equals("ok", ignoreCase = true),
    statusLabel = status,
    serverTime = serverTime?.toString(),
)
