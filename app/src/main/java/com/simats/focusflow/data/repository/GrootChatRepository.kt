package com.simats.focusflow.data.repository

import android.content.Context
import androidx.core.text.HtmlCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simats.focusflow.data.api.RetrofitClient
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.ChatHistoryMessage
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.viewmodel.ChatMessage
import com.simats.focusflow.ui.viewmodel.GrootAiUiState
import com.simats.focusflow.utils.GrootActionExecutor
import com.simats.focusflow.utils.GrootActionParser
import com.simats.focusflow.utils.GrootGuardrailHelper
import com.simats.focusflow.utils.UsageStatsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GrootChatRepository private constructor(
    private val appContext: Context,
    private val aiRepository: AiRepository = AiRepository(),
    private val productivityRepository: ProductivityRepository = ProductivityRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    private val _uiState = MutableStateFlow(GrootAiUiState())
    val uiState: StateFlow<GrootAiUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    private var actionExecutor: GrootActionExecutor? = null
    private var activeChatJob: Job? = null

    init {
        actionExecutor = GrootActionExecutor(appContext, productivityRepository, profileRepository)
        loadPersistedMessages()
        prewarmConnection()
    }

    fun prewarmConnection() {
        scope.launch {
            try {
                val pingUrl = RetrofitClient.GROOT_AI_BASE_URL
                val request = Request.Builder()
                    .url(pingUrl)
                    .head()
                    .build()
                RetrofitClient.okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) {
                        response.close()
                    }
                })
            } catch (_: Exception) {}
        }
    }

    private fun loadPersistedMessages() {
        scope.launch {
            try {
                val file = File(appContext.filesDir, "groot_chat_history.json")
                if (file.exists()) {
                    val json = file.readText()
                    if (json.isNotBlank()) {
                        val type = object : TypeToken<List<ChatMessage>>() {}.type
                        val saved: List<ChatMessage>? = gson.fromJson(json, type)
                        if (!saved.isNullOrEmpty()) {
                            _uiState.update { it.copy(messages = saved) }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun persistMessages(messages: List<ChatMessage>) {
        scope.launch {
            try {
                val file = File(appContext.filesDir, "groot_chat_history.json")
                val json = gson.toJson(messages)
                file.writeText(json)
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
        scope.launch {
            try {
                val file = File(appContext.filesDir, "groot_chat_history.json")
                if (file.exists()) {
                    file.delete()
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
                isAiThinking = false,
                isGenerating = false
            )
        }
    }

    fun stopGeneration() {
        activeChatJob?.cancel()
        activeChatJob = null
        val stoppedMsg = ChatMessage(
            isFromUser = false,
            text = "Response stopped."
        )
        val updated = _uiState.value.messages + stoppedMsg
        _uiState.update {
            it.copy(
                isAiThinking = false,
                isGenerating = false,
                messages = updated
            )
        }
        persistMessages(updated)
    }

    fun sendMessage(query: String? = null, userId: Int, sessionManager: SessionManager?) {
        val textToSend = (query ?: _uiState.value.inputText).trim()
        if (textToSend.isBlank() || _uiState.value.isAiThinking) return

        val userMessage = ChatMessage(isFromUser = true, text = textToSend)
        val updatedWithUser = _uiState.value.messages + userMessage

        if (GrootGuardrailHelper.isOffTopicQuery(textToSend)) {
            val declineMessage = ChatMessage(
                isFromUser = false,
                text = GrootGuardrailHelper.DECLINE_MESSAGE
            )
            val updatedWithDecline = updatedWithUser + declineMessage
            _uiState.update {
                it.copy(
                    messages = updatedWithDecline,
                    inputText = if (query == null) "" else it.inputText,
                    isAiThinking = false,
                    isGenerating = false
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
                isAiThinking = true,
                isGenerating = true
            )
        }
        persistMessages(updatedWithUser)

        activeChatJob?.cancel()
        activeChatJob = scope.launch {
            val session = sessionManager?.userSessionFlow?.first()
            val userName = session?.userName?.ifBlank { null } ?: "Sri Sai"
            val db = FocusSessionDatabase.getInstance(appContext)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todaySessions = db.getRecentSessions(userId, 50).filter {
                it.startedAt.startsWith(todayStr)
            }
            val sessionCount = todaySessions.size
            val todayFocusMins = db.getTodayFocusMinutes(userId)

            val streakData = StreakRepository.getInstance().getStreaks(userId).getOrNull()
            val streak = streakData?.currentStreak ?: 0

            val localTasks = db.getLocalTasks(userId)
            val pendingTasks = localTasks.filter { !it.isCompleted }
            val doneTasksCount = localTasks.count { it.isCompleted }
            val topPendingTitles = pendingTasks.take(3).joinToString { it.title }

            val habitsRes = try {
                productivityRepository.getHabits(userId).getOrNull()
            } catch (_: Exception) { null }
            val habitsList = habitsRes?.habits ?: emptyList()
            val completedHabitsCount = habitsList.count { it.isCompletedToday }
            val topHabitTitle = habitsList.maxByOrNull { it.streakCount }?.title ?: "None"

            val distractionSummary = UsageStatsHelper.queryTodayDistractionStats(appContext)
            val distractionMins = distractionSummary.totalDistractionMinutes
            val topDistractApp = distractionSummary.apps.firstOrNull { it.usageMinutes > 0 }
            val topDistractStr = if (topDistractApp != null) "${topDistractApp.appName} (${topDistractApp.usageMinutes}m)" else "Minimal"

            val appContextSummary = "Active User: $userName, Streak: $streak days, Focus Today: ${todayFocusMins}m across $sessionCount sessions, Tasks: ${pendingTasks.size} pending (${if (topPendingTitles.isNotBlank()) topPendingTitles else "None"}), $doneTasksCount done, Habits: ${habitsList.size} active ($completedHabitsCount completed today, top: $topHabitTitle), Distractions: ${distractionMins}m ($topDistractStr)"
            val promptWithContext = "[$appContextSummary]\n$textToSend"

            val result = aiRepository.getAiInsights(
                userId = userId,
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
                                    userId = userId,
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
                            val updated = _uiState.value.messages + offlineMsg
                            _uiState.update {
                                it.copy(messages = updated, isAiThinking = false, isGenerating = false)
                            }
                            persistMessages(updated)
                            return@fold
                        } else {
                            val offlineMsg = ChatMessage(
                                isFromUser = false,
                                text = "I am Groot. I'm connected and ready to help. You can tell me to start a focus session, add tasks, track habits, or check your streak stats anytime!",
                                actionExecuted = false
                            )
                            val updated = _uiState.value.messages + offlineMsg
                            _uiState.update {
                                it.copy(messages = updated, isAiThinking = false, isGenerating = false)
                            }
                            persistMessages(updated)
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
                        response.actions.isNotEmpty() -> response.actions.filter { !it.type.equals("NONE", ignoreCase = true) }
                        response.action != null && response.action.type.isNotBlank() && !response.action.type.equals("NONE", ignoreCase = true) -> listOf(response.action)
                        else -> GrootActionParser.parseActions(textToSend)?.actions ?: emptyList()
                    }

                    var actionExecuted = false
                    val transcript = (_uiState.value.messages + userMessage).joinToString("\n") {
                        (if (it.isFromUser) "User: " else "Groot: ") + it.text
                    }
                    for (act in actionsToProcess) {
                        if (act.type.equals("NONE", ignoreCase = true)) continue
                        val prepAct = if (act.type.equals("EXPORT_CHAT", ignoreCase = true) && !act.payload.containsKey("transcript")) {
                            act.copy(payload = act.payload + ("transcript" to transcript))
                        } else act
                        val ok = actionExecutor?.executeAction(
                            action = prepAct,
                            userId = userId,
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

                    val updatedWithAi = _uiState.value.messages + aiMessage
                    _uiState.update {
                        it.copy(
                            messages = updatedWithAi,
                            isAiThinking = false,
                            isGenerating = false
                        )
                    }
                    persistMessages(updatedWithAi)
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
                                userId = userId,
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
                        val updatedWithFallback = _uiState.value.messages + offlineMsg
                        _uiState.update {
                            it.copy(
                                messages = updatedWithFallback,
                                isAiThinking = false,
                                isGenerating = false
                            )
                        }
                        persistMessages(updatedWithFallback)
                    } else {
                        val fallbackText = "I am Groot. I'm connected and ready to help. You can tell me to start a focus session, add tasks, track habits, or check your streak stats anytime!"
                        val fallbackMessage = ChatMessage(
                            isFromUser = false,
                            text = fallbackText
                        )
                        val updatedWithFallback = _uiState.value.messages + fallbackMessage
                        _uiState.update {
                            it.copy(
                                messages = updatedWithFallback,
                                isAiThinking = false,
                                isGenerating = false
                            )
                        }
                        persistMessages(updatedWithFallback)
                    }
                }
            )
        }
    }

    companion object {
        @Volatile
        private var instance: GrootChatRepository? = null

        fun init(context: Context): GrootChatRepository {
            return instance ?: synchronized(this) {
                instance ?: GrootChatRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }

        fun getInstance(context: Context? = null): GrootChatRepository {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val ctx = context?.applicationContext ?: ProductivityRepository.appContext
                        ?: throw IllegalStateException("GrootChatRepository not initialized")
                    GrootChatRepository(ctx).also { instance = it }
                }
            }
        }
    }
}
