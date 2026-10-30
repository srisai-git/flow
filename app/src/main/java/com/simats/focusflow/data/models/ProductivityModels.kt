package com.simats.focusflow.data.models

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

/**
 * Task item data model.
 */
@Immutable
data class TaskItem(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("category")
    val category: String = "General",

    @SerializedName("priority")
    val priority: String = "medium", // "low", "medium", "high"

    @SerializedName("due_date")
    val dueDate: String = "",

    @SerializedName("is_completed")
    val isCompleted: Boolean = false,

    @SerializedName("created_at")
    val createdAt: String? = null
)

/**
 * Habit item data model.
 */
@Immutable
data class HabitItem(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("icon")
    val icon: String = "habit",

    @SerializedName("frequency")
    val frequency: String = "daily",

    @SerializedName("streak_count")
    val streakCount: Int = 0,

    @SerializedName("longest_streak")
    val longestStreak: Int = 0,

    @SerializedName("total_completions")
    val totalCompletions: Int = 0,

    @SerializedName("is_completed_today")
    val isCompletedToday: Boolean = false
)

@Immutable
data class HabitWeeklyChartItem(
    @SerializedName("date") val date: String = "",
    @SerializedName("day") val day: String = "",
    @SerializedName("completed_count") val completedCount: Int = 0,
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("completion_rate") val completionRate: Float = 0f
)

@Immutable
data class HabitMonthlyChartItem(
    @SerializedName("week_label") val weekLabel: String = "",
    @SerializedName("completed_count") val completedCount: Int = 0,
    @SerializedName("completion_rate") val completionRate: Float = 0f
)

@Immutable
data class HabitUserStats(
    @SerializedName("current_streak") val currentStreak: Int = 0,
    @SerializedName("longest_streak") val longestStreak: Int = 0,
    @SerializedName("total_habits_completed") val totalHabitsCompleted: Int = 0,
    @SerializedName("total_focus_minutes") val totalFocusMinutes: Int = 0,
    @SerializedName("avg_consistency_percent") val avgConsistencyPercent: Float = 0f,
    @SerializedName("weekly_chart_data") val weeklyChartData: List<HabitWeeklyChartItem> = emptyList(),
    @SerializedName("monthly_chart_data") val monthlyChartData: List<HabitMonthlyChartItem> = emptyList()
)

/**
 * Dashboard aggregate payload model.
 */
@Immutable
data class DashboardData(
    @SerializedName("user_name")
    val userName: String = "Sri Sai",

    @SerializedName("user_avatar")
    val userAvatar: String = "",

    @SerializedName("focus_minutes_today")
    val focusMinutesToday: Int = 0,

    @SerializedName("focus_goal_minutes")
    val focusGoalMinutes: Int = 240,

    @SerializedName("pending_task_count")
    val pendingTaskCount: Int = 0,

    @SerializedName("completed_task_count")
    val completedTaskCount: Int = 0,

    @SerializedName("streak_days")
    val streakDays: Int = 1,

    @SerializedName("longest_streak")
    val longestStreak: Int = 1,

    @SerializedName("total_habits_completed")
    val totalHabitsCompleted: Int = 0,

    @SerializedName("total_focus_minutes")
    val totalFocusMinutes: Int = 0,

    @SerializedName("priority_tasks")
    val priorityTasks: List<TaskItem> = emptyList(),

    @SerializedName("habits")
    val habits: List<HabitItem> = emptyList()
)

// API Responses
data class DashboardResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("data")
    val data: DashboardData? = null
)

data class TasksResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("data")
    val data: List<TaskItem> = emptyList()
)

data class TaskActionResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("is_completed")
    val isCompleted: Boolean? = null,

    @SerializedName("data")
    val data: TaskItem? = null
)

data class HabitsResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("data")
    val data: List<HabitItem> = emptyList(),

    @SerializedName("user_stats")
    val userStats: HabitUserStats? = null
)

data class HabitsResult(
    val habits: List<HabitItem> = emptyList(),
    val userStats: HabitUserStats = HabitUserStats()
)

data class HabitActionResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("is_completed_today")
    val isCompletedToday: Boolean? = null,

    @SerializedName("streak_count")
    val streakCount: Int? = null,

    @SerializedName("data")
    val data: HabitItem? = null
)

data class LogSessionRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("duration_minutes")
    val durationMinutes: Int,

    @SerializedName("session_type")
    val sessionType: String = "pomodoro",

    @SerializedName("task_id")
    val taskId: Int? = null
)

data class LogSessionResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("session_id")
    val sessionId: Int? = null,

    @SerializedName("today_total_minutes")
    val todayTotalMinutes: Int? = null
)

@Immutable
data class FocusSessionRecord(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("user_id")
    val userId: Int = 0,

    @SerializedName("task_id")
    val taskId: Int? = null,

    @SerializedName("duration_minutes")
    val durationMinutes: Int = 25,

    @SerializedName("session_type")
    val sessionType: String = "pomodoro",

    @SerializedName("status")
    val status: String = "completed",

    @SerializedName("started_at")
    val startedAt: String = "",

    @SerializedName("completed_at")
    val completedAt: String = "",

    @SerializedName("task_title")
    val taskTitle: String = "",

    @SerializedName("task_category")
    val taskCategory: String = ""
)

data class FocusSessionsResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("data")
    val data: List<FocusSessionRecord> = emptyList()
)

data class CreateTaskRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String = "",

    @SerializedName("priority")
    val priority: String = "medium",

    @SerializedName("due_date")
    val dueDate: String = "",

    @SerializedName("category")
    val category: String = "General"
)

data class ToggleTaskRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("task_id")
    val taskId: Int,

    @SerializedName("action")
    val action: String = "toggle",

    @SerializedName("is_completed")
    val isCompleted: Boolean? = null
)

data class DeleteTaskRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("task_id")
    val taskId: Int,

    @SerializedName("action")
    val action: String = "delete"
)

data class ToggleHabitRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("habit_id")
    val habitId: Int,

    @SerializedName("action")
    val action: String = "toggle"
)

data class CreateHabitRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("icon")
    val icon: String = "bolt",

    @SerializedName("frequency")
    val frequency: String = "daily",

    @SerializedName("action")
    val action: String = "create"
)

data class DeleteHabitRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("habit_id")
    val habitId: Int,

    @SerializedName("action")
    val action: String = "delete"
)

/**
 * Profile and user management data models.
 */
@Immutable
data class ProfileData(
    @SerializedName("id")
    val id: Int = 1,

    @SerializedName("name")
    val name: String = "Sri Sai",

    @SerializedName("email")
    val email: String = "sri.sai@example.com",

    @SerializedName("avatar")
    val avatar: String = "",

    @SerializedName("bio")
    val bio: String = "Deep work enthusiast & Computer Science researcher",

    @SerializedName("created_at")
    val createdAt: String = "",

    @SerializedName("member_since")
    val memberSince: String = "Member since October 2026",

    @SerializedName("total_focus_hours")
    val totalFocusHours: Float = 0f,

    @SerializedName("total_focus_minutes")
    val totalFocusMinutes: Int = 0,

    @SerializedName("completed_tasks_count")
    val completedTasksCount: Int = 0,

    @SerializedName("current_streak")
    val currentStreak: Int = 1,

    @SerializedName("daily_focus_goal_minutes")
    val dailyFocusGoalMinutes: Int = 240,

    @SerializedName("preferred_focus_duration")
    val preferredFocusDuration: Int = 45,

    @SerializedName("reminders_enabled")
    val remindersEnabled: Boolean = true
)

data class GetProfileResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: ProfileData? = null
)

data class UpdateProfileRequest(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("bio")
    val bio: String = "",

    @SerializedName("avatar")
    val avatar: String = "",

    @SerializedName("reminders_enabled")
    val remindersEnabled: Boolean? = null,

    @SerializedName("daily_focus_goal_minutes")
    val dailyFocusGoalMinutes: Int? = null,

    @SerializedName("preferred_focus_duration")
    val preferredFocusDuration: Int? = null
)

data class UpdateProfileResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: ProfileData? = null
)

@Immutable
data class DailyChartItem(
    @SerializedName("date") val date: String = "",
    @SerializedName("day") val day: String = "",
    @SerializedName("focus_minutes") val focusMinutes: Int = 0
)

@Immutable
data class TasksSummary(
    @SerializedName("total_tasks") val totalTasks: Int = 0,
    @SerializedName("pending_tasks") val pendingTasks: Int = 0,
    @SerializedName("completed_tasks") val completedTasks: Int = 0,
    @SerializedName("completion_rate_percent") val completionRatePercent: Float = 0f
)

@Immutable
data class InsightsData(
    @SerializedName("today_focus_minutes") val todayFocusMinutes: Int = 0,
    @SerializedName("week_focus_minutes") val weekFocusMinutes: Int = 0,
    @SerializedName("daily_focus_goal_minutes") val dailyFocusGoalMinutes: Int = 210,
    @SerializedName("daily_goal_progress_percent") val dailyGoalProgressPercent: Float = 0f,
    @SerializedName("current_streak_days") val currentStreakDays: Int = 0,
    @SerializedName("tasks_summary") val tasksSummary: TasksSummary = TasksSummary(),
    @SerializedName("daily_chart_data") val dailyChartData: List<DailyChartItem> = emptyList()
)

data class InsightsResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: InsightsData? = null
)

data class GrootRequest(
    @SerializedName("message") val message: String
)

data class GrootResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("response") val response: String? = null
)

@Immutable
data class AiRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("query_type") val queryType: String = "general",
    @SerializedName("prompt") val prompt: String? = null
)

@Immutable
data class AiResponse(
    @SerializedName("status") val status: String = "success",
    @SerializedName("insight") val insight: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class ResetStatsRequest(
    @SerializedName("user_id")
    val userId: Int
)

data class ResetStatsResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String = ""
)

