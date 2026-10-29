package com.simats.focusflow.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.R
import com.simats.focusflow.data.session.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Branded dark splash screen inspecting DataStore session for cold-launch auto-routing.
 */
@Composable
fun SplashScreen(
    sessionManager: SessionManager? = null,
    onNavigate: (destination: String) -> Unit = {},
    onTimeout: () -> Unit = {}
) {
    val context = LocalContext.current
    val manager = remember { sessionManager ?: SessionManager.getInstance(context) }

    val isPreview = LocalInspectionMode.current
    val scaleAnim = remember { Animatable(if (isPreview) 1f else 0.6f) }
    val alphaAnim = remember { Animatable(if (isPreview) 1f else 0f) }

    LaunchedEffect(Unit) {
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }

        // Check persistent session in background concurrently
        val session = manager.userSessionFlow.first()
        delay(950) // Ensures minimum smooth display time to eliminate flickering

        if (session.isLoggedIn) {
            if (session.isOnboardingCompleted) {
                onNavigate("dashboard")
            } else {
                onNavigate("make_it_yours/${session.userId ?: 1}")
            }
        } else {
            onNavigate("welcome_start")
            onTimeout()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070510)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "FocusFlow Logo",
                modifier = Modifier
                    .size(130.dp)
                    .scale(scaleAnim.value * pulseScale)
                    .alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "FocusFlow",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Mindful routines • Deep focus",
                color = Color(0xFFC4B5FD),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onTimeout = {})
}
