package com.simats.focusflow.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern Minimalist Home Dashboard for FocusFlow.
 * Dynamically theme-aware (Default Dark / Clean Light) with live pull-to-refresh.
 */
@Composable
fun DashboardScreen(
    onNavigateToTimer: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToHabits: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToSetIntention: () -> Unit = onNavigateToTimer,
    onNavigateToSessionHistory: () -> Unit = {},
    onNavigateToSearchFilters: () -> Unit = {},
    onNavigateToDataAi: () -> Unit = {},
    onNavigateToGrootAi: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors

    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
    }

    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.data

    val currentDateStr = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    val goalHours = (data.focusGoalMinutes / 60f).coerceAtLeast(1f)
    val completedHours = data.focusMinutesToday / 60f
    val progressFraction = (completedHours / goalHours).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "focusProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. TOP HEADER, REFRESH ACTION & STREAK BADGE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 14.sp,
                    color = colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = data.userName.ifBlank { "Focus User" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
                Text(
                    text = currentDateStr,
                    fontSize = 12.sp,
                    color = colors.primaryLight
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Groot AI Quick Launcher
                IconButton(
                    onClick = onNavigateToGrootAi,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.16f))
                        .border(1.dp, colors.primary.copy(alpha = 0.4f), CircleShape)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Ask Groot AI",
                        tint = colors.primaryLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Refresh Button
                IconButton(
                    onClick = { viewModel.loadDashboard() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Dashboard",
                        tint = colors.primaryLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Streak Badge (Clicking navigates to Habit Tracker)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.primary.copy(alpha = 0.15f))
                        .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToHabits() }
                        .bounceClick()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${data.streakDays} Day Streak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. DAILY TARGET PROGRESS CARD (Circular Ring with Violet Glow)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DAILY FOCUS TARGET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%.1f", completedHours),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = " / ${goalHours.toInt()} hrs",
                            fontSize = 16.sp,
                            color = colors.textMuted,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (completedHours >= goalHours) "Daily target achieved!" else "${(data.focusGoalMinutes - data.focusMinutesToday).coerceAtLeast(0)} mins remaining today",
                        fontSize = 12.sp,
                        color = if (completedHours >= goalHours) Color(0xFF10B981) else colors.textMuted
                    )
                }

                // Progress Dial
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val primaryColor = colors.primary
                    val primaryLight = colors.primaryLight
                    val surfaceVariant = colors.surfaceVariant

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()
                        // Background track
                        drawCircle(
                            color = surfaceVariant,
                            style = Stroke(strokeWidth)
                        )
                        // Active Arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(primaryColor, primaryLight, primaryColor)
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. QUICK START FOCUS BUTTON (1-tap launcher to Timer)
        Button(
            onClick = onNavigateToTimer,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Quick Start Pomodoro",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // GROOT AI COACH LAUNCHER CARD
        Surface(
            onClick = onNavigateToGrootAi,
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick(),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.primary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "GROOT AI COACH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Real-time AI advice on your focus & habits",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.primaryLight,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 4. HABIT SNAPSHOT SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DAILY HABITS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight,
                letterSpacing = 1.sp
            )
            Text(
                text = "View All",
                fontSize = 12.sp,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateToHabits() }
                    .bounceClick()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (data.habits.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text(
                    text = "No habits tracked yet. Tap 'View All' to add one!",
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(data.habits) { habit ->
                    HabitSnapshotChip(
                        habit = habit,
                        onToggle = { viewModel.toggleHabit(habit.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 5. TODAY'S PRIORITIES SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TODAY'S PRIORITIES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight,
                letterSpacing = 1.sp
            )
            Text(
                text = "Manage Tasks (${data.pendingTaskCount})",
                fontSize = 12.sp,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateToTasks() }
                    .bounceClick()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (data.priorityTasks.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.primaryLight,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "All caught up! No pending tasks.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                data.priorityTasks.forEach { task ->
                    PriorityTaskCard(
                        task = task,
                        onToggle = { viewModel.toggleTask(task.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
    }
}

@Composable
private fun HabitSnapshotChip(
    habit: HabitItem,
    onToggle: () -> Unit
) {
    val colors = FocusFlowTheme.colors

    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(14.dp),
        color = if (habit.isCompletedToday) colors.primary.copy(alpha = 0.2f) else colors.surface,
        border = BorderStroke(
            1.dp,
            if (habit.isCompletedToday) colors.primary else colors.border
        ),
        modifier = Modifier.bounceClick()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = if (habit.isCompletedToday) colors.primaryLight else colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = habit.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "${habit.streakCount}d streak",
                    fontSize = 11.sp,
                    color = if (habit.isCompletedToday) colors.primaryLight else colors.textMuted
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (habit.isCompletedToday) colors.primary else colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (habit.isCompletedToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityTaskCard(
    task: TaskItem,
    onToggle: () -> Unit
) {
    val colors = FocusFlowTheme.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .bounceClick(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) colors.primary else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (task.isCompleted) colors.primary else colors.border,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) colors.textMuted else colors.textPrimary,
                    style = if (task.isCompleted) MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    ) else MaterialTheme.typography.bodyMedium
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        maxLines = 1
                    )
                }
            }

            // Priority Indicator Tag
            val (tagColor, tagText) = when (task.priority.lowercase()) {
                "high" -> Color(0xFFEF4444) to "High"
                "low" -> Color(0xFF10B981) to "Low"
                else -> Color(0xFFF59E0B) to "Medium"
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = tagColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, tagColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = tagText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = tagColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
