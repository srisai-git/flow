package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDistractionScreen(
    onBack: () -> Unit = {},
    onResume: () -> Unit = {}
) {
    var notes by remember { mutableStateOf("Opened Instagram after a notification.") }
    var selectedCategory by remember { mutableStateOf("Social Media") }
    var timeAway by remember { mutableStateOf("< 1 min") }
    
    val categories = listOf("Social Media", "Notifications", "Gaming", "Browsing", "People", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Log distraction", "A QUICK CHECK-IN, NOT A SETBACK", onBack)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .background(Color(0xFFFFF9E6), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("Timer paused · 24:18 remaining", color = Color(0xFFB45309), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("What pulled your attention?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notice it, name it, then return to your task.", fontSize = 14.sp, color = Color(0xFF6B7280))
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Categories Grid (3 rows of 2)
                for (row in 0 until 3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (col in 0 until 2) {
                            val cat = categories[row * 2 + col]
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .background(if (isSelected) Color(0xFFF0EDFF) else Color.White, RoundedCornerShape(16.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF7158E2) else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(12.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(cat, fontSize = 14.sp, fontWeight = if(isSelected) FontWeight.Medium else FontWeight.Normal, color = Color(0xFF1E1E24))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Notes · optional", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF7158E2),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Time away from your task", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedControl(
                    options = listOf("< 1 min", "1-5 min", "> 5 min"),
                    selectedOption = timeAway,
                    onOptionSelected = { timeAway = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Interruption time is excluded from focused minutes.", fontSize = 12.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onResume,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Log Distraction & Resume", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))
        ) {
            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}