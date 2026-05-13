package com.uson.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class BankramenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        logFcmToken()
    }

    private fun createNotificationChannels() {
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
                Log.d("FCM", "Device token: ${task.result}")
            } else {
                Log.w("FCM", "Failed to get FCM token", task.exception)
            }
        }
    }

    companion object {
        const val CHANNEL_ID_PUSH = "bankramen_push"
    }
}
