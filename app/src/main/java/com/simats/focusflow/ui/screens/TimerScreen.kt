package com.simats.focusflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.models.FocusSessionRecord
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.TimerMode
import com.simats.focusflow.ui.viewmodel.TimerViewModel
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TimerViewModel = viewModel()
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors

    LaunchedEffect(Unit) {
        viewModel.initSessionManager(context, SessionManager.getInstance(context))
    }

    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // HEADER ROW WITH HISTORY & TUNE ACTION BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "POMODORO FOCUS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Deep Work Interval",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Recent Sessions History Button
                    IconButton(
                        onClick = { viewModel.setHistoryOpen(true) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.border, CircleShape)
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Session History",
                            tint = colors.primaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Customize Durations Action Button
                    IconButton(
                        onClick = { viewModel.setSettingsOpen(true) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.border, CircleShape)
                            .bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize Timer",
                            tint = colors.primaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. MODE TABS (Focus, Short Break, Long Break) with configured minutes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimerMode.values().forEach { mode ->
                    val isSelected = state.currentMode == mode
                    val tabBgColor by animateColorAsState(
                        targetValue = if (isSelected) colors.primary else Color.Transparent,
                        label = "tabBg"
                    )
                    val tabTextColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else colors.textMuted,
                        label = "tabText"
                    )

                    Surface(
                        onClick = { viewModel.selectMode(mode) },
                        shape = RoundedCornerShape(12.dp),
                        color = tabBgColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .bounceClick()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${mode.title} (${state.getDurationMinutes(mode)}m)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = tabTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. CENTRAL TIMER CIRCLE WITH ISOLATED COUNTDOWN DIAL
            TimerCountdownDial(
                remainingSeconds = state.remainingSeconds,
                totalSeconds = state.totalSeconds,
                isRunning = state.isRunning
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. CONTROLS ROW (Reset, Start/Pause, Skip)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = colors.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Primary Start / Pause Action Button
                Button(
                    onClick = { viewModel.toggleStartPause() },
                    modifier = Modifier
                        .height(64.dp)
                        .width(140.dp)
                        .bounceClick(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isRunning) "Pause" else "Start",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isRunning) "Pause" else "Start",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Skip Session Button
                IconButton(
                    onClick = { viewModel.skipSession() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip Session",
                        tint = colors.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sessions Completed Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Text(
                    text = "Sessions completed today: ${state.completedSessions}",
                    fontSize = 12.sp,
                    color = colors.primaryLight,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // RECENT FOCUS SESSIONS INLINE SECTION (Requirement 10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT FOCUS SESSIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryLight,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "View All (${state.recentSessions.size})",
                    fontSize = 12.sp,
                    color = colors.primaryLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.setHistoryOpen(true) }
                        .bounceClick()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (state.recentSessions.isEmpty()) {
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
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "No focus sessions recorded yet. Start a session above!",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.recentSessions.take(3).forEach { session ->
                        FocusSessionItemCard(session = session)
                    }
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }

        // 4. CUSTOMIZE DURATIONS BOTTOM SHEET (Allows 5 to 90 mins)
        if (state.isSettingsOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setSettingsOpen(false) },
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
                CustomizeDurationsSheet(
                    currentFocus = state.focusMinutes,
                    currentShortBreak = state.shortBreakMinutes,
                    currentLongBreak = state.longBreakMinutes,
                    onDismiss = { viewModel.setSettingsOpen(false) },
                    onSave = { f, sb, lb ->
                        viewModel.updateCustomDurations(f, sb, lb)
                    }
                )
            }
        }

        // 5. SESSION HISTORY BOTTOM SHEET (Requirement 10)
        if (state.isHistoryOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setHistoryOpen(false) },
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
                SessionHistorySheet(
                    sessions = state.recentSessions,
                    onDismiss = { viewModel.setHistoryOpen(false) }
                )
            }
        }
    }
}

/**
 * Bottom Sheet displaying authenticated focus sessions history queried from focus_sessions table.
 */
@Composable
private fun SessionHistorySheet(
    sessions: List<FocusSessionRecord>,
    onDismiss: () -> Unit
) {
    val colors = FocusFlowTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SESSION HISTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryLight,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Logged Focus Sessions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No focus session records found in database.",
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.id }) { s ->
                    FocusSessionItemCard(session = s)
                }
            }
        }
    }
}

@Composable
private fun FocusSessionItemCard(session: FocusSessionRecord) {
    val colors = FocusFlowTheme.colors

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = colors.primaryLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${session.durationMinutes} min Focus",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    val tag = if (session.taskTitle.isNotBlank()) {
                        session.taskTitle
                    } else if (session.taskCategory.isNotBlank()) {
                        session.taskCategory
                    } else {
                        "Deep Work"
                    }

                    Text(
                        text = "$tag • ${session.startedAt.ifBlank { "Recent" }}",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Completed",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

/**
 * Customize timer durations allowing interval from 5 to 90 minutes.
 */
@Composable
private fun CustomizeDurationsSheet(
    currentFocus: Int,
    currentShortBreak: Int,
    currentLongBreak: Int,
    onDismiss: () -> Unit,
    onSave: (focus: Int, shortBreak: Int, longBreak: Int) -> Unit
) {
    val colors = FocusFlowTheme.colors
    var focusVal by remember { mutableFloatStateOf(currentFocus.coerceIn(5, 90).toFloat()) }
    var shortBreakVal by remember { mutableFloatStateOf(currentShortBreak.toFloat()) }
    var longBreakVal by remember { mutableFloatStateOf(currentLongBreak.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timer Durations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Focus duration slider (5 - 90 mins)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Focus Interval", fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
            Text("${focusVal.roundToInt()} min", fontSize = 14.sp, color = colors.primaryLight, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = focusVal,
            onValueChange = { focusVal = it },
            valueRange = 5f..90f,
            steps = 16,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Short Break slider (1 - 15 mins)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Short Break", fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
            Text("${shortBreakVal.roundToInt()} min", fontSize = 14.sp, color = colors.primaryLight, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = shortBreakVal,
            onValueChange = { shortBreakVal = it },
            valueRange = 1f..15f,
            steps = 13,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Long Break slider (10 - 30 mins)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Long Break", fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
            Text("${longBreakVal.roundToInt()} min", fontSize = 14.sp, color = colors.primaryLight, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = longBreakVal,
            onValueChange = { longBreakVal = it },
            valueRange = 10f..30f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(focusVal.roundToInt(), shortBreakVal.roundToInt(), longBreakVal.roundToInt())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .bounceClick(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text(
                text = "Apply Durations",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Isolated Timer Countdown Dial composable that localizes 1-second recompositions
 * to eliminate whole-screen UI re-rendering during active focus sessions.
 */
@Composable
private fun TimerCountdownDial(
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean
) {
    val colors = FocusFlowTheme.colors
    val primaryColor = colors.primary
    val primaryLight = colors.primaryLight
    val surfaceVariant = colors.surfaceVariant

    val targetProgress = remember(remainingSeconds, totalSeconds) {
        if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 300),
        label = "timerProgress"
    )

    val formattedTime = remember(remainingSeconds) {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    val sweepBrush = remember(primaryColor, primaryLight) {
        Brush.sweepGradient(listOf(primaryColor, primaryLight, primaryColor))
    }

    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val strokeWidthPx = remember(density) { with(density) { 12.dp.toPx() } }
        val bgStroke = remember(strokeWidthPx) { Stroke(width = strokeWidthPx) }
        val arcStroke = remember(strokeWidthPx) { Stroke(width = strokeWidthPx, cap = StrokeCap.Round) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background Track Ring
            drawCircle(
                color = surfaceVariant,
                style = bgStroke
            )

            // Sweeping Progress Arc
            if (totalSeconds > 0) {
                drawArc(
                    brush = sweepBrush,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = arcStroke
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.textPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isRunning) "In Deep Flow" else "Paused",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isRunning) colors.primaryLight else colors.textMuted
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    TimerScreen()
}
