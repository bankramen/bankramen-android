package com.uson.myapplication.core.notification

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import com.uson.myapplication.service.TransactionNotificationService

fun isNotificationListenerAccessGranted(context: Context): Boolean {
    if (supportsServiceSpecificListenerCheck(Build.VERSION.SDK_INT)) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val listener = ComponentName(context, TransactionNotificationService::class.java)
        return manager.isNotificationListenerAccessGranted(listener)
    }

    return NotificationManagerCompat.getEnabledListenerPackages(context)
        .contains(context.packageName)
}

internal fun supportsServiceSpecificListenerCheck(sdkInt: Int): Boolean =
    sdkInt >= Build.VERSION_CODES.O_MR1

fun openNotificationListenerSettings(context: Context): Boolean = runCatching {
    context.startActivity(
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}.fold(
    onSuccess = { true },
    onFailure = { throwable ->
        if (throwable !is ActivityNotFoundException) throw throwable
        false
    },
)
