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
fun PlanNextDayScreen(onBack: () -> Unit = {}, onReviewTasks: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Plan your next productive\nday", null, onBack)
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Sample data · demo estimate", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("05–11 Oct 2026", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3E8FF), RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Estimated completion likelihood: 78%", fontSize = 16.sp, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
                Text("78%", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.background(Color.White, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("Illustrative estimate · not performance", color = Color(0xFF7158E2), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("For the proposed plan on Tuesday, 13 Oct.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Inputs to this example", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Available focus time", fontSize = 16.sp, color = Color(0xFF6B7280))
                    Text("150 min", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Historical task\ncompletion", fontSize = 16.sp, color = Color(0xFF6B7280))
                    Text("75% · 18 / 24", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Planned workload", fontSize = 16.sp, color = Color(0xFF6B7280))
                    Text("3 tasks · 130 min", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Estimate vs recorded history", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Historical", fontSize = 14.sp, color = Color(0xFF6B7280), modifier = Modifier.weight(0.3f))
                    Box(modifier = Modifier.weight(0.5f).height(12.dp).background(Color(0xFFE5E7EB), RoundedCornerShape(6.dp))) {
                        Box(modifier = Modifier.fillMaxWidth(0.75f).fillMaxHeight().background(Color(0xFF4FD1C5), RoundedCornerShape(6.dp)))
                    }
                    Text("75%", fontSize = 14.sp, color = Color(0xFF1E1E24), modifier = Modifier.weight(0.2f).padding(start = 12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Estimated", fontSize = 14.sp, color = Color(0xFF6B7280), modifier = Modifier.weight(0.3f))
                    Box(modifier = Modifier.weight(0.5f).height(12.dp).background(Color(0xFFE5E7EB), RoundedCornerShape(6.dp))) {
                        Box(modifier = Modifier.fillMaxWidth(0.78f).fillMaxHeight().background(Color(0xFF7158E2), RoundedCornerShape(6.dp)))
                    }
                    Text("78%", fontSize = 14.sp, color = Color(0xFF1E1E24), modifier = Modifier.weight(0.2f).padding(start = 12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("0–100% · unlike metrics shown for context only", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Estimates may be inaccurate. Sufficient recorded history is needed; this concept does not run or validate an AI model.", fontSize = 14.sp, color = Color(0xFF6B7280), lineHeight = 20.sp)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onReviewTasks, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFE5E7EB)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))) {
            Text("Review planned tasks", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}