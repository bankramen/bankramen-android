package com.uson.myapplication.core.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.uson.myapplication.core.notification.ParsedTransactionNotification
import com.uson.myapplication.core.notification.TransactionType

@Entity(tableName = "parsed_notifications")
data class ParsedNotificationEntity(
    @PrimaryKey val sourceKey: String,
    val packageName: String,
    val title: String,
    val body: String,
    val amount: Long?,
    val merchant: String?,
    val timestamp: Long,
    val paymentMethod: String,
    @ColumnInfo(defaultValue = "UNKNOWN") val transactionType: String,
)

fun ParsedTransactionNotification.toEntity(): ParsedNotificationEntity = ParsedNotificationEntity(
    sourceKey = sourceKey,
    packageName = packageName,
    title = title,
    body = body,
    amount = amount,
    merchant = merchant,
    timestamp = timestamp,
    paymentMethod = paymentMethod,
    transactionType = transactionType.name,
)

fun ParsedNotificationEntity.toModel(): ParsedTransactionNotification = ParsedTransactionNotification(
    sourceKey = sourceKey,
    packageName = packageName,
    title = title,
    body = body,
    amount = amount,
    merchant = merchant,
    timestamp = timestamp,
    paymentMethod = paymentMethod,
    transactionType = runCatching { TransactionType.valueOf(transactionType) }.getOrDefault(TransactionType.UNKNOWN),
)
