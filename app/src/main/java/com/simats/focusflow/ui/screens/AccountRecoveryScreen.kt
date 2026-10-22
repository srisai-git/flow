package com.simats.focusflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.focusflow.ui.components.CustomTopBar

@Composable
fun AccountRecoveryScreen(onBack: () -> Unit = {}, onBackToLogin: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        CustomTopBar("Account recovery", "DEMO CONFIRMATION", onBack)
        
        Spacer(modifier = Modifier.weight(1f))
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(100.dp)) {
                drawCircle(Color(0xFF10B981), style = Stroke(4.dp.toPx()))
                val path = Path().apply {
                    moveTo(size.width * 0.3f, size.height * 0.5f)
                    lineTo(size.width * 0.45f, size.height * 0.65f)
                    lineTo(size.width * 0.7f, size.height * 0.35f)
                }
                drawPath(path, Color(0xFF10B981), style = Stroke(4.dp.toPx()))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Account recovery demo complete", 
            fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "You've reached the end of the simulated recovery journey.", 
            fontSize = 16.sp, color = Color(0xFF6B7280),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(24.dp)) {
            Text("No email was sent and no server password was changed. Return to Login to continue exploring the demo.", fontSize = 14.sp, color = Color(0xFF6B7280), lineHeight = 20.sp)
        }
        
        Spacer(modifier = Modifier.weight(2f))
        
        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7158E2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back to Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Static preview · no authentication service", color = Color(0xFF9CA3AF), fontSize = 12.sp)
        }
    }
}