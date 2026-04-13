package com.uson.myapplication.core.notification

data class ParsedTransactionNotification(
    val sourceKey: String,
    val packageName: String,
    val title: String,
    val body: String,
    val amount: Long?,
    val merchant: String?,
    val timestamp: Long,
    val paymentMethod: String,
)
