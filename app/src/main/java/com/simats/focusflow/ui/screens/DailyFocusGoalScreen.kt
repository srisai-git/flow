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
fun DailyFocusGoalScreen(onBack: () -> Unit = {}, onSave: () -> Unit = {}) {
    var target by remember { mutableStateOf("3h 30m") }
    var block by remember { mutableStateOf("45 min") }
    var startHour by remember { mutableStateOf("09:00") }
    var endHour by remember { mutableStateOf("17:00") }
    
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    var selectedDays by remember { mutableStateOf(setOf(0, 1, 2, 3, 4)) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Daily focus goal", "YOUR ROUTINE", onBack)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Choose a realistic rhythm. You can adjust it as your day changes.", fontSize = 16.sp, color = Color(0xFF6B7280), lineHeight = 24.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Daily target", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFF7158E2), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Matches your current daily goal", fontSize = 12.sp, color = Color(0xFF10B981)) // Emerald
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
            Column {
                Text("Preferred focus block", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(16.dp))
                SegmentedControl(options = listOf("25 min", "45 min", "60 min"), selectedOption = block, onOptionSelected = { block = it })
                Spacer(modifier = Modifier.height(12.dp))
                Text("Current default: 45 minutes", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Daily working hours", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = startHour, onValueChange = { startHour = it },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFF7158E2), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )
            OutlinedTextField(
                value = endHour, onValueChange = { endHour = it },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor = Color(0xFF7158E2), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Active days", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEachIndexed { index, day ->
                val isSelected = selectedDays.contains(index)
                Box(
                    modifier = Modifier.size(40.dp).background(if (isSelected) Color(0xFFF0EDFF) else Color.White, RoundedCornerShape(12.dp)).border(1.dp, if (isSelected) Color(0xFF7158E2) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp)).clickable {
                        val newSet = selectedDays.toMutableSet()
                        if (isSelected) newSet.remove(index) else newSet.add(index)
                        selectedDays = newSet
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(day, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color(0xFF1E1E24) else Color(0xFF6B7280))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Monday–Friday selected", fontSize = 12.sp, color = Color(0xFF6B7280))
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
            Text("Goal progress uses focused minutes ÷ daily target. Home shows a rounded percentage.", fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)), shape = RoundedCornerShape(16.dp)) {
            Text("Save changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), border = BorderStroke(1.dp, Color(0xFFE5E7EB)), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))) {
            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}