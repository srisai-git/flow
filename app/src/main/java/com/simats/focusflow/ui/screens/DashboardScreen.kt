package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.BottomNavBar
import com.simats.focusflow.ui.components.TaskItem

@Composable
fun DashboardScreen(
    onNavigateToTasks: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSetIntention: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSessionHistory: () -> Unit = {},
    onNavigateToSearchFilters: () -> Unit = {},
    onNavigateToDataAi: () -> Unit = {}
) {
    Scaffold(
        bottomBar = { 
            BottomNavBar(
                selectedItem = "Home",
                onNavigate = { 
                    if (it == "Tasks") onNavigateToTasks() 
                    if (it == "Insights") onNavigateToInsights()
                    if (it == "Profile") onNavigateToProfile()
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
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning, Sai",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E24)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MONDAY, 12 OCT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }
                
                // Bell Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF0EDFF), CircleShape)
                        .clickable { onNavigateToNotifications() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val stroke = 2.dp.toPx()
                        val color = Color(0xFF7158E2)
                        drawArc(
                            color = color,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                            size = Size(size.width * 0.6f, size.height * 0.6f),
                            style = Stroke(stroke)
                        )
                        drawLine(color, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.2f, size.height * 0.7f), strokeWidth = stroke)
                        drawLine(color, Offset(size.width * 0.8f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.7f), strokeWidth = stroke)
                        drawLine(color, Offset(size.width * 0.1f, size.height * 0.7f), Offset(size.width * 0.9f, size.height * 0.7f), strokeWidth = stroke)
                        drawArc(
                            color = color,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.4f, size.height * 0.7f),
                            size = Size(size.width * 0.2f, size.height * 0.2f),
                            style = Stroke(stroke)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToSessionHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280)),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawCircle(Color(0xFF6B7280), style = Stroke(1.5.dp.toPx()))
                        drawLine(Color(0xFF6B7280), center, Offset(center.x, center.y - 4.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                        drawLine(Color(0xFF6B7280), center, Offset(center.x + 3.dp.toPx(), center.y + 3.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SESSION HISTORY", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                
                OutlinedButton(
                    onClick = onNavigateToSearchFilters,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280)),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawCircle(Color(0xFF6B7280), radius = 5.dp.toPx(), center = Offset(size.width*0.4f, size.height*0.4f), style = Stroke(1.5.dp.toPx()))
                        drawLine(Color(0xFF6B7280), Offset(size.width*0.6f, size.height*0.6f), Offset(size.width, size.height), strokeWidth = 1.5.dp.toPx())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SEARCH & FILTERS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Circular Progress
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(120.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color(0xFFF3F4F6),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = Color(0xFF7158E2),
                                    startAngle = -90f,
                                    sweepAngle = 360f * 0.65f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "65%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                                Text(text = "daily goal", fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        Column {
                            Text(text = "2h 15m", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                            Text(text = "focused today", fontSize = 14.sp, color = Color(0xFF6B7280))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "4 sessions completed", fontSize = 14.sp, color = Color(0xFF6B7280))
                            Text(text = "3 tasks remaining", fontSize = 14.sp, color = Color(0xFF6B7280))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Sample day · 3h 30m target · progress rounded\n(~65%).",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNavigateToSetIntention,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start Focus Session", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Tasks Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today's tasks", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Text(
                    text = "View all",
                    fontSize = 14.sp,
                    color = Color(0xFF7158E2),
                    modifier = Modifier.clickable { onNavigateToTasks() }.padding(4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column {
                    TaskItem("Study machine learning", "Study · 45 min", "High", onClick = onNavigateToTasks)
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    TaskItem("Finish project UI", "Creative · 60 min", "Medium", onClick = onNavigateToTasks)
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    TaskItem("Practice Python", "Coding · 25 min", "Low", onClick = onNavigateToTasks)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Sample data", fontSize = 12.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Inferred · based on last week", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Callout Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                    .clickable { onNavigateToDataAi() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val color = Color(0xFF7158E2)
                    val stroke = 2.dp.toPx()
                    drawLine(color, Offset(size.width/2, 0f), Offset(size.width/2, size.height), strokeWidth = stroke)
                    drawLine(color, Offset(0f, size.height/2), Offset(size.width, size.height/2), strokeWidth = stroke)
                    drawCircle(Color(0xFFF3F4F6), radius = 6.dp.toPx(), center = center)
                    drawCircle(color, radius = 4.dp.toPx(), center = center, style = Stroke(stroke))
                    // Sparkles
                    drawLine(color, Offset(size.width*0.2f, size.height*0.2f), Offset(size.width*0.3f, size.height*0.3f), strokeWidth = stroke)
                    drawLine(color, Offset(size.width*0.8f, size.height*0.2f), Offset(size.width*0.7f, size.height*0.3f), strokeWidth = stroke)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Personalized Analysis by Groot AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E24)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your recent morning sessions have been more consistent.",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}