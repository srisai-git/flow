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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.TimerMode
import com.simats.focusflow.ui.viewmodel.TimerViewModel
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
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 500),
        label = "timerProgress"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // HEADER ROW WITH TUNE / CUSTOMIZATION BUTTON
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

                // Customize Durations Action Button
                IconButton(
                    onClick = { viewModel.setSettingsOpen(true) },
                    modifier = Modifier
                        .size(44.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

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
                    val tabBg by animateColorAsState(
                        targetValue = if (isSelected) colors.primary else Color.Transparent,
                        label = "tabBg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else colors.textMuted,
                        label = "tabTextColor"
                    )
                    val mins = state.getDurationMinutes(mode)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tabBg)
                            .clickable { viewModel.selectMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${mode.title} (${mins}m)",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 2. CIRCULAR COUNTDOWN TIMER WITH VIOLET GLOW BRUSH
            Box(
                modifier = Modifier.size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = colors.primary
                val primaryLight = colors.primaryLight
                val surfaceVariant = colors.surfaceVariant

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    // Track background circle
                    drawCircle(
                        color = surfaceVariant,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active progress arc with violet gradient
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(primaryColor, primaryLight, primaryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.formattedTime,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (state.isRunning) "FOCUSING" else "PAUSED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isRunning) colors.primaryLight else colors.textMuted,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(44.dp))

            // 3. CONTROLS (Reset, Play/Pause, Skip)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier
                        .size(54.dp)
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

                // Main Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable { viewModel.toggleStartPause() }
                        .bounceClick(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isRunning) "Pause" else "Start",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Skip Button
                IconButton(
                    onClick = { viewModel.skipSession() },
                    modifier = Modifier
                        .size(54.dp)
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

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(100.dp))
        }

        // 4. CUSTOMIZE DURATIONS BOTTOM SHEET
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
    }
}

@Composable
private fun CustomizeDurationsSheet(
    currentFocus: Int,
    currentShortBreak: Int,
    currentLongBreak: Int,
    onDismiss: () -> Unit,
    onSave: (focus: Int, shortBreak: Int, longBreak: Int) -> Unit
) {
    val colors = FocusFlowTheme.colors
    var focusVal by remember { mutableFloatStateOf(currentFocus.toFloat()) }
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

        // 1. Focus duration slider (10 - 90 mins)
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
            valueRange = 10f..90f,
            steps = 15,
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

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    TimerScreen()
}
