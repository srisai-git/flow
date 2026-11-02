package com.simats.focusflow.data

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.UpdateTokenRequest
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object TokenSyncHelper {
    private const val TAG = "FlowTokenSync"

    /**
     * Sends a request to update_token.php using RetrofitClient's configured baseUrl.
     * Logs success or failure with tag "FlowTokenSync".
     */
    suspend fun syncFcmToken(token: String, userId: Int): Boolean {
        if (token.isBlank()) {
            Log.w(TAG, "Empty FCM token provided, skipping token sync.")
            return false
        }
        if (userId <= 0) {
            Log.w(TAG, "Invalid userId ($userId) provided, skipping token sync.")
            return false
        }

        return withContext(Dispatchers.IO) {
            // 1. Primary approach: Use RetrofitClient with constant baseUrl
            try {
                Log.d(TAG, "Attempting Retrofit token sync to ${RetrofitClient.baseUrl}update_token.php for user $userId")
                val response = RetrofitClient.flowApiService.updateFcmToken(
                    UpdateTokenRequest(userId = userId, fcmToken = token)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "Token sync succeeded via Retrofit: ${body?.message ?: "OK"}")
                    return@withContext true
                } else {
                    Log.w(TAG, "Retrofit token sync returned HTTP ${response.code()}: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Retrofit token sync failed: ${e.message}", e)
            }

            // 2. Secondary approach: Direct POST to RetrofitClient.baseUrl + update_token.php
            val retrofitTargetUrl = "${RetrofitClient.baseUrl}update_token.php"
            val success = postToken(token, userId, retrofitTargetUrl)
            if (success) return@withContext true

            // 3. Fallbacks: Local LAN / Emulator endpoints if reachable
            val fallbackUrls = listOf(
                "http://10.0.2.2/flow_backend/update_token.php",
                "http://172.23.176.89/flow_backend/update_token.php",
                "http://10.0.2.2/update_token.php",
                "http://172.23.176.89/update_token.php"
            )
            for (url in fallbackUrls) {
                if (postToken(token, userId, url)) {
                    return@withContext true
                }
            }

            false
        }
    }

    /**
     * Resolves the active user ID from SessionManager and the active FCM registration token,
     * then dispatches synchronization to the backend.
     */
    suspend fun syncForCurrentUser(context: Context, tokenOverride: String? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sessionManager = SessionManager.getInstance(context)
                val session = sessionManager.userSessionFlow.first()
                val userId = session.userId ?: 0
                if (userId <= 0) {
                    Log.d(TAG, "No logged-in user found in SessionManager yet (userId: $userId).")
                    return@withContext false
                }

                val token = tokenOverride ?: try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    Log.w(TAG, "Could not retrieve Firebase token asynchronously: ${e.message}")
                    null
                }

                if (token.isNullOrBlank()) {
                    Log.w(TAG, "No valid FCM token available to sync.")
                    return@withContext false
                }

                syncFcmToken(token, userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error in syncForCurrentUser: ${e.message}", e)
                false
            }
        }
    }

    private fun postToken(token: String, userId: Int, endpointUrl: String): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            Log.d(TAG, "Sending direct HTTP token sync request to: $endpointUrl (userId: $userId)")
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 5000
                readTimeout = 5000
                doOutput = true
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject().apply {
                put("user_id", userId)
                put("fcm_token", token)
            }.toString()

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(payload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            if (responseCode in 200..299) {
                Log.d(TAG, "Direct HTTP sync succeeded (HTTP $responseCode): $responseBody")
                true
            } else {
                Log.w(TAG, "Direct HTTP sync failed with HTTP $responseCode: $responseBody")
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Direct HTTP sync connection error for $endpointUrl: ${e.message}")
            false
        } finally {
            connection?.disconnect()
        }
    }
}
