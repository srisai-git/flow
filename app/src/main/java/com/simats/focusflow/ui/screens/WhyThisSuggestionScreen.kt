package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun WhyThisSuggestionScreen(onBack: () -> Unit = {}, onApply: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Why this suggestion?", "IDEAS TO TEST", onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text("Sample data", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Try 45 minutes at 09:00-11:00.", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        Spacer(modifier = Modifier.height(16.dp))
        Text("A starting hypothesis, not a proven best routine.", fontSize = 16.sp, color = Color(0xFF6B7280))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("What the sample week suggests", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Average focused duration", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("37.5 min", fontSize = 14.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sample sessions", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("20", fontSize = 14.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Morning / evening ratio", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("85% / 60%", fontSize = 14.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Focused minutes ÷ planned minutes by time window, not sessions completed.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3E8FF), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Make a small experiment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Test one 45-minute block, then a 5-minute break. Both are starting suggestions, not measured optima.", fontSize = 14.sp, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Shorten it if needed. Compare how focused you feel, not just how long you work.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
            Column {
                Text("RELEVANT HIGH-PRIORITY TASK", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(20.dp).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Study machine learning", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                        Text("Study · 45 min · 10:00", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Box(modifier = Modifier.background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("High", color = Color(0xFFB91C1C), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Fictional evidence; correlation is not proof. No live AI or validated causal claim.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Apply suggestion", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))
            ) {
                Text("Dismiss", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}