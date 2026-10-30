package com.simats.focusflow.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.repository.AiRepository
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Immutable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isFromUser: Boolean,
    val text: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

@Immutable
data class GrootAiUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            isFromUser = false,
            text = "I am Groot, your versatile FocusFlow AI assistant. While my specialty is productivity, time management, and habit optimization, I am ready to assist you with any technical, academic, coding, creative, or general questions you have. How can I help you today?"
        )
    ),
    val isAiThinking: Boolean = false,
    val inputText: String = ""
)

class GrootAiViewModel(
    private val aiRepository: AiRepository = AiRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrootAiUiState())
    val uiState: StateFlow<GrootAiUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 1

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        viewModelScope.launch {
            val session = manager.userSessionFlow.first()
            if (session.userId != null && session.userId > 0) {
                currentUserId = session.userId
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun sendMessage(query: String? = null) {
        val textToSend = (query ?: _uiState.value.inputText).trim()
        if (textToSend.isBlank() || _uiState.value.isAiThinking) return

        val userMessage = ChatMessage(isFromUser = true, text = textToSend)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = if (query == null) "" else it.inputText,
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            val queryType = when {
                textToSend.contains("consistency", ignoreCase = true) || textToSend.contains("habit", ignoreCase = true) -> "habit_insights"
                textToSend.contains("rhythm", ignoreCase = true) || textToSend.contains("focus", ignoreCase = true) -> "focus_advice"
                textToSend.contains("summary", ignoreCase = true) || textToSend.contains("progress", ignoreCase = true) -> "daily_summary"
                else -> "chat"
            }

            val result = aiRepository.getAiInsights(
                userId = currentUserId,
                queryType = queryType,
                prompt = textToSend
            )

            result.fold(
                onSuccess = { response ->
                    val aiText = response.insight ?: response.message ?: "I am Groot. Focus on your top priority task."
                    val aiMessage = ChatMessage(isFromUser = false, text = aiText)
                    _uiState.update {
                        it.copy(
                            messages = it.messages + aiMessage,
                            isAiThinking = false
                        )
                    }
                },
                onFailure = {
                    val fallbackText = when {
                        textToSend.matches(Regex("(?i)^(hi|hello|hey|greetings).*")) ->
                            "Hello. I am Groot. How can I help you today?"
                        textToSend.contains("who are you", ignoreCase = true) ->
                            "I am Groot, your AI assistant in FocusFlow."
                        else ->
                            "Break down this objective into clear steps and maintain focused execution."
                    }
                    val fallbackMessage = ChatMessage(
                        isFromUser = false,
                        text = fallbackText
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + fallbackMessage,
                            isAiThinking = false
                        )
                    }
                }
            )
        }
    }
}
