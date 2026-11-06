package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.DailyChartItem
import com.simats.focusflow.data.models.InsightsData
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FocusTimeAnalysisScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors

    var selectedPeriod by remember { mutableStateOf("This Week") }
    var insightsData by remember { mutableStateOf(InsightsData()) }
    var localDailyMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var localTodayMinutes by remember { mutableIntStateOf(0) }
    var localMonthMinutes by remember { mutableIntStateOf(0) }
    var localTotalMinutes by remember { mutableIntStateOf(0) }
    var todaySessionsCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    suspend fun refreshData() {
        try {
            val sessionManager = SessionManager.getInstance(context)
            val session = sessionManager.userSessionFlow.first()
            val userId = session.userId ?: 1
            val db = FocusSessionDatabase.getInstance(context)

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                localDailyMap = db.getDailyMinutesPast7Days(userId)
                localTodayMinutes = db.getTodayFocusMinutes(userId)
                localMonthMinutes = db.getCurrentMonthFocusMinutes(userId)
                localTotalMinutes = db.getTotalFocusMinutes(userId)
                val todayDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                todaySessionsCount = db.getRecentSessions(userId, 50)
                    .count { it.completedAt.startsWith(todayDateKey) || it.startedAt.startsWith(todayDateKey) }
            }

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

    LaunchedEffect(Unit) {
        refreshData()
        FocusSessionDatabase.sessionTableChangedFlow.collect {
            refreshData()
        }
    }

    val dateSdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val daySdf = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val todayDateKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

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

    val todayDateStr = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    // Merge remote daily chart data with local database
    val remoteDailyMap = remember(insightsData) {
        val map = mutableMapOf<String, Int>()
        insightsData.dailyChartData.forEach { item ->
            if (item.date.isNotBlank()) {
                map[item.date] = item.focusMinutes
            } else if (item.day.isNotBlank()) {
                map[item.day] = item.focusMinutes
            }
        }
        map
    }

    val displayDays = remember(localDailyMap, remoteDailyMap) {
        val list = mutableListOf<DailyChartItem>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateKey = dateSdf.format(dayCal.time)
            val dayLabel = daySdf.format(dayCal.time)
            val localMins = localDailyMap[dateKey] ?: 0
            val remoteMins = remoteDailyMap[dateKey] ?: remoteDailyMap[dayLabel] ?: 0
            val combinedMins = maxOf(localMins, remoteMins)
            list.add(DailyChartItem(date = dateKey, day = dayLabel, focusMinutes = combinedMins))
        }
        list
    }

    val totalWeekMinutes = remember(displayDays, insightsData) {
        val fromChart = displayDays.sumOf { it.focusMinutes }
        maxOf(fromChart, insightsData.weekFocusMinutes)
    }

    val effectiveTodayMinutes = remember(localTodayMinutes, localDailyMap, insightsData) {
        maxOf(localTodayMinutes, localDailyMap[todayDateKey] ?: 0, insightsData.todayFocusMinutes)
    }

    val selectedMinutes = when (selectedPeriod) {
        "Today" -> effectiveTodayMinutes
        "This Month" -> maxOf(localMonthMinutes, totalWeekMinutes)
        else -> totalWeekMinutes
    }
    val selectedHours = selectedMinutes / 60
    val selectedRemainingMins = selectedMinutes % 60
    val dailyAvgMins = totalWeekMinutes / 7
    val maxBarVal = maxOf(60f, displayDays.maxOfOrNull { it.focusMinutes.toFloat() } ?: 60f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Focus time analysis", null, onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // Period Toggle: Today vs This Week
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(12.dp))
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            listOf("Today", "This Week", "This Month").forEach { option ->
                val isSelected = option == selectedPeriod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) colors.primary else Color.Transparent)
                        .clickable { selectedPeriod = option }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) Color.White else colors.textMuted,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isLoading) "Loading..." else if (selectedPeriod == "Today") "Today's Summary" else "7-Day Summary",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (selectedPeriod == "Today") todayDateStr else currentDateRange,
                fontSize = 13.sp,
                color = colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedMinutes == 0) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = colors.primaryLight,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No data logged for this period.",
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Complete a focus session to see your stats.",
                        fontSize = 13.sp,
                        color = colors.textMuted,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Metrics Overview (Total Hours & Daily Average / Sessions Count)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedHours > 0) "${selectedHours}h ${selectedRemainingMins}m" else "${selectedRemainingMins}m",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedPeriod == "Today") "focused today" else "total focused",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedPeriod == "Today") "${maxOf(1, todaySessionsCount)}" else "${dailyAvgMins}m",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedPeriod == "Today") "sessions completed" else "daily average",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clean 7-Day Focus Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "PAST 7 DAYS FOCUS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Minutes logged per day",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        displayDays.forEach { item ->
                            val isToday = item.date == todayDateKey
                            val heightRatio = if (maxBarVal > 0f) {
                                (item.focusMinutes / maxBarVal).coerceIn(0.06f, 1f)
                            } else 0.06f

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Text(
                                    text = if (item.focusMinutes > 0) "${item.focusMinutes}m" else "-",
                                    fontSize = 11.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (item.focusMinutes > 0) colors.textPrimary else colors.textMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .fillMaxHeight(heightRatio)
                                        .background(
                                            if (item.focusMinutes > 0) {
                                                if (isToday) colors.primary else colors.primary.copy(alpha = 0.65f)
                                            } else {
                                                colors.border.copy(alpha = 0.4f)
                                            },
                                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.day,
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isToday) colors.primaryLight else if (item.focusMinutes > 0) colors.textPrimary else colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
