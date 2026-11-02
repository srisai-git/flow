package com.simats.focusflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.simats.focusflow.MainActivity
import com.simats.focusflow.R
import com.simats.focusflow.data.TokenSyncHelper
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FlowFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FlowFCM"
        const val CHANNEL_ID = "flow_remote_alerts"
        const val CHANNEL_NAME = "Flow Alerts & Reminders"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            val sessionManager = SessionManager.getInstance(applicationContext)
            val userId = try {
                sessionManager.userSessionFlow.first().userId ?: 1
            } catch (e: Exception) {
                1
            }
            TokenSyncHelper.syncFcmToken(token, userId)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val rawTitle = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "FocusFlow"

        val rawBody = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "New notification received."

        // Strict Zero-Emoji Policy: purge emoji characters
        val cleanTitle = purgeEmojis(rawTitle)
        val cleanBody = purgeEmojis(rawBody)

        showNotification(cleanTitle, cleanBody)
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts and schedule reminders from FocusFlow"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_flow)
            .setColor(0xFF8B5CF6.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notificationId, notification)
    }

    private fun purgeEmojis(text: String): String {
        return text.replace(Regex("[\\x{10000}-\\x{10FFFF}\\x{2600}-\\x{27BF}\\x{2300}-\\x{23FF}\\x{2B50}\\x{2B55}]"), "").trim()
    }
}

