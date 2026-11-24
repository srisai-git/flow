package com.simats.focusflow.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.focusflow.data.models.AiAction
import com.simats.focusflow.data.repository.AiRepository
import com.simats.focusflow.data.repository.GrootChatRepository
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.repository.ProfileRepository
import com.simats.focusflow.data.session.BlockedAppsManager
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.service.FocusTimerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Immutable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isFromUser: Boolean,
    val text: String,
    val action: AiAction? = null,
    val actions: List<AiAction> = emptyList(),
    val actionExecuted: Boolean = false,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
) {
    val allActions: List<AiAction>
        get() = if (actions.isNotEmpty()) actions else listOfNotNull(action)
}

@Immutable
data class GrootAiUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            isFromUser = false,
            text = "I am Groot, your AI productivity companion inside FocusFlow. Named after the resilient, loyal guardian from Guardians of the Galaxy, I embody steady growth and unwavering focus. I am here to help you manage your focus intervals, track habit streaks, eliminate distractions, and cultivate deep flow. How can I assist your productivity today?"
        )
    ),
    val isAiThinking: Boolean = false,
    val isGenerating: Boolean = false,
    val inputText: String = ""
)

class GrootAiViewModel(
    private val aiRepository: AiRepository = AiRepository(),
    private val productivityRepository: ProductivityRepository = ProductivityRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private var sessionManager: SessionManager? = null
) : ViewModel() {

    private val chatRepo: GrootChatRepository by lazy {
        GrootChatRepository.getInstance(appContext)
    }

    val uiState: StateFlow<GrootAiUiState>
        get() = chatRepo.uiState

    val navigationEvent: SharedFlow<String>
        get() = chatRepo.navigationEvent

    private var appContext: Context? = null
    private var currentUserId: Int = 1

    fun initContext(context: Context, manager: SessionManager) {
        this.appContext = context.applicationContext
        this.sessionManager = manager
        GrootChatRepository.init(context)
        viewModelScope.launch(Dispatchers.IO) {
            val session = manager.userSessionFlow.first()
            if (session.userId != null && session.userId > 0) {
                currentUserId = session.userId
            }
        }
    }

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        appContext?.let { GrootChatRepository.init(it) }
        viewModelScope.launch(Dispatchers.IO) {
            val session = manager.userSessionFlow.first()
            if (session.userId != null && session.userId > 0) {
                currentUserId = session.userId
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        chatRepo.onInputTextChanged(newText)
    }

    fun clearChat() {
        chatRepo.clearChat()
    }

    fun sendMessage(query: String? = null) {
        chatRepo.sendMessage(query, currentUserId, sessionManager)
    }

    fun stopGeneration() {
        chatRepo.stopGeneration()
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun handleActionClick(action: AiAction, onNavigate: (String) -> Unit) {
        val type = action.type.uppercase()
        when {
            type.contains("TIMER") || type.contains("STRICT") -> onNavigate("timer")
            type.contains("TASK") -> onNavigate("tasks")
            type.contains("HABIT") -> onNavigate("habits")
            type.contains("SUMMARY") || type.contains("ANALYTIC") || type.contains("INSIGHT") -> onNavigate("insights")
            type.contains("GOAL") -> onNavigate("profile")
            type.contains("NAVIGATE") -> {
                val target = action.resolvedRoute.lowercase()
                val route = when {
                    target.contains("task") -> "tasks"
                    target.contains("timer") || target.contains("focus") -> "timer"
                    target.contains("habit") -> "habits"
                    target.contains("insight") || target.contains("analytic") -> "insights"
                    target.contains("profile") || target.contains("setting") -> "profile"
                    target.contains("home") || target.contains("dash") -> "dashboard"
                    else -> target.ifBlank { "dashboard" }
                }
                onNavigate(route)
            }
            else -> onNavigate(action.resolvedRoute.ifBlank { "dashboard" })
        }
    }

    fun undoAction(action: AiAction) {
        val type = action.type.uppercase()
        val context = appContext ?: return
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "START_FOCUS_TIMER", "START_TIMER" -> {
                    withContext(Dispatchers.Main) {
                        FocusTimerService.resetTimer(context)
                    }
                }
                "ENABLE_STRICT_MODE" -> {
                    withContext(Dispatchers.Main) {
                        FocusTimerService.setStrictFocus(context, false)
                    }
                }
                "DISABLE_STRICT_MODE" -> {
                    withContext(Dispatchers.Main) {
                        FocusTimerService.setStrictFocus(context, true)
                    }
                }
                "BLOCK_APPS" -> {
                    val apps = (action.payload["apps"] as? List<*>)?.mapNotNull { it?.toString() }
                        ?: listOfNotNull(action.payload["title"]?.toString(), action.resolvedTitle.takeIf { it.isNotBlank() })
                    withContext(Dispatchers.Main) {
                        BlockedAppsManager.getInstance(context).blockAppsByNames(apps, block = false)
                    }
                }
                "UNBLOCK_APPS" -> {
                    val apps = (action.payload["apps"] as? List<*>)?.mapNotNull { it?.toString() }
                        ?: listOfNotNull(action.payload["title"]?.toString(), action.resolvedTitle.takeIf { it.isNotBlank() })
                    withContext(Dispatchers.Main) {
                        BlockedAppsManager.getInstance(context).blockAppsByNames(apps, block = true)
                    }
                }
                "CREATE_TASK" -> {
                    val targetTitle = action.resolvedTitle
                    val tasks = productivityRepository.getTasks(currentUserId).getOrNull() ?: emptyList()
                    val match = tasks.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                    if (match != null) {
                        productivityRepository.deleteTask(currentUserId, match.id)
                    }
                }
                "COMPLETE_TASK" -> {
                    val targetTitle = action.resolvedTitle
                    val tasks = productivityRepository.getTasks(currentUserId).getOrNull() ?: emptyList()
                    val match = tasks.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                    if (match != null) {
                        productivityRepository.toggleTask(currentUserId, match.id, isCompleted = false)
                    }
                }
                "CREATE_HABIT" -> {
                    val targetTitle = action.resolvedTitle
                    val habits = productivityRepository.getHabits(currentUserId).getOrNull()?.habits ?: emptyList()
                    val match = habits.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                    if (match != null) {
                        productivityRepository.deleteHabit(currentUserId, match.id)
                    }
                }
                "CHECK_OFF_HABIT", "LOG_HABIT" -> {
                    val targetTitle = action.resolvedTitle
                    val habits = productivityRepository.getHabits(currentUserId).getOrNull()?.habits ?: emptyList()
                    val match = habits.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                    if (match != null && match.isCompletedToday) {
                        productivityRepository.toggleHabit(currentUserId, match.id)
                    }
                }
            }

            val undoMsg = ChatMessage(
                isFromUser = false,
                text = "Undid action: ${action.title.ifBlank { action.type }}"
            )
            chatRepo.persistMessages(chatRepo.uiState.value.messages + undoMsg)
        }
    }
}
