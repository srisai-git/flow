package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.R
import com.simats.focusflow.ui.components.CustomTopBar
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(onBack: () -> Unit = {}) {
    var dailyReminder by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("09:00 AM") }
    var showTimeDropdown by remember { mutableStateOf(false) }
    var deadlineReminder by remember { mutableStateOf(true) }
    var breakReminder by remember { mutableStateOf(true) }
    var aiReportReminder by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val timeOptions = listOf("07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "06:00 PM", "08:00 PM")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CustomTopBar("Notifications & reminders", null, onBack)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Gentle prompts. Only when you want them.", fontSize = 14.sp, color = Color(0xFF6B7280))
            Spacer(modifier = Modifier.height(24.dp))
            
            // Daily Focus Reminder Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Daily focus reminder", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                            Text("A moment to choose your first task.", fontSize = 14.sp, color = Color(0xFF6B7280))
                        }
                        Switch(
                            checked = dailyReminder,
                            onCheckedChange = { dailyReminder = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7158E2))
                        )
                    }
                    
                    if (dailyReminder) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reminder time", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box {
                            OutlinedTextField(
                                value = reminderTime,
                                onValueChange = { reminderTime = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimeDropdown = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedBorderColor = Color(0xFF7158E2)
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { showTimeDropdown = !showTimeDropdown }) {
                                        Text("▼", color = Color(0xFF7158E2), fontSize = 10.sp)
                                    }
                                }
                            )
                            
                            DropdownMenu(
                                expanded = showTimeDropdown,
                                onDismissRequest = { showTimeDropdown = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .background(Color.White)
                            ) {
                                timeOptions.forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(time, fontSize = 14.sp, color = Color(0xFF1E1E24)) },
                                        onClick = {
                                            reminderTime = time
                                            showTimeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("08:00 AM", "09:00 AM", "10:00 AM", "08:00 PM").forEach { quickTime ->
                                val isSelected = reminderTime == quickTime
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) Color(0xFFF0EDFF) else Color(0xFFF3F4F6),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF7158E2) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { reminderTime = quickTime }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = quickTime,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color(0xFF7158E2) else Color(0xFF4B5563),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Other Reminders
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Deadline reminders", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                            Text("30 minutes before a task is due.", fontSize = 14.sp, color = Color(0xFF6B7280))
                        }
                        Switch(checked = deadlineReminder, onCheckedChange = { deadlineReminder = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7158E2)))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Break reminders", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                            Text("At the end of each focus block.", fontSize = 14.sp, color = Color(0xFF6B7280))
                        }
                        Switch(checked = breakReminder, onCheckedChange = { breakReminder = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7158E2)))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Weekly AI report", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                            Text("Sunday at 18:00 · summary report", fontSize = 14.sp, color = Color(0xFF6B7280))
                        }
                        Switch(checked = aiReportReminder, onCheckedChange = { aiReportReminder = it }, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7158E2)))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("NOTIFICATION PREVIEW", fontSize = 12.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notification Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFF070510), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Flow", fontSize = 14.sp, color = Color(0xFF1E1E24))
                        }
                        Text(if (dailyReminder) reminderTime else "Off", fontSize = 12.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("A little space for focus?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Your Study task is ready. Start when you are.", fontSize = 14.sp, color = Color(0xFF6B7280))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // System Notification Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = if (notificationsEnabled) "Notifications are enabled" else "Notifications are not enabled",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (notificationsEnabled) Color(0xFF10B981) else Color(0xFF1E1E24)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Allow focus and deadline reminders at your scheduled time.", fontSize = 14.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Text(
                            text = if (notificationsEnabled) "Configured ✓" else "Enable notifications",
                            color = Color(0xFF7158E2),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                notificationsEnabled = true
                                scope.launch {
                                    snackbarHostState.showSnackbar("Notifications enabled!")
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Daily focus reminder set for $reminderTime")
                    }
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save preferences", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen()
}