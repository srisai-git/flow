package com.simats.focusflow.ui.screens

import android.graphics.Paint
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.repository.ProductivityRepository
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.AmbientMeshGlowHeader
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class PomodoroTagItem(
    val id: String,
    val name: String,
    val color: Color
)

enum class PomodoroPhase(val title: String, val minutes: Int) {
    POMODORO("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

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

    var currentPhase by remember { mutableStateOf(PomodoroPhase.POMODORO) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var totalSeconds by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var completedSprints by remember { mutableIntStateOf(0) }

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

    LaunchedEffect(isRunning, currentPhase) {
        if (isRunning) {
            while (isActive && remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            if (remainingSeconds <= 0) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (currentPhase == PomodoroPhase.POMODORO) {
                    val nextCompleted = completedSprints + 1
                    completedSprints = nextCompleted
                    val activeTag = customTags.firstOrNull { it.id == selectedTagId }?.name ?: "Work"
                    scope.launch(Dispatchers.IO) {
                        try {
                            val sessionMgr = SessionManager.getInstance(context)
                            val uId = sessionMgr.userSessionFlow.first().userId ?: 1
                            repository.logSession(
                                userId = uId,
                                durationMinutes = 25,
                                sessionType = "pomodoro",
                                tag = activeTag,
                                status = "completed"
                            )
                        } catch (_: Exception) {}
                    }
                    val nextMode = if (nextCompleted % 4 == 0) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
                    currentPhase = nextMode
                    totalSeconds = nextMode.minutes * 60
                    remainingSeconds = nextMode.minutes * 60
                    isRunning = false
                } else {
                    currentPhase = PomodoroPhase.POMODORO
                    totalSeconds = 25 * 60
                    remainingSeconds = 25 * 60
                    isRunning = false
                }
            }
        }
    }

    val progress = remember(remainingSeconds, totalSeconds) {
        if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "pomodoro_progress"
    )

    val formattedTime = remember(remainingSeconds) {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07060D))
    ) {
        AmbientMeshGlowHeader(height = 240.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    .background(Color(0xFF181628))
                    .bounceClick()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFF1F1F5),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "POMODORO FOCUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5A5F),
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = "Deep Sprint Timer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF1F1F5)
                )
            }

            IconButton(
                onClick = { showInfoModal = true },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF181628))
                    .bounceClick()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Info",
                    tint = Color(0xFFFF5A5F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val hairlineBorder = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.03f)
            )
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF11101C),
            border = BorderStroke(1.dp, hairlineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
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
                        color = Color(0xFFC4B5FD),
                        letterSpacing = 1.sp
                    )

                    TextButton(
                        onClick = { showAddTagDialog = true },
                        modifier = Modifier.bounceClick()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Tag",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8B5CF6)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) tag.color.copy(alpha = 0.20f) else Color(0xFF181628),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) tag.color else Color.White.copy(alpha = 0.05f)
                            ),
                            modifier = Modifier.bounceClick()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(tag.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tag.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFF1F1F5) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(310.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val tomatoR = (size.minDimension / 2f) - 18.dp.toPx()

                val tomatoShading = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF4D4D),
                        Color(0xFFDC2626),
                        Color(0xFF991B1B),
                        Color(0xFF450A0A)
                    ),
                    center = Offset(cx - tomatoR * 0.28f, cy - tomatoR * 0.32f),
                    radius = tomatoR * 1.35f
                )

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Black.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy + tomatoR + 6.dp.toPx()),
                        radius = tomatoR * 0.85f
                    ),
                    topLeft = Offset(cx - tomatoR * 0.85f, cy + tomatoR - 8.dp.toPx()),
                    size = Size(tomatoR * 1.7f, 26.dp.toPx())
                )

                drawCircle(
                    brush = tomatoShading,
                    radius = tomatoR,
                    center = Offset(cx, cy)
                )

                val calyxTop = cy - tomatoR + 2.dp.toPx()
                val calyxPath = Path().apply {
                    moveTo(cx, calyxTop)
                    lineTo(cx + 14.dp.toPx(), calyxTop + 16.dp.toPx())
                    lineTo(cx + 28.dp.toPx(), calyxTop + 4.dp.toPx())
                    lineTo(cx + 18.dp.toPx(), calyxTop + 24.dp.toPx())
                    lineTo(cx + 30.dp.toPx(), calyxTop + 32.dp.toPx())
                    lineTo(cx + 10.dp.toPx(), calyxTop + 30.dp.toPx())
                    lineTo(cx, calyxTop + 42.dp.toPx())
                    lineTo(cx - 10.dp.toPx(), calyxTop + 30.dp.toPx())
                    lineTo(cx - 30.dp.toPx(), calyxTop + 32.dp.toPx())
                    lineTo(cx - 18.dp.toPx(), calyxTop + 24.dp.toPx())
                    lineTo(cx - 28.dp.toPx(), calyxTop + 4.dp.toPx())
                    lineTo(cx - 14.dp.toPx(), calyxTop + 16.dp.toPx())
                    close()
                }
                drawPath(
                    path = calyxPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF1B4D2E), Color(0xFF0F361E))
                    )
                )

                val stemPath = Path().apply {
                    moveTo(cx, calyxTop + 16.dp.toPx())
                    cubicTo(
                        cx - 4.dp.toPx(), calyxTop - 8.dp.toPx(),
                        cx + 10.dp.toPx(), calyxTop - 12.dp.toPx(),
                        cx + 6.dp.toPx(), calyxTop - 22.dp.toPx()
                    )
                }
                drawPath(
                    path = stemPath,
                    color = Color(0xFF22543D),
                    style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                )

                drawOval(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(cx - tomatoR * 0.58f, cy - tomatoR * 0.62f),
                    size = Size(tomatoR * 0.44f, tomatoR * 0.22f)
                )

                val seamY = cy
                drawLine(
                    color = Color(0xFF280303),
                    start = Offset(cx - tomatoR + 3.dp.toPx(), seamY),
                    end = Offset(cx + tomatoR - 3.dp.toPx(), seamY),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val activeMins = remainingSeconds / 60f
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                for (m in 0 until 60) {
                    var diff = (m - activeMins) % 60f
                    if (diff < -30f) diff += 60f
                    if (diff > 30f) diff -= 60f

                    if (kotlin.math.abs(diff) <= 16f) {
                        val frac = diff / 16f
                        val tickX = cx + frac * (tomatoR * 0.74f)
                        val isMajor = m % 5 == 0
                        val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                        val tickTop = seamY - tickLen - 2.dp.toPx()

                        drawLine(
                            color = if (isMajor) Color.White else Color.White.copy(alpha = 0.75f),
                            start = Offset(tickX, tickTop),
                            end = Offset(tickX, seamY - 2.dp.toPx()),
                            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        if (isMajor && kotlin.math.abs(diff) <= 13f) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "$m",
                                tickX,
                                tickTop - 4.dp.toPx(),
                                paint
                            )
                        }
                    }
                }

                val pointerPath = Path().apply {
                    moveTo(cx, seamY + 2.dp.toPx())
                    lineTo(cx - 6.dp.toPx(), seamY + 14.dp.toPx())
                    lineTo(cx + 6.dp.toPx(), seamY + 14.dp.toPx())
                    close()
                }
                drawPath(path = pointerPath, color = Color.White)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 80.dp)
            ) {
                Text(
                    text = currentPhase.title.uppercase(),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formattedTime,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(
                        fontFeatureSettings = "tnum",
                        letterSpacing = (-0.5).sp
                    )
                )

                val cycleNum = (completedSprints % 4) + 1
                Text(
                    text = "SPRINT $cycleNum OF 4",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (currentPhase != PomodoroPhase.POMODORO) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentPhase = PomodoroPhase.POMODORO
                    totalSeconds = 25 * 60
                    remainingSeconds = 25 * 60
                    isRunning = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                    contentColor = Color(0xFFEF4444)
                ),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(44.dp)
                    .bounceClick()
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Skip Break",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    remainingSeconds = currentPhase.minutes * 60
                    isRunning = false
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
                    isRunning = !isRunning
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(170.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFEF4444) else Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "PAUSE" else "START",
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
                    if (currentPhase == PomodoroPhase.POMODORO) {
                        val nextCount = completedSprints + 1
                        completedSprints = nextCount
                        val nextMode = if (nextCount % 4 == 0) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
                        currentPhase = nextMode
                        totalSeconds = nextMode.minutes * 60
                        remainingSeconds = nextMode.minutes * 60
                        isRunning = false
                    } else {
                        currentPhase = PomodoroPhase.POMODORO
                        totalSeconds = 25 * 60
                        remainingSeconds = 25 * 60
                        isRunning = false
                    }
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
                        text = "${currentPhase.minutes} min",
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
                        text = "5 min",
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
                        text = "$completedSprints",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showInfoModal) {
        ModalBottomSheet(
            onDismissRequest = { showInfoModal = false },
            containerColor = Color(0xFF11101C),
            scrimColor = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(44.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF232035))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5A5F).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFFF5A5F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "How Pomodoro Works",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F1F5)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                listOf(
                    "1. 25-Minute Sprint" to "Focus single-mindedly on your chosen task with zero interruptions.",
                    "2. 5-Minute Rest" to "Step away, breathe, stretch, and give your cognitive energy time to recharge.",
                    "3. 4-Sprint Milestone" to "After completing four full cycles, reward yourself with an extended 20-30 minute recovery break."
                ).forEach { (stepTitle, stepDesc) ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF181628),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = stepTitle,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC4B5FD)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = stepDesc,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
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
