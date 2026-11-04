package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TrendingUp
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
fun AiReportScreen(
    onBack: () -> Unit = {},
    onViewRecommendations: () -> Unit = {},
    onViewReportDetails: () -> Unit = {}
) {
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
                isLoading = false
            }
            result.onFailure {
                isLoading = false
            }
        } catch (_: Exception) {
            isLoading = false
        }
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

    val weekHours = insightsData.weekFocusMinutes / 60
    val weekMins = insightsData.weekFocusMinutes % 60
    val totalSessions = insightsData.totalSessions
    val tasks = insightsData.tasksSummary
    val dist = insightsData.distractionsSummary
    val peakDay = insightsData.dailyChartData.maxByOrNull { it.focusMinutes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Your weekly AI report", null, onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isLoading) "Generating..." else "AI Analysis",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(currentDateRange, fontSize = 14.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Synthesized from your active focus sessions, task tracking, and distraction records.",
            fontSize = 12.sp,
            color = colors.textMuted
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (totalSessions == 0 && insightsData.weekFocusMinutes == 0) {
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
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = colors.primaryLight,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Not enough focus data yet. Complete more sessions this week to unlock AI recommendations.",
                        fontSize = 14.sp,
                        color = colors.textMuted,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Key Findings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = colors.primaryLight,
                        modifier = Modifier.padding(end = 8.dp).size(18.dp)
                    )
                    Text("Key findings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Peak productivity was observed around ${insightsData.bestWindow}. Your task completion rate reached ${tasks.completionRatePercent.toInt()}% this week.",
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text(
                            text = "Peak window: ${insightsData.bestWindow}",
                            color = Color(0xFF0D9488),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.background(colors.surfaceVariant, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text(
                            text = "Tasks: ${tasks.completionRatePercent.toInt()}%",
                            color = colors.textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Calculated from session intervals, completed tasks, and interval focus depth.",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Strengths
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.padding(end = 8.dp).size(18.dp)
                    )
                    Text("Strengths", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = buildString {
                        append("${weekHours}h ${weekMins}m across $totalSessions sessions. ")
                        append("You completed ${tasks.completedTasks} of ${tasks.totalTasks} tasks (${tasks.completionRatePercent.toInt()}%). ")
                        if (peakDay != null && peakDay.focusMinutes > 0) {
                            append("${peakDay.day} led with ${peakDay.focusMinutes} focused minutes.")
                        }
                    },
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Challenges
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.padding(end = 8.dp).size(18.dp)
                    )
                    Text("Challenges", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (dist.totalEvents > 0) {
                        "${dist.totalEvents} interruptions recorded this week; ${dist.topCategory} was the leading factor with ${String.format(Locale.getDefault(), "%.1f", dist.perSessionRate)} events per session."
                    } else {
                        "Zero logged interruptions this week. Watch out for late evening session fatigue."
                    },
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recommendations
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("Recommended changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Schedule demanding deep work at ${insightsData.bestWindow}, your best observed focus window. Silence ${dist.topCategory.lowercase()} alerts before starting.",
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Adaptive pattern generated by Groot AI based on historical session consistency.",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onViewRecommendations,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("View Recommendations", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            OutlinedButton(
                onClick = onViewReportDetails,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .bounceClick(),
                border = BorderStroke(1.dp, colors.border),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
            ) {
                Text("View report details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
