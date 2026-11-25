package com.simats.focusflow.data.repository

import android.content.Context
import androidx.core.text.HtmlCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.ChatHistoryMessage
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.viewmodel.ChatMessage
import com.simats.focusflow.ui.viewmodel.GrootAiUiState
import com.simats.focusflow.utils.GrootActionExecutor
import com.simats.focusflow.utils.GrootGuardrailHelper
import com.simats.focusflow.utils.LocalGrootBrain
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

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
            val brainResult = LocalGrootBrain.process(
                query = textToSend,
                context = appContext,
                userId = userId
            )

            var actionExecuted = false
            val transcript = (_uiState.value.messages + userMessage).joinToString("\n") {
                (if (it.isFromUser) "User: " else "Groot: ") + it.text
            }
            for (act in brainResult.actions) {
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

            val safeAiText = if (brainResult.reply.isNotBlank()) {
                HtmlCompat.fromHtml(brainResult.reply, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
            } else {
                "Hey! Ready to lock in—want to start a focus timer or check today's tasks?"
            }

            val aiMessage = ChatMessage(
                isFromUser = false,
                text = safeAiText,
                action = brainResult.actions.firstOrNull(),
                actions = brainResult.actions,
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
