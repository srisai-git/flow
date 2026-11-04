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
fun WeeklyReportDetailsScreen(
    onBack: () -> Unit = {},
    onViewRecommendations: () -> Unit = {}
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
    val hasEnoughData = totalSessions > 0 || totalMins > 0

    val dailyData = insightsData.dailyChartData
    val maxVal = maxOf(60f, dailyData.maxOfOrNull { it.focusMinutes.toFloat() } ?: 60f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Weekly report details", null, onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (hasEnoughData) "Live Report" else "No Data Yet",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(currentDateRange, fontSize = 14.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Detailed aggregate metrics compiled directly from your focus records and completed tasks.",
            fontSize = 14.sp,
            color = colors.textMuted
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (!hasEnoughData) {
            // Clean Empty-State UI (Requirement 1 & 2)
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
                        text = "Not enough focus data yet. Complete more sessions this week to unlock AI recommendations.",
                        fontSize = 14.sp,
                        color = colors.textMuted,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            // Focused Minutes Per Day Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Focused minutes / day", fontSize = 15.sp, color = colors.textMuted)
                        Text(
                            text = "${totalMins / 60}h ${totalMins % 60}m",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dailyData.forEach { item ->
                            val heightRatio = if (maxVal > 0f) (item.focusMinutes / maxVal).coerceIn(0.04f, 1f) else 0.04f
                            val dayLetter = if (item.day.isNotEmpty()) item.day.take(1) else "D"
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Text("${item.focusMinutes}", fontSize = 10.sp, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(heightRatio)
                                        .background(
                                            if (item.focusMinutes > 0) Color(0xFF4FD1C5) else colors.border,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(dayLetter, fontSize = 12.sp, color = colors.textMuted)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Weekly Summary Breakdown Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text("Weekly Summary Metrics", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Focus time", fontSize = 13.sp, color = colors.textMuted)
                            Text("Total", fontSize = 13.sp, color = colors.primaryLight)
                        }
                        Text("${totalMins / 60}h ${totalMins % 60}m", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Focus sessions", fontSize = 13.sp, color = colors.textMuted)
                            Text("Completed", fontSize = 13.sp, color = colors.primaryLight)
                        }
                        Text("$totalSessions sessions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Tasks completed", fontSize = 13.sp, color = colors.textMuted)
                            Text("${insightsData.tasksSummary.completionRatePercent.toInt()}% rate", fontSize = 13.sp, color = colors.primaryLight)
                        }
                        Text("${insightsData.tasksSummary.completedTasks} / ${insightsData.tasksSummary.totalTasks}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Interruptions", fontSize = 13.sp, color = colors.textMuted)
                            Text("Recorded events", fontSize = 13.sp, color = colors.primaryLight)
                        }
                        Text("${insightsData.distractionsSummary.totalEvents} events", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Peak Efficiency Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Peak Efficiency Window", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your observed peak focus interval occurred during ${insightsData.bestWindow}. Schedule critical tasks here for maximum cognitive momentum.",
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onViewRecommendations,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("View Recommendations", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}
