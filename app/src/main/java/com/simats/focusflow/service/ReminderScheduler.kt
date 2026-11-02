package com.simats.focusflow.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"
    private const val REQUEST_CODE = 4401

    /**
     * Schedules or cancels the daily focus reminder alarm at the specified custom time.
     * Uses AlarmManager with exact RTC_WAKEUP alarms so the notification fires reliably
     * even when the device is idle or in doze mode.
     */
    fun scheduleDailyReminder(context: Context, timeStr: String, enabled: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager service not available on this device.")
            return
        }

        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = DailyReminderReceiver.ACTION_DAILY_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            Log.d(TAG, "Notifications or daily reminder disabled. Cancelling alarm.")
            alarmManager.cancel(pendingIntent)
            return
        }

        val targetCalendar = calculateTargetTime(timeStr)
        val triggerAtMillis = targetCalendar.timeInMillis
        val formattedTarget = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(targetCalendar.time)
        Log.d(TAG, "Scheduling exact daily reminder for: $formattedTarget (epoch: $triggerAtMillis)")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Alarm set with setExactAndAllowWhileIdle (API 31+)")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Alarm set with setAndAllowWhileIdle (API 31+ without exact permission)")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm set with setExactAndAllowWhileIdle (API 23-30)")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm set with setExact (Pre-API 23)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exact alarm scheduling failed: ${e.message}. Attempting fallback...", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Fallback alarm scheduling succeeded.")
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "All alarm scheduling attempts failed: ${fallbackEx.message}", fallbackEx)
            }
        }
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = DailyReminderReceiver.ACTION_DAILY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Daily reminder alarm cancelled.")
    }

    /**
     * Parses the custom time string (e.g. "08:35 AM", "02:15 PM") and calculates
     * the next occurrence timestamp. If the time has already passed today, schedules for tomorrow.
     */
    private fun calculateTargetTime(timeStr: String): Calendar {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()

        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val parsed = sdf.parse(timeStr.trim())
            if (parsed != null) {
                val tempCal = Calendar.getInstance().apply { time = parsed }
                target.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY))
                target.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE))
                target.set(Calendar.SECOND, 0)
                target.set(Calendar.MILLISECOND, 0)
            } else {
                target.set(Calendar.HOUR_OF_DAY, 9)
                target.set(Calendar.MINUTE, 0)
                target.set(Calendar.SECOND, 0)
                target.set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            target.set(Calendar.HOUR_OF_DAY, 9)
            target.set(Calendar.MINUTE, 0)
            target.set(Calendar.SECOND, 0)
            target.set(Calendar.MILLISECOND, 0)
        }

        // If the custom target time is within the past 10 seconds of today or earlier, schedule for tomorrow
        if (target.timeInMillis <= now.timeInMillis + 5000) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target
    }
}

