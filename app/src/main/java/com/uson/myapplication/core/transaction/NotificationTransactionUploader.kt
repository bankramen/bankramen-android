package com.uson.myapplication.core.transaction

import com.uson.myapplication.core.api.BankramenApiFactory
import com.uson.myapplication.core.auth.authLogDebug
import com.uson.myapplication.core.notification.ParsedTransactionNotification
import com.uson.myapplication.core.notification.TransactionType
import com.uson.myapplication.generated.api.TransactionApi
import com.uson.myapplication.generated.model.CreatePaymentNotificationTransactionRequest
import com.uson.myapplication.generated.model.CreateTransactionRequest
import java.time.Instant
import java.time.ZoneId

enum class NotificationUploadOutcome {
    Uploaded,
    Skipped,
}

class NotificationTransactionUploader(
    private val transactionApi: TransactionApi = BankramenApiFactory.createTransactionApi(),
) {
    suspend fun upload(notification: ParsedTransactionNotification): Result<NotificationUploadOutcome> = runCatching {
        val amount = notification.amount ?: return@runCatching NotificationUploadOutcome.Skipped
        val title = notification.merchant?.takeIf { it.isNotBlank() } ?: notification.title.ifBlank { notification.body }
        if (title.isBlank()) return@runCatching NotificationUploadOutcome.Skipped

        when (notification.transactionType) {
            TransactionType.PAYMENT -> {
                val response = transactionApi.createPaymentNotificationTransaction(
                    CreatePaymentNotificationTransactionRequest(
                        title = title,
                        amount = amount,
                    ),
                )
                if (!response.isSuccessful) {
                    error("Payment notification upload failed: HTTP ${response.code()}")
                }
                authLogDebug("payment notification uploaded title=$title amount=$amount")
                NotificationUploadOutcome.Uploaded
            }

            TransactionType.INCOME -> {
                val response = transactionApi.createTransaction(
                    CreateTransactionRequest(
                        type = CreateTransactionRequest.Type.INCOME,
                        amount = amount,
                        title = title,
                        category = CreateTransactionRequest.Category.TRANSFER,
                        transactionDate = Instant.ofEpochMilli(notification.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate(),
                    ),
                )
                if (!response.isSuccessful) {
                    error("Income notification upload failed: HTTP ${response.code()}")
                }
                authLogDebug("income notification uploaded title=$title amount=$amount")
                NotificationUploadOutcome.Uploaded
            }

            TransactionType.TRANSFER_OUT,
            TransactionType.UNKNOWN,
            -> NotificationUploadOutcome.Skipped
        }
    }
}
