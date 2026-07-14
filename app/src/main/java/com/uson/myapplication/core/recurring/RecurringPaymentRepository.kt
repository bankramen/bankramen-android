package com.uson.myapplication.core.recurring

import android.content.Context
import com.uson.myapplication.core.api.BankramenApiFactory
import com.uson.myapplication.core.auth.AuthGraph
import com.uson.myapplication.core.auth.authLogDebug
import com.uson.myapplication.core.auth.authLogError
import com.uson.myapplication.generated.api.APIApi
import com.uson.myapplication.generated.model.CreateRecurringPaymentRequest
import com.uson.myapplication.generated.model.RecurringPaymentListResponse
import com.uson.myapplication.generated.model.RecurringPaymentResponse
import java.time.LocalDate
import java.util.UUID

class RecurringPaymentRepository(
    context: Context,
    private val api: APIApi = BankramenApiFactory.createApiApi(),
) {
    private val sessionManager = AuthGraph.sessionManager(context)

    suspend fun getRecurringPayments(): Result<RecurringPaymentSnapshot> = runCatching {
        val userId = requireUserId()
        val response = api.getRecurringPayments(userId = userId)
        if (!response.isSuccessful) {
            authLogError("getRecurringPayments failed http=${response.code()}")
            error("Recurring payments fetch failed: HTTP ${response.code()}")
        }
        response.body()?.toSnapshot() ?: RecurringPaymentSnapshot()
    }

    suspend fun createRecurringPayment(
        transactionId: UUID,
        nextBillingDate: LocalDate,
        cycle: CreateRecurringPaymentRequest.Cycle = CreateRecurringPaymentRequest.Cycle.MONTHLY,
    ): Result<Unit> = runCatching {
        val userId = requireUserId()
        authLogDebug("createRecurringPayment request userId=$userId transactionId=$transactionId nextBillingDate=$nextBillingDate cycle=${cycle.value}")
        val response = api.create(
            userId = userId,
            createRecurringPaymentRequest = CreateRecurringPaymentRequest(
                transactionId = transactionId,
                cycle = cycle,
                nextBillingDate = nextBillingDate,
            ),
        )
        if (!response.isSuccessful) {
            authLogError("createRecurringPayment failed http=${response.code()}")
            error("Recurring payment creation failed: HTTP ${response.code()}")
        }
        authLogDebug("createRecurringPayment success transactionId=$transactionId")
    }

    suspend fun confirmRecurringPayment(recurringPaymentId: UUID): Result<Unit> = runCatching {
        val response = api.confirm(
            userId = requireUserId(),
            recurringPaymentId = recurringPaymentId,
        )
        if (!response.isSuccessful) {
            error("Recurring payment confirmation failed: HTTP ${response.code()}")
        }
    }

    suspend fun deleteRecurringPayment(recurringPaymentId: UUID): Result<Unit> = runCatching {
        val response = api.delete(
            userId = requireUserId(),
            recurringPaymentId = recurringPaymentId,
        )
        if (!response.isSuccessful) {
            error("Recurring payment deletion failed: HTTP ${response.code()}")
        }
    }

    private fun requireUserId(): UUID =
        sessionManager.currentSession()?.userId
            ?.takeIf(String::isNotBlank)
            ?.let { userId ->
                runCatching { UUID.fromString(userId) }
                    .onFailure { throwable ->
                        authLogError("invalid recurring payment userId userId=$userId", throwable)
                    }
                    .getOrNull()
            }
            ?: error("Authenticated userId is required for recurring payment API")
}

data class RecurringPaymentSnapshot(
    val monthlyScheduledTotalAmount: Long = 0L,
    val items: List<RecurringPaymentEntry> = emptyList(),
)

data class RecurringPaymentEntry(
    val id: UUID,
    val name: String,
    val amount: Long,
    val category: String,
    val categoryDisplayName: String,
    val cycle: String,
    val billingDay: Int,
    val nextBillingDate: LocalDate?,
    val registrationType: String,
    val confirmed: Boolean,
    val transactionCount: Int,
    val lastPaidDate: LocalDate?,
)

private fun RecurringPaymentListResponse.toSnapshot(): RecurringPaymentSnapshot = RecurringPaymentSnapshot(
    monthlyScheduledTotalAmount = monthlyScheduledTotalAmount ?: 0L,
    items = items.orEmpty().mapNotNull(RecurringPaymentResponse::toEntry),
)

private fun RecurringPaymentResponse.toEntry(): RecurringPaymentEntry? {
    val recurringPaymentId = recurringPaymentId ?: return null
    return RecurringPaymentEntry(
        id = recurringPaymentId,
        name = name.orEmpty(),
        amount = amount ?: 0L,
        category = category.orEmpty(),
        categoryDisplayName = categoryDisplayName.orEmpty(),
        cycle = cycle?.value.orEmpty(),
        billingDay = billingDay ?: 0,
        nextBillingDate = nextBillingDate,
        registrationType = registrationType?.value.orEmpty(),
        confirmed = confirmed == true,
        transactionCount = transactionCount ?: 0,
        lastPaidDate = lastPaidDate,
    )
}
