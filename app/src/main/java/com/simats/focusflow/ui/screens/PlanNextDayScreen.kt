package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.models.InsightsData
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlanNextDayScreen(onBack: () -> Unit = {}, onReviewTasks: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    var insightsData by remember { mutableStateOf(InsightsData()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val sessionManager = SessionManager.getInstance(context)
            val session = sessionManager.userSessionFlow.first()
            val userId = session.userId ?: 1
            val repo = ProductivityRepository()
            val result = repo.getInsights(userId)
            result.onSuccess { data ->
                insightsData = data
            }
        } catch (_: Exception) {}
        isLoading = false
    }

    val currentDateRange = remember {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        val endStr = sdf.format(cal.time)
        val yearStr = yearFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val startStr = sdf.format(cal.time)
        "$startStr – $endStr $yearStr"
    }

    val totalSessions = insightsData.totalSessions
    val totalMins = insightsData.weekFocusMinutes
    val tasks = insightsData.tasksSummary
    val hasEnoughData = totalSessions > 0 || totalMins > 0 || tasks.totalTasks > 0
    val estimatedLikelihood = if (tasks.completionRatePercent > 0f) tasks.completionRatePercent.toInt() else 75

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Plan your next productive day", null, onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (hasEnoughData) "Productivity Plan" else "No Data Yet",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(currentDateRange, fontSize = 14.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        if (!hasEnoughData) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.primaryLight,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No records found yet. Complete a focus session to see your stats.",
                        fontSize = 14.sp,
                        color = colors.textMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Likelihood card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text("Estimated completion likelihood", fontSize = 15.sp, color = colors.textMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("$estimatedLikelihood%", fontSize = 54.sp, fontWeight = FontWeight.Bold, color = colors.primaryLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Projected from your recent completion consistency of ${tasks.completedTasks} completed tasks.",
                        fontSize = 13.sp,
                        color = colors.textMuted,
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Inputs card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text("Baseline Historical Metrics", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Weekly focus time", fontSize = 14.sp, color = colors.textMuted)
                        Text("${totalMins / 60}h ${totalMins % 60}m", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    }
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Completed tasks", fontSize = 14.sp, color = colors.textMuted)
                        Text("${tasks.completedTasks} / ${tasks.totalTasks}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    }
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Peak focus window", fontSize = 14.sp, color = colors.textMuted)
                        Text(insightsData.bestWindow, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryLight)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onReviewTasks,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Review Tasks", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}
