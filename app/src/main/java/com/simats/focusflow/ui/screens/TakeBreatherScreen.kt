package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun TakeBreatherScreen(onResumeFocus: () -> Unit = {}, onSkipBreak: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        CustomTopBar("Take a breather", "BREAK · WORK TIMER PAUSED", onResumeFocus)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text("5-minute break", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Study machine learning", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), modifier = Modifier.align(Alignment.CenterHorizontally))
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(Color(0xFFF3F4F6), 0f, 360f, false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(Color(0xFF4FD1C5), -90f, 360f * (4.5f / 5f), false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("04:32", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    Text("break remaining of 05:00", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("☕", fontSize = 20.sp, color = Color(0xFF4FD1C5))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("A quiet moment away", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Stand up, stretch gently, or look away from your screen.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Focus stays paused at 24:18 of 25:00. Break time does not count as focused time.", fontSize = 12.sp, color = Color(0xFF9CA3AF), textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.CenterHorizontally))
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onResumeFocus,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Resume focus", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSkipBreak,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))
        ) {
            Text("Skip break", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Back returns to the paused timer without discarding it.", fontSize = 12.sp, color = Color(0xFF9CA3AF), modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}