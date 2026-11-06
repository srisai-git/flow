package com.simats.focusflow.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DailyReminderReceiver"
        const val ACTION_DAILY_REMINDER = "com.simats.focusflow.ACTION_DAILY_REMINDER"
        const val ACTION_DEADLINE_ALERT = "com.simats.focusflow.ACTION_DEADLINE_ALERT"
        const val ACTION_BREATHER_PROMPT = "com.simats.focusflow.ACTION_BREATHER_PROMPT"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "onReceive invoked with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d(TAG, "Device reboot detected. Restoring scheduled daily focus reminder.")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val sessionManager = SessionManager.getInstance(context)
                    val notificationsEnabled = sessionManager.notificationsEnabledFlow.first()
                    val dailyReminderEnabled = sessionManager.dailyReminderEnabledFlow.first()
                    val reminderTime = sessionManager.reminderTimeFlow.first()

                    if (notificationsEnabled && dailyReminderEnabled) {
                        ReminderScheduler.scheduleDailyReminder(context, reminderTime, true)
                        Log.d(TAG, "Successfully restored reminder for $reminderTime after reboot.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore reminder on boot: ${e.message}", e)
                }
            }
            return
        }

        if (action == ACTION_DEADLINE_ALERT) {
            val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Upcoming Task"
            val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
            NotificationHelper.showNotification(
                context = context,
                title = "Deadline Alert (1h 15m remaining)",
                body = "Task: '$title' is due in 1 hour and 15 minutes. Plan your deep focus block now.",
                route = "task_details",
                channelId = NotificationHelper.CHANNEL_TASK_ALERTS,
                notificationId = 20000 + taskId
            )
            Log.d(TAG, "Deadline alert notification fired for task: $title")
            return
        }

        if (action == ACTION_BREATHER_PROMPT) {
            NotificationHelper.sendBreatherNotification(context)
            Log.d(TAG, "Breather reminder notification fired successfully.")
            return
        }

        // Action is ACTION_DAILY_REMINDER - Wall clock reached user's custom reminder time
        NotificationHelper.createNotificationChannels(context)
        NotificationHelper.showNotification(
            context = context,
            title = "FocusFlow Focus Reminder",
            body = "Your focus session is scheduled. Tap to start entering deep flow.",
            route = "timer",
            channelId = NotificationHelper.CHANNEL_DAILY_REMINDERS,
            notificationId = 1001
        )
        Log.d(TAG, "Daily reminder notification fired successfully.")

        // Automatically reschedule for the next day at the exact same custom time
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionManager = SessionManager.getInstance(context)
                val notificationsEnabled = sessionManager.notificationsEnabledFlow.first()
                val dailyReminderEnabled = sessionManager.dailyReminderEnabledFlow.first()
                val reminderTime = sessionManager.reminderTimeFlow.first()

                if (notificationsEnabled && dailyReminderEnabled) {
                    ReminderScheduler.scheduleDailyReminder(context, reminderTime, true)
                    Log.d(TAG, "Scheduled next day reminder for $reminderTime.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule next day reminder: ${e.message}", e)
            }
        }
    }
}
