package com.simats.focusflow.utils

import android.content.Context
import android.content.Intent
import androidx.core.text.HtmlCompat
import com.simats.focusflow.data.models.AiAction
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.repository.ProfileRepository
import com.simats.focusflow.service.FocusBlockerAccessibilityService
import com.simats.focusflow.service.FocusTimerService
import com.simats.focusflow.ui.viewmodel.TimerMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GrootActionExecutor(
    private val context: Context,
    private val productivityRepository: ProductivityRepository = ProductivityRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) {
    suspend fun executeAction(
        action: AiAction,
        userId: Int,
        onNavigate: (String) -> Unit = {},
        onClearChat: () -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val type = action.type.uppercase()
            when (type) {
                "START_FOCUS_TIMER", "START_TIMER" -> {
                    val duration = action.resolvedDuration.coerceIn(1, 180)
                    val rawMode = action.resolvedMode.uppercase()
                    val mode = when {
                        rawMode.contains("SHORT") || (rawMode.contains("BREAK") && duration <= 10) -> TimerMode.SHORT_BREAK
                        rawMode.contains("LONG") || (rawMode.contains("BREAK") && duration > 10) -> TimerMode.LONG_BREAK
                        else -> TimerMode.FOCUS
                    }
                    withContext(Dispatchers.Main) {
                        FocusTimerService.setDurations(context, duration, 5, 15)
                        if (mode != TimerMode.FOCUS) {
                            FocusTimerService.selectMode(context, mode)
                        }
                        FocusTimerService.startTimer(context, userId)
                    }
                    true
                }
                "PAUSE_TIMER" -> {
                    withContext(Dispatchers.Main) {
                        FocusTimerService.pauseTimer(context)
                    }
                    true
                }
                "RESUME_TIMER" -> {
                    withContext(Dispatchers.Main) {
                        FocusTimerService.resumeTimer(context)
                    }
                    true
                }
                "STOP_TIMER", "RESET_TIMER" -> {
                    withContext(Dispatchers.Main) {
                        FocusTimerService.resetTimer(context)
                    }
                    true
                }
                "ENABLE_STRICT_MODE" -> {
                    withContext(Dispatchers.Main) {
                        FocusTimerService.setStrictFocus(context, true)
                        if (!FocusBlockerAccessibilityService.isAccessibilityServiceEnabled(context)) {
                            FocusBlockerAccessibilityService.openAccessibilitySettings(context)
                        }
                    }
                    true
                }
                "DISABLE_STRICT_MODE" -> {
                    withContext(Dispatchers.Main) {
                        FocusTimerService.setStrictFocus(context, false)
                    }
                    true
                }
                "BLOCK_APPS" -> {
                    val apps = (action.payload["apps"] as? List<*>)?.mapNotNull { it?.toString() }
                        ?: listOfNotNull(action.payload["title"]?.toString(), action.resolvedTitle.takeIf { it.isNotBlank() })
                    withContext(Dispatchers.Main) {
                        val manager = com.simats.focusflow.data.session.BlockedAppsManager.getInstance(context)
                        manager.blockAppsByNames(apps, block = true)
                    }
                    true
                }
                "UNBLOCK_APPS" -> {
                    val apps = (action.payload["apps"] as? List<*>)?.mapNotNull { it?.toString() }
                        ?: listOfNotNull(action.payload["title"]?.toString(), action.resolvedTitle.takeIf { it.isNotBlank() })
                    withContext(Dispatchers.Main) {
                        val manager = com.simats.focusflow.data.session.BlockedAppsManager.getInstance(context)
                        manager.blockAppsByNames(apps, block = false)
                    }
                    true
                }
                "CREATE_TASK" -> {
                    val rawTitle = action.resolvedTitle
                    val title = HtmlCompat.fromHtml(rawTitle, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
                    if (title.isNotBlank()) {
                        val priority = action.resolvedPriority.ifBlank { "medium" }.lowercase()
                        val category = action.category.ifBlank { "General" }
                        val dueDate = action.payload["due_date"]?.toString() ?: "Today"
                        val deadline = action.resolvedDeadlineTimestamp
                        productivityRepository.createTask(
                            userId = userId,
                            title = title,
                            description = "Created automatically by Groot AI",
                            priority = priority,
                            dueDate = dueDate,
                            category = category,
                            deadlineTimestamp = deadline
                        )
                        true
                    } else false
                }
                "COMPLETE_TASK" -> {
                    val id = action.resolvedId
                    val targetTitle = action.resolvedTitle
                    if (id > 0) {
                        productivityRepository.toggleTask(userId, id, isCompleted = true)
                        true
                    } else if (targetTitle.isNotBlank()) {
                        val tasks = productivityRepository.getTasks(userId).getOrNull() ?: emptyList()
                        val match = tasks.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                            ?: tasks.firstOrNull { targetTitle.contains(it.title, ignoreCase = true) }
                        if (match != null) {
                            productivityRepository.toggleTask(userId, match.id, isCompleted = true)
                            true
                        } else false
                    } else false
                }
                "UNCOMPLETE_TASK" -> {
                    val id = action.resolvedId
                    val targetTitle = action.resolvedTitle
                    if (id > 0) {
                        productivityRepository.toggleTask(userId, id, isCompleted = false)
                        true
                    } else if (targetTitle.isNotBlank()) {
                        val tasks = productivityRepository.getTasks(userId).getOrNull() ?: emptyList()
                        val match = tasks.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                        if (match != null) {
                            productivityRepository.toggleTask(userId, match.id, isCompleted = false)
                            true
                        } else false
                    } else false
                }
                "UPDATE_TASK" -> {
                    val id = action.resolvedId
                    val targetTitle = action.resolvedTitle
                    val newTitleRaw = action.payload["new_title"]?.toString() ?: targetTitle
                    val newTitle = HtmlCompat.fromHtml(newTitleRaw, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
                    val newPriority = action.resolvedPriority
                    val newDeadline = action.resolvedDeadlineTimestamp
                    val tasks = productivityRepository.getTasks(userId).getOrNull() ?: emptyList()
                    val match = if (id > 0) tasks.firstOrNull { it.id == id } else tasks.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                    if (match != null) {
                        productivityRepository.updateTask(
                            userId = userId,
                            taskId = match.id,
                            title = if (newTitle.isNotBlank()) newTitle else match.title,
                            description = match.description,
                            priority = newPriority.ifBlank { match.priority },
                            dueDate = match.dueDate,
                            category = match.category,
                            scheduledTime = match.scheduledTime,
                            deadlineTimestamp = newDeadline ?: match.deadlineTimestamp
                        )
                        true
                    } else false
                }
                "DELETE_TASK" -> {
                    val id = action.resolvedId
                    val targetTitle = action.resolvedTitle
                    if (id > 0) {
                        productivityRepository.deleteTask(userId, id)
                        true
                    } else if (targetTitle.isNotBlank()) {
                        val tasks = productivityRepository.getTasks(userId).getOrNull() ?: emptyList()
                        val match = tasks.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                        if (match != null) {
                            productivityRepository.deleteTask(userId, match.id)
                            true
                        } else false
                    } else false
                }
                "CLEAR_COMPLETED_TASKS" -> {
                    val tasks = productivityRepository.getTasks(userId).getOrNull() ?: emptyList()
                    tasks.filter { it.isCompleted }.forEach {
                        productivityRepository.deleteTask(userId, it.id)
                    }
                    true
                }
                "LIST_TASKS" -> true
                "CREATE_HABIT" -> {
                    val rawTitle = action.resolvedTitle
                    val title = HtmlCompat.fromHtml(rawTitle, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
                    if (title.isNotBlank()) {
                        val freq = action.payload["frequency"]?.toString() ?: "daily"
                        productivityRepository.createHabit(
                            userId = userId,
                            title = title,
                            icon = "bolt",
                            frequency = freq
                        )
                        true
                    } else false
                }
                "CHECK_OFF_HABIT", "LOG_HABIT" -> {
                    val id = action.resolvedId
                    val targetTitle = action.resolvedTitle
                    val habits = productivityRepository.getHabits(userId).getOrNull()?.habits ?: emptyList()
                    val match = if (id > 0) habits.firstOrNull { it.id == id } else habits.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                    if (match != null) {
                        if (!match.isCompletedToday) {
                            productivityRepository.toggleHabit(userId, match.id)
                        }
                        true
                    } else false
                }
                "UNCHECK_HABIT" -> {
                    val id = action.resolvedId
                    val targetTitle = action.resolvedTitle
                    val habits = productivityRepository.getHabits(userId).getOrNull()?.habits ?: emptyList()
                    val match = if (id > 0) habits.firstOrNull { it.id == id } else habits.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                    if (match != null) {
                        if (match.isCompletedToday) {
                            productivityRepository.toggleHabit(userId, match.id)
                        }
                        true
                    } else false
                }
                "DELETE_HABIT" -> {
                    val id = action.resolvedId
                    val targetTitle = action.resolvedTitle
                    val habits = productivityRepository.getHabits(userId).getOrNull()?.habits ?: emptyList()
                    val match = if (id > 0) habits.firstOrNull { it.id == id } else habits.firstOrNull { it.title.contains(targetTitle, ignoreCase = true) }
                    if (match != null) {
                        productivityRepository.deleteHabit(userId, match.id)
                        true
                    } else false
                }
                "HABIT_STATUS" -> true
                "SET_GOAL" -> {
                    if (action.dailyMinutes > 0) {
                        profileRepository.updatePreferences(
                            userId = userId,
                            dailyFocusGoalMinutes = action.dailyMinutes,
                            preferredFocusDuration = 45
                        )
                        true
                    } else false
                }
                "NAVIGATE_TAB", "NAVIGATE" -> {
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
                    withContext(Dispatchers.Main) {
                        onNavigate(route)
                    }
                    true
                }
                "EXPORT_CHAT" -> {
                    withContext(Dispatchers.Main) {
                        val chatText = action.payload["transcript"]?.toString() ?: ""
                        val sendIntent = Intent().apply {
                            this.action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, chatText)
                            setType("text/plain")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Export Groot AI Chat").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(shareIntent)
                    }
                    true
                }
                "CLEAR_CHAT" -> {
                    withContext(Dispatchers.Main) {
                        onClearChat()
                    }
                    true
                }
                "GET_FOCUS_SUMMARY" -> true
                else -> false
            }
        } catch (_: Exception) {
            false
        }
    }
}
