package com.simats.focusflow.data.repository

import com.simats.focusflow.data.api.FlowApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.AiRequest
import com.simats.focusflow.data.models.AiResponse
import com.simats.focusflow.data.models.ChatHistoryMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository interfacing with Groot AI backend proxy and local AI microservice.
 */
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
        try {
            val userPrompt = prompt ?: ""
            val activeHistory = history ?: synchronized(_history) { _history.toList() }

            val response = apiService.getAiInsights(
                AiRequest(
                    userId = userId,
                    prompt = userPrompt,
                    history = activeHistory,
                    queryType = queryType
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
            Result.success(
                AiResponse(
                    status = "offline",
                    insight = "I am Groot, your FocusFlow assistant. I am ready to help, but my local deep-reasoning engine (port 8000) is currently offline. Please ensure 'python server.py' is running in the groot_ai directory.",
                    message = e.localizedMessage
                )
            )
        }
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
}
