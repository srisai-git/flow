package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
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
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.utils.DistractionUsageSummary
import com.simats.focusflow.utils.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DistractionAnalysisScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    var insightsData by remember { mutableStateOf(InsightsData()) }
    var usageSummary by remember { mutableStateOf(DistractionUsageSummary()) }
    var hasPermission by remember { mutableStateOf(UsageStatsHelper.hasUsageStatsPermission(context)) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        hasPermission = UsageStatsHelper.hasUsageStatsPermission(context)
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

        if (hasPermission) {
            withContext(Dispatchers.IO) {
                val stats = UsageStatsHelper.queryWeeklyDistractionStats(context)
                withContext(Dispatchers.Main) {
                    usageSummary = stats
                }
            }
        }
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

    val palette = listOf(
        Color(0xFF8B5CF6),
        Color(0xFF4FD1C5),
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFF6B7280)
    )

    // Determine category data: prioritize real automated usage stats if available
    val categoryItems = remember(usageSummary, insightsData) {
        if (usageSummary.categoryBreakdown.isNotEmpty()) {
            val totalMins = maxOf(1, usageSummary.totalDistractionMinutes)
            usageSummary.categoryBreakdown.entries.map { (cat, mins) ->
                val pct = ((mins.toFloat() / totalMins.toFloat()) * 100).toInt()
                DistractionCategoryItem(category = cat, count = mins, percent = pct)
            }.sortedByDescending { it.count }
        } else if (insightsData.distractionsSummary.categories.isNotEmpty()) {
            insightsData.distractionsSummary.categories
        } else {
            emptyList()
        }
    }

    val totalDistractionMins = if (usageSummary.totalDistractionMinutes > 0) {
        usageSummary.totalDistractionMinutes
    } else {
        insightsData.distractionsSummary.totalEvents * 5
    }

    val topCategoryName = if (usageSummary.topCategory.isNotBlank()) {
        usageSummary.topCategory
    } else {
        insightsData.distractionsSummary.topCategory
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
                    text = if (hasPermission) "Automated Activity" else "Manual Records",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(currentDateRange, fontSize = 14.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // UsageStats Permission Banner if not enabled
        if (!hasPermission) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Automated Distraction Tracking",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enable Usage Access so FocusFlow can automatically detect phone screen time, app interruptions, and digital distractions during your focus routines.",
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            UsageStatsHelper.openUsageAccessSettings(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.bounceClick()
                    ) {
                        Text(
                            text = "Enable Automated Tracking",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
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
                        text = if (totalDistractionMins >= 60) "${totalDistractionMins / 60}h ${totalDistractionMins % 60}m" else "${totalDistractionMins}m",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text("screen distraction", fontSize = 14.sp, color = colors.textMuted)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (categoryItems.isNotEmpty()) "${categoryItems.size}" else "0",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text("active app categories", fontSize = 14.sp, color = colors.textMuted)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Donut Chart Card / Empty State
        if (categoryItems.isEmpty() && totalDistractionMins == 0) {
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
                            var currentAngle = -90f
                            categoryItems.forEachIndexed { index, cat ->
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
                            if (categoryItems.isEmpty()) {
                                drawCircle(colors.border, style = stroke)
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (totalDistractionMins >= 60) "${totalDistractionMins / 60}h" else "${totalDistractionMins}m",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text("logged", fontSize = 10.sp, color = colors.textMuted)
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text("Category Pattern", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$topCategoryName is the leading category across your phone screen time.",
                            fontSize = 13.sp,
                            color = colors.textMuted,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Actual Apps List Card (From UsageStatsManager)
        if (usageSummary.apps.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "TOP APPS USED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    usageSummary.apps.forEachIndexed { index, app ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = app.category,
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                            Text(
                                text = "${app.usageMinutes}m · ${app.percent}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textMuted
                            )
                        }
                        if (index < usageSummary.apps.size - 1) {
                            HorizontalDivider(
                                color = colors.border.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Category Breakdown Card
        if (categoryItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    categoryItems.forEachIndexed { index, cat ->
                        LegendRow(
                            color = palette[index % palette.size],
                            title = cat.category,
                            value = "${cat.count}m · ${cat.percent}%",
                            textColor = colors.textPrimary,
                            mutedColor = colors.textMuted
                        )
                        if (index < categoryItems.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Advice Card
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
                    text = if (totalDistractionMins > 0) {
                        "Most frequent: $topCategoryName. Try silencing notifications for $topCategoryName before your next focus interval."
                    } else {
                        "No distraction app usage recorded. Solid digital wellbeing discipline!"
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
    textColor: Color = FocusFlowTheme.colors.textPrimary,
    mutedColor: Color = FocusFlowTheme.colors.textMuted
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
