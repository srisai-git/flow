package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SegmentedControl
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetIntentionScreen(
    onBack: () -> Unit = {},
    onBeginFocus: () -> Unit = {}
) {
    val colors = FocusFlowTheme.colors
    var mode by remember { mutableStateOf("Selected task") }
    var duration by remember { mutableStateOf("25 min") }
    var sessionGoal by remember { mutableStateOf("Deep Focus Block") }
    var trackInterruptions by remember { mutableStateOf(true) }

    val durationNum = remember(duration) {
        duration.substringBefore(" ").toIntOrNull() ?: 25
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar(
            title = "Set your intention",
            subtitle = "ONE TASK. A LITTLE SPACE TO THINK.",
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        SegmentedControl(
            options = listOf("Selected task", "Free focus"),
            selectedOption = mode,
            onOptionSelected = { mode = it }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Focus on",
            fontSize = 13.sp,
            color = colors.textMuted,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = if (mode == "Selected task") "Priority Focus Session" else "Freeform Deep Work",
            onValueChange = { },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colors.border,
                focusedBorderColor = colors.primary,
                unfocusedContainerColor = colors.surface,
                focusedContainerColor = colors.surface,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            )
        )
        
        Spacer(modifier = Modifier.height(36.dp))
        
        // Circular Duration Indicator
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF262433),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = colors.primary,
                        startAngle = -90f,
                        sweepAngle = 360f * (durationNum.toFloat() / 60f),
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$durationNum",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "minutes of focus",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(36.dp))
        
        SegmentedControl(
            options = listOf("25 min", "45 min", "60 min"),
            selectedOption = duration,
            onOptionSelected = { duration = it }
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        Text(
            text = "Session goal · optional",
            fontSize = 13.sp,
            color = colors.textMuted,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = sessionGoal,
            onValueChange = { sessionGoal = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colors.border,
                focusedBorderColor = colors.primary,
                unfocusedContainerColor = colors.surface,
                focusedContainerColor = colors.surface,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Track interruptions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Log distractions without losing your place.",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
                Switch(
                    checked = trackInterruptions,
                    onCheckedChange = { trackInterruptions = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.primary,
                        uncheckedThumbColor = colors.textMuted,
                        uncheckedTrackColor = colors.surface
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "You do not need a perfect session. Just a beginning.",
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onBeginFocus,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Begin Focus", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
