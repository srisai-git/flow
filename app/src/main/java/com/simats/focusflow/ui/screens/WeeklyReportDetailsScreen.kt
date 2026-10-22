package com.simats.focusflow.ui.screens

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
fun WeeklyReportDetailsScreen(onBack: () -> Unit = {}, onViewRecommendations: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Weekly report details", "05-11 OCT 2026", onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text("Sample data", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Illustrative analysis · not live AI", fontSize = 14.sp, color = Color(0xFF6B7280))
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Focused minutes / day", fontSize = 16.sp, color = Color(0xFF6B7280))
                    Text("12h 30m", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(24.dp))
                val data = listOf(90, 120, 135, 150, 105, 90, 60)
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                val maxVal = 150f
                Row(modifier = Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    data.forEachIndexed { index, value ->
                        val heightRatio = value / maxVal
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                            Text("$value", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.width(20.dp).fillMaxHeight(heightRatio).background(Color(0xFF4FD1C5), RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(days[index], fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Compared with 28 Sep-04 Oct", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Focus time", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text("+25%", fontSize = 14.sp, color = Color(0xFF0D9488))
                    }
                    Text("10h → 12h 30m", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Sessions", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text("+2", fontSize = 14.sp, color = Color(0xFF0D9488))
                    }
                    Text("18 → 20", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Tasks completed", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text("67% → 75% · +8 pp", fontSize = 14.sp, color = Color(0xFF0D9488))
                    }
                    Text("16/24 → 18/24", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Interruptions", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text("-25%", fontSize = 14.sp, color = Color(0xFF0D9488))
                    }
                    Text("16 → 12", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3E8FF), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Morning 85% · Evening 60%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Focused minutes ÷ planned minutes in each time window. Not a session or task completion rate.", fontSize = 14.sp, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Morning had the higher ratio in this fictional week. This does not prove the time of day caused it.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Small fictional dataset; no validated prediction or causal finding. Task rates and their change are rounded.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onViewRecommendations,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("View Recommendations", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}