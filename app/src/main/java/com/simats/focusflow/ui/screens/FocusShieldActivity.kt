package com.simats.focusflow.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.MainActivity
import com.simats.focusflow.service.FocusTimerService
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FOCUSFLOWTheme

class FocusShieldActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        renderContent()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        renderContent()
    }

    private fun renderContent() {
        val blockedApp = intent.getStringExtra("EXTRA_BLOCKED_APP") ?: "Distracting App"

        setContent {
            FOCUSFLOWTheme {
                FocusShieldContent(
                    blockedApp = blockedApp,
                    onReturnToFlow = {
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(mainIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun FocusShieldContent(
    blockedApp: String,
    onReturnToFlow: () -> Unit
) {
    BackHandler {
        onReturnToFlow()
    }

    val timerState by FocusTimerService.timerStateFlow.collectAsState()

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF07060D),
            Color(0xFF1A103C),
            Color(0xFF07060D)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFFEF4444).copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Focus Shield Active",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$blockedApp is blocked while Strict Focus is active.",
                fontSize = 15.sp,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timerState.currentMode.title.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA78BFA),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = timerState.formattedTime,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Countdown Remaining",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onReturnToFlow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = "Return to Flow",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
