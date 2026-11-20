package com.simats.focusflow.data.api

import com.simats.focusflow.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query



interface FlowApiService {

    @GET("get_dashboard.php")
    suspend fun getDashboard(@Query("user_id") userId: Int): Response<DashboardResponse>

    @GET("tasks.php")
    suspend fun getTasks(@Query("user_id") userId: Int): Response<TasksResponse>

    @POST("tasks.php")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<TaskActionResponse>

    @POST("tasks.php")
    suspend fun updateTask(@Body request: UpdateTaskRequest): Response<TaskActionResponse>

    @POST("tasks.php")
    suspend fun toggleTask(@Body request: ToggleTaskRequest): Response<TaskActionResponse>

    @POST("tasks.php")
    suspend fun deleteTask(@Body request: DeleteTaskRequest): Response<TaskActionResponse>

    @POST("save_session.php")
    suspend fun saveSession(@Body request: LogSessionRequest): Response<LogSessionResponse>

    @POST("log_session.php")
    suspend fun logSession(@Body request: LogSessionRequest): Response<LogSessionResponse>

    @GET("log_session.php")
    suspend fun getRecentSessions(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int = 20
    ): Response<FocusSessionsResponse>

    @GET("habits.php")
    suspend fun getHabits(@Query("user_id") userId: Int): Response<HabitsResponse>

    @POST("habits.php")
    suspend fun toggleHabit(@Body request: ToggleHabitRequest): Response<HabitActionResponse>

    @POST("habits.php")
    suspend fun createHabit(@Body request: CreateHabitRequest): Response<HabitActionResponse>

    @POST("habits.php")
    suspend fun deleteHabit(@Body request: DeleteHabitRequest): Response<HabitActionResponse>

    @GET("streaks.php")
    suspend fun getStreaks(@Query("user_id") userId: Int): Response<StreakResponse>

    @POST("streaks.php")
    suspend fun updateStreak(
        @Query("user_id") userId: Int,
        @Query("streak_count") streakCount: Int
    ): Response<StreakResponse>

    @GET("get_profile.php")
    suspend fun getProfile(@Query("user_id") userId: Int): Response<GetProfileResponse>

    @POST("update_profile.php")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>

    @POST("reset_stats.php")
    suspend fun resetStats(@Body request: ResetStatsRequest): Response<ResetStatsResponse>

    @GET("insights/get_insights.php")
    suspend fun getInsights(@Query("user_id") userId: Int): Response<InsightsResponse>

    @POST("chat")
    suspend fun askGroot(@Body request: GrootRequest): Response<GrootResponse>

    @POST("chat")
    suspend fun getAiInsights(@Body request: AiRequest): Response<AiResponse>

    @POST("distractions.php")
    suspend fun logDistraction(@Body request: LogDistractionRequest): Response<LogDistractionResponse>

    @GET("distractions.php")
    suspend fun getDistractions(@Query("user_id") userId: Int): Response<DistractionsResponse>

    @POST("breakdown_task")
    suspend fun breakdownTask(@Body request: TaskBreakdownRequest): Response<TaskBreakdownResponse>

    @POST("update_token.php")
    suspend fun updateFcmToken(@Body request: UpdateTokenRequest): Response<UpdateTokenResponse>
}

