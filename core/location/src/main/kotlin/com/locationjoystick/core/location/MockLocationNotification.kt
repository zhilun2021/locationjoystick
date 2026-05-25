package com.locationjoystick.core.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.locationjoystick.core.common.constants.AppConstants

private val CHANNEL_ID = AppConstants.NotificationConstants.CHANNEL_ID_ACTIVE
private val CHANNEL_ID_PERM_ERROR = AppConstants.NotificationConstants.CHANNEL_ID_PERMISSION_ERROR

internal fun createMockLocationNotificationChannels(context: Context) {
    val channel =
        NotificationChannel(
            CHANNEL_ID,
            AppConstants.NotificationConstants.CHANNEL_NAME_ACTIVE,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = AppConstants.NotificationConstants.CHANNEL_DESC_ACTIVE
            setShowBadge(false)
        }
    val errorChannel =
        NotificationChannel(
            CHANNEL_ID_PERM_ERROR,
            AppConstants.NotificationConstants.CHANNEL_NAME_PERMISSION_ERROR,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = AppConstants.NotificationConstants.CHANNEL_DESC_PERMISSION_ERROR
        }
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(channel)
    notificationManager.createNotificationChannel(errorChannel)
}

internal fun buildMockLocationNotification(context: Context): Notification {
    val openAppIntent =
        context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.let { intent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }

    val stopIntent =
        Intent(context, MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_STOP
        }
    val stopPendingIntent =
        PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    return NotificationCompat
        .Builder(context, CHANNEL_ID)
        .setContentTitle(AppConstants.NotificationConstants.TITLE_ACTIVE)
        .setContentText(AppConstants.NotificationConstants.TEXT_ACTIVE)
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setContentIntent(openAppIntent)
        .addAction(
            android.R.drawable.ic_delete,
            AppConstants.NotificationConstants.ACTION_STOP,
            stopPendingIntent,
        ).setOngoing(true)
        .setSilent(true)
        .build()
}

internal fun postMockLocationPermissionErrorNotification(context: Context) {
    val openAppIntent =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    val notification =
        NotificationCompat
            .Builder(context, CHANNEL_ID_PERM_ERROR)
            .setContentTitle(AppConstants.NotificationConstants.TITLE_PERMISSION_ERROR)
            .setContentText(AppConstants.NotificationConstants.TEXT_PERMISSION_ERROR)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .build()
    context
        .getSystemService(NotificationManager::class.java)
        .notify(AppConstants.NotificationConstants.ID_PERMISSION_ERROR, notification)
}
