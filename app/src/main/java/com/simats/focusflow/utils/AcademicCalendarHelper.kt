package com.simats.focusflow.utils

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.repository.ProductivityRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AcademicEvent(
    val id: Long,
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val description: String? = null,
    val location: String? = null
) {
    val formattedDate: String
        get() = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(startMillis))

    val formattedTime: String
        get() = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(startMillis))
}

object AcademicCalendarHelper {

    private val KEYWORDS = listOf(
        "exam", "test", "viva", "quiz", "assignment", "submission", "lab", "lecture", "class"
    )

    fun hasCalendarPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun fetchAcademicEvents(context: Context): List<AcademicEvent> {
        if (!hasCalendarPermission(context)) return emptyList()

        val results = mutableListOf<AcademicEvent>()
        val startMillis = System.currentTimeMillis()
        val endMillis = startMillis + (7L * 24 * 60 * 60 * 1000L)

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION
        )

        try {
            val cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.use { c ->
                val idIdx = c.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                val titleIdx = c.getColumnIndex(CalendarContract.Instances.TITLE)
                val beginIdx = c.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endIdx = c.getColumnIndex(CalendarContract.Instances.END)
                val descIdx = c.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
                val locIdx = c.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)

                while (c.moveToNext()) {
                    val title = if (titleIdx >= 0) c.getString(titleIdx) ?: "" else ""
                    val desc = if (descIdx >= 0) c.getString(descIdx) else null
                    val begin = if (beginIdx >= 0) c.getLong(beginIdx) else 0L
                    val end = if (endIdx >= 0) c.getLong(endIdx) else 0L
                    val id = if (idIdx >= 0) c.getLong(idIdx) else 0L
                    val loc = if (locIdx >= 0) c.getString(locIdx) else null

                    if (title.isBlank()) continue

                    val matches = KEYWORDS.any { kw ->
                        title.contains(kw, ignoreCase = true) || (desc?.contains(kw, ignoreCase = true) == true)
                    }

                    if (matches) {
                        results.add(
                            AcademicEvent(
                                id = id,
                                title = title,
                                startMillis = begin,
                                endMillis = end,
                                description = desc,
                                location = loc
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
        }

        return results.distinctBy { "${it.title.trim()}_${it.startMillis}" }
    }

    suspend fun autoSchedulePrepTasks(
        events: List<AcademicEvent>,
        existingTasks: List<TaskItem>,
        userId: Int,
        repository: ProductivityRepository
    ): Boolean {
        var createdAny = false
        val existingTitles = existingTasks.map { it.title.trim().lowercase(Locale.getDefault()) }.toSet()

        for (event in events) {
            val prepTitle = "Prep for ${event.title.trim()}"
            if (existingTitles.contains(prepTitle.lowercase(Locale.getDefault()))) {
                continue
            }

            val prepTimestamp = event.startMillis - (24L * 60 * 60 * 1000L)
            val effectiveDeadline = if (prepTimestamp > System.currentTimeMillis()) prepTimestamp else System.currentTimeMillis()
            val dueDateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(effectiveDeadline))
            val scheduledTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(event.startMillis))

            val isHighPriority = listOf("exam", "test", "viva", "quiz").any {
                event.title.contains(it, ignoreCase = true)
            }
            val priority = if (isHighPriority) "high" else "medium"

            try {
                repository.createTask(
                    userId = userId,
                    title = prepTitle,
                    description = "Automatic academic preparation for ${event.title} (${event.formattedDate} at ${event.formattedTime})",
                    priority = priority,
                    dueDate = dueDateStr,
                    category = "Calendar Auto-Scheduled",
                    scheduledTime = scheduledTimeStr,
                    deadlineTimestamp = effectiveDeadline
                )
                createdAny = true
            } catch (_: Exception) {}
        }
        return createdAny
    }
}
