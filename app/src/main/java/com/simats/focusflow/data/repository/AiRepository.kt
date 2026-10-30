package com.simats.focusflow.data.repository

import com.simats.focusflow.data.api.FlowApiService
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.models.AiRequest
import com.simats.focusflow.data.models.AiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository interfacing with Groot AI backend proxy and local AI microservice.
 */
class AiRepository(
    private val apiService: FlowApiService = RetrofitClient.flowApiService
) {
    suspend fun getAiInsights(
        userId: Int,
        queryType: String = "general",
        prompt: String? = null
    ): Result<AiResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAiInsights(AiRequest(userId = userId, queryType = queryType, prompt = prompt))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "AI service temporarily unavailable"
                Result.success(
                    AiResponse(
                        status = "fallback",
                        insight = "Maintain your current routine by focusing on your primary high-priority task. Consistent small steps lead to compound productivity.",
                        message = errorMsg
                    )
                )
            }
        } catch (e: Exception) {
            Result.success(
                AiResponse(
                    status = "offline",
                    insight = "Focus on one high-impact task at a time. A single 25-minute Pomodoro block will build progressive momentum.",
                    message = e.localizedMessage
                )
            )
        }
    }

    suspend fun getHabitInsights(userId: Int): Result<String> = withContext(Dispatchers.IO) {
        val res = getAiInsights(userId = userId, queryType = "habit_insights")
        res.map { it.insight ?: it.message ?: "Consistency compounds over time. Keep your daily routines active to maintain peak streak momentum." }
    }

    suspend fun getFocusAdvice(userId: Int): Result<String> = withContext(Dispatchers.IO) {
        val res = getAiInsights(userId = userId, queryType = "focus_advice")
        res.map { it.insight ?: it.message ?: "Structure your workday into focused blocks with short breathers to maximize cognitive stamina." }
    }

    suspend fun askGroot(userId: Int, userQuestion: String): Result<String> = withContext(Dispatchers.IO) {
        val res = getAiInsights(userId = userId, queryType = "chat", prompt = userQuestion)
        res.map { it.insight ?: it.message ?: "I am Groot. Focus on what matters most today." }
    }
}

