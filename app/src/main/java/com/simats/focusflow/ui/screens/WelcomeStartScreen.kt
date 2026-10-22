package com.simats.focusflow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.AppLogo
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun WelcomeStartScreen(onBack: () -> Unit = {}, onCreateAccount: () -> Unit = {}, onLogin: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp).verticalScroll(rememberScrollState())
    ) {
        CustomTopBar(
            title = "Welcome to FocusFlow",
            showBackIcon = false
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppLogo(size = 36.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("FocusFlow AI", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Let's get started", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24))
        Spacer(modifier = Modifier.height(12.dp))
        Text("A little space for what matters. Build a focus routine that feels like you.", fontSize = 16.sp, color = Color(0xFF6B7280), lineHeight = 24.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Color(0xFFF0EDFF), style = Stroke(12.dp.toPx()))
                    drawArc(Color(0xFF7158E2), -90f, 180f, false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                }
                AppLogo(size = 90.dp)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("One thoughtful session at a time.", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0D9488), modifier = Modifier.align(Alignment.CenterHorizontally))
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onCreateAccount,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Create an account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7158E2))
        ) {
            Text("Already have an account? Log in", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Your focus records stay yours. Always.", fontSize = 12.sp, color = Color(0xFF6B7280), modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Demo only. No account is created and no sign-in takes place.", fontSize = 12.sp, color = Color(0xFF9CA3AF), modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}