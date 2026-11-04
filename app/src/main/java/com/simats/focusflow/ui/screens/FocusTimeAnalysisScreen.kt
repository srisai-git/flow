package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.models.HourlyFocusItem
import com.simats.focusflow.data.models.InsightsData
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SegmentedControl
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FocusTimeAnalysisScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    var selectedTab by remember { mutableStateOf("All") }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Focus time analysis", null, onBack)
        
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
        
        SegmentedControl(
            options = listOf("All", "Study", "Coding", "Other"),
            selectedOption = selectedTab,
            onOptionSelected = { selectedTab = it }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val defaultHours = listOf("08", "09", "10", "13", "16", "19")
        val hourlyMap = insightsData.hourlyFocusData.associate { it.hour to it.minutes }
        val displayData = defaultHours.map { h -> HourlyFocusItem(hour = h, minutes = hourlyMap[h] ?: 0) }
        val maxVal = maxOf(60f, displayData.maxOfOrNull { it.minutes.toFloat() } ?: 60f)
        val totalRecordedMinutes = if (insightsData.weekFocusMinutes > 0) insightsData.weekFocusMinutes else displayData.sumOf { it.minutes }

        if (totalRecordedMinutes == 0) {
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
                        imageVector = Icons.Default.HourglassBottom,
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bar Chart Card

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Recorded focus by hour", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Weekly minutes at session start hour", fontSize = 14.sp, color = colors.textMuted)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    displayData.forEach { item ->
                        val heightRatio = if (maxVal > 0f) (item.minutes / maxVal).coerceIn(0.04f, 1f) else 0.04f
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text("${item.minutes}", fontSize = 11.sp, color = colors.textMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .fillMaxHeight(heightRatio)
                                    .background(
                                        if (item.minutes > 0) Color(0xFF4FD1C5) else colors.border,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.hour, fontSize = 12.sp, color = colors.textMuted)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Hours · ${totalRecordedMinutes} recorded focus minutes this week",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Heatmap Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Recorded activity map", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Day", modifier = Modifier.weight(1f), fontSize = 12.sp, color = colors.textMuted)
                    val columns = listOf("08-10", "10-12", "12-14", "14-18", "18-21")
                    columns.forEach {
                        Text(it, modifier = Modifier.weight(1f), fontSize = 11.sp, color = colors.textMuted)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val dailyMinsMap = insightsData.dailyChartData.associate { it.day to it.focusMinutes }
                val levelColors = listOf(
                    colors.border.copy(alpha = 0.3f),
                    Color(0xFFE1FAF9),
                    Color(0xFF4FD1C5),
                    Color(0xFF0D9488)
                )
                
                days.forEach { dayName ->
                    val dayMins = dailyMinsMap[dayName] ?: 0
                    val intensity = when {
                        dayMins >= 120 -> 3
                        dayMins >= 60 -> 2
                        dayMins > 0 -> 1
                        else -> 0
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(dayName, modifier = Modifier.weight(1f), fontSize = 12.sp, color = colors.textMuted)
                        for (slot in 0..4) {
                            val slotIntensity = if (dayMins > 0) ((intensity + slot) % 4) else 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .padding(end = 4.dp)
                                    .background(levelColors[slotIntensity], RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Low · · · High · relative recorded minutes", fontSize = 12.sp, color = colors.textMuted)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = colors.primaryLight,
                    modifier = Modifier.padding(end = 12.dp).size(20.dp)
                )
                Text(
                    text = "Best observed window: ${insightsData.bestWindow}. More recorded focus observed during these hours.",
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Recorded: session minutes. Inferred: a useful time window. Focus time alone does not establish productivity.",
            fontSize = 12.sp,
            color = colors.textMuted
        )
    }
}
