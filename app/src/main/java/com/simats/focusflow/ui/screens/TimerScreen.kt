package com.simats.focusflow.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.local.FocusSessionDatabase
import com.simats.focusflow.data.models.FocusSessionRecord
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.service.NotificationHelper
import com.simats.focusflow.ui.components.AmbientMeshGlowHeader
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.TimerMode
import com.simats.focusflow.ui.viewmodel.TimerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    var showClockTimePicker by remember { mutableStateOf(false) }
    var showUsagePermissionDialog by remember { mutableStateOf(false) }
    var isStrictFocus by rememberSaveable { mutableStateOf(false) }
    var hasAccessibilityPermission by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var backgroundJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val refreshPermissions = {
        hasAccessibilityPermission = com.simats.focusflow.service.FocusBlockerAccessibilityService.isAccessibilityServiceEnabled(context)
        hasOverlayPermission = com.simats.focusflow.service.FocusBlockerAccessibilityService.canDrawOverlays(context)
    }

    LaunchedEffect(Unit) {
        isStrictFocus = com.simats.focusflow.service.FocusTimerService.timerStateFlow.value.isStrictFocus
        refreshPermissions()
    }

    DisposableEffect(lifecycleOwner, isStrictFocus, state.isRunning) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
            if (isStrictFocus && state.isRunning) {
                if (event == Lifecycle.Event.ON_STOP) {
                    backgroundJob?.cancel()
                    backgroundJob = scope.launch(Dispatchers.IO) {
                        delay(10000L)
                        if (lifecycleOwner.lifecycle.currentState < Lifecycle.State.STARTED && state.isRunning) {
                            try {
                                val sessionMgr = SessionManager.getInstance(context)
                                val session = sessionMgr.userSessionFlow.first()
                                val uId = session.userId ?: 1
                                val repo = ProductivityRepository()
                                repo.logDistraction(
                                    userId = uId,
                                    reason = "App backgrounded during focus",
                                    category = "Digital",
                                    durationSeconds = 10
                                )
                                val localDb = FocusSessionDatabase.getInstance(context)
                                localDb.insertDistraction(
                                    userId = uId,
                                    reason = "App backgrounded during focus",
                                    category = "Digital",
                                    durationSeconds = 10
                                )
                                NotificationHelper.showNotification(
                                    context = context,
                                    title = "Strict Focus Alert",
                                    body = "App was backgrounded during active focus. Return to stay in flow!",
                                    route = "timer"
                                )
                            } catch (_: Exception) {}
                        }
                    }
                } else if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                    backgroundJob?.cancel()
                    backgroundJob = null
                    refreshPermissions()
                }
            } else if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            backgroundJob?.cancel()
        }
    }

    LaunchedEffect(state.remainingSeconds) {
        if (state.remainingSeconds == 0 && state.totalSeconds > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
        ) {
            AmbientMeshGlowHeader(height = 260.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FOCUSFLOW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Focus Timer",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.currentMode != TimerMode.FOCUS) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Break in Progress",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Rest and recharge before next flow interval",
                                fontSize = 11.5.sp,
                                color = colors.textMuted
                            )
                        }
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.skipSession()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Skip Break", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    onClick = { viewModel.setSettingsOpen(true) },
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.bounceClick()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Interval: ",
                            fontSize = 12.5.sp,
                            color = colors.textMuted
                        )
                        Text(
                            text = "${state.getDurationMinutes(state.currentMode)} Minutes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Tap to adjust",
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }

                if (state.currentMode == TimerMode.FOCUS) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(5, 15, 25, 45, 60).forEach { mins ->
                            val isCurrent = state.focusMinutes == mins
                            Surface(
                                onClick = {
                                    viewModel.updateCustomDurations(
                                        focus = mins,
                                        shortBreak = state.shortBreakMinutes,
                                        longBreak = state.longBreakMinutes
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) colors.primary.copy(alpha = 0.18f) else colors.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrent) colors.primary else colors.border
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .bounceClick()
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) colors.primaryLight else colors.textMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TimerCountdownDial(
                remainingSeconds = state.remainingSeconds,
                totalSeconds = state.totalSeconds,
                isRunning = state.isRunning,
                onClockClick = {
                    if (!state.isRunning) {
                        showClockTimePicker = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            val hairlineBorder = colors.cardBorderBrush

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.resetTimer()
                    },
                    shape = CircleShape,
                    color = colors.surface,
                    border = BorderStroke(1.dp, hairlineBorder),
                    modifier = Modifier
                        .size(54.dp)
                        .bounceClick()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = colors.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(22.dp))

                Surface(
                    onClick = {
                        if (state.isRunning) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        viewModel.toggleStartPause()
                    },
                    modifier = Modifier
                        .height(64.dp)
                        .width(150.dp)
                        .bounceClick(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF8B5CF6),
                    border = BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.30f), Color.Transparent)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                Spacer(modifier = Modifier.width(22.dp))

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.skipSession()
                    },
                    shape = CircleShape,
                    color = colors.surface,
                    border = BorderStroke(1.dp, hairlineBorder),
                    modifier = Modifier
                        .size(54.dp)
                        .bounceClick()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip Session",
                            tint = colors.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, hairlineBorder)
            ) {
                Text(
                    text = "Sessions completed today: ${state.completedSessions}",
                    fontSize = 12.sp,
                    color = if (colors.isDark) Color(0xFFC4B5FD) else colors.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            val strictBorder = if (isStrictFocus) {
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFF8B5CF6), Color(0xFF10B981), Color(0xFF8B5CF6))
                    )
                )
            } else {
                BorderStroke(1.dp, hairlineBorder)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isStrictFocus) (if (colors.isDark) Color(0xFF16132A) else Color(0xFFF3E8FF)) else colors.surface,
                border = strictBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .bounceClick()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isStrictFocus)
                                        Brush.linearGradient(
                                            listOf(Color(0xFF8B5CF6).copy(alpha = 0.35f), Color(0xFF10B981).copy(alpha = 0.25f))
                                        )
                                    else
                                        Brush.linearGradient(
                                            listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))
                                        )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isStrictFocus) Color(0xFF10B981) else colors.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Strict Focus Mode",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isStrictFocus) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.16f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981))
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "SHIELD ARMED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isStrictFocus) "App blocker actively shields session" else "Lock out distracting social & game apps",
                                fontSize = 11.5.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                    Switch(
                        checked = isStrictFocus,
                        onCheckedChange = { checked ->
                            if (checked) {
                                isStrictFocus = true
                                com.simats.focusflow.service.FocusTimerService.setStrictFocus(context, true)
                                refreshPermissions()
                                if (!com.simats.focusflow.service.FocusBlockerAccessibilityService.isAccessibilityServiceEnabled(context)) {
                                    com.simats.focusflow.service.FocusBlockerAccessibilityService.openAccessibilitySettings(context)
                                } else if (!com.simats.focusflow.service.FocusBlockerAccessibilityService.canDrawOverlays(context)) {
                                    com.simats.focusflow.service.FocusBlockerAccessibilityService.openOverlaySettings(context)
                                }
                            } else {
                                isStrictFocus = false
                                com.simats.focusflow.service.FocusTimerService.setStrictFocus(context, false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8B5CF6),
                            uncheckedThumbColor = colors.textMuted,
                            uncheckedTrackColor = colors.border
                        )
                    )
                }
            }

            if (isStrictFocus && (!hasAccessibilityPermission || !hasOverlayPermission)) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(
                        1.dp,
                        if (hasAccessibilityPermission && hasOverlayPermission) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STRICT APP BLOCKER SETUP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasAccessibilityPermission && hasOverlayPermission) Color(0xFF10B981) else Color(0xFFF59E0B),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (hasAccessibilityPermission && hasOverlayPermission) "Active & Armed" else "Setup Needed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasAccessibilityPermission && hasOverlayPermission) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "1. Accessibility Service",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Required to detect and lock out blocked apps instantly",
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                            if (hasAccessibilityPermission) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        com.simats.focusflow.service.FocusBlockerAccessibilityService.openAccessibilitySettings(context)
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "2. Display Over Other Apps",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Displays full-screen Focus Shield over distracting apps",
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                            if (hasOverlayPermission) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        com.simats.focusflow.service.FocusBlockerAccessibilityService.openOverlaySettings(context)
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

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

        if (state.isSettingsOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setSettingsOpen(false) },
                containerColor = colors.surface,
                scrimColor = Color.Black.copy(alpha = 0.55f),
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

        if (state.isHistoryOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setHistoryOpen(false) },
                containerColor = colors.surface,
                scrimColor = Color.Black.copy(alpha = 0.55f),
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

        if (showClockTimePicker) {
            ClockTimePickerDialog(
                initialMinutes = state.focusMinutes,
                onDismiss = { showClockTimePicker = false },
                onConfirm = { hours, minutes ->
                    viewModel.setCustomFocusDuration(hours, minutes)
                    showClockTimePicker = false
                }
            )
        }

        if (showUsagePermissionDialog) {
            AlertDialog(
                onDismissRequest = { showUsagePermissionDialog = false },
                title = {
                    Text(
                        text = "Usage Access Required",
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = "Strict Focus Mode blocks distracting apps and shields your focus sessions. Please grant Usage Access permission to enable real-time app protection.",
                        color = colors.textMuted,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showUsagePermissionDialog = false
                            isStrictFocus = true
                            com.simats.focusflow.service.FocusTimerService.setStrictFocus(context, true)
                            com.simats.focusflow.utils.UsageStatsHelper.openUsageAccessSettings(context)
                        }
                    ) {
                        Text("Grant Permission", color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showUsagePermissionDialog = false
                            isStrictFocus = false
                            com.simats.focusflow.service.FocusTimerService.setStrictFocus(context, false)
                        }
                    ) {
                        Text("Cancel", color = colors.textMuted)
                    }
                },
                containerColor = colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
}



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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sessions.forEach { s ->
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6), contentColor = Color.White)
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



@Composable
private fun TimerCountdownDial(
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    onClockClick: (() -> Unit)? = null
) {
    val colors = FocusFlowTheme.colors
    val surfaceVariant = colors.surfaceVariant

    val targetProgress = remember(remainingSeconds, totalSeconds) {
        if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    }
    val animatedProgressState = animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 350),
        label = "timerProgress"
    )

    val elapsedFraction = remember(remainingSeconds, totalSeconds) {
        if (totalSeconds > 0) ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val currentRingColor = remember(elapsedFraction) {
        if (elapsedFraction < 0.5f) {
            androidx.compose.ui.graphics.lerp(Color(0xFF06B6D4), Color(0xFF8B5CF6), elapsedFraction * 2f)
        } else {
            androidx.compose.ui.graphics.lerp(Color(0xFF8B5CF6), Color(0xFF10B981), (elapsedFraction - 0.5f) * 2f)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "timerRingPulse")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlowAlpha"
    )
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    val formattedTime = remember(remainingSeconds) {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    val sweepBrush = remember(currentRingColor) {
        Brush.sweepGradient(
            listOf(
                Color(0xFF06B6D4),
                Color(0xFF8B5CF6),
                Color(0xFF10B981),
                currentRingColor,
                Color(0xFF06B6D4)
            )
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val dialSize = minOf(maxWidth * 0.72f, 250.dp).coerceAtLeast(180.dp)
        Box(
            modifier = Modifier
                .size(dialSize)
                .then(
                    if (!isRunning && onClockClick != null) {
                        Modifier
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onClockClick
                            )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val strokeWidthPx = remember(density) { with(density) { 12.dp.toPx() } }
            val glowWidthPx = remember(density) { with(density) { 22.dp.toPx() } }
            val bgStroke = remember(strokeWidthPx) { Stroke(width = strokeWidthPx) }
            val arcStroke = remember(strokeWidthPx) { Stroke(width = strokeWidthPx, cap = StrokeCap.Round) }
            val glowStroke = remember(glowWidthPx) { Stroke(width = glowWidthPx, cap = StrokeCap.Round) }
            val clipOvalPath = remember { androidx.compose.ui.graphics.Path() }
            val wavePath = remember { androidx.compose.ui.graphics.Path() }

            Canvas(modifier = Modifier.fillMaxSize()) {
                if (size.width <= 0f || size.height <= 0f || size.minDimension <= strokeWidthPx * 2f) return@Canvas
                val pad = strokeWidthPx / 2f
                val innerDiameter = size.minDimension - strokeWidthPx * 2f

                clipOvalPath.reset()
                clipOvalPath.addOval(androidx.compose.ui.geometry.Rect(strokeWidthPx, strokeWidthPx, size.width - strokeWidthPx, size.height - strokeWidthPx))

                clipPath(clipOvalPath) {
                    val progressVal = animatedProgressState.value.coerceIn(0f, 1f)
                    val liquidLevelY = size.height - (size.height - strokeWidthPx * 2f) * progressVal - strokeWidthPx
                    wavePath.reset()
                    val amplitude = if (isRunning) 6.dp.toPx() else 2.5.dp.toPx()
                    val waveLen = size.width * 0.75f
                    wavePath.moveTo(0f, size.height)
                    wavePath.lineTo(0f, liquidLevelY)
                    var wx = 0f
                    while (wx <= size.width) {
                        val wy = liquidLevelY + kotlin.math.sin((wx / waveLen) * 2 * Math.PI.toFloat() + wavePhase) * amplitude
                        wavePath.lineTo(wx, wy)
                        wx += 4f
                    }
                    wavePath.lineTo(size.width, size.height)
                    wavePath.close()

                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF06B6D4).copy(alpha = 0.16f),
                                Color(0xFF8B5CF6).copy(alpha = 0.32f),
                                Color(0xFF10B981).copy(alpha = 0.42f)
                            ),
                            startY = liquidLevelY,
                            endY = size.height
                        )
                    )
                }

                drawCircle(
                    color = surfaceVariant,
                    style = bgStroke
                )

                if (totalSeconds > 0) {
                    val currentStartAngle = -90f
                    drawArc(
                        brush = sweepBrush,
                        startAngle = currentStartAngle,
                        sweepAngle = animatedProgressState.value * 360f,
                        useCenter = false,
                        style = glowStroke,
                        alpha = if (isRunning) pulseGlowAlpha else 0.22f
                    )

                    drawArc(
                        brush = sweepBrush,
                        startAngle = currentStartAngle,
                        sweepAngle = animatedProgressState.value * 360f,
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
                    text = if (isRunning) "In Deep Flow" else "Tap clock to adjust",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRunning) currentRingColor else colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun ClockTimePickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (hours: Int, minutes: Int) -> Unit
) {
    val colors = FocusFlowTheme.colors
    var hours by remember { mutableIntStateOf((initialMinutes / 60).coerceIn(0, 3)) }
    var minutes by remember { mutableIntStateOf((initialMinutes % 60).coerceIn(0, 59)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "SET DURATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryLight,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Focus Timer Duration",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.background,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.width(96.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "HOURS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryLight,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            IconButton(
                                onClick = { if (hours < 3) hours++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plus hour", tint = colors.textPrimary)
                            }
                            Text(
                                text = String.format(Locale.US, "%02d", hours),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            IconButton(
                                onClick = { if (hours > 0) hours-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minus hour", tint = colors.textPrimary)
                            }
                        }
                    }

                    Text(
                        text = ":",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.background,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.width(96.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "MINUTES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryLight,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            IconButton(
                                onClick = {
                                    if (hours * 60 + minutes < 180) {
                                        if (minutes < 55) minutes += 5
                                        else if (hours < 3) {
                                            hours++
                                            minutes = 0
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plus minutes", tint = colors.textPrimary)
                            }
                            Text(
                                text = String.format(Locale.US, "%02d", minutes),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            IconButton(
                                onClick = {
                                    val currentTotal = hours * 60 + minutes
                                    if (currentTotal > 1) {
                                        if (minutes >= 5) minutes -= 5
                                        else if (hours > 0) {
                                            hours--
                                            minutes = 55
                                        } else {
                                            minutes = 1
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minus minutes", tint = colors.textPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15, 25, 45, 60, 90, 120).forEach { preset ->
                        val pHours = preset / 60
                        val pMins = preset % 60
                        val isSelected = (hours * 60 + minutes) == preset
                        Surface(
                            onClick = {
                                hours = pHours
                                minutes = pMins
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) colors.primary else Color.Transparent),
                            modifier = Modifier.bounceClick()
                        ) {
                            Text(
                                text = "${preset}m",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.primaryLight else colors.textMuted,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val totalMins = (hours * 60 + minutes).coerceIn(1, 180)
                Text(
                    text = "Total Focus Time: $totalMins minutes",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTotal = (hours * 60 + minutes).coerceIn(1, 180)
                    onConfirm(finalTotal / 60, finalTotal % 60)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Set Timer", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textMuted)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    TimerScreen()
}
