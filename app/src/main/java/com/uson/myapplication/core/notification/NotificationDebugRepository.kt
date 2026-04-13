package com.uson.myapplication.core.notification

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

object NotificationDebugRepository {
    private const val MaxItems = 50
    private const val DebugDirectoryName = "notification-debug"
    private const val DebugFileName = "parsed-transactions.log"

    private val _notifications = MutableStateFlow<List<ParsedTransactionNotification>>(emptyList())
    val notifications: StateFlow<List<ParsedTransactionNotification>> = _notifications.asStateFlow()

    fun record(context: Context, notification: ParsedTransactionNotification) {
        _notifications.update { current ->
            listOf(notification) + current.filterNot { it.sourceKey == notification.sourceKey }
        }
        _notifications.update { it.take(MaxItems) }

        val debugFile = File(File(context.filesDir, DebugDirectoryName), DebugFileName)
        debugFile.parentFile?.mkdirs()
        debugFile.appendText(notification.toLogLine())
    }

    fun debugFilePath(context: Context): String =
        File(File(context.filesDir, DebugDirectoryName), DebugFileName).absolutePath

    fun latestNotifications(): List<ParsedTransactionNotification> = notifications.value
}

private fun ParsedTransactionNotification.toLogLine(): String = buildString {
    append(timestamp)
    append('|')
    append(packageName)
    append('|')
    append(paymentMethod)
    append('|')
    append(amount ?: "")
    append('|')
    append(merchant.orEmpty())
    append('|')
    append(title.replace('\n', ' '))
    append('|')
    append(body.replace('\n', ' '))
    appendLine()
}
