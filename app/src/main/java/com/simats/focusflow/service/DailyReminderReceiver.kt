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

