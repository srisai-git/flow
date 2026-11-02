package com.simats.focusflow.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.simats.focusflow.data.TokenSyncHelper
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FlowFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FlowFCM"
        const val CHANNEL_ID = NotificationHelper.CHANNEL_REMOTE_ALERTS
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

        val route = remoteMessage.data["route"]
            ?: remoteMessage.data["screen"]
            ?: remoteMessage.notification?.clickAction

        NotificationHelper.showNotification(
            context = applicationContext,
            title = rawTitle,
            body = rawBody,
            route = route,
            channelId = CHANNEL_ID
        )
    }
}
