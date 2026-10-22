package com.simats.focusflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SegmentedControl

@Composable
fun FocusTimeAnalysisScreen(onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Focus time analysis", null, onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Sample data", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("05–11 Oct 2026", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        SegmentedControl(
            options = listOf("All", "Study", "Coding", "Other"),
            selectedOption = selectedTab,
            onOptionSelected = { selectedTab = it }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Bar Chart Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Recorded focus by hour", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Weekly minutes at session start hour", fontSize = 14.sp, color = Color(0xFF6B7280))
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val data = listOf(120, 180, 150, 120, 105, 75)
                val hours = listOf("08", "09", "10", "13", "16", "19")
                val maxVal = 180f
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEachIndexed { index, value ->
                        val heightRatio = value / maxVal
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                            Text("$value", fontSize = 12.sp, color = Color(0xFF6B7280))
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.width(24.dp).fillMaxHeight(heightRatio).background(Color(0xFF4FD1C5), RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(hours[index], fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Hours · all 750 recorded minutes shown", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Heatmap Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Recorded activity map", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
                
                // Headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Day", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color(0xFF6B7280))
                    val columns = listOf("08-10", "10-12", "12-14", "14-18", "18-21")
                    columns.forEach {
                        Text(it, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val heatmapData = listOf(
                    listOf(1, 2, 4, 1, 1), // Mon
                    listOf(4, 2, 4, 4, 1), // Tue
                    listOf(1, 2, 2, 1, 4), // Wed
                    listOf(2, 2, 2, 4, 1), // Thu
                    listOf(4, 2, 4, 1, 1), // Fri
                    listOf(1, 4, 1, 1, 1), // Sat
                    listOf(1, 1, 1, 1, 1)  // Sun
                )
                val colors = listOf(Color(0xFFE1FAF9), Color(0xFF4FD1C5), Color(0xFF319795), Color(0xFF0D9488))
                
                days.forEachIndexed { i, day ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(day, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color(0xFF6B7280))
                        heatmapData[i].forEach { value ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .padding(end = 4.dp)
                                    .background(colors[value - 1], RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Low · · · High · relative recorded minutes", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3E8FF), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row {
                Text("☼", color = Color(0xFF7158E2), fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                Text("Best observed window: 09:00–11:00. More recorded focus, not a measure of work quality.", fontSize = 14.sp, color = Color(0xFF1E1E24))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recorded: session minutes. Inferred: a useful time window. Focus time alone does not establish productivity.", fontSize = 12.sp, color = Color(0xFF9CA3AF))
    }
}