package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onNavigateNext: () -> Unit = {}, onBack: () -> Unit = {}) {
    val colors = FocusFlowTheme.colors
    var name by remember { mutableStateOf("") }
    var dailyGoal by remember { mutableStateOf("2h 00m") }
    var reminderTime by remember { mutableStateOf("09:00 AM") }
    var remindersEnabled by remember { mutableStateOf(true) }
    var selectedDuration by remember { mutableStateOf("45 min") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar(
            title = "Make it yours",
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Let's shape a routine that works for you.",
            fontSize = 14.sp,
            color = colors.textMuted
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Name Input
        Text(text = "Name", fontSize = 13.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Your name", color = colors.textMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colors.border,
                focusedBorderColor = colors.primary,
                unfocusedContainerColor = colors.surface,
                focusedContainerColor = colors.surface,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Primary Goal Input
        Text(text = "Primary goal", fontSize = 13.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = "Study",
            onValueChange = { },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colors.border,
                focusedBorderColor = colors.primary,
                unfocusedContainerColor = colors.surface,
                focusedContainerColor = colors.surface,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            )
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Preferred focus duration
        Text(text = "Preferred focus duration", fontSize = 13.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("25 min", "45 min", "60 min").forEach { option ->
                val isSelected = option == selectedDuration
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.primary else colors.border,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = if (isSelected) colors.primary.copy(alpha = 0.18f) else colors.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedDuration = option }
                        .bounceClick(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) colors.primaryLight else colors.textPrimary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Daily focus goal Input
        Text(text = "Daily focus goal", fontSize = 13.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = dailyGoal,
            onValueChange = { dailyGoal = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colors.border,
                focusedBorderColor = colors.primary,
                unfocusedContainerColor = colors.surface,
                focusedContainerColor = colors.surface,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Reminders
        Text(text = "Reminders", fontSize = 13.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = { remindersEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.primary,
                        uncheckedThumbColor = colors.textMuted,
                        uncheckedTrackColor = colors.surface
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Reminders enabled", fontSize = 14.sp, color = colors.textPrimary)
                Spacer(modifier = Modifier.weight(1f))
                
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    modifier = Modifier.width(110.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.border,
                        focusedBorderColor = colors.primary,
                        unfocusedContainerColor = colors.background,
                        focusedContainerColor = colors.background,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 13.sp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNavigateNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Continue", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetupScreenPreview() {
    SetupScreen(onNavigateNext = {})
}
