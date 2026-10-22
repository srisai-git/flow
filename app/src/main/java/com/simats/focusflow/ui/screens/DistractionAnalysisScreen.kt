package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun DistractionAnalysisScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Distraction analysis", null, onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Sample data", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("05–11 Oct 2026", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Overview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("12", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    Text("interruptions this week", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("0.6", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    Text("per recorded session", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Donut Chart Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(16.dp.toPx())
                        // Social Media (42%)
                        drawArc(Color(0xFF7158E2), -90f, 150f, false, style = stroke)
                        // Notifications (25%)
                        drawArc(Color(0xFF4FD1C5), 60f, 90f, false, style = stroke)
                        // Browsing (17%)
                        drawArc(Color(0xFFB45309), 150f, 60f, false, style = stroke)
                        // People (8%)
                        drawArc(Color(0xFF10B981), 210f, 30f, false, style = stroke)
                        // Other (8%)
                        drawArc(Color(0xFF6B7280), 240f, 30f, false, style = stroke)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("12", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("logged events", fontSize = 10.sp, color = Color(0xFF6B7280))
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text("Know the pattern.", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Social Media accounts for 42% of logged interruptions.", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // List Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                LegendRow(Color(0xFF7158E2), "Social Media", "5 · 42%")
                Spacer(modifier = Modifier.height(12.dp))
                LegendRow(Color(0xFF4FD1C5), "Notifications", "3 · 25%")
                Spacer(modifier = Modifier.height(12.dp))
                LegendRow(Color(0xFFB45309), "Browsing", "2 · 17%")
                Spacer(modifier = Modifier.height(12.dp))
                LegendRow(Color(0xFF10B981), "People", "1 · 8%")
                Spacer(modifier = Modifier.height(12.dp))
                LegendRow(Color(0xFF6B7280), "Other", "1 · 8%")
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Gaming: 0 · percentages rounded", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("↓ 25% vs last week", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("16 → 12 events", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3E8FF), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row {
                Canvas(modifier = Modifier.size(20.dp).padding(top = 4.dp)) {
                    drawRoundRect(Color(0xFF7158E2), size = Size(size.width*0.6f, size.height*0.8f), style = Stroke(2.dp.toPx()))
                    drawCircle(Color(0xFF7158E2), radius = 1.dp.toPx(), center = Offset(size.width*0.3f, size.height*0.7f))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Most frequent: Social Media. Try silencing social notifications before your next focus block.", fontSize = 14.sp, color = Color(0xFF1E1E24))
            }
        }
    }
}

@Composable
fun LegendRow(color: Color, title: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 14.sp, color = Color(0xFF1E1E24))
        }
        Text(value, fontSize = 14.sp, color = Color(0xFF6B7280))
    }
}