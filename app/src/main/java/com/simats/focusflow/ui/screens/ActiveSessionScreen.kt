package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.service.FocusTimerService
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme
import com.simats.focusflow.ui.viewmodel.TimerMode

@Composable
fun ActiveSessionScreen(
    onBack: () -> Unit = {},
    onEndSession: () -> Unit = {},
    onLogDistraction: () -> Unit = {},
    onTakeBreak: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    val timerState by FocusTimerService.timerStateFlow.collectAsState()

    val mins = timerState.remainingSeconds / 60
    val secs = timerState.remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", mins, secs)
    val totalMins = timerState.totalSeconds / 60
    val sweepFraction = if (timerState.totalSeconds > 0) {
        timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        CustomTopBar("In your flow", "FOCUS SESSION", onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${timerState.currentMode.title} · Session in progress",
                    color = colors.primaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (timerState.currentMode == TimerMode.FOCUS) "Deep Focus Session" else "Rest & Recovery",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(270.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = colors.border,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = colors.primary,
                        startAngle = -90f,
                        sweepAngle = 360f * sweepFraction,
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "remaining of ${totalMins}:00",
                        fontSize = 14.sp,
                        color = colors.textMuted
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Text(
            text = "Keep your attention anchored to a single priority.",
            fontSize = 14.sp,
            color = colors.textMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                if (timerState.isRunning) {
                    FocusTimerService.pauseTimer(context)
                } else {
                    FocusTimerService.startTimer(context, timerState.activeUserId)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (timerState.isRunning) "Pause" else "Resume",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        OutlinedButton(
            onClick = {
                FocusTimerService.selectMode(context, TimerMode.SHORT_BREAK)
                onTakeBreak()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            border = BorderStroke(1.dp, colors.border),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
        ) {
            Text("Take a break", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        OutlinedButton(
            onClick = onEndSession,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            border = BorderStroke(1.dp, colors.border),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textMuted)
        ) {
            Text("End session", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
