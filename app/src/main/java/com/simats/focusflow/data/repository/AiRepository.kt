package com.simats.focusflow.data.repository

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
                    history = activeHistory,
                    queryType = queryType,
                    maxTokens = 400,
                    temperature = 0.3f
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val reply = body.insight ?: body.message ?: ""
                if (userPrompt.isNotBlank()) {
                    synchronized(_history) {
                        _history.add(ChatHistoryMessage(role = "user", content = userPrompt))
                        if (reply.isNotBlank()) {
                            _history.add(ChatHistoryMessage(role = "assistant", content = reply))
                        }
                    }
                }
                Result.success(body)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "AI service temporarily unavailable"
                Result.success(
                    AiResponse(
                        status = "fallback",
                        insight = "I am Groot. Focus on your highest priority task, maintain deep work intervals, and keep momentum going.",
                        message = errorMsg
                    )
                )
            }
        } catch (e: Exception) {
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
                        insight = directReply,
                        message = directReply
                    )
                )
            } else {
                Result.success(
                    AiResponse(
                        status = "offline",
                        insight = "I am Groot, your FocusFlow assistant. I am ready to help, but my local deep-reasoning engine (port 8000) is currently offline. Please ensure 'python server.py' is running in the groot_ai directory.",
                        message = e.localizedMessage
                    )
                )
            }
        }
    }

    private fun tryDirectFastApiCall(promptText: String): String? {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val jsonPayload = com.google.gson.Gson().toJson(mapOf("message" to promptText, "prompt" to promptText))
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonPayload.toRequestBody(mediaType)

        val urls = listOf(
            "http://10.0.2.2:8000/ask_groot",
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
                    val reply = json.get("response")?.asString
                        ?: json.get("reply")?.asString
                        ?: json.get("insight")?.asString
                        ?: json.get("message")?.asString
                    if (!reply.isNullOrBlank()) {
                        return reply
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    suspend fun getHabitInsights(userId: Int): Result<String> = withContext(Dispatchers.IO) {
        val res = getAiInsights(userId = userId, queryType = "habit_insights", prompt = "Analyze my habit consistency.")
        res.map { it.insight ?: it.message ?: "Consistency compounds over time. Keep your daily routines active to maintain peak streak momentum." }
    }

    suspend fun getFocusAdvice(userId: Int): Result<String> = withContext(Dispatchers.IO) {
        val res = getAiInsights(userId = userId, queryType = "focus_advice", prompt = "Provide advice on my focus blocks.")
        res.map { it.insight ?: it.message ?: "Structure your workday into focused blocks with short breathers to maximize cognitive stamina." }
    }

    suspend fun askGroot(userId: Int, userQuestion: String): Result<String> = withContext(Dispatchers.IO) {
        val res = getAiInsights(userId = userId, queryType = "chat", prompt = userQuestion)
        res.map { it.insight ?: it.message ?: "I am Groot. Focus on what matters most today." }
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
        } catch (_: Exception) {}

        listOf(
            "Define requirements and scope for $trimmed",
            "Draft initial implementation for $trimmed",
            "Review, test and verify $trimmed",
            "Finalize and mark $trimmed complete"
        )
    }

    private fun tryFastApiBreakdown(taskTitle: String): List<String> {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val jsonPayload = com.google.gson.Gson().toJson(mapOf("task_title" to taskTitle, "title" to taskTitle))
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonPayload.toRequestBody(mediaType)

        val urls = listOf(
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
                }
            } catch (_: Exception) {}
        }
        return emptyList()
    }
}
