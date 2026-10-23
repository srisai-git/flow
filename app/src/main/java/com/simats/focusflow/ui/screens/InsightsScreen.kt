package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.BottomNavBar
import com.simats.focusflow.ui.components.SegmentedControl

@Composable
fun InsightsScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFocusAnalysis: () -> Unit = {},
    onNavigateToDistraction: () -> Unit = {},
    onNavigateToAiReport: () -> Unit = {},
    onNavigateToProgressConsistency: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Week") }

    Scaffold(
        bottomBar = { 
            BottomNavBar(
                selectedItem = "Insights",
                onNavigate = {
                    when(it) {
                        "Home" -> onNavigateToHome()
                        "Tasks" -> onNavigateToTasks()
                        "Profile" -> onNavigateToProfile()
                    }
                }
            ) 
        },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text("Insights", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(if (selectedTab == "Week") "05–11 Oct 2026" else "October 2026", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SegmentedControl(options = listOf("Week", "Month"), selectedOption = selectedTab, onOptionSelected = { selectedTab = it })
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bar Chart Card
            Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column {
                            Text(if (selectedTab == "Week") "12h 30m" else "54h 15m", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                            Text(if (selectedTab == "Week") "recorded focus time this week" else "recorded focus time this month", fontSize = 14.sp, color = Color(0xFF6B7280))
                        }
                        Text(if (selectedTab == "Week") "Minutes / day" else "Minutes / week", fontSize = 14.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Simple Bar Chart
                    val data = if (selectedTab == "Week") listOf(90, 120, 135, 150, 105, 90, 60) else listOf(720, 915, 840, 780)
                    val days = if (selectedTab == "Week") listOf("M", "T", "W", "T", "F", "S", "S") else listOf("W1", "W2", "W3", "W4")
                    val maxVal = if (selectedTab == "Week") 150f else 915f
                    
                    Row(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        data.forEachIndexed { index, value ->
                            val heightRatio = value / maxVal
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                                Text(if (selectedTab == "Week") "$value m" else "${value/60}h", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.width(if (selectedTab == "Week") 24.dp else 40.dp).fillMaxHeight(heightRatio).background(Color(0xFF4FD1C5), RoundedCornerShape(4.dp)))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(days[index], fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Card
            Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (selectedTab == "Week") "20" else "84", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text(if (selectedTab == "Week") "sessions this week" else "sessions this month", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(if (selectedTab == "Week") "75%" else "82%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text(if (selectedTab == "Week") "tasks · 18 of 24" else "tasks · 78 of 95", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (selectedTab == "Week") "37.5m" else "38.7m", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("average duration", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(if (selectedTab == "Week") "Thu" else "W2", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text(if (selectedTab == "Week") "best day · 2h 30m" else "best week · 15h 15m", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Links Card
            Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(vertical = 8.dp)) {
                Column {
                    MenuRow("Focus Time Analysis", onClick = onNavigateToFocusAnalysis)
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    MenuRow("Distraction Analysis", onClick = onNavigateToDistraction)
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    MenuRow("AI Productivity Report", isSparkle = true, onClick = onNavigateToAiReport)
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    MenuRow("Progress & Consistency", onClick = onNavigateToProgressConsistency)
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun MenuRow(title: String, isSparkle: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val c = Color(0xFF6B7280)
                if (isSparkle) {
                    val s = 2.dp.toPx()
                    drawLine(c, Offset(size.width/2, 0f), Offset(size.width/2, size.height), strokeWidth = s)
                    drawLine(c, Offset(0f, size.height/2), Offset(size.width, size.height/2), strokeWidth = s)
                    drawCircle(Color.White, 4.dp.toPx(), center)
                    drawCircle(c, 3.dp.toPx(), center, style = Stroke(s))
                } else {
                    // generic pie chart icon
                    drawCircle(c, style = Stroke(2.dp.toPx()))
                    drawLine(c, center, Offset(size.width*0.8f, size.height*0.2f), strokeWidth = 2.dp.toPx())
                    drawLine(c, center, Offset(size.width*0.8f, size.height*0.8f), strokeWidth = 2.dp.toPx())
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
        }
        Canvas(modifier = Modifier.size(16.dp)) {
            val c = Color(0xFF9CA3AF)
            drawLine(c, Offset(size.width*0.3f, size.height*0.2f), Offset(size.width*0.7f, size.height*0.5f), strokeWidth = 2.dp.toPx())
            drawLine(c, Offset(size.width*0.7f, size.height*0.5f), Offset(size.width*0.3f, size.height*0.8f), strokeWidth = 2.dp.toPx())
        }
    }
}