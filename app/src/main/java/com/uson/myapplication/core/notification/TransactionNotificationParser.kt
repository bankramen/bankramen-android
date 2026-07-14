package com.uson.myapplication.core.notification

import android.app.Notification
import android.service.notification.StatusBarNotification

object TransactionNotificationParser {
    private val supportedPackages = mapOf(
        "com.kakaopay.app" to "KAKAO_PAY",
        "com.kbankwith.smartbank" to "K_BANK",
    )

    private val amountRegex = Regex("([0-9][0-9,]*)원")
    private val merchantPaymentRegex = Regex("^(.+?)에서\\s")
    private val merchantTransferRecipientRegex = Regex("^(.+?)(?:님께|님에게|에게)\\s")
    private val merchantTransferDestRegex = Regex("^(.+?)(?:으로|로)\\s")
    private val selfTransferRegex = Regex("나한테\\s*송금")

    fun parse(sbn: StatusBarNotification): ParsedTransactionNotification? {
        if (!isSupportedPackage(sbn.packageName)) return null

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        return parse(
            sourceKey = sbn.key,
            packageName = sbn.packageName,
            title = title,
            text = text,
            bigText = bigText,
            timestamp = sbn.postTime,
        )
    }

    internal fun parse(
        sourceKey: String,
        packageName: String,
        title: String,
        text: String,
        bigText: String,
        timestamp: Long,
    ): ParsedTransactionNotification? {
        if (!isSupportedPackage(packageName)) return null

        val body = listOf(text, bigText)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = " ")
            .trim()

        if (title.isBlank() && body.isBlank()) return null
        if (isSelfTransferNotification(title = title, body = body)) return null

        val amount = extractAmount(body.ifBlank { title })
        val transactionType = resolveTransactionType(title = title, body = body, amount = amount)

        return ParsedTransactionNotification(
            sourceKey = sourceKey,
            packageName = packageName,
            title = title,
            body = body,
            amount = amount,
            merchant = extractMerchant(body = body, title = title, type = transactionType),
            timestamp = timestamp,
            paymentMethod = resolvePaymentMethod(packageName),
            transactionType = transactionType,
        )
    }

    private fun isSupportedPackage(packageName: String): Boolean = packageName in supportedPackages

    private fun extractAmount(rawText: String): Long? =
        amountRegex.find(rawText)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace(",", "")
            ?.toLongOrNull()

    private fun isSelfTransferNotification(title: String, body: String): Boolean =
        selfTransferRegex.containsMatchIn("$title $body")

    private fun extractMerchant(body: String, title: String, type: TransactionType): String? {
        if (body.isNotBlank()) {
            val extracted = when (type) {
                TransactionType.PAYMENT ->
                    merchantPaymentRegex.find(body)?.groupValues?.getOrNull(1)
                TransactionType.TRANSFER_OUT ->
                    merchantTransferRecipientRegex.find(body)?.groupValues?.getOrNull(1)
                        ?: merchantTransferDestRegex.find(body)?.groupValues?.getOrNull(1)
                else -> null
            }
            if (!extracted.isNullOrBlank()) return extracted.trim()
        }
        return title.ifBlank { null }
    }

    private fun resolvePaymentMethod(packageName: String): String =
        supportedPackages[packageName] ?: "UNKNOWN"

    private fun resolveTransactionType(
        title: String,
        body: String,
        amount: Long?,
    ): TransactionType {
        val haystack = "$title $body"
        return when {
            haystack.contains("입금") || haystack.contains("받음") -> TransactionType.INCOME
            haystack.contains("이체") || haystack.contains("송금") -> TransactionType.TRANSFER_OUT
            amount != null -> TransactionType.PAYMENT
            else -> TransactionType.UNKNOWN
        }
    }
}
