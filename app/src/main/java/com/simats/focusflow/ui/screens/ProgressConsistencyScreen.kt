package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.models.DailyChartItem
import com.simats.focusflow.data.models.InsightsData
import com.simats.focusflow.data.models.WeeklyActiveDayItem
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressConsistencyScreen(onBack: () -> Unit = {}) {
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

    val defaultDailyData = listOf(
        DailyChartItem(day = "Mon", focusMinutes = 0),
        DailyChartItem(day = "Tue", focusMinutes = 0),
        DailyChartItem(day = "Wed", focusMinutes = 0),
        DailyChartItem(day = "Thu", focusMinutes = 0),
        DailyChartItem(day = "Fri", focusMinutes = 0),
        DailyChartItem(day = "Sat", focusMinutes = 0),
        DailyChartItem(day = "Sun", focusMinutes = 0)
    )

    val dailyData = if (insightsData.dailyChartData.isNotEmpty()) insightsData.dailyChartData else defaultDailyData
    val maxVal = maxOf(60f, dailyData.maxOfOrNull { it.focusMinutes.toFloat() } ?: 60f)
    val totalWeekMins = dailyData.sumOf { it.focusMinutes }
    val activeDays = dailyData.count { it.focusMinutes > 0 }

    val monthlyData = if (insightsData.monthlySummary.weeklyActiveDays.isNotEmpty()) {
        insightsData.monthlySummary.weeklyActiveDays
    } else {
        listOf(
            WeeklyActiveDayItem("Week 1", 0),
            WeeklyActiveDayItem("Week 2", 0),
            WeeklyActiveDayItem("Week 3", 0),
            WeeklyActiveDayItem("Week 4", 0)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Progress & consistency", null, onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isLoading) "Loading..." else "Live records",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(currentDateRange, fontSize = 14.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Streak Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${insightsData.currentStreakDays} days",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text("current streak", fontSize = 14.sp, color = colors.textMuted)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${insightsData.longestStreakDays} days",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text("longest streak", fontSize = 14.sp, color = colors.textMuted)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Active day = any logged focus session. Consistency compounds over time.", fontSize = 14.sp, color = colors.textMuted)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weekly Rhythm Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Weekly rhythm · $activeDays / 7 active days", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
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
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Minutes focused · total ${totalWeekMins / 60}h ${totalWeekMins % 60}m",
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Monthly Consistency Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Monthly consistency · last 4 weeks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    monthlyData.forEach { wItem ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${wItem.activeDays}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (wItem.activeDays > 0) colors.primary else colors.border,
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(wItem.weekLabel, fontSize = 11.sp, color = colors.textMuted)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Active days / week · ${insightsData.monthlySummary.activeDaysCount} of 28 days",
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 4-Week Aggregate Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${insightsData.monthlySummary.fourWeekFocusHours.toInt()}h",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text("4-week total focus", fontSize = 14.sp, color = colors.textMuted)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${insightsData.monthlySummary.fourWeekTasksDone}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text("4-week tasks done", fontSize = 14.sp, color = colors.textMuted)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.padding(end = 12.dp).size(22.dp)
                    )
                    Column {
                        Text("Milestone · Consistent Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(
                            "$activeDays active days logged this week. Keep up the rhythm!",
                            fontSize = 14.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }
    }
}
