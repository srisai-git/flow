package com.simats.focusflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.models.InsightsData
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.AmbientMeshGlowHeader
import com.simats.focusflow.ui.components.InsightsSkeleton
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InsightsScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFocusAnalysis: () -> Unit = {},
    onNavigateToDistraction: () -> Unit = {},
    onNavigateToProgressConsistency: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    var selectedTab by remember { mutableStateOf("Week") }

    var insightsData by remember { mutableStateOf(InsightsData()) }
    var monthlyFocusMinutes by remember { mutableStateOf(0) }
    var weeklyDistractionsByDay by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
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
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val localDb = com.simats.focusflow.data.local.FocusSessionDatabase.getInstance(context)
                monthlyFocusMinutes = localDb.getCurrentMonthFocusMinutes(userId)
                val distList = localDb.getDistractions(userId, 100)
                val dayFmt = SimpleDateFormat("EEE", Locale.US)
                val parseFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val counts = mutableMapOf<String, Int>()
                distList.forEach { d ->
                    try {
                        val parsed = parseFmt.parse(d.createdAt)
                        if (parsed != null) {
                            val dayStr = dayFmt.format(parsed)
                            counts[dayStr] = (counts[dayStr] ?: 0) + 1
                        }
                    } catch (_: Exception) {}
                }
                weeklyDistractionsByDay = counts
            } catch (_: Exception) {}
        }
    }

    val currentDateRange = remember {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        val endStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val startStr = sdf.format(cal.time)
        "$startStr - $endStr"
    }

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            AmbientMeshGlowHeader(height = 240.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PERFORMANCE ANALYTICS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Productivity Insights",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.primary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = if (selectedTab == "Week") currentDateRange else SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                InsightsSkeleton()
            } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("Week", "Month").forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabBg by animateColorAsState(
                        targetValue = if (isSelected) colors.primary else Color.Transparent,
                        label = "tabBg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(tabBg)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val hairlineBorder = colors.cardBorderBrush

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, hairlineBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            val totalMins = if (selectedTab == "Week") {
                                insightsData.weekFocusMinutes
                            } else {
                                if (monthlyFocusMinutes > 0) monthlyFocusMinutes else insightsData.weekFocusMinutes * 4
                            }
                            val hours = totalMins / 60
                            val mins = totalMins % 60
                            Text(
                                text = "${hours}h ${mins}m",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (selectedTab == "Week") "recorded focus time this week" else "recorded focus time this month",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }

                        Text(
                            text = if (selectedTab == "Week") "Minutes / day" else "Hours / week",
                            fontSize = 12.sp,
                            color = colors.primaryLight,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (selectedTab == "Week") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Focus (min)", fontSize = 10.5.sp, color = colors.textMuted)
                            Spacer(modifier = Modifier.width(14.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Distractions", fontSize = 10.5.sp, color = colors.textMuted)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    val chartItems = insightsData.dailyChartData
                    val dataPoints = if (chartItems.isNotEmpty() && selectedTab == "Week") {
                        chartItems.map { it.focusMinutes }
                    } else if (selectedTab == "Week") {
                        listOf(0, 0, 0, 0, 0, 0, 0)
                    } else {
                        val avgWeek = insightsData.weekFocusMinutes
                        if (avgWeek > 0) listOf(avgWeek, avgWeek, avgWeek, avgWeek) else listOf(0, 0, 0, 0)
                    }

                    val labels = if (chartItems.isNotEmpty() && selectedTab == "Week") {
                        chartItems.map { it.day }
                    } else if (selectedTab == "Week") {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    } else {
                        listOf("W1", "W2", "W3", "W4")
                    }

                    val distractionPoints = labels.map { day ->
                        weeklyDistractionsByDay[day] ?: (weeklyDistractionsByDay[day.take(3)] ?: 0)
                    }

                    val maxFocus = (dataPoints.maxOrNull() ?: 60).coerceAtLeast(45).toFloat()
                    val maxDistract = (distractionPoints.maxOrNull() ?: 5).coerceAtLeast(5).toFloat()
                    val primaryColor = colors.primary
                    val primaryLight = colors.primaryLight
                    val surfaceVariant = colors.surfaceVariant

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (size.width <= 0f || size.height <= 0f) return@Canvas
                            val chartH = (size.height - 18.dp.toPx()).coerceAtLeast(10f)
                            if (selectedTab == "Week") {
                                val totalDays = labels.size
                                val groupWidth = 34.dp.toPx()
                                val totalGroupWidth = groupWidth * totalDays
                                val spacing = (size.width - totalGroupWidth) / (totalDays + 1)
                                val subBarWidth = (groupWidth - 4.dp.toPx()) / 2f

                                labels.forEachIndexed { index, _ ->
                                    val groupX = spacing + index * (groupWidth + spacing)
                                    val focusVal = dataPoints.getOrElse(index) { 0 }
                                    val distractVal = distractionPoints.getOrElse(index) { 0 }

                                    val focusRatio = if (focusVal > 0) (focusVal / maxFocus).coerceIn(0.06f, 1f) else 0f
                                    val focusH = chartH * focusRatio
                                    val focusY = chartH - focusH

                                    drawRoundRect(
                                        color = surfaceVariant,
                                        topLeft = Offset(groupX, 0f),
                                        size = Size(subBarWidth, chartH),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                    if (focusRatio > 0f) {
                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color(0xFF34D399), Color(0xFF10B981)),
                                                startY = focusY,
                                                endY = chartH
                                            ),
                                            topLeft = Offset(groupX, focusY),
                                            size = Size(subBarWidth, maxOf(focusH, 4.dp.toPx())),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }

                                    val distractX = groupX + subBarWidth + 4.dp.toPx()
                                    val distractRatio = if (distractVal > 0) (distractVal / maxDistract).coerceIn(0.06f, 1f) else 0f
                                    val distractH = chartH * distractRatio
                                    val distractY = chartH - distractH

                                    drawRoundRect(
                                        color = surfaceVariant,
                                        topLeft = Offset(distractX, 0f),
                                        size = Size(subBarWidth, chartH),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                    if (distractRatio > 0f) {
                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                                                startY = distractY,
                                                endY = chartH
                                            ),
                                            topLeft = Offset(distractX, distractY),
                                            size = Size(subBarWidth, maxOf(distractH, 4.dp.toPx())),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }
                                }
                            } else {
                                val barWidth = 42.dp.toPx()
                                val totalBars = dataPoints.size
                                val spacing = (size.width - (barWidth * totalBars)) / (totalBars + 1)

                                dataPoints.forEachIndexed { index, value ->
                                    val x = spacing + index * (barWidth + spacing)
                                    val ratio = if (value > 0) (value / maxFocus).coerceIn(0.05f, 1f) else 0f
                                    val barH = chartH * ratio
                                    val y = chartH - barH

                                    drawRoundRect(
                                        color = surfaceVariant,
                                        topLeft = Offset(x, 0f),
                                        size = Size(barWidth, chartH),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )

                                    if (ratio > 0f) {
                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(primaryLight, primaryColor),
                                                startY = y,
                                                endY = chartH
                                            ),
                                            topLeft = Offset(x, y),
                                            size = Size(barWidth, maxOf(barH, 4.dp.toPx())),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        labels.forEachIndexed { idx, lbl ->
                            Text(
                                text = lbl,
                                fontSize = 11.sp,
                                color = if (idx == labels.size - 1) colors.primaryLight else colors.textMuted,
                                fontWeight = if (idx == labels.size - 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, hairlineBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRODUCTIVITY INTELLIGENCE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = insightsData.bestWindow.ifBlank { "09:00 - 11:30" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Peak Window", fontSize = 11.sp, color = colors.textMuted)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = insightsData.distractionsSummary.topCategory.ifBlank { "Social Media" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Top Distraction", fontSize = 11.sp, color = colors.textMuted)
                        }

                        Column(modifier = Modifier.weight(0.8f)) {
                            Text(
                                text = "${insightsData.dailyGoalProgressPercent.toInt()}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Goal Attained", fontSize = 11.sp, color = colors.textMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val topDistract = insightsData.distractionsSummary.topCategory.ifBlank { "Social Media" }
                    val peakWin = insightsData.bestWindow.ifBlank { "09:00 - 11:30 AM" }
                    val goalPct = insightsData.dailyGoalProgressPercent.toInt()
                    val recommendationText = when {
                        goalPct >= 100 -> "Daily goal reached. Preserve momentum during $peakWin and keep $topDistract blocked."
                        goalPct >= 50 -> "On track for today. Schedule your next deep session during $peakWin to hit 100%."
                        else -> "Target your next focus sprint during $peakWin. Enable Strict Focus Mode to mitigate $topDistract."
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recommendationText,
                                fontSize = 12.sp,
                                color = colors.textPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.border, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${insightsData.currentStreakDays}d",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text("Current Streak", fontSize = 11.sp, color = colors.textMuted)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${insightsData.todayFocusMinutes}m",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text("Today's Focus", fontSize = 11.sp, color = colors.textMuted)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${insightsData.tasksSummary.completedTasks}/${insightsData.tasksSummary.totalTasks}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Text("Tasks Completed", fontSize = 11.sp, color = colors.textMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, hairlineBorder)
            ) {
                Column {
                    InsightNavRow(
                        icon = Icons.Default.HourglassBottom,
                        title = "Focus Time Analysis",
                        onClick = onNavigateToFocusAnalysis
                    )
                    HorizontalDivider(color = colors.border, thickness = 1.dp)

                    InsightNavRow(
                        icon = Icons.Default.PieChart,
                        title = "Distraction Analysis",
                        onClick = onNavigateToDistraction
                    )
                    HorizontalDivider(color = colors.border, thickness = 1.dp)



                    InsightNavRow(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        title = "Progress & Consistency",
                        onClick = onNavigateToProgressConsistency
                    )
                }
            }
            }

            Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun InsightNavRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isSparkle: Boolean = false
) {
    val colors = FocusFlowTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSparkle) colors.primaryLight else colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InsightsScreenPreview() {
    InsightsScreen()
}
