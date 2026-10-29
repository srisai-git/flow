package com.simats.focusflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
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
    onNavigateToAiReport: () -> Unit = {},
    onNavigateToProgressConsistency: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    var selectedTab by remember { mutableStateOf("Week") }

    var insightsData by remember { mutableStateOf(InsightsData()) }
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
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // HEADER
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

            // Date Range Pill
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

            // Segmented Switcher (Week / Month)
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

            // Bar Chart Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            val totalMins = if (selectedTab == "Week") insightsData.weekFocusMinutes else insightsData.weekFocusMinutes * 4
                            val hours = totalMins / 60
                            val mins = totalMins % 60
                            Text(
                                text = "${hours}h ${mins}m",
                                fontSize = 30.sp,
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Daily / Weekly Canvas Bar Chart (Strictly Authentic Database Records)
                    val chartItems = insightsData.dailyChartData
                    val dataPoints = if (chartItems.isNotEmpty() && selectedTab == "Week") {
                        chartItems.map { it.focusMinutes }
                    } else if (selectedTab == "Week") {
                        listOf(0, 0, 0, 0, 0, 0, 0)
                    } else {
                        // Monthly estimate based on verified session minutes
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

                    val maxVal = (dataPoints.maxOrNull() ?: 60).coerceAtLeast(60).toFloat()
                    val primaryColor = colors.primary
                    val primaryLight = colors.primaryLight
                    val surfaceVariant = colors.surfaceVariant

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = if (selectedTab == "Week") 22.dp.toPx() else 42.dp.toPx()
                            val totalBars = dataPoints.size
                            val spacing = (size.width - (barWidth * totalBars)) / (totalBars + 1)
                            val chartH = size.height - 24.dp.toPx()

                            dataPoints.forEachIndexed { index, value ->
                                val x = spacing + index * (barWidth + spacing)
                                val ratio = if (value > 0) (value / maxVal).coerceIn(0.05f, 1f) else 0f
                                val barH = chartH * ratio
                                val y = chartH - barH

                                // Track background
                                drawRoundRect(
                                    color = surfaceVariant,
                                    topLeft = Offset(x, 0f),
                                    size = Size(barWidth, chartH),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )

                                // Active Gradient Bar (only if authenticated user has recorded focus minutes)
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

            // 4 Stats Matrix Cards
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${insightsData.currentStreakDays}d",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text("Current Focus Streak", fontSize = 11.sp, color = colors.textMuted)

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "${insightsData.tasksSummary.completionRatePercent.toInt()}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Tasks (${insightsData.tasksSummary.completedTasks}/${insightsData.tasksSummary.totalTasks} Done)",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${insightsData.todayFocusMinutes}m",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text("Today's Focus Time", fontSize = 11.sp, color = colors.textMuted)

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "${insightsData.dailyGoalProgressPercent.toInt()}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryLight
                            )
                            Text("Daily Goal Attained", fontSize = 11.sp, color = colors.textMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Detailed Links Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column {
                    InsightNavRow(
                        icon = Icons.Default.Timer,
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
                        icon = Icons.Default.AutoAwesome,
                        title = "AI Productivity Report",
                        onClick = onNavigateToAiReport,
                        isSparkle = true
                    )
                    HorizontalDivider(color = colors.border, thickness = 1.dp)

                    InsightNavRow(
                        icon = Icons.Default.TrendingUp,
                        title = "Progress & Consistency",
                        onClick = onNavigateToProgressConsistency
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
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
