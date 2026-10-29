package com.simats.focusflow.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun RecommendationsScreen(onBack: () -> Unit = {}, onPlanPrediction: () -> Unit = {}, onWhySuggestion: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Recommendations", null, onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Sample data", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("05–11 Oct 2026", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ideas to test, not instructions. These sample suggestions may not suit your day.", fontSize = 14.sp, color = Color(0xFF6B7280))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        RecommendationCard(icon = Icons.Default.Timer, title = "Try 45-minute blocks", desc = "Your recorded sessions averaged 37.5 minutes. Test 45; shorten it if needed.")
        Spacer(modifier = Modifier.height(16.dp))
        
        RecommendationCard(icon = Icons.Default.WbSunny, title = "Protect 09:00–11:00", desc = "Morning planned-minute completion was 85%, versus 60% in the evening.")
        Spacer(modifier = Modifier.height(16.dp))
        
        RecommendationCard(icon = Icons.Default.Coffee, title = "Take a 5-minute break", desc = "Try a break after each block. Break length is a starting suggestion, not measured.")
        Spacer(modifier = Modifier.height(16.dp))
        
        RecommendationCard(icon = Icons.Default.List, title = "Start with machine learning", desc = "Your Study task is high priority. Put it before the two lower-priority tasks.")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onWhySuggestion() }) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Why this suggestion?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Text("See the recorded evidence and limits.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
            Text(">", color = Color(0xFF6B7280), fontSize = 20.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Apply", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onPlanPrediction,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))
        ) {
            Text("Plan with a Prediction ->", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RecommendationCard(icon: ImageVector, title: String, desc: String) {
    Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = Color(0xFF7158E2), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
    }
}