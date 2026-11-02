package com.simats.focusflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.simats.focusflow.MainActivity
import com.simats.focusflow.R

object NotificationHelper {

    const val CHANNEL_REMOTE_ALERTS = "flow_remote_alerts"
    const val CHANNEL_DAILY_REMINDERS = "flow_daily_reminders"
    const val CHANNEL_TASK_ALERTS = "flow_task_alerts"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val remoteChannel = NotificationChannel(
                CHANNEL_REMOTE_ALERTS,
                "Flow Alerts & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts and schedule reminders from FocusFlow"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDERS,
                "Daily Focus Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily scheduled prompts to start your focus workflow"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val taskChannel = NotificationChannel(
                CHANNEL_TASK_ALERTS,
                "Task & Deadline Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming and pending task deadlines"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannels(listOf(remoteChannel, dailyChannel, taskChannel))
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        route: String? = null,
        channelId: String = CHANNEL_REMOTE_ALERTS,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        createNotificationChannels(context)

        val cleanTitle = purgeEmojis(title.ifBlank { "FocusFlow" })
        val cleanBody = purgeEmojis(body.ifBlank { "You have a new focus alert." })

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!route.isNullOrBlank()) {
                putExtra("route", route)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_flow)
            .setColor(0xFF8B5CF6.toInt()) // Primary violet brand color
            .setContentTitle(cleanTitle)
            .setContentText(cleanBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cleanBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun sendTestNotification(context: Context, route: String = "timer") {
        showNotification(
            context = context,
            title = "FocusFlow Focus Reminder",
            body = "Your focus session is ready. Tap to start entering deep flow.",
            route = route,
            channelId = CHANNEL_REMOTE_ALERTS
        )
    }

    private fun purgeEmojis(text: String): String {
        return text.replace(Regex("[\\x{10000}-\\x{10FFFF}\\x{2600}-\\x{27BF}\\x{2300}-\\x{23FF}\\x{2B50}\\x{2B55}]"), "").trim()
    }
}
