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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun SessionSummaryScreen(
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    onViewInsights: () -> Unit = {},
    onSessionDetails: () -> Unit = {},
    onSessionHistory: () -> Unit = {}
) {
    var focusScore by remember { mutableStateOf(4) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("A little progress", "SESSION SUMMARY · UNSAVED PREVIEW", onBack)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Color(0xFF10B981), style = Stroke(6.dp.toPx())) // Emerald Green
                    val path = Path().apply {
                        moveTo(size.width * 0.3f, size.height * 0.5f)
                        lineTo(size.width * 0.45f, size.height * 0.65f)
                        lineTo(size.width * 0.7f, size.height * 0.35f)
                    }
                    drawPath(path, Color(0xFF1E1E24), style = Stroke(6.dp.toPx()))
                }
                Text("finished", fontSize = 12.sp, color = Color(0xFF6B7280), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Session complete!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(8.dp))
        Text("You made space for what matters.", fontSize = 16.sp, color = Color(0xFF6B7280), modifier = Modifier.align(Alignment.CenterHorizontally))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Study machine learning", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("25 min", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("planned focus", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("1", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("interruption · 42 sec", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("25 min", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("actual focus", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("25:42", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("total elapsed", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("25 focused minutes · interruption time excluded.", fontSize = 12.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("How focused did you feel?", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (i in 1..5) {
                val isSelected = i == focusScore
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .height(48.dp)
                        .border(1.dp, if (isSelected) Color(0xFF7158E2) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFFF0EDFF) else Color.White, RoundedCornerShape(12.dp))
                        .clickable { focusScore = i },
                    contentAlignment = Alignment.Center
                ) {
                    Text("$i", fontSize = 16.sp, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color(0xFF7158E2) else Color(0xFF6B7280))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Scattered", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            Text("Fully focused", fontSize = 12.sp, color = Color(0xFF9CA3AF))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save Session", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onViewInsights,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))
        ) {
            Text("View Insights", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Not yet included in the sample week or Home totals.", fontSize = 12.sp, color = Color(0xFF9CA3AF), modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onSessionDetails,
                modifier = Modifier.weight(1f).height(48.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))
            ) {
                Text("Session details", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            OutlinedButton(
                onClick = onSessionHistory,
                modifier = Modifier.weight(1f).height(48.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))
            ) {
                Text("Session history", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}