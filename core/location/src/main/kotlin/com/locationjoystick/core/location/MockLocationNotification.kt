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

internal data class ActionSpec(
    val label: String,
    val actionKey: String,
)

internal fun selectNotificationActions(
    replayActive: Boolean,
    replayPaused: Boolean,
): List<ActionSpec> =
    when {
        !replayActive -> {
            listOf(
                ActionSpec(AppConstants.NotificationConstants.ACTION_STOP, AppConstants.ServiceConstants.ACTION_STOP),
                ActionSpec(AppConstants.NotificationConstants.ACTION_OPEN_MAP, "nav_map"),
                ActionSpec(AppConstants.NotificationConstants.ACTION_OPEN_FAVORITES, "nav_favorites"),
            )
        }

        replayPaused -> {
            listOf(
                ActionSpec(AppConstants.NotificationConstants.ACTION_STOP, AppConstants.ServiceConstants.ACTION_STOP),
                ActionSpec(AppConstants.NotificationConstants.ACTION_RESUME, AppConstants.ServiceConstants.ACTION_ROUTE_REPLAY_RESUME),
                ActionSpec(AppConstants.NotificationConstants.ACTION_OPEN_MAP, "nav_map"),
            )
        }

        else -> {
            listOf(
                ActionSpec(AppConstants.NotificationConstants.ACTION_STOP, AppConstants.ServiceConstants.ACTION_STOP),
                ActionSpec(AppConstants.NotificationConstants.ACTION_PAUSE, AppConstants.ServiceConstants.ACTION_ROUTE_REPLAY_PAUSE),
                ActionSpec(AppConstants.NotificationConstants.ACTION_OPEN_MAP, "nav_map"),
            )
        }
    }

private fun activityPendingIntent(
    context: Context,
    requestCode: Int,
    extraKey: String,
): PendingIntent =
    PendingIntent.getActivity(
        context,
        requestCode,
        Intent().apply {
            setClassName(context.packageName, AppConstants.ServiceConstants.MAIN_ACTIVITY_CLASS)
            putExtra(extraKey, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

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

internal fun buildMockLocationNotification(
    context: Context,
    replayActive: Boolean = false,
    replayPaused: Boolean = false,
): Notification {
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

    val stopPendingIntent =
        PendingIntent.getService(
            context,
            1,
            Intent(context, MockLocationService::class.java).apply {
                action = MockLocationService.ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    val pausePendingIntent =
        PendingIntent.getService(
            context,
            4,
            Intent(context, MockLocationService::class.java).apply {
                action = AppConstants.ServiceConstants.ACTION_ROUTE_REPLAY_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    val resumePendingIntent =
        PendingIntent.getService(
            context,
            5,
            Intent(context, MockLocationService::class.java).apply {
                action = AppConstants.ServiceConstants.ACTION_ROUTE_REPLAY_RESUME
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    val mapPendingIntent = activityPendingIntent(context, 2, AppConstants.ServiceConstants.EXTRA_NAVIGATE_TO_MAP)
    val favoritesPendingIntent = activityPendingIntent(context, 3, AppConstants.ServiceConstants.EXTRA_NAVIGATE_TO_FAVORITES)

    val builder =
        NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setContentTitle(AppConstants.NotificationConstants.TITLE_ACTIVE)
            .setContentText(AppConstants.NotificationConstants.TEXT_ACTIVE)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setSilent(true)

    for (action in selectNotificationActions(replayActive, replayPaused)) {
        val pendingIntent =
            when (action.actionKey) {
                AppConstants.ServiceConstants.ACTION_STOP -> stopPendingIntent
                AppConstants.ServiceConstants.ACTION_ROUTE_REPLAY_PAUSE -> pausePendingIntent
                AppConstants.ServiceConstants.ACTION_ROUTE_REPLAY_RESUME -> resumePendingIntent
                "nav_map" -> mapPendingIntent
                "nav_favorites" -> favoritesPendingIntent
                else -> null
            }
        if (pendingIntent != null) {
            builder.addAction(0, action.label, pendingIntent)
        }
    }

    return builder.build()
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
