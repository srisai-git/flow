package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onNavigateNext: () -> Unit = {}, onBack: () -> Unit = {}) {
    var name by remember { mutableStateOf("Sri Sai") }
    var dailyGoal by remember { mutableStateOf("3h 30m") }
    var reminderTime by remember { mutableStateOf("09:00 AM") }
    var remindersEnabled by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        // Top Bar
        CustomTopBar(
            title = "Make it yours",
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Let's shape a routine that works for you.",
            fontSize = 16.sp,
            color = Color(0xFF6B7280)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Name Input
        Text(text = "Name", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Primary Goal Input
        Text(text = "Primary goal", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
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
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Preferred focus duration
        Text(text = "Preferred focus duration", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("25 min", "45 min", "60 min").forEach { option ->
                val isSelected = option == "45 min"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF7158E2) else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = if (isSelected) Color(0xFFF0EDFF) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) Color(0xFF7158E2) else Color(0xFF6B7280),
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Daily focus goal Input
        Text(text = "Daily focus goal", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = dailyGoal,
            onValueChange = { dailyGoal = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = Color(0xFF7158E2)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Reminders
        Text(text = "Reminders", fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = remindersEnabled,
                onCheckedChange = { remindersEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF7158E2)
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Reminders enabled", fontSize = 14.sp, color = Color(0xFF1E1E24))
            Spacer(modifier = Modifier.weight(1f))
            
            // Time Input
            OutlinedTextField(
                value = reminderTime,
                onValueChange = { reminderTime = it },
                modifier = Modifier.width(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedBorderColor = Color(0xFF7158E2)
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNavigateNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetupScreenPreview() {
    SetupScreen(onNavigateNext = {})
}