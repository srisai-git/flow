package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun ProgressConsistencyScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Progress & consistency", null, onBack)
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Sample data", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("05–11 Oct 2026", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("7 days", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("current streak", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("9 days", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("longest streak", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Active day = any recorded focus. Rest matters.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Weekly rhythm · 7 / 7 active days", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
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
                Spacer(modifier = Modifier.height(16.dp))
                Text("Minutes focused · total 12h 30m", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Monthly consistency · last 4 weeks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val d = listOf("4", "5", "6", "7")
                    val dates = listOf("14 Sep", "21 Sep", "28 Sep", "05 Oct")
                    for (i in 0..3) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(d[i], fontSize = 12.sp, color = Color(0xFF6B7280))
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.size(24.dp).background(Color(0xFF7158E2), RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(dates[i], fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Active days / week · 22 of 28 days", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("43h", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("4-week total focus", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("60", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("4-week tasks done", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tasks / week: 12 → 14 → 16 → 18", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Text("🏅", fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                    Column {
                        Text("Milestone · A week of showing up", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("7 active days. Your rhythm is growing.", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                }
            }
        }
    }
}