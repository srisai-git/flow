package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.data.session.SessionManager
import com.simats.focusflow.ui.components.CustomTopBar
import com.simats.focusflow.ui.components.bounceClick
import com.simats.focusflow.ui.theme.FocusFlowTheme

@Composable
fun ReadySetupCompleteScreen(
    onBack: () -> Unit = {},
    onGoToDashboard: () -> Unit = {},
    onEditPreferences: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = FocusFlowTheme.colors
    val sessionManager = remember { SessionManager.getInstance(context) }
    val userSession by sessionManager.userSessionFlow.collectAsState(initial = com.simats.focusflow.data.session.UserSession())

    val displayName = userSession.userName?.takeIf { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTopBar(
            title = "Setup complete",
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(36.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Color(0xFF10B981), style = Stroke(5.dp.toPx()))
                    val path = Path().apply {
                        moveTo(size.width * 0.33f, size.height * 0.5f)
                        lineTo(size.width * 0.45f, size.height * 0.62f)
                        lineTo(size.width * 0.67f, size.height * 0.38f)
                    }
                    drawPath(path, Color(0xFF10B981), style = Stroke(5.dp.toPx()))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = if (displayName != null) "You're all set, $displayName." else "You're all set.",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Start small. Make room for a focused day, at your own pace.",
            fontSize = 15.sp,
            color = colors.textMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(36.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Your Focus Routine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Text("Configured preferences", fontSize = 13.sp, color = colors.primaryLight)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                
                PreferenceRow(Icons.Default.MenuBook, "Primary goal", "Study")
                Spacer(modifier = Modifier.height(14.dp))
                PreferenceRow(Icons.Default.Timer, "Focus duration", "25 min")
                Spacer(modifier = Modifier.height(14.dp))
                PreferenceRow(Icons.Default.TrackChanges, "Daily focus goal", "2h 00m")
                Spacer(modifier = Modifier.height(14.dp))
                PreferenceRow(Icons.Default.Notifications, "Reminders", "Daily scheduled")
            }
        }
        
        Spacer(modifier = Modifier.height(36.dp))
        
        Button(
            onClick = onGoToDashboard,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Go to Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onEditPreferences,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(),
            border = BorderStroke(1.dp, colors.border),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
        ) {
            Text("Edit preferences", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PreferenceRow(icon: ImageVector, label: String, value: String) {
    val colors = FocusFlowTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colors.primaryLight, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(label, fontSize = 14.sp, color = colors.textMuted)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
    }
}
