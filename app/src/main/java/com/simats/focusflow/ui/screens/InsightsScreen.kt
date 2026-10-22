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
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text("Your productivity\npatterns", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), lineHeight = 40.sp)
                Canvas(modifier = Modifier.size(24.dp).padding(top = 8.dp)) {
                    val radius = 2.dp.toPx()
                    val c = Color(0xFF1E1E24)
                    drawCircle(c, radius, Offset(size.width*0.2f, size.height/2))
                    drawCircle(c, radius, Offset(size.width/2, size.height/2))
                    drawCircle(c, radius, Offset(size.width*0.8f, size.height/2))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(Color(0xFFE1FAF9), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Sample data", color = Color(0xFF0D9488), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("05–11 Oct 2026", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SegmentedControl(options = listOf("Week", "Month"), selectedOption = selectedTab, onOptionSelected = { selectedTab = it })
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bar Chart Card
            Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column {
                            Text("12h 30m", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                            Text("recorded focus time", fontSize = 14.sp, color = Color(0xFF6B7280))
                        }
                        Text("Minutes / day", fontSize = 14.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Simple Bar Chart
                    val data = listOf(90, 120, 135, 150, 105, 90, 60)
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val maxVal = 150f
                    
                    Row(modifier = Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        data.forEachIndexed { index, value ->
                            val heightRatio = value / maxVal
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                                Text("$value", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.width(24.dp).fillMaxHeight(heightRatio).background(Color(0xFF4FD1C5), RoundedCornerShape(4.dp)))
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
                        Text("20", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("sessions this week", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("75%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("tasks · 18 of 24", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("37.5m", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("average duration", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Thu", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("best day · 2h 30m", fontSize = 12.sp, color = Color(0xFF6B7280))
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