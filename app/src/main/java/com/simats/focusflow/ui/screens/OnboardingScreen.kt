package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.R
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.DarkBackground
import com.simats.focusflow.ui.theme.DarkBorder
import com.simats.focusflow.ui.theme.DarkPrimary
import com.simats.focusflow.ui.theme.DarkPrimaryLight
import com.simats.focusflow.ui.theme.DarkSurface
import com.simats.focusflow.ui.theme.DarkSurfaceVariant
import com.simats.focusflow.ui.theme.DarkTextMuted
import com.simats.focusflow.ui.theme.DarkTextPrimary

/**
 * Modern minimalist Onboarding Screen for FocusFlow.
 * Aligned with official dark canvas (#0F0F14), elevated surface (#181722),
 * crisp light typography (#F1F1F5), and violet accents (#8B5CF6).
 * Zero emojis.
 */
@Composable
fun OnboardingScreen(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        // App Brand Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "FocusFlow Logo",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "FocusFlow",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                Text(
                    text = "Mindful Productivity",
                    fontSize = 11.sp,
                    color = DarkPrimaryLight,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Hero Abstract Time-Flow Geometry
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val strokeW = 6.dp.toPx()

                // Outer geometric ring
                drawCircle(
                    color = DarkBorder,
                    radius = size.width * 0.45f,
                    center = center,
                    style = Stroke(strokeW)
                )

                // Vibrant sweep arc
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(DarkPrimary, DarkPrimaryLight, DarkPrimary),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = 240f,
                    useCenter = false,
                    style = Stroke(strokeW, cap = StrokeCap.Round)
                )

                // Secondary inner orbital ring
                drawCircle(
                    color = DarkSurfaceVariant,
                    radius = size.width * 0.32f,
                    center = center,
                    style = Stroke(3.dp.toPx())
                )

                // Core pulse node
                drawCircle(
                    color = DarkPrimary.copy(alpha = 0.25f),
                    radius = size.width * 0.18f,
                    center = center
                )
                drawCircle(
                    color = DarkPrimary,
                    radius = 8.dp.toPx(),
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Build deep focus.\nMaster your daily flow.",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkTextPrimary,
            lineHeight = 38.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Structured Pomodoro intervals, habit consistency tracking, and intelligent AI coaching tailored to your rhythm.",
            fontSize = 14.sp,
            color = DarkTextMuted,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Elevated Feature Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OnboardingFeatureCard(
                icon = Icons.Default.HourglassBottom,
                title = "Focused Work Intervals",
                description = "Customizable Pomodoro timer with persistent background tracking."
            )
            OnboardingFeatureCard(
                icon = Icons.Default.AllInclusive,
                title = "Compounding Habits",
                description = "Daily routine matrices and authentic streak analytics."
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Get Started Action Button
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Get Started",
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

        Spacer(modifier = Modifier.height(18.dp))

        // Privacy Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = DarkTextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Your productivity records stay private and local.",
                fontSize = 12.sp,
                color = DarkTextMuted
            )
        }
    }
}

@Composable
private fun OnboardingFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkPrimary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkPrimaryLight,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = DarkTextMuted,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Backward compatibility alias for GetStartedScreen.
 */
@Composable
fun GetStartedScreen(onNavigateToLogin: () -> Unit) {
    OnboardingScreen(onNavigateToLogin = onNavigateToLogin)
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(onNavigateToLogin = {})
}
