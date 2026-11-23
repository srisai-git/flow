package com.simats.focusflow.data.api

import com.simats.focusflow.data.models.AiRequest
import com.simats.focusflow.data.models.AiResponse
import com.simats.focusflow.data.models.GrootRequest
import com.simats.focusflow.data.models.GrootResponse
import com.simats.focusflow.data.models.TaskBreakdownRequest
import com.simats.focusflow.data.models.TaskBreakdownResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GrootApiService {

    @POST("chat")
    suspend fun chat(@Body request: AiRequest): Response<AiResponse>

    @POST("chat")
    suspend fun askGroot(@Body request: GrootRequest): Response<GrootResponse>

    @POST("breakdown_task")
    suspend fun breakdownTask(@Body request: TaskBreakdownRequest): Response<TaskBreakdownResponse>
}
