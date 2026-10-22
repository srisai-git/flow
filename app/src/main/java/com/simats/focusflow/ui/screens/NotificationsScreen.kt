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

@Composable
fun NotificationsScreen(onBack: () -> Unit = {}) {
    var dailyReminder by remember { mutableStateOf(true) }
    var deadlineReminder by remember { mutableStateOf(true) }
    var breakReminder by remember { mutableStateOf(true) }
    var aiReportReminder by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Notifications & reminders", null, onBack)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Gentle prompts. Only when you want them.", fontSize = 14.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily focus reminder", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                        Text("A moment to choose your first task.", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Switch(checked = dailyReminder, onCheckedChange = { dailyReminder = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7158E2)))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Reminder time", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "09:00 AM",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE5E7EB)),
                    trailingIcon = { Text("v", modifier = Modifier.padding(end=16.dp), color = Color(0xFF6B7280)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Deadline reminders", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                        Text("30 minutes before a task is due.", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Switch(checked = deadlineReminder, onCheckedChange = { deadlineReminder = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7158E2)))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Break reminders", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                        Text("At the end of each focus block.", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Switch(checked = breakReminder, onCheckedChange = { breakReminder = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7158E2)))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weekly AI report", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                        Text("Sunday at 18:00 · sample report", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                    Switch(checked = aiReportReminder, onCheckedChange = { aiReportReminder = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7158E2)))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("NOTIFICATION PREVIEW", fontSize = 12.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⛶", color = Color(0xFF7158E2), fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FocusFlow AI", fontSize = 14.sp, color = Color(0xFF1E1E24))
                    }
                    Text("09:00", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("A little space for focus?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Your Study task is ready. Start when you are.", fontSize = 14.sp, color = Color(0xFF6B7280))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
            Column {
                Text("Notifications are not enabled", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Allow focus and deadline reminders. Change this later in Android settings.", fontSize = 14.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Text("Enable notifications", color = Color(0xFF7158E2), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(32.dp))
                    Text("Not now", color = Color(0xFF7158E2), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)), shape = RoundedCornerShape(16.dp)) {
            Text("Save preferences", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}