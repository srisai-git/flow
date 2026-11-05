package com.simats.focusflow.utils

import com.simats.focusflow.data.models.AiAction

sealed interface ParsedGrootAction {
    val confirmationMessage: String
    val aiAction: AiAction

    data class StartTimer(val minutes: Int, val mode: String = "FOCUS") : ParsedGrootAction {
        override val confirmationMessage: String
            get() = "Started a ${minutes}-minute timer for you!"
        override val aiAction: AiAction
            get() = AiAction(
                type = "START_TIMER",
                title = "Focus Timer",
                description = "Started $minutes min focus timer",
                duration = minutes,
                mode = mode
            )
    }

    data class CreateHabit(val title: String) : ParsedGrootAction {
        override val confirmationMessage: String
            get() = "Created the habit '$title'!"
        override val aiAction: AiAction
            get() = AiAction(
                type = "CREATE_HABIT",
                title = title,
                description = "Created daily habit",
                category = "Daily"
            )
    }

    data class CreateTask(val title: String, val priority: String = "medium") : ParsedGrootAction {
        override val confirmationMessage: String
            get() = "Created the task '$title' with $priority priority!"
        override val aiAction: AiAction
            get() = AiAction(
                type = "CREATE_TASK",
                title = title,
                description = "Created task",
                priority = priority
            )
    }
}

object GrootActionParser {

    private val TIMER_REGEX = Regex("(?i)\\b(?:set|start|begin|run)\\s+(?:a\\s+)?(\\d+)\\s*(?:min|mins|minute|minutes)?\\s*(?:timer|pomodoro|focus)?")
    private val HABIT_REGEX = Regex("(?i)\\b(?:create|add|new|start)\\s+habit\\s+[\"']?([^\"'\\n]+)[\"']?")
    private val TASK_REGEX = Regex("(?i)\\b(?:create|add|new)\\s+task\\s+[\"']?([^\"'\\n]+)[\"']?")

    fun parseAction(prompt: String): ParsedGrootAction? {
        val trimmed = prompt.trim()

        val timerMatch = TIMER_REGEX.find(trimmed)
        if (timerMatch != null && timerMatch.groupValues[1].isNotBlank()) {
            val mins = timerMatch.groupValues[1].toIntOrNull()
            if (mins != null && mins in 1..180) {
                return ParsedGrootAction.StartTimer(minutes = mins)
            }
        }

        val habitMatch = HABIT_REGEX.find(trimmed)
        if (habitMatch != null && habitMatch.groupValues[1].isNotBlank()) {
            val title = habitMatch.groupValues[1].trim().trimEnd('.', '!')
            if (title.isNotBlank()) {
                return ParsedGrootAction.CreateHabit(title = title)
            }
        }

        val taskMatch = TASK_REGEX.find(trimmed)
        if (taskMatch != null && taskMatch.groupValues[1].isNotBlank()) {
            val title = taskMatch.groupValues[1].trim().trimEnd('.', '!')
            if (title.isNotBlank()) {
                return ParsedGrootAction.CreateTask(title = title)
            }
        }

        return null
    }
}
