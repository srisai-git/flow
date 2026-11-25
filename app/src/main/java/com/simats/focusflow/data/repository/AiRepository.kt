package com.simats.focusflow.data.repository

import com.simats.focusflow.data.models.AiAction
import com.simats.focusflow.data.models.AiResponse
import com.simats.focusflow.data.models.ChatHistoryMessage
import com.simats.focusflow.utils.LocalGrootBrain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepository {
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
        history: List<ChatHistoryMessage>? = null,
        appContext: String? = null,
        appKnowledge: String? = null
    ): Result<AiResponse> = withContext(Dispatchers.IO) {
        val userPrompt = prompt ?: ""
        val brainResult = LocalGrootBrain.process(
            query = userPrompt,
            context = ProductivityRepository.appContext,
            userId = userId
        )
        if (userPrompt.isNotBlank()) {
            synchronized(_history) {
                _history.add(ChatHistoryMessage(role = "user", content = userPrompt))
                _history.add(ChatHistoryMessage(role = "assistant", content = brainResult.reply))
            }
        }
        val safeText = brainResult.reply.ifBlank {
            when (queryType) {
                "habit_insights", "habit_report" -> "Consistency compounds over time. Keep your daily routines active to maintain peak streak momentum."
                "focus_advice" -> "Structure your workday into focused blocks with short breathers to maximize cognitive stamina."
                else -> "Hey! Ready to get things done today? Let me know if you want to start a focus session or add a task."
            }
        }
        Result.success(
            AiResponse(
                status = "success",
                reply = safeText,
                response = safeText,
                message = safeText,
                insight = safeText,
                text = safeText,
                action = brainResult.actions.firstOrNull(),
                actions = brainResult.actions
            )
        )
    }

    suspend fun getHabitInsights(userId: Int, prompt: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val habits = ProductivityRepository().getHabits(userId).getOrNull()?.habits ?: emptyList()
        Result.success(LocalGrootBrain.getLocalHabitInsights(habits))
    }

    suspend fun getFocusAdvice(userId: Int): Result<String> = withContext(Dispatchers.IO) {
        val ctx = LocalGrootBrain.loadUserContext(ProductivityRepository.appContext, userId)
        Result.success(LocalGrootBrain.getLocalFocusAdvice(ctx.todayFocusMinutes, ctx.totalDistractionMinutes, ctx.streak))
    }

    suspend fun askGroot(userId: Int, userQuestion: String): Result<String> = withContext(Dispatchers.IO) {
        val res = LocalGrootBrain.process(userQuestion, ProductivityRepository.appContext, userId)
        Result.success(res.reply)
    }

    suspend fun breakdownTask(taskTitle: String): List<String> = withContext(Dispatchers.IO) {
        val trimmed = taskTitle.trim()
        if (trimmed.isBlank()) return@withContext emptyList()
        LocalGrootBrain.breakdownTaskLocally(trimmed)
    }
}
