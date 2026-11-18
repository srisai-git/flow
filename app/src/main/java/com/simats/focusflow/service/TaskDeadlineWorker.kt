package com.simats.focusflow.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simats.focusflow.MainActivity
import com.simats.focusflow.R

class TaskDeadlineWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getInt(TaskDeadlineReceiver.EXTRA_TASK_ID, 0)
        val taskTitle = inputData.getString(TaskDeadlineReceiver.EXTRA_TASK_TITLE) ?: "Task"

        NotificationHelper.createNotificationChannels(context)

        if (!NotificationHelper.hasNotificationPermission(context)) {
            return Result.success()
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "tasks")
            putExtra("navigate_to", "tasks")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            20000 + taskId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_TASK_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_flow)
            .setColor(0xFF8B5CF6.toInt())
            .setContentTitle("Deadline Reached")
            .setContentText("Task \"$taskTitle\" has reached its scheduled deadline.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Task \"$taskTitle\" has reached its scheduled deadline."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(20000 + taskId, notification)

        return Result.success()
    }
}
