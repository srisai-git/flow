package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.bounceClick
import kotlin.math.roundToInt

private val CanvasDark = Color(0xFF0F0F14)
private val CardDark = Color(0xFF181722)
private val BorderDark = Color(0xFF262433)
private val VioletPrimary = Color(0xFF8B5CF6)
private val VioletLight = Color(0xFFC4B5FD)
private val TextWhite = Color(0xFFF1F1F5)
private val TextMuted = Color(0xFF9CA3AF)

@Composable
fun DailyFocusGoalScreen(onBack: () -> Unit = {}, onSave: () -> Unit = {}) {
    var goalHours by remember { mutableFloatStateOf(4f) }
    var selectedRhythm by remember { mutableIntStateOf(45) }
    val rhythmOptions = listOf(25, 45, 60)

    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    var selectedDays by remember { mutableStateOf(setOf(0, 1, 2, 3, 4)) }

    Scaffold(
        containerColor = CanvasDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(CanvasDark)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, BorderDark, CircleShape)
                        .bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "FOCUS PREFERENCES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletLight,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Daily Target & Rhythm",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Configure your target focus hours and interval cadence to maintain deep work rhythm across your week.",
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // DAILY TARGET CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily Target Goal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        Text("${goalHours.roundToInt()} hours / day", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VioletLight)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = goalHours,
                        onValueChange = { goalHours = it },
                        valueRange = 1f..12f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = VioletPrimary,
                            activeTrackColor = VioletPrimary,
                            inactiveTrackColor = Color(0xFF262433)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SESSION CADENCE CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("PREFERRED FOCUS BLOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VioletLight, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rhythmOptions.forEach { rhythm ->
                            val isSelected = selectedRhythm == rhythm
                            Surface(
                                onClick = { selectedRhythm = rhythm },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) VioletPrimary else CanvasDark,
                                border = BorderStroke(1.dp, if (isSelected) VioletPrimary else BorderDark),
                                modifier = Modifier
                                    .weight(1f)
                                    .bounceClick()
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${rhythm}m",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = when (rhythm) {
                                            25 -> "Pomodoro"
                                            45 -> "Standard"
                                            else -> "Deep Block"
                                        },
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACTIVE WORK DAYS
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("ACTIVE FOCUS DAYS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VioletLight, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEachIndexed { index, day ->
                            val isSelected = selectedDays.contains(index)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) VioletPrimary else CanvasDark)
                                    .border(1.dp, if (isSelected) VioletPrimary else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val newSet = selectedDays.toMutableSet()
                                        if (isSelected) newSet.remove(index) else newSet.add(index)
                                        selectedDays = newSet
                                    }
                                    .bounceClick(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // SAVE BUTTON
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Preferences", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
