package com.simats.focusflow.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.MakeItYoursState
import com.simats.focusflow.ui.viewmodel.MakeItYoursViewModel
import kotlin.math.roundToInt

private val BackgroundLight = Color(0xFFFFFFFF)
private val SurfaceCardLight = Color(0xFFF8FAFC)
private val SurfaceCardActiveLight = Color(0xFFEDE9FE)
private val BorderInactiveLight = Color(0xFFE2E8F0)
private val PrimaryViolet = Color(0xFF7C3AED)
private val TextPrimaryLight = Color(0xFF0F172A)
private val TextSecondaryLight = Color(0xFF475569)

@Composable
fun MakeItYoursScreen(
    userId: Int = 1,
    onEnterDashboard: () -> Unit = {},
    viewModel: MakeItYoursViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initSessionManager(SessionManager.getInstance(context))
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

    val focusStyles = listOf(
        FocusStyleOption(
            title = "Pomodoro 25/5",
            badge = "Classic Rhythm",
            description = "25m focus • 5m recharge. Ideal for steady pace & avoiding fatigue."
        ),
        FocusStyleOption(
            title = "Deep Session 50/10",
            badge = "Extended Flow",
            description = "50m deep work • 10m recharge. Optimal for complex, uninterrupted tasks."
        ),
        FocusStyleOption(
            title = "Flow State",
            badge = "Open Immersion",
            description = "Continuous open-ended timer with soft milestone chimes."
        )
    )

    Scaffold(
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryViolet.copy(alpha = 0.12f))
                    .border(1.dp, PrimaryViolet.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "STEP 2 OF 2 • CUSTOMIZE",
                    color = PrimaryViolet,
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
                color = TextPrimaryLight,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tailor your focus environment to match how your mind works best.",
                fontSize = 15.sp,
                color = TextSecondaryLight,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionLabel(title = "PRIMARY FOCUS AREA")
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
                        placeholder = { Text("Specify your focus area (e.g. Research, Writing)", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryLight,
                            unfocusedTextColor = TextPrimaryLight,
                            focusedContainerColor = SurfaceCardLight,
                            unfocusedContainerColor = SurfaceCardLight,
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = BorderInactiveLight
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionLabel(title = "DAILY TARGET HOURS")
            Spacer(modifier = Modifier.height(12.dp))

            DailyGoalCard(
                hours = state.dailyGoalHours,
                onHoursChanged = { viewModel.onDailyGoalHoursChanged(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionLabel(title = "SESSION RHYTHM")
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                focusStyles.forEach { option ->
                    val isSelected = state.preferredFocusStyle == option.title
                    FocusStyleCard(
                        option = option,
                        isSelected = isSelected,
                        onClick = { viewModel.onFocusStyleSelected(option.title) }
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
                    containerColor = PrimaryViolet,
                    disabledContainerColor = PrimaryViolet.copy(alpha = 0.5f)
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
                        Text(
                            text = "→",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryViolet,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun FocusAreaChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryViolet else BorderInactiveLight,
        label = "chipBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) SurfaceCardActiveLight else SurfaceCardLight,
        label = "chipBg"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .bounceClick(),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, borderColor)
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
                color = if (isSelected) PrimaryViolet else TextSecondaryLight,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(PrimaryViolet, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyGoalCard(
    hours: Int,
    onHoursChanged: (Int) -> Unit
) {
    val subtitle = when {
        hours <= 2 -> "Gentle baseline to build sustainable momentum"
        hours in 3..5 -> "Recommended: Optimal for consistent daily deep work"
        hours in 6..8 -> "High intensity: Recommended for intense project sprints"
        else -> "Extreme immersion: Be sure to schedule regular rest"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderInactiveLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Target Time",
                        fontSize = 13.sp,
                        color = TextSecondaryLight,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$hours ${if (hours == 1) "Hour" else "Hours"} / day",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryLight
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryViolet.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${hours * 60}m goal",
                        color = PrimaryViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (hours in 3..5) PrimaryViolet else TextSecondaryLight,
                fontWeight = if (hours in 3..5) FontWeight.SemiBold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = hours.toFloat(),
                onValueChange = { onHoursChanged(it.roundToInt()) },
                valueRange = 1f..12f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryViolet,
                    activeTrackColor = PrimaryViolet,
                    inactiveTrackColor = BorderInactiveLight,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1 hr",
                    fontSize = 11.sp,
                    color = TextSecondaryLight,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "6 hrs",
                    fontSize = 11.sp,
                    color = TextSecondaryLight,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "12 hrs",
                    fontSize = 11.sp,
                    color = TextSecondaryLight,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private data class FocusStyleOption(
    val title: String,
    val badge: String,
    val description: String
)

@Composable
private fun FocusStyleCard(
    option: FocusStyleOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryViolet else BorderInactiveLight,
        label = "styleBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) SurfaceCardActiveLight else SurfaceCardLight,
        label = "styleBg"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isSelected) PrimaryViolet else Color(0xFFCBD5E1), CircleShape)
                    .background(if (isSelected) PrimaryViolet else Color.Transparent),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = option.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryViolet.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = option.badge,
                            color = if (isSelected) PrimaryViolet else Color(0xFF64748B),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = option.description,
                    fontSize = 12.sp,
                    color = TextSecondaryLight,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MakeItYoursScreenPreview() {
    MakeItYoursScreen()
}
