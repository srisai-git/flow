package com.simats.focusflow.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object TaskDeadlineScheduler {

    fun scheduleTaskDeadline(context: Context, taskId: Int, taskTitle: String, deadlineTimestamp: Long) {
        if (taskId <= 0 || deadlineTimestamp <= 0L) return

        cancelTaskDeadline(context, taskId)

        val now = System.currentTimeMillis()
        val delayMillis = deadlineTimestamp - now
        if (delayMillis <= 0L) return

        var scheduledWithAlarmManager = false
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (alarmManager != null) {
            val intent = Intent(context, TaskDeadlineReceiver::class.java).apply {
                action = TaskDeadlineReceiver.ACTION_TASK_DEADLINE
                putExtra(TaskDeadlineReceiver.EXTRA_TASK_ID, taskId)
                putExtra(TaskDeadlineReceiver.EXTRA_TASK_TITLE, taskTitle)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                10000 + taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            deadlineTimestamp,
                            pendingIntent
                        )
                        scheduledWithAlarmManager = true
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        deadlineTimestamp,
                        pendingIntent
                    )
                    scheduledWithAlarmManager = true
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        deadlineTimestamp,
                        pendingIntent
                    )
                    scheduledWithAlarmManager = true
                }
            } catch (_: Exception) {
                scheduledWithAlarmManager = false
            }
        }

        if (!scheduledWithAlarmManager) {
            try {
                val inputData = Data.Builder()
                    .putInt(TaskDeadlineReceiver.EXTRA_TASK_ID, taskId)
                    .putString(TaskDeadlineReceiver.EXTRA_TASK_TITLE, taskTitle)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<TaskDeadlineWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("task_deadline_$taskId")
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            } catch (_: Exception) {}
        }
    }

    fun cancelTaskDeadline(context: Context, taskId: Int) {
        if (taskId <= 0) return

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager != null) {
                val intent = Intent(context, TaskDeadlineReceiver::class.java).apply {
                    action = TaskDeadlineReceiver.ACTION_TASK_DEADLINE
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    10000 + taskId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        } catch (_: Exception) {}

        try {
            WorkManager.getInstance(context).cancelAllWorkByTag("task_deadline_$taskId")
        } catch (_: Exception) {}
    }
}
