package com.simats.focusflow.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.models.DailyChartItem
import com.simats.focusflow.data.models.InsightsData
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.AmbientMeshGlowHeader
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ProgressConsistencyScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    var insightsData by remember { mutableStateOf(InsightsData()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDayIndex by remember { mutableIntStateOf(6) }

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

    val defaultDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dailyMap = insightsData.dailyChartData.associate { it.day to it.focusMinutes }
    val displayDays = defaultDays.map { day ->
        DailyChartItem(day = day, focusMinutes = dailyMap[day] ?: 0)
    }

    val maxDayMinutes = (displayDays.maxOfOrNull { it.focusMinutes } ?: 60).coerceAtLeast(60)
    val totalWeekMins = displayDays.sumOf { it.focusMinutes }
    val activeDaysCount = displayDays.count { it.focusMinutes > 0 }
    val currentStreak = insightsData.currentStreakDays
    val longestStreak = insightsData.longestStreakDays
    val totalDistractions = insightsData.distractionsSummary.totalEvents
    val consistencyRate = ((activeDaysCount / 7f) * 100).toInt()

    val efficiencyPercent = if (totalWeekMins + (totalDistractions * 15) > 0) {
        ((totalWeekMins.toFloat() / (totalWeekMins + (totalDistractions * 15)).toFloat()) * 100).toInt().coerceIn(10, 100)
    } else {
        100
    }

    val animatedEfficiency by animateFloatAsState(
        targetValue = efficiencyPercent / 100f,
        animationSpec = tween(durationMillis = 900),
        label = "efficiency_anim"
    )

    val neonBorder = Brush.horizontalGradient(
        listOf(
            Color(0xFF8B5CF6).copy(alpha = 0.45f),
            Color(0xFF06B6D4).copy(alpha = 0.45f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07060D))
    ) {
        AmbientMeshGlowHeader(height = 240.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
        CustomTopBar("Progress & Consistency", null, onBack)

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isLoading) "Loading..." else "Weekly Overview",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(currentDateRange, fontSize = 12.sp, color = colors.textMuted)
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, neonBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF97316).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFB923C),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CURRENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMuted,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "$currentStreak Days",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Active Streak",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, neonBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BEST",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMuted,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "$longestStreak Days",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Record Streak",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, neonBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "7-DAY CONSISTENCY TREND",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryLight,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val selectedDay = displayDays.getOrNull(selectedDayIndex)
                            Text(
                                text = if (selectedDay != null) "${selectedDay.day}: ${selectedDay.focusMinutes}m focused" else "$totalWeekMins mins total",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$consistencyRate% active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primaryLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val bottomPadding = 30.dp.toPx()
                            val topPadding = 16.dp.toPx()
                            val availableH = h - bottomPadding - topPadding
                            val spacing = w / (displayDays.size + 1)
                            val points = mutableListOf<Offset>()

                            for (gridLine in 0..3) {
                                val gy = topPadding + (availableH * (gridLine / 3f))
                                drawLine(
                                    color = Color(0xFF334155).copy(alpha = 0.25f),
                                    start = Offset(spacing * 0.5f, gy),
                                    end = Offset(w - spacing * 0.5f, gy),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            for (i in displayDays.indices) {
                                val item = displayDays[i]
                                val ratio = if (maxDayMinutes > 0) (item.focusMinutes.toFloat() / maxDayMinutes.toFloat()).coerceIn(0f, 1f) else 0f
                                val x = spacing * (i + 1)
                                val y = topPadding + availableH * (1f - ratio)
                                points.add(Offset(x, y))
                            }

                            if (points.size >= 2) {
                                val linePath = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 1 until points.size) {
                                        val p0 = points[i - 1]
                                        val p1 = points[i]
                                        val midX = (p0.x + p1.x) / 2f
                                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                                    }
                                }

                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    lineTo(points.last().x, topPadding + availableH)
                                    lineTo(points.first().x, topPadding + availableH)
                                    close()
                                }

                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF38BDF8).copy(alpha = 0.4f),
                                            Color(0xFF8B5CF6).copy(alpha = 0.12f),
                                            Color.Transparent
                                        ),
                                        startY = topPadding,
                                        endY = topPadding + availableH
                                    )
                                )

                                drawPath(
                                    path = linePath,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF38BDF8), Color(0xFF8B5CF6), Color(0xFF10B981))
                                    ),
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )

                                for (i in points.indices) {
                                    val p = points[i]
                                    val isSelected = i == selectedDayIndex
                                    drawCircle(
                                        color = if (isSelected) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color(0xFF38BDF8).copy(alpha = 0.3f),
                                        radius = if (isSelected) 9.dp.toPx() else 6.dp.toPx(),
                                        center = p
                                    )
                                    drawCircle(
                                        color = if (isSelected) Color(0xFFF59E0B) else Color.White,
                                        radius = if (isSelected) 4.5.dp.toPx() else 3.5.dp.toPx(),
                                        center = p
                                    )
                                }
                            }
                        }

                        val selectedDayItem = displayDays.getOrNull(selectedDayIndex)
                        if (selectedDayItem != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xF211101C),
                                border = BorderStroke(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f)))),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${selectedDayItem.day}: ${selectedDayItem.focusMinutes}m focus",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF1F1F5)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            displayDays.forEachIndexed { idx, dayItem ->
                                val isSelected = idx == selectedDayIndex
                                Text(
                                    text = dayItem.day,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF38BDF8) else colors.textMuted,
                                    modifier = Modifier
                                        .clickable { selectedDayIndex = idx }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, neonBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FOCUS VS DISTRACTION RATIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            letterSpacing = 1.2.sp
                        )

                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(
                            modifier = Modifier.size(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                val r = (size.minDimension - strokeWidth) / 2f

                                drawCircle(
                                    color = Color(0xFFEF4444).copy(alpha = 0.85f),
                                    radius = r,
                                    style = Stroke(width = strokeWidth)
                                )

                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6), Color(0xFF06B6D4))
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedEfficiency,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$efficiencyPercent%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Efficiency",
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF06B6D4))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Focus Sprint Time",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${totalWeekMins}m logged",
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Distraction Alerts",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "$totalDistractions events recorded",
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, neonBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BUILD CONSISTENCY TREND",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryLight,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "4-Week Habit Trajectory",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Monthly",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val weeklyData = insightsData.monthlySummary.weeklyActiveDays.ifEmpty {
                        listOf(
                            com.simats.focusflow.data.models.WeeklyActiveDayItem("Wk 1", 3),
                            com.simats.focusflow.data.models.WeeklyActiveDayItem("Wk 2", 4),
                            com.simats.focusflow.data.models.WeeklyActiveDayItem("Wk 3", 5),
                            com.simats.focusflow.data.models.WeeklyActiveDayItem("Wk 4", 6)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val bottomPad = 26.dp.toPx()
                            val topPad = 14.dp.toPx()
                            val availH = h - bottomPad - topPad
                            val spacing = w / (weeklyData.size + 1)
                            val points = mutableListOf<Offset>()

                            for (i in weeklyData.indices) {
                                val item = weeklyData[i]
                                val ratio = (item.activeDays.toFloat() / 7f).coerceIn(0f, 1f)
                                val x = spacing * (i + 1)
                                val y = topPad + availH * (1f - ratio)
                                points.add(Offset(x, y))
                            }

                            if (points.size >= 2) {
                                val linePath = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 1 until points.size) {
                                        val p0 = points[i - 1]
                                        val p1 = points[i]
                                        val midX = (p0.x + p1.x) / 2f
                                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                                    }
                                }

                                val areaFill = Path().apply {
                                    addPath(linePath)
                                    lineTo(points.last().x, topPad + availH)
                                    lineTo(points.first().x, topPad + availH)
                                    close()
                                }

                                drawPath(
                                    path = areaFill,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF10B981).copy(alpha = 0.35f),
                                            Color(0xFF06B6D4).copy(alpha = 0.1f),
                                            Color.Transparent
                                        ),
                                        startY = topPad,
                                        endY = topPad + availH
                                    )
                                )

                                drawPath(
                                    path = linePath,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF06B6D4), Color(0xFF10B981))
                                    ),
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )

                                for (p in points) {
                                    drawCircle(
                                        color = Color(0xFF10B981).copy(alpha = 0.35f),
                                        radius = 7.dp.toPx(),
                                        center = p
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 4.dp.toPx(),
                                        center = p
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            weeklyData.forEach { wk ->
                                Text(
                                    text = "${wk.weekLabel} (${wk.activeDays}d)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}
