package com.simats.focusflow.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.simats.focusflow.MainActivity
import com.simats.focusflow.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    const val CHANNEL_REMOTE_ALERTS = "flow_remote_alerts"
    const val CHANNEL_DAILY_REMINDERS = "flow_daily_reminders"
    const val CHANNEL_TASK_ALERTS = "flow_task_alerts"

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

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
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Suppressing notification.")
            return
        }

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
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Schedules a high-importance deadline alert exactly 1 hour and 15 minutes (75 minutes)
     * prior to the task due timestamp using exact alarm scheduling.
     */
    fun scheduleDeadlineNotification(
        context: Context,
        taskId: Int,
        taskTitle: String,
        dueDateStr: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val dueMillis = parseDueDateTime(dueDateStr) ?: return
        val alertMillis = dueMillis - (75L * 60 * 1000) // 1 hr 15 mins before due date

        if (alertMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "Task deadline alert time has already passed for task: $taskTitle")
            return
        }

        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = DailyReminderReceiver.ACTION_DEADLINE_ALERT
            putExtra(DailyReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(DailyReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            10000 + taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alertMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alertMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled 75m deadline alert for '$taskTitle' at epoch: $alertMillis")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule exact deadline alarm: ${e.message}")
        }
    }

    /**
     * Dispatches an immediate mindful breather notification after deep work sessions.
     */
    fun sendBreatherNotification(context: Context) {
        showNotification(
            context = context,
            title = "Time for a Mindful Breather",
            body = "Great focus interval completed. Step away from your screen, stretch, and take 3 deep breaths.",
            route = "take_breather",
            channelId = CHANNEL_DAILY_REMINDERS,
            notificationId = 3301
        )
    }

    /**
     * Schedules a breather prompt after consecutive focus intervals or when idle period exceeds threshold.
     */
    fun scheduleBreatherReminder(context: Context, delayMinutes: Long = 30) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = System.currentTimeMillis() + (delayMinutes * 60 * 1000)

        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = DailyReminderReceiver.ACTION_BREATHER_PROMPT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            3302,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule breather reminder: ${e.message}")
        }
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

    private fun parseDueDateTime(dateStr: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.US)
                val date = sdf.parse(dateStr)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return null
    }

    private fun purgeEmojis(text: String): String {
        return text.replace(Regex("[\\x{10000}-\\x{10FFFF}\\x{2600}-\\x{27BF}\\x{2300}-\\x{23FF}\\x{2B50}\\x{2B55}]"), "").trim()
    }
}
