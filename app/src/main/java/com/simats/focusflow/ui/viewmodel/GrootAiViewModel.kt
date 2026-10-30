package com.simats.focusflow.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.AiAction
import com.simats.focusflow.data.models.ChatHistoryMessage
import com.simats.focusflow.data.repository.AiRepository
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.repository.ProfileRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.service.FocusTimerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Immutable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isFromUser: Boolean,
    val text: String,
    val action: AiAction? = null,
    val actionExecuted: Boolean = false,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

@Immutable
data class GrootAiUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            isFromUser = false,
            text = "I am Groot, your autonomous FocusFlow AI collaborator. You can ask me anything across coding, math, general topics, or command me to perform in-app actions like starting focus timers, scheduling tasks, building habits, and analyzing your consistency."
        )
    ),
    val isAiThinking: Boolean = false,
    val inputText: String = ""
)

class GrootAiViewModel(
    private val aiRepository: AiRepository = AiRepository(),
    private val productivityRepository: ProductivityRepository = ProductivityRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrootAiUiState())
    val uiState: StateFlow<GrootAiUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    private var appContext: Context? = null
    private var currentUserId: Int = 1

    fun initContext(context: Context, manager: SessionManager) {
        this.appContext = context.applicationContext
        this.sessionManager = manager
        viewModelScope.launch(Dispatchers.IO) {
            val session = manager.userSessionFlow.first()
            if (session.userId != null && session.userId > 0) {
                currentUserId = session.userId
            }
        }
    }

    // Compatibility overload
    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        viewModelScope.launch(Dispatchers.IO) {
            val session = manager.userSessionFlow.first()
            if (session.userId != null && session.userId > 0) {
                currentUserId = session.userId
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun clearChat() {
        aiRepository.clearHistory()
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        isFromUser = false,
                        text = "I am Groot, your autonomous FocusFlow AI collaborator. You can ask me anything across coding, math, general topics, or command me to perform in-app actions like starting focus timers, scheduling tasks, building habits, and analyzing your consistency."
                    )
                ),
                inputText = "",
                isAiThinking = false
            )
        }
    }

    fun sendMessage(query: String? = null) {
        val textToSend = (query ?: _uiState.value.inputText).trim()
        if (textToSend.isBlank() || _uiState.value.isAiThinking) return

        val userMessage = ChatMessage(isFromUser = true, text = textToSend)
        val currentHistory = _uiState.value.messages.map {
            ChatHistoryMessage(
                role = if (it.isFromUser) "user" else "assistant",
                content = it.text
            )
        }

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = if (query == null) "" else it.inputText,
                isAiThinking = true
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val result = aiRepository.getAiInsights(
                userId = currentUserId,
                queryType = "chat",
                prompt = textToSend,
                history = currentHistory
            )

            result.fold(
                onSuccess = { response ->
                    val rawAiText = response.insight ?: response.message ?: "I am Groot. Focus on your top priority task."
                    val aiText = if (rawAiText.trimStart().startsWith("{") && rawAiText.contains("\"insight\"")) {
                        response.message ?: "I am Groot, ready to assist."
                    } else {
                        rawAiText
                    }

                    val action = response.action
                    var actionExecuted = false

                    // Perform autonomous action if detected
                    if (action != null) {
                        actionExecuted = performAutonomousAction(action)
                    }

                    val aiMessage = ChatMessage(
                        isFromUser = false,
                        text = aiText,
                        action = action,
                        actionExecuted = actionExecuted
                    )

                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                messages = it.messages + aiMessage,
                                isAiThinking = false
                            )
                        }
                    }
                },
                onFailure = {
                    val fallbackText = when {
                        textToSend.matches(Regex("(?i)^(hi|hello|hey|greetings).*")) ->
                            "Hello! I am Groot, your FocusFlow assistant. How can I help you today?"
                        textToSend.contains("who are you", ignoreCase = true) ->
                            "I am Groot, your versatile AI collaborator inside FocusFlow."
                        else ->
                            "I am Groot, your FocusFlow assistant. I am ready to help, but my local deep-reasoning engine (port 8000) is currently offline. Please ensure 'python server.py' is running in the groot_ai directory."
                    }
                    val fallbackMessage = ChatMessage(
                        isFromUser = false,
                        text = fallbackText
                    )
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                messages = it.messages + fallbackMessage,
                                isAiThinking = false
                            )
                        }
                    }
                }
            )
        }
    }

    private fun performAutonomousAction(action: AiAction): Boolean {
        return try {
            when (action.type.uppercase()) {
                "START_TIMER" -> {
                    appContext?.let { ctx ->
                        val duration = action.duration.coerceIn(5, 90)
                        FocusTimerService.setDurations(ctx, duration, 5, 15)
                        FocusTimerService.startTimer(ctx, currentUserId)
                        true
                    } ?: false
                }
                "CREATE_TASK" -> {
                    if (action.title.isNotBlank()) {
                        viewModelScope.launch(Dispatchers.IO) {
                            productivityRepository.createTask(
                                userId = currentUserId,
                                title = action.title,
                                description = "Created automatically by Groot AI",
                                priority = action.priority.ifBlank { "medium" },
                                dueDate = "",
                                category = action.category.ifBlank { "General" }
                            )
                        }
                        true
                    } else false
                }
                "CREATE_HABIT" -> {
                    if (action.title.isNotBlank()) {
                        viewModelScope.launch(Dispatchers.IO) {
                            productivityRepository.createHabit(
                                userId = currentUserId,
                                title = action.title,
                                icon = "bolt",
                                frequency = "daily"
                            )
                        }
                        true
                    } else false
                }
                "SET_GOAL" -> {
                    if (action.dailyMinutes > 0) {
                        viewModelScope.launch(Dispatchers.IO) {
                            profileRepository.updatePreferences(
                                userId = currentUserId,
                                dailyFocusGoalMinutes = action.dailyMinutes,
                                preferredFocusDuration = 45
                            )
                        }
                        true
                    } else false
                }
                "NAVIGATE" -> {
                    if (action.route.isNotBlank()) {
                        _navigationEvent.tryEmit(action.route)
                        true
                    } else false
                }
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun handleActionClick(action: AiAction, onNavigate: (String) -> Unit) {
        when (action.type.uppercase()) {
            "START_TIMER" -> onNavigate("timer")
            "CREATE_TASK" -> onNavigate("tasks")
            "CREATE_HABIT" -> onNavigate("habits")
            "SET_GOAL" -> onNavigate("profile")
            "NAVIGATE" -> onNavigate(action.route.ifBlank { "dashboard" })
        }
    }
}
