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
fun WhyThisSuggestionScreen(onBack: () -> Unit = {}, onApply: () -> Unit = {}) {
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
    val avgDuration = if (totalSessions > 0) totalMins / totalSessions else 25
    val hasEnoughData = totalSessions > 0 || totalMins > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Why this suggestion?", "RECORDED EVIDENCE", onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (hasEnoughData) "Live Metrics" else "No Data Yet",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(currentDateRange, fontSize = 14.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!hasEnoughData) {
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
            Text(
                text = "Target $avgDuration minutes during ${insightsData.bestWindow}.",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Calculated from your verified focus records and session stamina.",
                fontSize = 14.sp,
                color = colors.textMuted
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Recorded Evidence", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Average focused duration", fontSize = 14.sp, color = colors.textMuted)
                        Text("$avgDuration min", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    }
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Logged sessions this week", fontSize = 14.sp, color = colors.textMuted)
                        Text("$totalSessions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    }
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Task completion rate", fontSize = 14.sp, color = colors.textMuted)
                        Text("${insightsData.tasksSummary.completionRatePercent.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Recommended Action", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Work in $avgDuration-minute blocks, then take a 5-minute breather. Shorten the interval if fatigue appears.",
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            border = BorderStroke(1.dp, colors.border),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
        ) {
            Text("Back to Recommendations", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
