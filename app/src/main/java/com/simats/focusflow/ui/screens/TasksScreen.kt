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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.BottomNavBar
import com.simats.focusflow.ui.components.SegmentedControl
import com.simats.focusflow.ui.components.TaskItem

@Composable
fun TasksScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToProgressConsistency: () -> Unit = {},
    onNavigateToEditTask: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        bottomBar = { 
            BottomNavBar(
                selectedItem = "Tasks",
                onNavigate = { 
                    if (it == "Home") onNavigateToHome() 
                    if (it == "Insights") onNavigateToInsights()
                    if (it == "Profile") onNavigateToProfile()
                }
            ) 
        },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                            text = "Your tasks",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E24)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A CLEAR PLAN, ONE TASK AT A TIME",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                SegmentedControl(
                    options = listOf("All", "Active", "Completed"),
                    selectedOption = selectedTab,
                    onOptionSelected = { selectedTab = it }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tasks and sessions", color = Color(0xFF6B7280)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF7158E2),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Today Section (Shown on "All" and "Active")
                if (selectedTab == "All" || selectedTab == "Active") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Today · 3", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("12 Oct", fontSize = 14.sp, color = Color(0xFF7158E2))
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
                            TaskItem("Study machine learning", "Study · 45 min · 10:00", "High", onClick = onNavigateToEditTask)
                            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                            TaskItem("Finish project UI", "Creative · 60 min · 14:00", "Medium", onClick = onNavigateToEditTask)
                            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                            TaskItem("Practice Python", "Coding · 25 min · 17:00", "Low", onClick = onNavigateToEditTask)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Upcoming Section (Shown on "All" and "Active")
                if (selectedTab == "All" || selectedTab == "Active") {
                    Text("Upcoming · 2", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column {
                            TaskItem("Read model evaluation notes", "Study · 30 min · Tomorrow", "Medium", onClick = onNavigateToEditTask)
                            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                            TaskItem("Prepare presentation slides", "Work · 45 min · 14 Oct", "High", onClick = onNavigateToEditTask)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Completed Section (Shown on "All" and "Completed")
                if (selectedTab == "All" || selectedTab == "Completed") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProgressConsistency() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Completed today · 0", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                        Text("View stats >", fontSize = 12.sp, color = Color(0xFF7158E2), fontWeight = FontWeight.Medium)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                            .clickable { onNavigateToProgressConsistency() }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                drawCircle(Color(0xFFE5E7EB), style = Stroke(2.dp.toPx()))
                                val color = Color(0xFF9CA3AF)
                                drawLine(color, Offset(size.width * 0.3f, size.height * 0.5f), Offset(size.width * 0.45f, size.height * 0.65f), strokeWidth = 2.dp.toPx())
                                drawLine(color, Offset(size.width * 0.45f, size.height * 0.65f), Offset(size.width * 0.7f, size.height * 0.35f), strokeWidth = 2.dp.toPx())
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("No completed tasks yet today", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap here to check your overall Progress & Consistency.", fontSize = 14.sp, color = Color(0xFF7158E2))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            // Floating Action Button
            FloatingActionButton(
                onClick = onNavigateToEditTask,
                containerColor = Color(0xFF7158E2),
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 100.dp)
            ) {
                Text("+", fontSize = 24.sp, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    TasksScreen()
}