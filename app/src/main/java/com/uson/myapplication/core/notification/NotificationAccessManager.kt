package com.uson.myapplication.core.notification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

object NotificationAccessManager {
    fun hasAccess(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ).orEmpty()

        return enabledListeners
            .split(":")
            .mapNotNull(ComponentName::unflattenFromString)
            .any { it.packageName == context.packageName }
    }

    fun createSettingsIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
