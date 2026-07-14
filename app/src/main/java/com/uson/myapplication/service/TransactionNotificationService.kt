package com.uson.myapplication.service

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.uson.myapplication.core.notification.NotificationDebugRepository
import com.uson.myapplication.core.notification.NotificationStore
import com.uson.myapplication.core.notification.NotificationUploadStatusStore
import com.uson.myapplication.core.notification.TransactionNotificationParser
import com.uson.myapplication.core.transaction.NotificationTransactionUploader
import com.uson.myapplication.core.transaction.NotificationUploadOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TransactionNotificationService : NotificationListenerService() {
    private companion object {
        const val TAG = "TransactionNotificationService"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationTransactionUploader by lazy { NotificationTransactionUploader() }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Notification listener service created")
        NotificationDebugRepository.recordEvent("서비스 생성")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        NotificationDebugRepository.recordEvent("리스너 연결됨")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        Log.d(
            TAG,
            "Notification posted package=${sbn.packageName} key=${sbn.key}",
        )
        NotificationDebugRepository.recordEvent("알림 수신: ${sbn.packageName}")

        val parsedNotification = runCatching {
            TransactionNotificationParser.parse(sbn)
        }.onFailure {
            Log.w(TAG, "Failed to parse notification package=${sbn.packageName} key=${sbn.key}", it)
            NotificationDebugRepository.recordEvent("파싱 예외: ${sbn.packageName}")
        }.getOrNull()

        if (parsedNotification == null) {
            Log.d(TAG, "Notification ignored package=${sbn.packageName} key=${sbn.key}")
            NotificationDebugRepository.recordEvent("알림 무시: ${sbn.packageName}")
            return
        }

        serviceScope.launch {
            NotificationStore.get(applicationContext).save(parsedNotification)
            NotificationDebugRepository.record(
                context = applicationContext,
                notification = parsedNotification,
            )
            NotificationDebugRepository.recordEvent(
                "파싱 성공: ${parsedNotification.paymentMethod} ${parsedNotification.merchant ?: parsedNotification.title}",
            )
            notificationTransactionUploader.upload(parsedNotification)
                .onSuccess { outcome ->
                    when (outcome) {
                        NotificationUploadOutcome.Uploaded -> {
                            NotificationUploadStatusStore.get(applicationContext).markUploadSucceeded()
                            NotificationDebugRepository.recordEvent(
                                "업로드 성공: ${parsedNotification.amount ?: 0}원",
                            )
                        }

                        NotificationUploadOutcome.Skipped -> {
                            NotificationDebugRepository.recordEvent("업로드 대상 아님")
                        }
                    }
                }
                .onFailure {
                    NotificationUploadStatusStore.get(applicationContext).markUploadFailed()
                    Log.w(TAG, "Failed to upload parsed payment notification", it)
                    NotificationDebugRepository.recordEvent(
                        "업로드 실패: ${it.message ?: "unknown"}",
                    )
                }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected; requesting rebind")
        NotificationDebugRepository.recordEvent("리스너 연결 해제됨")
        runCatching {
            requestRebind(ComponentName(applicationContext, javaClass))
        }.onFailure {
            Log.w(TAG, "Failed to request notification listener rebind", it)
            NotificationDebugRepository.recordEvent("재바인드 요청 실패")
        }.onSuccess {
            NotificationDebugRepository.recordEvent("재바인드 요청함")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Notification listener service destroyed")
        NotificationDebugRepository.recordEvent("서비스 종료")
        serviceScope.cancel()
        super.onDestroy()
    }
}
