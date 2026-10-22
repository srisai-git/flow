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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun ReadySetupCompleteScreen(onBack: () -> Unit = {}, onGoToDashboard: () -> Unit = {}, onEditPreferences: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar(
            title = "Setup complete",
            subtitle = "DEMO SETUP · ALL SET",
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Color(0xFF7158E2), style = Stroke(6.dp.toPx()))
                    val path = Path().apply {
                        moveTo(size.width * 0.35f, size.height * 0.5f)
                        lineTo(size.width * 0.45f, size.height * 0.6f)
                        lineTo(size.width * 0.65f, size.height * 0.4f)
                    }
                    drawPath(path, Color(0xFF7158E2), style = Stroke(6.dp.toPx()))
                }
                Text("ready", fontSize = 14.sp, color = Color(0xFF9CA3AF), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("You're ready, Alex.", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Start small. Make room for a focused day, at your own pace.", fontSize = 18.sp, color = Color(0xFF6B7280), modifier = Modifier.align(Alignment.CenterHorizontally), textAlign = TextAlign.Center, lineHeight = 26.sp)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Alex Morgan's preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Text("Demo setup · your selected routine", fontSize = 14.sp, color = Color(0xFF0D9488))
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6))
                Spacer(modifier = Modifier.height(16.dp))
                
                PreferenceRow("📖", "Primary goal", "Study")
                Spacer(modifier = Modifier.height(16.dp))
                PreferenceRow("⏱", "Focus duration", "45 min")
                Spacer(modifier = Modifier.height(16.dp))
                PreferenceRow("🎯", "Daily focus goal", "3h 30m")
                Spacer(modifier = Modifier.height(16.dp))
                PreferenceRow("🔔", "Reminders", "09:00")
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onGoToDashboard,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Go to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onEditPreferences,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7158E2))
        ) {
            Text("Edit preferences", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Demo preferences only · nothing is saved. No reminders are sent and no AI is running.", fontSize = 12.sp, color = Color(0xFF9CA3AF), textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun PreferenceRow(icon: String, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(Color(0xFFF0EDFF), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text(icon, color = Color(0xFF7158E2), fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontSize = 16.sp, color = Color(0xFF6B7280))
        }
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
    }
}