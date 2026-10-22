package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetIntentionScreen(
    onBack: () -> Unit = {},
    onBeginFocus: () -> Unit = {}
) {
    var mode by remember { mutableStateOf("Selected task") }
    var duration by remember { mutableStateOf("25 min") }
    var sessionGoal by remember { mutableStateOf("Understand precision vs. recall") }
    var trackInterruptions by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        CustomTopBar(
            title = "Set your intention",
            subtitle = "ONE TASK. A LITTLE SPACE TO THINK.",
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SegmentedControl(
            options = listOf("Selected task", "Free focus"),
            selectedOption = mode,
            onOptionSelected = { mode = it }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Focus On Dropdown
        Text(text = "Focus on", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "Study machine learning",
            onValueChange = { },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2)
            ),
            trailingIcon = {
                Canvas(modifier = Modifier.size(24.dp).padding(end = 12.dp)) {
                    val stroke = 2.dp.toPx()
                    drawLine(
                        color = Color(0xFF6B7280),
                        start = Offset(0f, size.height * 0.4f),
                        end = Offset(size.width / 2, size.height * 0.6f),
                        strokeWidth = stroke
                    )
                    drawLine(
                        color = Color(0xFF6B7280),
                        start = Offset(size.width / 2, size.height * 0.6f),
                        end = Offset(size.width, size.height * 0.4f),
                        strokeWidth = stroke
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
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
                        color = Color(0xFFF3F4F6),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF7158E2),
                        startAngle = -90f,
                        sweepAngle = 360f * (25f / 60f), // visually representing 25 min out of 60
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "25", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    Text(text = "minutes of focus", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        SegmentedControl(
            options = listOf("25 min", "45 min", "60 min"),
            selectedOption = duration,
            onOptionSelected = { duration = it }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Session Goal
        Text(text = "Session goal · optional", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = sessionGoal,
            onValueChange = { sessionGoal = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Track Interruptions Toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Track interruptions", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Log distractions without losing your place.", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
                Switch(
                    checked = trackInterruptions,
                    onCheckedChange = { trackInterruptions = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF7158E2)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "You don't need a perfect session. Just a beginning.",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onBeginFocus,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Begin Focus", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetIntentionScreenPreview() {
    SetIntentionScreen()
}