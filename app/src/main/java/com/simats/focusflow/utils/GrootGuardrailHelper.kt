package com.simats.focusflow.utils

object GrootGuardrailHelper {

    val DECLINE_MESSAGE = "I'm dedicated exclusively to your productivity inside FocusFlow. I can help you start a timer, create a habit, or review your focus analytics. What would you like to accomplish today?"

    private val OFF_TOPIC_PATTERNS = listOf(
        Regex("(?i)\\b(rajini|rajinikanth|actor|actress|movie|cinema|film|celebrity|hero|director)\\b"),
        Regex("(?i)\\b(cricket|football|fifa|ipl|messi|ronaldo|politician|politics|president|minister)\\b"),
        Regex("(?i)\\b(song|music album|box office|bollywood|hollywood|kollywood|tollywood)\\b"),
        Regex("(?i)^(who is|who was|tell me about)\\s+(?!focusflow|groot|pomodoro|deep work)")
    )

    fun isOffTopicQuery(query: String): Boolean {
        val trimmed = query.trim()
        return OFF_TOPIC_PATTERNS.any { it.containsMatchIn(trimmed) }
    }

    /**
     * Checks if a user prompt is related to FocusFlow features or core productivity.
     */
    fun isFocusFlowDomainQuery(query: String): Boolean {
        val regex = Regex("(?i)\\b(timer|pomodoro|focus|habit|streak|task|break|analytic|stat|report|distraction|routine|goal)\\b")
        return regex.containsMatchIn(query)
    }
}
