package com.simats.focusflow.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simats.focusflow.data.models.AiAction
import com.simats.focusflow.data.models.ChatHistoryMessage
import com.simats.focusflow.data.repository.AiRepository
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.repository.ProfileRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.service.FocusTimerService
import com.simats.focusflow.utils.GrootActionExecutor
import com.simats.focusflow.utils.GrootActionParser
import com.simats.focusflow.utils.GrootGuardrailHelper
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
    private var actionExecutor: GrootActionExecutor? = null
    private val gson = Gson()

    fun initContext(context: Context, manager: SessionManager) {
        this.appContext = context.applicationContext
        this.sessionManager = manager
        this.actionExecutor = GrootActionExecutor(context.applicationContext, productivityRepository, profileRepository)
        loadPersistedMessages()
        viewModelScope.launch(Dispatchers.IO) {
            val session = manager.userSessionFlow.first()
            if (session.userId != null && session.userId > 0) {
                currentUserId = session.userId
            }
        }
    }

    fun initSessionManager(manager: SessionManager) {
        this.sessionManager = manager
        if (actionExecutor == null && appContext != null) {
            this.actionExecutor = GrootActionExecutor(appContext!!, productivityRepository, profileRepository)
        }
        loadPersistedMessages()
        viewModelScope.launch(Dispatchers.IO) {
            val session = manager.userSessionFlow.first()
            if (session.userId != null && session.userId > 0) {
                currentUserId = session.userId
            }
        }
    }

    private fun persistMessages(messages: List<ChatMessage>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                appContext?.let { ctx ->
                    val file = File(ctx.filesDir, "groot_chat_history.json")
                    val json = gson.toJson(messages)
                    file.writeText(json)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadPersistedMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                appContext?.let { ctx ->
                    val file = File(ctx.filesDir, "groot_chat_history.json")
                    if (file.exists()) {
                        val json = file.readText()
                        if (json.isNotBlank()) {
                            val type = object : TypeToken<List<ChatMessage>>() {}.type
                            val saved: List<ChatMessage>? = gson.fromJson(json, type)
                            if (!saved.isNullOrEmpty()) {
                                withContext(Dispatchers.Main) {
                                    _uiState.update { it.copy(messages = saved) }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun clearChat() {
        aiRepository.clearHistory()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                appContext?.let { ctx ->
                    val file = File(ctx.filesDir, "groot_chat_history.json")
                    if (file.exists()) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val defaultList = listOf(
            ChatMessage(
                isFromUser = false,
                text = "I am Groot, your AI productivity companion inside FocusFlow. I can help optimize your focus timer, track habits, analyze distractions, and guide your daily workflow."
            )
        )
        _uiState.update {
            it.copy(
                messages = defaultList,
                inputText = "",
                isAiThinking = false
            )
        }
    }

    fun sendMessage(query: String? = null) {
        val textToSend = (query ?: _uiState.value.inputText).trim()
        if (textToSend.isBlank() || _uiState.value.isAiThinking) return

        val userMessage = ChatMessage(isFromUser = true, text = textToSend)
        val updatedWithUser = _uiState.value.messages + userMessage

        if (com.simats.focusflow.utils.GrootGuardrailHelper.isOffTopicQuery(textToSend)) {
            val declineMessage = ChatMessage(
                isFromUser = false,
                text = com.simats.focusflow.utils.GrootGuardrailHelper.DECLINE_MESSAGE
            )
            val updatedWithDecline = updatedWithUser + declineMessage
            _uiState.update {
                it.copy(
                    messages = updatedWithDecline,
                    inputText = if (query == null) "" else it.inputText,
                    isAiThinking = false
                )
            }
            persistMessages(updatedWithDecline)
            return
        }

        val currentHistory = updatedWithUser.takeLast(10).map {
            ChatHistoryMessage(
                role = if (it.isFromUser) "user" else "assistant",
                content = it.text
            )
        }

        _uiState.update {
            it.copy(
                messages = updatedWithUser,
                inputText = if (query == null) "" else it.inputText,
                isAiThinking = true
            )
        }
        persistMessages(updatedWithUser)

        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionManager?.userSessionFlow?.first()
            val userName = session?.userName?.ifBlank { null } ?: "Sri Sai"
            val db = appContext?.let { com.simats.focusflow.data.local.FocusSessionDatabase.getInstance(it) }
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todaySessions = db?.getRecentSessions(currentUserId, 50)?.filter {
                it.startedAt.startsWith(todayStr)
            } ?: emptyList()
            val sessionCount = todaySessions.size

            val streakData = com.simats.focusflow.data.repository.StreakRepository.getInstance().getStreaks(currentUserId).getOrNull()
            val streak = streakData?.currentStreak ?: 1

            val userMetadataContext = "Active User: $userName, Current Streak: $streak days, Completed Sessions Today: $sessionCount"
            val promptWithContext = "[$userMetadataContext]\n$textToSend"

            val result = aiRepository.getAiInsights(
                userId = currentUserId,
                queryType = "chat",
                prompt = promptWithContext,
                history = currentHistory
            )

            result.fold(
                onSuccess = { response ->
                    if (response.status == "offline" || response.status == "fallback") {
                        val offlineResult = GrootActionParser.parseActions(textToSend)
                        if (offlineResult != null && offlineResult.actions.isNotEmpty()) {
                            var anyExec = false
                            val transcript = (_uiState.value.messages + userMessage).joinToString("\n") {
                                (if (it.isFromUser) "User: " else "Groot: ") + it.text
                            }
                            for (act in offlineResult.actions) {
                                val prepAct = if (act.type.equals("EXPORT_CHAT", ignoreCase = true) && !act.payload.containsKey("transcript")) {
                                    act.copy(payload = act.payload + ("transcript" to transcript))
                                } else act
                                val ok = actionExecutor?.executeAction(
                                    action = prepAct,
                                    userId = currentUserId,
                                    onNavigate = { _navigationEvent.tryEmit(it) },
                                    onClearChat = { clearChat() }
                                ) ?: false
                                if (ok) anyExec = true
                            }
                            val offlineMsg = ChatMessage(
                                isFromUser = false,
                                text = offlineResult.confirmationMessage,
                                action = offlineResult.actions.firstOrNull(),
                                actions = offlineResult.actions,
                                actionExecuted = anyExec
                            )
                            withContext(Dispatchers.Main) {
                                val updated = _uiState.value.messages + offlineMsg
                                _uiState.update {
                                    it.copy(messages = updated, isAiThinking = false)
                                }
                                persistMessages(updated)
                            }
                            return@fold
                        }
                    }

                    val rawAiText = response.resolvedText.ifBlank {
                        response.reply ?: response.response ?: response.text ?: response.insight ?: response.message ?: "I am Groot. Focus on your top priority task."
                    }
                    val unescapedText = HtmlCompat.fromHtml(rawAiText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
                    val aiText = if (unescapedText.trimStart().startsWith("{") && unescapedText.contains("\"insight\"")) {
                        response.message ?: "I am Groot, ready to assist."
                    } else {
                        unescapedText
                    }
                    val safeAiText = if (aiText.isNotBlank()) aiText else "I am Groot, ready to assist. How can I guide your focus or workflow today?"

                    val actionsToProcess = when {
                        response.actions.isNotEmpty() -> response.actions
                        response.action != null -> listOf(response.action)
                        else -> GrootActionParser.parseActions(textToSend)?.actions ?: emptyList()
                    }

                    var actionExecuted = false
                    val transcript = (_uiState.value.messages + userMessage).joinToString("\n") {
                        (if (it.isFromUser) "User: " else "Groot: ") + it.text
                    }
                    for (act in actionsToProcess) {
                        val prepAct = if (act.type.equals("EXPORT_CHAT", ignoreCase = true) && !act.payload.containsKey("transcript")) {
                            act.copy(payload = act.payload + ("transcript" to transcript))
                        } else act
                        val ok = actionExecutor?.executeAction(
                            action = prepAct,
                            userId = currentUserId,
                            onNavigate = { _navigationEvent.tryEmit(it) },
                            onClearChat = { clearChat() }
                        ) ?: false
                        if (ok) actionExecuted = true
                    }

                    val aiMessage = ChatMessage(
                        isFromUser = false,
                        text = safeAiText,
                        action = actionsToProcess.firstOrNull(),
                        actions = actionsToProcess,
                        actionExecuted = actionExecuted
                    )

                    withContext(Dispatchers.Main) {
                        val updatedWithAi = _uiState.value.messages + aiMessage
                        _uiState.update {
                            it.copy(
                                messages = updatedWithAi,
                                isAiThinking = false
                            )
                        }
                        persistMessages(updatedWithAi)
                    }
                },
                onFailure = {
                    val offlineResult = GrootActionParser.parseActions(textToSend)
                    if (offlineResult != null && offlineResult.actions.isNotEmpty()) {
                        var anyExec = false
                        val transcript = (_uiState.value.messages + userMessage).joinToString("\n") {
                            (if (it.isFromUser) "User: " else "Groot: ") + it.text
                        }
                        for (act in offlineResult.actions) {
                            val prepAct = if (act.type.equals("EXPORT_CHAT", ignoreCase = true) && !act.payload.containsKey("transcript")) {
                                act.copy(payload = act.payload + ("transcript" to transcript))
                            } else act
                            val ok = actionExecutor?.executeAction(
                                action = prepAct,
                                userId = currentUserId,
                                onNavigate = { _navigationEvent.tryEmit(it) },
                                onClearChat = { clearChat() }
                            ) ?: false
                            if (ok) anyExec = true
                        }
                        val offlineMsg = ChatMessage(
                            isFromUser = false,
                            text = offlineResult.confirmationMessage,
                            action = offlineResult.actions.firstOrNull(),
                            actions = offlineResult.actions,
                            actionExecuted = anyExec
                        )
                        withContext(Dispatchers.Main) {
                            val updatedWithFallback = _uiState.value.messages + offlineMsg
                            _uiState.update {
                                it.copy(
                                    messages = updatedWithFallback,
                                    isAiThinking = false
                                )
                            }
                            persistMessages(updatedWithFallback)
                        }
                    } else {
                        val fallbackText = "Unable to connect to Groot AI. Please check your network connection and verify the AI service is active."
                        val fallbackMessage = ChatMessage(
                            isFromUser = false,
                            text = fallbackText
                        )
                        withContext(Dispatchers.Main) {
                            val updatedWithFallback = _uiState.value.messages + fallbackMessage
                            _uiState.update {
                                it.copy(
                                    messages = updatedWithFallback,
                                    isAiThinking = false
                                )
                            }
                            persistMessages(updatedWithFallback)
                        }
                    }
                }
            )
        }
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
                        com.simats.focusflow.data.session.BlockedAppsManager.getInstance(context).blockAppsByNames(apps, block = false)
                    }
                }
                "UNBLOCK_APPS" -> {
                    val apps = (action.payload["apps"] as? List<*>)?.mapNotNull { it?.toString() }
                        ?: listOfNotNull(action.payload["title"]?.toString(), action.resolvedTitle.takeIf { it.isNotBlank() })
                    withContext(Dispatchers.Main) {
                        com.simats.focusflow.data.session.BlockedAppsManager.getInstance(context).blockAppsByNames(apps, block = true)
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
            withContext(Dispatchers.Main) {
                val updated = _uiState.value.messages + undoMsg
                _uiState.update { it.copy(messages = updated) }
                persistMessages(updated)
            }
        }
    }
}
