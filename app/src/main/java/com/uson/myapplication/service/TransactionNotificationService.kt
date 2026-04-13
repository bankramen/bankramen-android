package com.uson.myapplication.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.uson.myapplication.core.notification.NotificationDebugRepository
import com.uson.myapplication.core.notification.NotificationStore
import com.uson.myapplication.core.notification.TransactionNotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TransactionNotificationService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val parsedNotification = runCatching {
            TransactionNotificationParser.parse(sbn)
        }.getOrNull() ?: return

        serviceScope.launch {
            NotificationStore.get(applicationContext).save(parsedNotification)
            NotificationDebugRepository.record(
                context = applicationContext,
                notification = parsedNotification,
            )
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
