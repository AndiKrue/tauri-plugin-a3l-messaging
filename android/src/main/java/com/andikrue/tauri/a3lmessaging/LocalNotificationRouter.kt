package com.andikrue.tauri.a3lmessaging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.amazon.A3L.messaging.RemoteMessage
import kotlin.math.absoluteValue

internal object LocalNotificationRouter {
    private const val ENABLED = "_a3l_tauri_notify"
    private const val TITLE = "_a3l_tauri_title"
    private const val BODY = "_a3l_tauri_body"
    private const val URGENCY = "_a3l_tauri_urgency"
    private const val ACTION = "_a3l_tauri_action"

    fun maybeNotify(context: Context, message: RemoteMessage) {
        if (message.data[ENABLED]?.lowercase() !in setOf("1", "true", "yes")) return
        if (AppVisibility.isVisible()) return

        val urgency =
            when (message.data[URGENCY]?.lowercase()) {
                "low" -> "low"
                "urgent" -> "urgent"
                else -> "attention"
            }
        val title =
            message.data[TITLE]
                ?.takeIf { it.isNotBlank() }
                ?: message.notification?.title
                ?: context.applicationInfo.loadLabel(context.packageManager).toString()
        val body =
            message.data[BODY]
                ?.takeIf { it.isNotBlank() }
                ?: message.notification?.body
                ?: "New message"
        val actionLabel = message.data[ACTION]?.takeIf { it.isNotBlank() } ?: "OPEN"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tauri_a3l_" + urgency
        ensureChannel(notificationManager, channelId, urgency)

        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        launchIntent
            .addFlags(
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
            .putExtra("tauri_a3l_message_id", message.messageId)

        val identity = message.messageId ?: (message.sentTime.toString() + ":" + (message.from ?: ""))
        val notificationId = identity.hashCode().absoluteValue.coerceAtLeast(1)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                notificationId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val smallIcon =
            context.applicationInfo.icon.takeIf { it != 0 }
                ?: android.R.drawable.ic_dialog_info

        val builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, channelId)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)
            }

        builder
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_EVENT)
            .setColor(colorFor(urgency))
            .addAction(smallIcon, actionLabel, pendingIntent)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            builder.priority =
                if (urgency == "low") Notification.PRIORITY_LOW else Notification.PRIORITY_HIGH
        }

        notificationManager.notify(notificationId, builder.build())
    }

    private fun ensureChannel(
        manager: NotificationManager,
        channelId: String,
        urgency: String,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val importance =
            if (urgency == "low") {
                NotificationManager.IMPORTANCE_LOW
            } else {
                NotificationManager.IMPORTANCE_HIGH
            }
        val name =
            when (urgency) {
                "low" -> "A3L updates"
                "urgent" -> "A3L urgent"
                else -> "A3L attention"
            }
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(NotificationChannel(channelId, name, importance))
        }
    }

    private fun colorFor(urgency: String): Int =
        when (urgency) {
            "urgent" -> Color.rgb(211, 47, 47)
            "attention" -> Color.rgb(255, 179, 0)
            else -> Color.rgb(69, 90, 100)
        }
}
