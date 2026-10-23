package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun TaskDetailsScreen(onBack: () -> Unit = {}, onStartFocus: () -> Unit = {}, onEditTask: () -> Unit = {}) {
    var isComplete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        CustomTopBar("Task details", "TODAY · MONDAY, 12 OCT", onBack)
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.background(Color(0xFFF0EDFF), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Study", color = Color(0xFF7158E2), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Box(modifier = Modifier.background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("High priority", color = Color(0xFFB91C1C), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Study machine learning", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Description", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Review classification metrics and solve 3 practice questions.", fontSize = 16.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Scheduled", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("12 Oct 2026 · 10:00", fontSize = 14.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated duration", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("45 min", fontSize = 14.sp, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Status", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Text("Not started", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isComplete = !isComplete }) {
                    Checkbox(checked = isComplete, onCheckedChange = { isComplete = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7158E2)))
                    Text("Mark task complete", fontSize = 16.sp, color = Color(0xFF1E1E24))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF3E8FF), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("One task, one focus block", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(8.dp))
                Text("The 45-minute task estimate is separate from the duration you choose for a focus session.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onStartFocus,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Start focus", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onEditTask,
                modifier = Modifier.weight(1f).height(56.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24))
            ) {
                Text("Edit task", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFB91C1C))
            ) {
                Text("Delete task", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}