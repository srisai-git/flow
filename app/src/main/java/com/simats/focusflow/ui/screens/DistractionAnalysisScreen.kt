package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.models.DistractionCategoryItem
import com.simats.focusflow.data.models.InsightsData
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DistractionAnalysisScreen(onBack: () -> Unit = {}) {
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

    val palette = listOf(
        Color(0xFF7158E2),
        Color(0xFF4FD1C5),
        Color(0xFFB45309),
        Color(0xFF10B981),
        Color(0xFF6B7280)
    )

    val distSummary = insightsData.distractionsSummary
    val categories = if (distSummary.categories.isNotEmpty()) {
        distSummary.categories
    } else {
        listOf(
            DistractionCategoryItem("Social Media", 0, 0),
            DistractionCategoryItem("Notifications", 0, 0),
            DistractionCategoryItem("Browsing", 0, 0),
            DistractionCategoryItem("People", 0, 0),
            DistractionCategoryItem("Other", 0, 0)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Distraction analysis", null, onBack)
        
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
        
        // Overview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${distSummary.totalEvents}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text("interruptions this week", fontSize = 14.sp, color = colors.textMuted)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        String.format(Locale.getDefault(), "%.1f", distSummary.perSessionRate),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text("per recorded session", fontSize = 14.sp, color = colors.textMuted)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Donut Chart Card
        val topCategoryName = distSummary.topCategory
        val topCategoryPercent = categories.firstOrNull { it.category.equals(topCategoryName, ignoreCase = true) }?.percent
            ?: categories.firstOrNull()?.percent ?: 0

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(16.dp.toPx())
                        if (distSummary.totalEvents == 0) {
                            drawCircle(colors.border, style = stroke)
                        } else {
                            var currentAngle = -90f
                            categories.forEachIndexed { index, cat ->
                                val sweep = (cat.percent / 100f) * 360f
                                if (sweep > 0f) {
                                    drawArc(
                                        color = palette[index % palette.size],
                                        startAngle = currentAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = stroke
                                    )
                                    currentAngle += sweep
                                }
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${distSummary.totalEvents}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text("logged events", fontSize = 10.sp, color = colors.textMuted)
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text("Know the pattern.", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (distSummary.totalEvents > 0) {
                            "$topCategoryName accounts for $topCategoryPercent% of logged interruptions."
                        } else {
                            "Zero interruptions recorded this week. Solid focus discipline!"
                        },
                        fontSize = 14.sp,
                        color = colors.textMuted
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // List Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                categories.forEachIndexed { index, cat ->
                    LegendRow(
                        color = palette[index % palette.size],
                        title = cat.category,
                        value = "${cat.count} · ${cat.percent}%",
                        textColor = colors.textPrimary,
                        mutedColor = colors.textMuted
                    )
                    if (index < categories.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Percentages calculated across weekly focus sessions", fontSize = 12.sp, color = colors.textMuted)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFE1FAF9), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${distSummary.totalEvents} total",
                    color = Color(0xFF0D9488),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Logged interruptions this week", fontSize = 14.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(20.dp).padding(top = 4.dp)) {
                    drawRoundRect(colors.primaryLight, size = Size(size.width * 0.6f, size.height * 0.8f), style = Stroke(2.dp.toPx()))
                    drawCircle(colors.primaryLight, radius = 1.dp.toPx(), center = Offset(size.width * 0.3f, size.height * 0.7f))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (distSummary.totalEvents > 0) {
                        "Most frequent: $topCategoryName. Try silencing ${topCategoryName.lowercase()} alerts before your next focus block."
                    } else {
                        "Great job keeping distractions minimal. Continue setting clear session intentions."
                    },
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
fun LegendRow(
    color: Color,
    title: String,
    value: String,
    textColor: Color = Color(0xFF1E1E24),
    mutedColor: Color = Color(0xFF6B7280)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, color = textColor)
        }
        Text(value, fontSize = 14.sp, color = mutedColor)
    }
}
