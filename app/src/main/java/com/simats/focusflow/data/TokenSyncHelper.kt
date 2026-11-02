package com.simats.focusflow.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object TokenSyncHelper {
    private const val TAG = "FlowTokenSync"
    const val PRIMARY_URL = "http://172.23.176.89/update_token.php"
    private const val FALLBACK_URL = "http://10.0.2.2/update_token.php"

    /**
     * Sends a POST request to update_token.php with the user_id and fcm_token.
     * Logs success or failure with tag "FlowTokenSync".
     */
    suspend fun syncFcmToken(token: String, userId: Int = 1, targetUrl: String = PRIMARY_URL): Boolean {
        if (token.isBlank()) {
            Log.w(TAG, "Empty FCM token provided, skipping token sync.")
            return false
        }

        return withContext(Dispatchers.IO) {
            val success = postToken(token, userId, targetUrl)
            if (!success && targetUrl == PRIMARY_URL) {
                // If primary LAN IP connection fails (e.g. running in Android emulator), attempt fallback
                Log.d(TAG, "Primary sync failed, attempting fallback to $FALLBACK_URL...")
                postToken(token, userId, FALLBACK_URL)
            } else {
                success
            }
        }
    }

    private fun postToken(token: String, userId: Int, endpointUrl: String): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            Log.d(TAG, "Sending token sync request to: $endpointUrl (userId: $userId)")
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 7000
                readTimeout = 7000
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
                Log.d(TAG, "Token sync succeeded (HTTP $responseCode): $responseBody")
                true
            } else {
                Log.e(TAG, "Token sync failed with HTTP $responseCode: $responseBody")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token sync error for $endpointUrl: ${e.message}", e)
            false
        } finally {
            connection?.disconnect()
        }
    }
}

