package com.simats.focusflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.service.FocusTimerService
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.TimerMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PomodoroTagItem(
    val id: String,
    val name: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val repository = remember { ProductivityRepository() }

    val timerState by FocusTimerService.timerStateFlow.collectAsState()

    var showInfoModal by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }

    val tagColors = listOf(
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
        Color(0xFFEC4899),
        Color(0xFF06B6D4)
    )

    val customTags = remember {
        mutableStateListOf(
            PomodoroTagItem("1", "Work", Color(0xFF3B82F6)),
            PomodoroTagItem("2", "Study", Color(0xFF8B5CF6)),
            PomodoroTagItem("3", "Coding", Color(0xFF10B981))
        )
    }
    var selectedTagId by remember { mutableStateOf("1") }

    val animatedProgress by animateFloatAsState(
        targetValue = timerState.progress,
        label = "pomodoro_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POMODORO FOCUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = "Deep Sprint Timer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            IconButton(
                onClick = { showInfoModal = !showInfoModal },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Info",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = showInfoModal,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "What is the Pomodoro Technique?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        IconButton(
                            onClick = { showInfoModal = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Close",
                                tint = colors.textMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Focus completely on your chosen task for 25 uninterrupted minutes.\n2. When the timer rings, take a 5-minute restorative short break.\n3. After completing 4 focus sprints, reward yourself with an extended 15-30 minute break.\n\nThis proven rhythm enhances deep concentration, prevents mental fatigue, and boosts long-term productivity.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = colors.textMuted
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TASK TAG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryLight,
                        letterSpacing = 1.sp
                    )

                    TextButton(onClick = { showAddTagDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Tag",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customTags.forEach { tag ->
                        val isSelected = tag.id == selectedTagId
                        Surface(
                            onClick = {
                                selectedTagId = tag.id
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) tag.color.copy(alpha = 0.22f) else colors.surfaceVariant,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) tag.color else Color.Transparent
                            ),
                            modifier = Modifier.bounceClick()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(tag.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tag.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.textPrimary else colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(310.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val ringRadius = size.minDimension / 2f - 14.dp.toPx()

                drawCircle(
                    color = Color(0xFF261217),
                    radius = ringRadius,
                    style = Stroke(width = 12.dp.toPx())
                )

                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFEF4444),
                            Color(0xFFF97316),
                            Color(0xFFEF4444)
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )

                val tomatoRadius = ringRadius * 0.76f
                val tomatoCy = cy + 10.dp.toPx()

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF87171),
                            Color(0xFFEF4444),
                            Color(0xFF991B1B)
                        ),
                        center = Offset(cx - tomatoRadius * 0.28f, tomatoCy - tomatoRadius * 0.22f),
                        radius = tomatoRadius * 1.15f
                    ),
                    topLeft = Offset(cx - tomatoRadius, tomatoCy - tomatoRadius * 0.88f),
                    size = Size(tomatoRadius * 1.3f, tomatoRadius * 1.76f)
                )

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF87171),
                            Color(0xFFDC2626),
                            Color(0xFF7F1D1D)
                        ),
                        center = Offset(cx + tomatoRadius * 0.28f, tomatoCy - tomatoRadius * 0.22f),
                        radius = tomatoRadius * 1.15f
                    ),
                    topLeft = Offset(cx - tomatoRadius * 0.3f, tomatoCy - tomatoRadius * 0.88f),
                    size = Size(tomatoRadius * 1.3f, tomatoRadius * 1.76f)
                )

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(cx - tomatoRadius * 0.45f, tomatoCy - tomatoRadius * 0.62f),
                    size = Size(tomatoRadius * 0.45f, tomatoRadius * 0.26f)
                )

                val stemTopY = tomatoCy - tomatoRadius * 1.25f
                val stemAttachY = tomatoCy - tomatoRadius * 0.82f

                val stemPath = Path().apply {
                    moveTo(cx - 3.dp.toPx(), stemAttachY)
                    quadraticBezierTo(cx - 6.dp.toPx(), stemTopY + 4.dp.toPx(), cx + 7.dp.toPx(), stemTopY)
                    quadraticBezierTo(cx + 4.dp.toPx(), stemTopY + 8.dp.toPx(), cx + 3.dp.toPx(), stemAttachY)
                    close()
                }
                drawPath(
                    path = stemPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                    )
                )

                val leafLeft = Path().apply {
                    moveTo(cx, stemAttachY)
                    quadraticBezierTo(cx - tomatoRadius * 0.4f, stemAttachY - 14.dp.toPx(), cx - tomatoRadius * 0.52f, stemAttachY - 2.dp.toPx())
                    quadraticBezierTo(cx - tomatoRadius * 0.28f, stemAttachY + 2.dp.toPx(), cx, stemAttachY)
                    close()
                }
                drawPath(leafLeft, Color(0xFF22C55E))

                val leafRight = Path().apply {
                    moveTo(cx, stemAttachY)
                    quadraticBezierTo(cx + tomatoRadius * 0.4f, stemAttachY - 14.dp.toPx(), cx + tomatoRadius * 0.52f, stemAttachY - 2.dp.toPx())
                    quadraticBezierTo(cx + tomatoRadius * 0.28f, stemAttachY + 2.dp.toPx(), cx, stemAttachY)
                    close()
                }
                drawPath(leafRight, Color(0xFF16A34A))

                val leafCenter = Path().apply {
                    moveTo(cx, stemAttachY)
                    quadraticBezierTo(cx - 6.dp.toPx(), stemAttachY + 12.dp.toPx(), cx, stemAttachY + 18.dp.toPx())
                    quadraticBezierTo(cx + 6.dp.toPx(), stemAttachY + 12.dp.toPx(), cx, stemAttachY)
                    close()
                }
                drawPath(leafCenter, Color(0xFF4ADE80))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timerState.currentMode.title.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = timerState.formattedTime,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                val cycleNum = (timerState.completedSessions % 4) + 1
                Text(
                    text = "Sprint $cycleNum of 4",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    FocusTimerService.resetTimer(context)
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .bounceClick()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (timerState.isRunning) {
                        FocusTimerService.pauseTimer(context)
                    } else {
                        FocusTimerService.startTimer(context, timerState.activeUserId)
                    }
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(170.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerState.isRunning) Color(0xFFEF4444) else Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (timerState.isRunning) "PAUSE" else "START",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    FocusTimerService.skipSession(context)
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .bounceClick()
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FOCUS DURATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${timerState.focusMinutes} min",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(colors.border)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SHORT BREAK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${timerState.shortBreakMinutes} min",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(colors.border)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "COMPLETED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${timerState.completedSessions}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = {
                Text(
                    text = "Create Task Tag",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("Tag Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CHOOSE COLOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        tagColors.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        2.dp,
                                        if (selectedColorIndex == index) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedColorIndex = index },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColorIndex == index) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newTagName.trim()
                        if (trimmed.isNotBlank()) {
                            val newTag = PomodoroTagItem(
                                id = java.util.UUID.randomUUID().toString(),
                                name = trimmed,
                                color = tagColors[selectedColorIndex]
                            )
                            customTags.add(newTag)
                            selectedTagId = newTag.id
                            newTagName = ""
                            showAddTagDialog = false

                            scope.launch(Dispatchers.IO) {
                                try {
                                    repository.logDistraction(
                                        userId = timerState.activeUserId,
                                        reason = "Created tag: $trimmed",
                                        category = "TagCreation",
                                        durationSeconds = 0
                                    )
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
