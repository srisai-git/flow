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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.theme.DarkBackground
import com.simats.focusflow.ui.theme.DarkBorder
import com.simats.focusflow.ui.theme.DarkPrimary
import com.simats.focusflow.ui.theme.DarkPrimaryLight
import com.simats.focusflow.ui.theme.DarkSurface
import com.simats.focusflow.ui.theme.DarkSurfaceVariant
import com.simats.focusflow.ui.theme.DarkTextMuted
import com.simats.focusflow.ui.theme.DarkTextPrimary

/**
 * Branded dark Welcome / Get Started Screen for FocusFlow.
 * Adheres strictly to the unified dark theme (#0F0F14 canvas, #181722 surface cards,
 * #262433 borders, #8B5CF6 violet buttons, and zero emojis).
 */
@Composable
fun WelcomeStartScreen(
    onBack: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
    onLogin: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 44.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Subtle Brand Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
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
                    fontSize = 18.sp,
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

        Text(
            text = "Let's get started",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "A refined sanctuary for deep work, habit consistency, and personal clarity. Build a rhythm that feels authentic to you.",
            fontSize = 15.sp,
            color = DarkTextMuted,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Abstract Geometric Centerpiece
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f - 16.dp.toPx()

                    // Background orbit track
                    drawCircle(
                        color = Color(0xFF1E1C2B),
                        radius = radius,
                        style = Stroke(width = 8.dp.toPx())
                    )

                    // Secondary inner track
                    drawCircle(
                        color = Color(0xFF181722),
                        radius = radius - 20.dp.toPx(),
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Gradient active arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(DarkPrimary, DarkPrimaryLight, Color(0xFF6366F1), DarkPrimary)
                        ),
                        startAngle = -90f,
                        sweepAngle = 250f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Accent tick marker
                    drawCircle(
                        color = DarkPrimaryLight,
                        radius = 5.dp.toPx(),
                        center = Offset(
                            center.x + (radius) * kotlin.math.cos(Math.toRadians(160.0)).toFloat(),
                            center.y + (radius) * kotlin.math.sin(Math.toRadians(160.0)).toFloat()
                        )
                    )
                }

                // Center Icon / Emblem
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "FocusFlow Emblem",
                        modifier = Modifier.size(46.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Feature Highlight Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262433)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = DarkPrimaryLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Intentional flow sessions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextPrimary
                    )
                    Text(
                        text = "Real streaks, authentic metrics, and zero distractions.",
                        fontSize = 12.sp,
                        color = DarkTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Button(
            onClick = onCreateAccount,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Create an account",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = DarkSurface,
                contentColor = DarkTextPrimary
            )
        ) {
            Text(
                text = "Already have an account? Log in",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = DarkTextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Your focus records stay yours. Always.",
                fontSize = 12.sp,
                color = DarkTextMuted
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeStartScreenPreview() {
    WelcomeStartScreen()
}
