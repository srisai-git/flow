package com.simats.focusflow.utils

import android.content.Context
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.AiAction
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.repository.StreakRepository
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class LocalGrootResult(
    val reply: String,
    val actions: List<AiAction> = emptyList()
) {
    val action: AiAction?
        get() = actions.firstOrNull()
}

data class LocalUserContext(
    val userName: String,
    val todayFocusMinutes: Int,
    val sessionCount: Int,
    val streak: Int,
    val pendingTasks: List<TaskItem>,
    val completedTasksCount: Int,
    val habits: List<HabitItem>,
    val completedHabitsCount: Int,
    val topHabitTitle: String,
    val totalDistractionMinutes: Long,
    val topDistractingApp: String
)

object LocalGrootBrain {

    fun loadUserContext(context: Context?, userId: Int): LocalUserContext {
        val ctx = context ?: ProductivityRepository.appContext
        if (ctx == null) {
            return LocalUserContext("User", 0, 0, 0, emptyList(), 0, emptyList(), 0, "None", 0L, "None")
        }
        val db = FocusSessionDatabase.getInstance(ctx)
        val todaySessions = db.getTodaySessions(userId)
        val todayFocusMins = db.getTodayFocusMinutes(userId)
        val streak = runBlocking {
            try {
                StreakRepository.getInstance().getStreaks(userId).getOrNull()?.currentStreak ?: 0
            } catch (_: Exception) {
                0
            }
        }
        val tasks = db.getLocalTasks(userId)
        val pendingTasks = tasks.filter { !it.isCompleted }
        val completedTasksCount = tasks.count { it.isCompleted }
        val habits = runBlocking {
            try {
                ProductivityRepository().getHabits(userId).getOrNull()?.habits ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
        val completedHabitsCount = habits.count { it.isCompletedToday }
        val topHabitTitle = habits.maxByOrNull { it.streakCount }?.title ?: "None"
        val distractionSummary = UsageStatsHelper.queryTodayDistractionStats(ctx)
        val distractionMins = distractionSummary.totalDistractionMinutes.toLong()
        val topApp = distractionSummary.apps.firstOrNull { it.usageMinutes > 0 }
        val topDistractStr = if (topApp != null) "${topApp.appName} (${topApp.usageMinutes}m)" else "None"
        val userName = runBlocking {
            try {
                SessionManager.getInstance(ctx).userSessionFlow.first().userName.orEmpty().ifBlank { "User" }
            } catch (_: Exception) {
                "User"
            }
        }
        return LocalUserContext(
            userName = userName,
            todayFocusMinutes = todayFocusMins,
            sessionCount = todaySessions.size,
            streak = streak,
            pendingTasks = pendingTasks,
            completedTasksCount = completedTasksCount,
            habits = habits,
            completedHabitsCount = completedHabitsCount,
            topHabitTitle = topHabitTitle,
            totalDistractionMinutes = distractionMins,
            topDistractingApp = topDistractStr
        )
    }

    fun process(
        query: String,
        context: Context? = null,
        userId: Int = 1,
        userContext: LocalUserContext? = null
    ): LocalGrootResult {
        var cleaned = query.trim()
        if (cleaned.isBlank()) {
            return LocalGrootResult("Hey! Ready to get things done today? Let me know if you want to start a focus session or add a task.")
        }

        val ctxPrefixMatch = Regex("^\\[(?:Active (?:User|FocusFlow)|Active FocusFlow App Context:?)[\\s\\S]*?\\]\\s*\\n?([\\s\\S]*)$", RegexOption.IGNORE_CASE).find(cleaned)
        if (ctxPrefixMatch != null) {
            cleaned = ctxPrefixMatch.groupValues[1].trim()
        }
        val knPrefixMatch = Regex("^\\[App Knowledge:[\\s\\S]*?\\]\\s*\\n?([\\s\\S]*)$", RegexOption.IGNORE_CASE).find(cleaned)
        if (knPrefixMatch != null) {
            cleaned = knPrefixMatch.groupValues[1].trim()
        }
        if (cleaned.isBlank()) {
            return LocalGrootResult("Hey! Ready to get things done today? Let me know if you want to start a focus session or add a task.")
        }

        if (GrootGuardrailHelper.isOffTopicQuery(cleaned)) {
            return LocalGrootResult(GrootGuardrailHelper.DECLINE_MESSAGE)
        }

        val ctx = userContext ?: loadUserContext(context, userId)
        val qLower = cleaned.lowercase(Locale.ROOT)

        if (Regex("(?i)\\b(?:bye\\s+byee?|bye\\b|goodbye|cya|see\\s+ya|catch\\s+you\\s+later)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("Bye byee! Great focus session today. Keep crushing it and lock in again soon!")
        }

        if (Regex("(?i)\\b(?:fr|for\\s+real|no\\s+cap)\\b").containsMatchIn(cleaned) && cleaned.split("\\s+".toRegex()).size <= 4) {
            return LocalGrootResult("For real, no cap. Let's keep the streak compounding.")
        }

        if (Regex("(?i)\\b(?:bet|say\\s+less)\\b").containsMatchIn(cleaned) && cleaned.split("\\s+".toRegex()).size <= 4) {
            return LocalGrootResult("Bet. Say less - I got you covered.")
        }

        if (Regex("(?i)\\b(?:thanks|thank\\s+you|thx|appreciate\\s+it)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("Anytime! Keep that momentum going.")
        }

        if (Regex("(?i)\\b(?:i'?m\\s+cooked|feeling\\s+cooked|cooked)\\b").containsMatchIn(cleaned)) {
            val act = AiAction(
                type = "START_TIMER",
                duration = 15,
                mode = "LONG_BREAK",
                title = "Recovery Break",
                payload = mapOf("duration" to 15, "mode" to "LONG_BREAK", "duration_minutes" to 15)
            )
            return LocalGrootResult(
                reply = "You are not cooked, just experiencing cognitive fatigue. Take a 15-minute recovery breather to reset your brain.",
                actions = listOf(act)
            )
        }

        if (Regex("(?i)\\b(?:how\\s+are\\s+you|how\\s+are\\s+you\\s+doing|what'?s\\s+up|wassup|sup|how'?s\\s+it\\s+going|how\\s+you\\s+doing)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("I'm locked in and ready to help you hit your goals today! Would you like to start a focus sprint, review today's priorities, or check your stats?")
        }

        if (Regex("(?i)\\b(?:who\\s+are\\s+you|what\\s+is\\s+your\\s+name|who\\s+is\\s+groot|what\\s+are\\s+you)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("I am Groot, your AI productivity companion inside FocusFlow. I can help optimize your focus timer, track habits, analyze distractions, and guide your daily workflow.")
        }

        if (Regex("(?i)\\b(?:what\\s+can\\s+you\\s+do|capabilities|what\\s+actions\\s+can\\s+you\\s+do|commands|help\\s+me\\s+with\\s+commands|what\\s+do\\s+you\\s+do)\\b").containsMatchIn(cleaned) || cleaned.equals("help", ignoreCase = true)) {
            return LocalGrootResult("Here are the actions I can take for you right now:\n• Focus Timer: 'start a 25 min timer', 'lock in for 90 mins', '15 min break'\n• Strict Focus Mode: 'enable strict mode', 'block Instagram', 'unblock YouTube'\n• Tasks: 'add task Study Physics at 5 pm with high priority', 'finish task Study Physics', 'rename task'\n• Habits: 'create habit Read 20 pages daily', 'check off habit Read 20 pages'\n• Navigation: 'go to timer', 'open distractions', 'show habits', 'open tasks'\n• Profile & Export: 'change name to Sri', 'update bio', 'export my data'\n• Analytics & Advice: 'my stats today', 'how do streaks work', 'Pomodoro tips'")
        }

        if (Regex("^(?:hi|hello|hey|yo|good\\s+morning|good\\s+afternoon|good\\s+evening|morning)(?:\\s+(?:there|groot|focusflow|everyone|friend|buddy))?[!.]*$", RegexOption.IGNORE_CASE).containsMatchIn(cleaned)
            || (Regex("(?i)\\b(?:hi|hello|hey|yo|good\\s+morning)\\b").containsMatchIn(cleaned) && cleaned.split("\\s+".toRegex()).size <= 3)
        ) {
            return LocalGrootResult("Hey! Ready to get things done today? Let me know if you want to start a focus session or add a task.")
        }

        if (Regex("(?i)\\b(pomodoro|pomodoro technique|how.*pomodoro|explain.*pomodoro|pomodoro method)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("The Pomodoro Technique breaks work into 25-minute focused sprints followed by 5-minute short breaks. After 4 cycles, take a longer 15-30 minute restorative break. In FocusFlow, you can start any custom sprint by saying 'start a 25 min timer' or 'lock in for 45 mins'.")
        }

        if (Regex("(?i)\\b(how.*streak|streak.*work|streak.*rule|streak.*calculate|streak.*increment|streak.*qualif|explain.*streak)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("In FocusFlow, streaks increment strictly on real productivity completions: completing a focus session, checking off a task, or completing a habit today (never on login alone). Complete at least one action every day to keep your streak compounding!")
        }

        if (Regex("(?i)\\b(?:my\\s+stats|today'?s?\\s+stats|focus\\s+stats|focus\\s+summary|daily\\s+summary|my\\s+progress|how\\s+did\\s+i\\s+do|how\\s+am\\s+i\\s+doing|today'?s?\\s+progress|stats\\s+today|my\\s+analytics)\\b").containsMatchIn(cleaned)) {
            val statsReply = buildString {
                append("Here is your productivity breakdown today, ${ctx.userName}:\n")
                append("• Focus Time: ${ctx.todayFocusMinutes}m across ${ctx.sessionCount} session(s)\n")
                append("• Active Streak: ${ctx.streak} day(s)\n")
                append("• Screen Distractions: ${ctx.totalDistractionMinutes}m (Top: ${ctx.topDistractingApp})\n")
                append("• Habits Completed: ${ctx.completedHabitsCount}/${ctx.habits.size} (Top streak: ${ctx.topHabitTitle})\n")
                append("• Tasks: ${ctx.completedTasksCount} completed, ${ctx.pendingTasks.size} pending")
                if (ctx.pendingTasks.isNotEmpty()) {
                    append(" (${ctx.pendingTasks.take(2).joinToString { it.title }})")
                }
            }
            return LocalGrootResult(statsReply)
        }

        if (Regex("(?i)\\b(what are my tasks|pending tasks|my tasks|list tasks|show tasks)\\b").containsMatchIn(cleaned)) {
            val rep = if (ctx.pendingTasks.isNotEmpty()) {
                "You have ${ctx.pendingTasks.size} pending task(s):\n" + ctx.pendingTasks.take(5).joinToString("\n") { "• ${it.title} [${it.priority.uppercase()}]${if (it.dueDate.isNotBlank()) " due ${it.dueDate}" else ""}" }
            } else {
                "All tasks are completed! You have zero pending tasks."
            }
            return LocalGrootResult(rep)
        }

        if (Regex("(?i)\\b(how are my habits|my habits|active habits|habit status|habits today)\\b").containsMatchIn(cleaned)) {
            val rep = if (ctx.habits.isNotEmpty()) {
                "Today's Habit Tracker (${ctx.completedHabitsCount}/${ctx.habits.size} completed):\n" + ctx.habits.joinToString("\n") { "• ${it.title}: ${if (it.isCompletedToday) "Completed" else "Pending"} (${it.streakCount} day streak)" }
            } else {
                "You don't have any active habits created yet. Tell me 'create habit <name>' to start tracking one!"
            }
            return LocalGrootResult(rep)
        }

        if (Regex("(?i)\\b(how much focus|focus time today|how much did i focus|today'?s? focus|focus today|my focus minutes)\\b").containsMatchIn(cleaned)) {
            val rep = if (ctx.todayFocusMinutes > 0) {
                "You have logged ${ctx.todayFocusMinutes}m of focus today across ${ctx.sessionCount} sessions."
            } else {
                "You haven't logged any focus sessions today yet. Ready to start one?"
            }
            return LocalGrootResult(rep)
        }

        if (Regex("(?i)\\b(distraction|screen time|top distracting app|top distraction|my distractions)\\b").containsMatchIn(cleaned)) {
            val rep = if (ctx.totalDistractionMinutes > 0) {
                "Today's screen distractions total ${ctx.totalDistractionMinutes}m. Top distracting app: ${ctx.topDistractingApp}."
            } else {
                "Zero distraction minutes recorded today. Clean focus!"
            }
            return LocalGrootResult(rep)
        }

        if (Regex("(?i)\\b(what is my streak|my streak|active streak|current streak)\\b").containsMatchIn(cleaned)) {
            val rep = if (ctx.streak > 0) {
                "Your active streak is ${ctx.streak} days. Keep up the consistent focus!"
            } else {
                "You currently have a 0-day streak. Complete a focus session, task, or habit today to ignite it!"
            }
            return LocalGrootResult(rep)
        }

        if (Regex("(?i)\\b(procrastinat|stop procrastinating|how to stop procrastination|beat procrastination)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("Procrastination happens when a task feels too big or ambiguous. Beat it with the 2-Minute Rule: commit to working on the first small step for just 2 minutes. Once you start, momentum takes over. Tell me 'start a 15 min timer' to begin!")
        }

        if (Regex("(?i)\\b(help me focus|how to focus|study tips|how to study|study advice|focus tips|focus advice)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("Here are 3 key focus rules:\n1. Lock out distractions: turn on Strict Focus Mode to block social media.\n2. Single-task: pick only your top priority task.\n3. Sprint: start a 25 or 45-minute timer and don't stop until the bell rings. Tell me 'lock in for 25 mins' to start!")
        }

        if (Regex("(?i)\\b(motivat.*|give me motivation|quote|inspire|feeling lazy|unmotivated)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("Discipline is choosing between what you want now and what you want most. Every 25-minute focus block you complete compounds into massive long-term progress. You've got this—let's lock in for one focused sprint right now!")
        }

        if (Regex("(?i)\\b(what should i do next|what'?s next|what to do now|next task|recommend task)\\b").containsMatchIn(cleaned)) {
            val rep = if (ctx.pendingTasks.isNotEmpty()) {
                "Next priority: Tackle '${ctx.pendingTasks.first().title}'. You have ${ctx.pendingTasks.size} pending task(s). Want to start a 25-minute timer on it?"
            } else if (ctx.habits.any { !it.isCompletedToday }) {
                "All tasks are complete! You still have daily habits pending (e.g. '${ctx.habits.first { !it.isCompletedToday }.title}'). Want to check it off?"
            } else {
                "All tasks and habits completed today! Fantastic job. Ready for another focus sprint or a well-deserved break?"
            }
            return LocalGrootResult(rep)
        }

        if (Regex("(?i)\\b(core purpose|tech stack|what is focusflow|what stack|built with|how does this app work|how focusflow work)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("FocusFlow (com.simats.focusflow) is an offline-first Android productivity app built with Kotlin, Jetpack Compose, Room, DataStore, and a 100% on-device Groot AI engine.")
        }

        if (Regex("(?i)\\b(strict focus|accessibility service|how strict mode|how strict focus|strict mode work|block app)\\b").containsMatchIn(cleaned)
            && !Regex("(?i)\\b(block|unblock)\\s+(?!mode)").containsMatchIn(cleaned)
        ) {
            return LocalGrootResult("Strict Focus Mode runs directly via FocusBlockerAccessibilityService (GLOBAL_ACTION_HOME) with zero overlay permissions required. When active, opening any blocked app immediately redirects home with 150ms and 350ms follow-up enforcement.")
        }

        if (Regex("(?i)\\b(explain the focus timer|how timer work|how does timer work|how does the focus timer work|timer architecture)\\b").containsMatchIn(cleaned)
            || (Regex("(?i)\\b(focus timer|foreground service)\\b").containsMatchIn(cleaned) && Regex("(?i)\\b(how|what|explain|tell|describe|work)\\b").containsMatchIn(cleaned))
        ) {
            return LocalGrootResult("Focus Timer supports any custom duration (e.g., 25m, 90m, 180m, or any duration 1-720m) across FOCUS, SHORT_BREAK, and LONG_BREAK modes via a ForegroundService with live notification.")
        }

        if (Regex("(?i)\\b(distraction analysis|how distraction|usage stat|screen time work|track distraction|distraction tracking)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("Distraction Analysis tracks foreground usage via a single-foreground UsageStatsManager state machine displayed with clean horizontal progress bars for apps used for at least one minute.")
        }

        if (Regex("(?i)\\b(past 7|focus time analysis|weekly focus goal|7 days focus)\\b").containsMatchIn(cleaned)
            && !Regex("(?i)\\b(open|show|go to|navigate)\\b").containsMatchIn(cleaned)
        ) {
            return LocalGrootResult("Focus Time Analysis shows Today's time-of-day breakdown plus a rolling Past 7 Days Focus bar chart against the user's reactive weekly goal with a left-aligned threshold goal line.")
        }

        if (Regex("(?i)\\b(momentum|habit graph|habit tracking|how habit|habits work|how do habits|how are habits|how tasks and habits)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("Tasks show exact deadline times with no countdown clutter. Habits use frictionless creation (no day-of-week selector), inline streak counts, a unified Groot AI Habit Insights card with consistency score, and a dotted-line momentum trend graph.")
        }

        if (Regex("(?i)\\b(how does export|how export work|explain export)\\b").containsMatchIn(cleaned)) {
            return LocalGrootResult("Export Productivity Data generates a comprehensive full-history report in CSV and JSON formats covering all past focus sessions, tasks, habits, and distractions across all time.")
        }

        val breakdownMatch = Regex("(?i)\\b(?:break\\s*down|split|steps\\s+for)\\s+(?:task\\s+)?['\"]?([^'\"\\n]+)['\"]?$").find(cleaned)
        if (breakdownMatch != null) {
            val t = breakdownMatch.groupValues[1].trim().trimEnd('.', '!')
            val steps = breakdownTaskLocally(t)
            return LocalGrootResult("Here is a step-by-step breakdown for '$t':\n" + steps.mapIndexed { idx, s -> "${idx + 1}. $s" }.joinToString("\n"))
        }

        val parsedAction = GrootActionParser.parseActions(cleaned)
        if (parsedAction != null && parsedAction.actions.isNotEmpty()) {
            return LocalGrootResult(
                reply = parsedAction.confirmationMessage,
                actions = parsedAction.actions
            )
        }

        val isLockIn = Regex("(?i)\\b(?:lock(?:ed)?\\s+in|locking\\s+in)\\b").containsMatchIn(cleaned)
        val hourMatch = Regex("(?i)(\\d+(?:\\.\\d+)?)\\s*(?:hours?|hrs?|h)\\b").find(cleaned)
        val minMatch = Regex("(?i)(\\d+)\\s*(?:mins?|minutes?|m)\\b").find(cleaned)
        val isTimerCmd = Regex("(?i)\\b(?:start|set|run|begin|launch|activate)\\b").containsMatchIn(cleaned)
        val hasTimerKw = qLower.contains("timer") || qLower.contains("focus") || qLower.contains("break") || qLower.contains("pomodoro") || qLower.contains("session")

        if (isLockIn || (hasTimerKw && (isTimerCmd || hourMatch != null || minMatch != null || qLower.contains("min")))) {
            var dur = 25
            if (hourMatch != null) {
                val h = hourMatch.groupValues[1].toDoubleOrNull() ?: 1.0
                dur = (h * 60).toInt()
            } else if (minMatch != null) {
                dur = minMatch.groupValues[1].toIntOrNull() ?: 25
            } else {
                val numMatch = Regex("\\b(?:for|to|of)?\\s*(\\d+)\\s*(?:mins?|minutes?|m)?\\b", RegexOption.IGNORE_CASE).find(cleaned)
                if (numMatch != null && (numMatch.groupValues[1].toIntOrNull() ?: 0) in 1..720) {
                    dur = numMatch.groupValues[1].toInt()
                } else if (isLockIn) {
                    dur = 45
                }
            }
            dur = dur.coerceIn(1, 720)
            var mode = "FOCUS"
            if (qLower.contains("break") || qLower.contains("rest")) {
                mode = if (dur >= 15) "LONG_BREAK" else "SHORT_BREAK"
                if (dur == 25 && hourMatch == null && minMatch == null) dur = 5
            }
            val act = AiAction(
                type = "START_TIMER",
                duration = dur,
                mode = mode,
                title = "Focus Timer",
                payload = mapOf("duration" to dur, "mode" to mode, "duration_minutes" to dur)
            )
            val label = if (mode == "FOCUS") "focus session" else "break"
            return LocalGrootResult(
                reply = "Started a $dur-minute $label for you. Lock in!",
                actions = listOf(act)
            )
        }

        val compTaskMatch = Regex("(?i)\\b(?:finish|complete|check\\s*off|mark\\s+complete|mark\\s+done|done)\\s+(?:a\\s+|the\\s+)?(?:task\\s+)?['\"]?([^'\"\\n]+)['\"]?$").find(cleaned)
        if (compTaskMatch != null && !qLower.contains("habit") && !qLower.contains("timer")) {
            val title = compTaskMatch.groupValues[1].trim().trimEnd('.', '!')
            val act = AiAction(type = "COMPLETE_TASK", title = title, payload = mapOf("title" to title))
            return LocalGrootResult("Marked task '$title' as completed.", listOf(act))
        }

        val editTaskMatch = Regex("(?i)\\b(?:edit|rename|change|update)\\s+task\\s+['\"]?([^'\"\\n]+)['\"]?\\s+(?:to|as)\\s+['\"]?([^'\"\\n]+)['\"]?$").find(cleaned)
        if (editTaskMatch != null) {
            val oldT = editTaskMatch.groupValues[1].trim().trimEnd('.', '!')
            val newT = editTaskMatch.groupValues[2].trim().trimEnd('.', '!')
            val act = AiAction(type = "EDIT_TASK", title = oldT, payload = mapOf("new_title" to newT, "title" to oldT))
            return LocalGrootResult("Updated task '$oldT' to '$newT'.", listOf(act))
        }

        val createTaskMatch = Regex("(?i)\\b(?:set|create|add|new|schedule)\\s+(?:a\\s+)?task\\s+(?:named|to|called)?\\s*['\"]?([^'\"\\n]+)['\"]?$").find(cleaned)
        if (createTaskMatch != null) {
            var raw = createTaskMatch.groupValues[1].trim().trimEnd('.', '!')
            var priority = "medium"
            if (qLower.contains("high priority") || qLower.contains("urgent") || qLower.contains("critical")) {
                priority = "high"
                raw = raw.replace(Regex("(?i)\\s+(?:with|as)?\\s*high\\s+priority"), "").trim()
            } else if (qLower.contains("low priority")) {
                priority = "low"
                raw = raw.replace(Regex("(?i)\\s+(?:with|as)?\\s*low\\s+priority"), "").trim()
            }
            var dueDate = "Today"
            var deadlineTs: Long? = null
            val timeMatch = Regex("(?i)\\b(?:today|tomorrow)?\\s*(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b").find(raw)
            if (timeMatch != null) {
                val cal = Calendar.getInstance()
                if (qLower.contains("tomorrow")) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    dueDate = "Tomorrow"
                }
                val hour = timeMatch.groupValues[1].toInt()
                val minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
                val isPm = timeMatch.groupValues[3].equals("pm", ignoreCase = true)
                val calHour = if (isPm && hour < 12) hour + 12 else if (!isPm && hour == 12) 0 else hour
                cal.set(Calendar.HOUR_OF_DAY, calHour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                deadlineTs = cal.timeInMillis
                raw = raw.replace(timeMatch.value, "").trim()
            } else if (qLower.contains("tomorrow")) {
                dueDate = "Tomorrow"
                raw = raw.replace(Regex("(?i)\\s+due\\s+tomorrow|\\s+tomorrow"), "").trim()
            }
            raw = raw.replace(Regex("(?i)\\s+due\\s+today|\\s+today"), "").trim()
            val act = AiAction(
                type = "CREATE_TASK",
                title = raw,
                priority = priority,
                deadlineTimestamp = deadlineTs,
                payload = mapOf("title" to raw, "priority" to priority, "due_date" to dueDate, "deadlineTimestamp" to deadlineTs)
            )
            val timeStr = if (deadlineTs != null) " with deadline at ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(deadlineTs))}" else ""
            return LocalGrootResult("Created task '$raw' with $priority priority$timeStr.", listOf(act))
        }

        val compHabitMatch = Regex("(?i)\\b(?:check\\s*off|complete|done|log|finish|mark\\s+complete|mark\\s+done)\\s+(?:a\\s+|the\\s+)?(?:habit\\s+)?['\"]?([^'\"\\n]+)['\"]?$").find(cleaned)
        if (compHabitMatch != null && !qLower.contains("task") && !qLower.contains("timer")) {
            val title = compHabitMatch.groupValues[1].trim().trimEnd('.', '!')
            val act = AiAction(type = "COMPLETE_HABIT", title = title, payload = mapOf("title" to title))
            return LocalGrootResult("Checked off habit '$title' for today.", listOf(act))
        }

        val editHabitMatch = Regex("(?i)\\b(?:edit|rename|change|update)\\s+habit\\s+['\"]?([^'\"\\n]+)['\"]?\\s+(?:to|as)\\s+['\"]?([^'\"\\n]+)['\"]?$").find(cleaned)
        if (editHabitMatch != null) {
            val oldT = editHabitMatch.groupValues[1].trim().trimEnd('.', '!')
            val newT = editHabitMatch.groupValues[2].trim().trimEnd('.', '!')
            val act = AiAction(type = "EDIT_HABIT", title = oldT, payload = mapOf("new_title" to newT, "title" to oldT))
            return LocalGrootResult("Updated habit '$oldT' to '$newT'.", listOf(act))
        }

        val createHabitMatch = Regex("(?i)\\b(?:set|create|add|new|track|build)\\s+(?:a\\s+)?habit\\s+(?:named|to|called)?\\s*['\"]?([^'\"\\n]+)['\"]?$").find(cleaned)
        if (createHabitMatch != null) {
            val hTitle = createHabitMatch.groupValues[1].trim().trimEnd('.', '!')
            val act = AiAction(
                type = "CREATE_HABIT",
                title = hTitle,
                category = "Daily",
                payload = mapOf("title" to hTitle, "frequency" to "daily", "icon" to "bolt")
            )
            return LocalGrootResult("Created the habit '$hTitle'.", listOf(act))
        }

        val navMatch = Regex("(?i)\\b(?:open|show|go\\s+to|take\\s+me\\s+to|navigate\\s+to|view|switch\\s+to)\\s+(?:my\\s+|the\\s+)?([a-zA-Z_\\s]+)$").find(cleaned)
        if (navMatch != null) {
            val dest = navMatch.groupValues[1].trim().lowercase(Locale.ROOT)
            val screen = when {
                dest.contains("distract") || dest.contains("screen time") -> "distractions"
                dest.contains("timer") || dest.contains("clock") || dest.contains("pomodoro") -> "timer"
                dest.contains("habit") -> "habits"
                dest.contains("task") || dest.contains("todo") -> "tasks"
                dest.contains("analysis") || dest.contains("analytic") || dest.contains("trend") || dest.contains("insight") -> "focus_analysis"
                dest.contains("profile") -> "profile"
                dest.contains("setting") || dest.contains("pref") -> "settings"
                dest.contains("home") || dest.contains("dash") -> "home"
                else -> null
            }
            if (screen != null) {
                val act = AiAction(type = "NAVIGATE", route = screen, screenName = screen, payload = mapOf("screen" to screen, "tab" to screen, "route" to screen))
                return LocalGrootResult("Navigating to $screen.", listOf(act))
            }
        }

        val nameMatch = Regex("(?i)\\b(?:change|update|set)\\s+(?:my\\s+)?name\\s+to\\s+['\"]?([^'\"\\n]+)['\"]?$").find(cleaned)
        if (nameMatch != null) {
            val newName = nameMatch.groupValues[1].trim().trimEnd('.', '!')
            val act = AiAction(type = "UPDATE_PROFILE", title = newName, payload = mapOf("name" to newName))
            return LocalGrootResult("Updated your profile name to '$newName'.", listOf(act))
        }

        val bioMatch = Regex("(?i)\\b(?:change|update|set)\\s+(?:my\\s+)?bio\\s+to\\s+['\"]?([^'\"\\n]+)['\"]?$").find(cleaned)
        if (bioMatch != null) {
            val newBio = bioMatch.groupValues[1].trim().trimEnd('.', '!')
            val act = AiAction(type = "UPDATE_PROFILE", title = "Bio", payload = mapOf("bio" to newBio))
            return LocalGrootResult("Updated your profile bio.", listOf(act))
        }

        if (Regex("(?i)\\b(?:export|download|backup)\\s+(?:all\\s+|my\\s+|the\\s+)?(?:productivity\\s+)?(?:data|history|records)\\b").containsMatchIn(cleaned)) {
            val act = AiAction(type = "EXPORT_DATA", title = "Export Productivity Data", payload = mapOf("format" to "all"))
            return LocalGrootResult("Exporting your complete productivity data.", listOf(act))
        }

        if (Regex("(?i)\\b(?:export|share|download|save)\\s+(?:this\\s+)?(?:chat|transcript|conversation)\\b").containsMatchIn(cleaned)) {
            val act = AiAction(type = "EXPORT_CHAT", title = "Export Chat", payload = mapOf("format" to "chat"))
            return LocalGrootResult("Exporting chat transcript.", listOf(act))
        }

        if (Regex("(?i)\\b(?:clear|delete|reset)\\s+(?:this\\s+)?(?:chat|history|conversation)\\b").containsMatchIn(cleaned)) {
            val act = AiAction(type = "CLEAR_CHAT", title = "Clear Chat")
            return LocalGrootResult("Cleared chat history.", listOf(act))
        }

        val contextualReply = when {
            qLower.contains("task") || qLower.contains("todo") ->
                if (ctx.pendingTasks.isNotEmpty()) "You currently have ${ctx.pendingTasks.size} pending task(s), including '${ctx.pendingTasks.first().title}'. Tell me 'finish task' or 'start a 25 min timer' to work on it."
                else "All tasks are currently completed! Tell me 'add task <name>' to schedule a new one."
            qLower.contains("habit") || qLower.contains("routine") ->
                if (ctx.habits.isNotEmpty()) "You have completed ${ctx.completedHabitsCount} of ${ctx.habits.size} habit(s) today. Tell me 'check off habit <name>' to log completion."
                else "No habits set yet. Tell me 'create habit <name> daily' to build a new routine."
            qLower.contains("timer") || qLower.contains("focus") || qLower.contains("break") ->
                "Want to start a focus session? Tell me 'start a 25 min timer' or 'lock in for 45 mins' and I'll launch it immediately."
            qLower.contains("distract") || qLower.contains("app") || qLower.contains("screen") ->
                "Today's distractions: ${ctx.totalDistractionMinutes}m. Enable Strict Focus Mode to lock down distracting apps during focus hours."
            ctx.pendingTasks.isNotEmpty() ->
                "I'm here to help, ${ctx.userName}. You currently have ${ctx.pendingTasks.size} pending task(s) like '${ctx.pendingTasks.first().title}' and an active ${ctx.streak}-day streak. Want to start a focus session on it?"
            ctx.todayFocusMinutes > 0 ->
                "You've logged ${ctx.todayFocusMinutes}m of focus today with an active ${ctx.streak}-day streak, ${ctx.userName}. Let me know if you want to start another session, add a new task, or review your habits."
            else ->
                "Ready to build momentum today, ${ctx.userName}? You can ask me to start a focus timer, create a task, track a habit, or check your productivity stats."
        }
        return LocalGrootResult(contextualReply)
    }

    fun getLocalHabitInsights(habits: List<HabitItem>): String {
        if (habits.isEmpty()) {
            return "Create your first habit to begin building daily streak momentum."
        }
        val completed = habits.count { it.isCompletedToday }
        val total = habits.size
        val consistency = (completed.toFloat() / total * 100).toInt()
        val topStreak = habits.maxOfOrNull { it.streakCount } ?: 0
        return "Consistency is at $consistency% today ($completed of $total completed). Strongest streak is $topStreak days. Keep daily habits active to compound your focus score."
    }

    fun getLocalFocusAdvice(focusMinutes: Int, distractionMinutes: Long, streak: Int): String {
        return "You have $focusMinutes focus minutes logged today with $distractionMinutes minutes of distraction and an active $streak-day streak. Keep your sessions structured with short breathers to maximize cognitive endurance."
    }

    fun breakdownTaskLocally(taskTitle: String): List<String> {
        val t = taskTitle.trim()
        return listOf(
            "Define requirements and scope for $t",
            "Prepare resources and workspace for $t",
            "Execute core implementation of $t",
            "Review, test, and verify $t",
            "Finalize and mark $t complete"
        )
    }
}
