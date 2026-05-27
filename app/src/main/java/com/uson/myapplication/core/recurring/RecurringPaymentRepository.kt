package com.uson.myapplication.core.recurring

import android.content.Context
import com.uson.myapplication.core.api.BankramenApiFactory
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.generated.api.APIApi
import com.uson.myapplication.generated.model.CreateRecurringPaymentRequest
import java.time.LocalDate
import java.util.UUID

class RecurringPaymentRepository(
    context: Context,
    private val api: APIApi = BankramenApiFactory.createApiApi(),
) {
    private val sessionManager = AuthGraph.sessionManager(context)

    suspend fun createRecurringPayment(
        transactionId: UUID,
        nextBillingDate: LocalDate,
        cycle: CreateRecurringPaymentRequest.Cycle = CreateRecurringPaymentRequest.Cycle.MONTHLY,
    ): Result<Unit> = runCatching {
        val userId = requireUserId()
        val response = api.create(
            userId = userId,
            createRecurringPaymentRequest = CreateRecurringPaymentRequest(
                transactionId = transactionId,
                cycle = cycle,
                nextBillingDate = nextBillingDate,
            ),
        )
        if (!response.isSuccessful) {
            error("Recurring payment creation failed: HTTP ${response.code()}")
        }
    }

    suspend fun confirmRecurringPayment(recurringPaymentId: UUID): Result<Unit> = runCatching {
        val response = api.confirm(recurringPaymentId = recurringPaymentId)
        if (!response.isSuccessful) {
            error("Recurring payment confirmation failed: HTTP ${response.code()}")
        }
    }

    private fun requireUserId(): UUID =
        sessionManager.currentSession()?.userId
            ?.takeIf(String::isNotBlank)
            ?.let(UUID::fromString)
            ?: error("Authenticated userId is required for recurring payment API")
}
