package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.service.FocusTimerService
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme

@Composable
fun SessionSummaryScreen(
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    onViewInsights: () -> Unit = {},
    onSessionDetails: () -> Unit = {},
    onSessionHistory: () -> Unit = {}
) {
    val colors = FocusFlowTheme.colors
    val timerState by FocusTimerService.timerStateFlow.collectAsState()
    var focusScore by remember { mutableStateOf(4) }
    val durationMinutes = if (timerState.focusMinutes > 0) timerState.focusMinutes else 25

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Session completed", "SUMMARY", onBack)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(40.dp))
                    .border(2.dp, Color(0xFF10B981), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Session complete!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "You made dedicated space for what matters.",
            fontSize = 14.sp,
            color = colors.textMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Text("Focus Interval Record", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$durationMinutes min", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.primaryLight)
                        Text("planned focus", fontSize = 13.sp, color = colors.textMuted)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$durationMinutes min", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("actual focus", fontSize = 13.sp, color = colors.textMuted)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$durationMinutes focused minutes synchronized with your personal records.",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("How focused did you feel?", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (i in 1..5) {
                val isSelected = i == focusScore
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .height(46.dp)
                        .border(
                            1.dp,
                            if (isSelected) colors.primary else colors.border,
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (isSelected) colors.primary.copy(alpha = 0.18f) else colors.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { focusScore = i }
                        .bounceClick(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$i",
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) colors.primaryLight else colors.textMuted
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Scattered", fontSize = 12.sp, color = colors.textMuted)
            Text("Fully focused", fontSize = 12.sp, color = colors.textMuted)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save & Continue", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        OutlinedButton(
            onClick = onViewInsights,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            border = BorderStroke(1.dp, colors.border),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
        ) {
            Text("View Insights", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        OutlinedButton(
            onClick = onSessionHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            border = BorderStroke(1.dp, colors.border),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textMuted)
        ) {
            Text("Session history", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
