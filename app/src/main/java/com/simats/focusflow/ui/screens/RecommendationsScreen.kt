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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

data class RecommendationItem(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val desc: String
)

@Composable
fun RecommendationsScreen(
    onBack: () -> Unit = {},
    onPlanPrediction: () -> Unit = {},
    onWhySuggestion: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    var insightsData by remember { mutableStateOf(InsightsData()) }
    var isLoading by remember { mutableStateOf(true) }
    var dismissedIds by remember { mutableStateOf(setOf<String>()) }

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
    val hasEnoughData = totalSessions > 0 || totalMins >= 15

    // Dynamically generated recommendations from real database records
    val dynamicRecommendations = remember(insightsData) {
        if (!hasEnoughData) {
            emptyList()
        } else {
            val list = mutableListOf<RecommendationItem>()
            val avgDuration = if (totalSessions > 0) totalMins / totalSessions else 25

            list.add(
                RecommendationItem(
                    id = "rec_interval",
                    icon = Icons.Default.Timer,
                    title = "Target $avgDuration-minute focus blocks",
                    desc = "Your recorded sessions average $avgDuration minutes. Calibrating your timer to match your actual stamina minimizes cognitive fatigue."
                )
            )

            list.add(
                RecommendationItem(
                    id = "rec_window",
                    icon = Icons.Default.WbSunny,
                    title = "Protect ${insightsData.bestWindow}",
                    desc = "Your highest focus density occurs during ${insightsData.bestWindow}. Reserve this window for complex, deep-work tasks."
                )
            )

            list.add(
                RecommendationItem(
                    id = "rec_break",
                    icon = Icons.Default.Coffee,
                    title = "Take 5-minute breathers",
                    desc = "Schedule a short mental breather after each deep work interval to sustain high cognitive endurance."
                )
            )

            if (insightsData.distractionsSummary.totalEvents > 0) {
                list.add(
                    RecommendationItem(
                        id = "rec_distraction",
                        icon = Icons.Default.Security,
                        title = "Silence ${insightsData.distractionsSummary.topCategory}",
                        desc = "${insightsData.distractionsSummary.topCategory} was your primary source of interruption this week. Silence alerts before starting focus sessions."
                    )
                )
            }

            if (insightsData.tasksSummary.completedTasks > 0) {
                list.add(
                    RecommendationItem(
                        id = "rec_tasks",
                        icon = Icons.Default.CheckCircle,
                        title = "Maintain task momentum",
                        desc = "You completed ${insightsData.tasksSummary.completedTasks} tasks (${insightsData.tasksSummary.completionRatePercent.toInt()}% completion rate). Prioritize high-impact items early."
                    )
                )
            }

            list
        }
    }

    val visibleRecommendations = dynamicRecommendations.filter { it.id !in dismissedIds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Recommendations", null, onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (hasEnoughData) "Live Insights" else "Getting Started",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(currentDateRange, fontSize = 14.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Adaptive behavioral observations derived from your actual focus sessions and completed tasks.",
            fontSize = 14.sp,
            color = colors.textMuted
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (!hasEnoughData) {
            // Clean Empty-State UI (Requirement 1 & 2)
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
                        text = "Not enough focus data yet. Complete more sessions this week to unlock AI recommendations.",
                        fontSize = 14.sp,
                        color = colors.textMuted,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                }
            }
        } else if (visibleRecommendations.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text(
                    text = "All recommendations reviewed for this week. Great job keeping your flow aligned!",
                    fontSize = 14.sp,
                    color = colors.textMuted,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            visibleRecommendations.forEach { rec ->
                RecommendationCardDark(
                    icon = rec.icon,
                    title = rec.title,
                    desc = rec.desc,
                    onDismiss = {
                        dismissedIds = dismissedIds + rec.id
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // "Why this suggestion?" Action Link
        Surface(
            onClick = onWhySuggestion,
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier.fillMaxWidth().bounceClick()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.primaryLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Why this suggestion?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("See your recorded focus evidence and metrics.", fontSize = 12.sp, color = colors.textMuted)
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Dismiss / Close Button (Requirement 7: Removed "Apply" button)
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
            Text("Dismiss", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onPlanPrediction,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryLight)
        ) {
            Text("Plan with a Prediction", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RecommendationCardDark(
    icon: ImageVector,
    title: String,
    desc: String,
    onDismiss: () -> Unit
) {
    val colors = FocusFlowTheme.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.primary.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = desc,
                            fontSize = 13.sp,
                            color = colors.textMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss tip",
                        tint = colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
