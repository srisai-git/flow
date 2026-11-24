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

            if (Regex("(?i)\\b(?:lock(?:ed)?\\s+in|locking\\s+in)\\b").containsMatchIn(clause)) {
                val mins = 45
                actions.add(
                    AiAction(
                        type = "START_FOCUS_TIMER",
                        duration = mins,
                        mode = "FOCUS",
                        title = "Lock In Session",
                        payload = mapOf("duration" to mins, "mode" to "FOCUS")
                    )
                )
                replies.add("Locking in right now. Full focus mode activated - zero distractions. Let's get to work.")
                continue
            }

            if (Regex("(?i)\\b(?:i'?m\\s+cooked|feeling\\s+cooked|cooked)\\b").containsMatchIn(clause)) {
                val mins = 15
                actions.add(
                    AiAction(
                        type = "START_FOCUS_TIMER",
                        duration = mins,
                        mode = "LONG_BREAK",
                        title = "Recovery Break",
                        payload = mapOf("duration" to mins, "mode" to "LONG_BREAK")
                    )
                )
                replies.add("You are not cooked, just experiencing cognitive fatigue. Take a 15-minute recovery breather to reset your brain.")
                continue
            }

            if (Regex("(?i)\\b(?:fr|for\\s+real|no\\s+cap)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "CHAT", title = "Chat", payload = mapOf("reply" to "For real, no cap. Let's keep the streak compounding.")))
                replies.add("For real, no cap. Let's keep the streak compounding.")
                continue
            }

            if (Regex("(?i)\\b(?:bet|say\\s+less)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "CHAT", title = "Chat", payload = mapOf("reply" to "Bet. Say less - I got you covered.")))
                replies.add("Bet. Say less - I got you covered.")
                continue
            }

            if (Regex("(?i)\\b(?:bye\\s+byee?|bye\\b|goodbye|cya)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "CHAT", title = "Chat", payload = mapOf("reply" to "Bye byee! Great focus work today. Lock in again soon!")))
                replies.add("Bye byee! Great focus work today. Lock in again soon!")
                continue
            }

            if (!cLower.contains("task") && !cLower.contains("habit")) {
                val hourMatch = Regex("(?i)(\\d+)\\s*(?:hours?|hrs?|h)\\b").find(clause)
                val minMatch = Regex("(?i)(\\d+)\\s*(?:mins?|minutes?|m)\\b").find(clause)
                val timerKeywordMatch = Regex("(?i)\\b(?:start|set|run|begin|launch|activate)\\s+(?:a\\s+)?(?:(\\d+)\\s*)?(focus|timer|session|break|pomodoro)?\\b").find(clause)
                val hasTimerKeyword = cLower.contains("timer") || cLower.contains("focus") || cLower.contains("break") || cLower.contains("pomodoro") || cLower.contains("session")

                if (hourMatch != null || minMatch != null || (timerKeywordMatch != null && hasTimerKeyword)) {
                    var mins = 25
                    if (hourMatch != null) {
                        val h = hourMatch.groupValues[1].toIntOrNull() ?: 1
                        mins = h * 60
                    } else if (minMatch != null) {
                        mins = minMatch.groupValues[1].toIntOrNull() ?: 25
                    } else if (timerKeywordMatch != null && timerKeywordMatch.groupValues[1].isNotBlank()) {
                        mins = timerKeywordMatch.groupValues[1].toIntOrNull() ?: 25
                    }

                    var mode = "FOCUS"
                    if (cLower.contains("break")) {
                        mode = if (mins >= 15) "LONG_BREAK" else "SHORT_BREAK"
                        if (mins == 25 && minMatch == null && hourMatch == null) mins = 5
                    }
                    mins = mins.coerceIn(1, 720)
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

            val blockAppMatch = Regex("(?i)\\b(?:block|restrict|lock|add)\\s+(?:apps?\\s+)?(?:called\\s+)?([a-zA-Z0-9,\\s&]+?)(?:\\s+to\\s+blocked(?:\\s+apps?)?)?$").find(clause)
            if (blockAppMatch != null && (cLower.startsWith("block") || cLower.contains("to blocked"))) {
                val rawApps = blockAppMatch.groupValues[1]
                val appNames = rawApps.split(Regex("(?i)\\s+(?:and|&)\\s+|,\\s*")).map { it.trim() }.filter { it.isNotBlank() && !it.equals("apps", ignoreCase = true) }
                if (appNames.isNotEmpty()) {
                    actions.add(
                        AiAction(
                            type = "BLOCK_APPS",
                            title = "Block " + appNames.joinToString(", "),
                            payload = mapOf("apps" to appNames)
                        )
                    )
                    replies.add("Added ${appNames.joinToString(", ")} to blocked apps list.")
                    continue
                }
            }

            val unblockAppMatch = Regex("(?i)\\b(?:unblock|unlock|allow|remove)\\s+(?:apps?\\s+)?(?:called\\s+)?([a-zA-Z0-9,\\s&]+?)(?:\\s+from\\s+blocked(?:\\s+apps?)?)?$").find(clause)
            if (unblockAppMatch != null && (cLower.startsWith("unblock") || cLower.contains("from blocked"))) {
                val rawApps = unblockAppMatch.groupValues[1]
                val appNames = rawApps.split(Regex("(?i)\\s+(?:and|&)\\s+|,\\s*")).map { it.trim() }.filter { it.isNotBlank() && !it.equals("apps", ignoreCase = true) }
                if (appNames.isNotEmpty()) {
                    actions.add(
                        AiAction(
                            type = "UNBLOCK_APPS",
                            title = "Unblock " + appNames.joinToString(", "),
                            payload = mapOf("apps" to appNames)
                        )
                    )
                    replies.add("Removed ${appNames.joinToString(", ")} from blocked apps list.")
                    continue
                }
            }

            if (Regex("(?i)\\b(?:clear|delete|remove)\\s+(?:all\\s+)?completed\\s+tasks?\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "CLEAR_COMPLETED_TASKS", title = "Clear Completed Tasks"))
                replies.add("Cleared all completed tasks.")
                continue
            }

            val compMatch = Regex("(?i)\\b(?:complete|finish|done|mark\\s+complete|mark\\s+done|check\\s+off)\\s+(?:a\\s+|the\\s+)?task\\s+[\"']?([^\"'\\n]+)[\"']?$").find(clause)
            if (compMatch != null && !cLower.contains("habit") && !cLower.contains("timer")) {
                val tTitle = compMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "COMPLETE_TASK", title = tTitle, payload = mapOf("title" to tTitle)))
                replies.add("Marked task '$tTitle' as completed.")
                continue
            }

            val uncompMatch = Regex("(?i)\\b(?:uncomplete|reopen|undo)\\s+(?:a\\s+|the\\s+)?task\\s+[\"']?([^\"'\\n]+)[\"']?$").find(clause)
            if (uncompMatch != null && !cLower.contains("habit") && !cLower.contains("timer")) {
                val tTitle = uncompMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "UNCOMPLETE_TASK", title = tTitle, payload = mapOf("title" to tTitle)))
                replies.add("Reopened task '$tTitle'.")
                continue
            }

            val delTaskMatch = Regex("(?i)\\b(?:delete|remove)\\s+(?:a\\s+|the\\s+)?task\\s+[\"']?([^\"'\\n]+)[\"']?$").find(clause)
            if (delTaskMatch != null) {
                val tTitle = delTaskMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "DELETE_TASK", title = tTitle, payload = mapOf("title" to tTitle)))
                replies.add("Deleted task '$tTitle'.")
                continue
            }

            val taskMatch = Regex("(?i)\\b(?:set|create|add|new|schedule)\\s+(?:a\\s+)?task\\s+(?:named|to|called)?\\s*[\"']?([^\"'\\n]+)[\"']?").find(clause)
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
                var category = "General"
                val catMatch = Regex("(?i)\\s+under\\s+([a-zA-Z]+)").find(rawTitle)
                if (catMatch != null) {
                    category = catMatch.groupValues[1].trim()
                    rawTitle = rawTitle.replace(catMatch.value, "").trim()
                }
                var dueDate = "Today"
                if (cLower.contains("due tomorrow")) {
                    dueDate = "Tomorrow"
                    rawTitle = rawTitle.replace(Regex("(?i)\\s+due\\s+tomorrow"), "").trim()
                } else if (cLower.contains("due today")) {
                    dueDate = "Today"
                    rawTitle = rawTitle.replace(Regex("(?i)\\s+due\\s+today"), "").trim()
                }
                actions.add(
                    AiAction(
                        type = "CREATE_TASK",
                        title = rawTitle,
                        priority = priority,
                        category = category,
                        payload = mapOf("title" to rawTitle, "priority" to priority, "category" to category, "due_date" to dueDate)
                    )
                )
                replies.add("Created task '$rawTitle' with $priority priority due $dueDate.")
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

            val habitMatch = Regex("(?i)\\b(?:set|create|add|new|track|build)\\s+(?:a\\s+)?habit\\s+(?:named|to|called)?\\s*[\"']?([^\"'\\n]+)[\"']?").find(clause)
            if (habitMatch != null) {
                var hTitle = habitMatch.groupValues[1].trim().trimEnd('.', '!')
                var frequency = "daily"
                if (cLower.contains("weekdays")) {
                    frequency = "weekdays"
                    hTitle = hTitle.replace(Regex("(?i)\\s+on\\s+weekdays|\\s+weekdays"), "").trim()
                } else if (cLower.contains("weekends")) {
                    frequency = "Sun,Sat"
                    hTitle = hTitle.replace(Regex("(?i)\\s+on\\s+weekends|\\s+weekends"), "").trim()
                }
                actions.add(
                    AiAction(
                        type = "CREATE_HABIT",
                        title = hTitle,
                        category = "Daily",
                        payload = mapOf("title" to hTitle, "frequency" to frequency, "icon" to "bolt")
                    )
                )
                replies.add("Created the habit '$hTitle' ($frequency).")
                continue
            }

            val chkHabitMatch = Regex("(?i)\\b(?:check\\s+off|log|complete|done|mark\\s+complete|mark\\s+done)\\s+(?:a\\s+|the\\s+)?habit\\s+[\"']?([^\"'\\n]+)[\"']?").find(clause)
            if (chkHabitMatch != null && !cLower.contains("task") && !cLower.contains("timer")) {
                val hTitle = chkHabitMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "CHECK_OFF_HABIT", title = hTitle, payload = mapOf("title" to hTitle)))
                replies.add("Checked off habit '$hTitle' for today.")
                continue
            }

            val delHabitMatch = Regex("(?i)\\b(?:delete|remove)\\s+(?:a\\s+|the\\s+)?habit\\s+[\"']?([^\"'\\n]+)[\"']?").find(clause)
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

            val navMatch = Regex("(?i)\\b(?:open|show|go to|take me to|navigate to|view|switch to)?\\s*(?:my\\s+|the\\s+)?(timer|focus timer|focus screen|focus|insights|analytics|habits screen|habits|tasks screen|tasks|task manager|profile|settings|dashboard|home screen|home)\\b").find(clause)
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

            val nameMatch = Regex("(?i)\\b(?:change|update|set)\\s+(?:my\\s+)?name\\s+to\\s+[\"']?([^\"'\\n]+)[\"']?$").find(clause)
            if (nameMatch != null) {
                val newName = nameMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "UPDATE_PROFILE", title = newName, payload = mapOf("name" to newName)))
                replies.add("Updated your profile name to '$newName'.")
                continue
            }

            val bioMatch = Regex("(?i)\\b(?:change|update|set)\\s+(?:my\\s+)?bio\\s+to\\s+[\"']?([^\"'\\n]+)[\"']?$").find(clause)
            if (bioMatch != null) {
                val newBio = bioMatch.groupValues[1].trim().trimEnd('.', '!')
                actions.add(AiAction(type = "UPDATE_PROFILE", title = "Bio", payload = mapOf("bio" to newBio)))
                replies.add("Updated your profile bio.")
                continue
            }

            if (Regex("(?i)\\b(?:export|download|backup)\\s+(?:all\\s+)?(?:productivity\\s+)?(?:data|history|records)\\b").containsMatchIn(clause)) {
                actions.add(AiAction(type = "EXPORT_DATA", title = "Export Productivity Data"))
                replies.add("Exporting your complete productivity history.")
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
