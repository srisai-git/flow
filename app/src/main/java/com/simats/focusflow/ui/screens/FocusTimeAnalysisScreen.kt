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
import androidx.compose.material.icons.filled.TrendingUp
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
import com.simats.focusflow.data.models.FocusSessionRecord
import com.simats.focusflow.data.models.InsightsData
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

private data class DayTimeBlock(
    val name: String,
    val timeRange: String,
    val minutes: Int
)

private data class MonthWeekBreakdown(
    val weekLabel: String,
    val dateRange: String,
    val minutes: Int,
    val isCurrentWeek: Boolean
)

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
    var localTodaySessions by remember { mutableStateOf<List<FocusSessionRecord>>(emptyList()) }
    var localMonthWeeks by remember { mutableStateOf<List<Int>>(listOf(0, 0, 0, 0)) }
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
                localTodaySessions = db.getTodaySessions(userId)
                localMonthWeeks = db.getMonthWeeklyMinutes(userId)

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

    val currentMonthDateStr = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
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

    val effectiveMonthMinutes = remember(localMonthMinutes, totalWeekMinutes, insightsData) {
        maxOf(localMonthMinutes, totalWeekMinutes, (insightsData.monthlySummary.fourWeekFocusHours * 60).toInt())
    }

    val selectedMinutes = when (selectedPeriod) {
        "Today" -> effectiveTodayMinutes
        "This Month" -> effectiveMonthMinutes
        else -> totalWeekMinutes
    }
    val selectedHours = selectedMinutes / 60
    val selectedRemainingMins = selectedMinutes % 60
    val dailyAvgMins = totalWeekMinutes / 7

    // Day View Time-of-Day Blocks (Morning, Afternoon, Evening, Night)
    val dayTimeBlocks = remember(localTodaySessions, effectiveTodayMinutes) {
        var morning = 0
        var afternoon = 0
        var evening = 0
        var night = 0

        for (session in localTodaySessions) {
            val timeStr = session.startedAt.ifBlank { session.completedAt }
            val hour = try {
                if (timeStr.length >= 13) {
                    timeStr.substring(11, 13).toInt()
                } else {
                    12
                }
            } catch (_: Exception) {
                12
            }
            when (hour) {
                in 6..11 -> morning += session.durationMinutes
                in 12..16 -> afternoon += session.durationMinutes
                in 17..20 -> evening += session.durationMinutes
                else -> night += session.durationMinutes
            }
        }

        if (morning + afternoon + evening + night == 0 && effectiveTodayMinutes > 0) {
            val curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            when (curHour) {
                in 6..11 -> morning = effectiveTodayMinutes
                in 12..16 -> afternoon = effectiveTodayMinutes
                in 17..20 -> evening = effectiveTodayMinutes
                else -> night = effectiveTodayMinutes
            }
        }

        listOf(
            DayTimeBlock("Morning", "6 AM – 12 PM", morning),
            DayTimeBlock("Afternoon", "12 PM – 5 PM", afternoon),
            DayTimeBlock("Evening", "5 PM – 9 PM", evening),
            DayTimeBlock("Night", "9 PM – 6 AM", night)
        )
    }

    val peakDayBlock = remember(dayTimeBlocks) {
        val maxMins = dayTimeBlocks.maxOfOrNull { it.minutes } ?: 0
        if (maxMins > 0) dayTimeBlocks.firstOrNull { it.minutes == maxMins } else null
    }

    // Month View 4-Week Breakdown
    val monthWeekItems = remember(localMonthWeeks, effectiveMonthMinutes) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val maxDaysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentWeekIdx = when {
            currentDay <= 7 -> 0
            currentDay <= 14 -> 1
            currentDay <= 21 -> 2
            else -> 3
        }

        val weeksMins = localMonthWeeks.toMutableList()
        while (weeksMins.size < 4) weeksMins.add(0)

        if (weeksMins.sum() == 0 && effectiveMonthMinutes > 0) {
            weeksMins[currentWeekIdx] = effectiveMonthMinutes
        }

        listOf(
            MonthWeekBreakdown("Week 1", "Day 1 – 7", weeksMins[0], currentWeekIdx == 0),
            MonthWeekBreakdown("Week 2", "Day 8 – 14", weeksMins[1], currentWeekIdx == 1),
            MonthWeekBreakdown("Week 3", "Day 15 – 21", weeksMins[2], currentWeekIdx == 2),
            MonthWeekBreakdown("Week 4", "Day 22 – $maxDaysInMonth", weeksMins[3], currentWeekIdx == 3)
        )
    }

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

        // Period Toggle: Today vs This Week vs This Month
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

        // Period header badge & date range
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val badgeText = when (selectedPeriod) {
                    "Today" -> if (isLoading) "Loading..." else "Today's Summary"
                    "This Month" -> if (isLoading) "Loading..." else "Monthly Summary"
                    else -> if (isLoading) "Loading..." else "7-Day Summary"
                }
                Text(
                    text = badgeText,
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            val dateLabel = when (selectedPeriod) {
                "Today" -> todayDateStr
                "This Month" -> currentMonthDateStr
                else -> currentDateRange
            }
            Text(
                text = dateLabel,
                fontSize = 13.sp,
                color = colors.textMuted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Metrics Overview Box
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
                    val primaryStat = if (selectedHours > 0) {
                        "${selectedHours}h ${selectedRemainingMins}m"
                    } else {
                        "${selectedRemainingMins}m"
                    }
                    val primaryLabel = when (selectedPeriod) {
                        "Today" -> "focused today"
                        "This Month" -> "month-to-date focused"
                        else -> "total focused"
                    }
                    Text(
                        text = primaryStat,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = primaryLabel,
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    val secondaryStat = when (selectedPeriod) {
                        "Today" -> "${maxOf(todaySessionsCount, if (effectiveTodayMinutes > 0) 1 else 0)}"
                        "This Month" -> "${selectedMinutes / 4}m"
                        else -> "${dailyAvgMins}m"
                    }
                    val secondaryLabel = when (selectedPeriod) {
                        "Today" -> "sessions completed"
                        "This Month" -> "weekly average"
                        else -> "daily average"
                    }
                    Text(
                        text = secondaryStat,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = secondaryLabel,
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedMinutes == 0) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = colors.primaryLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "No focus logged for this period yet",
                            fontSize = 14.sp,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Complete a focus session to see your live insights.",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }

        when (selectedPeriod) {
            "Today" -> {
                // Day View: Time-of-Day Blocks with Peak Focus Window
                if (peakDayBlock != null && peakDayBlock.minutes > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(colors.primary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "PEAK FOCUS WINDOW",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryLight,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(colors.primary, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = peakDayBlock.name.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Most productive between ${peakDayBlock.timeRange} (${peakDayBlock.minutes} min logged)",
                                    fontSize = 13.sp,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "TODAY'S TIME-OF-DAY BREAKDOWN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Focus distribution across day segments",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        dayTimeBlocks.forEach { block ->
                            val isPeak = (peakDayBlock != null && block.name == peakDayBlock.name && block.minutes > 0)
                            val ratio = if (effectiveTodayMinutes > 0) {
                                (block.minutes.toFloat() / effectiveTodayMinutes.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isPeak) colors.primary.copy(alpha = 0.08f) else colors.background,
                                border = BorderStroke(
                                    if (isPeak) 1.5.dp else 1.dp,
                                    if (isPeak) colors.primary else colors.border
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = block.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = block.timeRange,
                                                fontSize = 12.sp,
                                                color = colors.textMuted
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isPeak) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(colors.primary, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "PEAK",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(
                                                text = "${block.minutes}m",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (block.minutes > 0) colors.primaryLight else colors.textMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.border.copy(alpha = 0.4f))
                                    ) {
                                        if (ratio > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(ratio)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isPeak) colors.primary else colors.primary.copy(alpha = 0.65f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "This Month" -> {
                // Month View: 4-week cumulative comparison bars
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "MONTH-TO-DATE BREAKDOWN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryLight,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "4-week comparative growth",
                                    fontSize = 13.sp,
                                    color = colors.textMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Avg ${selectedMinutes / 4}m / wk",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val maxWeekMinutes = maxOf(60f, (monthWeekItems.maxOfOrNull { it.minutes.toFloat() } ?: 60f))

                        monthWeekItems.forEach { item ->
                            val ratio = if (maxWeekMinutes > 0f) {
                                (item.minutes / maxWeekMinutes).coerceIn(0.04f, 1f)
                            } else 0.04f
                            val percentOfTotal = if (selectedMinutes > 0) {
                                (item.minutes * 100 / selectedMinutes)
                            } else 0

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (item.isCurrentWeek) colors.primary.copy(alpha = 0.08f) else colors.background,
                                border = BorderStroke(
                                    if (item.isCurrentWeek) 1.5.dp else 1.dp,
                                    if (item.isCurrentWeek) colors.primary else colors.border
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.weekLabel,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.dateRange,
                                                fontSize = 12.sp,
                                                color = colors.textMuted
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (item.isCurrentWeek) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(colors.primary, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "CURRENT",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(
                                                text = if (item.minutes >= 60) "${item.minutes / 60}h ${item.minutes % 60}m" else "${item.minutes}m",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.minutes > 0) colors.primaryLight else colors.textMuted
                                            )
                                            if (selectedMinutes > 0) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "($percentOfTotal%)",
                                                    fontSize = 12.sp,
                                                    color = colors.textMuted
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(colors.border.copy(alpha = 0.4f))
                                    ) {
                                        if (item.minutes > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(ratio)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(
                                                        if (item.isCurrentWeek) colors.primary else colors.primary.copy(alpha = 0.65f)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                // Week View: 7-Day Bar Chart with Daily Goal Line (60m threshold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
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
                                    text = "Daily Goal: 60m threshold",
                                    fontSize = 13.sp,
                                    color = colors.textMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${displayDays.count { it.focusMinutes >= 60 }}/7 Met Goal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val weekChartMax = maxOf(70f, (displayDays.maxOfOrNull { it.focusMinutes.toFloat() } ?: 60f) * 1.15f)
                        val goalRatio = (60f / weekChartMax).coerceIn(0.1f, 0.95f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            // Horizontal Goal Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomStart)
                                    .padding(bottom = (180.dp * goalRatio))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(1.dp)
                                            .background(colors.primary.copy(alpha = 0.45f))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Goal 60m",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryLight
                                    )
                                }
                            }

                            // Bars Row
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                displayDays.forEach { item ->
                                    val isToday = item.date == todayDateKey
                                    val metGoal = item.focusMinutes >= 60
                                    val heightRatio = if (weekChartMax > 0f) {
                                        (item.focusMinutes / weekChartMax).coerceIn(0.04f, 1f)
                                    } else 0.04f

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.fillMaxHeight()
                                    ) {
                                        Text(
                                            text = if (item.focusMinutes > 0) "${item.focusMinutes}m" else "-",
                                            fontSize = 11.sp,
                                            fontWeight = if (metGoal || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (metGoal) colors.primaryLight else if (item.focusMinutes > 0) colors.textPrimary else colors.textMuted
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .fillMaxHeight(heightRatio)
                                                .background(
                                                    if (item.focusMinutes > 0) {
                                                        if (metGoal) colors.primary else colors.primary.copy(alpha = 0.65f)
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.background, RoundedCornerShape(10.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Focus Goal",
                                fontSize = 13.sp,
                                color = colors.textMuted
                            )
                            Text(
                                text = "${totalWeekMinutes}m / 420m (${(totalWeekMinutes * 100 / 420).coerceAtMost(100)}%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
