package com.simats.focusflow.utils

import com.simats.focusflow.data.models.AiAction

data class ParsedActionResult(
    val actions: List<AiAction>,
    val confirmationMessage: String
) {
    val aiAction: AiAction?
        get() = actions.firstOrNull()
}

object GrootActionParser {

    fun parseActions(prompt: String): ParsedActionResult? {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return null

        val subClauses = trimmed.split(Regex("(?i)\\s+(?:and|then)\\s+|;|\\n")).map { it.trim() }.filter { it.isNotBlank() }
        val clausesToParse = if (subClauses.isNotEmpty()) subClauses else listOf(trimmed)

        val actions = mutableListOf<AiAction>()
        val replies = mutableListOf<String>()

        for (clause in clausesToParse) {
            val cLower = clause.lowercase()

            val timerMatch = Regex("(?i)\\b(?:start|set|run|begin|launch|activate)\\s+(?:a\\s+)?(?:(\\d+)\\s*(?:min|mins|minute|minutes|m)?)?\\s*(focus|timer|session|break|pomodoro)?").find(clause)
            val timerMatch2 = Regex("(?i)(\\d+)\\s*(?:min|mins|minute|minutes|m)\\s*(focus|timer|session|break|pomodoro)").find(clause)
            if ((timerMatch != null && (timerMatch.groupValues[1].isNotBlank() || timerMatch.groupValues[2].isNotBlank())) || timerMatch2 != null) {
                var mins = 25
                if (timerMatch != null && timerMatch.groupValues[1].isNotBlank()) {
                    mins = timerMatch.groupValues[1].toIntOrNull() ?: 25
                } else if (timerMatch2 != null) {
                    mins = timerMatch2.groupValues[1].toIntOrNull() ?: 25
                }
                var mode = "FOCUS"
                if (cLower.contains("break")) {
                    mode = if (mins >= 15) "LONG_BREAK" else "SHORT_BREAK"
                    if (mins == 25) mins = 5
                }
                mins = mins.coerceIn(1, 180)
                actions.add(
                    AiAction(
                        type = "START_FOCUS_TIMER",
                        duration = mins,
                        mode = mode,
                        title = "Focus Timer",
                        payload = mapOf("duration" to mins, "mode" to mode)
                    )
                )
                val label = if (mode == "FOCUS") "focus session" else "break"
                replies.add("Started a $mins-minute $label for you.")
                continue
            }

            if (Regex("(?i)\\b(?:pause|hold)\\s+(?:the\\s+)?(?:timer|focus|session)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "PAUSE_TIMER", title = "Pause Timer"))
                replies.add("Paused the active focus timer.")
                continue
            }

            if (Regex("(?i)\\b(?:resume|continue|unpause)\\s+(?:the\\s+)?(?:timer|focus|session)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "RESUME_TIMER", title = "Resume Timer"))
                replies.add("Resumed your focus timer.")
                continue
            }

            if (Regex("(?i)\\b(?:stop|end|cancel|reset)\\s+(?:the\\s+)?(?:timer|focus|session)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "STOP_TIMER", title = "Stop Timer"))
                replies.add("Stopped the timer.")
                continue
            }

            if (Regex("(?i)\\b(?:enable|turn on|activate|start)\\s+strict(?:\\s+focus)?(?:\\s+mode)?\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "ENABLE_STRICT_MODE", title = "Strict Focus Mode"))
                replies.add("Enabled Strict Focus Mode. Distracting apps are now locked.")
                continue
            }

            if (Regex("(?i)\\b(?:disable|turn off|deactivate|stop)\\s+strict(?:\\s+focus)?(?:\\s+mode)?\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "DISABLE_STRICT_MODE", title = "Strict Focus Mode"))
                replies.add("Disabled Strict Focus Mode.")
                continue
            }

            if (Regex("(?i)\\b(?:clear|delete|remove)\\s+(?:all\\s+)?completed\\s+tasks?\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "CLEAR_COMPLETED_TASKS", title = "Clear Completed Tasks"))
                replies.add("Cleared all completed tasks.")
                continue
            }

            val compMatch = Regex("(?i)\\b(?:complete|finish|done|mark\\s+complete|check\\s+off)\\s+(?:task\\s+)?[\"']?([^\"'\\n]+)[\"']?$").find(clause)
            if (compMatch != null && !cLower.contains("habit") && !cLower.contains("timer")) {
                val tTitle = compMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "COMPLETE_TASK", title = tTitle, payload = mapOf("title" to tTitle)))
                replies.add("Marked task '$tTitle' as completed.")
                continue
            }

            val uncompMatch = Regex("(?i)\\b(?:uncomplete|reopen|undo)\\s+(?:task\\s+)?[\"']?([^\"'\\n]+)[\"']?$").find(clause)
            if (uncompMatch != null && !cLower.contains("habit") && !cLower.contains("timer")) {
                val tTitle = uncompMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "UNCOMPLETE_TASK", title = tTitle, payload = mapOf("title" to tTitle)))
                replies.add("Reopened task '$tTitle'.")
                continue
            }

            val delTaskMatch = Regex("(?i)\\b(?:delete|remove)\\s+task\\s+[\"']?([^\"'\\n]+)[\"']?$").find(clause)
            if (delTaskMatch != null) {
                val tTitle = delTaskMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "DELETE_TASK", title = tTitle, payload = mapOf("title" to tTitle)))
                replies.add("Deleted task '$tTitle'.")
                continue
            }

            val taskMatch = Regex("(?i)\\b(?:create|add|new|schedule)\\s+(?:a\\s+)?task\\s+(?:named|to|called)?\\s*[\"']?([^\"'\\n]+)[\"']?").find(clause)
            if (taskMatch != null) {
                var rawTitle = taskMatch.groupValues[1].trim().trimEnd('.', '!')
                var priority = "medium"
                if (cLower.contains("high priority") || cLower.contains("urgent") || cLower.contains("critical")) {
                    priority = "high"
                    rawTitle = rawTitle.replace(Regex("(?i)\\s+with\\s+high\\s+priority"), "").trim()
                } else if (cLower.contains("low priority")) {
                    priority = "low"
                    rawTitle = rawTitle.replace(Regex("(?i)\\s+with\\s+low\\s+priority"), "").trim()
                }
                actions.add(
                    AiAction(
                        type = "CREATE_TASK",
                        title = rawTitle,
                        priority = priority,
                        category = "General",
                        payload = mapOf("title" to rawTitle, "priority" to priority, "category" to "General")
                    )
                )
                replies.add("Created task '$rawTitle' with $priority priority.")
                continue
            }

            if (Regex("(?i)\\b(?:list|show|view|get|what are)\\s+(?:my\\s+)?(?:pending|overdue|completed|all)?\\s*tasks\\b").containsMatchIn(clause)) {
                var flt = "all"
                if (cLower.contains("pending")) flt = "pending"
                else if (cLower.contains("completed")) flt = "completed"
                else if (cLower.contains("overdue")) flt = "overdue"
                else if (cLower.contains("high")) flt = "high_priority"
                actions.add(AiAction(type = "LIST_TASKS", title = "Tasks Overview", payload = mapOf("filter" to flt)))
                replies.add("Here is your $flt tasks overview.")
                continue
            }

            val habitMatch = Regex("(?i)\\b(?:create|add|new|track|build)\\s+(?:a\\s+)?habit\\s+(?:named|to|called)?\\s*[\"']?([^\"'\\n]+)[\"']?").find(clause)
            if (habitMatch != null) {
                val hTitle = habitMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(
                    AiAction(
                        type = "CREATE_HABIT",
                        title = hTitle,
                        category = "Daily",
                        payload = mapOf("title" to hTitle, "frequency" to "daily", "icon" to "bolt")
                    )
                )
                replies.add("Created the daily habit '$hTitle'.")
                continue
            }

            val chkHabitMatch = Regex("(?i)\\b(?:check\\s+off|log|complete|done)\\s+(?:habit\\s+)?[\"']?([^\"'\\n]+)[\"']?").find(clause)
            if (chkHabitMatch != null && !cLower.contains("task") && !cLower.contains("timer")) {
                val hTitle = chkHabitMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "CHECK_OFF_HABIT", title = hTitle, payload = mapOf("title" to hTitle)))
                replies.add("Checked off habit '$hTitle' for today.")
                continue
            }

            val delHabitMatch = Regex("(?i)\\b(?:delete|remove)\\s+habit\\s+[\"']?([^\"'\\n]+)[\"']?").find(clause)
            if (delHabitMatch != null) {
                val hTitle = delHabitMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "DELETE_HABIT", title = hTitle, payload = mapOf("title" to hTitle)))
                replies.add("Deleted habit '$hTitle'.")
                continue
            }

            if (Regex("(?i)\\b(?:habit\\s+status|check\\s+habits|my\\s+habits\\s+today|how\\s+are\\s+my\\s+habits)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "HABIT_STATUS", title = "Habit Status"))
                replies.add("Here is your habit completion status for today.")
                continue
            }

            if (Regex("(?i)\\b(?:focus\\s+summary|my\\s+stats|my\\s+analytics|how\\s+much\\s+did\\s+i\\s+focus|today'?s?\\s+stats|focus\\s+minutes)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "GET_FOCUS_SUMMARY", title = "Focus Summary"))
                replies.add("Here is your focus and productivity breakdown.")
                continue
            }

            val navMatch = Regex("(?i)\\b(?:open|show|go to|take me to|navigate to|view)\\s+(?:my\\s+)?(timer|focus timer|insights|analytics|habits|tasks|task manager|profile|settings|dashboard|home)\\b").find(clause)
            if (navMatch != null) {
                val dest = navMatch.groupValues[1].lowercase()
                val tab = when {
                    dest.contains("timer") || dest.contains("focus") -> "focus_timer"
                    dest.contains("habit") -> "habits"
                    dest.contains("insight") || dest.contains("analytic") -> "analytics"
                    dest.contains("profile") || dest.contains("setting") -> "profile"
                    dest.contains("home") || dest.contains("dash") -> "home"
                    else -> "tasks"
                }
                actions.add(AiAction(type = "NAVIGATE_TAB", route = tab, screenName = tab, payload = mapOf("tab" to tab, "route" to tab)))
                replies.add("Navigating to $tab.")
                continue
            }

            if (Regex("(?i)\\b(?:export|share|download|save)\\s+(?:this\\s+)?(?:chat|transcript|conversation)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "EXPORT_CHAT", title = "Export Chat"))
                replies.add("Exporting chat transcript.")
                continue
            }

            if (Regex("(?i)\\b(?:clear|delete|reset)\\s+(?:this\\s+)?(?:chat|history|conversation)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "CLEAR_CHAT", title = "Clear Chat"))
                replies.add("Cleared chat history.")
                continue
            }
        }

        if (actions.isEmpty()) return null
        return ParsedActionResult(
            actions = actions,
            confirmationMessage = replies.joinToString(" ")
        )
    }

    fun parseAction(prompt: String): ParsedActionResult? {
        return parseActions(prompt)
    }

    fun canHandleAction(prompt: String): Boolean {
        return parseActions(prompt) != null
    }
}
