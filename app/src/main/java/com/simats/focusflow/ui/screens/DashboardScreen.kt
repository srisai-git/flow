package com.simats.focusflow.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.models.HabitItem
import com.simats.focusflow.data.models.TaskItem
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.GrootAvatar
import com.simats.focusflow.ui.components.PriorityBadge
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.DarkPrimary
import com.simats.focusflow.ui.theme.DarkPrimaryLight
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern Aesthetic Home Dashboard Screen for FocusFlow.
 * Features an abstract geometric clock hero canvas, streak modal sheet,
 * quick-start timer, priorities quick list, habit matrix, and Groot AI snippet.
 * Strict zero-emoji compliance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTimer: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToHabits: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToSetIntention: () -> Unit = {},
    onNavigateToSessionHistory: () -> Unit = {},
    onNavigateToSearchFilters: () -> Unit = {},
    onNavigateToDataAi: () -> Unit = {},
    onNavigateToGrootAi: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
    }

    val colors = FocusFlowTheme.colors
    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.data

    val currentDateStr = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    val completedHours = remember(data.focusMinutesToday) {
        data.focusMinutesToday / 60f
    }
    val goalHours = remember(data.focusGoalMinutes) {
        (data.focusGoalMinutes / 60f).coerceAtLeast(1f)
    }
    val progressFraction = remember(completedHours, goalHours) {
        (completedHours / goalHours).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
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

        // 1. TOP HEADER WITH GREETING, STREAK BADGE & GROOT AI ICON
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome back,",
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = data.userName.ifBlank { "Focus User" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentDateStr,
                    fontSize = 12.sp,
                    color = colors.primaryLight
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Streak Badge (Isolated to Home Top Bar - Tapping opens Streak Bottom Sheet)
                Surface(
                    onClick = { viewModel.setStreakModalOpen(true) },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                    modifier = Modifier.bounceClick()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${data.streakDays}d Streak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Groot AI Assistant Quick Launcher Icon (Single Top-Bar Entry Point)
                IconButton(
                    onClick = onNavigateToGrootAi,
                    modifier = Modifier
                        .size(42.dp)
                        .bounceClick()
                ) {
                    GrootAvatar(
                        size = 40.dp,
                        containerColor = colors.surface,
                        borderColor = colors.primary.copy(alpha = 0.4f),
                        tint = colors.primaryLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. CENTRAL HERO CARD: ABSTRACT GEOMETRIC TIME-FLOW CLOCK & TARGET
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
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
                        text = "DAILY FOCUS RHYTHM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.2.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${(progressFraction * 100).toInt()}% Target",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Text Column: Focus Progress numbers
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", completedHours),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = " / ${goalHours.toInt()} hrs",
                                fontSize = 16.sp,
                                color = colors.textMuted,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 5.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (completedHours >= goalHours) {
                                "Daily target achieved!"
                            } else {
                                val remainingMins = (data.focusGoalMinutes - data.focusMinutesToday).coerceAtLeast(0)
                                "$remainingMins min left to goal"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (completedHours >= goalHours) Color(0xFF10B981) else colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${data.focusMinutesToday} focused minutes today across your active deep work intervals.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = colors.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right Side: Abstract Geometric Time-Flow Clock Canvas
                    AbstractGeometricClock(
                        progress = animatedProgress,
                        modifier = Modifier.size(108.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. PRODUCTIVITY VIBE / COMPACT GROOT AI SNIPPET
        Surface(
            onClick = onNavigateToGrootAi,
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF161522),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(colors.primary.copy(alpha = 0.55f), colors.border))),
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.18f))
                    )
                    GrootAvatar(
                        size = 32.dp,
                        containerColor = colors.primary.copy(alpha = 0.25f),
                        borderColor = colors.primary.copy(alpha = 0.5f),
                        tint = colors.primaryLight
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GROOT AI ASSISTANT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = uiState.aiSnippet,
                        fontSize = 12.5.sp,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.primary.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. QUICK-START FOCUS TIMER CARD (Tap-to-start 25m focus session)
        Surface(
            onClick = onNavigateToTimer,
            shape = RoundedCornerShape(18.dp),
            color = colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Timer",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Quick Start Pomodoro",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "25 min Deep Work Interval",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. TODAY'S PRIORITIES SECTION (Zero Empty Space)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TODAY'S PRIORITIES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight,
                letterSpacing = 1.sp
            )
            Text(
                text = "Manage (${data.pendingTaskCount})",
                fontSize = 12.sp,
                color = colors.primaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateToTasks() }
                    .bounceClick()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (data.priorityTasks.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.primaryLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "All priorities completed! Tap 'Manage' to add tasks.",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.priorityTasks.take(3).forEach { task ->
                    key(task.id) {
                        PriorityTaskRow(
                            task = task,
                            onToggle = { viewModel.toggleTask(task.id) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. HABIT RHYTHM MATRIX (Quick Daily Routine Completion Row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HABIT RHYTHM MATRIX",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryLight,
                letterSpacing = 1.sp
            )
            Text(
                text = "View Habits",
                fontSize = 12.sp,
                color = colors.primaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateToHabits() }
                    .bounceClick()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (data.habits.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text(
                    text = "No habits tracked yet. Tap 'View Habits' to build routines!",
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
                items(data.habits, key = { it.id }) { habit ->
                    HabitSnapshotChip(
                        habit = habit,
                        onToggle = { viewModel.toggleHabit(habit.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp)) // Space for bottom navigation bar
    }

    // STREAK MODAL BOTTOM SHEET (Requested in Requirement 4)
    if (uiState.isStreakModalOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setStreakModalOpen(false) },
            containerColor = colors.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.border)
                )
            }
        ) {
            StreakModalSheetContent(
                currentStreak = data.streakDays,
                longestStreak = data.longestStreak,
                habitsCompleted = data.totalHabitsCompleted,
                totalFocusMinutes = data.totalFocusMinutes,
                onDismiss = { viewModel.setStreakModalOpen(false) },
                onShare = {
                    val shareHours = data.totalFocusMinutes / 60
                    val shareMins = data.totalFocusMinutes % 60
                    val shareText = "I'm on a ${data.streakDays}-day deep focus streak with FocusFlow! Logged ${shareHours}h ${shareMins}m of productive flow."
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share FocusFlow Streak")
                    context.startActivity(shareIntent)
                }
            )
        }
    }
}

/**
 * Abstract, aesthetic geometric clock or time-flow visualization in Compose Canvas:
 * Concentric minimalist arc rings representing daily focus progress,
 * minimalist hour tick divisions, and current time rhythm indicator.
 * Strictly geometric - zero human illustrations.
 */
@Composable
private fun AbstractGeometricClock(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = FocusFlowTheme.colors
    val primary = colors.primary
    val primaryLight = colors.primaryLight
    val border = colors.border
    val textPrimary = colors.textPrimary

    // Subtle gentle pulse for inner time rhythm node
    val infiniteTransition = rememberInfiniteTransition(label = "clockPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val density = LocalDensity.current
    val stroke6Px = remember(density) { with(density) { 6.dp.toPx() } }
    val stroke1_5Px = remember(density) { with(density) { 1.5.dp.toPx() } }
    val stroke2Px = remember(density) { with(density) { 2.dp.toPx() } }
    val stroke1Px = remember(density) { with(density) { 1.dp.toPx() } }

    val trackStroke = remember(stroke6Px) { Stroke(width = stroke6Px) }
    val arcStroke = remember(stroke6Px) { Stroke(width = stroke6Px, cap = StrokeCap.Round) }
    val innerTrackStroke = remember(stroke1_5Px) { Stroke(width = stroke1_5Px) }

    val sweepBrush = remember(primary, primaryLight) {
        Brush.sweepGradient(listOf(primary, primaryLight, primary))
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val outerRadius = w * 0.44f
        val innerRadius = w * 0.32f

        // 1. 12 Geometric Clock Tick Marks around outer edge
        for (i in 0 until 12) {
            val angleDeg = i * 30.0 - 90.0
            val angleRad = Math.toRadians(angleDeg)
            val isMajor = (i % 3 == 0)
            val tickLen = if (isMajor) w * 0.08f else w * 0.045f
            val strokeW = if (isMajor) stroke2Px else stroke1Px

            val startR = outerRadius + stroke2Px
            val endR = startR + tickLen

            val startX = (center.x + startR * cos(angleRad)).toFloat()
            val startY = (center.y + startR * sin(angleRad)).toFloat()
            val endX = (center.x + endR * cos(angleRad)).toFloat()
            val endY = (center.y + endR * sin(angleRad)).toFloat()

            drawLine(
                color = if (isMajor) primaryLight.copy(alpha = 0.7f) else border,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }

        // 2. Outer Track Arc Ring (Background)
        drawCircle(
            color = border,
            radius = outerRadius,
            center = center,
            style = trackStroke
        )

        // 3. Concentric Focus Progress Arc Ring
        if (progress > 0f) {
            drawArc(
                brush = sweepBrush,
                startAngle = -90f,
                sweepAngle = (progress * 360f).coerceIn(1f, 360f),
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = arcStroke
            )
        }

        // 4. Secondary Inner Track Arc (Time Flow Rhythm)
        drawCircle(
            color = border.copy(alpha = 0.5f),
            radius = innerRadius,
            center = center,
            style = innerTrackStroke
        )

        // 5. Center Dial & Rhythm Node
        drawCircle(
            color = primary.copy(alpha = 0.16f),
            radius = innerRadius * 0.7f,
            center = center
        )

        drawCircle(
            color = primary,
            radius = 4.dp.toPx() * pulseScale,
            center = center
        )

        // Minimalist Geometric Hour / Minute Indicator Hands
        val progressAngleRad = Math.toRadians((progress * 360.0 - 90.0))
        val handLength = innerRadius * 0.85f
        val handEndX = (center.x + handLength * cos(progressAngleRad)).toFloat()
        val handEndY = (center.y + handLength * sin(progressAngleRad)).toFloat()

        drawLine(
            color = primaryLight,
            start = center,
            end = Offset(handEndX, handEndY),
            strokeWidth = stroke2Px,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Streak Bottom Sheet matching Requirement 4:
 * Vector flame icon -> "[X] days" | "Current streak"
 * Vector calendar icon -> "[X] days" | "Longest streak"
 * Vector check icon -> "[X]" | "Habits completed"
 * Vector timer icon -> "[X] hrs [Y] min" | "Total focus time"
 * Bottom Action: "Share" button triggering Android's native share intent (ACTION_SEND).
 * Zero community counts.
 */
@Composable
private fun StreakModalSheetContent(
    currentStreak: Int,
    longestStreak: Int,
    habitsCompleted: Int,
    totalFocusMinutes: Int,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val colors = FocusFlowTheme.colors
    val totalHours = totalFocusMinutes / 60
    val totalMins = totalFocusMinutes % 60

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(
            text = "STREAK & DISCIPLINE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryLight,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Focus Consistency",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Row 1: Flame Icon -> Current Streak
        StreakMetricRow(
            icon = Icons.Rounded.LocalFireDepartment,
            iconTint = Color(0xFFF59E0B),
            title = "$currentStreak days",
            subtitle = "Current streak"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Row 2: Calendar Icon -> Longest Streak
        StreakMetricRow(
            icon = Icons.Default.CalendarMonth,
            iconTint = DarkPrimaryLight,
            title = "$longestStreak days",
            subtitle = "Longest streak"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Row 3: Check Icon -> Habits completed
        StreakMetricRow(
            icon = Icons.Default.Check,
            iconTint = Color(0xFF10B981),
            title = "$habitsCompleted",
            subtitle = "Habits completed"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Row 4: Timer Icon -> Total focus time
        StreakMetricRow(
            icon = Icons.Default.HourglassBottom,
            iconTint = DarkPrimary,
            title = "${totalHours} hrs ${totalMins} min",
            subtitle = "Total focus time"
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Share Action Button (Triggers ACTION_SEND native intent)
        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Share Streak",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun StreakMetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    val colors = FocusFlowTheme.colors

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun PriorityTaskRow(
    task: TaskItem,
    onToggle: () -> Unit
) {
    val colors = FocusFlowTheme.colors

    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) colors.primary else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (task.isCompleted) colors.primary else colors.textMuted,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) colors.textMuted else colors.textPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = task.category,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }

            PriorityBadge(priority = task.priority)
        }
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
                modifier = Modifier.size(18.dp)
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
        }
    }
}
