package com.uson.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.uson.myapplication.core.api.BankramenApiFactory
import com.uson.myapplication.core.auth.DeviceTokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BankramenApplication : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.IO)
    private val deviceTokenRepository by lazy { DeviceTokenRepository(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        BankramenApiFactory.initialize(applicationContext)
        createNotificationChannels()
        logFcmToken()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID_PUSH,
                "뱅크라면 알림",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "뱅크라면 푸시 알림" }
        )
    }

    private fun logFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result.orEmpty()
                Log.d("FCM", "Device token: $token")
                applicationScope.launch {
                    deviceTokenRepository.registerDeviceToken(token)
                        .onFailure { Log.w("FCM", "Failed to register device token on app start", it) }
                }
            } else {
                Log.w("FCM", "Failed to get FCM token", task.exception)
            }
        }
    }

    companion object {
        const val CHANNEL_ID_PUSH = "bankramen_push"
    }
}
