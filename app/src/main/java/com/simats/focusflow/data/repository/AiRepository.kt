package com.simats.focusflow.data.repository

import android.util.Log
import com.simats.focusflow.data.api.FlowApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.AiRequest
import com.simats.focusflow.data.models.AiResponse
import com.simats.focusflow.data.models.ChatHistoryMessage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepository(
    private val apiService: FlowApiService = RetrofitClient.flowApiService
) {
    private val _history = mutableListOf<ChatHistoryMessage>()
    val conversationHistory: List<ChatHistoryMessage>
        get() = synchronized(_history) { _history.toList() }

    fun clearHistory() {
        synchronized(_history) { _history.clear() }
    }

    suspend fun getAiInsights(
        userId: Int,
        queryType: String = "chat",
        prompt: String? = null,
        history: List<ChatHistoryMessage>? = null
    ): Result<AiResponse> = withContext(Dispatchers.IO) {
        val userPrompt = prompt ?: ""
        try {
            val activeHistory = history ?: synchronized(_history) { _history.toList() }

            val response = apiService.getAiInsights(
                AiRequest(
                    userId = userId,
                    prompt = userPrompt,
                    message = userPrompt,
                    query = userPrompt,
                    text = userPrompt,
                    history = activeHistory,
                    queryType = queryType,
                    maxTokens = 400,
                    temperature = 0.3f
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val reply = body.resolvedText.ifBlank {
                    body.reply ?: body.response ?: body.message ?: body.insight ?: ""
                }
                if (userPrompt.isNotBlank()) {
                    synchronized(_history) {
                        _history.add(ChatHistoryMessage(role = "user", content = userPrompt))
                        if (reply.isNotBlank()) {
                            _history.add(ChatHistoryMessage(role = "assistant", content = reply))
                        }
                    }
                }
                Result.success(
                    body.copy(
                        reply = reply,
                        response = reply,
                        message = reply,
                        insight = reply,
                        text = reply
                    )
                )
            } else {
                val errorMsg = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                Log.e("GrootAI", "Retrofit getAiInsights failed: code=${response.code()}, error=$errorMsg")
                val directReply = tryDirectFastApiCall(userPrompt)
                if (!directReply.isNullOrBlank()) {
                    if (userPrompt.isNotBlank()) {
                        synchronized(_history) {
                            _history.add(ChatHistoryMessage(role = "user", content = userPrompt))
                            _history.add(ChatHistoryMessage(role = "assistant", content = directReply))
                        }
                    }
                    Result.success(
                        AiResponse(
                            status = "success",
                            reply = directReply,
                            response = directReply,
                            message = directReply,
                            insight = directReply,
                            text = directReply
                        )
                    )
                } else {
                    Result.success(
                        AiResponse(
                            status = "fallback",
                            insight = "I am Groot. Focus on your highest priority task, maintain deep work intervals, and keep momentum going. ($errorMsg)",
                            message = errorMsg,
                            reply = "I am Groot. Focus on your highest priority task, maintain deep work intervals, and keep momentum going. ($errorMsg)",
                            response = "I am Groot. Focus on your highest priority task, maintain deep work intervals, and keep momentum going. ($errorMsg)",
                            text = "I am Groot. Focus on your highest priority task, maintain deep work intervals, and keep momentum going. ($errorMsg)"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("GrootAI", "Retrofit getAiInsights exception: ${e.message}", e)
            val directReply = tryDirectFastApiCall(userPrompt)
            if (!directReply.isNullOrBlank()) {
                if (userPrompt.isNotBlank()) {
                    synchronized(_history) {
                        _history.add(ChatHistoryMessage(role = "user", content = userPrompt))
                        _history.add(ChatHistoryMessage(role = "assistant", content = directReply))
                    }
                }
                Result.success(
                    AiResponse(
                        status = "success",
                        reply = directReply,
                        response = directReply,
                        message = directReply,
                        insight = directReply,
                        text = directReply
                    )
                )
            } else {
                Result.success(
                    AiResponse(
                        status = "offline",
                        insight = "I am Groot, your FocusFlow assistant. Network error reaching AI engine: ${e.localizedMessage ?: e.message}",
                        message = e.localizedMessage ?: e.message ?: "Connection error",
                        reply = "I am Groot, your FocusFlow assistant. Network error reaching AI engine: ${e.localizedMessage ?: e.message}",
                        response = "I am Groot, your FocusFlow assistant. Network error reaching AI engine: ${e.localizedMessage ?: e.message}",
                        text = "I am Groot, your FocusFlow assistant. Network error reaching AI engine: ${e.localizedMessage ?: e.message}"
                    )
                )
            }
        }
    }

    private fun tryDirectFastApiCall(promptText: String): String? {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val jsonPayload = com.google.gson.Gson().toJson(
            mapOf(
                "message" to promptText,
                "prompt" to promptText,
                "query" to promptText,
                "text" to promptText
            )
        )
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonPayload.toRequestBody(mediaType)

        val urls = listOf(
            "https://focusflow-groot-ai.onrender.com/chat",
            "https://focusflow-groot-ai.onrender.com/ask_groot",
            "http://10.0.2.2:8000/chat",
            "http://10.0.2.2:8000/ask_groot",
            "http://127.0.0.1:8000/chat",
            "http://127.0.0.1:8000/ask_groot"
        )
        for (url in urls) {
            try {
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .post(body)
                    .build()
                val resp = client.newCall(request).execute()
                if (resp.isSuccessful) {
                    val respStr = resp.body?.string() ?: ""
                    val json = com.google.gson.JsonParser.parseString(respStr).asJsonObject
                    val reply = json.get("reply")?.asString
                        ?: json.get("response")?.asString
                        ?: json.get("message")?.asString
                        ?: json.get("text")?.asString
                        ?: json.get("insight")?.asString
                    if (!reply.isNullOrBlank()) {
                        return reply
                    }
                } else {
                    val errBody = resp.body?.string() ?: ""
                    Log.e("GrootAI", "Direct OkHttp call to $url failed with HTTP ${resp.code}: $errBody")
                }
            } catch (e: Exception) {
                Log.e("GrootAI", "Direct OkHttp call to $url exception: ${e.message}", e)
            }
        }
        return null
    }

    suspend fun getHabitInsights(userId: Int): Result<String> = withContext(Dispatchers.IO) {
        val res = getAiInsights(userId = userId, queryType = "habit_insights", prompt = "Analyze my habit consistency.")
        res.map { it.resolvedText.ifBlank { it.reply ?: it.response ?: it.message ?: it.insight ?: "Consistency compounds over time. Keep your daily routines active to maintain peak streak momentum." } }
    }

    suspend fun getFocusAdvice(userId: Int): Result<String> = withContext(Dispatchers.IO) {
        val res = getAiInsights(userId = userId, queryType = "focus_advice", prompt = "Provide advice on my focus blocks.")
        res.map { it.resolvedText.ifBlank { it.reply ?: it.response ?: it.message ?: it.insight ?: "Structure your workday into focused blocks with short breathers to maximize cognitive stamina." } }
    }

    suspend fun askGroot(userId: Int, userQuestion: String): Result<String> = withContext(Dispatchers.IO) {
        val res = getAiInsights(userId = userId, queryType = "chat", prompt = userQuestion)
        res.map { it.resolvedText.ifBlank { it.reply ?: it.response ?: it.message ?: it.insight ?: "I am Groot. Focus on what matters most today." } }
    }

    suspend fun breakdownTask(taskTitle: String): List<String> = withContext(Dispatchers.IO) {
        val trimmed = taskTitle.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val fastApiResult = tryFastApiBreakdown(trimmed)
        if (fastApiResult.isNotEmpty()) {
            return@withContext fastApiResult
        }

        try {
            val response = apiService.breakdownTask(com.simats.focusflow.data.models.TaskBreakdownRequest(taskTitle = trimmed, title = trimmed))
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.subtasks
                if (list.isNotEmpty()) return@withContext list
            }
        } catch (e: Exception) {
            Log.e("GrootAI", "Retrofit breakdownTask exception: ${e.message}", e)
        }

        listOf(
            "Define requirements and scope for $trimmed",
            "Draft initial implementation for $trimmed",
            "Review, test and verify $trimmed",
            "Finalize and mark $trimmed complete"
        )
    }

    private fun tryFastApiBreakdown(taskTitle: String): List<String> {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val jsonPayload = com.google.gson.Gson().toJson(
            mapOf(
                "task_title" to taskTitle,
                "title" to taskTitle,
                "prompt" to taskTitle
            )
        )
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonPayload.toRequestBody(mediaType)

        val urls = listOf(
            "https://focusflow-groot-ai.onrender.com/breakdown_task",
            "http://10.0.2.2:8000/breakdown_task",
            "http://127.0.0.1:8000/breakdown_task"
        )
        for (url in urls) {
            try {
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .post(body)
                    .build()
                val resp = client.newCall(request).execute()
                if (resp.isSuccessful) {
                    val respStr = resp.body?.string() ?: ""
                    val json = com.google.gson.JsonParser.parseString(respStr).asJsonObject
                    val subtasksArr = json.getAsJsonArray("subtasks")
                    if (subtasksArr != null && subtasksArr.size() > 0) {
                        return subtasksArr.map { it.asString }
                    }
                } else {
                    Log.e("GrootAI", "Direct OkHttp breakdown call to $url failed with HTTP ${resp.code}")
                }
            } catch (e: Exception) {
                Log.e("GrootAI", "Direct OkHttp breakdown call to $url exception: ${e.message}", e)
            }
        }
        return emptyList()
    }
}
