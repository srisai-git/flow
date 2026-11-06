package com.simats.focusflow.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.simats.focusflow.data.models.FocusSessionRecord
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportDataHelper {

    enum class ExportFormat {
        TEXT,
        CSV,
        BOTH
    }

    suspend fun generateAndShareExport(
        context: Context,
        userId: Int,
        userName: String = "FocusFlow User",
        incSessions: Boolean = true,
        incTasks: Boolean = true,
        incStreaks: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val prodRepo = ProductivityRepository()
            val profRepo = ProfileRepository()

            // 1. Fetch real database metrics
            val profileRes = profRepo.getProfile(userId).getOrNull()
            val totalHours = profileRes?.totalFocusHours ?: 0f
            val streakDays = profileRes?.currentStreak ?: 0
            val resolvedName = profileRes?.name?.takeIf { it.isNotBlank() } ?: userName

            val localSessions = try {
                com.simats.focusflow.data.local.FocusSessionDatabase.getInstance(context).getRecentSessions(userId, 5000)
            } catch (_: Exception) {
                emptyList()
            }
            val remoteSessions = if (incSessions) prodRepo.getRecentSessions(userId, 500).getOrDefault(emptyList()) else emptyList()
            val sessions = (localSessions + remoteSessions)
                .filter { it.userId == userId }
                .distinctBy { if (it.id > 0) "id_${it.id}" else "${it.startedAt}_${it.durationMinutes}" }
                .sortedByDescending { it.id }
            val habitsResult = if (incStreaks) prodRepo.getHabits(userId).getOrNull() else null
            val habitsList = habitsResult?.habits ?: emptyList()
            val tasks = if (incTasks) prodRepo.getTasks(userId).getOrDefault(emptyList()) else emptyList()
            val completedTasks = tasks.filter { it.isCompleted }

            val exportTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val dateSlug = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

            // 2. Build Structured Markdown / Text
            val textContent = buildStructuredText(
                userName = resolvedName,
                exportDate = exportTimestamp,
                totalHours = totalHours,
                streakSummary = "$streakDays days",
                sessions = sessions,
                habits = habitsList,
                completedTasks = completedTasks,
                incSessions = incSessions,
                incStreaks = incStreaks,
                incTasks = incTasks
            )

            // 3. Build Structured CSV
            val csvContent = buildStructuredCsv(
                userName = resolvedName,
                exportDate = exportTimestamp,
                totalHours = totalHours,
                streakSummary = "$streakDays days",
                sessions = sessions,
                habits = habitsList,
                completedTasks = completedTasks,
                incSessions = incSessions,
                incStreaks = incStreaks,
                incTasks = incTasks
            )

            // 4. Build Clean Share Message without emojis
            val shareMessageText = buildString {
                appendLine("FOCUSFLOW PRODUCTIVITY EXPORT")
                appendLine("User: $resolvedName")
                appendLine("Date: $exportTimestamp")
                appendLine()
                appendLine("-------------------------")
                appendLine("SUMMARY METRICS")
                appendLine("-------------------------")
                appendLine("Total Focus Time: ${String.format(Locale.US, "%.1f", totalHours)} hrs")
                appendLine("Current Habit Streak: $streakDays days")
                appendLine("Tasks Completed: ${completedTasks.size}")
                appendLine()

                if (incSessions && sessions.isNotEmpty()) {
                    appendLine("-------------------------")
                    appendLine("RECENT FOCUS SESSIONS (Total: ${sessions.size})")
                    appendLine("-------------------------")
                    sessions.take(10).forEach { s ->
                        val date = s.startedAt.take(10).ifBlank { exportTimestamp.take(10) }
                        val tag = s.taskTitle.ifBlank { s.sessionType }
                        appendLine("- $date: ${s.durationMinutes} min ($tag)")
                    }
                    if (sessions.size > 10) {
                        appendLine("  ...and ${sessions.size - 10} more sessions in attached export files")
                    }
                    appendLine()
                }

                if (incStreaks && habitsList.isNotEmpty()) {
                    appendLine("-------------------------")
                    appendLine("HABIT CONSISTENCY")
                    appendLine("-------------------------")
                    habitsList.forEach { h ->
                        val best = if (h.longestStreak > 0) h.longestStreak else h.streakCount
                        appendLine("- ${h.title}: ${h.streakCount}d streak (Best: ${best}d)")
                    }
                    appendLine()
                }

                if (incTasks && completedTasks.isNotEmpty()) {
                    appendLine("-------------------------")
                    appendLine("COMPLETED TASKS")
                    appendLine("-------------------------")
                    completedTasks.take(10).forEach { t ->
                        appendLine("- [x] ${t.title} (${t.priority.uppercase()})")
                    }
                    if (completedTasks.size > 10) {
                        appendLine("  ...and ${completedTasks.size - 10} more tasks in attached export files")
                    }
                    appendLine()
                }

                appendLine("Empowering mindful deep work with FocusFlow.")
            }

            // 5. Write files to internal cache directory
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val txtFile = File(exportDir, "focusflow_export_$dateSlug.txt").apply {
                writeText(textContent)
            }
            val csvFile = File(exportDir, "focusflow_export_$dateSlug.csv").apply {
                writeText(csvContent)
            }

            // 6. Build Content URIs via FileProvider
            val authority = "${context.packageName}.fileprovider"
            val txtUri = FileProvider.getUriForFile(context, authority, txtFile)
            val csvUri = FileProvider.getUriForFile(context, authority, csvFile)

            val shareUris = arrayListOf<Uri>(csvUri, txtUri)

            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareUris)
                putExtra(Intent.EXTRA_SUBJECT, "FocusFlow Productivity Export - $resolvedName")
                putExtra(Intent.EXTRA_TEXT, shareMessageText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                val chooser = Intent.createChooser(shareIntent, "Export Productivity Records").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun buildStructuredText(
        userName: String,
        exportDate: String,
        totalHours: Float,
        streakSummary: String,
        sessions: List<FocusSessionRecord>,
        habits: List<HabitItem>,
        completedTasks: List<TaskItem>,
        incSessions: Boolean,
        incStreaks: Boolean,
        incTasks: Boolean
    ): String = buildString {
        appendLine("================================================================================")
        appendLine("                       FOCUSFLOW PRODUCTIVITY EXPORT                            ")
        appendLine("================================================================================")
        appendLine("User:                $userName")
        appendLine("Export Date:         $exportDate")
        appendLine("Total Focus Hours:   $totalHours hrs")
        appendLine("Habit Streaks:       $streakSummary")
        appendLine("================================================================================")
        appendLine()

        if (incSessions) {
            appendLine("### SECTION 1: FOCUS SESSIONS")
            appendLine("--------------------------------------------------------------------------------")
            appendLine(String.format("%-12s | %-10s | %-14s | %-20s | %-10s", "Date", "Start Time", "Duration (min)", "Task/Tag", "Status"))
            appendLine("--------------------------------------------------------------------------------")
            if (sessions.isEmpty()) {
                appendLine("No completed focus sessions recorded yet.")
            } else {
                sessions.forEach { s ->
                    val dateOnly = s.startedAt.take(10).ifBlank { exportDate.take(10) }
                    val timeOnly = if (s.startedAt.length >= 16) s.startedAt.substring(11, 16) else "00:00"
                    val tag = s.taskTitle ?: s.sessionType
                    appendLine(String.format("%-12s | %-10s | %-14d | %-20s | %-10s", dateOnly, timeOnly, s.durationMinutes, tag.take(20), s.status))
                }
            }
            appendLine()
        }

        if (incStreaks) {
            appendLine("### SECTION 2: HABIT CONSISTENCY")
            appendLine("--------------------------------------------------------------------------------")
            appendLine(String.format("%-24s | %-16s | %-14s | %-12s | %-15s", "Habit Name", "Target Frequency", "Current Streak", "Best Streak", "Total Completed"))
            appendLine("--------------------------------------------------------------------------------")
            if (habits.isEmpty()) {
                appendLine("No habit routines recorded yet.")
            } else {
                habits.forEach { h ->
                    val best = if (h.longestStreak > 0) h.longestStreak else h.streakCount
                    val totalDone = h.totalCompletions
                    appendLine(String.format("%-24s | %-16s | %-14s | %-12s | %-15s", h.title.take(24), "Daily", "${h.streakCount} days", "$best days", "$totalDone"))
                }
            }
            appendLine()
        }

        if (incTasks) {
            appendLine("### SECTION 3: TASKS COMPLETED")
            appendLine("--------------------------------------------------------------------------------")
            appendLine(String.format("%-36s | %-10s | %-16s", "Task Title", "Priority", "Completion Date"))
            appendLine("--------------------------------------------------------------------------------")
            if (completedTasks.isEmpty()) {
                appendLine("No completed tasks in archive.")
            } else {
                completedTasks.forEach { t ->
                    val completionDate = t.dueDate ?: exportDate.take(10)
                    appendLine(String.format("%-36s | %-10s | %-16s", t.title.take(36), t.priority.uppercase(), completionDate))
                }
            }
            appendLine()
        }

        appendLine("================================================================================")
        appendLine("Generated by FocusFlow Mindful Productivity Engine.")
        appendLine("================================================================================")
    }

    private fun buildStructuredCsv(
        userName: String,
        exportDate: String,
        totalHours: Float,
        streakSummary: String,
        sessions: List<FocusSessionRecord>,
        habits: List<HabitItem>,
        completedTasks: List<TaskItem>,
        incSessions: Boolean,
        incStreaks: Boolean,
        incTasks: Boolean
    ): String = buildString {
        appendLine("FOCUSFLOW PRODUCTIVITY EXPORT")
        appendLine("User,\"$userName\"")
        appendLine("Export Date,\"$exportDate\"")
        appendLine("Total Focus Hours,$totalHours")
        appendLine("Habit Streaks Summary,\"$streakSummary\"")
        appendLine("")

        if (incSessions) {
            appendLine("SECTION 1: FOCUS SESSIONS")
            appendLine("Date,Session Type,Duration (Minutes),Status")
            sessions.forEach { s ->
                val dateOnly = s.startedAt.take(10).ifBlank { exportDate.take(10) }
                val sessionType = (s.sessionType.ifBlank { "pomodoro" }).replace("\"", "\"\"")
                appendLine("$dateOnly,\"$sessionType\",${s.durationMinutes},${s.status}")
            }
            appendLine("")
        }

        if (incStreaks) {
            appendLine("SECTION 2: HABIT LOGS")
            appendLine("Habit Name,Completed Date,Status")
            habits.forEach { h ->
                val name = h.title.replace("\"", "\"\"")
                val completedDate = if (h.isCompletedToday) exportDate.take(10) else "Pending"
                val status = if (h.isCompletedToday) "completed" else "active"
                appendLine("\"$name\",$completedDate,$status")
            }
            appendLine("")
        }

        if (incTasks) {
            appendLine("SECTION 3: TASKS COMPLETED")
            appendLine("Task Title,Priority,Completion Date")
            completedTasks.forEach { t ->
                val title = t.title.replace("\"", "\"\"")
                val completionDate = t.dueDate ?: exportDate.take(10)
                appendLine("\"$title\",${t.priority},$completionDate")
            }
            appendLine("")
        }
    }
}
