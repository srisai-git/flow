package com.simats.focusflow.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.viewmodel.MakeItYoursState
import com.simats.focusflow.ui.viewmodel.MakeItYoursViewModel
import kotlin.math.roundToInt

// Custom Dark Minimalist Colors
private val BackgroundDark = Color(0xFF0F0F14)
private val SurfaceCard = Color(0xFF181722)
private val SurfaceCardActive = Color(0xFF221F33)
private val BorderInactive = Color(0xFF272535)
private val VioletAccent = Color(0xFF8B5CF6)
private val VioletMuted = Color(0xFFC4B5FD)
private val TextPrimary = Color(0xFFF9FAFB)
private val TextSecondary = Color(0xFFA1A1AA)

/**
 * "Make It Yours" Onboarding Customization Screen for FocusFlow.
 * Captures user focus areas, daily target slider (1-12h), and session rhythm.
 */
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

    val focusAreas = listOf(
        "Coding & Projects",
        "Study & Academics",
        "Creative Work",
        "Deep Work"
    )

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
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Step Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                    .border(1.dp, VioletAccent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "STEP 2 OF 2 • CUSTOMIZE",
                    color = VioletMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Header
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

            Spacer(modifier = Modifier.height(32.dp))

            // 1. PRIMARY FOCUS AREA SECTION
            SectionLabel(title = "PRIMARY FOCUS AREA")
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (chunk in focusAreas.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (area in chunk) {
                            val isSelected = state.selectedFocusArea == area
                            FocusAreaChip(
                                label = area,
                                isSelected = isSelected,
                                onClick = { viewModel.onFocusAreaSelected(area) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. DAILY TARGET HOURS SLIDER SECTION
            SectionLabel(title = "DAILY TARGET HOURS")
            Spacer(modifier = Modifier.height(12.dp))

            DailyGoalCard(
                hours = state.dailyGoalHours,
                onHoursChanged = { viewModel.onDailyGoalHoursChanged(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. SESSION RHYTHM SECTION
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

            // CTA Action Button
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
                    containerColor = VioletAccent,
                    disabledContainerColor = VioletAccent.copy(alpha = 0.5f)
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
        color = VioletMuted,
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
        targetValue = if (isSelected) VioletAccent else BorderInactive,
        label = "chipBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) SurfaceCardActive else SurfaceCard,
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
                color = if (isSelected) TextPrimary else TextSecondary,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(VioletAccent, CircleShape),
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
        color = SurfaceCard,
        border = BorderStroke(1.dp, BorderInactive)
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
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$hours ${if (hours == 1) "Hour" else "Hours"} / day",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(VioletAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${hours * 60}m goal",
                        color = VioletMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (hours in 3..5) VioletMuted else TextSecondary,
                fontWeight = if (hours in 3..5) FontWeight.SemiBold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = hours.toFloat(),
                onValueChange = { onHoursChanged(it.roundToInt()) },
                valueRange = 1f..12f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = VioletAccent,
                    activeTrackColor = VioletAccent,
                    inactiveTrackColor = BorderInactive,
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
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "6 hrs",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "12 hrs",
                    fontSize = 11.sp,
                    color = TextSecondary,
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
        targetValue = if (isSelected) VioletAccent else BorderInactive,
        label = "styleBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) SurfaceCardActive else SurfaceCard,
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
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = option.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) VioletAccent.copy(alpha = 0.25f)
                                else Color(0xFF272535)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = option.badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) VioletMuted else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = option.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Radio indicator
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) VioletAccent else BorderInactive,
                        shape = CircleShape
                    )
                    .background(
                        if (isSelected) VioletAccent else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MakeItYoursScreenPreview() {
    MakeItYoursScreen()
}

