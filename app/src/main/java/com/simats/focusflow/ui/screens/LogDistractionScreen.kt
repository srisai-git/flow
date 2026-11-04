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
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDistractionScreen(
    onBack: () -> Unit = {},
    onResume: () -> Unit = {}
) {
    val colors = FocusFlowTheme.colors
    var notes by remember { mutableStateOf("Opened Instagram after a notification.") }
    var selectedCategory by remember { mutableStateOf("Social Media") }
    var timeAway by remember { mutableStateOf("< 1 min") }
    
    val categories = listOf("Social Media", "Notifications", "Gaming", "Browsing", "People", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar("Log distraction", "A QUICK CHECK-IN, NOT A SETBACK", onBack)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(
            modifier = Modifier
                .background(colors.primary.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                .border(1.dp, colors.primary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                "Timer paused · 24:18 remaining",
                color = colors.primaryLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "What pulled your attention?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Notice it, name it, then return to your task.",
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Categories Grid (3 rows of 2)
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0 until 2) {
                            val cat = categories[row * 2 + col]
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(72.dp)
                                    .background(
                                        if (isSelected) colors.primary.copy(alpha = 0.18f) else colors.background,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) colors.primary else colors.border,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(12.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(
                                    cat,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) colors.primaryLight else colors.textPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Notes · optional", fontSize = 13.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.border,
                        focusedBorderColor = colors.primary,
                        unfocusedContainerColor = colors.background,
                        focusedContainerColor = colors.background,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text("Time away from your task", fontSize = 13.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedControl(
                    options = listOf("< 1 min", "1-5 min", "> 5 min"),
                    selectedOption = timeAway,
                    onOptionSelected = { timeAway = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Interruption time is excluded from focused minutes.",
            fontSize = 12.sp,
            color = colors.textMuted
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onResume,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                "Log Distraction & Resume",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            border = BorderStroke(1.dp, colors.border),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
        ) {
            Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
