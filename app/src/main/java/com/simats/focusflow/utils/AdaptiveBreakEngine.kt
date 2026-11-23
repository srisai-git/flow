package com.simats.focusflow.utils

import com.simats.focusflow.data.models.FocusSessionRecord
import java.text.SimpleDateFormat
import java.util.Locale

data class BreakRecommendation(
    val recommendedMinutes: Int,
    val reason: String,
    val isFatigued: Boolean
)

object AdaptiveBreakEngine {

    fun calculateRecommendation(
        focusMinutes: Int,
        completedSessionsToday: Int,
        recentSessions: List<FocusSessionRecord>,
        hadInterruptions: Boolean = false
    ): BreakRecommendation {
        val baseBreak = maxOf(5, (focusMinutes / 5))
        var breakDuration = baseBreak
        var reason = "Balanced 1:5 focus-to-rest ratio for cognitive recharge"
        var isFatigued = false

        val threeHoursAgoMillis = System.currentTimeMillis() - (3L * 60 * 60 * 1000L)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val sessionsInLastThreeHours = recentSessions.filter { session ->
            if (session.sessionType.contains("break", ignoreCase = true)) return@filter false
            val parsedTime = try {
                if (session.completedAt.isNotBlank()) {
                    sdf.parse(session.completedAt)?.time
                } else if (session.startedAt.isNotBlank()) {
                    sdf.parse(session.startedAt)?.time
                } else null
            } catch (_: Exception) {
                null
            } ?: 0L
            parsedTime >= threeHoursAgoMillis
        }

        val focusedMinutesInLastThreeHours = sessionsInLastThreeHours.sumOf { it.durationMinutes }

        val hasLongBreakInLastThreeHours = recentSessions.any { session ->
            if (!session.sessionType.contains("break", ignoreCase = true)) return@any false
            val parsedTime = try {
                if (session.completedAt.isNotBlank()) {
                    sdf.parse(session.completedAt)?.time
                } else if (session.startedAt.isNotBlank()) {
                    sdf.parse(session.startedAt)?.time
                } else null
            } catch (_: Exception) {
                null
            } ?: 0L
            parsedTime >= threeHoursAgoMillis && session.durationMinutes >= 15
        }

        val highCumulativeLoad = completedSessionsToday >= 3 || 
            (focusedMinutesInLastThreeHours >= 75 && !hasLongBreakInLastThreeHours)

        if (highCumulativeLoad) {
            breakDuration = maxOf(breakDuration, 15)
            reason = "High cumulative focus load — take a 15m recovery reset"
            isFatigued = true
        }

        if (hadInterruptions) {
            breakDuration += 2
            reason = if (isFatigued) {
                "High cumulative focus load — take a ${breakDuration}m recovery reset"
            } else {
                "Frequent interruptions detected — extra 2m mental reset"
            }
            isFatigued = true
        }

        return BreakRecommendation(
            recommendedMinutes = breakDuration,
            reason = reason,
            isFatigued = isFatigued
        )
    }
}
