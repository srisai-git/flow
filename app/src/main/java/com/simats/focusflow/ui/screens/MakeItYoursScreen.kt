package com.simats.focusflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.MakeItYoursViewModel

private val BackgroundObsidian = Color(0xFF0B0A10)
private val SurfaceCardObsidian = Color(0xFF13111C)
private val BorderInactive = Color(0xFF262338)
private val AccentViolet = Color(0xFF7C3AED)
private val AccentElectricViolet = Color(0xFF8B5CF6)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF94A3B8)

private data class DailyGoalOption(
    val id: String,
    val title: String,
    val description: String,
    val goalHours: Int,
    val defaultInterval: Int,
    val badge: String
)

@Composable
fun MakeItYoursScreen(
    userId: Int = 1,
    onEnterDashboard: () -> Unit = {},
    viewModel: MakeItYoursViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initSessionManager(context, SessionManager.getInstance(context))
    }

    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    val state by viewModel.uiState.collectAsState()
    var customFocusArea by remember { mutableStateOf("") }

    val standardAreas = listOf(
        "Coding & Projects",
        "Study & Academics",
        "Creative Work",
        "Deep Work"
    )
    val allOptions = standardAreas + "Others"
    val isOthersSelected = state.selectedFocusArea == "Others" || (!standardAreas.contains(state.selectedFocusArea) && state.selectedFocusArea.isNotBlank())

    val dailyGoalOptions = remember {
        listOf(
            DailyGoalOption(
                id = "2h",
                title = "2 Hours / Day",
                description = "Light & consistent daily habit",
                goalHours = 2,
                defaultInterval = 25,
                badge = "25m timer"
            ),
            DailyGoalOption(
                id = "3h",
                title = "3 Hours / Day",
                description = "Balanced study & project workflow",
                goalHours = 3,
                defaultInterval = 45,
                badge = "45m timer"
            ),
            DailyGoalOption(
                id = "5h",
                title = "5 Hours / Day",
                description = "Intensive exam & deep-work sprint",
                goalHours = 5,
                defaultInterval = 50,
                badge = "50m timer"
            ),
            DailyGoalOption(
                id = "custom",
                title = "Custom Goal",
                description = "Adjust anytime in your workspace",
                goalHours = 4,
                defaultInterval = 30,
                badge = "30m timer"
            )
        )
    }

    Scaffold(
        containerColor = BackgroundObsidian
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundObsidian)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentViolet.copy(alpha = 0.16f))
                    .border(1.dp, AccentElectricViolet.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "STEP 2 OF 2 • CUSTOMIZE",
                    color = Color(0xFFC4B5FD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "MAKE IT YOURS",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tailor your focus environment to match how your mind works best.",
                fontSize = 15.sp,
                color = TextSecondary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "PRIMARY FOCUS AREA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC4B5FD),
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (chunk in allOptions.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (area in chunk) {
                            val isSelected = if (area == "Others") isOthersSelected else state.selectedFocusArea == area
                            FocusAreaChip(
                                label = area,
                                isSelected = isSelected,
                                onClick = {
                                    if (area == "Others") {
                                        viewModel.onFocusAreaSelected(if (customFocusArea.isNotBlank()) customFocusArea else "Others")
                                    } else {
                                        viewModel.onFocusAreaSelected(area)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (chunk.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (isOthersSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customFocusArea,
                        onValueChange = {
                            customFocusArea = it
                            viewModel.onFocusAreaSelected(if (it.isNotBlank()) it else "Others")
                        },
                        placeholder = { Text("Specify your focus area (e.g. Research, Writing)", color = Color(0xFF64748B), fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceCardObsidian,
                            unfocusedContainerColor = SurfaceCardObsidian,
                            focusedBorderColor = AccentElectricViolet,
                            unfocusedBorderColor = BorderInactive
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column {
                Text(
                    text = "Set Your Daily Focus Goal",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We'll calibrate your Focus Readiness ring and daily targets.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dailyGoalOptions.forEach { option ->
                    val isSelected = state.selectedGoalId == option.id || (state.selectedGoalId.isBlank() && option.id == "3h")
                    DailyFocusGoalCard(
                        option = option,
                        isSelected = isSelected,
                        onClick = {
                            viewModel.onSelectDailyGoal(option.id, option.goalHours, option.defaultInterval)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    viewModel.onSavePreferences {
                        onEnterDashboard()
                    }
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentViolet,
                    disabledContainerColor = AccentViolet.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Enter FocusFlow",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FocusAreaChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AccentElectricViolet else BorderInactive,
        label = "chipBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AccentViolet.copy(alpha = 0.16f) else SurfaceCardObsidian,
        label = "chipBg"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .bounceClick(),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFFC4B5FD) else TextSecondary,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(AccentViolet, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyFocusGoalCard(
    option: DailyGoalOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AccentElectricViolet else BorderInactive,
        label = "goalBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AccentViolet.copy(alpha = 0.14f) else SurfaceCardObsidian,
        label = "goalBg"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isSelected) AccentElectricViolet else Color(0xFF3B3554), CircleShape)
                    .background(if (isSelected) AccentViolet else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = option.description,
                    fontSize = 12.5.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AccentViolet.copy(alpha = 0.22f) else Color(0xFF1E1B2E))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = option.badge,
                    color = if (isSelected) Color(0xFFC4B5FD) else Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
