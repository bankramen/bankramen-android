package com.uson.myapplication.core.notification

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationUploadStatus(
    val hasFailure: Boolean = false,
)

class NotificationUploadStatusStore private constructor(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _status = MutableStateFlow(
        NotificationUploadStatus(hasFailure = preferences.getBoolean(KEY_UPLOAD_FAILED, false)),
    )
    val status: StateFlow<NotificationUploadStatus> = _status.asStateFlow()

    fun markUploadFailed() {
        preferences.edit().putBoolean(KEY_UPLOAD_FAILED, true).apply()
        _status.value = NotificationUploadStatus(hasFailure = true)
    }

    fun markUploadSucceeded() {
        preferences.edit().putBoolean(KEY_UPLOAD_FAILED, false).apply()
        _status.value = NotificationUploadStatus(hasFailure = false)
    }

    companion object {
        private const val PREFERENCES_NAME = "notification_upload_status"
        private const val KEY_UPLOAD_FAILED = "upload_failed"

        @Volatile
        private var instance: NotificationUploadStatusStore? = null

        fun get(context: Context): NotificationUploadStatusStore = instance ?: synchronized(this) {
            instance ?: NotificationUploadStatusStore(context.applicationContext).also { instance = it }
        }
    }
}
