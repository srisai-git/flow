package com.simats.focusflow

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.service.NotificationHelper

class FocusFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("FocusFlowApplication", "FirebaseApp initialization failed: ${e.message}")
        }

        try {
            ProductivityRepository.init(this)
        } catch (e: Exception) {
            Log.e("FocusFlowApplication", "ProductivityRepository initialization failed: ${e.message}")
        }

        try {
            NotificationHelper.createNotificationChannels(this)
        } catch (e: Exception) {
            Log.e("FocusFlowApplication", "NotificationHelper initialization failed: ${e.message}")
        }
    }
}

