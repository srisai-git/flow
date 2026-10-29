package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun AiReportScreen(onBack: () -> Unit = {}, onViewRecommendations: () -> Unit = {}, onViewReportDetails: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Your weekly AI report", null, onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Sample data", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("05–11 Oct 2026", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Illustrative analysis of fictional records, not live AI.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Key Findings
        Box(
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            Column {
                Row {
                    Icon(Icons.Default.Insights, contentDescription = null, tint = Color(0xFF7158E2), modifier = Modifier.padding(end = 8.dp).size(18.dp))
                    Text("Key findings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Your recorded morning sessions had a higher completion rate than your evening sessions.", fontSize = 16.sp, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Morning 85%", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Evening 60%", color = Color(0xFF4B5563), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Completion = focused minutes ÷ planned minutes, by time window. Not a task completion rate.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Strengths
        Box(
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            Column {
                Row {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.padding(end = 8.dp).size(18.dp))
                    Text("Strengths", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("12h 30m across 20 sessions. You completed 18 of 24 tasks (75%). Thursday led with 150 focused minutes.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Challenges
        Box(
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            Column {
                Row {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.padding(end = 8.dp).size(18.dp))
                    Text("Challenges", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("12 interruptions this week; Social Media led with 5. Evening planned-minute completion was 60%.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recommendations
        Box(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF3E8FF), RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            Column {
                Text("Recommended changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Try demanding work at 09:00–11:00, your best observed focus window. Silence social alerts before starting.", fontSize = 16.sp, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
                Text("An inferred pattern, not a causal or validated finding.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onViewRecommendations,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("View Recommendations", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = onViewReportDetails,
                modifier = Modifier.weight(1f).height(56.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))
            ) {
                Text("View report details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}