package com.uson.myapplication.core.notification

import android.app.Notification
import android.service.notification.StatusBarNotification

object TransactionNotificationParser {
    private val supportedPackagePrefixes = listOf(
        "com.kakaopay",
        "viva.republica",
        "com.naver",
    )

    private val amountRegex = Regex("([0-9][0-9,]*)원")

    fun parse(sbn: StatusBarNotification): ParsedTransactionNotification? {
        if (!isSupportedPackage(sbn.packageName)) return null

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val body = listOf(text, bigText)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = " ")
            .trim()

        if (title.isBlank() && body.isBlank()) return null

        val amount = extractAmount(body.ifBlank { title })

        return ParsedTransactionNotification(
            sourceKey = sbn.key,
            packageName = sbn.packageName,
            title = title,
            body = body,
            amount = amount,
            merchant = title.ifBlank { null },
            timestamp = sbn.postTime,
            paymentMethod = resolvePaymentMethod(sbn.packageName),
        )
    }

    private fun isSupportedPackage(packageName: String): Boolean =
        supportedPackagePrefixes.any(packageName::startsWith)

    private fun extractAmount(rawText: String): Long? =
        amountRegex.find(rawText)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace(",", "")
            ?.toLongOrNull()

    private fun resolvePaymentMethod(packageName: String): String = when {
        packageName.startsWith("com.kakaopay") -> "KAKAO_PAY"
        packageName.startsWith("viva.republica") -> "TOSS"
        packageName.startsWith("com.naver") -> "NAVER_PAY"
        else -> "UNKNOWN"
    }
}
