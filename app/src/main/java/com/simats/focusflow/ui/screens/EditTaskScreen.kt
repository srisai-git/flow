package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.SegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(onBack: () -> Unit = {}) {
    var title by remember { mutableStateOf("Study machine learning") }
    var description by remember { mutableStateOf("Review classification metrics and solve 3 practice questions.") }
    var duration by remember { mutableStateOf("45 min") }
    var dueDate by remember { mutableStateOf("12 Oct 2026") }
    var priority by remember { mutableStateOf("High") }
    var reminderEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        CustomTopBar(
            title = "Edit task",
            subtitle = "TASK DETAILS",
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Task Title
        Text(text = "Task title", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2)
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Title looks good · ${title.length} / 80 characters",
            fontSize = 12.sp,
            color = Color(0xFF10B981) // Emerald green
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Description
        Text(text = "Description", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Category
        Text(text = "Category", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "Study",
            onValueChange = { },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2)
            ),
            trailingIcon = {
                Canvas(modifier = Modifier.size(24.dp).padding(end = 12.dp)) {
                    val stroke = 2.dp.toPx()
                    drawLine(
                        color = Color(0xFF6B7280),
                        start = Offset(0f, size.height * 0.4f),
                        end = Offset(size.width / 2, size.height * 0.6f),
                        strokeWidth = stroke
                    )
                    drawLine(
                        color = Color(0xFF6B7280),
                        start = Offset(size.width / 2, size.height * 0.6f),
                        end = Offset(size.width, size.height * 0.4f),
                        strokeWidth = stroke
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Priority
        Text(text = "Priority", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        SegmentedControl(
            options = listOf("Low", "Medium", "High"),
            selectedOption = priority,
            onOptionSelected = { priority = it }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Duration and Due Date Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Estimated duration", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF7158E2)
                    ),
                    singleLine = true
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Due date", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = Color(0xFF7158E2)
                    ),
                    singleLine = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Reminder Toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Reminder", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1E24))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Today at 09:50 · 10 min before", fontSize = 12.sp, color = Color(0xFF6B7280))
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF7158E2)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "A reminder is set before your 10:00 study slot.",
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save Task", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E1E24)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditTaskScreenPreview() {
    EditTaskScreen()
}