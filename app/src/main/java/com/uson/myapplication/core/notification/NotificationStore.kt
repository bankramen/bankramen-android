package com.uson.myapplication.core.notification

import android.content.Context
import com.uson.myapplication.core.local.BankramenDatabaseProvider
import com.uson.myapplication.core.local.toEntity
import com.uson.myapplication.core.local.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class NotificationSummary(
    val count: Int = 0,
    val latestMerchant: String? = null,
    val latestAmount: Long? = null,
)

class NotificationStore private constructor(
    context: Context,
) {
    private val dao = BankramenDatabaseProvider.get(context).parsedNotificationDao()

    val recentNotifications: Flow<List<ParsedTransactionNotification>> =
        dao.observeRecent().map { entities -> entities.map { it.toModel() } }

    val summary: Flow<NotificationSummary> = combine(
        dao.observeCount(),
        dao.observeLatest(),
    ) { count, latest ->
        NotificationSummary(
            count = count,
            latestMerchant = latest?.merchant ?: latest?.title,
            latestAmount = latest?.amount,
        )
    }

    suspend fun save(notification: ParsedTransactionNotification) {
        dao.upsert(notification.toEntity())
    }

    companion object {
        @Volatile
        private var instance: NotificationStore? = null

        fun get(context: Context): NotificationStore = instance ?: synchronized(this) {
            instance ?: NotificationStore(context.applicationContext).also { instance = it }
        }
    }
}
